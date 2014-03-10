/**
 * ﻿Copyright (C) 2007 - 2014 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.transactional.deploymentprofiles;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
import org.n52.wps.transactional.algorithm.GenericTransactionalAlgorithm;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;



public class BPELDeploymentProfile extends DeploymentProfile{
	private Node suitCase;
	private Node bpel;
	private Node clientWSDL;
	private Map<Integer, Node> wsdlList;
	
	
	public BPELDeploymentProfile(Node payload, String processID) {
		super(payload, processID);
		try {
		
			extractInformation(payload);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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

                //System.out.println("deployProcessDocument: ");
                //printNode(deployProcessDocument, "");
		//writeXmlFile(deployProcessDocument, "C:\\BPEL\\request.xml");
		
		//	1. suitcase
		//suitCase = XPathAPI.selectSingleNode(deployProcessDocument, "/DeployProcessRequest/BPELDeploymentProfile/SuitCase/BPELSuitcase");
                suitCase = XPathAPI.selectSingleNode(deployProcessDocument, "/DeployProcess/DeploymentProfile/SuitCase/BPELSuitcase/deploy");
		//select processName
		//String processName = XPathAPI.selectSingleNode(suitCase, "/DeployProcessRequest/BPELDeploymentProfile/SuitCase/BPELSuitcase/BPELProcess/@id").getTextContent();
                //System.out.println("suitCase: ");
                //printNode(suitCase, "");
                String processName ="";
                //Node pn;
                //    pn = XPathAPI.selectSingleNode(suitCase, "//deploy/process");
                try{
                    //processName = XPathAPI.selectSingleNode(suitCase, "//process/@name").getTextContent();
                    processName = XPathAPI.selectSingleNode(deployProcessDocument, "/DeployProcess/ProcessDescriptions/ProcessDescription/Identifier/text()").getNodeValue().trim();
                }catch(DOMException de){
                    de.printStackTrace();
                }catch(Exception e){
                    e.printStackTrace();
                }
		
		
		//	2. clientwsdl
		//clientWSDL = XPathAPI.selectSingleNode(deployProcessDocument, "/DeployProcessRequest/BPELDeploymentProfile/ProcessWSDL/definitions");
                clientWSDL = XPathAPI.selectSingleNode(deployProcessDocument, "/DeployProcess/DeploymentProfile/ProcessWSDL/definitions");
		//System.out.println("clientWSDL: ");
                //printNode(clientWSDL, "");
		
		//  3. other wsdl
		//Node tempWSDLList = XPathAPI.selectSingleNode(deployProcessDocument, "/DeployProcessRequest/BPELDeploymentProfile/WSDL-List");
                Node tempWSDLList = XPathAPI.selectSingleNode(deployProcessDocument, "/DeployProcess/DeploymentProfile/WSDL-List");
		//NodeIterator wsdlIterator = XPathAPI.selectNodeIterator(tempWSDLList, "/DeployProcessRequest/BPELDeploymentProfile/WSDL-List/definitions");
                NodeIterator wsdlIterator = XPathAPI.selectNodeIterator(tempWSDLList, "/DeployProcess/DeploymentProfile/WSDL-List/definitions");
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
		//bpel = XPathAPI.selectSingleNode(deployProcessDocument, "/DeployProcessRequest/BPELDeploymentProfile/BPEL/process");
                bpel = XPathAPI.selectSingleNode(deployProcessDocument, "/DeployProcess/DeploymentProfile/BPEL/process");
		//System.out.println("bpel: ");
                //printNode(bpel, "");
		
	
	

		
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

        public void printNode(Node node, String indent) {
		switch (node.getNodeType()) {
		case Node.DOCUMENT_NODE:
			System.out.println(indent + "<?xml version=\"1.0\"?>");

			NodeList nodes = node.getChildNodes();

			if (nodes != null) {
				for (int i = 0; i < nodes.getLength(); i++) {
					printNode(nodes.item(i), "");
				}
			}

			break;

		case Node.ELEMENT_NODE:

			String name = node.getNodeName();
			System.out.print(indent + "<" + name);

			NamedNodeMap attributes = node.getAttributes();

			for (int i = 0; i < attributes.getLength(); i++) {
				Node current = attributes.item(i);
				System.out.print(" " + current.getNodeName() +
				        "=\"" + current.getNodeValue() + "\"");
			}

			System.out.println(">");

			NodeList children = node.getChildNodes();

			if (children != null) {
				for (int i = 0; i < children.getLength();
					        i++) {
					printNode(children.item(i),
					        indent + "  ");
				}
			}

			System.out.println(indent + "</" + name + ">");

			break;

		case Node.TEXT_NODE:
		case Node.CDATA_SECTION_NODE:
			System.out.println(indent + node.getNodeValue());

			break;

		case Node.PROCESSING_INSTRUCTION_NODE:
			System.out.println(indent + "<?" + node.getNodeName() +
			        " " + node.getNodeValue() + " ?>");

			break;

		case Node.ENTITY_REFERENCE_NODE:
			System.out.println("&" + node.getNodeName() + ";");

			break;

		case Node.DOCUMENT_TYPE_NODE:

			DocumentType docType = (DocumentType) node;
			System.out.print("<!DOCTYPE " + docType.getName());

			if (docType.getPublicId() != null) {
				System.out.print("PUBLIC \"" +
				        docType.getPublicId() + "\"");
			} else {
				System.out.print(" SYSTEM ");
			}

			System.out.println("\"" + docType.getSystemId() +
			        "\" >");

			break;
		}
	}


}
