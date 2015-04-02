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
import static org.hamcrest.Matchers.not;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n52.wps.commons.WPSConfig;

public class ExecuteKvpIT {

    private static String url;

    private final String processSucceeded = "ProcessSucceeded";
    private final String exceptionReport = "ExceptionReport";

    private String referenceComplexBinaryInputURLEncoded;
    private String referenceComplexXMLInputURLEncoded;
    
    @BeforeClass
    public static void beforeClass() throws XmlException, IOException {
        url = AllTestsIT.getURL();
        WPSConfig.forceInitialization("src/main/webapp/WEB-INF/config/wps_config.xml");// FIXME bpross-52n: I
                                                                                       // don't think this is
                                                                                       // needed
    }
    
    @Before
    public void before(){    	
    	try {
			referenceComplexBinaryInputURLEncoded = URLEncoder.encode(AllTestsIT.referenceComplexBinaryInputURL, "UTF-8");
			referenceComplexXMLInputURLEncoded = URLEncoder.encode(AllTestsIT.referenceComplexXMLInputURL, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}    	
    }
    
    @Test
    public void testExecuteKVPSynchronousLiteralDataReponseDoc() throws IOException {
        System.out.println("\nRunning testExecuteKVPSynchronousLiteralDataReponseDoc");

        String getURL = ExecuteKvpIT.url + "?Service=WPS&Request=Execute&Version=1.0.0&Identifier=org.n52.wps.server.algorithm.test.DummyTestClass&DataInputs=LiteralInputData=seventy@datatype=xs%3Astring@uom=meter&ResponseDocument=LiteralOutputData";

        String response = GetClient.sendRequest(getURL);

        assertThat(response, response, not(containsString(exceptionReport)));
    	assertThat(response, response, containsString("seventy"));
    	assertThat(response, response, containsString("uom=\"meter\""));
    }

    @Test
    public void testExecuteKVPSynchronousLiteralDataRawData() throws IOException {
    	System.out.println("\nRunning testExecuteKVPSynchronousLiteralDataRawData");

    	String getURL = ExecuteKvpIT.url + "?Service=WPS&Request=Execute&Version=1.0.0&Identifier=org.n52.wps.server.algorithm.test.DummyTestClass&DataInputs=LiteralInputData=seventy@datatype=xs%3Astring@uom=meter&RawDataOutput=LiteralOutputData";

    	String response = GetClient.sendRequest(getURL);

    	assertThat(response, response, not(containsString(exceptionReport)));
    	assertThat(response, response, containsString("seventy"));
    }

    @Test
    public void testExecuteKVPSynchronousComplexDataReponseDoc() throws IOException {
    	System.out.println("\nRunning testExecuteKVPSynchronousComplexDataReponseDoc");

    	String getURL = ExecuteKvpIT.url + "?Service=WPS&Request=Execute&Version=1.0.0&Identifier=org.n52.wps.server.algorithm.JTSConvexHullAlgorithm&DataInputs=data=LINESTRING%28848383.15654512%206793127.8657339,848440.48431633%206793777.5804742,849137.97219934%206793739.3619601,849004.20739986%206793414.5045899%29@mimeType=application/wkt&ResponseDocument=result";

    	String response = GetClient.sendRequest(getURL);

    	assertThat(response, response, not(containsString(exceptionReport)));
    	assertThat(response, response, containsString("Polygon"));
    }

    @Test
    public void testExecuteKVPSynchronousComplexDataRawData() throws IOException {
    	System.out.println("\nRunning testExecuteKVPSynchronousComplexDataRawData");

    	String getURL = ExecuteKvpIT.url + "?Service=WPS&Request=Execute&Version=1.0.0&Identifier=org.n52.wps.server.algorithm.JTSConvexHullAlgorithm&DataInputs=data=LINESTRING%28848383.15654512%206793127.8657339,848440.48431633%206793777.5804742,849137.97219934%206793739.3619601,849004.20739986%206793414.5045899%29@mimeType=application/wkt&RawDataOutput=result";

    	String response = GetClient.sendRequest(getURL);

    	assertThat(response, response, not(containsString(exceptionReport)));
    	assertThat(response, response, containsString("Polygon"));
    }

    @Test
    public void testExecuteKVPSynchronousBBoxDataReponseDoc() throws IOException {
    	System.out.println("\nRunning testExecuteKVPSynchronousBBoxDataReponseDoc");

    	String getURL = ExecuteKvpIT.url + "?Service=WPS&Request=Execute&Version=1.0.0&Identifier=org.n52.wps.server.algorithm.test.DummyTestClass&DataInputs=BBOXInputData=46,102,47,103,urn%3Aogc%3Adef%3Acrs%3AEPSG%3A6.6%3A4326,2&ResponseDocument=BBOXOutputData";

    	String response = GetClient.sendRequest(getURL);

    	assertThat(response, response, not(containsString(exceptionReport)));

    	String expectedResult = "BoundingBoxData";

    	String expectedResult2 = "crs=\"urn:ogc:def:crs:EPSG:6.6:4326\"";

    	String expectedResult3 = "dimensions=\"2\"";

    	String expectedResult4 = "LowerCorner";

    	String expectedResult5 = "UpperCorner";

    	String expectedResult6 = "46.0 102.0";

    	String expectedResult7 = "47.0 103.0";

    	assertThat(response, response, containsString(expectedResult));
    	assertThat(response, response, containsString(expectedResult2));
    	assertThat(response, response, containsString(expectedResult3));
    	assertThat(response, response, containsString(expectedResult4));
    	assertThat(response, response, containsString(expectedResult5));
    	assertThat(response, response, containsString(expectedResult6));
    	assertThat(response, response, containsString(expectedResult7));
    }

    @Test
    public void testExecuteKVPSynchronousBBoxDataRawData() throws IOException {
    	System.out.println("\nRunning testExecuteKVPSynchronousBBoxDataRawData");

    	String getURL = ExecuteKvpIT.url + "?Service=WPS&Request=Execute&Version=1.0.0&Identifier=org.n52.wps.server.algorithm.test.DummyTestClass&DataInputs=BBOXInputData=46,102,47,103,urn%3Aogc%3Adef%3Acrs%3AEPSG%3A6.6%3A4326,2&RawDataOutput=BBOXOutputData";

        String response = GetClient.sendRequest(getURL);
    	assertThat(response, response, not(containsString(exceptionReport)));
        assertThat(response, response, containsString("<wps:BoundingBoxData"));
        assertThat(response, response, containsString("xmlns:ows=\"http://www.opengis.net/ows/1.1\""));
        assertThat(response, response, containsString("xmlns:wps=\"http://www.opengis.net/wps/1.0.0\""));
        assertThat(response, response, containsString("crs=\"urn:ogc:def:crs:EPSG:6.6:4326\""));
        assertThat(response, response, containsString("dimensions=\"2\""));
        assertThat(response, response, containsString("<ows:LowerCorner>46.0 102.0</ows:LowerCorner>"));
        assertThat(response, response, containsString("<ows:UpperCorner>47.0 103.0</ows:UpperCorner>"));
        assertThat(response, response, containsString("</wps:BoundingBoxData>"));
    }

    @Test
    public void testExecuteKVPSynchronousComplexDataReferenceResponseDoc() throws IOException {
    	System.out.println("\nRunning testExecuteKVPSynchronousComplexDataReferenceResponseDoc");
    	
    	String inputID = "complexInput";
    	String inputMimeType = "text/xml";
    	String outputID = "complexOutput";
    	
    	String getURL = ExecuteKvpIT.url + "?Service=WPS&Request=Execute&Version=1.0.0&Identifier=org.n52.wps.server.algorithm.test.EchoProcess&DataInputs=" + inputID + "=@href=" + referenceComplexXMLInputURLEncoded + "@mimeType=" + inputMimeType + "&ResponseDocument=" + outputID;

    	String response = GetClient.sendRequest(getURL);

    	String expectedResult = "ProcessSucceeded";
    	String expectedResult3 = "TestData";

    	assertThat(response, response, not(containsString(exceptionReport)));
    	assertThat(response, response, containsString(expectedResult));
    	assertThat(response, response, containsString(outputID));
    	assertThat(response, response, containsString(expectedResult3));
    }

    @Test
    public void testExecuteKVPSynchronousComplexDataReferenceResponseDocSchemaMimeType() throws IOException {
    	System.out.println("\nRunning testExecuteKVPSynchronousComplexDataReferenceResponseDocSchemaMimeType");

    	String inputID = "complexInput";
    	String inputMimeType = "text/xml";
    	String outputID = "complexOutput";
    	String outputMimeType = "text/xml";
    	
    	String getURL = ExecuteKvpIT.url + "?Service=WPS&Request=Execute&Version=1.0.0&Identifier=org.n52.wps.server.algorithm.test.EchoProcess&DataInputs=" + inputID + "=@href=" + referenceComplexXMLInputURLEncoded + "@mimeType=" + inputMimeType + "&ResponseDocument=" + outputID + "@mimeType=" + outputMimeType + "";

    	String response = GetClient.sendRequest(getURL);

    	String expectedResult = processSucceeded;
    	String expectedResult5 = "mimeType=\"" + outputMimeType + "\"";

    	assertThat(response, response, not(containsString(exceptionReport)));
    	assertThat(response, response, containsString(expectedResult));
    	assertThat(response, response, containsString(outputID));
    	assertThat(response, response, containsString(expectedResult5));
    }

    @Test
    public void testExecuteKVPSynchronousComplexDataReferenceResponseDocMimeTypeEncodingBase64() throws IOException {
    	System.out.println("\nRunning testExecuteKVPSynchronousComplexDataReferenceResponseDocMimeTypeEncodingBase64");

    	String getURL = ExecuteKvpIT.url + "?Service=WPS&Request=Execute&Version=1.0.0&Identifier=org.n52.wps.server.algorithm.test.MultiReferenceBinaryInputAlgorithm&DataInputs=data=@href=" + referenceComplexBinaryInputURLEncoded + "@mimeType=image/tiff;data=@href=" + referenceComplexBinaryInputURLEncoded + "@mimeType=image/tiff&ResponseDocument=result@mimeType=image/tiff@encoding=base64";

    	String response = GetClient.sendRequest(getURL);

    	String expectedResult2 = "result";
    	String expectedResult3 = "encoding=\"base64\"";
    	String expectedResult4 = " mimeType=\"image/tiff\"";

    	assertThat(response, response, not(containsString(exceptionReport)));
    	assertThat(response, response, containsString(processSucceeded));       
    	assertThat(response, response, containsString(expectedResult2));
    	assertThat(response, response, containsString(expectedResult3));
    	assertThat(response, response, containsString(expectedResult4));
    	AllTestsIT.checkInlineResultBase64(response);
    }

    @Test
    public void testExecuteKVPSynchronousComplexDataReferenceRawData() throws IOException {
    	System.out.println("\nRunning testExecuteKVPSynchronousComplexDataReferenceRawData");

    	String inputID = "complexInput";
    	String inputMimeType = "text/xml";
    	String outputID = "complexOutput";
    	String outputMimeType = "text/xml";
    	
    	String getURL = ExecuteKvpIT.url + "?Service=WPS&Request=Execute&Version=1.0.0&Identifier=org.n52.wps.server.algorithm.test.EchoProcess&DataInputs=" + inputID + "=@href=" + referenceComplexXMLInputURLEncoded + "@mimeType=" + inputMimeType + "&RawDataOutput=" + outputID + "@mimeType=" + outputMimeType + "";

    	String response = GetClient.sendRequest(getURL);

    	String expectedResult = "<TestData>";

    	assertThat(response, response, not(containsString(exceptionReport)));
    	assertThat(response, response, containsString(expectedResult));  
    	
    }
}
