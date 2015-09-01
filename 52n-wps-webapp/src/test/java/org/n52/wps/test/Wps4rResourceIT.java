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
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URL;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.r.RResource;
import org.n52.wps.server.r.data.R_Resource;

/**
 * 
 * @author Daniel Nüst
 *
 */
public class Wps4rResourceIT {

    private static String wpsWebappUrl;

    @BeforeClass
    public static void beforeClass() {
        wpsWebappUrl = AllTestsIT.getWebappURL();
        WPSConfig config = Mockito.mock(WPSConfig.class);
        Mockito.when(config.getServiceBaseUrl()).thenReturn(wpsWebappUrl);
        // ConfigMocker.setConfig(config);
    }

    @Test
    public void resourcesCanBeRequested() throws ExceptionReport, IOException {
        URL resourceURL = RResource.getResourceURL(new R_Resource("org.n52.wps.server.r.test.resources",
                                                                  "test/dummy1.txt",
                                                                  true));

        String response = GetClient.sendRequest(resourceURL.toExternalForm());
        assertNotNull(response);
        assertThat("response text file contains correct text", response, containsString("This is a dummy txt-file"));
    }

    @Test
    public void scriptFilesCanBeRequested() throws ExceptionReport, IOException {
        URL scriptURL = RResource.getScriptURL("org.n52.wps.server.r.test.resources");

        String response = GetClient.sendRequest(scriptURL.toExternalForm());
        assertNotNull(response);
        assertThat(response, containsString("test.resources"));
        assertThat(response, containsString("wps.resource:"));
    }

    @Test
    public void sessionInfoCanBeRequested() throws ExceptionReport, IOException {
        URL sessionInfoURL = RResource.getSessionInfoURL();

        String response = GetClient.sendRequest(sessionInfoURL.toExternalForm());
        assertNotNull(response);
        assertThat(response, containsString("R version"));
        assertThat(response, containsString("attached base packages:"));
        assertThat(response, containsString("Platform:"));
    }

}
