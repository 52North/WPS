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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

public class DescribeProcessKvpIT {

	private final String testProcessID = "org.n52.wps.server.algorithm.test.EchoProcess";
	private final String testProcessID2 = "org.n52.wps.server.algorithm.test.MultiReferenceBinaryInputAlgorithm";
	
    private static String url;

    @BeforeClass
    public static void beforeClass() {
        url = AllTestsIT.getURL();
    }

    @Test
    public void testDescribeProcessCompleteSingle() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testDescribeProcessCompleteSingle");

        String response = GetClient.sendRequest(url, "Service=WPS&Request=DescribeProcess&Version=1.0.0&Identifier=" + testProcessID);
        
        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString(testProcessID));
    }

    @Test
    public void testDescribeProcessCompleteMultiple() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testDescribeProcessCompleteMultiple");

        String response = GetClient.sendRequest(url, "Service=WPS&Request=DescribeProcess&Version=1.0.0&Identifier=" + testProcessID + "," + testProcessID2);

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString(testProcessID));
        assertThat(response, response, containsString(testProcessID2));

    }

    @Test
    public void testDescribeProcessCompleteAll() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testDescribeProcessCompleteAll");
        
        String response = GetClient.sendRequest(url, "Service=WPS&Request=DescribeProcess&Version=1.0.0&Identifier=all");
        
        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString(testProcessID));
        assertThat(response, response, containsString(testProcessID2));

    }

    @Test
    public void testDescribeProcessMissingVersionParameter() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testDescribeProcessMissingVersionParameter");
        
        String response = GetClient.sendRequest(url, "Service=WPS&Request=DescribeProcess&Identifier=" + testProcessID);
     
        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, containsString("ExceptionReport"));
        assertThat(response, response, containsString("locator=\"version\""));
        assertThat(response, response, not(containsString(testProcessID)));
    }

    @Test
    public void testDescribeProcessMissingServiceParameter() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testDescribeProcessMissingServiceParameter");
        
        String response = GetClient.sendRequest(url, "Request=DescribeProcess&Version=1.0.0&Identifier=" + testProcessID);
        
        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, containsString("ExceptionReport"));
        assertThat(response, response, containsString("locator=\"service\""));
        assertThat(response, response, not(containsString(testProcessID)));
    }

    @Test
    public void testDescribeProcessMissingIdentifierParameter() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testDescribeProcessMissingIdentifierParameter");
        
        String response = GetClient.sendRequest(url, "Request=DescribeProcess&service=WPS&Version=1.0.0");
        
        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, containsString("ExceptionReport"));
        assertThat(response, response, containsString("MissingParameterValue"));
        assertThat(response, response, containsString("locator=\"identifier\""));
        assertThat(response, response, not(containsString(testProcessID)));
    }

    @Test
    public void testDescribeProcessWrongIdentifierParameter() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testDescribeProcessWrongIdentifierParameter");
        
        String response = GetClient.sendRequest(url, "Request=DescribeProcess&service=WPS&Version=1.0.0&Identifier=XXX");
        
        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, containsString("ExceptionReport"));
        assertThat(response, response, containsString("InvalidParameterValue"));
        assertThat(response, response, containsString("locator=\"identifier\""));
        assertThat(response, response, not(containsString(testProcessID)));
    }
    
    @Test
    public void testDescribeProcessMissingIdentifierValue() throws IOException, ParserConfigurationException, SAXException {
    	System.out.println("\nRunning testDescribeProcessMissingIdentifierValue");
    	
    	String response = GetClient.sendRequest(url, "Request=DescribeProcess&service=WPS&Version=1.0.0&Identifier=");
    	
    	assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
    	assertThat(response, response, containsString("ExceptionReport"));
    	assertThat(response, response, containsString("InvalidParameterValue"));
    	assertThat(response, response, containsString("locator=\"identifier\""));
    	assertThat(response, response, not(containsString(testProcessID)));
    }
}
