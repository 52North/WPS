package org.n52.wps.transactional.deploy.bpel.oracle;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
 
public class JarCreator {
    
	public static ByteArrayOutputStream createJar(String processID, Node suitcase, Node workflow, Node clientWSDL, Map<Integer, Node> wsdlList) throws IOException, ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException{
		
		Manifest manifest =new Manifest();
		Attributes manifestAttr = manifest.getMainAttributes();
		manifestAttr.putValue("Manifest-Version","1.0");
		manifestAttr.putValue("Timestamp",""+System.currentTimeMillis());
		manifestAttr.putValue("Process-Id",processID);
		manifestAttr.putValue("Created-By","1.6.0 (Sun Microsystems Inc.)");
		manifestAttr.putValue("Revision-Tag","1.0");
		
		
		
	    ByteArrayOutputStream out = new ByteArrayOutputStream();;
			
		JarOutputStream jos = new JarOutputStream(out, manifest);
		//JarUtils.writeToJar((Resource) element.getValue(), (String) element.getKey(), jarStream);
		
		
		 
		
	    
	    JarEntry jarEntry=new JarEntry("BPEL-INF/");
	    jos.putNextEntry(jarEntry);
		jos.closeEntry();
		
		
		jos.putNextEntry(new JarEntry("bpel.xml"));
		ByteArrayOutputStream outputStream = createOutputStreamFromNode(suitcase);
		jos.write(outputStream.toByteArray());
		jos.closeEntry();
		
		outputStream = createOutputStreamFromNode(workflow);
		jos.putNextEntry(new JarEntry("workflow.bpel"));
		jos.write(outputStream.toByteArray());
		jos.closeEntry();
		
		outputStream = createOutputStreamFromNode(clientWSDL);
		jos.putNextEntry(new JarEntry("client.wsdl"));
		jos.write(outputStream.toByteArray());
		jos.closeEntry();
		
		
		for(Integer index : wsdlList.keySet()){
			outputStream = createOutputStreamFromNode(wsdlList.get(index));
			jos.putNextEntry(new JarEntry("wps"+index+".wsdl"));
			jos.write(outputStream.toByteArray());
			jos.closeEntry();
		}
		jos.flush();
		jos.close();
	    
		
		
	    return out;
		
		  
		    
		
	}

	private static ByteArrayOutputStream createOutputStreamFromNode(Node node) throws ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException {
		//System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "com.sun.org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = factory.newDocumentBuilder();
		Document tempDocument = documentBuilder.newDocument();
		Node importedNode = tempDocument.importNode(node, true);
		tempDocument.appendChild(importedNode);
        // Prepare the DOM document for writing
        Source source = new DOMSource(tempDocument);

        // Prepare the output file
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Result result = new StreamResult(outputStream);

        // Write the DOM document to the file
        Transformer xformer = TransformerFactory.newInstance().newTransformer();
        xformer.transform(source, result);
        return outputStream;
	}
	
	
public static void storeJar(String processID, Node suitcase, Node workflow, Node clientWSDL, Map<Integer, Node> wsdlList, String jarPath) throws IOException, ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException{
		
		Manifest manifest =new Manifest();
		Attributes manifestAttr = manifest.getMainAttributes();
		manifestAttr.putValue("Manifest-Version","1.0");
		manifestAttr.putValue("Timestamp",""+System.currentTimeMillis());
		manifestAttr.putValue("Process-Id",processID);
		manifestAttr.putValue("Created-By","1.6.0 (Sun Microsystems Inc.)");
		manifestAttr.putValue("Revision-Tag","1.0");
		
		FileOutputStream out = new FileOutputStream(jarPath);
		
		
			
		JarOutputStream jos = new JarOutputStream(out, manifest);
		  JarEntry jarEntry=new JarEntry("BPEL-INF/");
		    jos.putNextEntry(jarEntry);
			jos.closeEntry();
			
			
			
		
		jos.putNextEntry(new JarEntry("bpel.xml"));
		ByteArrayOutputStream outputStream = createOutputStreamFromNode(suitcase);
		jos.write(outputStream.toByteArray());
		jos.closeEntry();
		
		outputStream = createOutputStreamFromNode(workflow);
		jos.putNextEntry(new JarEntry("workflow.bpel"));
		jos.write(outputStream.toByteArray());
		jos.closeEntry();
		
		outputStream = createOutputStreamFromNode(clientWSDL);
		jos.putNextEntry(new JarEntry("client.wsdl"));
		jos.write(outputStream.toByteArray());
		jos.closeEntry();
		
		
		for(Integer index : wsdlList.keySet()){
			outputStream = createOutputStreamFromNode(wsdlList.get(index));
			jos.putNextEntry(new JarEntry("wps"+index+".wsdl"));
			jos.write(outputStream.toByteArray());
			jos.closeEntry();
		}
		jos.flush();
		jos.close();
	    
		
		
	   out.flush();
	   out.close();
		
		  
		    
		
	}
}

