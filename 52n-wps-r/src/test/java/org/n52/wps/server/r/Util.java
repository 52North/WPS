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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.xmlbeans.XmlException;
import org.junit.Assert;
import org.mockito.Mockito;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.webapp.api.ConfigurationManager;
import org.n52.wps.webapp.entities.Server;
import org.n52.wps.webapp.service.ConfigurationService;

public class Util {

    public static final Server testserver = new Server("http", "testhost", 42, "wps");

    public static File loadFile(String filePath) {
        URL r = Util.class.getResource(filePath);
        File f;
        try {
            f = new File(r.toURI());
        }
        catch (URISyntaxException e) {
            Assert.fail("Invalid file path (not URI): " + e.getMessage());
            return null;
        }

        return f;
    }

    /**
     * public constructor access for {@link R_Config}
     * 
     * use helper method to instatiate the R_Config in this class, which is in the same package as R_Config
     * and therefore can call the protected constructur.
     * 
     * @return
     */
    public static R_Config getConfig() {
        return new R_Config();
    }

    public static void mockGenericWPSConfig() throws FileNotFoundException, IOException, XmlException {
        // mockup configuration service to return a valid URL
        ConfigurationService service = Mockito.mock(ConfigurationService.class);
        Mockito.when(service.getConfigurationModule(Server.class.getName())).thenReturn(testserver);
        // add more fields needed in the tests here

        ConfigurationManager manager = Mockito.mock(ConfigurationManager.class);
        Mockito.when(manager.getConfigurationServices()).thenReturn(service);
        WPSConfig.getInstance().setConfigurationManager(manager);
    }

}
