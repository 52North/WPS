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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xmlbeans.XmlException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n52.wps.server.ExceptionReport;
import org.xml.sax.SAXException;

import net.opengis.wps.x20.StatusInfoDocument;

public class GetResultKvpIT {

    private static String url;

    @BeforeClass
    public static void beforeClass() {
        url = AllTestsIT.getURL();
    }

    @Test
    public void resultNotReady() throws ParserConfigurationException, SAXException, IOException {

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                +"<wps:Execute xmlns:wps=\"http://www.opengis.net/wps/2.0\" xmlns:ows=\"http://www.opengis.net/ows/2.0\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/2.0 http://schemas.opengis.net/wps/2.0/wps.xsd\" service=\"WPS\" version=\"2.0.0\" response=\"document\" mode=\"async\">"
                +"  <ows:Identifier>org.n52.wps.server.algorithm.test.LongRunningDummyTestClass</ows:Identifier>"
                +"  <wps:Input id=\"LiteralInputData\">"
                +"    <wps:Data>"
                +"      <wps:LiteralValue>007</wps:LiteralValue>"
                +"    </wps:Data>"
                +"  </wps:Input>"
                +"  <wps:Output id=\"LiteralOutputData\" transmission=\"value\" mimeType=\"application/vnd.geo%2Bjson\"/>"
                +"</wps:Execute>";

        String response = PostClient.sendRequest(url, payload);
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("Status"));
        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));

        String jobId = "";

        try {
            StatusInfoDocument statusInfoDocument = StatusInfoDocument.Factory.parse(response);

            jobId = statusInfoDocument.getStatusInfo().getJobID();
        } catch (XmlException e) {
            fail(e.getMessage());
        }

        GetClient.checkForExceptionReport(url, "Service=WPS&Request=GetResult&version=2.0.0&jobID=" + jobId, HttpServletResponse.SC_BAD_REQUEST, ExceptionReport.RESULT_NOT_READY, jobId);

    }

}
