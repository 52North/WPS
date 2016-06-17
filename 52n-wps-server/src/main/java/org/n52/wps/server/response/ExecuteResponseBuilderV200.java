/**
 * Copyright (C) 2007-2015 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *       • Apache License, version 2.0
 *       • Apache Software License, version 1.0
 *       • GNU Lesser General Public License, version 3
 *       • Mozilla Public License, versions 1.0, 1.1 and 2.0
 *       • Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.wps.server.response;

import java.io.InputStream;

import net.opengis.ows.x20.BoundingBoxType;
import net.opengis.ows.x20.DomainMetadataType;
import net.opengis.ows.x20.LanguageStringType;
import net.opengis.wps.x20.BoundingBoxDataDocument.BoundingBoxData;
import net.opengis.wps.x20.ComplexDataType;
import net.opengis.wps.x20.DataTransmissionModeType;
import net.opengis.wps.x20.ExecuteRequestType;
import net.opengis.wps.x20.FormatDocument.Format;
import net.opengis.wps.x20.LiteralDataType;
import net.opengis.wps.x20.OutputDefinitionType;
import net.opengis.wps.x20.OutputDescriptionType;
import net.opengis.wps.x20.ProcessOfferingDocument.ProcessOffering;
import net.opengis.wps.x20.ResultDocument;
import net.opengis.wps.x20.StatusInfoDocument;
import net.opengis.wps.x20.StatusInfoDocument.StatusInfo;

import org.apache.xmlbeans.XmlObject;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.data.IBBOXData;
import org.n52.wps.io.data.IData;
import org.n52.wps.server.RepositoryManagerSingletonWrapper;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.ProcessDescription;
import org.n52.wps.server.RepositoryManager;
import org.n52.wps.server.database.DatabaseFactory;
import org.n52.wps.server.request.ExecuteRequestV200;
import org.n52.wps.util.XMLBeansHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WPS Execute operation response. By default, this XML document is delivered to the client in response to an Execute request. If "status" is "false" in the Execute operation request, this document is normally returned when process execution has been completed.
 * If "status" in the Execute request is "true", this response shall be returned as soon as the Execute request has been accepted for processing. In this case, the same XML document is also made available as a web-accessible resource from the URL identified in the statusLocation, and the WPS server shall repopulate it once the process has completed. It may repopulate it on an ongoing basis while the process is executing.
 * However, the response to an Execute request will not include this element in the special case where the output is a single complex value result and the Execute request indicates that "store" is "false".
 * Instead, the server shall return the complex result (e.g., GIF image or GML) directly, without encoding it in the ExecuteResponse. If processing fails in this special case, the normal ExecuteResponse shall be sent, with the error condition indicated. This option is provided to simplify the programming required for simple clients and for service chaining.
 * @author Timon ter Braak, Benjamin Pross
 *
 */
public class ExecuteResponseBuilderV200 implements ExecuteResponseBuilder{

	private String identifier;
	private ExecuteRequestV200 request;
	private ResultDocument resultDoc;
	private StatusInfoDocument statusInfoDoc;
	private RawData rawDataHandler = null;
	private ProcessOffering description;
	private ProcessDescription superDescription;
	private static Logger LOGGER = LoggerFactory.getLogger(ExecuteResponseBuilderV200.class);

	public static enum Status {
		Accepted, Failed, Succeeded, Running
	}

	public ExecuteResponseBuilderV200(ExecuteRequestV200 request) throws ExceptionReport{
		this.request = request;
		resultDoc = ResultDocument.Factory.newInstance();
		resultDoc.addNewResult();
		resultDoc.getResult().setJobID(request.getUniqueId().toString());
		XMLBeansHelper.addSchemaLocationToXMLObject(resultDoc, "http://www.opengis.net/wps/2.0 http://schemas.opengis.net/wps/2.0/wps.xsd");
		statusInfoDoc = StatusInfoDocument.Factory.newInstance();
		statusInfoDoc.addNewStatusInfo();
		this.identifier = request.getAlgorithmIdentifier().trim();
		superDescription = RepositoryManagerSingletonWrapper.getInstance().getProcessDescription(this.identifier);
		description = (ProcessOffering) superDescription.getProcessDescriptionType(WPSConfig.VERSION_200);
		if(description==null){
			throw new RuntimeException("Error while accessing the process description for "+ identifier);
		}
	}

