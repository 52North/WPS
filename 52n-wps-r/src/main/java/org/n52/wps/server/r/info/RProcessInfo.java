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

package org.n52.wps.server.r.info;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.r.R_Config;
import org.n52.wps.server.r.metadata.RAnnotationParser;

public class RProcessInfo {

    private static Logger LOGGER = LoggerFactory.getLogger(RProcessInfo.class);
    private String wkn;
    private Exception lastException;
    private boolean isValid;

    static List<RProcessInfo> rProcessInfoList;

    public RProcessInfo(String wkn) {
        this.wkn = wkn;

        File scriptfile;
        FileInputStream fis = null;
        try {
            scriptfile = R_Config.getInstance().wknToFile(wkn);
            RAnnotationParser parser = new RAnnotationParser();
            fis = new FileInputStream(scriptfile);
            this.isValid = parser.validateScript(fis, wkn);
        }
        catch (Exception e) {
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
            return R_Config.getInstance().getScriptURL(this.wkn).getPath();
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
        return R_Config.getInstance().isScriptAvailable(this.wkn);
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
    	if(getLastException() == null)
    		return null;
    	else
    		return  getLastException().getMessage();
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
