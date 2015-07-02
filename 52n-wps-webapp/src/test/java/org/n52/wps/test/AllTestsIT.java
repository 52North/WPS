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
package org.n52.wps.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.opengis.wps.x100.ComplexDataType;
import net.opengis.wps.x100.DataType;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.ExecuteResponseDocument.ExecuteResponse.ProcessOutputs;
import net.opengis.wps.x100.OutputDataType;
import net.opengis.wps.x100.OutputReferenceType;

import org.apache.commons.codec.binary.Base64;
import org.n52.wps.commons.WPSConfig;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class AllTestsIT {

    public static final String referenceComplexBinaryInputURL = AllTestsIT.getURL().replace(WPSConfig.SERVLET_PATH,
                                                                                     "static/testData/elev_srtm_30m21.tif");
    public static final String referenceComplexXMLInputURL = AllTestsIT.getURL().replace(WPSConfig.SERVLET_PATH,
                                                                                  "static/testData/test-data.xml");	
    private final static String TIFF_MAGIC = "II";

    public static int getPort() {
        return Integer.parseInt(System.getProperty("test.port", "8080"));
    }

    public static String getHost() {
        return System.getProperty("test.host", "127.0.0.1");
    }

    public static String getContext() {
        return System.getProperty("test.context", "/wps/WebProcessingService");
    }

    public static String getURL() {
        return "http://" + getHost() + ":" + getPort() + getContext();
    }

    public static String getWebappURL() {
        return "http://" + getHost() + ":" + getPort() + getContext().replace("/" + WPSConfig.SERVLET_PATH, "");
    }

    public static Document parseXML(String xmlString) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        StringReader inStream = new StringReader(xmlString);
        InputSource inSource = new InputSource(inStream);
        return documentBuilder.parse(inSource);
    }

    public static void validateBinaryBase64Async(String response) throws IOException,
            ParserConfigurationException,
            SAXException {
        String referencedDocument = getAsyncDoc(response);
        assertThat(referencedDocument, referencedDocument, not(containsString("ExceptionReport")));
        assertThat(referencedDocument, referencedDocument, containsString("ExecuteResponse"));
        assertThat(referencedDocument,
                   referencedDocument,
                   anyOf(containsString("AAEGAAMAAAABAAEAAAEVAAMAAAABA"),
                         containsString("Tk9SVEg6IDIyOD"),
                         containsString("SUkqAAgAAAASAAABAwABAAAAIwAA")));
    }

    public static String getRefAsString(String response) throws ParserConfigurationException, SAXException, IOException {
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("ProcessSucceeded"));
        assertThat(response, response, containsString("Reference"));

        Document doc = AllTestsIT.parseXML(response);
        NodeList executeResponse = doc.getElementsByTagName("wps:Reference");

        assertThat(executeResponse.getLength(), greaterThan(0));

        NamedNodeMap attributes = executeResponse.item(0).getAttributes();
        String statusLocation = attributes.getNamedItem("href").getNodeValue();
        String[] splittedURL = statusLocation.split("RetrieveResultServlet?");

        assertThat(splittedURL.length, equalTo(2));

        String referencedDocument = GetClient.sendRequest(splittedURL[0] + "RetrieveResultServlet", splittedURL[1]);

        return referencedDocument;
    }

    public static InputStream getRefAsStream(String response) throws ParserConfigurationException,
            SAXException,
            IOException {
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("ProcessSucceeded"));
        assertThat(response, response, containsString("Reference"));

        Document doc = AllTestsIT.parseXML(response);
        NodeList executeResponse = doc.getElementsByTagName("wps:Reference");

        assertThat(executeResponse.getLength(), greaterThan(0));

        NamedNodeMap attributes = executeResponse.item(0).getAttributes();
        String statusLocation = attributes.getNamedItem("href").getNodeValue();
        String[] splittedURL = statusLocation.split("RetrieveResultServlet?");

        assertThat(splittedURL.length, equalTo(2));

        return GetClient.sendRequestForInputStream(splittedURL[0] + "RetrieveResultServlet", splittedURL[1]);
    }

    public static String getAsyncDoc(String response) throws IOException, ParserConfigurationException, SAXException {
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("statusLocation"));

        Document doc;
        doc = AllTestsIT.parseXML(response);

        NodeList executeResponse = doc.getElementsByTagName("wps:ExecuteResponse");

        assertThat(executeResponse.getLength(), greaterThan(0));

        NamedNodeMap attributes = executeResponse.item(0).getAttributes();
        String statusLocation = attributes.getNamedItem("statusLocation").getNodeValue();
        String[] splittedURL = statusLocation.split("RetrieveResultServlet?");

        assertThat(splittedURL.length, equalTo(2));

        String referencedDocument = GetClient.sendRequest(splittedURL[0] + "RetrieveResultServlet", splittedURL[1]);

        assertThat(referencedDocument, referencedDocument, not(containsString("ExceptionReport")));
        assertThat(referencedDocument, referencedDocument, containsString("Status"));

        for (int i = 0; i < 4; i++) {
            if ( !referencedDocument.contains("ProcessSucceeded") && !referencedDocument.contains("ProcessFailed")) {
                try {
                    System.out.println("WPS process still processing. Waiting...");
                    Thread.sleep(1000 * 3);
                    referencedDocument = GetClient.sendRequest(splittedURL[0] + "RetrieveResultServlet", splittedURL[1]);
                }
                catch (InterruptedException ignore) {
                    // do nothing
                }
            }
            else {
                return referencedDocument;
            }
        }
        throw new IOException("Test did not complete in allotted time");
    }
	
	public static void checkContentDispositionOfRetrieveResultServlet(String response, String filename, String suffix)
			throws IOException, ParserConfigurationException, SAXException {

		String refResult = AllTestsIT.getAsyncDoc(response);
		
    	ExecuteResponseDocument document = null;
    	
    	try {    		
    		document = ExecuteResponseDocument.Factory.parse(refResult);	    		
		} catch (Exception e) {
			System.err.println("Could not parse execute response document.");
		}   	
    	
    	assertThat(document, not(nullValue()));    	
    	
    	ProcessOutputs outputs = document.getExecuteResponse().getProcessOutputs();
    	
    	assertThat(outputs, not(nullValue()));    	
    	assertThat(outputs.sizeOfOutputArray(), not(0)); 
    	
    	OutputDataType outputDataType = document.getExecuteResponse().getProcessOutputs().getOutputArray(0);
    	
    	assertThat(outputDataType, not(nullValue()));       	
    	
    	OutputReferenceType data = outputDataType.getReference();  
    	
    	assertThat(data, not(nullValue()));
    	
    	String url = data.getHref();
    	
    	if(filename != null){
    		//concat filename to URL
    		url = url.concat("&filename=" + filename);
    	}
    	
    	URLConnection urlConnection = new URL(url).openConnection();
    	
    	List<String> headerFields = urlConnection.getHeaderFields().get("Content-Disposition");
    	
    	boolean oneHeaderFieldContainsFilename = false;
    	
		for (String field : headerFields) {
			if(field.contains("filename")){
				oneHeaderFieldContainsFilename = true;
				if(suffix != null && !suffix.equals("")){
					assertTrue(field.endsWith(suffix + "\""));
				}
				if(filename != null && !filename.equals("")){
					assertTrue(field.contains(filename));
				}
			}
		}
		assertTrue(oneHeaderFieldContainsFilename);
	}

    public static void checkReferenceXMLResult(String response, String stringThatShouldBeContained) throws ParserConfigurationException,
            SAXException,
            IOException {
        String referencedDocument = getRefAsString(response);
        assertThat(referencedDocument, referencedDocument, not(containsString("ExceptionReport")));
        assertThat(referencedDocument, referencedDocument, containsString(stringThatShouldBeContained));
        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
    }
    
    public static void checkReferenceBinaryResultBase64(String response) throws ParserConfigurationException,
    SAXException,
    IOException {
    	assertThat(response, response, not(containsString("ExceptionReport")));
    	assertThat(response, response, containsString("ProcessSucceeded"));
    	assertThat(response, response, containsString("Reference"));
    	
    	String responseAsString = getRefAsString(response);
    	
    	assertTrue(Base64.isBase64(responseAsString));
    }
    
    public static void checkReferenceBinaryResultDefault(String response) throws ParserConfigurationException,
    SAXException,
    IOException {
    	assertThat(response, response, not(containsString("ExceptionReport")));
    	assertThat(response, response, containsString("ProcessSucceeded"));
    	assertThat(response, response, containsString("Reference"));
    	
    	String responseAsString = getRefAsString(response);
    	
    	assertThat(responseAsString, responseAsString, containsString(TIFF_MAGIC));
    }
    
    public static void checkInlineResultBase64(String response){
    	
    	ExecuteResponseDocument document = null;
    	
    	try {    		
    		document = ExecuteResponseDocument.Factory.parse(response);	    		
		} catch (Exception e) {
			System.err.println("Could not parse execute response document.");
		}   	
    	
    	assertThat(document, not(nullValue()));    	
    	
    	ProcessOutputs outputs = document.getExecuteResponse().getProcessOutputs();
    	
    	assertThat(outputs, not(nullValue()));    	
    	assertThat(outputs.sizeOfOutputArray(), not(0)); 
    	
    	OutputDataType outputDataType = document.getExecuteResponse().getProcessOutputs().getOutputArray(0);
    	
    	assertThat(outputDataType, not(nullValue()));       	
    	
    	DataType data = outputDataType.getData();
    	
    	assertTrue(data.isSetComplexData());    	
    	
    	ComplexDataType complexData = outputDataType.getData().getComplexData();
    	
    	assertThat(complexData, not(nullValue())); 
    	
    	Node domNode = complexData.getDomNode();
    	
    	assertThat(domNode, not(nullValue()));       
    	
    	Node firstChild = domNode.getFirstChild();
    	
    	assertThat(firstChild, not(nullValue()));       
    	
    	String nodeValue = firstChild.getNodeValue();
    	
    	assertThat(nodeValue, not(nullValue()));   
		
		assertTrue(Base64.isBase64(nodeValue));
    	
    }
    
    public static void checkRawBinaryResultBase64(InputStream stream){

        String responseAsString = saveInputStreamToString(stream);
        
        assertTrue(Base64.isBase64(responseAsString));
    }

    public static void checkRawBinaryResultDefault(InputStream stream){

    	String responseAsString = saveInputStreamToString(stream);
        
        assertThat(responseAsString, responseAsString, containsString(TIFF_MAGIC));
    }
    
    public static String saveInputStreamToString(InputStream stream){
		
    	StringBuilder stringBuilder = new StringBuilder();
    	
    	try {
    		
    		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
    		
    		String line = null;
    		
    		while ((line = bufferedReader.readLine()) !=  null) {
				stringBuilder.append(line);
			}
    		
		} catch (Exception e) {
			System.err.println("Could not save inputstream content to String.");
		} finally{    		
    		try {
				stream.close();
			} catch (IOException e) {
                //
			}
		}
    	
		return stringBuilder.toString();
		
    }
}