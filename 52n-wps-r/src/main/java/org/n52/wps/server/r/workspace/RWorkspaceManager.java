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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.GenericFileDataConstants;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.r.RInjectionFilter;
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
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RFileOutputStream;
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

    private static final int TEMPDIR_NAME_LENGTH = 8;

    private static final String WARNING_OUTPUT_NAME = "warnings";

    private R_Config config;

    private RConnection connection;

    /**
     * Indicates if the R working directory should be deleted after process execution If wpsWorkDirIsRWorkDir
     * is set true, deletion of the directory shall be determined by deleteWPSWorDirectory
     */
    private boolean deleteRWorkDirectory = true;
    
    /**
     * Indicates if the WPS working directory should be deleted after process execution
     */
    private boolean deleteWPSWorkDirectory = true;

    private RExecutor executor;

    private RInjectionFilter filter = new RInjectionFilter();

    private RIOHandler iohandler;

    /**
     * In case of errors, this variable may be changed to true during runtime to prevent the system from
     * deleting the wrong files
     */
    private boolean temporaryPreventRWorkingDirectoryFromDelete = false;

    private String wpsWorkDir;

    private boolean wpsWorkDirIsRWorkDir = true;

    public RWorkspaceManager(RConnection connection, RIOHandler iohandler, R_Config config) {
        this.connection = connection;
        this.wpsWorkDir = createWorkspacePath();
        this.executor = new RExecutor();
        this.iohandler = iohandler;
        this.config = config;
    }

    public void cleanUpSession(String originalWorkDir) throws RserveException, REXPMismatchException {
//        R_Config config = R_Config.getInstance();
//        RConnection connection = rCon;
//        if (rCon == null || !rCon.isConnected()) {
//            log.debug("[R] opening new connection for cleanup...");
//            connection = config.openRConnection();
//        }

        log.debug("[R] cleaning up workspace.");
        this.connection.eval("rm(list = ls())");

        if (this.wpsWorkDirIsRWorkDir) { // <- R won't delete the folder if it
                                         // is the same as the wps work
                                         // directory
            log.debug("[R] closing stream.");
            // connection.
            this.connection.close();
            return;
        }

        // deletes R work directory:
        if (this.wpsWorkDir != null) {
            String currentwd = this.connection.eval("getwd()").asString();

            log.debug("[R] setwd to {} (was: {})", originalWorkDir, currentwd);

            // the next lines throws and exception, because r_basedir might not
            // succesfully have been
            // set, so check first
            connection.eval("setwd(\"" + originalWorkDir + "\")");
            // should be true usually, if not, workdirectory has been
            // changed unexpectedly (prob. inside script)
            if (currentwd != this.wpsWorkDir && this.deleteRWorkDirectory) {
                if ( !this.temporaryPreventRWorkingDirectoryFromDelete) {
                    log.debug("[R] unlinking (recursive) {}", currentwd);
                    connection.eval("unlink(\"" + currentwd + "\", recursive=TRUE)");
                }
                else
                    this.temporaryPreventRWorkingDirectoryFromDelete = false;
            }
            else
                log.warn("Unexpected R workdirectory at end of R session, check the R sript for unwanted workdirectory changes");
        }

        log.debug("[R] closing stream.");
        connection.close();
    }

    public void cleanUpWorkdir() {
        // try to delete current local workdir - folder
        if (this.deleteWPSWorkDirectory) {
            File workdir = new File(wpsWorkDir);
            boolean deleted = deleteRecursive(workdir);
            if ( !deleted)
                log.warn("Failed to delete temporary WPS Workdirectory: " + workdir.getAbsolutePath());
        }
    }

    protected String createWorkspacePath() {
        File tempdir = new File(System.getProperty("java.io.tmpdir"), "wps4r-wps-workdir-tmp-"
                + UUID.randomUUID().toString().substring(0, TEMPDIR_NAME_LENGTH));
        tempdir.mkdir();
        return tempdir.getAbsolutePath();
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
            IOException,
            RserveException,
            REXPMismatchException {
        log.debug("Loading input values...");

        // Searching for missing inputs to apply standard values:
        log.debug("in annonations: " + Arrays.toString(inAnnotations.toArray()));

        // -------------------------------
        // Input value initialization:
        // -------------------------------
        HashMap<String, String> inputValues = new HashMap<String, String>();
        Iterator<Map.Entry<String, List<IData>>> iterator = inputData.entrySet().iterator();

        // parses input values to R-compatible literals and streams input files to workspace
        while (iterator.hasNext()) {
            Map.Entry<String, List<IData>> entry = iterator.next();
            inputValues.put(entry.getKey(), this.iohandler.parseInput(entry.getValue(), connection));
            RAnnotation current = RAnnotation.filterAnnotations(inAnnotations, RAttribute.IDENTIFIER, entry.getKey()).get(0);
            inAnnotations.remove(current);
        }
        log.debug("Input: {}", Arrays.toString(inAnnotations.toArray()));

        // parses default values to R-compatible literals:
        for (RAnnotation rAnnotation : inAnnotations) {
            String id = rAnnotation.getStringValue(RAttribute.IDENTIFIER);
            String value = rAnnotation.getStringValue(RAttribute.DEFAULT_VALUE);
            Class< ? extends IData> iClass = this.iohandler.getInputDataType(id, inAnnotations);
            String inputValue = this.iohandler.parseLiteralInput(iClass, value);
            log.debug("Loaded input value '{}' for {}", inputValue, rAnnotation);
            inputValues.put(id, inputValue);
        }
        log.debug("Assigns: {}", Arrays.toString(inputValues.entrySet().toArray()));

        // assign values to the (clean) workspace:
        log.debug("[R] assign values.");
        Iterator<Map.Entry<String, String>> inputValuesIterator = inputValues.entrySet().iterator();

        // load input variables
        while (inputValuesIterator.hasNext()) {
            Map.Entry<String, String> entry = inputValuesIterator.next();
            // use eval, not assign (assign only parses strings)
            String statement = entry.getKey() + " <- " + entry.getValue();
            log.debug("[R] running {}", statement);
            connection.eval(statement);
        }

        RLogger.log(connection, "workspace content after loading input values:");
        connection.eval("cat(capture.output(ls()), \"\n\")");
    }

    public void loadResources(List<RAnnotation> resources) throws RserveException,
            RAnnotationException,
            ExceptionReport,
            IOException {
        loadResourcesListInSession(resources);
        loadResourcesToWorkspace(resources);
    }

    private void loadResourcesListInSession(Collection<RAnnotation> resources) throws RserveException,
            RAnnotationException {
        log.debug("Saving resources in session: {}", resources);

        String wpsScriptResources = null;
        // Assign and concatenate lists of resources given by the ressource annotations
        wpsScriptResources = "list()";
        connection.eval(RWPSSessionVariables.R_SESSION_SCRIPT_RESOURCES + " <- " + wpsScriptResources);
        for (RAnnotation annotation : resources) {
            wpsScriptResources = annotation.getStringValue(RAttribute.NAMED_LIST_R_SYNTAX);
            connection.eval(RWPSSessionVariables.R_SESSION_SCRIPT_RESOURCES + " <- " + "append("
                    + RWPSSessionVariables.R_SESSION_SCRIPT_RESOURCES + ", " + wpsScriptResources + ")");
        }

        log.debug("[R] assigned recource urls to variable '{}': {}",
                  RWPSSessionVariables.R_SESSION_SCRIPT_RESOURCES,
                  wpsScriptResources);
        RLogger.logVariable(connection, RWPSSessionVariables.R_SESSION_SCRIPT_RESOURCES);
    }

    private void loadResourcesToWorkspace(Collection<RAnnotation> resources) throws RAnnotationException,
            ExceptionReport,
            IOException {
        log.debug("Loading resources into session: {}", resources);

        for (RAnnotation resourceAnnotation : resources) {
            Object resObject = resourceAnnotation.getObjectValue(RAttribute.NAMED_LIST);
            Collection< ? > resourceCollection;

            if (resObject instanceof Collection< ? >)
                resourceCollection = (Collection< ? >) resObject;
            else
                continue;

            for (Object element : resourceCollection) {
                R_Resource resource;
                if (element instanceof R_Resource)
                    resource = (R_Resource) element;
                else
                    continue;

                File resourceFile = resource.getFullResourcePath(this.config);
                if (resourceFile == null || !resourceFile.exists()) {
                    throw new ExceptionReport("Resource cannot be loaded: " + resourceAnnotation,
                                              ExceptionReport.NO_APPLICABLE_CODE);
                }
                log.debug("Loading resource " + resourceAnnotation);
                streamFromWPSToRserve(resourceFile);
            }
        }
    }

    private void loadUtilityScripts() throws RserveException,
            IOException,
            FileNotFoundException,
            RAnnotationException,
            ExceptionReport {
        log.debug("[R] loading utility scripts.");
        File[] utils = new File(config.utilsDirFull).listFiles(new RFileExtensionFilter());
        for (File file : utils) {
            this.executor.executeScript(file, this.connection);
        }

        RLogger.log(connection, "workspace content after loading utility scripts:");
        connection.eval("cat(capture.output(ls()), \"\n\")");
    }

    public void loadWPSSessionVariables(String processWKN) throws RserveException, RAnnotationException {
        log.debug("[R] loading session variables.");

        RLogger.log(connection, "Environment:");
        connection.eval("environment()");

        String cmd = RWPSSessionVariables.WPS_SERVER + " <- TRUE";
        connection.eval(cmd);
        log.debug("[R] {}", cmd);
        RLogger.logVariable(connection, RWPSSessionVariables.WPS_SERVER);

        cmd = RWPSSessionVariables.WPS_SERVER_NAME + " <- \"52N-WPS\"";
        connection.eval(cmd);
        log.debug("[R] {}", cmd);
        RLogger.logVariable(connection, RWPSSessionVariables.WPS_SERVER_NAME);

        connection.assign(RWPSSessionVariables.RESOURCE_URL_NAME, config.getResourceDirURL());
        log.debug("[R] assigned resource directory to variable '{}': {}",
                  RWPSSessionVariables.RESOURCE_URL_NAME,
                  config.getResourceDirURL());
        RLogger.logVariable(connection, RWPSSessionVariables.RESOURCE_URL_NAME);

        URL processDescription = config.getProcessDescriptionURL(processWKN);

        connection.assign(RWPSSessionVariables.PROCESS_DESCRIPTION, processDescription.toString());
        RLogger.logVariable(connection, RWPSSessionVariables.PROCESS_DESCRIPTION);

        log.debug("[R] assigned process description to variable '{}': {}",
                  RWPSSessionVariables.PROCESS_DESCRIPTION,
                  processDescription);

        RLogger.log(connection, "workspace content after loading session variables:");
        connection.eval("cat(capture.output(ls()), \"\n\")");
    }

    /**
     * Retrieves warnings that occured during the last execution of a script
     * 
     * Note that the warnings()-method is not reliable for Rserve because it does not return warnings in most
     * cases, a workaround to retrieve the warnings is applied
     */
    private String parseWarnings(RConnection rCon) throws RserveException, REXPMismatchException {
        String warnings = "";
        REXP result = rCon.eval("wps_warn");
        if ( !result.isNull()) {
            String[] warningsArray = result.asStrings();
            for (int i = 0; i < warningsArray.length; i++) {
                String warn = warningsArray[i];
                warnings += "warning " + (i + 1) + ": '" + warn + "'\n";
            }
        }

        if (warnings.isEmpty())
            warnings = "The process proceeded without any warnings from R.";
        return warnings;
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
        log.debug("preparing workspace...");

        log.debug("[R] Rengine: {}", REngine.getLastEngine());
        log.debug("[R] R server version: {}", connection.getServerVersion());

        log.debug("[R] cleaning session.");
        connection.eval("rm(list = ls())");

        // Retrieve the preset R working directory (R will be reset to
        // this directory after the process run)
        String originalBasedir = connection.eval("getwd()").asString();
        // Set R working directory according to configuration
        setRWorkingDirectoryBeforeProcessing(originalBasedir);

        loadUtilityScripts();
        loadWPSSessionVariables(processWKN);

        return originalBasedir;
    }

    /**
     * saves an image to the working directory that may help debugging R scripts
     */
    public void saveImage(String name) throws RserveException {
        String filename = name + "." + RDATA_FILE_EXTENSION;
        REXP result = connection.eval("save.image(file=\"" + filename + "\")");
        log.debug("Saved image to {} with result {}", filename, result);
    }
    
    /**
     * @return the result has including sessionInfo() and warnings()
     * @throws REXPMismatchException
     */
    public HashMap<String, IData> saveOutputValues(Collection<RAnnotation> outAnnotations) throws RAnnotationException,
            RserveException,
            ExceptionReport {
        HashMap<String, IData> result = new HashMap<String, IData>();

        for (RAnnotation rAnnotation : outAnnotations) {
            String resultId = rAnnotation.getStringValue(RAttribute.IDENTIFIER);
            REXP evalResult = connection.eval(resultId);
            // TODO depending on the generated outputs deleteWorkDirectory must be set!
            try {
                IData output = this.iohandler.parseOutput(connection,
                                                          resultId,
                                                          evalResult,
                                                          outAnnotations,
                                                          this.wpsWorkDirIsRWorkDir,
                                                          wpsWorkDir);
                result.put(resultId, output);

                log.debug("Output for {} is {}", resultId, output);
            }
            catch (ExceptionReport e) {
                throw e; // re-throw exception reports
            }
            catch (Exception e) {
                log.error("Could not create output for {}", resultId, e);
            }
        }

        try {
            String sessionInfo = RSessionInfo.getSessionInfo(connection);
            InputStream sessionInfoStream = new ByteArrayInputStream(sessionInfo.getBytes("UTF-8"));
            result.put("sessionInfo",
                       new GenericFileDataBinding(new GenericFileData(sessionInfoStream,
                                                                      GenericFileDataConstants.MIME_TYPE_PLAIN_TEXT)));
            sessionInfoStream.close();

            String warnings = parseWarnings(connection);
            InputStream warningsStream = new ByteArrayInputStream(warnings.getBytes("UTF-8"));
            result.put(WARNING_OUTPUT_NAME,
                       new GenericFileDataBinding(new GenericFileData(sessionInfoStream,
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

        return result;
    }
    
    /**
     * Sets the R working directory according to the "R_Work_Dir" configuration parameter. 4 cases are
     * supported: 'default', 'preset', 'temporary' and 'custom'.
     * 
     * Do not confuse the R working directory with the temporary WPS working directory (this.currentworkdir)!
     * R and WPS use the same directory under default configuration, with Rserve on localhost, but running R
     * on a remote machine requires separate working directories for WPS and R.
     * 
     * @return the new working directory
     */
    private String setRWorkingDirectoryBeforeProcessing(String currentWPSWorkDir) throws REXPMismatchException,
            RserveException,
            ExceptionReport {
        log.debug("Original getwd(): {}", connection.eval("getwd()").asString());
        String configuredRWorkDir = this.config.getConfigVariable(RWPSConfigVariables.R_WORK_DIR);
        log.debug("Try to set R work directory according to {} = {}",
                  RWPSConfigVariables.R_WORK_DIR,
                  configuredRWorkDir);
        REXP result = null;
        boolean isLocalhost = this.config.getRServeHost().equalsIgnoreCase("localhost");
        log.debug("Working on localhost: {}", isLocalhost);

        if (configuredRWorkDir == null || configuredRWorkDir.equals("")
                || configuredRWorkDir.trim().equalsIgnoreCase("default")) {
            // Default behaviour: R work directory is the same as temporary WPS work directory if R runs
            // locally otherwise, for remote connections, it is dependent on the configuration of R and Rserve
            if (isLocalhost) {
                this.wpsWorkDirIsRWorkDir = true;
                result = connection.eval("setwd(\"" + currentWPSWorkDir.replace("\\", "/") + "\")");
            }
            else {
                // setting the R working directory relative to default R
                // directory
                // R starts from a work directory dependent on the behaviour and
                // configuration of the R/Rserve
                // installation
                this.wpsWorkDirIsRWorkDir = false;
                String randomFolderName = "wps4r-r-workdir-" + UUID.randomUUID().toString().substring(0, 8);
                connection.eval("dir.create(\"" + randomFolderName + "\")"); // quotation
                                                                       // marks!
                result = connection.eval("setwd(\"" + randomFolderName + "\")"); // don't
                                                                           // forget
                                                                           // the
                                                                           // escaped
                                                                           // quotation
                                                                           // marks
            }

        }
        else if (configuredRWorkDir.trim().equalsIgnoreCase("preset")) {
            // setting the R working directory relative to default R directory
            wpsWorkDirIsRWorkDir = false;
            String randomFolderName = "wps4r-r-workdir-" + UUID.randomUUID().toString().substring(0, 8);
            connection.eval("dir.create(\"" + randomFolderName + "\")"); // quotation
                                                                   // marks!
            result = connection.eval("setwd(\"" + randomFolderName + "\")"); // quotation
        } // marks!
          // setting the R working directory in a temporal folder
        else if (configuredRWorkDir.trim().equalsIgnoreCase("temporary")) {
            if (isLocalhost) {
                wpsWorkDirIsRWorkDir = true;
                result = connection.eval("setwd(\"" + currentWPSWorkDir.replace("\\", "/") + "\")");
            }
            else {
                wpsWorkDirIsRWorkDir = false;
                try {
                    result = connection.eval("setwd(\"tempdir()\")");
                }
                catch (RserveException e) {
                    temporaryPreventRWorkingDirectoryFromDelete = true;
                    throw new ExceptionReport("Invalid configuration of WPS4R, failed to create temporal working directory",
                                              ExceptionReport.REMOTE_COMPUTATION_ERROR,
                                              e);
                }

            }
        }
        else if (configuredRWorkDir.trim().equalsIgnoreCase("manual")) {

            String path = null;
            boolean isInvalidPath = false;
            if (isLocalhost) {
                try {
                    path = this.config.getConfigVariableFullPath(RWPSConfigVariables.R_WORK_DIR);
                    String randomFolderName = "wps4r-r-workdir-" + UUID.randomUUID().toString().substring(0, 8);
                    if (new File(path).isDirectory()) {
                        path = (path + "/" + randomFolderName).replace("\\", "/");
                        new File(path).mkdir();
                        result = connection.eval("setwd(\"" + path + "\")");
                    }
                    else {
                        isInvalidPath = true;
                    }
                }
                catch (ExceptionReport e) {
                    isInvalidPath = true;
                }
            }
            else {
                this.wpsWorkDirIsRWorkDir = false;
                boolean isExistingDir = connection.eval("isTRUE(file.info(\"" + configuredRWorkDir + "\")$isdir)").asInteger() == 1;
                if (isExistingDir) {
                    String randomFolderName = "wps4r-r-workdir-" + UUID.randomUUID().toString().substring(0, 8);
                    path = (path + "/" + randomFolderName).replace("\\", "/");
                    result = connection.eval("setwd(\"" + path + "\")");
                }
                else {
                    isInvalidPath = true;
                }
            }

            if (isInvalidPath) {
                log.warn("Invalid configurarion for variable '{}' | . Variable is switched temporarily to default.",
                         RWPSConfigVariables.R_WORK_DIR,
                         configuredRWorkDir);
                this.config.setConfigVariable(RWPSConfigVariables.R_WORK_DIR, "default");
                setRWorkingDirectoryBeforeProcessing(currentWPSWorkDir);
            }
        }

        if (connection.eval("length(dir()) == 0").asInteger() != 1) {
            this.temporaryPreventRWorkingDirectoryFromDelete = true;
            throw new ExceptionReport("Non-empty R working directory on process startup. The process will not be executed to prevent the system from damage."
                                              + " Please check the configuration of WPS4R.",
                                      ExceptionReport.REMOTE_COMPUTATION_ERROR);
        }

        String newWorkDir = result.asString();
        log.debug("[R] Old wd: {} | New wd: {}", newWorkDir, connection.eval("getwd()").asString());
        RLogger.logGenericRProcess(connection, "working directory: " + connection.eval("getwd()").asString());

        return newWorkDir;
    }

    private void streamFromWPSToRserve(File source) throws IOException {
        RFileOutputStream rfos = connection.createFile(source.getName());

        byte[] buffer = new byte[2048];
        FileInputStream is = new FileInputStream(source);
        int stop = is.read(buffer);

        while (stop != -1) {
            rfos.write(buffer, 0, stop);
            stop = is.read(buffer);
        }

        rfos.flush();
        rfos.close();
        is.close();
    }

}
