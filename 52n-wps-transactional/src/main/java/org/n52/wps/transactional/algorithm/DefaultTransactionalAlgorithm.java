package org.n52.wps.transactional.algorithm;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

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
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.OutputDataType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionsDocument;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.AbstractAlgorithm;
import org.n52.wps.server.AbstractTransactionalAlgorithm;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.transactional.deploy.IDeployManager;
import org.n52.wps.transactional.service.DefaultTransactionalProcessRepository;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class DefaultTransactionalAlgorithm extends AbstractTransactionalAlgorithm{
	
	private String error;
	private static Logger LOGGER = Logger.getLogger(AbstractAlgorithm.class);
	private ProcessDescriptionType processDescription;
	private String workspace;
	
	private static final String OGC_OWS_URI = "http://www.opengeospatial.net/ows";
	
	public DefaultTransactionalAlgorithm(String processID, Class<?> registeredRepository){
		super(processID);
		WPSConfig wpsConfig = WPSConfig.getInstance();
		Property[] properties = wpsConfig.getPropertiesForRepositoryClass(registeredRepository.getName());
		this.workspace = wpsConfig.getPropertyForKey(properties,"WorkspaceLocationRoot").getStringValue();
		this.error = "";
		
	}
	
	public ProcessDescriptionType getDescription()  {
		return initializeDescription();
	}
	
	
	public HashMap run(ExecuteDocument payload){
		Document responseDocument;
		HashMap<String,Object> resultHash = new HashMap<String,Object>();
		try {	
		//forward request
			
			//TODO get deploy manager class from config
			IDeployManager deployManager = null;
			
			responseDocument = deployManager.invoke(payload, getAlgorithmID());
			
			//1.parse results;
			
			//TODO make temporaryfile
		
			writeXmlFile(responseDocument,workspace+"\\BPEL\\serverside.xml");
			
			ExecuteResponseDocument executeResponseDocument = ExecuteResponseDocument.Factory.parse(new File(workspace+"\\BPEL\\serverside.xml"));
			
	//			2.look at each Output Element
			OutputDataType[] resultValues = executeResponseDocument.getExecuteResponse().getProcessOutputs().getOutputArray();
			for(int i = 0; i<resultValues.length;i++){
				OutputDataType ioElement  = resultValues[i];
				//3.get the identifier as key
				String key = ioElement.getIdentifier().getStringValue();
				//4.the the literal value as String
				if(ioElement.getData().getLiteralData()!=null){
					resultHash.put(key, OutputParser.handleLiteralValue(ioElement) );
				}
				//5.parse the complex value
				if(ioElement.getData().getComplexData()!=null){
					
					resultHash.put(key,  OutputParser.handleComplexValue(ioElement, getDescription()));
						
					
				}
				//6.parse the complex value reference
				if(ioElement.getReference()!=null){
					resultHash.put(key, OutputParser.handleComplexValueReference(ioElement));
				}
				
				//7.parse Bounding Box value
				if(ioElement.getData().getBoundingBoxData()!=null){
					resultHash.put(key, OutputParser.handleBBoxValue(ioElement));
				}
				
				
		}
		
		} catch (XmlException e) {
			error = "Could not create ExecuteResponseDocument";
			LOGGER.warn(error + " Reason: " +e.getMessage());
			throw new RuntimeException(error,e);
		} catch (ExceptionReport e) {
			error = e.getMessage();
			LOGGER.warn( "Error processing results. Reason: " +e.getMessage());
			throw new RuntimeException(error,e);
		} catch (RemoteException e) {
			error = e.getMessage();
			LOGGER.warn( "Error processing results. Reason: " +e.getMessage());
			throw new RuntimeException(error,e);
		} catch (Exception e) {
			error = e.getMessage();
			LOGGER.warn( "Error processing results. Reason: " +e.getMessage());
			throw new RuntimeException(error,e);
		} 
		
		//add response id
		

		return resultHash;
	}


	public String getErrors() {
		return error;
	}


	protected ProcessDescriptionType initializeDescription() {
		String fullPath =  DefaultTransactionalAlgorithm.class.getProtectionDomain().getCodeSource().getLocation().toString();
		int searchIndex= fullPath.indexOf("WEB-INF");
		String subPath = fullPath.substring(0, searchIndex);
		subPath = subPath.replaceFirst("file:/", "");
		try {
			File xmlDesc = new File(subPath+"\\WEB-INF\\ProcessDescriptions\\"+getAlgorithmID()+".xml");
			XmlOptions option = new XmlOptions();
			option.setLoadTrimTextBuffer();
			ProcessDescriptionsDocument doc = ProcessDescriptionsDocument.Factory.parse(xmlDesc, option);
			if(doc.getProcessDescriptions().getProcessDescriptionArray().length == 0) {
				LOGGER.warn("ProcessDescription does not contain any description");
				return null;
			}
			
			doc.getProcessDescriptions().getProcessDescriptionArray(0).getIdentifier().setStringValue(getAlgorithmID());


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

	/**
	 * Do nothing. Do everything in run(Document payload)
	 */
	public HashMap run(HashMap layers, HashMap parameters) {
		return new HashMap();
		
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
	

	private static void writeXmlFile(Document doc, String filename) {
        try {
        	if(filename==null){
        		filename = "C:\\BPEL\\serverside.xml";
        	}
            // Prepare the DOM document for writing
            Source source = new DOMSource(doc);
    
            // Prepare the output file
            File file = new File(filename);
            file.createNewFile();
            Result result = new StreamResult(file);
    
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

	public String getWellKnownName() {
		// TODO Auto-generated method stub
		return null;
	}

	public Map run(Map layers, Map parameters) {
		// TODO Auto-generated method stub
		return null;
	}

	

	
	
	
	
	
	
	
}
