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
package org.n52.wps.server.database;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.n52.wps.commons.WPSConfig;

public class GenerateRetrieveResultURL {
    
    private final String testId = "test";
    private final String yourProtocol = "yourProtocol";
    private final String yourHost = "yourHost";
    private final String yourPort = "yourPort";
    private final String yourWebAppPath = "yourWebAppPath";
    
    private final String completeRetrieveResultURL = yourProtocol + String.format("://%s:%s/%s/RetrieveResultServlet?id=%s", yourHost,yourPort,yourWebAppPath,testId);
    private final String retrieveResultURLNoPort = yourProtocol + String.format("://%s/%s/RetrieveResultServlet?id=%s", yourHost,yourWebAppPath,testId);
//    private final String retrieveResultURLNoPort = "yourProtocol://yourHost/yourWebAppPath/RetrieveResultServlet?id=" + testId;
    private final String retrieveResultURLNoWebAppPath = yourProtocol + String.format("://%s:%s/RetrieveResultServlet?id=%s", yourHost,yourPort,testId);
//    private final String retrieveResultURLNoWebAppPath = "yourProtocol://yourHost:yourPort/RetrieveResultServlet?id=" + testId;
    private final String retrieveResultURLNoPortNoWebAppPath = yourProtocol + String.format("://%s/RetrieveResultServlet?id=%s", yourHost,testId);
//    private final String retrieveResultURLNoPortNoWebAppPath = "yourProtocol://yourHost/RetrieveResultServlet?id=" + testId;
        
    @Test
    public void generateRetrieveResultURLComplete(){
        
        WPSConfig.getInstance().getWPSConfig().getServer().setProtocol("yourProtocol");
        WPSConfig.getInstance().getWPSConfig().getServer().setHostname("yourHost");
        WPSConfig.getInstance().getWPSConfig().getServer().setHostport("yourPort");
        WPSConfig.getInstance().getWPSConfig().getServer().setWebappPath("yourWebAppPath");
        
        FlatFileDatabase ffDb = new FlatFileDatabase();
        
        assertTrue(ffDb.generateRetrieveResultURL(testId).equals(completeRetrieveResultURL));        
    }
    
    @Test
    public void generateRetrieveResultURLPortNull(){
        
        WPSConfig.getInstance().getWPSConfig().getServer().setProtocol("yourProtocol");
        WPSConfig.getInstance().getWPSConfig().getServer().setHostname("yourHost");
        WPSConfig.getInstance().getWPSConfig().getServer().setHostport(null);
        WPSConfig.getInstance().getWPSConfig().getServer().setWebappPath("yourWebAppPath");
        
        FlatFileDatabase ffDb = new FlatFileDatabase();
        
        assertTrue(ffDb.generateRetrieveResultURL(testId).equals(retrieveResultURLNoPort));        
    }
    
    @Test
    public void generateRetrieveResultURLPortEmpty(){
        
        WPSConfig.getInstance().getWPSConfig().getServer().setProtocol("yourProtocol");
        WPSConfig.getInstance().getWPSConfig().getServer().setHostname("yourHost");
        WPSConfig.getInstance().getWPSConfig().getServer().setHostport("");
        WPSConfig.getInstance().getWPSConfig().getServer().setWebappPath("yourWebAppPath");
        
        FlatFileDatabase ffDb = new FlatFileDatabase();
        
        assertTrue(ffDb.generateRetrieveResultURL(testId).equals(retrieveResultURLNoPort));        
    }
    
    @Test
    public void generateRetrieveResultURLWebAppPathNull(){
        
        WPSConfig.getInstance().getWPSConfig().getServer().setProtocol("yourProtocol");
        WPSConfig.getInstance().getWPSConfig().getServer().setHostname("yourHost");
        WPSConfig.getInstance().getWPSConfig().getServer().setHostport("yourPort");
        WPSConfig.getInstance().getWPSConfig().getServer().setWebappPath(null);
        
        FlatFileDatabase ffDb = new FlatFileDatabase();
        
        assertTrue(ffDb.generateRetrieveResultURL(testId).equals(retrieveResultURLNoWebAppPath));        
    }
    
    @Test
    public void generateRetrieveResultURLWebAppPathEmpty(){
        
        WPSConfig.getInstance().getWPSConfig().getServer().setProtocol("yourProtocol");
        WPSConfig.getInstance().getWPSConfig().getServer().setHostname("yourHost");
        WPSConfig.getInstance().getWPSConfig().getServer().setHostport("yourPort");
        WPSConfig.getInstance().getWPSConfig().getServer().setWebappPath("");
        
        FlatFileDatabase ffDb = new FlatFileDatabase();
        
        assertTrue(ffDb.generateRetrieveResultURL(testId).equals(retrieveResultURLNoWebAppPath));        
    }
    
    @Test
    public void generateRetrieveResultURLPortEmptyWebAppPathEmpty(){
        
        WPSConfig.getInstance().getWPSConfig().getServer().setProtocol("yourProtocol");
        WPSConfig.getInstance().getWPSConfig().getServer().setHostname("yourHost");
        WPSConfig.getInstance().getWPSConfig().getServer().setHostport("");
        WPSConfig.getInstance().getWPSConfig().getServer().setWebappPath("");
        
        FlatFileDatabase ffDb = new FlatFileDatabase();
        
        assertTrue(ffDb.generateRetrieveResultURL(testId).equals(retrieveResultURLNoPortNoWebAppPath));
    }
    
}
