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

import java.io.IOException;
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
        String response = GetClient.sendRequest(url, "Service=WPS");

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, containsString("ExceptionReport"));
        assertThat(response, response, containsString("exceptionCode=\"MissingParameterValue\""));
        
        assertThat(response, response, not(containsString("<wps:Capabilities")));
    }
    
    @Test
    public void missingServiceParameter() throws IOException,
            ParserConfigurationException,
            SAXException {
        String response = GetClient.sendRequest(url, "Request=GetCapabilities");

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, containsString("ExceptionReport"));
        assertThat(response, response, containsString("exceptionCode=\"MissingParameterValue\""));
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
        String response = GetClient.sendRequest(url, "Service=WPS&Request=GetCapabilities&acceptVersions=42.17");

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, containsString("ExceptionReport"));
        assertThat(response, response, containsString("VersionNegotiationFailed"));

        assertThat(response, response, not(containsString("<wps:Capabilities")));
    }
    
    @Test
    public void wrongServiceParameter() throws ParserConfigurationException, SAXException, IOException {
        String response = GetClient.sendRequest(url, "Service=HotDogStand&Request=GetCapabilities");

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, containsString("ExceptionReport"));
        assertThat(response, response, containsString("exceptionCode=\"InvalidParameterValue\""));
    }
}