	public void update() throws ExceptionReport {

		// if status succeeded, update response with result
		if (statusInfoDoc.getStatusInfo().getStatus().equals(Status.Succeeded.toString())) {
			// the response only include dataInputs, if the property is set to true;
			// has the client specified the outputs?
				// Get the outputdescriptions from the algorithm

				OutputDescriptionType[] outputDescs = description.getProcess().getOutputArray();
				if(request.isRawData()) {
					//TODO check how this should be handled
					OutputDefinitionType rawDataOutput = request.getExecute().getOutputArray(0);
					String id = rawDataOutput.getId();
					OutputDescriptionType desc = XMLBeansHelper.findOutputByID(id, outputDescs);
					if(desc.getDataDescription() instanceof ComplexDataType) {
						String encoding = getEncoding(rawDataOutput);
						String schema = getSchema(rawDataOutput);
						String responseMimeType = getMimeType(rawDataOutput);
						generateComplexDataOutput(id, false, true, schema, responseMimeType, encoding, null);
					}

					else if (desc.getDataDescription() instanceof LiteralDataType) {
						String mimeType = null;
						String schema = null;
						String encoding = null;

						LiteralDataType literalDataType = (LiteralDataType)desc.getDataDescription();

						DomainMetadataType dataType = literalDataType.getLiteralDataDomainArray(0).getDataType();
						String reference = dataType != null ? dataType.getReference() : null;
						generateLiteralDataOutput(id, resultDoc, true, reference, schema, mimeType, encoding, desc.getTitleArray(0));
					}
					else if (desc.getDataDescription() instanceof BoundingBoxData) {
						generateBBOXOutput(id, resultDoc, true, desc.getTitleArray(0));
					}
					return;
				}
				// Get the outputdefinitions from the clients request
				// For each request of output
				for(int i = 0; i<request.getExecute().getOutputArray().length; i++) {
					OutputDefinitionType definition = request.getExecute().getOutputArray(i);
					String responseID = definition.getId();
					OutputDescriptionType desc = XMLBeansHelper.findOutputByID(responseID, outputDescs);
					if(desc==null){
						throw new ExceptionReport("Could not find the output id " + responseID, ExceptionReport.INVALID_PARAMETER_VALUE);
					}
					if(desc.getDataDescription() instanceof ComplexDataType) {
						String mimeType = getMimeType(definition);
						String schema = getSchema(definition);
						String encoding = getEncoding(definition);

						generateComplexDataOutput(responseID, definition.getTransmission().equals(DataTransmissionModeType.REFERENCE), false,  schema, mimeType, encoding, desc.getTitleArray(0));
					}
					else if (desc.getDataDescription() instanceof LiteralDataType) {
						String mimeType = null;
						String schema = null;
						String encoding = null;

						LiteralDataType literalDataType = (LiteralDataType)desc.getDataDescription();

						DomainMetadataType dataType = literalDataType.getLiteralDataDomainArray(0).getDataType();
						String reference = dataType != null ? dataType.getReference() : null;
						generateLiteralDataOutput(responseID, resultDoc, false, reference, schema, mimeType, encoding, desc.getTitleArray(0));
					}
					else if (desc.getDataDescription() instanceof BoundingBoxData) {
						generateBBOXOutput(responseID, resultDoc, false, desc.getTitleArray(0));
					}
					else{
						throw new ExceptionReport("Requested type not supported: BBOX", ExceptionReport.INVALID_PARAMETER_VALUE);
					}
				}
		}
	}

	/**
	 * Returns the schema according to the given output description and type.
	 */
	private static String getSchema(OutputDefinitionType def) {
		String schema = null;
		if(def != null) {
			schema = def.getSchema();
		}

		return schema;
	}

	private static String getEncoding(OutputDefinitionType def) {
		String encoding = null;
		if(def != null) {
			encoding = def.getEncoding();
		}
		return encoding;
	}

	public String getMimeType() {
		return getMimeType(null);
	}

