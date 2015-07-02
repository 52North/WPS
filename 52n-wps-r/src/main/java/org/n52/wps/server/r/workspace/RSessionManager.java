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

package org.n52.wps.server.r.workspace;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.GenericFileDataConstants;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.r.RResource;
import org.n52.wps.server.r.RWPSConfigVariables;
import org.n52.wps.server.r.RWPSSessionVariables;
import org.n52.wps.server.r.R_Config;
import org.n52.wps.server.r.data.R_Resource;
import org.n52.wps.server.r.syntax.RAnnotationException;
import org.n52.wps.server.r.util.RExecutor;
import org.n52.wps.server.r.util.RLogger;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RSessionManager {

    private static Logger log = LoggerFactory.getLogger(RSessionManager.class);

    private static final String NO_WARNINGS_MESSAGE = "The process proceeded without any warnings from R.";

    private static final String SESSION_INFO_OUTPUT_NAME = "sessionInfo";

    private static final String WARNING_OUTPUT_NAME = "warnings";

    private R_Config config;

    private RConnection connection;

    private boolean cleanOnStartup = true;

    public RSessionManager(RConnection rCon, R_Config config) {
        this.connection = rCon;
        this.config = config;

        log.debug("NEW {}", this);
    }

    private void cleanSession() {
        try {
            RLogger.log(connection, "CLEANING R SESSION!");
            this.connection.eval("rm(list = ls())");
        }
        catch (RserveException e) {
            log.error("Problem cleaning session", e);
        }
    }

    public void cleanUp() {
        log.debug("Cleaning up session...");

        RLogger.log(connection, "Session after process run:");
        RLogger.logSessionContent(connection);

        cleanSession();
    }

    public void configureSession(String processWKN, RExecutor executor) throws ExceptionReport,
            RserveException,
            FileNotFoundException,
            IOException,
            RAnnotationException {
        log.debug("Configuring R session...");

        if (cleanOnStartup) {
            cleanSession();
        }

        // configure memory limit
        StringBuilder cmd = new StringBuilder();

        String memoryLimit = config.getConfigVariable(RWPSConfigVariables.R_SESSION_MEMORY_LIMIT);
        cmd.append("memory.limit(");
        cmd.append(memoryLimit);
        cmd.append(")");

        try {
            REXP expr = this.connection.eval(cmd.toString());
            log.debug("Memory limit is '{}' (configuration value is '{}')", expr.asString(), memoryLimit);
            this.connection.eval("cat(\"Memory info > memory.size = \", toString(memory.size()), \" (max: \", toString(memory.size(TRUE)), \"); memory.limit = \", memory.limit(), \"\n\")");
        }
        catch (RserveException e) {
            log.error("Problem setting the memory limit", e);
        }
        catch (REXPMismatchException e) {
            log.error("Problem setting the memory limit", e);
        }

        loadWPSSessionVariables(processWKN);
        loadUtilityScripts(executor);
    }

    private String getConsoleOutput(String cmd) throws RserveException, REXPMismatchException {
        return this.connection.eval("paste(capture.output(print(" + cmd + ")), collapse='\\n')").asString();
    }

    public String getRVersion() throws RserveException, REXPMismatchException {
        return getConsoleOutput("R.version[\"version.string\"]");
    }

    public String getSessionInfo() throws RserveException, REXPMismatchException {
        return getConsoleOutput("sessionInfo()");
    }

    private void loadUtilityScripts(RExecutor executor) throws RserveException,
            IOException,
            FileNotFoundException,
            RAnnotationException,
            ExceptionReport {
        log.debug("Loading utility scripts.");

        Collection<File> utils = config.getUtilsFiles();
        log.debug("Loading {} utils files: {}", utils.size(), Arrays.toString(utils.toArray()));
        for (File file : utils) {
            if (file.exists())
                executor.executeScript(file, this.connection);
            else
                log.warn("Configured script file does not longer exist: {}", file);
        }

        RLogger.log(connection, "workspace content after loading utility scripts:");
        RLogger.logSessionContent(connection);
    }

    private void loadWPSSessionVariables(String processWKN) throws ExceptionReport {
        log.debug("Loading session variables.");

        try {
            RLogger.log(connection, "Environment:");
            connection.eval("cat(capture.output(environment()), \"\n\")");

            String cmd = RWPSSessionVariables.WPS_SERVER + " <- TRUE";
            connection.eval(cmd);
            RLogger.logVariable(connection, RWPSSessionVariables.WPS_SERVER);

            cmd = RWPSSessionVariables.WPS_SERVER_NAME + " <- \"52N-WPS\"";
            connection.eval(cmd);
            RLogger.logVariable(connection, RWPSSessionVariables.WPS_SERVER_NAME);

            String resourceUrl = RResource.getResourceURL(new R_Resource(processWKN, "", true)).toExternalForm();
            assignAndLog(RWPSSessionVariables.RESOURCES_ENDPOINT, resourceUrl);

            String scriptUrl;
            try {
                scriptUrl = RResource.getScriptURL(processWKN).toExternalForm();
            }
            catch (MalformedURLException e) {
                log.warn("Could not retrieve script URL", e);
                scriptUrl = "N/A";
            }
            assignAndLog(RWPSSessionVariables.SCRIPT_URL, scriptUrl);

            URL processDescription = config.getProcessDescriptionURL(processWKN);
            assignAndLog(RWPSSessionVariables.PROCESS_DESCRIPTION, processDescription.toString());

            // create session variable for warning storage
            cmd = RWPSSessionVariables.WARNING_OUTPUT_STORAGE + " = c()";
            connection.eval(cmd);
            RLogger.logVariable(connection, RWPSSessionVariables.WARNING_OUTPUT_STORAGE);

            RLogger.log(connection, "workspace content after loading session variables:");
            RLogger.logSessionContent(connection);
        }
        catch (RserveException e) {
            log.error("Error loading WPS session variables for process {}", processWKN);
            throw new ExceptionReport("Could not load session variables for " + processWKN,
                                      ExceptionReport.REMOTE_COMPUTATION_ERROR,
                                      e);
        }
    }

    private void assignAndLog(String name, String value) throws RserveException {
        connection.assign(name, value);
        RLogger.logVariable(connection, name);
        log.debug("Assigned process description to variable '{}': {}", name, value);
    }

    /**
     * Retrieves warnings that occured during the last execution of a script
     * 
     * Note that the warnings()-method is not reliable for Rserve because it does not return warnings in most
     * cases. Therefore a specific warnings function is used to retrieve the warnings.
     */
    private String getWarnings() throws RserveException, REXPMismatchException {
        REXP result = connection.eval(RWPSSessionVariables.WARNING_OUTPUT_STORAGE);

        StringBuilder warnings = new StringBuilder();
        if ( !result.isNull()) {
            String[] warningsArray = result.asStrings();
            for (int i = 0; i < warningsArray.length; i++) {
                String currentWarning = warningsArray[i];

                warnings.append("warning ");
                warnings.append( (i));
                warnings.append(": '");
                warnings.append(currentWarning);
                warnings.append("'\n");
            }
        }

        if (warnings.length() < 1)
            return NO_WARNINGS_MESSAGE;
        return warnings.toString();
    }

    public HashMap<String, IData> saveInfos(HashMap<String, IData> result) {
        try {
            String sessionInfo = getSessionInfo();
            InputStream sessionInfoStream = new ByteArrayInputStream(sessionInfo.getBytes("UTF-8"));
            result.put(SESSION_INFO_OUTPUT_NAME,
                       new GenericFileDataBinding(new GenericFileData(sessionInfoStream,
                                                                      GenericFileDataConstants.MIME_TYPE_PLAIN_TEXT)));
            sessionInfoStream.close();

            String warnings = getWarnings();
            InputStream warningsStream = new ByteArrayInputStream(warnings.getBytes("UTF-8"));
            result.put(WARNING_OUTPUT_NAME,
                       new GenericFileDataBinding(new GenericFileData(warningsStream,
                                                                      GenericFileDataConstants.MIME_TYPE_PLAIN_TEXT)));
            warningsStream.close();
        }
        catch (IOException | REXPMismatchException | RserveException e) {
            log.error("Could not save session info and warnings.", e);
        }

        return result;
    }

    public void loadImportedScripts(RExecutor executor, Collection<File> imports) throws RserveException,
            IOException,
            RAnnotationException,
            ExceptionReport {
        log.debug("Loading {} imports: {}", imports.size(), Arrays.toString(imports.toArray()));

        for (File file : imports) {
            if (file.exists())
                executor.executeScript(file, this.connection);
            else
                log.warn("Imported script does not exist: {}", file);
        }

        RLogger.log(connection, "workspace content after loading imports:");
        RLogger.logSessionContent(connection);
    }

}
