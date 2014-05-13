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
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.GenericFileDataConstants;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.r.FilteredRConnection;
import org.n52.wps.server.r.RWPSConfigVariables;
import org.n52.wps.server.r.RWPSSessionVariables;
import org.n52.wps.server.r.R_Config;
import org.n52.wps.server.r.data.R_Resource;
import org.n52.wps.server.r.syntax.RAnnotation;
import org.n52.wps.server.r.syntax.RAnnotationException;
import org.n52.wps.server.r.syntax.RAttribute;
import org.n52.wps.server.r.util.RExecutor;
import org.n52.wps.server.r.util.RFileExtensionFilter;
import org.n52.wps.server.r.util.RLogger;
import org.n52.wps.server.r.util.RSessionInfo;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngine;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Daniel Nüst
 * 
 */
public class RWorkspaceManager {

    private static Logger log = LoggerFactory.getLogger(RWorkspaceManager.class);

    private static final String RDATA_FILE_EXTENSION = "RData";

    private static final String WARNING_OUTPUT_NAME = "warnings";

    private static final String NO_WARNINGS_MESSAGE = "The process proceeded without any warnings from R.";

    private static final String SESSION_INFO_OUTPUT_NAME = "sessionInfo";

    private R_Config config;

    private FilteredRConnection connection;

    /**
     * Indicates if the WPS working directory should be deleted after process execution
     */
    private boolean deleteWPSWorkDirectory = true;

    private RExecutor executor;

    private RIOHandler iohandler;

    private RWorkspace workspace;

    public RWorkspaceManager(FilteredRConnection connection, RIOHandler iohandler, R_Config config) {
        this.connection = connection;
        this.workspace = new RWorkspace();
        this.executor = new RExecutor();
        this.iohandler = iohandler;
        this.config = config;
    }

    public void cleanUpInR(String originalWorkDir) {
        // R_Config config = R_Config.getInstance();
        // RConnection connection = rCon;
        // if (rCon == null || !rCon.isConnected()) {
        // log.debug("[R] opening new connection for cleanup...");
        // connection = config.openRConnection();
        // }

        log.debug("Cleaning up workspace.");
        try {
            this.connection.eval("rm(list = ls())");
        }
        catch (RserveException e) {
            log.error("Could not remove items from workspace", e);
        }

        this.workspace.delete(this.connection, originalWorkDir);
    }

    public void cleanUpWithWPS() {
        try {
            if (this.deleteWPSWorkDirectory) {
                // try to delete current local workdir - folder
                File workdir = new File(workspace.getPath());
                boolean deleted = deleteRecursive(workdir);
                if ( !deleted)
                    log.warn("Failed to delete temporary WPS Workdirectory: " + workdir.getAbsolutePath());
            }
        }
        catch (Exception e) {
            log.error("Problem deleting the wps work directory.", e);
        }
    }

    /**
     * Deletes File or Directory completely with its content
     * 
     * @param in
     *        File or directory
     * @return true if all content could be deleted
     */
    private boolean deleteRecursive(File in) {
        boolean success = true;
        if ( !in.exists()) {
            return false;
        }
        if (in.isDirectory()) {
            File[] files = in.listFiles();
            for (File file : files) {
                if (file.isFile()) {
                    success = success && file.delete();
                }
                if (file.isDirectory()) {
                    success = success && deleteRecursive(file);
                }
            }
        }
        if (success) {
            success = success && in.delete();
        }
        return success;
    }

