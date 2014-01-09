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

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

public class DescribeProcessPostIT {

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
                + "<ows:Identifier>org.n52.wps.server.algorithm.SimpleBufferAlgorithm</ows:Identifier>"
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
        assertTrue(response.contains("org.n52.wps.server.algorithm.SimpleBufferAlgorithm"));
    }

    @Test
    public void testDescribeProcessCompleteSingleWrongLanguage() {
        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:DescribeProcess xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsDescribeProcess_request.xsd\" service=\"WPS\" version=\"1.0.0\" language=\"en-CA\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.SimpleBufferAlgorithm</ows:Identifier>"
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
        assertTrue( !response.contains("org.n52.wps.server.algorithm.SimpleBufferAlgorithm"));
    }

    @Test
    public void testDescribeProcessCompleteMultiple() {
        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:DescribeProcess xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsDescribeProcess_request.xsd\" service=\"WPS\" version=\"1.0.0\" language=\"en-US\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.SimpleBufferAlgorithm</ows:Identifier>"
                + "<ows:Identifier>org.n52.wps.server.algorithm.simplify.DouglasPeuckerAlgorithm</ows:Identifier>"
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
        assertTrue(response.contains("org.n52.wps.server.algorithm.SimpleBufferAlgorithm"));
        assertTrue(response.contains("org.n52.wps.server.algorithm.simplify.DouglasPeuckerAlgorithm"));

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
        assertTrue(response.contains("org.n52.wps.server.algorithm.SimpleBufferAlgorithm"));
        assertTrue(response.contains("org.n52.wps.server.algorithm.simplify.DouglasPeuckerAlgorithm"));

    }

    @Test
    public void testDescribeProcessMissingVersionParameter() {
        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:DescribeProcess xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsDescribeProcess_request.xsd\" service=\"WPS\" language=\"en-US\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.SimpleBufferAlgorithm</ows:Identifier>" +

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
        assertTrue( !response.contains("org.n52.wps.server.algorithm.SimpleBufferAlgorithm"));
    }

    @Test
    public void testDescribeProcessMissingServiceParameter() {
        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:DescribeProcess xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsDescribeProcess_request.xsd\" version=\"1.0.0\" language=\"en-US\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.SimpleBufferAlgorithm</ows:Identifier>"
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
        assertTrue( !response.contains("org.n52.wps.server.algorithm.SimpleBufferAlgorithm"));
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
        assertTrue( !response.contains("org.n52.wps.server.algorithm.SimpleBufferAlgorithm"));
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
        assertTrue( !response.contains("org.n52.wps.server.algorithm.SimpleBufferAlgorithm"));
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
