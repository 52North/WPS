/*****************************************************************
Copyright � 2007 52�North Initiative for Geospatial Open Source Software GmbH

 Author: foerster

 Contact: Andreas Wytzisk, 
 52�North Initiative for Geospatial Open Source SoftwareGmbH, 
 Martin-Luther-King-Weg 24,
 48155 Muenster, Germany, 
 info@52north.org

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 version 2 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; even without the implied WARRANTY OF
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program (see gnu-gpl v2.txt). If not, write to
 the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA 02111-1307, USA or visit the Free
 Software Foundation�s web page, http://www.fsf.org.

 ***************************************************************/


package org.n52.wps.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

import net.opengis.ows.x11.DomainMetadataType;
import net.opengis.wps.x100.ComplexDataDescriptionType;
import net.opengis.wps.x100.ComplexDataType;
import net.opengis.wps.x100.DocumentOutputDefinitionType;
import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.InputReferenceType;
import net.opengis.wps.x100.InputType;
import net.opengis.wps.x100.LiteralDataType;
import net.opengis.wps.x100.OutputDefinitionType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ExecuteDocument.Execute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.wps.io.GeneratorFactory;
import org.n52.wps.io.IGenerator;
import org.n52.wps.io.IOHandler;
import org.n52.wps.io.data.IData;

/**
 * @author foerster
 * TODO: this does not handle referenced datasets
 */

public class ExecuteRequestBuilder {
	ProcessDescriptionType processDesc;
	ExecuteDocument execute;
	String SUPPORTED_VERSION = "1.0.0";
	
	private static Logger LOGGER = LoggerFactory.getLogger(ExecuteRequestBuilder.class);
	

	public ExecuteRequestBuilder(ProcessDescriptionType processDesc) {
		this.processDesc = processDesc;
		execute = ExecuteDocument.Factory.newInstance();
		Execute ex = execute.addNewExecute();
		ex.setService("WPS");
		ex.setVersion(SUPPORTED_VERSION);
		ex.addNewIdentifier().setStringValue(processDesc.getIdentifier().getStringValue());
		ex.addNewDataInputs();
	}
	
	public ExecuteRequestBuilder(ProcessDescriptionType processDesc, ExecuteDocument execute) {
		this.processDesc = processDesc;
		this.execute = execute;
	}
	
	/**
	 * add an input element. sets the data in the xml request
	 * 
	 * @param parameterID the ID of the input (see process description)
	 * @param value the actual value (for xml data xml for binary data is should be base64 encoded data)
	 * @param schema schema if applicable otherwise null
	 * @param encoding encoding if not the default encoding (for default encoding set it to null) (i.e. binary data, use base64)
	 * @param mimeType mimetype of the data, has to be set
	 * @throws WPSClientException
	 */
	public void addComplexData(String parameterID, IData value, String schema, String encoding, String mimeType) throws WPSClientException {
		GeneratorFactory fac = StaticDataHandlerRepository.getGeneratorFactory();
		InputDescriptionType inputDesc = getParameterDescription(parameterID);
		if (inputDesc == null) {
			throw new IllegalArgumentException("inputDesription is null for: " + parameterID);
		}
		if (inputDesc.getComplexData() == null) {
			throw new IllegalArgumentException("inputDescription is not of type ComplexData: " + parameterID);			
		}
		
			
		LOGGER.debug("Looking for matching Generator ..." + 
				" schema: " + schema +
				" mimeType: " + mimeType +
				" encoding: " + encoding);
		
		IGenerator generator = fac.getGenerator(schema, mimeType, encoding, value.getClass());
		
		if (generator == null) {
			// generator is still null
			throw new IllegalArgumentException("Could not find an appropriate generator for parameter: " + parameterID);
		}
		
		
		InputStream stream = null;  
				
			InputType input = execute.getExecute().getDataInputs().addNewInput();
			input.addNewIdentifier().setStringValue(inputDesc.getIdentifier().getStringValue());
			// encoding is UTF-8 (or nothing and we default to UTF-8)
			// everything that goes to this condition should be inline xml data
		try {
			
			if (encoding == null || encoding.equals("") || encoding.equalsIgnoreCase(IOHandler.DEFAULT_ENCODING)){
					stream = generator.generateStream(value, mimeType, schema);
					
			}else if(encoding.equalsIgnoreCase("base64")){
					stream = generator.generateBase64Stream(value, mimeType, schema);
			}else{
				throw new WPSClientException("Encoding not supported");
			}
					ComplexDataType data = input.addNewData().addNewComplexData();
					data.set(XmlObject.Factory.parse(stream));
					if (schema != null) {
						data.setSchema(schema);
					}
					if (mimeType != null) {
						data.setMimeType(mimeType);
					}
					if (encoding != null) {
						data.setEncoding(encoding);
					}
			}catch(XmlException e) {
					throw new IllegalArgumentException("error inserting node into execute request", e);
			} catch (IOException e) {
					throw new IllegalArgumentException("error reading generator output", e);
			}
			
	}