    public void loadInputValues(Map<String, List<IData>> inputData, List<RAnnotation> inAnnotations) throws RAnnotationException,
            ExceptionReport {
        log.debug("Loading input values...");

        // Searching for missing inputs to apply standard values:
        log.debug("in annonations: " + Arrays.toString(inAnnotations.toArray()));

        // -------------------------------
        // Input value initialization:
        // -------------------------------
        HashMap<String, String> inputValues = new HashMap<String, String>();
        ArrayList<String> inputValuesWithValues = new ArrayList<String>();

        for (Entry<String, List<IData>> entry : inputData.entrySet()) {
            // parses input values to R-compatible literals and streams input files to workspace
            try {
                String entryRValue = this.iohandler.parseInput(entry.getValue(), connection);
                log.debug("Parsed input for '{}' to '{}' based on value '{}'",
                          entry.getKey(),
                          entryRValue,
                          entry.getValue());
                inputValues.put(entry.getKey(), entryRValue);

                inputValuesWithValues.add(entry.getKey());
            }
            catch (RserveException e) {
                log.error("Error parsing input value {}", entry, e);
                throw new ExceptionReport("Error parsing input value: " + entry,
                                          ExceptionReport.INVALID_PARAMETER_VALUE,
                                          e);
            }
            catch (REXPMismatchException e) {
                log.error("Error parsing input value {}", entry, e);
                throw new ExceptionReport("Error parsing input value: " + entry,
                                          ExceptionReport.INVALID_PARAMETER_VALUE,
                                          e);
            }
            catch (IOException e) {
                log.error("Error parsing input value {}", entry, e);
                throw new ExceptionReport("Error parsing input value: " + entry,
                                          ExceptionReport.INVALID_PARAMETER_VALUE,
                                          e);
            }
        }
        log.debug("Input: {}", Arrays.toString(inAnnotations.toArray()));

        // parses default values to R-compatible literals if no value has been set
        for (RAnnotation rAnnotation : inAnnotations) {
            String id = rAnnotation.getStringValue(RAttribute.IDENTIFIER);
            if ( !inputValuesWithValues.contains(id)) {
                String value = rAnnotation.getStringValue(RAttribute.DEFAULT_VALUE);
                Class< ? extends IData> iClass = this.iohandler.getInputDataType(id, inAnnotations);
                String inputValue = this.iohandler.parseLiteralInput(iClass, value);
                log.debug("Loaded default input value '{}' for '{}'", inputValue, rAnnotation);
                inputValues.put(id, inputValue);
            }
        }
        log.debug("Assigns (including defaults): {}", Arrays.toString(inputValues.entrySet().toArray()));

        // assign values to the (clean) workspace:
        log.debug("Assigning values...");
        Set<Entry<String, String>> inputValues2 = inputValues.entrySet();

        for (Entry<String, String> entry : inputValues2) {
            // use eval, not assign (assign only parses strings)
            String statement = entry.getKey() + " <- " + entry.getValue();
            log.debug("Running statement '{}'", statement);

            try {
                connection.filteredEval(statement);
            }
            catch (RserveException e) {
                log.error("Error executing statement '{}'", statement, e);
                throw new ExceptionReport("Error executing statement: " + statement + ": " + e.getMessage(),
                                          ExceptionReport.INVALID_PARAMETER_VALUE,
                                          e);
            }
        }

        RLogger.log(connection, "Workspace content after loading input values:");
        try {
            connection.filteredEval("cat(capture.output(ls()), \"\\n\")");
        }
        catch (RserveException e) {
            log.error("Error capturing ls() output.", e);
        }
    }

    public void loadResources(List<RAnnotation> resources) throws RAnnotationException, ExceptionReport, IOException {
        try {
            loadResourcesListInSession(resources);
        }
        catch (RserveException e) {
            log.error("Problem loading resources list to session, list: {}", resources, e);
        }

        loadResourcesToWorkspace(resources);

        log.debug("Workspace contents after resource loading: {}", this.workspace.listFiles());
    }

    private void loadResourcesListInSession(Collection<RAnnotation> resources) throws RserveException,
            RAnnotationException {
        log.debug("Saving resources in session: {}", resources);

        String wpsScriptResources = null;
        // Assign and concatenate lists of resources given by the ressource annotations
        wpsScriptResources = "list()";
        connection.filteredEval(RWPSSessionVariables.SCRIPT_RESOURCES + " <- " + wpsScriptResources);
        for (RAnnotation annotation : resources) {
            wpsScriptResources = annotation.getStringValue(RAttribute.NAMED_LIST_R_SYNTAX);
            connection.filteredEval(RWPSSessionVariables.SCRIPT_RESOURCES + " <- " + "append("
                    + RWPSSessionVariables.SCRIPT_RESOURCES + ", " + wpsScriptResources + ")");
        }

        log.debug("Assigned recource urls to variable '{}': {}",
                  RWPSSessionVariables.SCRIPT_RESOURCES,
                  wpsScriptResources);
        RLogger.logVariable(connection, RWPSSessionVariables.SCRIPT_RESOURCES);
    }

    private void loadResourcesToWorkspace(Collection<RAnnotation> resources) throws RAnnotationException,
            ExceptionReport,
            IOException {
        log.debug("Loading resources into workspace: {}", resources);

        for (RAnnotation resourceAnnotation : resources) {
            Object resObject = resourceAnnotation.getObjectValue(RAttribute.NAMED_LIST);
            Collection< ? > resourceCollection;

            if (resObject instanceof Collection< ? >)
                resourceCollection = (Collection< ? >) resObject;
            else {
                log.warn("Unsupported resource object: {}", resObject);
                continue;
            }

            for (Object element : resourceCollection) {
                R_Resource resource;
                if (element instanceof R_Resource)
                    resource = (R_Resource) element;
                else {
                    log.warn("Unsupported resource element: {}", element);
                    continue;
                }

                File resourceFile = resource.getFullResourcePath(this.config);
                if (resourceFile == null || !resourceFile.exists()) {
                    throw new ExceptionReport("Resource does not exist: " + resourceAnnotation,
                                              ExceptionReport.NO_APPLICABLE_CODE);
                }
                log.debug("Loading resource {} from file {} (directory: {})",
                          resource,
                          resourceFile,
                          resourceFile.isDirectory());
                streamFromWPSToRserve(resourceFile);
            }
        }

        log.debug("Loaded resources, workspace files: {}", this.workspace.listFiles());
    }

