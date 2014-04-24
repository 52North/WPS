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

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

public class DescribeProcessPostIT {

	private final String testProcessID = "org.n52.wps.server.algorithm.test.EchoProcess";
	private final String testProcessID2 = "org.n52.wps.server.algorithm.test.MultiReferenceBinaryInputAlgorithm";
	
    private static String url;

    @BeforeClass
    public static void setUp() {
        url = AllTestsIT.getURL();
    }

    /*
     * *GetCapabilities* - DescribeProcess POST request for a single process - DescribeProcess POST request
     * for a mutliple processes - DescribeProcess POST request with missing "version" paramater -
     * DescribeProcess POST request with missing "service" paramater - DescribeProcess POST request with
     * missing "identifier" paramater - DescribeProcess POST request with wrong "identifier" paramater value
     */

    @Test
    public void testDescribeProcessCompleteSingle() {
        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:DescribeProcess xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsDescribeProcess_request.xsd\" service=\"WPS\" version=\"1.0.0\" language=\"en-US\">"
                + "<ows:Identifier>" + testProcessID + "</ows:Identifier>"
                + "</wps:DescribeProcess>";

        String response = "";
        try {
            response = PostClient.sendRequest(DescribeProcessPostIT.url, payload);
            // parseXML(response);
        }
        catch (Exception e) {
            fail(e.getMessage());
        }

        assertTrue( !response.contains("ExceptionReport"));
        assertTrue(response.contains(testProcessID));
    }

    @Test
    public void testDescribeProcessCompleteSingleWrongLanguage() {
        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:DescribeProcess xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsDescribeProcess_request.xsd\" service=\"WPS\" version=\"1.0.0\" language=\"en-CA\">"
                + "<ows:Identifier>" + testProcessID + "</ows:Identifier>"
                + "</wps:DescribeProcess>";

        String response = "";
        try {
            response = PostClient.sendRequest(DescribeProcessPostIT.url, payload);
            // parseXML(response);
        }
        catch (Exception e) {
            fail(e.getMessage());
        }

        assertTrue(response.contains("ExceptionReport"));
        assertTrue(response.contains("language"));
        assertTrue( !response.contains(testProcessID));
    }

    @Test
    public void testDescribeProcessCompleteMultiple() {
        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:DescribeProcess xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsDescribeProcess_request.xsd\" service=\"WPS\" version=\"1.0.0\" language=\"en-US\">"
                + "<ows:Identifier>" + testProcessID + "</ows:Identifier>"
                + "<ows:Identifier>" + testProcessID2 + "</ows:Identifier>"
                + "</wps:DescribeProcess>";

        String response = "";
        try {
            response = PostClient.sendRequest(DescribeProcessPostIT.url, payload);
            // parseXML(response);
        }
        catch (Exception e) {
            fail(e.getMessage());
        }
        assertTrue( !response.contains("ExceptionReport"));
        assertTrue(response.contains(testProcessID));
        assertTrue(response.contains(testProcessID2));

    }

    @Test
    public void testDescribeProcessCompleteAll() {
        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:DescribeProcess xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsDescribeProcess_request.xsd\" service=\"WPS\" version=\"1.0.0\" language=\"en-US\">"
                + "<ows:Identifier>all</ows:Identifier>" +

                "</wps:DescribeProcess>";

        String response = "";
        try {
            response = PostClient.sendRequest(DescribeProcessPostIT.url, payload);
            // parseXML(response);
        }
        catch (Exception e) {
            fail(e.getMessage());
        }

        assertTrue( !response.contains("ExceptionReport"));
        assertTrue(response.contains(testProcessID));
        assertTrue(response.contains(testProcessID2));

    }

    @Test
    public void testDescribeProcessMissingVersionParameter() {
        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:DescribeProcess xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsDescribeProcess_request.xsd\" service=\"WPS\" language=\"en-US\">"
                + "<ows:Identifier>" + testProcessID + "</ows:Identifier>" +

                "</wps:DescribeProcess>";

        String response = "";
        try {
            response = PostClient.sendRequest(DescribeProcessPostIT.url, payload);
        }
        catch (IOException e) {
            fail(e.getMessage());
        }

        assertTrue(response.contains("ExceptionReport"));
        assertTrue(response.contains("locator=\"version\""));
        assertTrue( !response.contains(testProcessID));
    }

    @Test
    public void testDescribeProcessMissingServiceParameter() {
        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:DescribeProcess xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsDescribeProcess_request.xsd\" version=\"1.0.0\" language=\"en-US\">"
                + "<ows:Identifier>" + testProcessID + "</ows:Identifier>"
                + "</wps:DescribeProcess>";

        String response = "";
        try {
            response = PostClient.sendRequest(DescribeProcessPostIT.url, payload);
        }
        catch (IOException e) {
            fail(e.getMessage());
        }

        assertTrue(response.contains("ExceptionReport"));
        assertTrue(response.contains("locator=\"service\""));
        assertTrue( !response.contains(testProcessID));
    }

    @Test
    public void testDescribeProcessMissingIdentifierParameter() {
        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:DescribeProcess xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsDescribeProcess_request.xsd\" service=\"WPS\" version=\"1.0.0\" language=\"en-US\">"
                + "</wps:DescribeProcess>";

        String response = "";
        try {
            response = PostClient.sendRequest(DescribeProcessPostIT.url, payload);
        }
        catch (IOException e) {
            fail(e.getMessage());
        }

        assertTrue(response.contains("ExceptionReport"));
        assertTrue(response.contains("MissingParameterValue"));
        assertTrue(response.contains("locator=\"identifier\""));
        assertTrue( !response.contains(testProcessID));
    }
    
    @Test
    public void testDescribeProcessMissingIdentifierValue() {
        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:DescribeProcess xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsDescribeProcess_request.xsd\" service=\"WPS\" version=\"1.0.0\" language=\"en-US\">"
                + "<ows:Identifier></ows:Identifier>" + "</wps:DescribeProcess>";

        String response = "";
        try {
            response = PostClient.sendRequest(DescribeProcessPostIT.url, payload);
        }
        catch (IOException e) {
            fail(e.getMessage());
        }

        assertTrue(response.contains("ExceptionReport"));
        assertTrue(response.contains("InvalidParameterValue"));
        assertTrue(response.contains("locator=\"identifier\""));
        assertTrue( !response.contains(testProcessID));
    }

    @Test
    public void testDescribeProcessWrongIdentifierParameter() {
        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:DescribeProcess xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsDescribeProcess_request.xsd\" service=\"WPS\" version=\"1.0.0\" language=\"en-US\">"
                + "<ows:Identifier>XXX</ows:Identifier>" + "</wps:DescribeProcess>";

        String response = "";
        try {
            response = PostClient.sendRequest(DescribeProcessPostIT.url, payload);
        }
        catch (IOException e) {
            fail(e.getMessage());
        }

        assertTrue(response.contains("ExceptionReport"));
        assertTrue(response.contains("InvalidParameterValue"));
        assertTrue(response.contains("locator=\"identifier\""));
    }

}
