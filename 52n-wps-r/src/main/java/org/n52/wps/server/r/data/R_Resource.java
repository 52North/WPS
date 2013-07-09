/**
 * ﻿Copyright (C) 2010
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

package org.n52.wps.server.r.data;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.r.RWPSConfigVariables;
import org.n52.wps.server.r.R_Config;

public class R_Resource {

    private static Logger LOGGER = LoggerFactory.getLogger(R_Resource.class);

    private String resourceValue;

    public R_Resource(String resourceValue) {
        this.resourceValue = resourceValue;
    }

    public String getResourceValue() {
        return this.resourceValue;
    }

    public URL getFullResourceURL() {
        String fullResourceURL = R_Config.getInstance().getResourceDirURL() + "/" + this.resourceValue;

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
            conn.setRequestMethod("HEAD"); // should be conn.setRequestMethod("HEAD");
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
