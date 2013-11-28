/***************************************************************
 This implementation provides a framework to publish processes to the
web through the  OGC Web Processing Service interface. The framework 
is extensible in terms of processes and data handlers. 

 Copyright (C) 2006 by con terra GmbH

 Authors: 
	Bastian Schaeffer, Institute for Geoinformatics, Muenster, Germany

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

 ***************************************************************/


package org.n52.wps.transactional.algorithm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.opengis.ows.x11.ExceptionReportDocument;
import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionsDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xpath.XPathAPI;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractTransactionalAlgorithm;
import org.n52.wps.transactional.deploy.IProcessManager;
import org.n52.wps.transactional.service.TransactionalHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class GenericTransactionalAlgorithm extends AbstractTransactionalAlgorithm{
	
	private List<String> errors;
	private static Logger LOGGER = LoggerFactory.getLogger(GenericTransactionalAlgorithm.class);
	private ProcessDescriptionType processDescription;
	private String workspace;
	
	private static final String OGC_OWS_URI = "http://www.opengeospatial.net/ows";
	
	public GenericTransactionalAlgorithm(String processID, Class<?> registeredRepository){
		super(processID);
		WPSConfig wpsConfig = WPSConfig.getInstance();
		Property[] properties = wpsConfig.getPropertiesForRepositoryClass(registeredRepository.getName());
		this.workspace = wpsConfig.getPropertyForKey(properties,"WorkspaceLocationRoot").getStringValue();
		this.errors = new ArrayList<String>();
		processDescription = initializeDescription();
		
	}
	
	public ProcessDescriptionType getDescription()  {
		return processDescription;
	}
	
	
	public HashMap<String, IData> run(ExecuteDocument payload){
		Document responseDocument;
		HashMap<String,IData> resultHash = new HashMap<String,IData>();
		try {	
		//forward request
			
			//TODO get deploy manager class from config
			IProcessManager deployManager = TransactionalHelper.getProcessManagerForSchema("BPELProfile.xsd");
                        
			responseDocument = deployManager.invoke(payload, getAlgorithmID());
			
			//1.parse results;
			
			//TODO make temporaryfile
		
			//writeXmlFile(responseDocument,workspace+"\\BPEL\\serverside.xml");
			File tempFile = File.createTempFile("wpsbpelresult", ".xml", null);
			
			File tempFile2 = File.createTempFile("wpsbpelresult", ".xml", null);
			//
            writeXmlFile(responseDocument,tempFile2);

            Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(tempFile2);
            
			GenericFileData data = new GenericFileData(tempFile, "text/xml");
			
			Node gml = XPathAPI.selectSingleNode(d, "//Output/Data/ComplexData/FeatureCollection");
			
			String identifier = XPathAPI.selectSingleNode(d, "//Output/Identifier").getFirstChild().getNodeValue().trim();
			
			writeXmlFile(gml, tempFile);
			
			GenericFileDataBinding binding = new GenericFileDataBinding(data);
			
			resultHash.put(identifier, binding);
			
			//ExecuteResponseDocument executeResponseDocument = ExecuteResponseDocument.Factory.parse(new File(workspace+"\\BPEL\\serverside.xml"));
//            ExecuteResponseDocument executeResponseDocument = ExecuteResponseDocument.Factory.parse(tempFile);
//			
//	//			2.look at each Output Element
//			OutputDataType[] resultValues = executeResponseDocument.getExecuteResponse().getProcessOutputs().getOutputArray();
//			for(int i = 0; i<resultValues.length;i++){
//				OutputDataType ioElement  = resultValues[i];
//				//3.get the identifier as key
//				String key = ioElement.getIdentifier().getStringValue();
//				//4.the the literal value as String
//				if(ioElement.getData().getLiteralData()!=null){
//					resultHash.put(key, OutputParser.handleLiteralValue(ioElement));
//				}
//				//5.parse the complex value
//				if(ioElement.getData().getComplexData()!=null){
//					
//					resultHash.put(key,  OutputParser.handleComplexValue(ioElement, getDescription()));
//						
//					
//				}
//				//6.parse the complex value reference
//				if(ioElement.getReference()!=null){
//					//TODO handle this
//					//download the data, parse it and put it in the hashmap
//					//resultHash.put(key, OutputParser.handleComplexValueReference(ioElement));
//				}
//				
//				//7.parse Bounding Box value
//				if(ioElement.getData().getBoundingBoxData()!=null){
//					resultHash.put(key, OutputParser.handleBBoxValue(ioElement));
//				}
//				
//				
//		}
		
		} catch (Exception e) {
			String error = "Could not create ExecuteResponseDocument";
			errors.add(error);
			LOGGER.warn(error + " Reason: " +e.getMessage());
			throw new RuntimeException(error,e);
		} 
		
		//add response id
		

		return resultHash;
	}


	public List<String> getErrors() {
		return errors;
	}


	protected ProcessDescriptionType initializeDescription() {
		String fullPath =  GenericTransactionalAlgorithm.class.getProtectionDomain().getCodeSource().getLocation().toString();
		int searchIndex= fullPath.indexOf("WEB-INF");
		String subPath = fullPath.substring(0, searchIndex);
		subPath = subPath.replaceFirst("file:/", "");
                String processID = getAlgorithmID();
                //sanitize processID: strip version number and namespace if passed in
                if (processID.contains("-"))
                    processID = processID.split("-")[0];
                if (processID.contains("}"))
                    processID = processID.split("}")[1];
		try {
			File xmlDesc = new File(subPath+File.separator+"WEB-INF"+File.separator+"ProcessDescriptions"+File.separator+processID+".xml");
			XmlOptions option = new XmlOptions();
			option.setLoadTrimTextBuffer();
			ProcessDescriptionsDocument doc = ProcessDescriptionsDocument.Factory.parse(xmlDesc, option);
			if(doc.getProcessDescriptions().getProcessDescriptionArray().length == 0) {
				LOGGER.warn("ProcessDescription does not contain any description");
				return null;
			}
			
			doc.getProcessDescriptions().getProcessDescriptionArray(0).getIdentifier().setStringValue(processID);


			return doc.getProcessDescriptions().getProcessDescriptionArray(0);
		}
		catch(IOException e) {
			LOGGER.warn("Could not initialize algorithm, parsing error: " + getAlgorithmID(), e);
		}
		catch(XmlException e) {
			LOGGER.warn("Could not initialize algorithm, parsing error: " +getAlgorithmID(), e);
		}
		return null;
		
	}
		


	public boolean processDescriptionIsValid() {
		return processDescription.validate();
	}

	
	
	private Document checkResultDocument(Document doc){
		if(getFirstElementNode(doc.getFirstChild()).getNodeName().equals("ExceptionReport") && getFirstElementNode(doc.getFirstChild()).getNamespaceURI().equals(OGC_OWS_URI)) {
			try {
				ExceptionReportDocument exceptionDoc = ExceptionReportDocument.Factory.parse(doc);
				throw new RuntimeException("Error occured while executing query");
			}
			catch(Exception e) {
				throw new RuntimeException("Error while parsing ExceptionReport retrieved from server", e);
			}
		}
		return doc;
	}

	private Node getFirstElementNode(Node node) {
		if(node == null) {
			return null;
		}
		if(node.getNodeType() == Node.ELEMENT_NODE) {
			return node;
		}
		else {
			return getFirstElementNode(node.getNextSibling());
		}
		
	}
	

	//private static void writeXmlFile(Document doc, String filename) {
        private static void writeXmlFile(Document doc, File file) {
        try {
//        	if(filename==null){
//        		filename = "C:\\BPEL\\serverside.xml";
//        	}
            // Prepare the DOM document for writing
            Source source = new DOMSource(doc);
    
            // Prepare the output file
            //File file = new File(filename);
            //file.createNewFile();
            //Result result = new StreamResult(file);
            Result result = new StreamResult(file.toURI().getPath());
    
            // Write the DOM document to the file
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
        	System.out.println("error");
        } catch (TransformerException e) {
        	System.out.println("error");
        } catch (Exception e) {
        	System.out.println("error");
        }
		
		
	}

        
	private static void writeXmlFile(Node n, File file) {
		try {
			// if(filename==null){
			// filename = "C:\\BPEL\\serverside.xml";
			// }
			// Prepare the DOM document for writing
			Source source = new DOMSource(n);

			// Prepare the output file
			// File file = new File(filename);
			// file.createNewFile();
			// Result result = new StreamResult(file);
			Result result = new StreamResult(file.toURI().getPath());

			// Write the DOM document to the file
			Transformer xformer = TransformerFactory.newInstance()
					.newTransformer();
			xformer.transform(source, result);
		} catch (TransformerConfigurationException e) {
			System.out.println("error");
		} catch (TransformerException e) {
			System.out.println("error");
		} catch (Exception e) {
			System.out.println("error");
		}

	}
        
	public String getWellKnownName() {
		return "";
	}

	public Class getInputDataType(String id) {
		InputDescriptionType[] inputs = processDescription.getDataInputs().getInputArray();
		for(InputDescriptionType input : inputs){
			if(input.getIdentifier().getStringValue().equals(id)){
				if(input.isSetLiteralData()){
					String datatype = input.getLiteralData().getDataType().getStringValue();
					if(datatype.contains("tring")){
							return LiteralStringBinding.class;
					}
					if(datatype.contains("ollean")){
						return LiteralBooleanBinding.class;
					}
					if(datatype.contains("loat") || datatype.contains("ouble")){
						return LiteralDoubleBinding.class;
					}
					if(datatype.contains("nt")){
						return LiteralIntBinding.class;
					}
				}
				if(input.isSetComplexData()){
					 String mimeType = input.getComplexData().getDefault().getFormat().getMimeType();
					 if(mimeType.contains("xml") || (mimeType.contains("XML"))){
						 return GTVectorDataBinding.class;
					 }else{
						 return GTRasterDataBinding.class;
					 }
				}
			}
		}
		throw new RuntimeException("Could not determie internal inputDataType");
	}

	public Class getOutputDataType(String id) {
		OutputDescriptionType[] outputs = processDescription.getProcessOutputs().getOutputArray();
		
		for(OutputDescriptionType output : outputs){
			
			if(output.isSetLiteralOutput()){
				String datatype = output.getLiteralOutput().getDataType().getStringValue();
				if(datatype.contains("tring")){
					return LiteralStringBinding.class;
				}
				if(datatype.contains("ollean")){
					return LiteralBooleanBinding.class;
				}
				if(datatype.contains("loat") || datatype.contains("ouble")){
					return LiteralDoubleBinding.class;
				}
				if(datatype.contains("nt")){
					return LiteralIntBinding.class;
				}
			}
			if(output.isSetComplexOutput()){
				String mimeType = output.getComplexOutput().getDefault().getFormat().getMimeType();
				if(mimeType.contains("xml") || (mimeType.contains("XML"))){
					return GenericFileDataBinding.class;
				}else{
					return GenericFileDataBinding.class;
				}
			}
		}
		throw new RuntimeException("Could not determie internal inputDataType");
	}

	public Map<String, IData> run(Map<String, List<IData>> inputData) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
