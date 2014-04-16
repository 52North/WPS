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
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.n52.wps.ServerDocument.Server;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.WebProcessingService;
import org.n52.wps.server.r.metadata.RAnnotationParser;
import org.n52.wps.server.r.syntax.RAnnotation;
import org.n52.wps.server.r.syntax.RAnnotationException;
import org.n52.wps.server.r.syntax.RAnnotationType;
import org.n52.wps.server.r.syntax.RAttribute;
import org.n52.wps.server.r.util.RConnector;
import org.n52.wps.server.r.util.RSessionInfo;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class R_Config {

    private final int TEMPDIR_NAME_LENGTH = 8;

    private static Logger LOGGER = LoggerFactory.getLogger(R_Config.class);

    public static final String SCRIPT_FILE_EXTENSION = "R";

    public static final String SCRIPT_FILE_SUFFIX = "." + SCRIPT_FILE_EXTENSION;

    public static final String WKN_PREFIX = "org.n52.wps.server.r.";

    // TODO for resources to be downloadable the cannot be in WEB-INF, or this
    // must be handled with a
    // servlet, which is probably a better solution to keep track of files, see
    // http://www.jguru.com/faq/view.jsp?EID=10646
    private final String R_BASE_DIR = "R";

    private final String UTILS_DIR = "utils";

    private static final String DEFAULT_RSERVE_HOST = "localhost";

    private static final int DEFAULT_RSERVE_PORT = 6311;

    private static final boolean DEFAULT_ENABLEBATCHSTART = false;

    /** R scripts with utility functions to pre-load */
    public String utilsDirFull;

    private HashMap<RWPSConfigVariables, String> configVariables = new HashMap<RWPSConfigVariables, String>();

    private static R_Config instance = null;

    private RConnector connector = new RConnector();

    private RAnnotationParser annotationParser = new RAnnotationParser();

    /** Maps current R-script files to identifiers **/
    private HashMap<File, String> fileToWknMap = new HashMap<File, String>();

    /** Maps each identifier to an R script file **/
    private HashMap<String, File> wknToFileMap = new HashMap<String, File>();

    /** caches conflicts for the wkn-Rscript mapping until resetWknFileMapping is invoked **/
    private HashMap<String, ExceptionReport> wknConflicts = new HashMap<String, ExceptionReport>();

    private R_Config() {
        try {
            String wpsBasedir = WebProcessingService.BASE_DIR;
            if (wpsBasedir != null) {
                File f = new File(wpsBasedir, R_BASE_DIR);
                String baseDirFull = f.getAbsolutePath();
                f = new File(baseDirFull, UTILS_DIR);
                this.utilsDirFull = f.getAbsolutePath();
            }
            else
                LOGGER.error("Could not get basedir from WPS!");
        }
        catch (Exception e) {
            LOGGER.error("Error getting full path of baseDir and configDir.", e);
        }
    }

    public static R_Config getInstance() {
        if (instance == null)
            instance = new R_Config();

        return instance;
    }

    public void setConfigVariable(RWPSConfigVariables key, String value) {
        this.configVariables.put(key, value);
    }

    public String getConfigVariable(RWPSConfigVariables key) {
        return this.configVariables.get(key);
    }

    public String getConfigVariableFullPath(RWPSConfigVariables key) throws ExceptionReport {
        String path = getConfigVariable(key);
        if (path == null)
            throw new ExceptionReport("Config variable is not set!", "Inconsistent property");
        File testFile = new File(path);
        if ( !testFile.isAbsolute()) {
            testFile = new File(WebProcessingService.BASE_DIR, path);
        }
        if ( !testFile.exists())
            throw new ExceptionReport("Invalid config property of name \"" + key + "\" and value \"" + path
                    + "\". It denotes a non-existent path.", "Inconsistent property");

        return testFile.getAbsolutePath();
    }

    public URL getSessionInfoURL() throws MalformedURLException {
        return new URL(getUrlPathUpToWebapp() + "/R/sessioninfo.jsp");
    }

    // FIXME this should use generic WPS methods to get the URL
    public String getResourceDirURL() {
        String webapp = getUrlPathUpToWebapp();
        String resourceDirectory = getResourceDirectory();
        if (webapp != null & resourceDirectory != null)
            return webapp + "/" + resourceDirectory.replace("\\", "/");

        LOGGER.warn("Cannot create resource dir URL");
        return null;
    }

    public String getResourceDirectory() {
        return getConfigVariable(RWPSConfigVariables.RESOURCE_DIR);
    }

    public URL getScriptURL(String wkn) throws MalformedURLException, ExceptionReport {
        String fname = null;
        try {
            fname = getScriptFileForWKN(wkn).getName();
        }
        catch (IOException e) {
            LOGGER.error("Could not open session.", e);
            throw new ExceptionReport("Could not open script file.", "Input/Output", e);
        }

        if (fname == null)
            return null;
        String script_dir = getConfigVariable(RWPSConfigVariables.SCRIPT_DIR);
        URL url = new URL(getUrlPathUpToWebapp() + "/" + script_dir.replace("\\", "/") + "/" + fname);
        return url;
    }

    public String getUrlPathUpToWebapp() {
        Server server = WPSConfig.getInstance().getWPSConfig().getServer();
        String host = server.getHostname();
        String port = server.getHostport();
        String webapppath = server.getWebappPath();

        return "http://" + host + ":" + port + "/" + webapppath;
    }

    public URL getOutputFileURL(String currentWorkdir, String filename) throws IOException {
        // check if file exists
        String path = currentWorkdir + "/" + filename;
        File out = new File(path);
        if ( ! (out.isFile() && out.canRead()))
            throw new IOException("Error in creating URL: " + currentWorkdir + " / " + path + " not found or broken.");

        // create URL
        path = path.substring(WebProcessingService.BASE_DIR.length() + 1, path.length());
        String urlString = getUrlPathUpToWebapp() + "/" + path;

        return new URL(urlString);
    }

    public static class RFileExtensionFilter implements FileFilter {

        @Override
        public boolean accept(File f) {
            if (f.isFile() && f.canRead()) {
                String name = f.getName();
                if (name.endsWith(R_Config.SCRIPT_FILE_SUFFIX))
                    return true;
            }
            return false;
        }
    }

    void registerScript(File file) throws FileNotFoundException, RAnnotationException, IOException, ExceptionReport {
        if ( !fileToWknMap.containsKey(file.getAbsoluteFile())) {
            FileInputStream fis = new FileInputStream(file);
            List<RAnnotation> annotations = annotationParser.parseAnnotationsfromScript(fis);
            RAnnotation description = RAnnotation.filterAnnotations(annotations, RAnnotationType.DESCRIPTION).get(0);
            String process_id = description.getStringValue(RAttribute.IDENTIFIER);
            String wkn = WKN_PREFIX + process_id;

            if (fileToWknMap.containsValue(wkn)) {
                File conflictFile = getScriptFileForWKN(wkn);
                if ( !conflictFile.exists()) {
                    LOGGER.info("Cached mapping from " + wkn + " to file " + conflictFile.getName()
                            + " replaced by file " + file.getName());
                }
                else if ( !file.equals(conflictFile)) {
                    String e_message = "Conflicting identifier '" + wkn + "' detected " + "for R scripts '"
                            + file.getName() + "' and '" + conflictFile.getName() + "'";
                    ExceptionReport e = new ExceptionReport(e_message, ExceptionReport.NO_APPLICABLE_CODE);
                    LOGGER.error(e_message);
                    wknConflicts.put(wkn, e);
                    throw e;
                }
            }

            fileToWknMap.put(file.getAbsoluteFile(), wkn);
            wknToFileMap.put(wkn, file.getAbsoluteFile());
            fis.close();
        }
    }

    public String getWKNForScriptFile(File file) throws RAnnotationException, IOException, ExceptionReport {
        if ( !file.exists())
            throw new FileNotFoundException("File not found: " + file.getName());

        return fileToWknMap.get(file);
    }

    public File getScriptFileForWKN(String wkn) throws IOException, ExceptionReport {
        // check for existing identifier conflicts
        if (wknConflicts.containsKey(wkn))
            throw wknConflicts.get(wkn);

        File out = wknToFileMap.get(wkn);
        if (out != null && out.exists() && out.isFile() && out.canRead()) {
            return out;
        }
        else {
            String fname = out == null ? "(unknown)" : out.getName();
            throw new IOException("Error in Process: " + wkn + ", File " + fname + " not found or broken.");
        }
    }

    public void resetWknFileMapping() {
        this.wknToFileMap.clear();
        this.fileToWknMap.clear();
        this.wknConflicts.clear();
    }

    public String createTemporaryWPSWorkDir() {
        File tempdir = new File(System.getProperty("java.io.tmpdir"), "wps4r-wps-workdir-tmp-"
                + UUID.randomUUID().toString().substring(0, TEMPDIR_NAME_LENGTH));
        tempdir.mkdir();
        return tempdir.getAbsolutePath();
    }

    public String getScriptDirFullPath() {
        return new File(WebProcessingService.BASE_DIR, getConfigVariable(RWPSConfigVariables.SCRIPT_DIR)).getAbsolutePath();
    }

    public boolean isScriptAvailable(String identifier) {
        try {
            File f = getScriptFileForWKN(identifier);
            boolean out = f.exists();
            return out;
        }
        catch (Exception e) {
            LOGGER.error("Script file unavailable for process id " + identifier, e);
            return false;
        }
    }

    public boolean isScriptValid(String wkn) {
        FileInputStream fis = null;

        try {
            File file = getScriptFileForWKN(wkn);
            // RAnnotationParser parser = new RAnnotationParser();
            fis = new FileInputStream(file);
            boolean valid = annotationParser.validateScript(fis, wkn);

            return valid;
        }
        catch (IOException e) {
            LOGGER.error("Script file unavailable for process " + wkn + ".", e);
            return false;
        }
        catch (Exception e) {
            LOGGER.error("Validation of process " + wkn + " failed.", e);
            return false;
        }
        finally {
            if (fis != null)
                try {
                    fis.close();
                }
                catch (IOException e) {
                    LOGGER.error("Could not flose file input.", e);
                }
        }
    }

    public void killRserveOnWindows() {
        try {
            if (Runtime.getRuntime().exec("taskkill /IM RServe.exe /T /F").waitFor() == 0)
                ;
            return;
        }
        catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public RConnection openRConnection() throws RserveException {
        return this.connector.getNewConnection(this.getEnableBatchStart(),
                                               this.getRServeHost(),
                                               this.getRServePort(),
                                               this.getRServeUser(),
                                               this.getRServePassword());
    }

    public String getRServePassword() {
        return getConfigVariable(RWPSConfigVariables.RSERVE_PASSWORD);
    }

    public String getRServeUser() {
        return getConfigVariable(RWPSConfigVariables.RSERVE_USER);
    }

    public int getRServePort() {
        int port_number = DEFAULT_RSERVE_PORT;

        String port = getConfigVariable(RWPSConfigVariables.RSERVE_PORT);
        // try to retrieve config variable
        if (port != null && !port.equals("")) {
            try {
                port_number = Integer.parseInt(port);
            }
            catch (NumberFormatException e) {
                LOGGER.warn("Config variable " + RWPSConfigVariables.RSERVE_PORT
                        + " does not contain a parseble integer. Using default port " + port_number);
            }
        }
        return port_number;
    }

    public String getRServeHost() {
        String host = getConfigVariable(RWPSConfigVariables.RSERVE_HOST);
        if (host == null || host.equals("")) {
            host = DEFAULT_RSERVE_HOST;
        }
        return host;
    }

    public boolean getEnableBatchStart() {
        boolean isBatch = DEFAULT_ENABLEBATCHSTART;
        // try to retrieve config variable
        String batch_c = getConfigVariable(RWPSConfigVariables.ENABLE_BATCH_START);
        if (batch_c != null && !batch_c.equals("")) {
            try {
                isBatch = Boolean.parseBoolean(batch_c);
            }
            catch (NumberFormatException e) {
                LOGGER.warn("Config variable " + RWPSConfigVariables.RSERVE_PORT
                        + " does not contain a parseble boolean. Using default port " + isBatch);
            }
        }

        return isBatch;
    }

    public String getCurrentSessionInfo() throws RserveException, REXPMismatchException {
        RConnection rCon = openRConnection();
        String info = RSessionInfo.getSessionInfo(rCon);
        rCon.close();
        return info;
    }
}
