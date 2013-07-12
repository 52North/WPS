/***************************************************************
 This implementation provides a framework to publish processes to the
web through the  OGC Web Processing Service interface. The framework 
is extensible in terms of processes and data handlers. It is compliant 
to the WPS version 0.4.0 (OGC 05-007r4). 

 Copyright (C) 2006 by con terra GmbH

 Authors: 
	Theodor Foerster, ITC, Enschede, the Netherlands
	Carsten Priess, Institute for geoinformatics, University of
Muenster, Germany
	Timon Ter Braak, University of Twente, the Netherlands
	Bastian Schaeffer, Institute for geoinformatics, University of Muenster, Germany


 Contact: Albert Remke, con terra GmbH, Martin-Luther-King-Weg 24,
 48155 Muenster, Germany, 52n@conterra.de

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 version 2 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program (see gnu-gpl v2.txt); if not, write to
 the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA  02111-1307, USA or visit the web page of the Free
 Software Foundation, http://www.fsf.org.

 Created on: 13.06.2006
 ***************************************************************/
package org.n52.wps.server.response;

import java.io.InputStream;
import java.util.Calendar;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import net.opengis.ows.x11.DomainMetadataType;
import net.opengis.ows.x11.LanguageStringType;
import net.opengis.wps.x100.DataInputsType;
import net.opengis.wps.x100.DocumentOutputDefinitionType;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.ExecuteResponseDocument.ExecuteResponse;
import net.opengis.wps.x100.OutputDefinitionType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.StatusType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.data.IData;
import org.n52.wps.server.CapabilitiesConfiguration;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.RepositoryManager;
import org.n52.wps.server.WebProcessingService;
import org.n52.wps.server.database.DatabaseFactory;
import org.n52.wps.server.request.ExecuteRequest;
import org.n52.wps.server.request.Request;
import org.n52.wps.util.XMLBeansHelper;

/**
 * WPS Execute operation response. By default, this XML document is delivered to the client in response to an Execute request. If "status" is "false" in the Execute operation request, this document is normally returned when process execution has been completed.
 * If "status" in the Execute request is "true", this response shall be returned as soon as the Execute request has been accepted for processing. In this case, the same XML document is also made available as a web-accessible resource from the URL identified in the statusLocation, and the WPS server shall repopulate it once the process has completed. It may repopulate it on an ongoing basis while the process is executing.
 * However, the response to an Execute request will not include this element in the special case where the output is a single complex value result and the Execute request indicates that "store" is "false".
 * Instead, the server shall return the complex result (e.g., GIF image or GML) directly, without encoding it in the ExecuteResponse. If processing fails in this special case, the normal ExecuteResponse shall be sent, with the error condition indicated. This option is provided to simplify the programming required for simple clients and for service chaining.
 * @author Timon ter Braak
 *
 */
public class ExecuteResponseBuilder {

	private String identifier;
	private DataInputsType dataInputs;
	//private DocumentOutputDefinitionType[] outputDefs;
	private ExecuteRequest request;	
	private ExecuteResponseDocument doc;
	private RawData rawDataHandler = null; 
	private ProcessDescriptionType description;
	private static Logger LOGGER = LoggerFactory.getLogger(ExecuteResponseBuilder.class);
	private Calendar creationTime;
	
