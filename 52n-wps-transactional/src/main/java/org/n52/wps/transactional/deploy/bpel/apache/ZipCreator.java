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
package org.n52.wps.transactional.deploy.bpel.apache;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 *
 * @author hoffmannn
 */
public class ZipCreator {
    
    public static final int BUF_SIZE = 8192;

  public static final int STATUS_OK          = 0;
  public static final int STATUS_OUT_FAIL    = 1; // No output stream.
  public static final int STATUS_ZIP_FAIL    = 2; // No zipped file
  public static final int STATUS_IN_FAIL     = 4; // No input stream.
  public static final int STATUS_UNZIP_FAIL  = 5; // No decompressed zip file

  private static String fMessages [] = {
    "Operation succeeded",
    "Failed to create output stream",
    "Failed to create zipped file",
    "Failed to open input stream",
    "Failed to decompress zip file"
  };

  /** Return a brief message for each status number. **/
  public static String getStatusMessage (int msg_number) {
    return fMessages [msg_number];
  }
  
    public static int makeZIP(String processID, Node suitcase, Node workflow, Node clientWSDL, Map<Integer, Node> wsdlList, File zipFile) throws IOException, ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException{
    
    
    FileOutputStream zip_output = new FileOutputStream(zipFile);

    ZipOutputStream zip_out_stream = new ZipOutputStream(zip_output);
   
    try {
      // Use the file name for the ZipEntry name.
      ZipEntry zip_entry = new ZipEntry ("deploy.xml");
      zip_out_stream.putNextEntry (zip_entry);
      ByteArrayOutputStream outputStream = nodeToString(suitcase);
      zip_out_stream.write(outputStream.toByteArray());
      zip_out_stream.closeEntry();

      String bpelname = XPathAPI.selectSingleNode(workflow,"@name").getTextContent();
      outputStream = nodeToString(workflow);
//      outputStream = createOutputStreamFromNode(workflow);
      zip_out_stream.putNextEntry (new ZipEntry (bpelname + ".bpel"));
      zip_out_stream.write(outputStream.toByteArray());
      zip_out_stream.closeEntry();

      String wsdlname = XPathAPI.selectSingleNode(clientWSDL,"@name").getTextContent();
      outputStream = nodeToString(clientWSDL);
      zip_out_stream.putNextEntry (new ZipEntry (wsdlname + ".wsdl"));
      zip_out_stream.write(outputStream.toByteArray());
      zip_out_stream.closeEntry();
      
      for (Integer index : wsdlList.keySet()){
            outputStream = nodeToString(wsdlList.get(index));
            zip_out_stream.putNextEntry (new ZipEntry ("wps"+index+".wsdl"));
            zip_out_stream.write(outputStream.toByteArray());
            zip_out_stream.closeEntry();
      }
    }
    catch (IOException e) {
      return STATUS_ZIP_FAIL;
    }
    // Close up the output file
    try {
        zip_out_stream.flush();
        zip_out_stream.close ();
    }
    catch (IOException e) {}

    zip_output.flush();
    zip_output.close();
    
    return STATUS_OK;

  } // zipFile


    
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
    
	private static ByteArrayOutputStream nodeToString(Node node) throws TransformerFactoryConfigurationError, TransformerException {
		StringWriter stringWriter = new StringWriter();
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.transform(new DOMSource(node), new StreamResult(stringWriter));
		
		String s = stringWriter.toString();
		
		s = s.replaceAll("xmlns=\"\"", "");
		
		stringWriter = new StringWriter();
		
		stringWriter.append(s);
		
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		
		try {
			bOut.write(s.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return bOut;
	}
    
//	private static ByteArrayOutputStream nodeToString(Node node) throws TransformerFactoryConfigurationError, TransformerException {
//		Transformer transformer = TransformerFactory.newInstance().newTransformer();
//		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
//        
//		// Prepare the output file
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        Result result = new StreamResult(outputStream);
//		
//		transformer.transform(new DOMSource(node), result);
//		
//		return outputStream;
//	}
    
}
