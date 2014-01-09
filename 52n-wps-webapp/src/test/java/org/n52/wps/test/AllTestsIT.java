/**
 * ﻿Copyright (C) 2007
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.opengis.wps.x100.ComplexDataType;
import net.opengis.wps.x100.DataType;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.ExecuteResponseDocument.ExecuteResponse.ProcessOutputs;
import net.opengis.wps.x100.OutputDataType;

import org.apache.commons.codec.binary.Base64;
import org.geotools.coverage.grid.GridCoverage2D;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.datahandler.parser.GeotiffParser;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class AllTestsIT {

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
                    Thread.sleep(1000 * 10);
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

    public static void checkReferenceXMLResult(String response) throws ParserConfigurationException,
            SAXException,
            IOException {
        String referencedDocument = getRefAsString(response);
        assertThat(referencedDocument, referencedDocument, not(containsString("ExceptionReport")));
        assertThat(referencedDocument, referencedDocument, containsString("LinearRing"));
        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
    }

    public static void checkReferenceBinaryResultBase64(String response) throws ParserConfigurationException,
            SAXException,
            IOException {
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("ProcessSucceeded"));
        assertThat(response, response, containsString("Reference"));

        InputStream stream = getRefAsStream(response);
        GeotiffParser parser = new GeotiffParser();
        IData data = parser.parseBase64(stream, "image/tiff", null);
        assertThat(data.getPayload() instanceof GridCoverage2D, is(true));
        stream.close();
    }

    public static void checkReferenceBinaryResultDefault(String response) throws ParserConfigurationException,
            SAXException,
            IOException {
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("ProcessSucceeded"));
        assertThat(response, response, containsString("Reference"));

        InputStream stream = getRefAsStream(response);
        GeotiffParser parser = new GeotiffParser();
        IData data = parser.parse(stream, "image/tiff", null);
        assertThat(data.getPayload() instanceof GridCoverage2D, is(true));
        stream.close();
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
}