    private void loadUtilityScripts() throws RserveException,
            IOException,
            FileNotFoundException,
            RAnnotationException,
            ExceptionReport {
        log.debug("Loading utility scripts.");
        File[] utils = new File(config.utilsDirFull).listFiles(new RFileExtensionFilter());
        for (File file : utils) {
            this.executor.executeScript(file, this.connection);
        }

        RLogger.log(connection, "workspace content after loading utility scripts:");
        connection.filteredEval("cat(capture.output(ls()), \"\n\")");
    }

    public void loadWPSSessionVariables(String processWKN) throws ExceptionReport {
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

            connection.assign(RWPSSessionVariables.RESOURCE_URL_NAME, config.getResourceDirURL());
            log.debug("Assigned resource directory to variable '{}': {}",
                      RWPSSessionVariables.RESOURCE_URL_NAME,
                      config.getResourceDirURL());
            RLogger.logVariable(connection, RWPSSessionVariables.RESOURCE_URL_NAME);

            URL processDescription = config.getProcessDescriptionURL(processWKN);

            connection.assign(RWPSSessionVariables.PROCESS_DESCRIPTION, processDescription.toString());
            RLogger.logVariable(connection, RWPSSessionVariables.PROCESS_DESCRIPTION);

            log.debug("Assigned process description to variable '{}': {}",
                      RWPSSessionVariables.PROCESS_DESCRIPTION,
                      processDescription);

            // create session variable for warning storage
            cmd = RWPSSessionVariables.WARNING_OUTPUT_STORAGE + "=c()\n";
            connection.eval(cmd);

            RLogger.log(connection, "workspace content after loading session variables:");
            connection.eval("cat(capture.output(ls()), \"\n\")");
        }
        catch (RserveException e) {
            log.error("Error loading WPS session variables for process {}", processWKN);
            throw new ExceptionReport("Could not load session variables for " + processWKN,
                                      ExceptionReport.REMOTE_COMPUTATION_ERROR,
                                      e);
        }
    }

    /**
     * Retrieves warnings that occured during the last execution of a script
     * 
     * Note that the warnings()-method is not reliable for Rserve because it does not return warnings in most
     * cases. Therefore a specific warnings function is used to retrieve the warnings.
     */
    private String parseWarnings() throws RserveException, REXPMismatchException {
        StringBuilder warnings = new StringBuilder();
        REXP result = connection.eval(RWPSSessionVariables.WARNING_OUTPUT_STORAGE);
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

    /**
     * @return the original work directory or the R session
     */
    public String prepareWorkspace(Map<String, List<IData>> inputData, String processWKN) throws RserveException,
            REXPMismatchException,
            ExceptionReport,
            FileNotFoundException,
            IOException,
            RAnnotationException {
        log.debug("Preparing workspace...");

        log.debug("Rengine: {} | R server version: {}", REngine.getLastEngine(), connection.getServerVersion());

        log.debug("Cleaning session.");
        connection.eval("rm(list = ls())");

        // Retrieve the preset R working directory (R will be reset to
        // this directory after the process run)
        String originalWD = connection.eval("getwd()").asString();

        // Set R working directory according to configuration
        String strategy = this.config.getConfigVariable(RWPSConfigVariables.R_WORK_DIR_STRATEGY);
        boolean isRserveOnLocalhost = this.config.getRServeHost().equalsIgnoreCase("localhost");
        String workDirNameSetting = null;

        try {
            workDirNameSetting = this.config.getConfigVariableFullPath(RWPSConfigVariables.R_WORK_DIR_NAME);
        }
        catch (ExceptionReport e) {
            log.error("The config variable {} references a non-existing directory. This will be an issue if the variable is used. The current strategy is '{}'.",
                      RWPSConfigVariables.R_WORK_DIR_NAME,
                      strategy,
                      e);
        }

        this.workspace.setWorkingDirectory(this.connection,
                                           originalWD,
                                           strategy,
                                           isRserveOnLocalhost,
                                           workDirNameSetting);

        loadUtilityScripts();
        loadWPSSessionVariables(processWKN);

        return originalWD;
    }

    /**
     * saves an image to the working directory that may help debugging R scripts
     */
    public boolean saveImage(String name) {
        String filename = name + "." + RDATA_FILE_EXTENSION;
        try {
            REXP result = connection.eval("save.image(file=\"" + filename + "\")");
            log.debug("Saved image to {} with result {}", filename, result);
            return true;
        }
        catch (RserveException e) {
            log.error("Could not save image to {}", filename, e);
            return false;
        }
    }

    /**
     * @return the result has including sessionInfo() and warnings()
     * @throws REXPMismatchException
     */
    public HashMap<String, IData> saveOutputValues(Collection<RAnnotation> outAnnotations) throws RAnnotationException,
            ExceptionReport {
        HashMap<String, IData> result = new HashMap<String, IData>();

        for (RAnnotation rAnnotation : outAnnotations) {
            String resultId = rAnnotation.getStringValue(RAttribute.IDENTIFIER);
            REXP evalResult;
            try {
                evalResult = connection.eval(resultId);
            }
            catch (RserveException e) {
                log.error("Could not find value for annotation {} in the current session, result id: {}",
                          rAnnotation,
                          resultId,
                          e);
                throw new ExceptionReport("Error saving output value " + resultId,
                                          ExceptionReport.REMOTE_COMPUTATION_ERROR,
                                          e);
            }

            // TODO depending on the generated outputs deleteWorkDirectory must be set!
            try {
                IData output = this.iohandler.parseOutput(connection,
                                                          resultId,
                                                          evalResult,
                                                          outAnnotations,
                                                          this.workspace);
                result.put(resultId, output);

                log.debug("Output for {} is {} with payload {}", resultId, output, output.getPayload());
            }
            catch (ExceptionReport e) {
                throw e;
            }
            catch (RserveException e) {
                log.error("Could not create output for {}", resultId, e);
            }
            catch (IOException e) {
                log.error("Could not create output for {}", resultId, e);
            }
            catch (REXPMismatchException e) {
                log.error("Could not create output for {}", resultId, e);
            }
        }

        try {
            String sessionInfo = RSessionInfo.getSessionInfo(connection);
            InputStream sessionInfoStream = new ByteArrayInputStream(sessionInfo.getBytes("UTF-8"));
            result.put(SESSION_INFO_OUTPUT_NAME,
                       new GenericFileDataBinding(new GenericFileData(sessionInfoStream,
                                                                      GenericFileDataConstants.MIME_TYPE_PLAIN_TEXT)));
            sessionInfoStream.close();

            String warnings = parseWarnings();
            InputStream warningsStream = new ByteArrayInputStream(warnings.getBytes("UTF-8"));
            result.put(WARNING_OUTPUT_NAME,
                       new GenericFileDataBinding(new GenericFileData(warningsStream,
                                                                      GenericFileDataConstants.MIME_TYPE_PLAIN_TEXT)));
            warningsStream.close();
        }
        catch (UnsupportedEncodingException e) {
            log.error("Could not save session info and warnings.", e);
        }
        catch (IOException e) {
            log.error("Could not save session info and warnings.", e);
        }
        catch (REXPMismatchException e) {
            log.error("Could not save session info and warnings.", e);
        }
        catch (RserveException e) {
            log.error("Could not save session info and warnings.", e);
        }

        return result;
    }

    private void streamFromWPSToRserve(File source) throws IOException {
        streamFromWPSToRserve(source, RWorkspace.ROOT);
    }

    private void streamFromWPSToRserve(File source, String path) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(path);
        sb.append("/");
        sb.append(source.getName());
        String name = sb.toString();

        log.debug("Copying {} (directory: {}, path: '{}') to as '{}' to {} ",
                  source,
                  source.isDirectory(),
                  path,
                  name,
                  this.workspace);

        if ( !source.isDirectory()) {
            
            this.workspace.copyFile(source, name, connection);
        }
        else {
            // create directory and append path for recursive calls
            try {
                // create subdir in R
                this.workspace.createDirectory(name, this.connection);

                String[] files = source.list();
                for (String file : files) {
                    File sourceFile = new File(source, file);
                    streamFromWPSToRserve(sourceFile, name);
                }
            }
            catch (RserveException e) {
                log.error("Error creating directory in workdir", e);
                throw new IOException(e);
            }
            
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RWorkspaceManager [");
        if (connection != null) {
            builder.append("connection=");
            builder.append(connection);
            builder.append(", ");
        }
        builder.append("deleteWPSWorkDirectory=");
        builder.append(deleteWPSWorkDirectory);
        builder.append(", ");
        if (executor != null) {
            builder.append("executor=");
            builder.append(executor);
            builder.append(", ");
        }
        if (iohandler != null) {
            builder.append("iohandler=");
            builder.append(iohandler);
            builder.append(", ");
        }
        if (workspace != null) {
            builder.append("workspace=");
            builder.append(workspace);
        }
        builder.append("]");
        return builder.toString();
    }

}
