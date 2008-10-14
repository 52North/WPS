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


package org.n52.wps.transactional.deploymentprofiles;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xpath.XPathAPI;
import org.n52.wps.transactional.algorithm.DefaultTransactionalAlgorithm;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

public class BPELDeploymentProfile extends DeploymentProfile{
	private Node suitCase;
	private Node bpel;
	private Node clientWSDL;
	private Map<Integer, Node> wsdlList;
	
	
	public BPELDeploymentProfile(Node payload, String processID) {
		super(payload, processID);
		
	}

	public Node getSuitCase() {
		return suitCase;
	}

	public Node getBPEL() {
		return bpel;
	}

	public Node getClientWSDL() {
		return clientWSDL;
	}

	public Map<Integer, Node> getWSDLList() {
		return wsdlList;
	}
	
	
	private void extractInformation(Node deployProcessDocument) throws Exception {
		//parse out
		//	1. suitcase
		//	2. clientwsdl
		//  3. other wsdl
		//  4. bpel
		//	5.create describe process
		
		
		//	1. suitcase
		suitCase = XPathAPI.selectSingleNode(deployProcessDocument, "/DeployProcess/SuitCase/BPELSuitcase");
		//select processName
		String processName = XPathAPI.selectSingleNode(suitCase, "/DeployProcess/SuitCase/BPELSuitcase/BPELProcess/@id").getTextContent();
			
		
		
		//	2. clientwsdl
		clientWSDL = XPathAPI.selectSingleNode(deployProcessDocument, "/DeployProcess/ClientWSDL/definitions");
		
		
		//  3. other wsdl
		Node tempWSDLList = XPathAPI.selectSingleNode(deployProcessDocument, "/DeployProcess/WSDL-List");
		NodeIterator wsdlIterator = XPathAPI.selectNodeIterator(tempWSDLList, "/DeployProcess/WSDL-List/definitions");
		//Note: WSDL files are written in the correct order
		wsdlList = new HashMap<Integer, Node>();
		int index = 0;
		while(true){
			Node wsdl = wsdlIterator.nextNode();
			if(wsdl == null){
				break;
			}
			wsdlList.put(index, wsdl);
			index = index +1;
		}
		
		
		//  4. bpel
		bpel = XPathAPI.selectSingleNode(deployProcessDocument, "/DeployProcess/BPEL/process");
			
			
		//	  5. create describe process
		Node describeProcess = XPathAPI.selectSingleNode(deployProcessDocument, "/DeployProcess/DescribeProcessDocument/ProcessDescriptions");
		String fullPath =  DefaultTransactionalAlgorithm.class.getProtectionDomain().getCodeSource().getLocation().toString();
		int searchIndex= fullPath.indexOf("WEB-INF");
		String subPath = fullPath.substring(0, searchIndex);
		subPath = subPath.replaceFirst("file:", "");
		writeXmlFile(describeProcess, subPath+"\\WEB-INF\\ProcessDescriptions\\"+processName+".xml");
		
		
	}
	

		
	private void writeXmlFile(Node node, String filename) throws IOException, TransformerFactoryConfigurationError, TransformerException, ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = factory.newDocumentBuilder();
		Document tempDocument = documentBuilder.newDocument();
		Node importedNode = tempDocument.importNode(node, true);
		tempDocument.appendChild(importedNode);
        // Prepare the DOM document for writing
        Source source = new DOMSource(tempDocument);

        // Prepare the output file
        File file = new File(filename);
        String parent = file.getParent();
        File directory = new File(parent);
        directory.mkdirs();
        //file.createNewFile();
        Result result = new StreamResult(file);

        // Write the DOM document to the file
        Transformer xformer = TransformerFactory.newInstance().newTransformer();
        xformer.transform(source, result);
    
	
	
		}
	
	private void writeXmlFile(Document doc, String filename) {
        try {
        	
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
	

}