	/**
	 * Add literal data to the request
	 * @param parameterID the ID of the input paramter according to the describe process
	 * @param value the value. other types than strings have to be converted to string. The datatype is automatically determined and set accordingly to the process description
	 */
	public void addLiteralData(String parameterID, String value) {
		InputDescriptionType inputDesc = this.getParameterDescription(parameterID);
		if (inputDesc == null) {
			throw new IllegalArgumentException("inputDesription is null for: " + parameterID);
		}
		if (inputDesc.getLiteralData() == null) {
			throw new IllegalArgumentException("inputDescription is not of type literalData: " + parameterID);			
		}
		InputType input = execute.getExecute().getDataInputs().addNewInput();
		input.addNewIdentifier().setStringValue(parameterID);
		input.addNewData().addNewLiteralData().setStringValue(value);
		DomainMetadataType dataType = inputDesc.getLiteralData().getDataType();
		if (dataType != null) {
			input.getData().getLiteralData().setDataType(dataType.getReference());
		}
	}

/**
 * Sets a reference to input data
 * 
 * @param parameterID ID of the input element
 * @param value reference URL
 * @param schema schema if applicable otherwise null
 * @param encoding encoding if applicable (typically not), otherwise null
 * @param mimetype mimetype of the input according to the process description. has to be set
 */
	public void addComplexDataReference(String parameterID, String value, String schema, String encoding, String mimetype) {
		InputDescriptionType inputDesc = getParameterDescription(parameterID);
		if (inputDesc == null) {
			throw new IllegalArgumentException("inputDesription is null for: " + parameterID);
		}
		if (inputDesc.getComplexData() == null) {
			throw new IllegalArgumentException("inputDescription is not of type complexData: " + parameterID);
		}
			
		InputType input = execute.getExecute().getDataInputs().addNewInput();
		input.addNewIdentifier().setStringValue(parameterID);
		input.addNewReference().setHref(value);
		if (schema != null) {
			input.getReference().setSchema(schema);
		}

		if (encoding != null) {
			input.getReference().setEncoding(encoding);
		}
		if (mimetype != null) {
			input.getReference().setMimeType(mimetype);
		}
	}

	/**
	 * checks, if the execute, which has been build is valid according to the process description.
	 * @return
	 */
	public boolean isExecuteValid() {
		return true;
	}

