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

    private static String url;

    @BeforeClass
    public static void beforeClass() {
        url = AllTestsIT.getURL();
    }

    @Test
    public void testDescribeProcessCompleteSingle() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testDescribeProcessCompleteSingle");

        String response = GetClient.sendRequest(url, "Service=WPS&Request=DescribeProcess&Version=1.0.0&Identifier=org.n52.wps.server.algorithm.SimpleBufferAlgorithm");
        
        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("org.n52.wps.server.algorithm.SimpleBufferAlgorithm"));
    }

    @Test
    public void testDescribeProcessCompleteMultiple() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testDescribeProcessCompleteMultiple");

        String response = GetClient.sendRequest(url, "Service=WPS&Request=DescribeProcess&Version=1.0.0&Identifier=org.n52.wps.server.algorithm.SimpleBufferAlgorithm,org.n52.wps.server.algorithm.simplify.DouglasPeuckerAlgorithm");

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("org.n52.wps.server.algorithm.SimpleBufferAlgorithm"));
        assertThat(response, response, containsString("org.n52.wps.server.algorithm.simplify.DouglasPeuckerAlgorithm"));

    }

    @Test
    public void testDescribeProcessCompleteAll() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testDescribeProcessCompleteAll");
        
        String response = GetClient.sendRequest(url, "Service=WPS&Request=DescribeProcess&Version=1.0.0&Identifier=all");
        
        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("org.n52.wps.server.algorithm.SimpleBufferAlgorithm"));
        assertThat(response, response, containsString("org.n52.wps.server.algorithm.simplify.DouglasPeuckerAlgorithm"));

    }

    @Test
    public void testDescribeProcessMissingVersionParameter() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testDescribeProcessMissingVersionParameter");
        
        String response = GetClient.sendRequest(url, "Service=WPS&Request=DescribeProcess&Identifier=org.n52.wps.server.algorithm.SimpleBufferAlgorithm");
     
        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, containsString("ExceptionReport"));
        assertThat(response, response, containsString("locator=\"version\""));
        assertThat(response, response, not(containsString("org.n52.wps.server.algorithm.SimpleBufferAlgorithm")));
    }

    @Test
    public void testDescribeProcessMissingServiceParameter() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testDescribeProcessMissingServiceParameter");
        
        String response = GetClient.sendRequest(url, "Request=DescribeProcess&Version=1.0.0&Identifier=org.n52.wps.server.algorithm.SimpleBufferAlgorithm");
        
        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, containsString("ExceptionReport"));
        assertThat(response, response, containsString("locator=\"service\""));
        assertThat(response, response, not(containsString("org.n52.wps.server.algorithm.SimpleBufferAlgorithm")));
    }

    @Test
    public void testDescribeProcessMissingIdentifierParameter() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testDescribeProcessMissingIdentifierParameter");
        
        String response = GetClient.sendRequest(url, "Request=DescribeProcess&service=WPS&Version=1.0.0");
        
        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, containsString("ExceptionReport"));
        assertThat(response, response, containsString("MissingParameterValue"));
        assertThat(response, response, containsString("locator=\"identifier\""));
        assertThat(response, response, not(containsString("org.n52.wps.server.algorithm.SimpleBufferAlgorithm")));
    }

    @Test
    public void testDescribeProcessWrongIdentifierParameter() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testDescribeProcessWrongIdentifierParameter");
        
        String response = GetClient.sendRequest(url, "Request=DescribeProcess&service=WPS&Version=1.0.0&Identifier=XXX");
        
        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, containsString("ExceptionReport"));
        assertThat(response, response, containsString("InvalidParameterValue"));
        assertThat(response, response, containsString("locator=\"identifier\""));
        assertThat(response, response, not(containsString("org.n52.wps.server.algorithm.SimpleBufferAlgorithm")));
    }
    
    @Test
    public void testDescribeProcessMissingIdentifierValue() throws IOException, ParserConfigurationException, SAXException {
    	System.out.println("\nRunning testDescribeProcessMissingIdentifierValue");
    	
    	String response = GetClient.sendRequest(url, "Request=DescribeProcess&service=WPS&Version=1.0.0&Identifier=");
    	
    	assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
    	assertThat(response, response, containsString("ExceptionReport"));
    	assertThat(response, response, containsString("InvalidParameterValue"));
    	assertThat(response, response, containsString("locator=\"identifier\""));
    	assertThat(response, response, not(containsString("org.n52.wps.server.algorithm.SimpleBufferAlgorithm")));
    }
}
