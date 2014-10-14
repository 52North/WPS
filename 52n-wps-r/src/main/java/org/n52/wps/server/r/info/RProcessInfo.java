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

package org.n52.wps.server.r.info;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.r.R_Config;
import org.n52.wps.server.r.metadata.RAnnotationParser;
import org.n52.wps.server.r.syntax.RAnnotationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RProcessInfo {

    private static Logger LOGGER = LoggerFactory.getLogger(RProcessInfo.class);

    private String wkn;

    private Exception lastException;

    private boolean isValid;

    private R_Config config;

    static List<RProcessInfo> rProcessInfoList;

    public RProcessInfo(String wkn, R_Config config) {
        this.wkn = wkn;
        this.config = config;

        File scriptfile;
        FileInputStream fis = null;
        try {
            scriptfile = config.getScriptFileForWKN(wkn);
            RAnnotationParser parser = new RAnnotationParser(this.config);
            fis = new FileInputStream(scriptfile);
            this.isValid = parser.validateScript(fis, wkn);
        }
        catch (RuntimeException | ExceptionReport | IOException | RAnnotationException e) {
            LOGGER.error("Script validation failed. Last exception stored for the process information.", e);
            this.lastException = e;
            this.isValid = false;
        }
        finally {
            if (fis != null)
                try {
                    fis.close();
                }
                catch (IOException e) {
                    LOGGER.error("Could not close file input stream of script file.", e);
                }
        }
    }

    public String getWkn() {
        return this.wkn;
    }

    public String getScriptURL() {
        try {
            return config.getScriptURL(this.wkn).getPath();
        }
        catch (ExceptionReport e) {
            e.printStackTrace();
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean isAvailable() {
        return config.isScriptAvailable(this.wkn);
    }

    public boolean isValid() {

        return this.isValid;
    }

    public Exception getLastException() {
        return this.lastException;
    }

    /**
     * @return The last Error message or null
     */
    public String getLastErrormessage() {
        if (getLastException() == null)
            return null;
        else
            return getLastException().getMessage();
    }

    public static List<RProcessInfo> getRProcessInfoList() {
        if (rProcessInfoList == null) {
            rProcessInfoList = new ArrayList<RProcessInfo>();
        }
        return rProcessInfoList;
    }

    /**
     * To be set on repository startup
     * 
     * @param rProcessInfoList
     */
    public static void setRProcessInfoList(List<RProcessInfo> rProcessInfoList) {
        RProcessInfo.rProcessInfoList = rProcessInfoList;
    }

}
