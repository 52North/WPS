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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import net.opengis.ows.x11.ExceptionReportDocument;
import net.opengis.wps.x100.ComplexDataType;
import net.opengis.wps.x100.DataType;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.ExecuteResponseDocument.ExecuteResponse.ProcessOutputs;
import net.opengis.wps.x100.InputType;
import net.opengis.wps.x100.LiteralDataType;
import net.opengis.wps.x100.OutputDataType;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n52.wps.client.ExecuteRequestBuilder;
import org.n52.wps.client.WPSClientException;
import org.n52.wps.client.WPSClientSession;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class ExecutePostIT {

    private final static String TIFF_MAGIC = "<![CDATA[II";
    private static String url;
    private ExecuteRequestBuilder echoProcessExecuteRequestBuilder;
    private final String echoProcessIdentifier = "org.n52.wps.server.algorithm.test.EchoProcess";
    private final String echoProcessInlineComplexXMLInput = "<TestData><this><is><xml><Data>Test</Data></xml></is></this></TestData>";
    private final String testDataNodeName = "TestData";    
    private final String echoProcessLiteralInputID = "literalInput";
    private final String echoProcessLiteralInputString = "testData";
    private final String echoProcessComplexInputID = "complexInput";
    private final String echoProcessComplexMimeTypeTextXML = "text/xml";
    private final String echoProcessComplexOutputID = "complexOutput";
    private final String echoProcessLiteralOutputID = "literalOutput";    

    private ExecuteRequestBuilder multiReferenceBinaryInputAlgorithmExecuteRequestBuilder;
    private final String multiReferenceBinaryInputAlgorithmIdentifier = "org.n52.wps.server.algorithm.test.MultiReferenceBinaryInputAlgorithm";
    private final String multiReferenceBinaryInputAlgorithmComplexInputID = "data";
    private final String multiReferenceBinaryInputAlgorithmComplexOutputID = "result";
    private final String multiReferenceBinaryInputAlgorithmComplexMimeTypeImageTiff= "image/tiff";
    private final String base64TiffStart= "SUkqAAgAAAASAAA";
    
    private String tiffImageBinaryInputAsBase64String;
    
    @BeforeClass
    public static void beforeClass() throws XmlException, IOException {
        url = AllTestsIT.getURL();
        
    }
    
    @Before
    public void before(){
        
		WPSClientSession wpsClient = WPSClientSession.getInstance();

		ProcessDescriptionType echoProcessDescription;
		try {
			echoProcessDescription = wpsClient
					.getProcessDescription(url, echoProcessIdentifier);
			
			echoProcessExecuteRequestBuilder = new ExecuteRequestBuilder(echoProcessDescription);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		assertThat(echoProcessExecuteRequestBuilder, is(not(nullValue())));
		
		ProcessDescriptionType multiReferenceBinaryInputAlgorithmDescription;
		
		try {
			multiReferenceBinaryInputAlgorithmDescription = wpsClient
					.getProcessDescription(url, multiReferenceBinaryInputAlgorithmIdentifier);
			
			multiReferenceBinaryInputAlgorithmExecuteRequestBuilder = new ExecuteRequestBuilder(multiReferenceBinaryInputAlgorithmDescription);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		assertThat(multiReferenceBinaryInputAlgorithmExecuteRequestBuilder, is(not(nullValue())));
		
		InputStream tiffImageInputStream = getClass().getResourceAsStream("/Execute/image.tiff.base64");
		
		BufferedReader tiffImageInputStreamReader = new BufferedReader(new InputStreamReader(tiffImageInputStream));
		
		StringBuilder tiffImageInputStringBuilder = new StringBuilder();
		
		String line = "";
		
		try {
			while ((line = tiffImageInputStreamReader.readLine()) != null) {
				tiffImageInputStringBuilder.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		tiffImageBinaryInputAsBase64String = tiffImageInputStringBuilder.toString();
		
		assertThat(tiffImageBinaryInputAsBase64String, is(not(nullValue())));
		assertThat(tiffImageBinaryInputAsBase64String, is(not(equalTo(""))));		
    	
    }
    
    /*Complex inline XML input */
    @Test
    public void testExecutePOSTInlineComplexXMLSynchronousXMLOutput() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTInlineComplexXMLSynchronousXMLOutput");
        
        try {
			echoProcessExecuteRequestBuilder.addComplexData(echoProcessComplexInputID, echoProcessInlineComplexXMLInput, null, null, echoProcessComplexMimeTypeTextXML);

			echoProcessExecuteRequestBuilder.setResponseDocument(echoProcessComplexOutputID, null, null, echoProcessComplexMimeTypeTextXML);
			
			Object responseObject =  WPSClientSession.getInstance().execute(url, echoProcessExecuteRequestBuilder.getExecute());
			
	        assertThat(responseObject, is(not(nullValue())));
	        assertThat(responseObject, is(not(instanceOf(ExceptionReportDocument.class))));
	        
	        if (responseObject instanceof ExecuteResponseDocument) {
				
	        	ExecuteResponseDocument executeResponseDocument = (ExecuteResponseDocument)responseObject;	
	        	
	        	checkIdentifier(executeResponseDocument, echoProcessComplexOutputID);
	        	
	        	checkIfResultContainsTestXMLData(executeResponseDocument);
	        	
			}
		} catch (WPSClientException e) {
			e.printStackTrace();
		}     
    }
    
    /*Complex XML input by reference */
    @Test
    public void testExecutePOSTreferenceComplexXMLSynchronousXMLOutput() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTreferenceComplexXMLSynchronousXMLOutput");
        
        try {
        	echoProcessExecuteRequestBuilder.addComplexDataReference(echoProcessComplexInputID, AllTestsIT.referenceComplexXMLInputURL, null, null, echoProcessComplexMimeTypeTextXML);

			echoProcessExecuteRequestBuilder.setResponseDocument(echoProcessComplexOutputID, null, null, echoProcessComplexMimeTypeTextXML);
			
			Object responseObject =  WPSClientSession.getInstance().execute(url, echoProcessExecuteRequestBuilder.getExecute());
			
	        assertThat(responseObject, is(not(nullValue())));
	        assertThat(responseObject, is(not(instanceOf(ExceptionReportDocument.class))));
	        
	        if (responseObject instanceof ExecuteResponseDocument) {
				
	        	ExecuteResponseDocument executeResponseDocument = (ExecuteResponseDocument)responseObject;	
	        	
	        	checkIdentifier(executeResponseDocument, echoProcessComplexOutputID);
	        	
	        	checkIfResultContainsTestXMLData(executeResponseDocument);
	        	
			}
		} catch (WPSClientException e) {
			e.printStackTrace();
		}     
    }    
    
    /*Complex XML Input by reference using a post request*/
    @Test
    public void testExecutePOSTreferencePOSTComplexXMLSynchronousXMLOutput() throws IOException, ParserConfigurationException, SAXException {
    	System.out.println("\nRunning testExecutePOSTreferencePOSTComplexXMLSynchronousXMLOutput");
    	
    	echoProcessExecuteRequestBuilder.addComplexDataReference(echoProcessComplexInputID,  AllTestsIT.referenceComplexXMLInputURL, null, null, echoProcessComplexMimeTypeTextXML);

		echoProcessExecuteRequestBuilder.setRawData(echoProcessComplexOutputID, null, null, echoProcessComplexMimeTypeTextXML);
		    	
    	String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
    			+"<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
    			+"	http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
    			+"	<ows:Identifier>org.n52.wps.server.algorithm.test.EchoProcess</ows:Identifier>"
    			+"	<wps:DataInputs>"
    			+"		<wps:Input>"
    			+"			<ows:Identifier>complexInput</ows:Identifier>"
    			+"			<wps:Reference mimeType=\"text/xml\" xlink:href=\"" + AllTestsIT.getURL() + "\">"
    			+"			<wps:Body>"
    			+ echoProcessExecuteRequestBuilder.getExecute().toString()
    			+"			</wps:Body>"
    			+"			</wps:Reference>"
    			+"		</wps:Input>"
    			+"	</wps:DataInputs>"
    			+"	<wps:ResponseForm>"
    			+"	<wps:ResponseDocument storeExecuteResponse=\"false\">"
    			+"		<wps:Output asReference=\"false\">"
    			+"			<ows:Identifier>complexOutput</ows:Identifier>"
    			+"		</wps:Output>"
    			+"	</wps:ResponseDocument>"
    			+"	</wps:ResponseForm>"
    			+"</wps:Execute>";
    	String response = PostClient.sendRequest(url, payload);
    	
    	assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
    	assertThat(response, response, not(containsString("ExceptionReport")));
    	assertThat(response, response, containsString(testDataNodeName));
    }
    
    /*Multiple complex XML Input by reference */
    @Test
    public void testExecutePOSTMultipleReferenceComplexXMLSynchronousXMLOutput() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTMultipleReferenceComplexXMLSynchronousXMLOutput");
        
        try {
        	echoProcessExecuteRequestBuilder.addComplexDataReference(echoProcessComplexInputID, AllTestsIT.referenceComplexXMLInputURL, null, null, echoProcessComplexMimeTypeTextXML);
        	echoProcessExecuteRequestBuilder.addComplexDataReference(echoProcessComplexInputID, AllTestsIT.referenceComplexXMLInputURL, null, null, echoProcessComplexMimeTypeTextXML);

			echoProcessExecuteRequestBuilder.setResponseDocument(echoProcessComplexOutputID, null, null, echoProcessComplexMimeTypeTextXML);
			
			Object responseObject =  WPSClientSession.getInstance().execute(url, echoProcessExecuteRequestBuilder.getExecute());
			
	        assertThat(responseObject, is(not(nullValue())));
	        assertThat(responseObject, is(not(instanceOf(ExceptionReportDocument.class))));
	        
	        if (responseObject instanceof ExecuteResponseDocument) {
				
	        	ExecuteResponseDocument executeResponseDocument = (ExecuteResponseDocument)responseObject;	
	        	
	        	checkIdentifier(executeResponseDocument, echoProcessComplexOutputID);
	        	
	        	checkIfResultContainsTestXMLData(executeResponseDocument);	        	
			}
		} catch (WPSClientException e) {
			e.printStackTrace();
		}
    }
    
    private InputType createComplexInlineInput(String identifier, String value, String schema, String encoding, String mimeType){
    	
    	InputType inputType = InputType.Factory.newInstance();
		
		inputType.addNewIdentifier().setStringValue(identifier);
		
		try {
			
			ComplexDataType data = inputType.addNewData().addNewComplexData();
			
			XmlOptions xmlOptions = new XmlOptions();
			
			data.set(XmlObject.Factory.parse(value, xmlOptions));
			if (schema != null) {
				data.setSchema(schema);
			}
			if (mimeType != null) {
				data.setMimeType(mimeType);
			}
			if (encoding != null) {
				data.setEncoding(encoding);
			}
		} catch (XmlException e) {
			throw new IllegalArgumentException(
					"error inserting node into complexdata", e);
		}
		
		return inputType;
    	
    }
    
    private Object createAndSubmitMultiReferenceBinaryInputAlgorithmExecuteWithResponseDocument(List<InputType> inputs, boolean status, boolean storeSupport, boolean asReference) throws WPSClientException{    	
    	
        multiReferenceBinaryInputAlgorithmExecuteRequestBuilder.addComplexDataReference(multiReferenceBinaryInputAlgorithmComplexInputID,
                                                                                        AllTestsIT.referenceComplexBinaryInputURL,
                                                                                        null,
                                                                                        null,
                                                                                        multiReferenceBinaryInputAlgorithmComplexMimeTypeImageTiff);
		
        multiReferenceBinaryInputAlgorithmExecuteRequestBuilder.addComplexDataReference(multiReferenceBinaryInputAlgorithmComplexInputID,
                                                                                        AllTestsIT.referenceComplexBinaryInputURL,
                                                                                        null,
                                                                                        null,
                                                                                        multiReferenceBinaryInputAlgorithmComplexMimeTypeImageTiff);

		multiReferenceBinaryInputAlgorithmExecuteRequestBuilder.setResponseDocument(multiReferenceBinaryInputAlgorithmComplexOutputID, null, "base64", multiReferenceBinaryInputAlgorithmComplexMimeTypeImageTiff);
		
		multiReferenceBinaryInputAlgorithmExecuteRequestBuilder.setStoreSupport(multiReferenceBinaryInputAlgorithmComplexOutputID, storeSupport);
		multiReferenceBinaryInputAlgorithmExecuteRequestBuilder.setStatus(multiReferenceBinaryInputAlgorithmComplexOutputID, status);
		multiReferenceBinaryInputAlgorithmExecuteRequestBuilder.setAsReference(multiReferenceBinaryInputAlgorithmComplexOutputID, asReference);		
		
		Object responseObject =  WPSClientSession.getInstance().execute(url, multiReferenceBinaryInputAlgorithmExecuteRequestBuilder.getExecute());
		
		return responseObject; 
    }
    
    /*Multiple complex XML Input by reference */
    @Test
    public void testExecutePOSTMultipleReferenceComplexBinarySynchronousBinaryOutput() throws IOException, ParserConfigurationException, SAXException {
    	System.out.println("\nRunning testExecutePOSTMultipleReferenceComplexBinarySynchronousBinaryOutput");

        try {
			
			Object responseObject =  createAndSubmitMultiReferenceBinaryInputAlgorithmExecuteWithResponseDocument(false, false, false);
			
	        assertThat(responseObject, is(not(nullValue())));
	        assertThat(responseObject, is(not(instanceOf(ExceptionReportDocument.class))));
	        
	        if (responseObject instanceof ExecuteResponseDocument) {
				
	        	ExecuteResponseDocument executeResponseDocument = (ExecuteResponseDocument)responseObject;	
	        	
	        	checkIdentifier(executeResponseDocument, multiReferenceBinaryInputAlgorithmComplexOutputID);       	
			}
		} catch (WPSClientException e) {
			e.printStackTrace();
		}    	
    }
    
    /*Complex XML Input by reference, POST*/
    @Test
    public void testExecutePOSTReferenceComplexXMLSynchronousXMLOutput_WFS_POST_MissingMimeType() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTReferenceComplexXMLSynchronousXMLOutput_WFS_POST_MissingMimeType");
        
    	echoProcessExecuteRequestBuilder.addComplexDataReference(echoProcessComplexInputID, AllTestsIT.referenceComplexXMLInputURL, null, null, echoProcessComplexMimeTypeTextXML);

		echoProcessExecuteRequestBuilder.setRawData(echoProcessComplexOutputID, null, null, echoProcessComplexMimeTypeTextXML);
        
    	String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
    			+"<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
    			+"	http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
    			+"	<ows:Identifier>org.n52.wps.server.algorithm.test.EchoProcess</ows:Identifier>"
    			+"	<wps:DataInputs>"
    			+"		<wps:Input>"
    			+"			<ows:Identifier>complexInput</ows:Identifier>"
    			+"			<wps:Reference xlink:href=\"" + AllTestsIT.getURL() + "\">"
    			+"			<wps:Body>"
    			+ echoProcessExecuteRequestBuilder.getExecute().toString()
    			+"			</wps:Body>"
    			+"			</wps:Reference>"
    			+"		</wps:Input>"
    			+"	</wps:DataInputs>"
    			+"	<wps:ResponseForm>"
    			+"	<wps:ResponseDocument storeExecuteResponse=\"false\">"
    			+"		<wps:Output asReference=\"false\">"
    			+"			<ows:Identifier>complexOutput</ows:Identifier>"
    			+"		</wps:Output>"
    			+"	</wps:ResponseDocument>"
    			+"	</wps:ResponseForm>"
    			+"</wps:Execute>";
    	String response = PostClient.sendRequest(url, payload);
    	
    	assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
    	assertThat(response, response, not(containsString("ExceptionReport")));
    	assertThat(response, response, containsString(testDataNodeName));
    }

    /*Complex binary Input by value */
    @Test
    public void testExecutePOSTInlineComplexBinaryASynchronousBinaryOutput() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTInlineComplexBinaryASynchronousBinaryOutput");
        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.test.MultiReferenceBinaryInputAlgorithm</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>data</ows:Identifier>"
                + "<wps:Data>"
                + "<wps:ComplexData mimeType=\"image/tiff\" encoding=\"base64\">"
                + tiffImageBinaryInputAsBase64String
                + "</wps:ComplexData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "<wps:Input>"
                + "<ows:Identifier>data</ows:Identifier>"
                + "<wps:Data>"
                + "<wps:ComplexData mimeType=\"image/tiff\" encoding=\"base64\">"
                + tiffImageBinaryInputAsBase64String
                + "</wps:ComplexData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"true\" storeExecuteResponse=\"true\">"
                + "<wps:Output encoding=\"base64\" mimeType=\"image/tiff\">"
                + "<ows:Identifier>result</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        AllTestsIT.validateBinaryBase64Async(response);
    }

    /*Complex binary Input by value */
    @Test
    public void testExecutePOSTInlineAndReferenceComplexBinaryASynchronousBinaryOutput() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTInlineAndReferenceComplexBinaryASynchronousBinaryOutput");
        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.test.MultiReferenceBinaryInputAlgorithm</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>data</ows:Identifier>"
                + "<wps:Data>"
                + "<wps:ComplexData mimeType=\"image/tiff\" encoding=\"base64\">"
                + tiffImageBinaryInputAsBase64String
                + "</wps:ComplexData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "<wps:Input>"
                + "<ows:Identifier>data</ows:Identifier>"
                + "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"" + AllTestsIT.referenceComplexBinaryInputURL + "\">"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"true\" storeExecuteResponse=\"true\">"
                + "<wps:Output encoding=\"base64\" mimeType=\"image/tiff\">"
                + "<ows:Identifier>result</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        AllTestsIT.validateBinaryBase64Async(response);
    }
    
    /*Complex binary Input by reference */
    @Test
    public void testExecutePOSTReferenceComplexBinaryASynchronousBinaryOutput() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTReferenceComplexBinaryASynchronousBinaryOutput");
        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.test.MultiReferenceBinaryInputAlgorithm</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>data</ows:Identifier>"
                + "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"" + AllTestsIT.referenceComplexBinaryInputURL
                + "\">"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "<wps:Input>"
                + "<ows:Identifier>data</ows:Identifier>"
                + "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"" + AllTestsIT.referenceComplexBinaryInputURL
                + "\">"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"true\" storeExecuteResponse=\"true\">"
                + "<wps:Output encoding=\"base64\" >"
                + "<ows:Identifier>result</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        AllTestsIT.validateBinaryBase64Async(response);
    }

    /*Literal Input by value String */
    @Test
    public void testExecutePOSTLiteralStringSynchronousXMLOutput() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTLiteralStringSynchronousXMLOutput");
        
        try {
//        	echoProcessExecuteRequestBuilder.addComplexDataReference(echoProcessComplexInputID, echoProcessReferenceComplexXMLInput, null, null, echoProcessComplexMimeTypeTextXML);
        	echoProcessExecuteRequestBuilder.addLiteralData(echoProcessLiteralInputID, echoProcessLiteralInputString);

			echoProcessExecuteRequestBuilder.setResponseDocument(echoProcessLiteralOutputID, null, null, null);
			
			Object responseObject =  WPSClientSession.getInstance().execute(url, echoProcessExecuteRequestBuilder.getExecute());
			
	        assertThat(responseObject, is(not(nullValue())));
	        assertThat(responseObject, is(not(instanceOf(ExceptionReportDocument.class))));
	        
	        if (responseObject instanceof ExecuteResponseDocument) {
				
	        	ExecuteResponseDocument executeResponseDocument = (ExecuteResponseDocument)responseObject;	
	        	
	        	checkIdentifier(executeResponseDocument, echoProcessLiteralOutputID);
	        	
	        	checkIfResultContainsTestStringData(executeResponseDocument);
	        	
			}
		} catch (WPSClientException e) {
			e.printStackTrace();
		}
    }

    /*BBOX Input by value */
    @Test
    public void testExecutePOSTinlineBBOXDataInputTestSynchronousBBOXOutput() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTinlineBBOXDataInputTestSynchronousBBOXOutput");
        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 "
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.test.DummyTestClass</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>BBOXInputData</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:BoundingBoxData crs=\"EPSG:4326\">"
                + "<ows:LowerCorner>46.75 13.05</ows:LowerCorner>"
                + "<ows:UpperCorner>46.85 13.25</ows:UpperCorner>"
                + "</wps:BoundingBoxData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"false\" storeExecuteResponse=\"false\">"
                + "<wps:Output asReference=\"false\">"
                + "<ows:Identifier>BBOXOutputData</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("46.75 13.05"));
        assertThat(response, response, containsString("EPSG"));
    }

    /*BBOX Input by value NO EPSG*/
    @Test
    public void testExecutePOSTinlineBBOXDataInputTestSynchronousBBOXOutputNoEPSG() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTinlineBBOXDataInputTestSynchronousBBOXOutputNoEPSG");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 "
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.test.DummyTestClass</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>BBOXInputData</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:BoundingBoxData>"
                + "<ows:LowerCorner>46.75 13.05</ows:LowerCorner>"
                + "<ows:UpperCorner>46.85 13.25</ows:UpperCorner>"
                + "</wps:BoundingBoxData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"false\" storeExecuteResponse=\"false\">"
                + "<wps:Output asReference=\"false\">"
                + "<ows:Identifier>BBOXOutputData</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, not(containsString("EPSG")));
        assertThat(response, response, containsString("46.75 13.05"));
    }

    /*Complex XML Output by value TODO: this could be checked with the testExecutePOSTInlineComplexXMLSynchronousXMLOutput*/
    @Test
    public void testExecutePOSTComplexXMLSynchronousXMLOutput() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTComplexXMLSynchronousXMLOutput");

        try {
			echoProcessExecuteRequestBuilder.addComplexData(echoProcessComplexInputID, echoProcessInlineComplexXMLInput, null, null, echoProcessComplexMimeTypeTextXML);

			echoProcessExecuteRequestBuilder.setResponseDocument(echoProcessComplexOutputID, null, null, echoProcessComplexMimeTypeTextXML);
			
			Object responseObject =  WPSClientSession.getInstance().execute(url, echoProcessExecuteRequestBuilder.getExecute());
			
	        assertThat(responseObject, is(not(nullValue())));
	        assertThat(responseObject, is(not(instanceOf(ExceptionReportDocument.class))));
	        
	        if (responseObject instanceof ExecuteResponseDocument) {
				
	        	ExecuteResponseDocument executeResponseDocument = (ExecuteResponseDocument)responseObject;	
	        	
	        	checkIdentifier(executeResponseDocument, echoProcessComplexOutputID);
	        	
	        	checkIfResultContainsTestXMLData(executeResponseDocument);	        	
			}
		} catch (WPSClientException e) {
			e.printStackTrace();
		}   
    }

    /*Complex XML Output by reference*/
    @Test
    public void testExecutePOSTComplexXMLSynchronousXMLOutputByReference() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTComplexXMLSynchronousXMLOutputByReference");

        try {
			
			Object responseObject =  createAndSubmitEchoProcessExecuteWithResponseDocument(false, false, true);
			
	        assertThat(responseObject, is(not(nullValue())));
	        assertThat(responseObject, is(not(instanceOf(ExceptionReportDocument.class))));
	        
	        if (responseObject instanceof ExecuteResponseDocument) {
				
	        	ExecuteResponseDocument executeResponseDocument = (ExecuteResponseDocument)responseObject;	
	        	
	        	checkIdentifier(executeResponseDocument, echoProcessComplexOutputID);
	        	
	        	AllTestsIT.checkReferenceXMLResult(executeResponseDocument.toString(), testDataNodeName);
			}
		} catch (WPSClientException e) {
			e.printStackTrace();
		}
    }
    
    /*Complex inline XML Output*/
    @Test
    public void testExecutePOSTComplexXMLSynchronousXMLOutputStatusTrue() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTComplexXMLSynchronousXMLOutputStatusTrue");

        try {
			
			Object responseObject =  createAndSubmitEchoProcessExecuteWithResponseDocument(true, false, false);
			
	        assertThat(responseObject, is(not(nullValue())));
	        assertThat(responseObject, is(not(instanceOf(ExceptionReportDocument.class))));
	        
			if (responseObject instanceof ExecuteResponseDocument) {

				ExecuteResponseDocument executeResponseDocument = (ExecuteResponseDocument) responseObject;

				checkIdentifier(executeResponseDocument,
						echoProcessComplexOutputID);

				String response = executeResponseDocument.toString();

				assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
				assertThat(response, response, not(containsString("ExceptionReport")));
				assertThat(response, response, containsString(testDataNodeName));
				assertThat(response, response, containsString("ProcessSucceeded"));
			}
		} catch (WPSClientException e) {
			e.printStackTrace();
		}
    }
    
    @Test
    public void testExecutePOSTComplexXMLASynchronousXMLOutputStoreStatusTrue() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTComplexXMLASynchronousXMLOutputStoreStatusTrue");

        try {
			
			Object responseObject =  createAndSubmitEchoProcessExecuteWithResponseDocument(true, true, false);
			
	        assertThat(responseObject, is(not(nullValue())));
	        assertThat(responseObject, is(not(instanceOf(ExceptionReportDocument.class))));
	        
			if (responseObject instanceof ExecuteResponseDocument) {

				ExecuteResponseDocument executeResponseDocument = (ExecuteResponseDocument) responseObject;

				String response = executeResponseDocument.toString();
				
		        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
		        assertThat(response, response, containsString("statusLocation"));
		
		        String refResult = AllTestsIT.getAsyncDoc(response);
		        assertThat(refResult, refResult, containsString(echoProcessComplexOutputID));
		        assertThat(refResult, refResult, containsString(testDataNodeName));
			}
		} catch (WPSClientException e) {
			e.printStackTrace();
		}
    }

    @Test
    public void testExecutePOSTComplexXMLASynchronousXMLOutputStoreTrue() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTComplexXMLASynchronousXMLOutputStoreTrue");

        try {
			
			Object responseObject =  createAndSubmitEchoProcessExecuteWithResponseDocument(false, true, false);
			
	        assertThat(responseObject, is(not(nullValue())));
	        assertThat(responseObject, is(not(instanceOf(ExceptionReportDocument.class))));
	        
			if (responseObject instanceof ExecuteResponseDocument) {

				ExecuteResponseDocument executeResponseDocument = (ExecuteResponseDocument) responseObject;

				String response = executeResponseDocument.toString();
				
		        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
		        assertThat(response, response, containsString("statusLocation"));
		
		        String refResult = AllTestsIT.getAsyncDoc(response);
		        assertThat(refResult, refResult, containsString(echoProcessComplexOutputID));
		        assertThat(refResult, refResult, containsString(testDataNodeName));
			}
		} catch (WPSClientException e) {
			e.printStackTrace();
		}
    }

    @Test
    public void testExecutePOSTComplexXMLASynchronousXMLOutputReferenceStoreTrue() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTComplexXMLASynchronousXMLOutputReferenceStoreTrue");

        try {
			
			Object responseObject =  createAndSubmitEchoProcessExecuteWithResponseDocument(false, true, true);
			
	        assertThat(responseObject, is(not(nullValue())));
	        assertThat(responseObject, is(not(instanceOf(ExceptionReportDocument.class))));
	        
			if (responseObject instanceof ExecuteResponseDocument) {

				ExecuteResponseDocument executeResponseDocument = (ExecuteResponseDocument) responseObject;

				String response = executeResponseDocument.toString();
				
		        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
		        assertThat(response, response, containsString("statusLocation"));
		
		        String doc = AllTestsIT.getAsyncDoc(response);
		        String refResult = AllTestsIT.getRefAsString(doc);
		        assertThat(refResult, refResult, containsString(testDataNodeName));
			}
		} catch (WPSClientException e) {
			e.printStackTrace();
		}
    }

    @Test
    public void testExecutePOSTComplexXMLASynchronousXMLOutputByReferenceStatusStoreTrue() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTComplexXMLASynchronousXMLOutputByReferenceStatusStoreTrue");

        try {
			
			Object responseObject =  createAndSubmitEchoProcessExecuteWithResponseDocument(true, true, true);
			
	        assertThat(responseObject, is(not(nullValue())));
	        assertThat(responseObject, is(not(instanceOf(ExceptionReportDocument.class))));
	        
			if (responseObject instanceof ExecuteResponseDocument) {

				ExecuteResponseDocument executeResponseDocument = (ExecuteResponseDocument) responseObject;

				String response = executeResponseDocument.toString();
				
		        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
		        assertThat(response, response, containsString("statusLocation"));
		
		        String doc = AllTestsIT.getAsyncDoc(response);
		        String refResult = AllTestsIT.getRefAsString(doc);
		        assertThat(refResult, refResult, containsString(testDataNodeName));
			}
		} catch (WPSClientException e) {
			e.printStackTrace();
		}
    }

    @Test
    public void testExecutePOSTComplexXMLASynchronousRawXMLOutput() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTComplexXMLASynchronousRawXMLOutput");
        
        try {
			
			Object responseObject =  createAndSubmitEchoProcessExecuteWithRawData();
			
	        assertThat(responseObject, is(not(nullValue())));
	        assertThat(responseObject, is(not(instanceOf(ExceptionReportDocument.class))));
	        
			if (responseObject instanceof ExecuteResponseDocument) {

				ExecuteResponseDocument executeResponseDocument = (ExecuteResponseDocument) responseObject;

				String response = executeResponseDocument.toString();
				
		        assertThat(response, response, containsString(testDataNodeName));
		        assertThat(response, response, not(containsString("Execute")));
			}
		} catch (WPSClientException e) {
			e.printStackTrace();
		}
    }
    
    @Test
    public void testExecutePOSTComplexBinaryASynchronousBinaryOutputStoreStatusReferenceTrueHasCorrectSuffix() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTComplexBinaryASynchronousBinaryOutputStoreStatusReferenceTrueHasCorrectSuffix");

        try {
			
			Object responseObject =  createAndSubmitMultiReferenceBinaryInputAlgorithmExecuteWithResponseDocument(false, true, true);
			
	        assertThat(responseObject, is(not(nullValue())));
	        assertThat(responseObject, is(not(instanceOf(ExceptionReportDocument.class))));
	        
	        if (responseObject instanceof ExecuteResponseDocument) {
				
	        	ExecuteResponseDocument executeResponseDocument = (ExecuteResponseDocument)responseObject;	
	        	
	        	String response = executeResponseDocument.toString();
	        	
	            assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
	            assertThat(response, response, not(containsString("ExceptionReport")));
	            assertThat(response, response, containsString("statusLocation"));
	            
	            AllTestsIT.checkContentDispositionOfRetrieveResultServlet(response, null, ".tiff");
			}
		} catch (WPSClientException e) {
			e.printStackTrace();
		}
    }
    
    @Test
    public void testExecutePOSTComplexBinaryASynchronousBinaryOutputStoreStatusReferenceTrueHasCorrectFilename() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTComplexBinaryASynchronousBinaryOutputStoreStatusReferenceTrueHasCorrectFilename");

        try {
			
			Object responseObject =  createAndSubmitMultiReferenceBinaryInputAlgorithmExecuteWithResponseDocument(false, true, true);
			
	        assertThat(responseObject, is(not(nullValue())));
	        assertThat(responseObject, is(not(instanceOf(ExceptionReportDocument.class))));
	        
	        if (responseObject instanceof ExecuteResponseDocument) {
				
	        	ExecuteResponseDocument executeResponseDocument = (ExecuteResponseDocument)responseObject;	
	        	
	        	String response = executeResponseDocument.toString();
	        	
	            assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
	            assertThat(response, response, not(containsString("ExceptionReport")));
	            assertThat(response, response, containsString("statusLocation"));
	            
	            AllTestsIT.checkContentDispositionOfRetrieveResultServlet(response, "result", ".tiff");
			}
		} catch (WPSClientException e) {
			e.printStackTrace();
		}
    }

    @Test
    public void testExecutePOSTValueComplexBinarySynchronousBinaryOutputBase64() throws ParserConfigurationException, IOException, SAXException {
        System.out.println("\nRunning testExecutePOSTValueComplexBinarySynchronousBinaryOutputBase64");

        try {
			
			Object responseObject =  createAndSubmitMultiReferenceBinaryInputAlgorithmExecuteWithResponseDocument(false, false, false);
			
	        assertThat(responseObject, is(not(nullValue())));
	        assertThat(responseObject, is(not(instanceOf(ExceptionReportDocument.class))));
	        
	        if (responseObject instanceof ExecuteResponseDocument) {
				
	        	ExecuteResponseDocument executeResponseDocument = (ExecuteResponseDocument)responseObject;	
	        	
	        	String response = executeResponseDocument.toString();

	            assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
	            assertThat(response, response, not(containsString("ExceptionReport")));
	            assertThat(response, response, containsString("ExecuteResponse"));	            
	            AllTestsIT.checkInlineResultBase64(response);
			}
		} catch (WPSClientException e) {
			e.printStackTrace();
		}
    }

    @Test
    public void testExecutePOSTValueComplexBinarySynchronousBinaryOutputNoEncoding() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTValueComplexBinarySynchronousBinaryOutputNoEncoding");

        try {
			
			Object responseObject =  createAndSubmitMultiReferenceBinaryInputAlgorithmExecuteWithResponseDocument(false, false, false, null);
			
	        assertThat(responseObject, is(not(nullValue())));
	        assertThat(responseObject, is(not(instanceOf(ExceptionReportDocument.class))));
	        
	        if (responseObject instanceof ExecuteResponseDocument) {
				
	        	ExecuteResponseDocument executeResponseDocument = (ExecuteResponseDocument)responseObject;	
	        	
	        	String response = executeResponseDocument.toString();

	            assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
	            assertThat(response, response, not(containsString("ExceptionReport")));
	            assertThat(response, response, containsString("ExecuteResponse"));
	            assertThat(response, response, containsString(base64TiffStart));
			}
		} catch (WPSClientException e) {
			e.printStackTrace();
		}
    }

    @Test
    public void testExecutePOSTValueComplexBinarySynchronousBinaryOutputAsReferenceBase64() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTValueComplexBinarySynchronousBinaryOutputAsReferenceBase64");

        try {
			
			Object responseObject =  createAndSubmitMultiReferenceBinaryInputAlgorithmExecuteWithResponseDocument(false, false, true);
			
	        assertThat(responseObject, is(not(nullValue())));
	        assertThat(responseObject, is(not(instanceOf(ExceptionReportDocument.class))));
	        
	        if (responseObject instanceof ExecuteResponseDocument) {
				
	        	ExecuteResponseDocument executeResponseDocument = (ExecuteResponseDocument)responseObject;	
	        	
	        	String response = executeResponseDocument.toString();

	            assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
	            assertThat(response, response, not(containsString("ExceptionReport")));
	            assertThat(response, response, containsString("ExecuteResponse"));
	            AllTestsIT.checkReferenceBinaryResultBase64(response);
			}
		} catch (WPSClientException e) {
			e.printStackTrace();
		}
    }

    @Test
    public void testExecutePOSTValueComplexBinarySynchronousBinaryOutputAsReferenceNoEncoding() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTValueComplexBinarySynchronousBinaryOutputAsReferenceNoEncoding");

        try {
			
			Object responseObject =  createAndSubmitMultiReferenceBinaryInputAlgorithmExecuteWithResponseDocument(false, false, true, null);
			
	        assertThat(responseObject, is(not(nullValue())));
	        assertThat(responseObject, is(not(instanceOf(ExceptionReportDocument.class))));
	        
	        if (responseObject instanceof ExecuteResponseDocument) {
				
	        	ExecuteResponseDocument executeResponseDocument = (ExecuteResponseDocument)responseObject;	
	        	
	        	String response = executeResponseDocument.toString();

	            assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
	            assertThat(response, response, not(containsString("ExceptionReport")));
	            AllTestsIT.checkReferenceBinaryResultDefault(response);
			}
		} catch (WPSClientException e) {
			e.printStackTrace();
		}
    }

    @Test
    public void testExecutePOSTValueComplexBinarySynchronousBinaryOutputStatusBase64() throws ParserConfigurationException, SAXException, IOException {
        System.out.println("\nRunning testExecutePOSTValueComplexBinarySynchronousBinaryOutputStatusBase64");

        try {
			
			Object responseObject =  createAndSubmitMultiReferenceBinaryInputAlgorithmExecuteWithResponseDocument(true, false, false);
			
	        assertThat(responseObject, is(not(nullValue())));
	        assertThat(responseObject, is(not(instanceOf(ExceptionReportDocument.class))));
	        
	        if (responseObject instanceof ExecuteResponseDocument) {
				
	        	ExecuteResponseDocument executeResponseDocument = (ExecuteResponseDocument)responseObject;	
	        	
	        	String response = executeResponseDocument.toString();
	        	
	            assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
	            assertThat(response, response, not(containsString("ExceptionReport")));
	            assertThat(response, response, containsString("ExecuteResponse"));
	            assertThat(response, response, containsString("Status"));
	            AllTestsIT.checkInlineResultBase64(response);
			}
		} catch (WPSClientException e) {
			e.printStackTrace();
		}
    }

    @Test
    public void testExecutePOSTValueComplexBinarySynchronousBinaryOutputStatusNoEncoding() throws ParserConfigurationException, SAXException, IOException {
        System.out.println("\nRunning testExecutePOSTValueComplexBinarySynchronousBinaryOutputStatusNoEncoding");

        try {
			
			Object responseObject =  createAndSubmitMultiReferenceBinaryInputAlgorithmExecuteWithResponseDocument(true, false, false, null);
			
	        assertThat(responseObject, is(not(nullValue())));
	        assertThat(responseObject, is(not(instanceOf(ExceptionReportDocument.class))));
	        
	        if (responseObject instanceof ExecuteResponseDocument) {
				
	        	ExecuteResponseDocument executeResponseDocument = (ExecuteResponseDocument)responseObject;	
	        	
	        	String response = executeResponseDocument.toString();
	        	
	            assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
	            assertThat(response, response, not(containsString("ExceptionReport")));
	            assertThat(response, response, containsString("ProcessSucceeded"));
	            assertThat(response, response, containsString(base64TiffStart));
			}
		} catch (WPSClientException e) {
			e.printStackTrace();
		}
    }

    @Test
    public void testExecutePOSTValueComplexBinarySynchronousBinaryOutputAsReferenceStatusBase64() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTValueComplexBinarySynchronousBinaryOutputAsReferenceStatusBase64");

        try {
			
			Object responseObject =  createAndSubmitMultiReferenceBinaryInputAlgorithmExecuteWithResponseDocument(true, false, true);
			
	        assertThat(responseObject, is(not(nullValue())));
	        assertThat(responseObject, is(not(instanceOf(ExceptionReportDocument.class))));
	        
	        if (responseObject instanceof ExecuteResponseDocument) {
				
	        	ExecuteResponseDocument executeResponseDocument = (ExecuteResponseDocument)responseObject;	
	        	
	        	String response = executeResponseDocument.toString();
	        	
	            assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
	            assertThat(response, response, not(containsString("ExceptionReport")));
	            assertThat(response, response, containsString("Status"));
	            AllTestsIT.checkReferenceBinaryResultBase64(response);
			}
		} catch (WPSClientException e) {
			e.printStackTrace();
		}
    }

    @Test
    public void testExecutePOSTValueComplexBinarySynchronousBinaryOutputAsReferenceStatusNoEncoding() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTValueComplexBinarySynchronousBinaryOutputAsReferenceStatusNoEncoding");

        try {
			
			Object responseObject =  createAndSubmitMultiReferenceBinaryInputAlgorithmExecuteWithResponseDocument(true, false, true, null);
			
	        assertThat(responseObject, is(not(nullValue())));
	        assertThat(responseObject, is(not(instanceOf(ExceptionReportDocument.class))));
	        
	        if (responseObject instanceof ExecuteResponseDocument) {
				
	        	ExecuteResponseDocument executeResponseDocument = (ExecuteResponseDocument)responseObject;	
	        	
	        	String response = executeResponseDocument.toString();
	        	
	            assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
	            assertThat(response, response, not(containsString("ExceptionReport")));
	            assertThat(response, response, containsString("Status"));
	            AllTestsIT.checkReferenceBinaryResultDefault(response);
			}
		} catch (WPSClientException e) {
			e.printStackTrace();
		}
    }

    @Test
    public void testExecutePOSTValueComplexBinaryASynchronousBinaryOutputStoreBase64() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTValueComplexBinaryASynchronousBinaryOutputStoreBase64");
        
        try {
			
			Object responseObject =  createAndSubmitMultiReferenceBinaryInputAlgorithmExecuteWithResponseDocument(false, true, false);
			
	        assertThat(responseObject, is(not(nullValue())));
	        assertThat(responseObject, is(not(instanceOf(ExceptionReportDocument.class))));
	        
	        if (responseObject instanceof ExecuteResponseDocument) {
				
	        	ExecuteResponseDocument executeResponseDocument = (ExecuteResponseDocument)responseObject;	
	        	
	        	String response = executeResponseDocument.toString();
	        	
	            assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
	            assertThat(response, response, not(containsString("ExceptionReport")));
	            assertThat(response, response, containsString("Status"));
	            AllTestsIT.validateBinaryBase64Async(response);
			}
		} catch (WPSClientException e) {
			e.printStackTrace();
		}
    }

    @Test
    public void testExecutePOSTValueComplexBinaryASynchronousBinaryOutputStoreNoEncoding() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTValueComplexBinaryASynchronousBinaryOutputStoreNoEncoding");
        
        try {
			
			Object responseObject =  createAndSubmitMultiReferenceBinaryInputAlgorithmExecuteWithResponseDocument(false, true, false, null);
			
	        assertThat(responseObject, is(not(nullValue())));
	        assertThat(responseObject, is(not(instanceOf(ExceptionReportDocument.class))));
	        
	        if (responseObject instanceof ExecuteResponseDocument) {
				
	        	ExecuteResponseDocument executeResponseDocument = (ExecuteResponseDocument)responseObject;	
	        	
	        	String response = executeResponseDocument.toString();
	        	
	            String async = AllTestsIT.getAsyncDoc(response);
	            assertThat(AllTestsIT.parseXML(async), is(not(nullValue())));
	            assertThat(async, async, not(containsString("ExceptionReport")));
	            assertThat(async, async, containsString("ProcessSucceeded"));
	            assertThat(async, async, containsString(base64TiffStart));
			}
		} catch (WPSClientException e) {
			e.printStackTrace();
		}
    }

    @Test
    public void testExecutePOSTValueComplexBinaryASynchronousBinaryOutputStoreReferenceBase64() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTValueComplexBinaryASynchronousBinaryOutputStoreReferenceBase64");

        try {
			
			Object responseObject =  createAndSubmitMultiReferenceBinaryInputAlgorithmExecuteWithResponseDocument(false, true, true);
			
	        assertThat(responseObject, is(not(nullValue())));
	        assertThat(responseObject, is(not(instanceOf(ExceptionReportDocument.class))));
	        
	        if (responseObject instanceof ExecuteResponseDocument) {
				
	        	ExecuteResponseDocument executeResponseDocument = (ExecuteResponseDocument)responseObject;	
	        	
	        	String response = executeResponseDocument.toString();
	        	
	            String asynDoc = AllTestsIT.getAsyncDoc(response);
	            assertThat(AllTestsIT.parseXML(asynDoc), is(not(nullValue())));
	            assertThat(asynDoc, asynDoc, not(containsString("ExceptionReport")));
	            AllTestsIT.checkReferenceBinaryResultBase64(asynDoc);
			}
		} catch (WPSClientException e) {
			e.printStackTrace();
		}
    }

    @Test
    public void testExecutePOSTValueComplexBinaryASynchronousBinaryOutputStoreReferenceNoEncoding() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTValueComplexBinaryASynchronousBinaryOutputStoreReferenceNoEncoding");

        try {
			
			Object responseObject =  createAndSubmitMultiReferenceBinaryInputAlgorithmExecuteWithResponseDocument(false, true, true, null);
			
	        assertThat(responseObject, is(not(nullValue())));
	        assertThat(responseObject, is(not(instanceOf(ExceptionReportDocument.class))));
	        
	        if (responseObject instanceof ExecuteResponseDocument) {
				
	        	ExecuteResponseDocument executeResponseDocument = (ExecuteResponseDocument)responseObject;	
	        	
	        	String response = executeResponseDocument.toString();
	        	
	            assertThat(response, response, not(containsString("ExceptionReport")));
	            assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
	            String asynDoc = AllTestsIT.getAsyncDoc(response);
	            AllTestsIT.checkReferenceBinaryResultDefault(asynDoc);
			}
		} catch (WPSClientException e) {
			e.printStackTrace();
		}
    }

    @Test
    public void testExecutePOSTValueComplexBinaryASynchronousBinaryOutputStoreStatusBase64() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTValueComplexBinaryASynchronousBinaryOutputStoreStatusBase64");

        try {
			
			Object responseObject =  createAndSubmitMultiReferenceBinaryInputAlgorithmExecuteWithResponseDocument(true, true, false);
			
	        assertThat(responseObject, is(not(nullValue())));
	        assertThat(responseObject, is(not(instanceOf(ExceptionReportDocument.class))));
	        
	        if (responseObject instanceof ExecuteResponseDocument) {
				
	        	ExecuteResponseDocument executeResponseDocument = (ExecuteResponseDocument)responseObject;	
	        	
	        	String response = executeResponseDocument.toString();
	        	
	            assertThat(response, response, not(containsString("ExceptionReport")));
	            assertThat(response, response, containsString("Status"));
	            AllTestsIT.validateBinaryBase64Async(response);
			}
		} catch (WPSClientException e) {
			e.printStackTrace();
		}
    }

    @Test
    public void testExecutePOSTValueComplexBinaryASynchronousBinaryOutputStoreStatusNoEncoding() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTValueComplexBinaryASynchronousBinaryOutputStoreStatusNoEncoding");

        try {
			
			Object responseObject =  createAndSubmitMultiReferenceBinaryInputAlgorithmExecuteWithResponseDocument(true, true, false, null);
			
	        assertThat(responseObject, is(not(nullValue())));
	        assertThat(responseObject, is(not(instanceOf(ExceptionReportDocument.class))));
	        
	        if (responseObject instanceof ExecuteResponseDocument) {
				
	        	ExecuteResponseDocument executeResponseDocument = (ExecuteResponseDocument)responseObject;	
	        	
	        	String response = executeResponseDocument.toString();

	            String asyncDoc = AllTestsIT.getAsyncDoc(response);
	            assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
	            assertThat(asyncDoc, asyncDoc, containsString("ProcessSucceeded"));
	            assertThat(asyncDoc, asyncDoc, containsString(base64TiffStart));
			}
		} catch (WPSClientException e) {
			e.printStackTrace();
		}
    }

    @Test
    public void testExecutePOSTValueComplexBinaryASynchronousBinaryOutputRawBase64() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTValueComplexBinaryASynchronousBinaryOutputRawBase64");

        try {
			
			Object responseObject =  createAndSubmitMultiReferenceBinaryInputAlgorithmExecuteWithResponseDocument(true, true, true);
			
	        assertThat(responseObject, is(not(nullValue())));
	        assertThat(responseObject, is(not(instanceOf(ExceptionReportDocument.class))));
	        
	        if (responseObject instanceof ExecuteResponseDocument) {
				
	        	ExecuteResponseDocument executeResponseDocument = (ExecuteResponseDocument)responseObject;	
	        	
	        	String response = executeResponseDocument.toString();
	        	assertThat(response, response, not(containsString("ExceptionReport")));
	        	assertThat(response, response, containsString("Status"));
	        	assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
	        	String refDoc = AllTestsIT.getAsyncDoc(response);
	        	AllTestsIT.checkReferenceBinaryResultBase64(refDoc);
			}
		} catch (WPSClientException e) {
			e.printStackTrace();
		}
    }

    @Test
    public void testExecutePOSTValueComplexBinaryASynchronousBinaryOutputReferenceStoreStatusNoEncoding() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTValueComplexBinaryASynchronousBinaryOutputReferenceStoreStatusNoEncoding");

        try {
			
			Object responseObject =  createAndSubmitMultiReferenceBinaryInputAlgorithmExecuteWithResponseDocument(true, true, true, null);
			
	        assertThat(responseObject, is(not(nullValue())));
	        assertThat(responseObject, is(not(instanceOf(ExceptionReportDocument.class))));
	        
	        if (responseObject instanceof ExecuteResponseDocument) {
				
	        	ExecuteResponseDocument executeResponseDocument = (ExecuteResponseDocument)responseObject;	
	        	
	        	String response = executeResponseDocument.toString();

	            assertThat(response, response, not(containsString("ExceptionReport")));
	            assertThat(response, response, containsString("Status"));
	            assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
	            String refDoc = AllTestsIT.getAsyncDoc(response);
	            AllTestsIT.checkReferenceBinaryResultDefault(refDoc);
			}
		} catch (WPSClientException e) {
			e.printStackTrace();
		}
    }

    @Test
    public void testExecutePOSTValueComplexBinarySynchronousBinaryOutputRawbase64() throws IOException {
        System.out.println("\nRunning testExecutePOSTValueComplexBinarySynchronousBinaryOutputRawbase64");

        try {
			
			Object responseObject =  createAndSubmitMultiReferenceBinaryInputAlgorithmExecuteWithRawData(true, true, true, "base64");
			
	        assertThat(responseObject, is(not(nullValue())));
	        assertThat(responseObject, is(not(instanceOf(ExceptionReportDocument.class))));
	        
	        if (responseObject instanceof InputStream) {
				
	        	AllTestsIT.checkRawBinaryResultBase64((InputStream) responseObject);
			}
		} catch (WPSClientException e) {
			e.printStackTrace();
		}
    }

    @Test
    public void testExecutePOSTValueComplexBinarySynchronousBinaryOutputRawNoEncoding() throws IOException {
        System.out.println("\nRunning testExecutePOSTValueComplexBinarySynchronousBinaryOutputRawNoEncoding");

        try {
			
			Object responseObject =  createAndSubmitMultiReferenceBinaryInputAlgorithmExecuteWithRawData(true, true, true, null);
			
	        assertThat(responseObject, is(not(nullValue())));
	        assertThat(responseObject, is(not(instanceOf(ExceptionReportDocument.class))));
	        
	        if (responseObject instanceof InputStream) {
				
	        	AllTestsIT.checkRawBinaryResultDefault((InputStream) responseObject);
			}
		} catch (WPSClientException e) {
			e.printStackTrace();
		}
    }

    @Test
    public void testExecutePOSTinlineLiteralDataSynchronousLiteralOutput() throws IOException {
        System.out.println("\nRunning testExecutePOSTinlineLiteralDataSynchronousLiteralOutput");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 "
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.test.DummyTestClass</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>LiteralInputData</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:LiteralData>007</wps:LiteralData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"false\" storeExecuteResponse=\"false\">"
                + "<wps:Output asReference=\"false\">"
                + "<ows:Identifier>LiteralOutputData</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("007"));
    }

    @Test
    public void testExecutePOSTinlineLiteralDataSynchronousLiteralOutputStatus() throws IOException {
        System.out.println("\nRunning testExecutePOSTinlineLiteralDataSynchronousLiteralOutputStatus");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 "
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.test.DummyTestClass</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>LiteralInputData</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:LiteralData>007</wps:LiteralData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"true\" storeExecuteResponse=\"false\">"
                + "<wps:Output asReference=\"false\">"
                + "<ows:Identifier>LiteralOutputData</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("Status"));
        assertThat(response, response, containsString("007"));
    }
    
    @Test
    public void testExecutePOSTinlineLiteralDataASynchronousLiteralOutputStoreStatus() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTinlineLiteralDataASynchronousLiteralOutputStoreStatus");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 "
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.test.LongRunningDummyTestClass</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>LiteralInputData</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:LiteralData>007</wps:LiteralData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"true\" storeExecuteResponse=\"true\">"
                + "<wps:Output asReference=\"true\">"
                + "<ows:Identifier>LiteralOutputData</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        
        String response = PostClient.sendRequest(url, payload);
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("Status"));
        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        String refDoc = AllTestsIT.getAsyncDoc(response);
        assertThat(refDoc, refDoc, containsString("Status"));
        assertThat(refDoc, refDoc, containsString("007"));
    }

    @Test
    public void testExecutePOSTinlineLiteralDataASynchronousLiteralOutputStore() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTinlineLiteralDataASynchronousLiteralOutputStore");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 "
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.test.DummyTestClass</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>LiteralInputData</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:LiteralData>007</wps:LiteralData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"false\" storeExecuteResponse=\"true\">"
                + "<wps:Output asReference=\"false\">"
                + "<ows:Identifier>LiteralOutputData</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, not(containsString("ExceptionReport")));
        
        String asyncDoc = AllTestsIT.getAsyncDoc(response);
        assertThat(asyncDoc, asyncDoc, containsString("007"));
    }

    @Test
    public void testExecutePOSTinlineLiteralDataSynchronousLiteralOutputRaw() throws IOException {
        System.out.println("\nRunning testExecutePOSTinlineLiteralDataSynchronousLiteralOutputRaw");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 "
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.test.DummyTestClass</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>LiteralInputData</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:LiteralData>007</wps:LiteralData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:RawDataOutput>"
                + "<ows:Identifier>LiteralOutputData</ows:Identifier>"
                + "</wps:RawDataOutput>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, not(containsString("Response")));
        assertThat(response, response, containsString("007"));
    }
    
    @Test
    public void testExecutePOSTinlineLiteralDataSynchronousLiteralOutputUOM() throws IOException {
    	System.out.println("\nRunning testExecutePOSTinlineLiteralDataSynchronousLiteralOutputUOM");
    	
    	String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
    			+ "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 "
    			+ "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
    			+ "<ows:Identifier>org.n52.wps.server.algorithm.test.DummyTestClass</ows:Identifier>"
    			+ "<wps:DataInputs>"
    			+ "<wps:Input>"
    			+ "<ows:Identifier>LiteralInputData</ows:Identifier>"
    			+ "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
    			+ "<wps:Data>"
    			+ "<wps:LiteralData uom=\"m\">007</wps:LiteralData>"
    			+ "</wps:Data>"
    			+ "</wps:Input>"
    			+ "</wps:DataInputs>"
    			+ "<wps:ResponseForm>"
    			+ "<wps:ResponseDocument status=\"false\" storeExecuteResponse=\"false\">"
    			+ "<wps:Output asReference=\"false\">"
    			+ "<ows:Identifier>LiteralOutputData</ows:Identifier>"
    			+ "</wps:Output>"
    			+ "</wps:ResponseDocument>"
    			+ "</wps:ResponseForm>"
    			+ "</wps:Execute>";
    	String response = PostClient.sendRequest(url, payload);
    	assertThat(response, response, not(containsString("ExceptionReport")));
    	assertThat(response, response, containsString("007"));
    	assertThat(response, response, containsString("uom=\"m\""));
    }

    @Test
    public void testExecutePOSTinlineBBOXDataSynchronousBBOXOutput() throws IOException {
        System.out.println("\nRunning testExecutePOSTinlineBBOXDataSynchronousBBOXOutput");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 "
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.test.DummyTestClass</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>BBOXInputData</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:BoundingBoxData crs=\"EPSG:4326\">"
                + "<ows:LowerCorner>46.75 13.05</ows:LowerCorner>"
                + "<ows:UpperCorner>46.85 13.25</ows:UpperCorner>"
                + "</wps:BoundingBoxData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"false\" storeExecuteResponse=\"false\">"
                + "<wps:Output asReference=\"false\">"
                + "<ows:Identifier>BBOXOutputData</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("46.75 13.05"));
        assertThat(response, response, containsString("EPSG:4326"));
    }

    @Test
    public void testExecutePOSTinlineBBOXDataSynchronousBBOXOutputStatus() throws IOException {
        System.out.println("\nRunning testExecutePOSTinlineBBOXDataSynchronousBBOXOutputStatus");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 "
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.test.DummyTestClass</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>BBOXInputData</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:BoundingBoxData crs=\"EPSG:4326\">"
                + "<ows:LowerCorner>46.75 13.05</ows:LowerCorner>"
                + "<ows:UpperCorner>46.85 13.25</ows:UpperCorner>"
                + "</wps:BoundingBoxData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"true\" storeExecuteResponse=\"false\">"
                + "<wps:Output asReference=\"false\">"
                + "<ows:Identifier>BBOXOutputData</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("Status"));
        assertThat(response, response, containsString("46.75 13.05"));
        assertThat(response, response, containsString("EPSG:4326"));
    }

    @Test
    public void testExecutePOSTinlineBBOXDataASynchronousBBOXOutputStore() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTinlineBBOXDataASynchronousBBOXOutputStore");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 "
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.test.DummyTestClass</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>BBOXInputData</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:BoundingBoxData crs=\"EPSG:4326\">"
                + "<ows:LowerCorner>46.75 13.05</ows:LowerCorner>"
                + "<ows:UpperCorner>46.85 13.25</ows:UpperCorner>"
                + "</wps:BoundingBoxData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"false\" storeExecuteResponse=\"true\">"
                + "<wps:Output asReference=\"false\">"
                + "<ows:Identifier>BBOXOutputData</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        assertThat(response, response, not(containsString("ExceptionReport")));
        String asyncDoc = AllTestsIT.getAsyncDoc(response);
        assertThat(asyncDoc, asyncDoc, containsString("46.75 13.05"));
        assertThat(asyncDoc, asyncDoc, containsString("EPSG:4326"));
    }

    @Test
    public void testExecutePOSTinlineBBOXDataASynchronousBBOXOutputStoreStatus() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTinlineBBOXDataASynchronousBBOXOutputStoreStatus");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 "
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.test.DummyTestClass</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>BBOXInputData</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:BoundingBoxData crs=\"EPSG:4326\">"
                + "<ows:LowerCorner>46.75 13.05</ows:LowerCorner>"
                + "<ows:UpperCorner>46.85 13.25</ows:UpperCorner>"
                + "</wps:BoundingBoxData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"false\" storeExecuteResponse=\"true\">"
                + "<wps:Output asReference=\"false\">"
                + "<ows:Identifier>BBOXOutputData</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("Status"));
        
        String asyncDoc = AllTestsIT.getAsyncDoc(response);
        assertThat(asyncDoc, asyncDoc, containsString("46.75 13.05"));
        assertThat(asyncDoc, asyncDoc, containsString("EPSG:4326"));
    }

    @Test
    public void testExecutePOSTinlineBBOXDataSynchronousBBOXOutputRaw() throws IOException {
        System.out.println("\nRunning testExecutePOSTinlineBBOXDataSynchronousBBOXOutputRaw");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 "
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.test.DummyTestClass</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>BBOXInputData</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:BoundingBoxData crs=\"EPSG:4326\">"
                + "<ows:LowerCorner>46.75 13.05</ows:LowerCorner>"
                + "<ows:UpperCorner>46.85 13.25</ows:UpperCorner>"
                + "</wps:BoundingBoxData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:RawDataOutput>"
                + "<ows:Identifier>BBOXOutputData</ows:Identifier>"
                + "</wps:RawDataOutput>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, not(containsString("Response")));
        
        assertThat(response, response, containsString("46.75 13.05"));
        assertThat(response, response, containsString("EPSG:4326"));
    }

    @Test
    public void testExecutePOSTinlineBBOXDataSynchronousBBOXOutputNoEPSG() throws IOException {
        System.out.println("\nRunning testExecutePOSTinlineBBOXDataSynchronousBBOXOutputNoEPSG");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 "
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.test.DummyTestClass</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>BBOXInputData</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:BoundingBoxData>"
                + "<ows:LowerCorner>46.75 13.05</ows:LowerCorner>"
                + "<ows:UpperCorner>46.85 13.25</ows:UpperCorner>"
                + "</wps:BoundingBoxData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"false\" storeExecuteResponse=\"false\">"
                + "<wps:Output asReference=\"false\">"
                + "<ows:Identifier>BBOXOutputData</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, not(containsString("EPSG")));
        assertThat(response, response, containsString("46.75 13.05"));
    }

    @Test
    public void testExecutePOSTinlineBBOXDataSynchronousBBOXOutputStatusNoEPSG() throws IOException {
        System.out.println("\nRunning testExecutePOSTinlineBBOXDataSynchronousBBOXOutputStatusNoEPSG");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 "
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.test.DummyTestClass</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>BBOXInputData</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:BoundingBoxData crs=\"EPSG:4326\">"
                + "<ows:LowerCorner>46.75 13.05</ows:LowerCorner>"
                + "<ows:UpperCorner>46.85 13.25</ows:UpperCorner>"
                + "</wps:BoundingBoxData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"true\" storeExecuteResponse=\"false\">"
                + "<wps:Output asReference=\"false\">"
                + "<ows:Identifier>BBOXOutputData</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("46.75 13.05"));
        assertThat(response, response, containsString("Status"));
        assertThat(response, response, containsString("EPSG"));
    }

    @Test
    public void testExecutePOSTinlineBBOXDataASynchronousBBOXOutputStoreNoEPSG() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTinlineBBOXDataASynchronousBBOXOutputStoreNoEPSG");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 "
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.test.DummyTestClass</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>BBOXInputData</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:BoundingBoxData>"
                + "<ows:LowerCorner>46.75 13.05</ows:LowerCorner>"
                + "<ows:UpperCorner>46.85 13.25</ows:UpperCorner>"
                + "</wps:BoundingBoxData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"false\" storeExecuteResponse=\"true\">"
                + "<wps:Output asReference=\"false\">"
                + "<ows:Identifier>BBOXOutputData</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        assertThat(response, response, not(containsString("ExceptionReport")));

        String asyncDoc = AllTestsIT.getAsyncDoc(response);
        assertThat(asyncDoc, asyncDoc, containsString("Status"));
        assertThat(asyncDoc, asyncDoc, not(containsString("EPSG")));
    }

    @Test
    public void testExecutePOSTinlineBBOXDataASynchronousBBOXOutputStoreStatusNoEPSG() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTinlineBBOXDataASynchronousBBOXOutputStoreStatusNoEPSG");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 "
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.test.DummyTestClass</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>BBOXInputData</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:BoundingBoxData>"
                + "<ows:LowerCorner>46.75 13.05</ows:LowerCorner>"
                + "<ows:UpperCorner>46.85 13.25</ows:UpperCorner>"
                + "</wps:BoundingBoxData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"false\" storeExecuteResponse=\"true\">"
                + "<wps:Output asReference=\"false\">"
                + "<ows:Identifier>BBOXOutputData</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("Status"));
        assertThat(response, response, not(containsString("EPSG")));

        String asyncDoc = AllTestsIT.getAsyncDoc(response);
        assertThat(asyncDoc, asyncDoc, containsString("46.75 13.05"));
    }

    @Test
    public void testExecutePOSTinlineBBOXDataSynchronousBBOXOutputRawNoEPSG() throws IOException {
        System.out.println("\nRunning testExecutePOSTinlineBBOXDataSynchronousBBOXOutputRawNoEPSG");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 "
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.test.DummyTestClass</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>BBOXInputData</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:BoundingBoxData>"
                + "<ows:LowerCorner>46.75 13.05</ows:LowerCorner>"
                + "<ows:UpperCorner>46.85 13.25</ows:UpperCorner>"
                + "</wps:BoundingBoxData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:RawDataOutput>"
                + "<ows:Identifier>BBOXOutputData</ows:Identifier>"
                + "</wps:RawDataOutput>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, not(containsString("Response")));
        assertThat(response, response, not(containsString("EPSG")));
        assertThat(response, response, containsString("46.75 13.05"));
    }

    private Object createAndSubmitMultiReferenceBinaryInputAlgorithmExecuteWithResponseDocument(boolean status, boolean storeSupport, boolean asReference, String outputEncoding) throws WPSClientException{    	
    	
        multiReferenceBinaryInputAlgorithmExecuteRequestBuilder.addComplexDataReference(multiReferenceBinaryInputAlgorithmComplexInputID,
                                                                                        AllTestsIT.referenceComplexBinaryInputURL,
                                                                                        null,
                                                                                        null,
                                                                                        multiReferenceBinaryInputAlgorithmComplexMimeTypeImageTiff);
		
        multiReferenceBinaryInputAlgorithmExecuteRequestBuilder.addComplexDataReference(multiReferenceBinaryInputAlgorithmComplexInputID,
                                                                                        AllTestsIT.referenceComplexBinaryInputURL,
                                                                                        null,
                                                                                        null,
                                                                                        multiReferenceBinaryInputAlgorithmComplexMimeTypeImageTiff);

		multiReferenceBinaryInputAlgorithmExecuteRequestBuilder.setResponseDocument(multiReferenceBinaryInputAlgorithmComplexOutputID, null, outputEncoding, multiReferenceBinaryInputAlgorithmComplexMimeTypeImageTiff);
		
		multiReferenceBinaryInputAlgorithmExecuteRequestBuilder.setStoreSupport(multiReferenceBinaryInputAlgorithmComplexOutputID, storeSupport);
		multiReferenceBinaryInputAlgorithmExecuteRequestBuilder.setStatus(multiReferenceBinaryInputAlgorithmComplexOutputID, status);
		multiReferenceBinaryInputAlgorithmExecuteRequestBuilder.setAsReference(multiReferenceBinaryInputAlgorithmComplexOutputID, asReference);		
		
		Object responseObject =  WPSClientSession.getInstance().execute(url, multiReferenceBinaryInputAlgorithmExecuteRequestBuilder.getExecute());
		
		return responseObject;
    }
    
    private Object createAndSubmitMultiReferenceBinaryInputAlgorithmExecuteWithResponseDocument(boolean status, boolean storeSupport, boolean asReference) throws WPSClientException{    	
    	
        multiReferenceBinaryInputAlgorithmExecuteRequestBuilder.addComplexDataReference(multiReferenceBinaryInputAlgorithmComplexInputID,
                                                                                        AllTestsIT.referenceComplexBinaryInputURL,
                                                                                        null,
                                                                                        null,
                                                                                        multiReferenceBinaryInputAlgorithmComplexMimeTypeImageTiff);
		
        multiReferenceBinaryInputAlgorithmExecuteRequestBuilder.addComplexDataReference(multiReferenceBinaryInputAlgorithmComplexInputID,
                                                                                        AllTestsIT.referenceComplexBinaryInputURL,
                                                                                        null,
                                                                                        null,
                                                                                        multiReferenceBinaryInputAlgorithmComplexMimeTypeImageTiff);

		multiReferenceBinaryInputAlgorithmExecuteRequestBuilder.setResponseDocument(multiReferenceBinaryInputAlgorithmComplexOutputID, null, "base64", multiReferenceBinaryInputAlgorithmComplexMimeTypeImageTiff);
		
		multiReferenceBinaryInputAlgorithmExecuteRequestBuilder.setStoreSupport(multiReferenceBinaryInputAlgorithmComplexOutputID, storeSupport);
		multiReferenceBinaryInputAlgorithmExecuteRequestBuilder.setStatus(multiReferenceBinaryInputAlgorithmComplexOutputID, status);
		multiReferenceBinaryInputAlgorithmExecuteRequestBuilder.setAsReference(multiReferenceBinaryInputAlgorithmComplexOutputID, asReference);		
		
		Object responseObject =  WPSClientSession.getInstance().execute(url, multiReferenceBinaryInputAlgorithmExecuteRequestBuilder.getExecute());
		
		return responseObject;
    }
    
    private Object createAndSubmitMultiReferenceBinaryInputAlgorithmExecuteWithRawData(boolean status, boolean storeSupport, boolean asReference, String encoding) throws WPSClientException{    	
    	
        multiReferenceBinaryInputAlgorithmExecuteRequestBuilder.addComplexDataReference(multiReferenceBinaryInputAlgorithmComplexInputID,
                                                                                        AllTestsIT.referenceComplexBinaryInputURL,
                                                                                        null,
                                                                                        null,
                                                                                        multiReferenceBinaryInputAlgorithmComplexMimeTypeImageTiff);
		
        multiReferenceBinaryInputAlgorithmExecuteRequestBuilder.addComplexDataReference(multiReferenceBinaryInputAlgorithmComplexInputID,
                                                                                        AllTestsIT.referenceComplexBinaryInputURL,
                                                                                        null,
                                                                                        null,
                                                                                        multiReferenceBinaryInputAlgorithmComplexMimeTypeImageTiff);

		multiReferenceBinaryInputAlgorithmExecuteRequestBuilder.setRawData(multiReferenceBinaryInputAlgorithmComplexOutputID, null, encoding, multiReferenceBinaryInputAlgorithmComplexMimeTypeImageTiff);
		
		Object responseObject =  WPSClientSession.getInstance().execute(url, multiReferenceBinaryInputAlgorithmExecuteRequestBuilder.getExecute());
		
		return responseObject;
    }

    private Object createAndSubmitEchoProcessExecuteWithResponseDocument(boolean status, boolean storeSupport, boolean asReference) throws WPSClientException{    	

		echoProcessExecuteRequestBuilder.addComplexData(echoProcessComplexInputID, echoProcessInlineComplexXMLInput, null, null, echoProcessComplexMimeTypeTextXML);

		echoProcessExecuteRequestBuilder.setResponseDocument(echoProcessComplexOutputID, null, null, echoProcessComplexMimeTypeTextXML);
		
		echoProcessExecuteRequestBuilder.setStoreSupport(echoProcessComplexOutputID, storeSupport);
		echoProcessExecuteRequestBuilder.setStatus(echoProcessComplexOutputID, status);
		echoProcessExecuteRequestBuilder.setAsReference(echoProcessComplexOutputID, asReference);
		
		
		Object responseObject =  WPSClientSession.getInstance().execute(url, echoProcessExecuteRequestBuilder.getExecute());
		
		return responseObject; 
    }
    
    private Object createAndSubmitEchoProcessExecuteWithRawData() throws WPSClientException{    	

		echoProcessExecuteRequestBuilder.addComplexData(echoProcessComplexInputID, echoProcessInlineComplexXMLInput, null, null, echoProcessComplexMimeTypeTextXML);
		
		echoProcessExecuteRequestBuilder.setRawData(echoProcessComplexOutputID, null, null, echoProcessComplexMimeTypeTextXML);
		
		Object responseObject =  WPSClientSession.getInstance().execute(url, echoProcessExecuteRequestBuilder.getExecute());
		
		return responseObject; 
    }
    
    private OutputDataType getFirstOutputData(ExecuteResponseDocument executeResponseDocument){
    	ProcessOutputs outputs = executeResponseDocument.getExecuteResponse().getProcessOutputs();
    	
    	assertThat(outputs, not(nullValue()));    	
    	assertThat(outputs.sizeOfOutputArray(), not(0)); 
    	
    	OutputDataType outputDataType = executeResponseDocument.getExecuteResponse().getProcessOutputs().getOutputArray(0);
    	
    	return outputDataType;
    }
    
    private void checkIdentifier(ExecuteResponseDocument executeResponseDocument, String outputID){
    	    	
    	String identifier = getFirstOutputData(executeResponseDocument).getIdentifier().getStringValue();	        
    	
    	assertThat(identifier, is(equalTo(outputID)));    	
    }
    
    private DataType getData(ExecuteResponseDocument executeResponseDocument){
    	
    	OutputDataType outputDataType = getFirstOutputData(executeResponseDocument);
    	
    	assertThat(outputDataType, not(nullValue()));       	
    	
    	DataType data = outputDataType.getData();
    	
    	assertThat(data, not(nullValue())); 
    	
    	return data;    	
    }
    
    private void checkIfResultContainsTestXMLData(ExecuteResponseDocument executeResponseDocument){
    	
    	DataType data = getData(executeResponseDocument);
    	
    	assertTrue(data.isSetComplexData());    	
    	
    	ComplexDataType complexData = data.getComplexData();
    	
    	assertThat(complexData, not(nullValue())); 
    	
    	Node domNode = complexData.getDomNode();
    	
    	assertThat(domNode, not(nullValue()));       
    	
    	assertThat(domNode.getChildNodes(), not(nullValue())); 
    	assertThat(domNode.getChildNodes().getLength(), greaterThan(1)); 
    	
    	Node secondChild = domNode.getChildNodes().item(1);
 
    	assertThat(secondChild, not(nullValue()));       
    	
    	String nodeName = secondChild.getNodeName();
    	
    	assertThat(nodeName, is(equalTo(testDataNodeName)));   
    	
    }
    
    private void checkIfResultContainsTestStringData(ExecuteResponseDocument executeResponseDocument){
    	
    	DataType data = getData(executeResponseDocument);
    	
    	assertTrue(data.isSetLiteralData());    	
    	
    	LiteralDataType literalData = data.getLiteralData();
    	
    	assertThat(literalData, not(nullValue())); 
    	
    	Node domNode = literalData.getDomNode();
    	
    	assertThat(domNode, not(nullValue()));       
    	
    	assertThat(domNode.getChildNodes(), not(nullValue())); 
    	
    	Node firstChild = domNode.getFirstChild();
    	
    	String nodeValue = firstChild.getNodeValue();
    	
    	assertThat(nodeValue, is(equalTo(echoProcessLiteralInputString)));   
    	
    }
}
