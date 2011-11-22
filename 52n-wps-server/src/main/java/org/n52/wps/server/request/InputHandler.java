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
package org.n52.wps.server.request;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
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
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.InputType;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.log4j.Logger;
import org.n52.wps.io.IParser;
import org.n52.wps.io.ParserFactory;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.datahandler.xml.AbstractXMLParser;
import org.n52.wps.io.datahandler.xml.GML2BasicParser;
import org.n52.wps.io.datahandler.xml.GML3BasicParser;
import org.n52.wps.io.datahandler.xml.SimpleGMLParser;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.repository.RepositoryManager;
import org.n52.wps.server.request.strategy.ReferenceStrategyRegister;
import org.n52.wps.util.BasicXMLTypeFactory;
import org.w3c.dom.Node;

/**
 * Handles the input of the client and stores it into a Map.
 */
public class InputHandler {

	protected static Logger LOGGER = Logger.getLogger(InputHandler.class);
	protected Map<String, List<IData>> inputData = new HashMap<String, List<IData>>();
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
		LOGGER.info(inputs.toString());
		this. algorithmIdentifier = algorithmIdentifier;
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
	protected void handleComplexData(InputType input) throws ExceptionReport{
		LOGGER.info(input.toString());
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
		InputDescriptionType inputDesc = null;
		for(InputDescriptionType tempDesc : this.processDesc.getDataInputs().getInputArray()) {
			if(inputID.equals(tempDesc.getIdentifier().getStringValue())) {
				inputDesc = tempDesc;
				break;
			}
		}

		if(inputDesc == null) {
			LOGGER.debug("input cannot be found in description for " + processDesc.getIdentifier().getStringValue() + "," + inputID);
		}
		
		String schema = input.getData().getComplexData().getSchema();
		String encoding = input.getData().getComplexData().getEncoding();
		String mimeType = input.getData().getComplexData().getMimeType();
		if(mimeType == null) {
			mimeType = inputDesc.getComplexData().getDefault().getFormat().getMimeType();
		}
		if(encoding == null) {
			encoding = inputDesc.getComplexData().getDefault().getFormat().getEncoding();
		}
		if(schema==null) {
			schema = inputDesc.getComplexData().getDefault().getFormat().getSchema();
		}
		IParser parser = null;
//		if(this.algorithmIdentifier==null)
//			parser = ParserFactory.getInstance().getParser(schema, mimeType, encoding);
//		else
		try {
			Class algorithmInput = RepositoryManager.getInstance().getInputDataTypeForAlgorithm(this.algorithmIdentifier, inputID);
			LOGGER.info("InputType of Algo is "+algorithmInput.getName()+"-"+schema);
			try {
			parser = ParserFactory.getInstance().getParser(schema, mimeType, encoding, algorithmInput);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			LOGGER.info("Parser found");

		} catch (RuntimeException e) {
			throw new ExceptionReport("Error obtaining input data", ExceptionReport.NO_APPLICABLE_CODE, e);
		}
		if(parser == null) {
			LOGGER.info("parser null");
			parser = ParserFactory.getInstance().getSimpleParser();
		}
		IData collection = null;
		if(parser instanceof AbstractXMLParser) {
			try {
				boolean xsiisIn = complexValue.contains("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
				if(!xsiisIn){
						complexValue = complexValue.replace("xsi:schemaLocation", "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation");
				}
			
				collection = ((AbstractXMLParser)parser).parseXML(complexValue);
			}
			catch(RuntimeException e) {
				throw new ExceptionReport("Error occured, while XML parsing", 
						ExceptionReport.NO_APPLICABLE_CODE, e);
			}
		}
		//embedded binary data
		else {
			File f;
			try {
				f = File.createTempFile("wps" + UUID.randomUUID(), "tmp");
				FileOutputStream fos = new FileOutputStream(f);
				
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
				collection = parser.parse(new FileInputStream(f), mimeType);
				System.gc();
				f.delete();
			} catch (IOException e) {
				throw new ExceptionReport("Error occured, while Base64 extracting", 
						ExceptionReport.NO_APPLICABLE_CODE, e);
			}
		
			
			

			
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
	protected void handleLiteralData(InputType input) throws ExceptionReport {
		String inputID = input.getIdentifier().getStringValue();
		String parameter = input.getData().getLiteralData().getStringValue();
		String xmlDataType = input.getData().getLiteralData().getDataType();
		if(xmlDataType == null) {
			InputDescriptionType inputDesc = null;
			for(InputDescriptionType tempDesc : this.processDesc.getDataInputs().getInputArray()) {
				if(inputID.equals(tempDesc.getIdentifier().getStringValue())) {
					inputDesc = tempDesc;
					break;
				}
			}
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
	protected void handleComplexValueReference(InputType input) throws ExceptionReport{
		String inputID = input.getIdentifier().getStringValue();
		
		ReferenceStrategyRegister register = ReferenceStrategyRegister.getInstance();
		InputStream stream = register.resolveReference(input);
		
		String dataURLString = input.getReference().getHref();
		//dataURLString = URLDecoder.decode(dataURLString);
		//dataURLString = dataURLString.replace("&amp;", "");
		LOGGER.debug("Loading data from: " + dataURLString);
		InputDescriptionType inputDesc = null;
		for(InputDescriptionType tempDesc : this.processDesc.getDataInputs().getInputArray()) {
			if(inputID.equals(tempDesc.getIdentifier().getStringValue())) {
				inputDesc = tempDesc;
				break;
			}
		}

		if(inputDesc == null) {
			LOGGER.debug("Input cannot be found in description for " + 
					this.processDesc.getIdentifier().getStringValue() + "," + inputID);
		}
		
		String schema = input.getReference().getSchema();
		String encoding = input.getReference().getEncoding();
		String mimeType = input.getReference().getMimeType();
		if(mimeType == null) {
			mimeType = inputDesc.getComplexData().getDefault().getFormat().getMimeType();
		}
		if(schema == null && !(mimeType.equalsIgnoreCase("application/x-zipped-shp"))) {
			schema = inputDesc.getComplexData().getDefault().getFormat().getSchema();
		}
		if(encoding == null) {
			encoding = inputDesc.getComplexData().getDefault().getFormat().getEncoding();
		}
		
		LOGGER.debug("Loading parser for: "+ schema + "," + mimeType + "," + encoding);
		IParser parser = null;
//		if(this.algorithmIdentifier==null)
//			parser = ParserFactory.getInstance().getParser(schema, mimeType, encoding);
//		else
		try {
			Class algorithmInputClass = RepositoryManager.getInstance().getInputDataTypeForAlgorithm(this.algorithmIdentifier, inputID);
			if(algorithmInputClass == null) {
				throw new RuntimeException("Could not determine internal input class for input" + inputID);
			}
			try {
				parser = ParserFactory.getInstance().getParser(schema, mimeType, encoding, algorithmInputClass);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(parser == null) {
				LOGGER.warn("No applicable parser found. Trying simpleGMLParser");
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
					if(parser instanceof GML2BasicParser){
						//make sure we get GML2
						dataURLString = dataURLString+"&outputFormat=GML2";
					}
					if(parser instanceof GML3BasicParser){
						//make sure we get GML3
						dataURLString = dataURLString+"&outputFormat=GML3";
					}
					
				
					
			}
			
						
			IData parsedInputData = parser.parse(stream, mimeType);				
			
			//enable maxxoccurs of parameters with the same name.
			if(inputData.containsKey(inputID)) {
				List<IData> list = inputData.get(inputID);
				list.add(parsedInputData);
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
	protected void handleBBoxValue(InputType input) throws ExceptionReport{
		//String inputID = input.getIdentifier().getStringValue();
		throw new ExceptionReport("BBox is not supported", ExceptionReport.OPERATION_NOT_SUPPORTED);
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
