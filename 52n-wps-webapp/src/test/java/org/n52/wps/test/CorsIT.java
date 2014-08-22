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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Random;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author Daniel Nüst
 *
 */
public class CorsIT {

    private static String wpsUrl;

    private static HttpClient client;

    private String randomOrigin;

    private GetMethod method;

    @BeforeClass
    public static void beforeClass() {
        wpsUrl = AllTestsIT.getURL();
        client = new HttpClient();
    }

    @Before
    public void before() {
        randomOrigin = "http://localhost:" + Math.abs(new Random().nextInt(9999));
    }

    @Test
    public void getCapabilitiesGetRequestWorksWithCORS() throws IOException {
        String url = wpsUrl + "?Service=WPS&Request=GetCapabilities";
        method = new GetMethod(url);
        method.addRequestHeader("Origin", randomOrigin);

        int code = client.executeMethod(method);
        assertThat(code, is(equalTo(200)));
        Header acaoHeader = method.getResponseHeader("Access-Control-Allow-Origin");

        assertNotNull(acaoHeader);
        assertThat(acaoHeader.toString(), containsString(randomOrigin.toString()));
    }

    @Test
    public void describeProcessGetRequestWorksWithCORS() throws IOException {
        String url = wpsUrl + "?Service=WPS&Request=DescribeProcess&Version=1.0.0&Identifier="
                + "org.n52.wps.server.algorithm.test.EchoProcess";
        method = new GetMethod(url);
        method.addRequestHeader("Origin", randomOrigin);

        int code = client.executeMethod(method);
        assertThat(code, is(equalTo(200)));
        Header acaoHeader = method.getResponseHeader("Access-Control-Allow-Origin");

        assertNotNull(acaoHeader);
        assertThat(acaoHeader.toString(), containsString(randomOrigin.toString()));
    }

    @After
    public void releaseConnection() {
        method.releaseConnection();
    }
}
