/**
 * ﻿Copyright (C) 2010 - 2014 52°North Initiative for Geospatial Open Source
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

package org.n52.wps.server.r;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.xmlbeans.XmlException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.r.syntax.RAnnotationException;

/**
 * 
 * @author Daniel
 *
 */
public class ServerResource {

    private String wkn = "wkn.42";

    @BeforeClass
    public static void initConfig() throws FileNotFoundException, XmlException, IOException {
        Util.mockGenericWPSConfig();
    }

    @Test
    public void scriptUrlIsGenerated() throws RAnnotationException, ExceptionReport, MalformedURLException {
        URL scriptURL = RResource.getScriptURL(wkn);

        String expectedUrl = "http://" + Util.testserver.getHostname() + ":" + Util.testserver.getHostport() + "/"
                + Util.testserver.getWebappPath() + RResource.R_ENDPOINT + RResource.SCRIPT_PATH + "/" + wkn;
        assertThat("script url is correct", scriptURL.toString(), is(equalTo(expectedUrl)));
    }

}
