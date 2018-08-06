/*
 * Copyright (C) 2007-2018 52°North Initiative for Geospatial Open Source
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

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

public class GetCapabilitiesKvpIT {

    private static String url;

    @BeforeClass
    public static void beforeClass() {
        url = AllTestsIT.getURL();
    }

    @Test
    public void complete() throws ParserConfigurationException, SAXException, IOException {
        String response = GetClient.sendRequest(url, "Service=WPS&Request=GetCapabilities");

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, not(containsString("ExceptionReport")));

        assertThat(response, response, containsString("<wps:Capabilities"));
        assertThat(response, response, containsString("<ows:Operation name=\"Execute\">"));
        assertThat(response, response, containsString("<ows:ServiceType>WPS</ows:ServiceType>"));
    }

    @Test
    public void missingRequestParameter() throws IOException,
            ParserConfigurationException,
            SAXException {
        GetClient.checkForExceptionReport(url, "Service=WPS", HttpServletResponse.SC_BAD_REQUEST, "MissingParameterValue");
    }

    @Test
    public void missingServiceParameter() throws IOException,
            ParserConfigurationException,
            SAXException {
        GetClient.checkForExceptionReport(url, "Request=GetCapabilities", HttpServletResponse.SC_BAD_REQUEST, "MissingParameterValue");
    }

    @Test
    public void noVersionParameter() throws ParserConfigurationException,
            SAXException,
            IOException {
        String response = GetClient.sendRequest(url, "Service=WPS&Request=GetCapabilities");

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, not(containsString("ExceptionReport")));

        assertThat(response, response, containsString("<wps:Capabilities"));
        assertThat(response, response, containsString("<ows:Operation name=\"Execute\">"));
        assertThat(response, response, containsString("<ows:ServiceType>WPS</ows:ServiceType>"));
    }

    @Test
    public void wrongVersion() throws ParserConfigurationException, SAXException, IOException {
        GetClient.checkForExceptionReport(url, "Service=WPS&Request=GetCapabilities&acceptVersions=42.17", HttpServletResponse.SC_BAD_REQUEST, "VersionNegotiationFailed");
    }

    @Test
    public void wrongServiceParameter() throws ParserConfigurationException, SAXException, IOException {
        GetClient.checkForExceptionReport(url, "Service=HotDogStand&Request=GetCapabilities", HttpServletResponse.SC_BAD_REQUEST, "InvalidParameterValue");
    }
}
