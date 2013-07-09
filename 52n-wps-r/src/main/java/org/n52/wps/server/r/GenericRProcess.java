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

package org.n52.wps.server.r;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.n52.wps.io.IOUtils;
import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.GenericFileDataConstants;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.ILiteralData;
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.io.data.binding.literal.LiteralByteBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralFloatBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralLongBinding;
import org.n52.wps.io.data.binding.literal.LiteralShortBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.io.datahandler.generator.GeotiffGenerator;
import org.n52.wps.io.datahandler.parser.GeotiffParser;
import org.n52.wps.server.AbstractObservableAlgorithm;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.r.data.RDataType;
import org.n52.wps.server.r.data.RDataTypeRegistry;
import org.n52.wps.server.r.data.RTypeDefinition;
import org.n52.wps.server.r.data.R_Resource;
import org.n52.wps.server.r.metadata.RAnnotationParser;
import org.n52.wps.server.r.metadata.RProcessDescriptionCreator;
import org.n52.wps.server.r.syntax.RAnnotation;
import org.n52.wps.server.r.syntax.RAnnotationException;
import org.n52.wps.server.r.syntax.RAnnotationType;
import org.n52.wps.server.r.syntax.RAttribute;
import org.n52.wps.server.r.syntax.RegExp;
import org.n52.wps.server.r.syntax.ResourceAnnotation;
import org.n52.wps.server.r.util.RLogger;
import org.n52.wps.server.r.util.RSessionInfo;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RFileInputStream;
import org.rosuda.REngine.Rserve.RFileOutputStream;
import org.rosuda.REngine.Rserve.RserveException;

public class GenericRProcess extends AbstractObservableAlgorithm {

    private static Logger log = LoggerFactory.getLogger(GenericRProcess.class);

    // private variables holding process information - initialization in
    // constructor
    private List<RAnnotation> annotations;

    /**
     * Indicates the current workdirectory on the WPS. This is NOT to confuse with the R/Rserve workdirectory
     * used inside the run-method.
     */
    private String currentWPSWorkDir;

    private boolean deleteWorkDirectory = false;

    private RAnnotationParser parser = new RAnnotationParser();

    public GenericRProcess(String wellKnownName) {
    	//note that superconstructor calls method initializeDescription()
        super(wellKnownName); 
        log.info("NEW " + this.toString());
    }
    
    public GenericRProcess() {
    	//note that superconstructor calls method initializeDescription()
        super();
        log.info("NEW " + this.toString());
    }

    private List<String> errors = new ArrayList<String>();

    private File scriptFile = null;

    private boolean debugScript = true;

    private boolean wpsWorkDirIsRWorkDir = true;

    public List<String> getErrors() {
        return this.errors;
    }

    /**
     * This method should be overwritten, in case you want to have a way of initializing.
     * 
     * In detail it looks for a xml descfile, which is located in the same directory as the implementing class
     * and has the same name as the class, but with the extension XML.
     * 
     * @return
     */
    protected ProcessDescriptionType initializeDescription() {
        log.info("Initializing description for " + this.toString());

        // Reading process information from script annotations:
        InputStream rScriptStream = null;
        try {
            String wkn = getWellKnownName();
            log.debug("Loading file for " + wkn);
            R_Config config = R_Config.getInstance();

            this.scriptFile = config.wknToFile(wkn);
            log.debug("File loaded: " + this.scriptFile.getAbsolutePath());

            if (this.scriptFile == null) {
                log.warn("Loaded script file is " + this.scriptFile);
                return null; // FIXME throw exception?
            }

            rScriptStream = new FileInputStream(this.scriptFile);
            if(parser == null)
            	this.parser = new RAnnotationParser(); //prevents NullpointerException
            this.annotations = this.parser.parseAnnotationsfromScript(rScriptStream);

            // have to process the resources to get full URLs to the files
            for (RAnnotation ann : this.annotations) {
                if (ann.getType().equals(RAnnotationType.RESOURCE)) {
                    if (ann instanceof ResourceAnnotation) {
                        ResourceAnnotation rann = (ResourceAnnotation) ann;

                        // FIXME problem: cannot get attributeHash here?
                        Iterator<R_Resource> iterator = rann.getResources().iterator();
                        while (iterator.hasNext()) {
                            R_Resource resource = iterator.next();

                            StringBuilder namedList = new StringBuilder();
                            namedList.append("list(");

                            String fullResourcePath = resource.getFullResourceURL().toExternalForm();

                            String resourceName = "\"" + resource.getResourceValue() + "\"";

                            if (fullResourcePath != null) {
                                namedList.append(resourceName + " = " + "\"" + fullResourcePath + "\"");
                                if (iterator.hasNext()) {
                                    namedList.append(", ");
                                }
                            }
                            else
                                log.warn("Resource NOT added becaues full resource path missing: " + resourceName);

                            namedList.append(")");
                        }
                    }
                }
            }

            // submits annotation with process informations to
            // ProcessdescriptionCreator:
            RProcessDescriptionCreator creator = new RProcessDescriptionCreator();
            ProcessDescriptionType doc = creator.createDescribeProcessType(this.annotations,
                                                                           wkn,
                                                                           config.getScriptURL(wkn),
                                                                           config.getSessionInfoURL());

            log.debug("Created process description for " + wkn + ":\n" + doc.xmlText());
            return doc;
        }
        catch (RAnnotationException rae) {
            log.error(rae.getMessage());
            throw new RuntimeException("Annotation error while parsing process description: " + rae.getMessage());
        }
        catch (IOException ioe) {
            log.error("I/O error while parsing process description: " + ioe.getMessage());
            throw new RuntimeException("I/O error while parsing process description: " + ioe.getMessage());
        }
        catch (ExceptionReport e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Error creating process descriptionn.", e);
        }
        finally {
            try {
                if (rScriptStream != null)
                    rScriptStream.close();
            }
            catch (IOException e) {
                log.error("Error closing script stream.", e);
            }
        }
    }