	/**
	 * this sets store for the specific output.
	 * @param parentInput
	 * @return
	 */
	public boolean setStoreSupport(String outputName) {
		DocumentOutputDefinitionType outputDef = null;
		if (!execute.getExecute().isSetResponseForm()) {
			execute.getExecute().addNewResponseForm();
		}
		if (!execute.getExecute().getResponseForm().isSetResponseDocument()) {
			execute.getExecute().getResponseForm().addNewResponseDocument();
		}
		for(DocumentOutputDefinitionType outputDefTemp: execute.getExecute().getResponseForm().getResponseDocument().getOutputArray()) {
			if(outputDefTemp.getIdentifier().getStringValue().equals(outputName)) {
				outputDef = outputDefTemp;
				break;
			}
		}
		if (outputDef == null) {
			outputDef = execute.getExecute().getResponseForm()
					.getResponseDocument().addNewOutput();
		}
		for (OutputDescriptionType outputDesc : processDesc.getProcessOutputs().getOutputArray()) {
			if (outputDesc.getIdentifier().getStringValue().equals(outputName)) {
				outputDef.setIdentifier(outputDesc.getIdentifier());
				ComplexDataDescriptionType format = outputDesc.getComplexOutput().getDefault().getFormat();
				if (format.getMimeType() != null) {
					outputDef.setMimeType(format.getMimeType());
				}
				if (format.getEncoding() != null) {
					outputDef.setEncoding(format.getEncoding());
				}
				if (format.getSchema() != null) {
					outputDef.setSchema(format.getSchema());
				}
				outputDef.setAsReference(true);
			}
		}
		return true;
	}

