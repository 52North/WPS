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
