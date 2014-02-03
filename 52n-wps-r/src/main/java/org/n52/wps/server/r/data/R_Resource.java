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
package org.n52.wps.server.r.data;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.r.RWPSConfigVariables;
import org.n52.wps.server.r.R_Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class R_Resource {

    private static Logger LOGGER = LoggerFactory.getLogger(CustomDataTypeManager.class);

    private String resourceValue;

    public R_Resource(String resourceValue) {
        this.resourceValue = resourceValue;
    }

    public String getResourceValue() {
        return this.resourceValue;
    }

    public URL getFullResourceURL() {
        R_Config config = R_Config.getInstance();
        String dirUrl = config.getResourceDirURL();

        String fullResourceURL = null;
        if (dirUrl != null)
            fullResourceURL = dirUrl + "/" + this.resourceValue;
        else
            fullResourceURL = "http://not_available/" + this.resourceValue;

        URL resourceURL;
        try {
            resourceURL = new URL(fullResourceURL);
        }
        catch (MalformedURLException e) {
            LOGGER.error("Could not create URL from resource: " + fullResourceURL, e);
            return null;
        }

        // TODO fix resource existing testing
        // if ( !urlResourceExists(resourceURL)) {
        // LOGGER.warn("Resource file from annotation '" + resourcePath
        // + "' could not be found in the file system at " + resourceURL);
        // return null;
        // }

        return resourceURL;
    }

    public File getFullResourcePath() {
        String fullResourcePath = null;
        try {
            fullResourcePath = R_Config.getInstance().getConfigVariableFullPath(RWPSConfigVariables.RESOURCE_DIR)
                    + File.separatorChar + this.resourceValue;
        }
        catch (ExceptionReport e) {
            LOGGER.error("Cannot locate resource File: " + this.resourceValue, e);
            e.printStackTrace();
        }

        File resourceFile = new File(fullResourcePath);
        if ( !resourceFile.exists()) {
            LOGGER.error("Cannot locate resource File: " + this.resourceValue + ", path: " + fullResourcePath);
            return null;
        }

        // TODO fix resource existing testing
        // if ( !urlResourceExists(resourceURL)) {
        // LOGGER.warn("Resource file from annotation '" + resourcePath
        // + "' could not be found in the file system at " + resourceURL);
        // return null;
        // }

        return resourceFile;
    }

    @SuppressWarnings("unused")
    private static boolean urlResourceExists(URL url) {
        HttpURLConnection conn = null;

        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("HEAD"); // should be
                                           // conn.setRequestMethod("HEAD");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
        }
        catch (IOException e) {
            LOGGER.error("Could not open connection to URL " + url, e);
            return false;
        }

        // does not work
        long length = conn.getContentLength();
        System.out.println(length);

        try {
            conn.connect();
        }
        catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        // does not work
        int code;
        try {
            code = conn.getResponseCode();
        }
        catch (IOException e) {
            LOGGER.error("Could not get header from connection.", e);
            return false;
        }

        return (code == HttpURLConnection.HTTP_OK);

        // last resort
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("R_Resource [resourceValue=");
        builder.append(this.resourceValue);
        builder.append("]");
        return builder.toString();
    }

}