	/**
	 * Set this if you want the data to a schema offered in the process description
	 * @param schema
	 * @param outputName
	 * @return
	 */
	public boolean setSchemaForOutput(String schema, String outputName) {
		if (!execute.getExecute().isSetResponseForm()) {
			execute.getExecute().addNewResponseForm();
		}
		if (!execute.getExecute().getResponseForm().isSetResponseDocument()) {
			execute.getExecute().getResponseForm().addNewResponseDocument();
		}
		OutputDescriptionType outputDesc = getOutputDescription(outputName);
		DocumentOutputDefinitionType outputDef = getOutputDefinition(outputName);
		if (outputDef == null) {
			outputDef = execute.getExecute().getResponseForm()
					.getResponseDocument().addNewOutput();
			outputDef.setIdentifier(outputDesc.getIdentifier());
		}
		String defaultSchema = outputDesc.getComplexOutput().getDefault()
				.getFormat().getSchema();
		if ((defaultSchema != null && defaultSchema.equals(schema))
				|| (defaultSchema == null && schema == null)) {
			outputDef.setSchema(schema);
			return true;
		} else {
			for (ComplexDataDescriptionType data : outputDesc
					.getComplexOutput().getSupported().getFormatArray()) {
				if (data.getSchema() != null && data.getSchema().equals(schema)) {
					outputDef.setSchema(schema);
					return true;
				} else if ((data.getSchema() == null && schema == null)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * sets the desired mimetype of the output. if not set, the default mimetype will be used as stated in the process description
	 * @param mimeType the name of the mimetype as announced in the processdescription
	 * @param outputName the Identifier of the output element
	 * @return success
	 */
	public boolean setMimeTypeForOutput(String mimeType, String outputName) {
		if (!execute.getExecute().isSetResponseForm()) {
			execute.getExecute().addNewResponseForm();
		}
		if (!execute.getExecute().getResponseForm().isSetResponseDocument()) {
			execute.getExecute().getResponseForm().addNewResponseDocument();
		}
		OutputDescriptionType outputDesc = getOutputDescription(outputName);
		DocumentOutputDefinitionType outputDef = getOutputDefinition(outputName);
		if (outputDef == null) {
			outputDef = execute.getExecute().getResponseForm()
					.getResponseDocument().addNewOutput();
			outputDef.setIdentifier(outputDesc.getIdentifier());
		}

		String defaultMimeType = outputDesc.getComplexOutput().getDefault()
				.getFormat().getMimeType();
		if (defaultMimeType == null) {
			defaultMimeType = "text/xml";
		}
		if (defaultMimeType.equals(mimeType)) {
//			outputDef.setMimeType(mimeType);//TODO: check
			return true;
		} else {
			for (ComplexDataDescriptionType data : outputDesc
					.getComplexOutput().getSupported().getFormatArray()) {
				String m = data.getMimeType();
				if (m != null && m.equals(mimeType)) {
					outputDef.setMimeType(mimeType);
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * sets the encoding. neccessary if data should not be retrieved in the default encoding (i.e. binary data in XML respones not raw data responses)
	 * @param encoding use base64
	 * @param outputName ID of the output
	 * @return
	 */
	public boolean setEncodingForOutput(String encoding, String outputName) {
		if (!execute.getExecute().isSetResponseForm()) {
			execute.getExecute().addNewResponseForm();
		}
		if (!execute.getExecute().getResponseForm().isSetResponseDocument()) {
			execute.getExecute().getResponseForm().addNewResponseDocument();
		}
		OutputDescriptionType outputDesc = getOutputDescription(outputName);
		DocumentOutputDefinitionType outputDef = getOutputDefinition(outputName);
		if (outputDef == null) {
			outputDef = execute.getExecute().getResponseForm()
					.getResponseDocument().addNewOutput();
			outputDef.setIdentifier(outputDesc.getIdentifier());
		}

		String defaultEncoding = outputDesc.getComplexOutput().getDefault()
				.getFormat().getEncoding();
		if (defaultEncoding == null) {
			defaultEncoding = IOHandler.DEFAULT_ENCODING;
		}
		if (defaultEncoding.equals(encoding)) {
			return true;
		} else {
			ComplexDataDescriptionType[] supportedFormats = outputDesc
					.getComplexOutput().getSupported().getFormatArray();
			for (ComplexDataDescriptionType data : supportedFormats) {
				String e = data.getEncoding();
				if (e != null && e.equals(encoding)) {
					outputDef.setEncoding(encoding);
					return true;
				}
			}
		}

		return false;
	}

	private OutputDescriptionType getOutputDescription(String outputName) {
		for (OutputDescriptionType outputDesc : processDesc.getProcessOutputs()
				.getOutputArray()) {
			if (outputDesc.getIdentifier().getStringValue().equals(outputName)) {
				return outputDesc;
			}
		}

		return null;
	}

	private DocumentOutputDefinitionType getOutputDefinition(String outputName) {
		DocumentOutputDefinitionType[] outputs = execute.getExecute()
				.getResponseForm().getResponseDocument().getOutputArray();
		for (DocumentOutputDefinitionType outputDef : outputs) {
			if (outputDef.getIdentifier().getStringValue().equals(outputName)) {
				return outputDef;
			}
		}

		return null;
	}

	/**
	 * Asks for data as raw data, i.e. without WPS XML wrapping
	 * @param schema if applicable otherwise null
	 * @param encoding if default encoding = null, otherwise base64
	 * @param mimeType requested mimetype of the output according to the process description. if not set, default mime type is used.
	 * @return
	 */
	public boolean setRawData(String schema, String encoding, String mimeType) {
		if (processDesc.getProcessOutputs().getOutputArray().length != 1) {
			return false;
		}
		OutputDefinitionType output = execute.getExecute().addNewResponseForm().addNewRawDataOutput();
		ComplexDataDescriptionType complexDesc = processDesc.getProcessOutputs().getOutputArray(0).getComplexOutput().getDefault().getFormat();
		output.setIdentifier(processDesc.getProcessOutputs().getOutputArray(0).getIdentifier());
		
		if (schema != null) {
			output.setSchema(schema);
		}
		if (mimeType != null) {
			output.setMimeType(mimeType);
		}
		if (encoding != null) {
			output.setEncoding(encoding);
		}
		return true;
	}

	/**
	 * XML representation of the created request.
	 * @return
	 */
	public ExecuteDocument getExecute() {
		return execute;
	}

	/**
	 * return a KVP representation for the created execute document.
	 * @return KVP request string
	 */
	public String getExecuteAsGETString() {
		String request = "?service=wps&request=execute&version=1.0.0&identifier=";
		request = request + processDesc.getIdentifier().getStringValue();
		request = request + "&DataInputs=";
		InputType[] inputs = execute.getExecute().getDataInputs().getInputArray();
		int inputCounter = 0;
		for(InputType input : inputs){
			
			request = request + input.getIdentifier().getStringValue();
			
			if(input.isSetReference()){
				//reference
				InputReferenceType reference = input.getReference();
				request = request + "="+"@xlink:href="+ URLEncoder.encode(reference.getHref());
				if(reference.isSetEncoding()){
					request = request + "@encoding="+reference.getEncoding();
				}
				if(reference.isSetMimeType()){
					request = request + "@format="+reference.getMimeType();
				}
				if(reference.isSetEncoding()){
					request = request + "@schema="+reference.getSchema();
				}
			}
			if(input.isSetData()){
				if(input.getData().isSetComplexData()){
					//complex
					ComplexDataType complexData = input.getData().getComplexData();
					request = request + "=" + URLEncoder.encode(input.getData().getComplexData().xmlText());
					if(complexData.isSetEncoding()){
						request = request + "@encoding="+complexData.getEncoding();
					}
					if(complexData.isSetMimeType()){
						request = request + "@format="+complexData.getMimeType();
					}
					if(complexData.isSetEncoding()){
						request = request + "@schema="+complexData.getSchema();
					}
				}
				if(input.getData().isSetLiteralData()){
					//literal
					LiteralDataType literalData = input.getData().getLiteralData();
					request = request + "=" + literalData.getStringValue();
					if(literalData.isSetDataType()){
						request = request + "@datatype="+literalData.getDataType();
					}
					if(literalData.isSetUom()){
						request = request + "@datatype="+literalData.getUom();
					}
				}
			}
			//concatenation for next input element
			inputCounter = inputCounter +1;
			if(inputCounter<inputs.length){
				request = request + ";";
			}
			
		}
		if(execute.getExecute().getResponseForm().getResponseDocument()==null){
			throw new RuntimeException("Responresponsedocument=se Form missing");
		}
		DocumentOutputDefinitionType[] outputs = execute.getExecute().getResponseForm().getResponseDocument().getOutputArray();
		int outputCounter = 0;
		if(execute.getExecute().getResponseForm().isSetRawDataOutput()){
			request = request + "&rawdataoutput=";
		}else{
			request = request + "&responsedocument=";
		}
		for(DocumentOutputDefinitionType output : outputs){
			request = request + output.getIdentifier().getStringValue();
			if(output.isSetEncoding()){
				request = request + "@encoding="+output.getEncoding();
			}
			if(output.isSetMimeType()){
				request = request + "@format="+output.getMimeType();
			}
			if(output.isSetEncoding()){
				request = request + "@schema="+output.getSchema();
			}
			if(output.isSetUom()){
				request = request + "@datatype="+output.getUom();
			}
			//concatenation for next output element
			outputCounter = outputCounter +1;
			if(outputCounter<outputs.length){
				request = request + ";";
			}
		}
		
		if( execute.getExecute().getResponseForm().getResponseDocument().isSetStoreExecuteResponse()){
			request = request + "&storeExecuteResponse=true";
		}
		if( execute.getExecute().getResponseForm().getResponseDocument().isSetStatus()){
			request = request + "&status=true";
		}
		if( execute.getExecute().getResponseForm().getResponseDocument().isSetLineage()){
			request = request + "&lineage=true";
		}
		
		return request;
	}

	
	/**
	 * 
	 * @param id
	 * @return the specified parameterdescription. if not available it returns null.
	 */
	private InputDescriptionType getParameterDescription(String id) {
		InputDescriptionType[] inputDescs = processDesc.getDataInputs().getInputArray();
		for (InputDescriptionType inputDesc : inputDescs) {
			if(inputDesc.getIdentifier().getStringValue().equals(id))
			{
				return inputDesc;
			}
		}
		return null;
	}
}
