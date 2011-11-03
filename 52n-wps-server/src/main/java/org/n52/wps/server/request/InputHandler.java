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
	Matthias Mueller, TU Dresden


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
package org.n52.wps.server.request;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.opengis.ows.x11.DomainMetadataType;
import net.opengis.ows.x11.RangeType;
import net.opengis.ows.x11.ValueType;
import net.opengis.wps.x100.ComplexDataDescriptionType;
import net.opengis.wps.x100.ComplexDataType;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.InputReferenceType;
import net.opengis.wps.x100.InputType;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.log4j.Logger;
import org.n52.wps.io.IOHandler;
import org.n52.wps.io.IParser;
import org.n52.wps.io.ParserFactory;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.bbox.GTReferenceEnvelope;
import org.n52.wps.io.data.binding.literal.LiteralByteBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralFloatBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralLongBinding;
import org.n52.wps.io.data.binding.literal.LiteralShortBinding;
import org.n52.wps.io.datahandler.parser.GML2BasicParser;
import org.n52.wps.io.datahandler.parser.GML3BasicParser;
import org.n52.wps.io.datahandler.parser.SimpleGMLParser;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.RepositoryManager;
import org.n52.wps.server.request.strategy.ReferenceStrategyRegister;
import org.n52.wps.util.BasicXMLTypeFactory;
import org.w3c.dom.Node;

/**
 * Handles the input of the client and stores it into a Map.
 */
public class InputHandler {

	private static Logger LOGGER = Logger.getLogger(InputHandler.class);
	private Map<String, List<IData>> inputData = new HashMap<String, List<IData>>();
	private ProcessDescriptionType processDesc;
	private String algorithmIdentifier = null; // Needed to take care of handling a conflict between different parsers.
	/**
	 * Initializes a parser that handles each (line of) input based on the type of input.
	 * @see #handleComplexData(IOValueType)
	 * @see #handleComplexValueReference(IOValueType)
	 * @see #handleLiteralData(IOValueType)
	 * @see #handleBBoxValue(IOValueType)
	 * @param inputs The client input
	 */
	public InputHandler(InputType[] inputs, String algorithmIdentifier) throws ExceptionReport{
		this.algorithmIdentifier = algorithmIdentifier;
		this.processDesc = RepositoryManager.getInstance().getProcessDescription(algorithmIdentifier);
		for(InputType input : inputs) {
			String inputID = input.getIdentifier().getStringValue();
			
			if(input.getData() != null) {
				if(input.getData().getComplexData() != null) {
					handleComplexData(input);
				}
				else if(input.getData().getLiteralData() != null) {
					handleLiteralData(input);
				}
				else if(input.getData().getBoundingBoxData() != null) {
					handleBBoxValue(input);
				}
			}
			else if(input.getReference() != null) {
				handleComplexValueReference(input);
			}
			else {
				throw new ExceptionReport("Error while accessing the inputValue: " + inputID, 
						ExceptionReport.INVALID_PARAMETER_VALUE);
			}
		}
	}
	