	public String getMimeType(XmlObject definitionObject) {

		String mimeType = "";

		if (definitionObject instanceof OutputDefinitionType) {

			OutputDefinitionType def = (OutputDefinitionType) definitionObject;

			mimeType = def.getMimeType();
			OutputDescriptionType[] outputDescs = description.getProcess()
					.getOutputArray();

			String inputID = "";

			if (def != null) {
				inputID = def.getId();
			}

			OutputDescriptionType outputDes = null;

			for (OutputDescriptionType tmpOutputDes : outputDescs) {
				if (inputID.equalsIgnoreCase(tmpOutputDes.getIdentifier()
						.getStringValue())) {
					outputDes = tmpOutputDes;
					break;
				}
			}

			// use default mime type
			if (mimeType == null) {

				Format[] formats = outputDes.getDataDescription()
						.getFormatArray();

				for (Format format : formats) {
					if (format.isSetDefault()) {
						mimeType = format.getMimeType();
						break;
					}
				}

				LOGGER.warn("Using default mime type: " + mimeType
						+ " for output: " + inputID);
			}
		}

		return mimeType;
	}

	private void generateComplexDataOutput(String responseID, boolean asReference, boolean rawData, String schema, String mimeType, String encoding, LanguageStringType title) throws ExceptionReport{
		IData obj = request.getAttachedResult().get(responseID);
		if(rawData) {
			rawDataHandler = new RawData(obj, responseID, schema, encoding, mimeType, this.identifier, superDescription);
		}
		else {
			OutputDataItem handler = new OutputDataItem(obj, responseID, schema, encoding, mimeType, title, this.identifier, superDescription);
			if(asReference) {
				handler.updateResponseAsReference(resultDoc, (request.getUniqueId()).toString(),mimeType);
			}
			else {
				handler.updateResponseForInlineComplexData(resultDoc);
			}
		}

	}

	private void generateLiteralDataOutput(String responseID, ResultDocument res, boolean rawData, String dataTypeReference, String schema, String mimeType, String encoding, LanguageStringType title) throws ExceptionReport {
		IData obj = request.getAttachedResult().get(responseID);
		if(rawData) {
			rawDataHandler = new RawData(obj, responseID, schema, encoding, mimeType, this.identifier, superDescription);
		}else{
			OutputDataItem handler = new OutputDataItem(obj, responseID, schema, encoding, mimeType, title, this.identifier, superDescription);
			handler.updateResponseForLiteralData(res, dataTypeReference);
		}
	}

	private void generateBBOXOutput(String responseID, ResultDocument res, boolean rawData, LanguageStringType title) throws ExceptionReport {
        IBBOXData obj = (IBBOXData) request.getAttachedResult().get(responseID);
		if(rawData) {
			rawDataHandler = new RawData(obj, responseID, null, null, null, this.identifier, superDescription);
		}else{
			OutputDataItem handler = new OutputDataItem(obj, responseID, null, null, null, title, this.identifier, superDescription);
			handler.updateResponseForBBOXData(res, obj);
		}

	}

	public InputStream getAsStream() throws ExceptionReport{
		if(request.isRawData() && rawDataHandler != null) {
			return rawDataHandler.getAsStream();
		}

		if(request.getExecute().getMode().equals(ExecuteRequestType.Mode.SYNC)){
			return resultDoc.newInputStream(XMLBeansHelper.getXmlOptions());
		}else if(statusInfoDoc.getStatusInfo().getStatus().equals(Status.Succeeded.toString())){
			//save last status info and return result document
		        XMLBeansHelper.addSchemaLocationToXMLObject(statusInfoDoc, "http://www.opengis.net/wps/2.0 http://schemas.opengis.net/wps/2.0/wps.xsd");
			DatabaseFactory.getDatabase().insertResponse(
					request.getUniqueId().toString(), statusInfoDoc.newInputStream(XMLBeansHelper.getXmlOptions()));
			return resultDoc.newInputStream(XMLBeansHelper.getXmlOptions());
		}             
		XMLBeansHelper.addSchemaLocationToXMLObject(statusInfoDoc, "http://www.opengis.net/wps/2.0 http://schemas.opengis.net/wps/2.0/wps.xsd");
		return statusInfoDoc.newInputStream(XMLBeansHelper.getXmlOptions());
	}

	public void setStatus(XmlObject statusObject) {

		if(statusObject instanceof StatusInfo){

			StatusInfo status = (StatusInfo)statusObject;

			statusInfoDoc.setStatusInfo(status);
		}else{
			LOGGER.warn(String.format("XMLObject not of type \"net.opengis.wps.x20.StatusInfoDocument.StatusInfo\", but {}. Cannot not set status. ", statusObject.getClass()));
		}
	}

}