    public Map<String, IData> run(Map<String, List<IData>> inputData) throws ExceptionReport {
        log.info("Running " + this.toString());
        if (log.isDebugEnabled())
            log.debug("inputData: " + Arrays.toString(inputData.entrySet().toArray()));

        R_Config config = R_Config.getInstance();

        this.currentWPSWorkDir = config.createTemporaryWPSWorkDir();

        // File file = new File(this.currentWPSWorkDir);
        // file.mkdirs();
        log.debug("Temp folder for WPS4R: " + this.currentWPSWorkDir);

        // ------------------------------------
        // interaction with R follows here:
        // ------------------------------------
        RConnection rCon = null;
        HashMap<String, IData> resulthash = new HashMap<String, IData>();
        try {
            String r_basedir = null;
            try {
                // initializes connection and pre-settings
                rCon = config.openRConnection();

                RLogger.logGenericRProcess(rCon,
                                           "Running algorithm with input "
                                                   + Arrays.deepToString(inputData.entrySet().toArray()));

                log.debug("[R] cleaning session.");
                // ensure that session is clean;
                rCon.eval("rm(list = ls())");

                // Retrieve the preset R working directory (R will be reset to this directory after the
                // process run)
                r_basedir = rCon.eval("getwd()").asString();
                // Set R working directory according to configuration
                setRWorkingDirectoryBeforeProcessing(rCon);

                loadRUtilityScripts(rCon);

                // Searching for missing inputs to apply standard values:
                List<RAnnotation> inNotations = RAnnotation.filterAnnotations(this.annotations, RAnnotationType.INPUT);
                log.debug("inNonations: " + Arrays.toString(inNotations.toArray()));

                // -------------------------------
                // Input value initialization:
                // -------------------------------
                HashMap<String, String> inputValues = new HashMap<String, String>();
                Iterator<Map.Entry<String, List<IData>>> iterator = inputData.entrySet().iterator();

                // parses input values to R-compatible literals and streams
                // input files to workspace
                log.debug("Parsing input values.");
                while (iterator.hasNext()) {
                    Map.Entry<String, List<IData>> entry = iterator.next();
                    inputValues.put(entry.getKey(), parseInput(entry.getValue(), rCon));
                    RAnnotation current = RAnnotation.filterAnnotations(inNotations,
                                                                        RAttribute.IDENTIFIER,
                                                                        entry.getKey()).get(0);
                    inNotations.remove(current);
                }
                log.debug("Input: " + Arrays.toString(inNotations.toArray()));

                // parses default values to R-compatible literals:
                for (RAnnotation rAnnotation : inNotations) {
                    String id = rAnnotation.getStringValue(RAttribute.IDENTIFIER);
                    String value = rAnnotation.getStringValue(RAttribute.DEFAULT_VALUE);
                    Class< ? extends IData> iClass = getInputDataType(id);
                    // solution should be suitable for most literal input
                    // values:
                    inputValues.put(id, parseLiteralInput(iClass, value));
                }
                log.debug("Assigns: " + Arrays.toString(inputValues.entrySet().toArray()));

                // delete help variables and utility functions from workspace:
                log.debug("[R] remove utility functions.");
                rCon.eval("rm(list = ls())");

                // assign values to the (clean) workspace:
                log.debug("[R] assign values.");
                Iterator<Map.Entry<String, String>> inputValuesIterator = inputValues.entrySet().iterator();

                /*
                 * create workspace metadata - TODO this should be done using global variables or an
                 * environment...
                 */
                assignRWPSSessionVariables(rCon);

                // load input variables
                while (inputValuesIterator.hasNext()) {
                    Map.Entry<String, String> entry = inputValuesIterator.next();
                    // use eval, not assign (assign only parses strings)
                    String statement = entry.getKey() + " <- " + entry.getValue();
                    log.debug("[R] running " + statement);
                    rCon.eval(statement);
                }

                // FIXME load resources
                List<RAnnotation> resAnnotList = RAnnotation.filterAnnotations(this.annotations,
                                                                               RAnnotationType.RESOURCE);
                for (RAnnotation res : resAnnotList) {
                    if ( ! (res instanceof ResourceAnnotation))
                        continue;
                    ResourceAnnotation resourceAnnotation = (ResourceAnnotation) res;
                    for (R_Resource resource : resourceAnnotation.getResources()) {
                        log.debug("Loading resource " + res);
                        streamFromWPSToRserve(rCon, resource.getFullResourcePath());
                    }

                }

                // retrieve R-Script from path:
                InputStream rScriptStream = null;
                File rScriptFile = null;
                try {
                    rScriptFile = config.wknToFile(getWellKnownName());
                    rScriptStream = new FileInputStream(rScriptFile);
                }
                catch (IOException e) {
                    log.error("Error reading script file.", e);
                    throw new ExceptionReport("Could not read script file " + rScriptFile + " for algorithm "
                            + getWellKnownName(), "Input/Output", e);
                }

                // save an image that may help debugging R scripts
                if (log.isDebugEnabled()) {
                    rCon.eval("save.image(file=\"debug.RData\")");
                    log.debug("Saved image to debug.RData");
                }

                // -------------------------------
                // R script execution:
                // -------------------------------
                boolean success = false;
                success = executeScript(rScriptStream, rCon);

                try {
                    // BufferedReader rScriptStream;
                    rScriptStream.close();
                }
                catch (IOException e) {
                    log.warn("Connection to R script cannot be closed for process " + getWellKnownName());
                }

                if ( !success) {
                    String message = "Failure while executing R script. See logs for details";
                    log.error(message);
                }

                // retrieving result (REXP - Regular Expression Datatype)
                List<RAnnotation> outNotations = RAnnotation.filterAnnotations(this.annotations, RAnnotationType.OUTPUT);
                for (RAnnotation rAnnotation : outNotations) {
                    String result_id = rAnnotation.getStringValue(RAttribute.IDENTIFIER);
                    REXP result = rCon.eval(result_id);
                    // TODO: change ParseOutput
                    // TODO depending on the generated outputs,
                    // deleteWorkDirectory must be set!
                    resulthash.put(result_id, parseOutput(result_id, result, rCon));
                }

                String sessionInfo = RSessionInfo.getSessionInfo(rCon);
                InputStream byteArrayInputStream = new ByteArrayInputStream(sessionInfo.getBytes("UTF-8"));
                resulthash.put("sessionInfo", new GenericFileDataBinding(new GenericFileData(byteArrayInputStream, GenericFileDataConstants.MIME_TYPE_PLAIN_TEXT)));
  
                String warnings = RSessionInfo.getConsoleOutput(rCon, "warnings()");
                byteArrayInputStream = new ByteArrayInputStream(warnings.getBytes("UTF-8"));
                resulthash.put("warnings", new GenericFileDataBinding(new GenericFileData(byteArrayInputStream, GenericFileDataConstants.MIME_TYPE_PLAIN_TEXT)));

            }
            catch (IOException e) {
                String message = "Attempt to run R script file failed:\n" + e.getClass() + " - "
                        + e.getLocalizedMessage() + "\n" + e.getCause();
                log.error(message, e);
                throw new ExceptionReport(message, e.getClass().getName(), e);
            }
            catch (RAnnotationException e) {
                String message = "R script cannot be executed due to invalid annotations.";
                log.error(message, e);
                throw new ExceptionReport(message, e.getClass().getName(), e);

            }
            catch (RserveException e) {
                log.error("Rserve problem executing script: " + e.getMessage(), e);
                throw e;
            }
            finally {
                cleanUpRSession(rCon, r_basedir);
            }
        }
        catch (RserveException e) {
            String message = "An R Connection Error occured:\n" + e.getClass() + " - " + e.getLocalizedMessage() + "\n"
                    + e.getCause();
            log.error(message, e);
            throw new ExceptionReport("Error with the R connection", "R", "R_Connection", e);
        }
        catch (REXPMismatchException e) {
            String message = "An R Parsing Error occoured:\n" + e.getMessage() + e.getClass() + " - "
                    + e.getLocalizedMessage() + "\n" + e.getCause();
            log.error(message, e);
            throw new ExceptionReport(message, "R", "R_Connection", e);
        }

        // try to delete current local workdir - folder
        if (this.deleteWorkDirectory) {
            File workdir = new File(this.currentWPSWorkDir);
            boolean deleted = deleteRecursive(workdir);
            if ( !deleted)
                log.warn("Failed to delete temporary WPS Workdirectory: " + workdir.getAbsolutePath());
        }

        log.debug("RESULT: " + Arrays.toString(resulthash.entrySet().toArray()));
        return resulthash;
    }