	/**
	 * Handles the complexValue, which in this case should always include XML 
	 * which can be parsed into a FeatureCollection.
	 * @param input The client input
	 * @throws ExceptionReport If error occured while parsing XML
	 */
	private void handleComplexData(InputType input) throws ExceptionReport{
		String inputID = input.getIdentifier().getStringValue();
		
		Node complexValueNode = input.getData().getComplexData().getDomNode();
		
		String complexValue = "";
		try {
			complexValue = nodeToString(complexValueNode);
			//remove complexvalue element. getFirstChild
			complexValue = complexValue.substring(complexValue.indexOf(">")+1,complexValue.lastIndexOf("</"));
		} catch (TransformerFactoryConfigurationError e1) {
			throw new RuntimeException("Could not parse inline data. Reason " +e1);
		} catch (TransformerException e1) {
			throw new RuntimeException("Could not parse inline data. Reason " +e1);
		}
		InputDescriptionType inputReferenceDesc = null;
		for(InputDescriptionType tempDesc : this.processDesc.getDataInputs().getInputArray()) {
			if(inputID.equals(tempDesc.getIdentifier().getStringValue())) {
				inputReferenceDesc = tempDesc;
				break;
			}
		}

		if(inputReferenceDesc == null) {
			LOGGER.debug("input cannot be found in description for " + processDesc.getIdentifier().getStringValue() + "," + inputID);
		}
		
//select parser
		
		//1. mimeType set?
		//yes--> set it
			//1.1 schema/encoding set?
			//yes-->set it
			//not-->set default values for parser with matching mime type
		
		//no--> schema or/and encoding are set?
					//yes-->use it, look if only one mime type can be found
					//not-->use default values
			
			
		
		String schema = null;
		String mimeType = null;
		String encoding = null;
		
		// overwrite with data format from request if appropriate
		ComplexDataType data = input.getData().getComplexData();
		
		if (data.isSetMimeType() && data.getMimeType() != null){
			//mime type in request
			mimeType = data.getMimeType();
			ComplexDataDescriptionType format = null;
			
			String defaultMimeType = inputReferenceDesc.getComplexData().getDefault().getFormat().getMimeType();
			
			
			boolean canUseDefault = false;
			if(defaultMimeType.equalsIgnoreCase(mimeType)){
				ComplexDataDescriptionType potenitalFormat = inputReferenceDesc.getComplexData().getDefault().getFormat();
				if(data.getSchema() != null && data.getEncoding() == null){
					if(data.getSchema().equalsIgnoreCase(potenitalFormat.getSchema())){
						canUseDefault = true;
						format = potenitalFormat;
					}
				}
				if(data.getSchema() == null && data.getEncoding() != null){
					if(data.getEncoding().equalsIgnoreCase(potenitalFormat.getEncoding())){
						canUseDefault = true;
						format = potenitalFormat;
					}
					
				}
				if(data.getSchema() != null && data.getEncoding() != null){
					if(data.getSchema().equalsIgnoreCase(potenitalFormat.getSchema()) && data.getEncoding().equalsIgnoreCase(potenitalFormat.getEncoding())){
						canUseDefault = true;
						format = potenitalFormat;
					}
					
				}
				if(data.getSchema() == null && data.getEncoding() == null){
					canUseDefault = true;
					format = potenitalFormat;
				}
				
			}
			if(!canUseDefault){
				 ComplexDataDescriptionType[] formats = inputReferenceDesc.getComplexData().getSupported().getFormatArray();
				 for(ComplexDataDescriptionType potenitalFormat : formats){
					 if(potenitalFormat.getMimeType().equalsIgnoreCase(mimeType)){
						 if(data.getSchema() != null && data.getEncoding() == null){
								if(data.getSchema().equalsIgnoreCase(potenitalFormat.getSchema())){
									format = potenitalFormat;
								}
							}
							if(data.getSchema() == null && data.getEncoding() != null){
								if(data.getEncoding().equalsIgnoreCase(potenitalFormat.getEncoding())){
									format = potenitalFormat;
								}
								
							}
							if(data.getSchema() != null && data.getEncoding() != null){
								if(data.getSchema().equalsIgnoreCase(potenitalFormat.getSchema()) && data.getEncoding().equalsIgnoreCase(potenitalFormat.getEncoding())){
									format = potenitalFormat;
								}
								
							}
							if(data.getSchema() == null && data.getEncoding() == null){
								format = potenitalFormat;
							}
					 }
				 }
			}
			if(format == null){
				throw new ExceptionReport("Could not determine output format", ExceptionReport.INVALID_PARAMETER_VALUE);
			}
			
			mimeType = format.getMimeType();
			
			if(format.isSetEncoding()){
				//no encoding provided--> select default one for mimeType
				encoding = format.getEncoding();
			}
			
			if(format.isSetSchema()){
				//no encoding provided--> select default one for mimeType
				schema = format.getSchema();
			}
			
		}else{
			//mimeType not in request
			
			if(mimeType==null && !data.isSetEncoding() && !data.isSetSchema()){
					//nothing set, use default values
					schema = inputReferenceDesc.getComplexData().getDefault().getFormat().getSchema();
					mimeType = inputReferenceDesc.getComplexData().getDefault().getFormat().getMimeType();
					encoding = inputReferenceDesc.getComplexData().getDefault().getFormat().getEncoding();
				
			}else{
					//do a smart search an look if a mimeType can be found for either schema and/or encoding
					
				if(mimeType==null){	
					if(data.isSetEncoding() && !data.isSetSchema()){
							//encoding set only
							ComplexDataDescriptionType encodingFormat = null;
							String defaultEncoding = inputReferenceDesc.getComplexData().getDefault().getFormat().getEncoding();
							int found = 0;
							String foundEncoding = null;
							if(defaultEncoding.equalsIgnoreCase(data.getEncoding())){
								foundEncoding = inputReferenceDesc.getComplexData().getDefault().getFormat().getEncoding();
								encodingFormat = inputReferenceDesc.getComplexData().getDefault().getFormat();
								found = found +1;
							}else{
								 ComplexDataDescriptionType[] formats = inputReferenceDesc.getComplexData().getSupported().getFormatArray();
								 for(ComplexDataDescriptionType tempFormat : formats){
									 if(tempFormat.getEncoding().equalsIgnoreCase(data.getEncoding())){
										 foundEncoding = tempFormat.getEncoding();
										 encodingFormat = tempFormat;
										 found = found +1;
									 }
								 }
							}
							
							if(found == 1){
								encoding = foundEncoding;
								mimeType = encodingFormat.getMimeType();
								if(encodingFormat.isSetSchema()){
									schema = encodingFormat.getSchema();
								}
							}else{
								throw new ExceptionReport("Request incomplete. Could not determine a suitable input format based on the given input [mime Type missing and given encoding not unique]", ExceptionReport.MISSING_PARAMETER_VALUE);
							}
							
						}
						if(data.isSetSchema() && !data.isSetEncoding()){
							//schema set only
							ComplexDataDescriptionType schemaFormat = null;
							String defaultSchema = inputReferenceDesc.getComplexData().getDefault().getFormat().getSchema();
							int found = 0;
							String foundSchema = null;
							if(defaultSchema.equalsIgnoreCase(data.getSchema())){
								foundSchema = inputReferenceDesc.getComplexData().getDefault().getFormat().getSchema();
								schemaFormat = inputReferenceDesc.getComplexData().getDefault().getFormat();
								found = found +1;
							}else{
								 ComplexDataDescriptionType[] formats = inputReferenceDesc.getComplexData().getSupported().getFormatArray();
								 for(ComplexDataDescriptionType tempFormat : formats){
									 if(tempFormat.getEncoding().equalsIgnoreCase(data.getSchema())){
										 foundSchema = tempFormat.getSchema();
										 schemaFormat =tempFormat;
										 found = found +1;
									 }
								 }
							}
							
							if(found == 1){
								schema = foundSchema;
								mimeType = schemaFormat.getMimeType();
								if(schemaFormat.isSetEncoding()){
									encoding = schemaFormat.getEncoding();
								}
							}else{
								throw new ExceptionReport("Request incomplete. Could not determine a suitable input format based on the given input [mime Type missing and given schema not unique]", ExceptionReport.MISSING_PARAMETER_VALUE);
							}
							
						}
						if(data.isSetEncoding() && data.isSetSchema()){
							//schema and encoding set
							
							
							//encoding
							String defaultEncoding = inputReferenceDesc.getComplexData().getDefault().getFormat().getEncoding();
							
							List<ComplexDataDescriptionType> foundEncodingList = new ArrayList<ComplexDataDescriptionType>();
							if(defaultEncoding.equalsIgnoreCase(data.getEncoding())){
								foundEncodingList.add(inputReferenceDesc.getComplexData().getDefault().getFormat());
								
								
							}else{
								 ComplexDataDescriptionType[] formats = inputReferenceDesc.getComplexData().getSupported().getFormatArray();
								 for(ComplexDataDescriptionType tempFormat : formats){
									 if(tempFormat.getEncoding().equalsIgnoreCase(data.getEncoding())){
										 foundEncodingList.add(tempFormat);
									 }
							}
							
							
							
							
							//schema
							List<ComplexDataDescriptionType> foundSchemaList = new ArrayList<ComplexDataDescriptionType>();
							String defaultSchema = inputReferenceDesc.getComplexData().getDefault().getFormat().getSchema();
							if(defaultSchema.equalsIgnoreCase(data.getSchema())){
								foundSchemaList.add(inputReferenceDesc.getComplexData().getDefault().getFormat());
							}else{
								 formats = inputReferenceDesc.getComplexData().getSupported().getFormatArray();
								 for(ComplexDataDescriptionType tempFormat : formats){
									 if(tempFormat.getEncoding().equalsIgnoreCase(data.getSchema())){
										 foundSchemaList.add(tempFormat);
									 }
								 }
							}
							
							
							//results
							ComplexDataDescriptionType foundCommonFormat = null;
							for(ComplexDataDescriptionType encodingFormat : foundEncodingList){
								for(ComplexDataDescriptionType schemaFormat : foundSchemaList){
									if(encodingFormat.equals(schemaFormat)){
										foundCommonFormat = encodingFormat;
									}
								}
									
								
							}
							
							if(foundCommonFormat!=null){
								mimeType = foundCommonFormat.getMimeType();
								if(foundCommonFormat.isSetEncoding()){
									encoding = foundCommonFormat.getEncoding();
								}
								if(foundCommonFormat.isSetSchema()){
									schema = foundCommonFormat.getSchema();
								}
							}else{
								throw new ExceptionReport("Request incomplete. Could not determine a suitable input format based on the given input [mime Type missing and given encoding and schema are not unique]", ExceptionReport.MISSING_PARAMETER_VALUE);
							}
							
						}
							
					}
						
				}
			}

		}
		
		
		
		
		
		IParser parser = null;
		try {
			Class algorithmInput = RepositoryManager.getInstance().getInputDataTypeForAlgorithm(this.algorithmIdentifier, inputID);
			
			LOGGER.debug("Looking for matching Parser ..." + 
					" schema: " + schema +
					" mimeType: " + mimeType +
					" encoding: " + encoding);
			
			parser = ParserFactory.getInstance().getParser(schema, mimeType, encoding, algorithmInput);
		} catch (RuntimeException e) {
			throw new ExceptionReport("Error obtaining input data", ExceptionReport.NO_APPLICABLE_CODE, e);
		}
		if(parser == null) {
			throw new ExceptionReport("Error. No applicable parser found for " + schema + "," + mimeType + "," + encoding, ExceptionReport.NO_APPLICABLE_CODE);
		}
		IData collection = null;
		
		// encoding is UTF-8 (or nothing and we default to UTF-8)
		// everything that goes to this condition should be inline xml data
		if (encoding == null || encoding.equals("") || encoding.equalsIgnoreCase(IOHandler.DEFAULT_ENCODING)){
			try {
				boolean xsiisIn = complexValue.contains("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
				if(!xsiisIn){
						complexValue = complexValue.replace("xsi:schemaLocation", "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation");
				}
				
				InputStream stream = new ByteArrayInputStream(complexValue.getBytes());
				collection = parser.parse(stream, mimeType, schema);
			}
			catch(RuntimeException e) {
				throw new ExceptionReport("Error occured, while XML parsing", 
						ExceptionReport.NO_APPLICABLE_CODE, e);
			}
		}
		
		// in case encoding is base64
		// everything that goes to this condition should be inline base64 data
		else if (encoding.equalsIgnoreCase(IOHandler.ENCODING_BASE64)){
			File f = null;
			FileOutputStream fos = null;
			try {
				f = File.createTempFile("wps" + UUID.randomUUID(), "tmp");
				fos = new FileOutputStream(f);
				
				if(complexValue.startsWith("<xml-fragment")){
					int startIndex = complexValue.indexOf(">");
					complexValue = complexValue.substring(startIndex+1);
					
					int endIndex = complexValue.indexOf("</xml-fragment");
					complexValue = complexValue.substring(0,endIndex);
					
				}
				StringReader sr = new StringReader(complexValue);
				int i = sr.read();
				while(i != -1){
					fos.write(i);
					i = sr.read();
				}
				fos.close();
				collection = parser.parseBase64(new FileInputStream(f), mimeType, schema);
				System.gc();
				f.delete();
			} catch (IOException e) {
				throw new ExceptionReport("Error occured, while Base64 extracting", 
						ExceptionReport.NO_APPLICABLE_CODE, e);
			} finally {
				try {
					if (fos != null){
						fos.close();
					}
					if (f != null){
						f.delete();
					}
				} catch (Exception e) {
					throw new ExceptionReport("Unable to generate tempfile", ExceptionReport.NO_APPLICABLE_CODE);
				}
			}
		}
		
		else {
			throw new ExceptionReport("Unable to generate encoding " + encoding, ExceptionReport.NO_APPLICABLE_CODE);
		}
		
		//enable maxxoccurs of parameters with the same name.
		if(inputData.containsKey(inputID)) {
			List<IData> list = inputData.get(inputID);
			list.add(collection);
		}
		else {
			List<IData> list = new ArrayList<IData>();
			list.add(collection);
			inputData.put(inputID, list);
		}
	}

	/**
	 * Handles the literalData
	 * @param input The client's input
	 * @throws ExceptionReport If the type of the parameter is invalid.
	 */
	private void handleLiteralData(InputType input) throws ExceptionReport {
		String inputID = input.getIdentifier().getStringValue();
		String parameter = input.getData().getLiteralData().getStringValue();
		String xmlDataType = input.getData().getLiteralData().getDataType();
		
		InputDescriptionType inputDesc = null;
		for(InputDescriptionType tempDesc : this.processDesc.getDataInputs().getInputArray()) {
			if(inputID.equals(tempDesc.getIdentifier().getStringValue())) {
				inputDesc = tempDesc;
				break;
			}
		}
		
		if(xmlDataType == null) {
			DomainMetadataType dataType = inputDesc.getLiteralData().getDataType();
			xmlDataType = dataType != null ? dataType.getReference() : null;
		}
		
		IData parameterObj = null;
		try {
			parameterObj = BasicXMLTypeFactory.getBasicJavaObject(xmlDataType, parameter);
		}
		catch(RuntimeException e) {
			throw new ExceptionReport("The passed parameterValue: " + parameter + ", but should be of type: " + xmlDataType, ExceptionReport.INVALID_PARAMETER_VALUE);
		}
		
		//validate allowed values.
		if(inputDesc.getLiteralData().isSetAllowedValues()){
			if((!inputDesc.getLiteralData().isSetAnyValue())){
				ValueType[] allowedValues = inputDesc.getLiteralData().getAllowedValues().getValueArray();
				boolean foundAllowedValue = false;
				for(ValueType allowedValue : allowedValues){
					if(input.getData().getLiteralData().getStringValue().equals(allowedValue.getStringValue())){
						foundAllowedValue = true;
						
					}
				}
				RangeType[] allowedRanges = {};
				if(parameterObj instanceof LiteralIntBinding || parameterObj instanceof LiteralDoubleBinding || parameterObj instanceof LiteralShortBinding || parameterObj instanceof LiteralFloatBinding || parameterObj instanceof LiteralLongBinding || parameterObj instanceof LiteralByteBinding){
				
					allowedRanges = inputDesc.getLiteralData().getAllowedValues().getRangeArray();
					for(RangeType allowedRange : allowedRanges){
						if((parameterObj instanceof LiteralIntBinding)){
							int min = new Integer(allowedRange.getMinimumValue().getStringValue());
							int max = new Integer(allowedRange.getMaximumValue().getStringValue());
							if((Integer)(parameterObj.getPayload())>min && (Integer)parameterObj.getPayload()<max){
								foundAllowedValue = true;
							}
						}
						if((parameterObj instanceof LiteralDoubleBinding)){
							Double min = new Double(allowedRange.getMinimumValue().getStringValue());
							Double max = new Double(allowedRange.getMaximumValue().getStringValue());
							if((Double)(parameterObj.getPayload())>min && (Double)parameterObj.getPayload()<max){
								foundAllowedValue = true;
							}
						}
						if((parameterObj instanceof LiteralShortBinding)){
							Short min = new Short(allowedRange.getMinimumValue().getStringValue());
							Short max = new Short(allowedRange.getMaximumValue().getStringValue());
							if((Short)(parameterObj.getPayload())>min && (Short)parameterObj.getPayload()<max){
								foundAllowedValue = true;
							}
						}
						if((parameterObj instanceof LiteralFloatBinding)){
							Float min = new Float(allowedRange.getMinimumValue().getStringValue());
							Float max = new Float(allowedRange.getMaximumValue().getStringValue());
							if((Float)(parameterObj.getPayload())>min && (Float)parameterObj.getPayload()<max){
								foundAllowedValue = true;
							}
						}
						if((parameterObj instanceof LiteralLongBinding)){
							Long min = new Long(allowedRange.getMinimumValue().getStringValue());
							Long max = new Long(allowedRange.getMaximumValue().getStringValue());
							if((Long)(parameterObj.getPayload())>min && (Long)parameterObj.getPayload()<max){
								foundAllowedValue = true;
							}
						}
						if((parameterObj instanceof LiteralByteBinding)){
							Byte min = new Byte(allowedRange.getMinimumValue().getStringValue());
							Byte max = new Byte(allowedRange.getMaximumValue().getStringValue());
							if((Byte)(parameterObj.getPayload())>min && (Byte)parameterObj.getPayload()<max){
								foundAllowedValue = true;
							}
						}
						
					}
				}
				
				
				if(!foundAllowedValue && (allowedValues.length!=0 || allowedRanges.length!=0)){
					throw new ExceptionReport("Input with ID " + inputID + " does not contain an allowed value. See ProcessDescription.", ExceptionReport.INVALID_PARAMETER_VALUE);
				}
				
			}
		}
		
		
		if(parameterObj == null) {
			throw new ExceptionReport("XML datatype as LiteralParameter is not supported by the server: dataType " + xmlDataType, 
					ExceptionReport.INVALID_PARAMETER_VALUE);
		}
		//enable maxxoccurs of parameters with the same name.
		if(inputData.containsKey(inputID)) {
			List<IData> list = inputData.get(inputID);
			list.add(parameterObj);
		}
		else {
			List<IData> list = new ArrayList<IData>();
			list.add(parameterObj);
			inputData.put(inputID, list);
		}
		
	}
	
	/**
	 * Handles the ComplexValueReference
	 * @param input The client input
	 * @throws ExceptionReport If the input (as url) is invalid, or there is an error while parsing the XML.
	 */
	private void handleComplexValueReference(InputType input) throws ExceptionReport{
		String inputID = input.getIdentifier().getStringValue();
		
		ReferenceStrategyRegister register = ReferenceStrategyRegister.getInstance();
		InputStream stream = register.resolveReference(input);
		
		String dataURLString = input.getReference().getHref();
		//dataURLString = URLDecoder.decode(dataURLString);
		//dataURLString = dataURLString.replace("&amp;", "");
		LOGGER.debug("Loading data from: " + dataURLString);
		
		
		/**
		 * initialize data format with default values defaults and overwrite with defaults from request if applicable
		 */
		InputDescriptionType inputPD = null;
		for(InputDescriptionType tempDesc : this.processDesc.getDataInputs().getInputArray()) {
			if(inputID.equals(tempDesc.getIdentifier().getStringValue())) {
				inputPD = tempDesc;
				break;
			}
		}
		if(inputPD == null) { // check if there is a corresponding input identifier in the process description
			LOGGER.debug("Input cannot be found in description for " + this.processDesc.getIdentifier().getStringValue() + "," + inputID);
			throw new RuntimeException("Input cannot be found in description for " + this.processDesc.getIdentifier().getStringValue() + "," + inputID);
		}

		//select parser
		
		//1. mimeType set?
		//yes--> set it
			//1.1 schema/encoding set?
			//yes-->set it
			//not-->set default values for parser with matching mime type
		
		//no--> look in http stream
		//2. mimeType set in http stream
			//yes -->set it
				//2.1 schema/encoding set?
				//yes-->set it
				//not-->set default values for parser with matching mime type
			//no--> schema or/and encoding are set?
					//yes-->use it, look if only one mime type can be found
					//not-->use default values
			
			
		
		String schema = null;
		String mimeType = null;
		String encoding = null;
		
		// overwrite with data format from request if appropriate
		InputReferenceType referenceData = input.getReference();
		
		if (referenceData.isSetMimeType() && referenceData.getMimeType() != null){
			//mime type in request
			mimeType = referenceData.getMimeType();
			ComplexDataDescriptionType format = null;
			
			String defaultMimeType = inputPD.getComplexData().getDefault().getFormat().getMimeType();
			
			boolean canUseDefault = false;
			if(defaultMimeType.equalsIgnoreCase(mimeType)){
				ComplexDataDescriptionType potenitalFormat = inputPD.getComplexData().getDefault().getFormat();
				if(referenceData.getSchema() != null && referenceData.getEncoding() == null){
					if(referenceData.getSchema().equalsIgnoreCase(potenitalFormat.getSchema())){
						canUseDefault = true;
						format = potenitalFormat;
					}
				}
				if(referenceData.getSchema() == null && referenceData.getEncoding() != null){
					if(referenceData.getEncoding().equalsIgnoreCase(potenitalFormat.getEncoding())){
						canUseDefault = true;
						format = potenitalFormat;
					}
					
				}
				if(referenceData.getSchema() != null && referenceData.getEncoding() != null){
					if(referenceData.getSchema().equalsIgnoreCase(potenitalFormat.getSchema()) && referenceData.getEncoding().equalsIgnoreCase(potenitalFormat.getEncoding())){
						canUseDefault = true;
						format = potenitalFormat;
					}
					
				}
				if(referenceData.getSchema() == null && referenceData.getEncoding() == null){
					canUseDefault = true;
					format = potenitalFormat;
				}
				
			}
			if(!canUseDefault){
				 ComplexDataDescriptionType[] formats = inputPD.getComplexData().getSupported().getFormatArray();
				 for(ComplexDataDescriptionType potenitalFormat : formats){
					 if(potenitalFormat.getMimeType().equalsIgnoreCase(mimeType)){
						 if(referenceData.getSchema() != null && referenceData.getEncoding() == null){
								if(referenceData.getSchema().equalsIgnoreCase(potenitalFormat.getSchema())){
									format = potenitalFormat;
								}
							}
							if(referenceData.getSchema() == null && referenceData.getEncoding() != null){
								if(referenceData.getEncoding().equalsIgnoreCase(potenitalFormat.getEncoding())){
									format = potenitalFormat;
								}
								
							}
							if(referenceData.getSchema() != null && referenceData.getEncoding() != null){
								if(referenceData.getSchema().equalsIgnoreCase(potenitalFormat.getSchema()) && referenceData.getEncoding().equalsIgnoreCase(potenitalFormat.getEncoding())){
									format = potenitalFormat;
								}
								
							}
							if(referenceData.getSchema() == null && referenceData.getEncoding() == null){
								format = potenitalFormat;
							}
					 }
				 }
			}
			if(format == null){
				throw new ExceptionReport("Could not determine output format", ExceptionReport.INVALID_PARAMETER_VALUE);
			}
			
			mimeType = format.getMimeType();
			
			if(format.isSetEncoding()){
				//no encoding provided--> select default one for mimeType
				encoding = format.getEncoding();
			}
			
			if(format.isSetSchema()){
				//no encoding provided--> select default one for mimeType
				schema = format.getSchema();
			}
			
		}else{
			//mimeType not in request
			//try to fetch mimetype from http stream
			 URL url;
			try {
				url = new URL(dataURLString);
			
				 URLConnection urlConnection = url.openConnection();
				 mimeType = urlConnection.getContentType();
				 ComplexDataDescriptionType format = null;
				 
				 if(mimeType!=null){
					 	String defaultMimeType = inputPD.getComplexData().getDefault().getFormat().getMimeType();
						
					 
												
						boolean canUseDefault = false;
						if(defaultMimeType.equalsIgnoreCase(mimeType)){
							ComplexDataDescriptionType potenitalFormat = inputPD.getComplexData().getDefault().getFormat();
							if(referenceData.getSchema() != null && referenceData.getEncoding() == null){
								if(referenceData.getSchema().equalsIgnoreCase(potenitalFormat.getSchema())){
									canUseDefault = true;
									format = potenitalFormat;
								}
							}
							if(referenceData.getSchema() == null && referenceData.getEncoding() != null){
								if(referenceData.getEncoding().equalsIgnoreCase(potenitalFormat.getEncoding())){
									canUseDefault = true;
									format = potenitalFormat;
								}
								
							}
							if(referenceData.getSchema() != null && referenceData.getEncoding() != null){
								if(referenceData.getSchema().equalsIgnoreCase(potenitalFormat.getSchema()) && referenceData.getEncoding().equalsIgnoreCase(potenitalFormat.getEncoding())){
									canUseDefault = true;
									format = potenitalFormat;
								}
								
							}
							if(referenceData.getSchema() == null && referenceData.getEncoding() == null){
								canUseDefault = true;
								format = potenitalFormat;
							}
							
						}
						if(!canUseDefault){
							 ComplexDataDescriptionType[] formats = inputPD.getComplexData().getSupported().getFormatArray();
							 for(ComplexDataDescriptionType potenitalFormat : formats){
								 if(potenitalFormat.getMimeType().equalsIgnoreCase(mimeType)){
									 if(referenceData.getSchema() != null && referenceData.getEncoding() == null){
											if(referenceData.getSchema().equalsIgnoreCase(potenitalFormat.getSchema())){
												format = potenitalFormat;
											}
										}
										if(referenceData.getSchema() == null && referenceData.getEncoding() != null){
											if(referenceData.getEncoding().equalsIgnoreCase(potenitalFormat.getEncoding())){
												format = potenitalFormat;
											}
											
										}
										if(referenceData.getSchema() != null && referenceData.getEncoding() != null){
											if(referenceData.getSchema().equalsIgnoreCase(potenitalFormat.getSchema()) && referenceData.getEncoding().equalsIgnoreCase(potenitalFormat.getEncoding())){
												format = potenitalFormat;
											}
											
										}
										if(referenceData.getSchema() == null && referenceData.getEncoding() == null){
											format = potenitalFormat;
										}
								 }
							 }
						}
						if(format == null){
							throw new ExceptionReport("Could not determine output format", ExceptionReport.INVALID_PARAMETER_VALUE);
						}
						
						mimeType = format.getMimeType();
						
						if(format.isSetEncoding()){
							//no encoding provided--> select default one for mimeType
							encoding = format.getEncoding();
						}
						
						if(format.isSetSchema()){
							//no encoding provided--> select default one for mimeType
							schema = format.getSchema();
						}
				 }
				 
			} catch (MalformedURLException e) {
				e.printStackTrace();
				LOGGER.debug("Could not determine MimeType from Input URL: " + dataURLString);
			} catch (IOException e) {
				e.printStackTrace();
				LOGGER.debug("Could not determine MimeType from Input URL: " + dataURLString);
			}
			
			if(mimeType==null && !referenceData.isSetEncoding() && !referenceData.isSetSchema()){
					//nothing set, use default values
					schema = inputPD.getComplexData().getDefault().getFormat().getSchema();
					mimeType = inputPD.getComplexData().getDefault().getFormat().getMimeType();
					encoding = inputPD.getComplexData().getDefault().getFormat().getEncoding();
				
			}else{
					//do a smart search an look if a mimeType can be found for either schema and/or encoding
					
				if(mimeType==null){	
					if(referenceData.isSetEncoding() && !referenceData.isSetSchema()){
							//encoding set only
							ComplexDataDescriptionType encodingFormat = null;
							String defaultEncoding = inputPD.getComplexData().getDefault().getFormat().getEncoding();
							int found = 0;
							String foundEncoding = null;
							if(defaultEncoding.equalsIgnoreCase(referenceData.getEncoding())){
								foundEncoding = inputPD.getComplexData().getDefault().getFormat().getEncoding();
								encodingFormat = inputPD.getComplexData().getDefault().getFormat();
								found = found +1;
							}else{
								 ComplexDataDescriptionType[] formats = inputPD.getComplexData().getSupported().getFormatArray();
								 for(ComplexDataDescriptionType tempFormat : formats){
									 if(tempFormat.getEncoding().equalsIgnoreCase(referenceData.getEncoding())){
										 foundEncoding = tempFormat.getEncoding();
										 encodingFormat = tempFormat;
										 found = found +1;
									 }
								 }
							}
							
							if(found == 1){
								encoding = foundEncoding;
								mimeType = encodingFormat.getMimeType();
								if(encodingFormat.isSetSchema()){
									schema = encodingFormat.getSchema();
								}
							}else{
								throw new ExceptionReport("Request incomplete. Could not determine a suitable input format based on the given input [mime Type missing and given encoding not unique]", ExceptionReport.MISSING_PARAMETER_VALUE);
							}
							
						}
						if(referenceData.isSetSchema() && !referenceData.isSetEncoding()){
							//schema set only
							ComplexDataDescriptionType schemaFormat = null;
							String defaultSchema = inputPD.getComplexData().getDefault().getFormat().getSchema();
							int found = 0;
							String foundSchema = null;
							if(defaultSchema.equalsIgnoreCase(referenceData.getSchema())){
								foundSchema = inputPD.getComplexData().getDefault().getFormat().getSchema();
								schemaFormat = inputPD.getComplexData().getDefault().getFormat();
								found = found +1;
							}else{
								 ComplexDataDescriptionType[] formats = inputPD.getComplexData().getSupported().getFormatArray();
								 for(ComplexDataDescriptionType tempFormat : formats){
									 if(tempFormat.getEncoding().equalsIgnoreCase(referenceData.getSchema())){
										 foundSchema = tempFormat.getSchema();
										 schemaFormat =tempFormat;
										 found = found +1;
									 }
								 }
							}
							
							if(found == 1){
								schema = foundSchema;
								mimeType = schemaFormat.getMimeType();
								if(schemaFormat.isSetEncoding()){
									encoding = schemaFormat.getEncoding();
								}
							}else{
								throw new ExceptionReport("Request incomplete. Could not determine a suitable input format based on the given input [mime Type missing and given schema not unique]", ExceptionReport.MISSING_PARAMETER_VALUE);
							}
							
						}
						if(referenceData.isSetEncoding() && referenceData.isSetSchema()){
							//schema and encoding set
							
							
							//encoding
							String defaultEncoding = inputPD.getComplexData().getDefault().getFormat().getEncoding();
							
							List<ComplexDataDescriptionType> foundEncodingList = new ArrayList<ComplexDataDescriptionType>();
							if(defaultEncoding.equalsIgnoreCase(referenceData.getEncoding())){
								foundEncodingList.add(inputPD.getComplexData().getDefault().getFormat());
								
								
							}else{
								 ComplexDataDescriptionType[] formats = inputPD.getComplexData().getSupported().getFormatArray();
								 for(ComplexDataDescriptionType tempFormat : formats){
									 if(tempFormat.getEncoding().equalsIgnoreCase(referenceData.getEncoding())){
										 foundEncodingList.add(tempFormat);
									 }
							}
							
							
							
							
							//schema
							List<ComplexDataDescriptionType> foundSchemaList = new ArrayList<ComplexDataDescriptionType>();
							String defaultSchema = inputPD.getComplexData().getDefault().getFormat().getSchema();
							if(defaultSchema.equalsIgnoreCase(referenceData.getSchema())){
								foundSchemaList.add(inputPD.getComplexData().getDefault().getFormat());
							}else{
								 formats = inputPD.getComplexData().getSupported().getFormatArray();
								 for(ComplexDataDescriptionType tempFormat : formats){
									 if(tempFormat.getEncoding().equalsIgnoreCase(referenceData.getSchema())){
										 foundSchemaList.add(tempFormat);
									 }
								 }
							}
							
							
							//results
							ComplexDataDescriptionType foundCommonFormat = null;
							for(ComplexDataDescriptionType encodingFormat : foundEncodingList){
								for(ComplexDataDescriptionType schemaFormat : foundSchemaList){
									if(encodingFormat.equals(schemaFormat)){
										foundCommonFormat = encodingFormat;
									}
								}
									
								
							}
							
							if(foundCommonFormat!=null){
								mimeType = foundCommonFormat.getMimeType();
								if(foundCommonFormat.isSetEncoding()){
									encoding = foundCommonFormat.getEncoding();
								}
								if(foundCommonFormat.isSetSchema()){
									schema = foundCommonFormat.getSchema();
								}
							}else{
								throw new ExceptionReport("Request incomplete. Could not determine a suitable input format based on the given input [mime Type missing and given encoding and schema are not unique]", ExceptionReport.MISSING_PARAMETER_VALUE);
							}
							
						}
							
					}
						
				}
			}

		}
		
		
		LOGGER.debug("Loading parser for: "+ schema + "," + mimeType + "," + encoding);
		
		IParser parser = null;
		try {
			Class algorithmInputClass = RepositoryManager.getInstance().getInputDataTypeForAlgorithm(this.algorithmIdentifier, inputID);
			if(algorithmInputClass == null) {
				throw new RuntimeException("Could not determine internal input class for input" + inputID);
			}
			LOGGER.debug("Looking for matching Parser ..." + 
					" schema: " + schema +
					" mimeType: " + mimeType +
					" encoding: " + encoding);
			
			parser = ParserFactory.getInstance().getParser(schema, mimeType, encoding, algorithmInputClass);
			
			if(parser == null) {
				throw new ExceptionReport("Error. No applicable parser found for " + schema + "," + mimeType + "," + encoding, ExceptionReport.NO_APPLICABLE_CODE);
			}
		} catch (RuntimeException e) {
			throw new ExceptionReport("Error obtaining input data", ExceptionReport.NO_APPLICABLE_CODE, e);
		}
		
			
			/****PROXY*****/
			/*String decodedURL = URLDecoder.decode(dataURLString);
			decodedURL = decodedURL.replace("&amp;", "&");
			if(decodedURL.indexOf("&BBOX")==-1){
				decodedURL = decodedURL.replace("BBOX", "&BBOX");
				decodedURL = decodedURL.replace("outputFormat", "&outputFormat");
				decodedURL = decodedURL.replace("SRS", "&SRS");
				decodedURL = decodedURL.replace("REQUEST", "&REQUEST");
				decodedURL = decodedURL.replace("VERSION", "&VERSION");
				decodedURL = decodedURL.replace("SERVICE", "&SERVICE");
				decodedURL = decodedURL.replace("format", "&format");
			}*/
			
		
			//lookup WFS
			if(dataURLString.toUpperCase().contains("REQUEST=GETFEATURE") &&
				dataURLString.toUpperCase().contains("SERVICE=WFS")){
					if(parser instanceof SimpleGMLParser){
						parser = new GML2BasicParser();
					}
					if(parser instanceof GML2BasicParser && !dataURLString.toUpperCase().contains("OUTPUTFORMAT=GML2")){
						//make sure we get GML2
						dataURLString = dataURLString+"&outputFormat=GML2";
					}
					if(parser instanceof GML3BasicParser && !dataURLString.toUpperCase().contains("OUTPUTFORMAT=GML3")){
						//make sure we get GML3
						dataURLString = dataURLString+"&outputFormat=GML3";
					}
			}


						
			IData parsedInputData = parser.parse(stream, mimeType, schema);				
			
			//enable maxxoccurs of parameters with the same name.
			if(inputData.containsKey(inputID)) {
				List<IData> list = inputData.get(inputID);
				list.add(parsedInputData);
				inputData.put(inputID, list);
			}
			else {
				List<IData> list = new ArrayList<IData>();
				list.add(parsedInputData);
				inputData.put(inputID, list);
			}
		
		
	}
	
	/**
	 * Handles BBoxValue
	 * @param input The client input
	 */
	private void handleBBoxValue(InputType input) throws ExceptionReport{
		String crs = input.getData().getBoundingBoxData().getCrs();
		List lowerCorner = input.getData().getBoundingBoxData().getLowerCorner();
		List upperCorner = input.getData().getBoundingBoxData().getUpperCorner();
		
		if(lowerCorner.size()!=2 || upperCorner.size()!=2){
			throw new ExceptionReport("Error while parsing the BBOX data", ExceptionReport.INVALID_PARAMETER_VALUE);
		}
		IData envelope = new GTReferenceEnvelope(lowerCorner.get(0),lowerCorner.get(1),upperCorner.get(0), upperCorner.get(1), crs);
		
		List<IData> resultList = new ArrayList<IData>();
		resultList.add(envelope);
		inputData.put(input.getIdentifier().getStringValue(), resultList);		
	}
	
	/**
	 * Gets the resulting InputLayers from the parser
	 * @return A map with the parsed input
	 */
	public Map<String, List<IData>> getParsedInputData(){
		return inputData;
	}
	
	
	
//	private InputStream retrievingZippedContent(URLConnection conn) throws IOException{
//		String contentType = conn.getContentEncoding();
//		if(contentType != null && contentType.equals("gzip")) {
//			return new GZIPInputStream(conn.getInputStream());
//		}
//		else{
//			return conn.getInputStream();
//		}
//	}
	
	 private String nodeToString(Node node) throws TransformerFactoryConfigurationError, TransformerException {
		  StringWriter stringWriter = new StringWriter();
		  Transformer transformer = TransformerFactory.newInstance().newTransformer();
		  transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		  transformer.transform(new DOMSource(node), new StreamResult(stringWriter));
		  
		  return stringWriter.toString();
	 }
}