	public ExecuteResponseBuilder(ExecuteRequest request) throws ExceptionReport{
		this.request = request;
		doc = ExecuteResponseDocument.Factory.newInstance();
		doc.addNewExecuteResponse();
		XmlCursor c = doc.newCursor();
		c.toFirstChild();
		c.toLastAttribute();
		c.setAttributeText(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation"), "http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsExecute_response.xsd");
		doc.getExecuteResponse().setServiceInstance(CapabilitiesConfiguration.ENDPOINT_URL+"?REQUEST=GetCapabilities&SERVICE=WPS");
		doc.getExecuteResponse().setLang(WebProcessingService.DEFAULT_LANGUAGE);
		doc.getExecuteResponse().setService("WPS");
		doc.getExecuteResponse().setVersion(Request.SUPPORTED_VERSION);
		this.identifier = request.getExecute().getIdentifier().getStringValue().trim();
		ExecuteResponse responseElem = doc.getExecuteResponse();
		responseElem.addNewProcess().addNewIdentifier().setStringValue(identifier);
		description = RepositoryManager.getInstance().getProcessDescription(this.identifier);
		if(description==null){
			throw new RuntimeException("Error while accessing the process description for "+ identifier);
		}
		responseElem.getProcess().setTitle(description.getTitle());
		responseElem.getProcess().setProcessVersion(description.getProcessVersion());
		creationTime = Calendar.getInstance();
	}
	
	public void update() throws ExceptionReport {
		// copying the request parameters to the response
		ExecuteResponse responseElem = doc.getExecuteResponse();
				
		// if status succeeded, update reponse with result
		if (responseElem.getStatus().isSetProcessSucceeded()) {
			// the response only include dataInputs, if the property is set to true;
			//if(Boolean.getBoolean(WPSConfiguration.getInstance().getProperty(WebProcessingService.PROPERTY_NAME_INCLUDE_DATAINPUTS_IN_RESPONSE))) {
			if(new Boolean(WPSConfig.getInstance().getWPSConfig().getServer().getIncludeDataInputsInResponse())){
				dataInputs = request.getExecute().getDataInputs();
				responseElem.setDataInputs(dataInputs);
			}
			responseElem.addNewProcessOutputs();
			// has the client specified the outputs?
			if (request.getExecute().isSetResponseForm()) {
				// Get the outputdescriptions from the algorithm

				OutputDescriptionType[] outputDescs = description.getProcessOutputs().getOutputArray();
				if(request.isRawData()) {
					OutputDefinitionType rawDataOutput = request.getExecute().getResponseForm().getRawDataOutput();
					String id = rawDataOutput.getIdentifier().getStringValue();
					OutputDescriptionType desc = XMLBeansHelper.findOutputByID(id, outputDescs);
					if(desc.isSetComplexOutput()) {
						String encoding = ExecuteResponseBuilder.getEncoding(desc, rawDataOutput);
						String schema = ExecuteResponseBuilder.getSchema(desc, rawDataOutput);
						String responseMimeType = getMimeType(rawDataOutput);
						generateComplexDataOutput(id, false, true, schema, responseMimeType, encoding, null);
					}

					else if (desc.isSetLiteralOutput()) {
						String mimeType = null;
						String schema = null;
						String encoding = null;
						DomainMetadataType dataType = desc.getLiteralOutput().getDataType();
						String reference = dataType != null ? dataType.getReference() : null;
						generateLiteralDataOutput(id, doc, true, reference, schema, mimeType, encoding, desc.getTitle());
					}
					else if (desc.isSetBoundingBoxOutput()) {
						generateBBOXOutput(id, doc, true, desc.getTitle());
					}
					return;
				}
				// Get the outputdefinitions from the clients request
				// For each request of output
				for(int i = 0; i<request.getExecute().getResponseForm().getResponseDocument().getOutputArray().length; i++) {
					OutputDefinitionType definition = request.getExecute().getResponseForm().getResponseDocument().getOutputArray(i);
					DocumentOutputDefinitionType documentDef = request.getExecute().getResponseForm().getResponseDocument().getOutputArray(i);
					String responseID = definition.getIdentifier().getStringValue();
					OutputDescriptionType desc = XMLBeansHelper.findOutputByID(responseID, outputDescs);
					if(desc==null){
						throw new ExceptionReport("Could not find the output id " + responseID, ExceptionReport.INVALID_PARAMETER_VALUE);
					}
					if(desc.isSetComplexOutput()) {
						String mimeType = getMimeType(definition);
						String schema = ExecuteResponseBuilder.getSchema(desc, definition);
						String encoding = ExecuteResponseBuilder.getEncoding(desc, definition);
						generateComplexDataOutput(responseID, documentDef.getAsReference(), false,  schema, mimeType, encoding, desc.getTitle());
					}
					else if (desc.isSetLiteralOutput()) {
						String mimeType = null;
						String schema = null;
						String encoding = null;
						DomainMetadataType dataType = desc.getLiteralOutput().getDataType();
						String reference = dataType != null ? dataType.getReference() : null;
						generateLiteralDataOutput(responseID, doc, false, reference, schema, mimeType, encoding, desc.getTitle());
					}
					else if (desc.isSetBoundingBoxOutput()) {
						generateBBOXOutput(responseID, doc, false, desc.getTitle());
					}
					else{
						throw new ExceptionReport("Requested type not supported: BBOX", ExceptionReport.INVALID_PARAMETER_VALUE);
					}
				}
			}
			else {
				LOGGER.info("OutputDefinitions are not stated explicitly in request");

				// THIS IS A WORKAROUND AND ACTUALLY NOT COMPLIANT TO THE SPEC.	

				ProcessDescriptionType description = RepositoryManager.getInstance().getProcessDescription(request.getExecute().getIdentifier().getStringValue());
				if(description==null){
					throw new RuntimeException("Error while accessing the process description for "+ request.getExecute().getIdentifier().getStringValue());
				}

				OutputDescriptionType [] d = description.getProcessOutputs().getOutputArray();
				for (int i = 0; i < d.length; i++)
				{
					if(d[i].isSetComplexOutput()) {
						String schema = d[i].getComplexOutput().getDefault().getFormat().getSchema();
						String encoding = d[i].getComplexOutput().getDefault().getFormat().getEncoding();
						String mimeType = d[i].getComplexOutput().getDefault().getFormat().getMimeType();
						generateComplexDataOutput(d[i].getIdentifier().getStringValue(), false, false, schema, mimeType, encoding, d[i].getTitle());
					}
					else if(d[i].isSetLiteralOutput()) {
						generateLiteralDataOutput(d[i].getIdentifier().getStringValue(), doc, false, d[i].getLiteralOutput().getDataType().getReference(), null, null, null, d[i].getTitle());
					}
				}
			}
		} else if(request.isStoreResponse()) {
			responseElem.setStatusLocation(DatabaseFactory.getDatabase().generateRetrieveResultURL((request.getUniqueId()).toString()));
		}
	}

	

	/** 
	 * Returns the schema according to the given output description and type.
	 */
	private static String getSchema(OutputDescriptionType desc, OutputDefinitionType def) {
		String schema = null;
		if(def != null) {
			schema = def.getSchema();
		}
		
		return schema;
	}	

	private static String getEncoding(OutputDescriptionType desc, OutputDefinitionType def) {
		String encoding = null;
		if(def != null) {
			encoding = def.getEncoding();
		}
		return encoding;
	}
	
	public String getMimeType() {
		return getMimeType(null);
	}
	
	public String getMimeType(OutputDefinitionType def) {
		
		String mimeType = "";
		OutputDescriptionType[] outputDescs = description.getProcessOutputs()
				.getOutputArray();

		boolean isResponseForm = request.getExecute().isSetResponseForm();

		String inputID = "";

		if(def != null){
			inputID = def.getIdentifier().getStringValue();
		}else if(isResponseForm){
			
			if (request.getExecute().getResponseForm().isSetRawDataOutput()) {
				inputID = request.getExecute().getResponseForm().getRawDataOutput()
						.getIdentifier().getStringValue();
			} else if (request.getExecute().getResponseForm()
							.isSetResponseDocument()) {
				inputID = request.getExecute().getResponseForm()
						.getResponseDocument().getOutputArray(0).getIdentifier()
						.getStringValue();
			}
		}

		OutputDescriptionType outputDes = null;

		for (OutputDescriptionType tmpOutputDes : outputDescs) {
			if (inputID.equalsIgnoreCase(tmpOutputDes.getIdentifier()
					.getStringValue())) {
				outputDes = tmpOutputDes;
				break;
			}
		}

		if (isResponseForm) {
			// Get the outputdescriptions from the algorithm
			if (request.isRawData()) {
				mimeType = request.getExecute().getResponseForm()
						.getRawDataOutput().getMimeType();
			} else {
				// mimeType = "text/xml";
				// MSS 03/02/2009 defaulting to text/xml doesn't work when the
				// data is a complex raster
				if (outputDes.isSetLiteralOutput()
						|| outputDes.isSetBoundingBoxOutput()) {
					mimeType = "text/plain";
				} else {
					if (def != null) {
						mimeType = def.getMimeType();
					} else {
						if (outputDes.isSetComplexOutput()) {
							mimeType = outputDes.getComplexOutput()
									.getDefault().getFormat().getMimeType();
							LOGGER.warn("Using default mime type: "
									+ mimeType
									+ " for input: "
									+ inputID);
						}
					}
				}
			}
		}
		if (mimeType == null) {
			if (outputDes.isSetLiteralOutput()
					|| outputDes.isSetBoundingBoxOutput()) {
				mimeType = "text/plain";
			} else if (outputDes.isSetComplexOutput()) {
				mimeType = outputDes.getComplexOutput().getDefault()
						.getFormat().getMimeType();
				LOGGER.warn("Using default mime type: " + mimeType
						+ " for input: "
						+ inputID);
			}
		}

		return mimeType;
	}

	private void generateComplexDataOutput(String responseID, boolean asReference, boolean rawData, String schema, String mimeType, String encoding, LanguageStringType title) throws ExceptionReport{
		IData obj = request.getAttachedResult().get(responseID);
		if(rawData) {
			rawDataHandler = new RawData(obj, responseID, schema, encoding, mimeType, this.identifier, description);
		}
		else {
			OutputDataItem handler = new OutputDataItem(obj, responseID, schema, encoding, mimeType, title, this.identifier, description);
			if(asReference) {
				handler.updateResponseAsReference(doc, (request.getUniqueId()).toString(),mimeType);
			}
			else {
				handler.updateResponseForInlineComplexData(doc);
			}
		}
		
	}

	private void generateLiteralDataOutput(String responseID, ExecuteResponseDocument res, boolean rawData, String dataTypeReference, String schema, String mimeType, String encoding, LanguageStringType title) throws ExceptionReport {
		IData obj = request.getAttachedResult().get(responseID);
		if(rawData) {
			rawDataHandler = new RawData(obj, responseID, schema, encoding, mimeType, this.identifier, description);
		}else{
			OutputDataItem handler = new OutputDataItem(obj, responseID, schema, encoding, mimeType, title, this.identifier, description);
			handler.updateResponseForLiteralData(res, dataTypeReference);
		}
	}
	
	private void generateBBOXOutput(String responseID, ExecuteResponseDocument res, boolean rawData, LanguageStringType title) throws ExceptionReport {
		IData obj = request.getAttachedResult().get(responseID);
		if(rawData) {
			rawDataHandler = new RawData(obj, responseID, null, null, null, this.identifier, description);
		}else{
			OutputDataItem handler = new OutputDataItem(obj, responseID, null, null, null, title, this.identifier, description);
			handler.updateResponseForBBOXData(res, obj);
		}
		
	}

	public InputStream getAsStream() throws ExceptionReport{
		if(request.isRawData() && rawDataHandler != null) {
			return rawDataHandler.getAsStream();
		}
		if(request.isStoreResponse()) {
			doc.getExecuteResponse().setStatusLocation(DatabaseFactory.getDatabase().generateRetrieveResultURL((request.getUniqueId()).toString()));
		}
		try {
			//Forces XMLBeans to write the namespaces in front of all other attributes. Otherwise the xml is not valid
			XmlOptions opts = new XmlOptions();
			opts.setSaveNamespacesFirst();
			return doc.newInputStream(opts);
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void setStatus(StatusType status) {
		//workaround, should be generated either at the creation of the document or when the process has been finished.
		status.setCreationTime(creationTime);
		doc.getExecuteResponse().setStatus(status);
	}

}