    /**
     * Sets the R working directory according to the "R_Work_Dir" configuration parameter. 4 cases are
     * supported: default, preset, temporary and custom.
     * 
     * Do not confuse the R working directory with the temporary WPS working directory (this.currentworkdir)!
     * R and WPS use the same directory under default configuration, with Rserve on localhost, but running R
     * on a remote machine requires separate working directories for WPS and R.
     * 
     * @param rCon
     *        The (open) R connection to be used. This method inherently does not call open- or
     *        close-operations.
     * @throws REXPMismatchException
     * @throws RserveException
     */
    private void setRWorkingDirectoryBeforeProcessing(RConnection rCon) throws REXPMismatchException, RserveException {
        R_Config rconf = R_Config.getInstance();

        log.debug("[WPS4R] Original getwd(): " + rCon.eval("getwd()").asString());
        String config_RWorkDir = rconf.getConfigVariable(RWPSConfigVariables.R_WORK_DIR);
        log.debug("Try to set R work directory according to " + RWPSConfigVariables.R_WORK_DIR + " | "
                + config_RWorkDir);
        REXP result = null;
        boolean isLocalhost = rconf.getConfigVariable(RWPSConfigVariables.RSERVE_HOST).equalsIgnoreCase("localhost");

        if (config_RWorkDir == null || config_RWorkDir.equals("") || config_RWorkDir.trim().equalsIgnoreCase("default")) {
            // Default behaviour: R work directory is the same as temporary WPS work directory if R runs
            // locally otherwise,
            // for remote connections, it is dependent on the configuration of R / Rserve
            if (isLocalhost) {
                this.wpsWorkDirIsRWorkDir = true;
                result = rCon.eval("setwd(\"" + this.currentWPSWorkDir.replace("\\", "/") + "\")");
            }
            else {
                // setting the R working directory relative to default R directory
                // R starts from a work directory dependent on the behaviour and configuration of the R/Rserve
                // installation
                this.wpsWorkDirIsRWorkDir = false;
                String randomFolderName = "wps4r-r-workdir-" + UUID.randomUUID().toString().substring(0, 8);
                rCon.eval("dir.create(\"" + randomFolderName + "\")"); // quotation marks!
                result = rCon.eval("setwd(\"" + randomFolderName + "\")"); // don't forget the escaped
                                                                           // quotation marks
            }

        }
        else if (config_RWorkDir.trim().equalsIgnoreCase("preset")) {
            // setting the R working directory relative to default R directory
            String randomFolderName = "wps4r-r-workdir-" + UUID.randomUUID().toString().substring(0, 8);
            rCon.eval("dir.create(\"" + randomFolderName + "\")"); // quotation marks!
            result = rCon.eval("setwd(\"" + randomFolderName + "\")"); // quotation marks!
        }
        else if (config_RWorkDir.trim().equalsIgnoreCase("temporary")) {
            if (isLocalhost) {
                this.wpsWorkDirIsRWorkDir = true;
                result = rCon.eval("setwd(\"" + this.currentWPSWorkDir.replace("\\", "/") + "\")");
            }
            else {
                result = rCon.eval("setwd(\"tempdir()\")");
            }
        }
        else {

            String path = null;
            boolean isInvalidPath = false;
            if (isLocalhost) {
                try {
                    path = rconf.getConfigVariableFullPath(RWPSConfigVariables.R_WORK_DIR);
                    String randomFolderName = "wps4r-r-workdir-" + UUID.randomUUID().toString().substring(0, 8);
                    if (new File(path).isDirectory()) {
                        path = (path + "/" + randomFolderName).replace("\\", "/");
                        new File(path).mkdir();
                        result = rCon.eval("setwd(\"" + path + "\")");
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
                boolean isExistingDir = rCon.eval("isTRUE(file.info(\"" + config_RWorkDir + "\")$isdir)").asInteger() == 1;
                if (isExistingDir) {
                    String randomFolderName = "wps4r-r-workdir-" + UUID.randomUUID().toString().substring(0, 8);
                    path = (path + "/" + randomFolderName).replace("\\", "/");
                    result = rCon.eval("setwd(\"" + path + "\")");
                }
                else {
                    isInvalidPath = true;
                }
            }

            if (isInvalidPath) {
                log.warn("[WPS4R] Invalid configurarion for variable \"" + RWPSConfigVariables.R_WORK_DIR + "\" | "
                        + config_RWorkDir + ". Variable is switched temporary to default.");
                rconf.setConfigVariable(RWPSConfigVariables.R_WORK_DIR, "default");
                setRWorkingDirectoryBeforeProcessing(rCon);
            }
        }

        log.debug("[R] Old wd: " + result.asString() + " | New wd: " + rCon.eval("getwd()").asString());
        RLogger.logGenericRProcess(rCon, "working directory: " + rCon.eval("getwd()").asString());
    }

    /**
     * @param config
     * @param rCon
     * @param r_basedir
     * @throws RserveException
     * @throws REXPMismatchException
     */
    private void cleanUpRSession(RConnection rCon, String r_basedir) throws RserveException, REXPMismatchException {
        R_Config config = R_Config.getInstance();
        RConnection connection = rCon;
        if (rCon == null || !rCon.isConnected()) {
            log.debug("[R] opening new connection for cleanup...");
            connection = config.openRConnection();
        }

        log.debug("[R] cleaning up workspace.");
        connection.eval("rm(list = ls())");

        if (this.wpsWorkDirIsRWorkDir) { // <- R won't delete the folder if it is the same as the wps work
                                         // directory
            log.debug("[R] closing stream.");
            connection.close();
            return;
        }

        // deletes R work directory:
        if (r_basedir != null) {
            String currentwd = connection.eval("getwd()").asString();

            log.debug("[R] setwd to " + r_basedir + " (was: " + currentwd + ")");

            // the next lines throws and exception, because r_basedir might not succesfully have been
            // set, so check first
            connection.eval("setwd(\"" + r_basedir + "\")");
            // should be true usually, if not, workdirectory has been
            // changed unexpectedly (prob. inside script)
            if (currentwd != r_basedir) {
                log.debug("[R] unlinking (recursive) " + currentwd);
                connection.eval("unlink(\"" + currentwd + "\", recursive=TRUE)");
            }
            else
                log.warn("Unexpected R workdirectory at end of R session, check the R sript for unwanted workdirectory changes");
        }

        log.debug("[R] closing stream.");
        connection.close();
    }

    private void assignRWPSSessionVariables(RConnection rCon) throws RserveException, RAnnotationException {
        R_Config config = R_Config.getInstance();

        // assign link to resource folder to an R variable
        String cmd = RWPSSessionVariables.WPS_SERVER_NAME + " <- TRUE";
        rCon.eval(cmd);
        log.debug("[R] " + cmd);

        rCon.assign(RWPSSessionVariables.RESOURCE_URL_NAME, config.getResourceDirURL());
        // should have the same result as rCon.eval(resourceUrl <- "lala");
        log.debug("[R] assigned resource directory to variable \"" + RWPSSessionVariables.RESOURCE_URL_NAME + ":\" "
                + config.getResourceDirURL());

        List<RAnnotation> resAnnotList = RAnnotation.filterAnnotations(this.annotations, RAnnotationType.RESOURCE);
        String wpsScriptResources = null;
        if (resAnnotList.size() == 1) // FIXME does this work for several resources?
            wpsScriptResources = resAnnotList.get(0).getStringValue(RAttribute.NAMED_LIST);
        else
            wpsScriptResources = "list()";
        // evaluations of commands given by strings require "eval"-method
        rCon.eval(RWPSSessionVariables.R_SESSION_SCRIPT_RESOURCES + " = " + wpsScriptResources);

        log.debug("[R] assigned recource urls to variable \"" + RWPSSessionVariables.R_SESSION_SCRIPT_RESOURCES
                + ":\" " + wpsScriptResources);

        String processDescription = R_Config.getInstance().getUrlPathUpToWebapp()
                + "/WebProcessingService?Request=DescribeProcess&identifier=" + this.getWellKnownName();

        rCon.assign(RWPSSessionVariables.PROCESS_DESCRIPTION, processDescription);
        log.debug("[R] assigned process description to variable \"" + RWPSSessionVariables.PROCESS_DESCRIPTION + ":\" "
                + processDescription);
    }

    /**
     * @param rCon
     * @throws RserveException
     * @throws IOException
     * @throws FileNotFoundException
     * @throws RAnnotationException
     */
    private void loadRUtilityScripts(RConnection rCon) throws RserveException,
            IOException,
            FileNotFoundException,
            RAnnotationException {
        log.debug("[R] loading utility scripts.");
        R_Config config = R_Config.getInstance();
        File[] utils = new File(config.utilsDirFull).listFiles(new R_Config.RFileExtensionFilter());
        for (File file : utils) {
            executeScript(new FileInputStream(file), rCon);
        }
    }

    /**
     * parses iData values to string representations which can be evaluated by Rserve, complex data will be
     * preprocessed and handled here, uses parseLiteralInput for parsing literal Data
     * 
     * @param input
     *        input value as databinding
     * @param Rconnection
     *        (open)
     * @return String which could be evaluated by RConnection.eval(String)
     * @throws IOException
     * @throws RserveException
     * @throws REXPMismatchException
     */
    private String parseInput(List<IData> input, RConnection rCon) throws IOException,
            RserveException,
            REXPMismatchException {

        String result = null;
        // building an R - vector of input entries containing more than one
        // value:
        if (input.size() > 1) {
            result = "c(";
            // parsing elements 1..n-1 to vector:
            for (int i = 0; i < input.size() - 1; i++) {
                if (input.get(i).equals(null))
                    continue;
                result += parseInput(input.subList(i, i + 1), rCon);
                result += ", ";
            }
            // parsing last element separately to vecor:
            result += parseInput(input.subList(input.size() - 1, input.size()), rCon);
            result += ")";
        }

        IData ivalue = input.get(0);
        Class< ? extends IData> iclass = ivalue.getClass();
        if (ivalue instanceof ILiteralData)
            return parseLiteralInput(iclass, ivalue.getPayload());

        if (ivalue instanceof GenericFileDataBinding) {
            GenericFileData value = (GenericFileData) ivalue.getPayload();

            InputStream is = value.getDataStream();
            String ext = value.getFileExtension();
            result = streamFromWPSToRserve(rCon, is, ext);
            is.close();

            return result;
        }

        if (ivalue instanceof GTRasterDataBinding) {
            GeotiffGenerator tiffGen = new GeotiffGenerator();
            InputStream is = tiffGen.generateStream(ivalue, GenericFileDataConstants.MIME_TYPE_GEOTIFF, "base64");
            // String ext = value.getFileExtension();
            result = streamFromWPSToRserve(rCon, is, "tiff");
            is.close();

            return result;
        }

        if (ivalue instanceof GTVectorDataBinding) {
            GTVectorDataBinding value = (GTVectorDataBinding) ivalue;
            File shp = value.getPayloadAsShpFile();

            String path = shp.getAbsolutePath();
            String baseName = path.substring(0, path.length() - ".shp".length());
            File shx = new File(baseName + ".shx");
            File dbf = new File(baseName + ".dbf");
            File prj = new File(baseName + ".prj");

            File shpZip = IOUtils.zip(shp, shx, dbf, prj);

            InputStream is = new FileInputStream(shpZip);
            String ext = "shp";
            result = streamFromWPSToRserve(rCon, is, ext);

            is.close();

            return result;
        }

        // if nothing was supported:
        String message = "An unsuported IData Class occured for input: " + input.get(0).getClass();
        log.error(message);
        throw new RuntimeException(message);
    }

    /**
     * Streams a file from WPS to Rserve workdirectory
     * 
     * @param rCon
     *        active RConnecion
     * @param is
     *        inputstream of the inputfile
     * @param ext
     *        basefile extension
     * @return
     * @throws IOException
     * @throws REXPMismatchException
     * @throws RserveException
     */
    private String streamFromWPSToRserve(RConnection rCon, InputStream is, String ext) throws IOException,
            REXPMismatchException,
            RserveException {
        String result;
        String randomname = UUID.randomUUID().toString();
        String inputFileName = randomname;

        RFileOutputStream rfos = rCon.createFile(inputFileName);

        byte[] buffer = new byte[2048];
        int stop = is.read(buffer);

        while (stop != -1) {
            rfos.write(buffer, 0, stop);
            stop = is.read(buffer);
        }
        rfos.flush();
        rfos.close();
        is.close();
        // R unzips archive files and renames files with unique
        // random names
        // TODO: check whether input is a zip archive or not
        result = rCon.eval("unzipRename(" + "\"" + inputFileName + "\", " + "\"" + randomname + "\", " + "\"" + ext
                + "\")").asString();
        result = "\"" + result + "\"";
        return result;
    }

    private void streamFromWPSToRserve(RConnection rCon, File source) throws IOException {
        RFileOutputStream rfos = rCon.createFile(source.getName());

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

    @SuppressWarnings("unchecked")
    private String parseLiteralInput(Class< ? extends IData> iClass, Object value) {
        String result = null;

        List<Class< ? extends ILiteralData>> easyLiterals = Arrays.asList(LiteralByteBinding.class,
                                                                          LiteralDoubleBinding.class,
                                                                          LiteralFloatBinding.class,
                                                                          LiteralIntBinding.class,
                                                                          LiteralLongBinding.class,
                                                                          LiteralShortBinding.class);

        if (easyLiterals.contains(iClass)) {
            result = "" + value;
        }
        else if (iClass.equals(LiteralBooleanBinding.class)) {
            if ((Boolean) value)
                result = "TRUE";
            else
                result = "FALSE";
        }
        else {
            if ( !iClass.equals(LiteralStringBinding.class)) {
                String message = "An unsuported IData class occured for input: " + iClass
                        + "it will be interpreted as character value within R";
                log.warn(message);
            }

            result = "\"" + value + "\"";
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private IData parseOutput(String result_id, REXP result, RConnection rCon) throws IOException,
            REXPMismatchException,
            RserveException,
            RAnnotationException {
        log.debug("parsing Output with id " + result_id + " from result " + result_id + " based on connection " + rCon);

        Class< ? extends IData> iClass = getOutputDataType(result_id);
        log.debug("Output data type: " + iClass.toString());

        if (iClass.equals(GenericFileDataBinding.class)) {
            if (log.isDebugEnabled())
                log.debug("Creating output with GenericFileDataBinding");
            String mimeType = "application/unknown";

            // extract filename from R
            String resultString = result.asString();
            File resultFile = new File(resultString);

            log.debug("Loading file " + resultFile.getAbsolutePath());

            if ( !resultFile.isAbsolute())
                // relative path names are alway relative to R work directory
                resultFile = new File(rCon.eval("getwd()").asString(), resultFile.getName());

            // Transfer file from R workdir to WPS workdir
            File outputFile = null;
            if ( !this.wpsWorkDirIsRWorkDir) {
                outputFile = streamFromRserveToWPS(rCon, resultFile.getAbsolutePath());
            }
            else {
                outputFile = resultFile;
            }

            if ( !outputFile.exists())
                throw new IOException("Output file does not exists: " + resultFile.getAbsolutePath());

            // extract mimetype from annotations (TODO: might have to be
            // simplified somewhen)
            List<RAnnotation> list = RAnnotation.filterAnnotations(this.annotations,
                                                                   RAnnotationType.OUTPUT,
                                                                   RAttribute.IDENTIFIER,
                                                                   result_id);

            RAnnotation anot = list.get(0);
            String rType = anot.getStringValue(RAttribute.TYPE);
            mimeType = RDataTypeRegistry.getInstance().getType(rType).getProcessKey();
            GenericFileData out = new GenericFileData(outputFile, mimeType);

            return new GenericFileDataBinding(out);
        }
        else if (iClass.equals(GTVectorDataBinding.class)) {

            String mimeType = "application/unknown";

            // extract filename from R
            String filename = new File(result.asString()).getName();

            RAnnotation out = RAnnotation.filterAnnotations(this.annotations, RAnnotationType.OUTPUT).get(0);
            RTypeDefinition dataType = out.getRDataType();
            File outputFile;

            if (dataType.equals(RDataType.SHAPE) || dataType.equals(RDataType.SHAPE_ZIP2) && !this.wpsWorkDirIsRWorkDir) {
                loadRUtilityScripts(rCon);
                String zip = "";
                REXP ev = rCon.eval("zipShp(\"" + filename + "\")");

                // filname = baseName (+ suffix)
                String baseName = null;

                if (filename.endsWith(".shp"))
                    baseName = filename.substring(0, filename.length() - ".shp".length());
                else
                    baseName = filename;

                // zip all -- stream --> unzip all or stream each file?
                if ( !ev.isNull()) {
                    zip = ev.asString();
                    File zipfile = streamFromRserveToWPS(rCon, zip);
                    outputFile = IOUtils.unzip(zipfile, "shp").get(0);
                }
                else {
                    log.info("R call to zip() does not work, streaming of shapefile without zipping");
                    String[] dir = rCon.eval("dir()").asStrings();
                    for (String f : dir) {
                        if (f.startsWith(baseName) && !f.equals(filename))
                            streamFromRserveToWPS(rCon, f);
                    }

                    outputFile = streamFromRserveToWPS(rCon, filename);
                }
            }
            else {
                if ( !this.wpsWorkDirIsRWorkDir) {
                    outputFile = streamFromRserveToWPS(rCon, filename);
                }
                else {
                    outputFile = new File(filename);
                    if ( !outputFile.isAbsolute())
                        // relative path names are alway relative to R work directory
                        outputFile = new File(rCon.eval("getwd()").asString(), outputFile.getName());

                }

                // outputFile = streamFromRserveToWPS(rCon, filename);
            }
            // extract mimetype from annotations (TODO: might have to be
            // simplified somewhen)
            List<RAnnotation> list = RAnnotation.filterAnnotations(this.annotations,
                                                                   RAnnotationType.OUTPUT,
                                                                   RAttribute.IDENTIFIER,
                                                                   result_id);

            RAnnotation anot = list.get(0);
            String rType = anot.getStringValue(RAttribute.TYPE);
            mimeType = RDataTypeRegistry.getInstance().getType(rType).getProcessKey();

            GenericFileData gfd = new GenericFileData(outputFile, mimeType);
            GTVectorDataBinding gtvec = gfd.getAsGTVectorDataBinding();
            return gtvec;
        }
        else if (iClass.equals(GTRasterDataBinding.class)) {
            String mimeType = "application/unknown";

            // extract filename from R
            String filename = new File(result.asString()).getName();
            File tempfile = streamFromRserveToWPS(rCon, filename);

            // extract mimetype from annotations (TODO: might have to be
            // simplified somewhen)
            List<RAnnotation> list = RAnnotation.filterAnnotations(this.annotations,
                                                                   RAnnotationType.OUTPUT,
                                                                   RAttribute.IDENTIFIER,
                                                                   result_id);

            RAnnotation anot = list.get(0);
            String rType = anot.getStringValue(RAttribute.TYPE);
            mimeType = RDataTypeRegistry.getInstance().getType(rType).getProcessKey();

            GeotiffParser tiffPar = new GeotiffParser();
            FileInputStream fis = new FileInputStream(tempfile);
            GTRasterDataBinding output = tiffPar.parse(fis, mimeType, "base64");
            fis.close();

            return output;
        }
        else if (iClass.equals(LiteralBooleanBinding.class)) {
            if (log.isDebugEnabled())
                log.debug("Creating output with LiteralBooleanBinding");

            int tresult = result.asInteger();
            switch (tresult) {
            case 1:
                return new LiteralBooleanBinding(true);
            case 0:
                return new LiteralBooleanBinding(false);
            default:
                break;
            }
        }

        Class[] easyLiterals = new Class[] {LiteralByteBinding.class,
                                            LiteralDoubleBinding.class,
                                            LiteralFloatBinding.class,
                                            LiteralIntBinding.class,
                                            LiteralLongBinding.class,
                                            LiteralShortBinding.class,
                                            LiteralStringBinding.class};

        // TODO: Might be a risky solution in terms of unknown constructors:
        for (Class< ? > literal : easyLiterals) {
            if (iClass.equals(literal)) {
                Constructor<IData> cons = null;
                try {
                    cons = (Constructor<IData>) iClass.getConstructors()[0];
                    Constructor< ? > param = cons.getParameterTypes()[0].getConstructor(String.class);
                    return cons.newInstance(param.newInstance(result.asString()));

                }
                catch (Exception e) {
                    String message = "Error for parsing String to IData for " + result_id + " and class " + iClass
                            + "\n" + e.getMessage();
                    log.error(message, e);
                    throw new RuntimeException(message);
                }
            }
        }

        String message = "R_Proccess: Unsuported Output Data Class declared for id " + result_id + ":" + iClass;
        log.error(message);

        throw new RuntimeException(message);
    }

    /**
     * Streams a File from R workdirectory to a temporal file in the WPS4R workdirectory
     * (R.Config.WORK_DIR/random folder)
     * 
     * @param rCon
     *        an open R connection
     * @param filename
     *        name or path of the file located in the R workdirectory
     * @return Location of a file which has been streamed
     * @throws IOException
     * @throws FileNotFoundException
     */
    private File streamFromRserveToWPS(RConnection rCon, String filename) throws IOException, FileNotFoundException {
        File tempfile = new File(filename);
        File destination = new File(this.currentWPSWorkDir);
        if ( !destination.exists())
            destination.mkdirs();
        tempfile = new File(destination, tempfile.getName());

        // Do streaming Rserve --> WPS tempfile
        RFileInputStream fis = rCon.openFile(filename);
        FileOutputStream fos = new FileOutputStream(tempfile);
        byte[] buffer = new byte[2048];
        int stop = fis.read(buffer);
        while (stop != -1) {
            fos.write(buffer, 0, stop);
            stop = fis.read(buffer);
        }
        fis.close();
        fos.close();
        // tempfile.deleteOnExit();
        return tempfile;
    }

    /**
     * 
     * @param script
     *        R input script
     * @param rCon
     *        Connection - should be open usually / otherwise it will be opened and closed separately
     * @return true if read was successful
     * @throws RserveException
     * @throws IOException
     * @throws RAnnotationException
     * @throws RuntimeException
     *         if R reports an error
     */
    private boolean executeScript(InputStream script, RConnection rCon) throws RserveException,
            IOException,
            RAnnotationException {
        log.debug("Executing script...");
        boolean success = true;

        BufferedReader fr = new BufferedReader(new InputStreamReader(script));
        if ( !fr.ready())
            return false;

        // reading script:
        StringBuilder text = new StringBuilder();
        // surrounds R script with try / catch block in R and an initial digit
        // setting
        text.append("error = try({" + '\n' + "options(digits=12)" + '\n');

        // is set true when wps.off-annotations occur
        // this indicates that parts of the script shall not pass to Rserve
        boolean wpsoff_state = false;

        while (fr.ready()) {
            String line = fr.readLine();

            if (line.contains("setwd(")) {
                log.warn("The running R script contains a call to \"setwd(...)\". "
                        + "This may cause runtime-errors and unexpected behaviour of WPS4R. "
                        + "It is strongly advise to not use this function in process scripts.");
            }

            if (line.contains(RegExp.WPS_OFF) && line.contains(RegExp.WPS_ON))
                // TODO: check in validation
                throw new RAnnotationException("Invalid R-script: Only one wps.on; / wps.off; expression per line!");

            if (line.contains(RegExp.WPS_OFF)) {
                wpsoff_state = true;
            }
            else if (line.contains(RegExp.WPS_ON)) {
                wpsoff_state = false;
            }
            else if (wpsoff_state)
                line = "# (ignored by " + RegExp.WPS_OFF + ") " + line;

            text.append(line + "\n");
        }
        text.append("})" + '\n' + "hasError = class(error) == \"try-error\" " + '\n'
                + "if(hasError) error_message = as.character(error)" + '\n');

        if (this.debugScript && log.isDebugEnabled())
            log.debug(text.toString());

        // call the actual script here
        rCon.eval(text.toString());

        try {
            // handling internal R errors:
            if (rCon.eval("hasError").asInteger() == 1) {
                String message = "An R-error occured while executing R-script: \n"
                        + rCon.eval("error_message").asString();
                log.error(message);
                success = false;
                throw new RuntimeException(message);
            }

            // retrieving error from Rserve
        }
        catch (REXPMismatchException e) {
            log.warn("Error handling during R-script execution failed: " + e.getMessage());
            success = false;
        }

        return success;
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

    /**
     * Searches annotations (class attribute) for Inputs / Outputs with a specific referring id
     * 
     * @param ioType
     * @param id
     * @return
     * @throws RAnnotationException
     */
    private Class< ? extends IData> getIODataType(RAnnotationType ioType, String id) throws RAnnotationException {
        Class< ? extends IData> dataType = null;
        List<RAnnotation> ioNotations = RAnnotation.filterAnnotations(this.annotations,
                                                                      ioType,
                                                                      RAttribute.IDENTIFIER,
                                                                      id);
        if (ioNotations.isEmpty()) {
            log.error("Missing R-script-annotation of type " + ioType.toString().toLowerCase() + " for id \"" + id
                    + "\" ,datatype - class not found");
            return null;
        }
        if (ioNotations.size() > 1) {
            log.warn("R-script contains more than one annotation of type " + ioType.toString().toLowerCase()
                    + " for id \"" + id + "\n" + " WPS selects the first one.");
        }

        RAnnotation annotation = ioNotations.get(0);
        String rClass = annotation.getStringValue(RAttribute.TYPE);
        dataType = RAnnotation.getDataClass(rClass);

        if (dataType == null) {
            log.error("R-script-annotation for " + ioType.toString().toLowerCase() + " id \"" + id
                    + "\" contains unsuported data format identifier \"" + rClass + "\"");
        }
        return dataType;
    }

    public Class< ? extends IData> getInputDataType(String id) {
        try {
            return getIODataType(RAnnotationType.INPUT, id);
        }
        catch (RAnnotationException e) {
            String message = "Data type for id " + id + " could not be retrieved, return null";
            log.error(message, e);
        }
        return null;
    };

    public Class< ? extends IData> getOutputDataType(String id) {
    	if(id.equalsIgnoreCase("sessionInfo") || id.equalsIgnoreCase("warnings")) 
    		return GenericFileDataBinding.class;
    	
        try {
            return getIODataType(RAnnotationType.OUTPUT, id);
        }
        catch (RAnnotationException e) {
            String message = "Data type for id " + id + " could not be retrieved, return null";
            log.error(message, e);
        }
        return null;
    };

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("GenericRProcess [script = ");
        sb.append(this.scriptFile);
        if (this.annotations != null) {
            sb.append(", annotations = ");
            sb.append(Arrays.toString(this.annotations.toArray()));
        }
        sb.append("]");
        return sb.toString();
    }

}
