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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.parsers.ParserConfigurationException;

import net.opengis.wps.x100.CapabilitiesDocument;

import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

public class GetCapabilitiesPostIT {
    private static String url;

    @BeforeClass
    public static void before() {
        url = AllTestsIT.getURL();
    }

    @Test
    public void complete() throws XmlException, IOException {
        URL resource = GetCapabilitiesPostIT.class.getResource("/GetCapabilities/GetCapabilities.xml");
        XmlObject payload = XmlObject.Factory.parse(resource);

        String response = "";
        try {
            response = PostClient.sendRequest(url, payload.toString());
        }
        catch (IOException e) {
            fail(e.getMessage());
        }

        assertThat(response, response, not(containsString("ExceptionReport")));

        assertThat(response, response, containsString("<wps:Capabilities"));
        assertThat(response, response, containsString("<ows:Operation name=\"Execute\">"));
        assertThat(response, response, containsString("<ows:ServiceType>WPS</ows:ServiceType>"));
        assertThat(response, response, containsString("<ows:ServiceProvider>"));
        assertThat(response, response, containsString("</ows:OperationsMetadata>"));
        assertThat(response, response, containsString("</wps:ProcessOfferings>"));
        assertThat(response, response, containsString("</wps:Capabilities>"));
    }

    @Test
    public void validateCapabilities() throws XmlException, IOException {
        URL resource = GetCapabilitiesPostIT.class.getResource("/GetCapabilities/GetCapabilities.xml");
        XmlObject payload = XmlObject.Factory.parse(resource);

        String response = "";
        try {
            response = PostClient.sendRequest(url, payload.toString());
        }
        catch (IOException e) {
            fail(e.getMessage());
        }

        CapabilitiesDocument capsDoc = CapabilitiesDocument.Factory.parse(response);
        
        XmlOptions opts = new XmlOptions();
        ArrayList<XmlError> errors = new ArrayList<XmlError>();
        opts.setErrorListener(errors);
        boolean valid = capsDoc.validate(opts);

        assertTrue(Arrays.deepToString(errors.toArray()), valid);
    }

    @Test
    public void wrongVersion() throws XmlException, IOException {
        URL resource = GetCapabilitiesPostIT.class.getResource("/GetCapabilities/WrongVersion.xml");
        XmlObject payload = XmlObject.Factory.parse(resource);

        String response = "";
        try {
            response = PostClient.sendRequest(url, payload.toString());
        }
        catch (IOException e) {
            fail(e.getMessage());
        }

        assertThat(response, response, containsString("ExceptionReport"));
        assertThat(response, response, containsString("VersionNegotiationFailed"));

        assertThat(response, response, not(containsString("<wps:Capabilities")));
    }

    @Test
    public void wrongServiceParameter() throws ParserConfigurationException, SAXException, IOException, XmlException {
        URL resource = GetCapabilitiesPostIT.class.getResource("/GetCapabilities/WrongService.xml");
        XmlObject payload = XmlObject.Factory.parse(resource);

        String response = "";
        try {
            response = PostClient.sendRequest(GetCapabilitiesPostIT.url, payload.toString());
        }
        catch (IOException e) {
            fail(e.getMessage());
        }

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, containsString("ExceptionReport"));
        assertThat(response, response, containsString("exceptionCode=\"InvalidParameterValue\""));
    }
    
    @Test
    public void missingServiceParameter() throws ParserConfigurationException, SAXException, IOException, XmlException {
        URL resource = GetCapabilitiesPostIT.class.getResource("/GetCapabilities/MissingService.xml");
        XmlObject payload = XmlObject.Factory.parse(resource);

        String response = "";
        try {
            response = PostClient.sendRequest(GetCapabilitiesPostIT.url, payload.toString());
        }
        catch (IOException e) {
            fail(e.getMessage());
        }

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, containsString("ExceptionReport"));
        assertThat(response, response, containsString("exceptionCode=\"MissingParameterValue\""));
    }

}
