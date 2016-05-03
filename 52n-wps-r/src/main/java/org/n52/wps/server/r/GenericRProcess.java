/**
 * ﻿Copyright (C) 2010 - 2016 52°North Initiative for Geospatial Open Source
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.n52.wps.io.data.IData;
import org.n52.wps.server.AbstractObservableAlgorithm;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.r.metadata.RAnnotationParser;
import org.n52.wps.server.r.metadata.RProcessDescriptionCreator;
import org.n52.wps.server.r.syntax.RAnnotation;
import org.n52.wps.server.r.syntax.RAnnotationException;
import org.n52.wps.server.r.syntax.RAnnotationType;
import org.n52.wps.server.r.util.RExecutor;
import org.n52.wps.server.r.util.RLogger;
import org.n52.wps.server.r.workspace.RIOHandler;
import org.n52.wps.server.r.workspace.RSessionManager;
import org.n52.wps.server.r.workspace.RWorkspaceManager;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericRProcess extends AbstractObservableAlgorithm {

    private static Logger log = LoggerFactory.getLogger(GenericRProcess.class);

    // private variables holding process information - initialization in
    // constructor
    private List<RAnnotation> annotations;

    private R_Config config;

    private List<String> errors = new ArrayList<String>();

    private RExecutor executor = new RExecutor();

    private RIOHandler iohandler = new RIOHandler();

    private RAnnotationParser parser;

    private File scriptFile = null;

    private boolean shutdownRServerAfterRun = false;

    private Thread updateThread;
    
    private boolean stopUpdateThread = false;
    
    private long lastStatusUpdate = 0;
    
    public GenericRProcess(String wellKnownName) {
        super(wellKnownName);

        log.debug("NEW {}", this);
    }

    public List<String> getErrors() {
        return this.errors;
    }

    @Override
    public Class< ? extends IData> getInputDataType(String id) {
        return this.iohandler.getInputDataType(id, this.annotations);
    }

    @Override
    public Class< ? > getOutputDataType(String id) {
        return this.iohandler.getOutputDataType(id, this.annotations);
    }

    @Override
    protected ProcessDescriptionType initializeDescription() {
        this.config = R_Config.getInstance(); // call here because method is invoked by super constructor

        // Reading process information from script annotations:
        InputStream rScriptStream = null;
        try {
            String wkn = getWellKnownName();
            log.debug("Loading file for {}", wkn);

            this.scriptFile = config.getScriptFileForWKN(wkn);
            log.debug("File loaded: {}", this.scriptFile.getAbsolutePath());

            log.info("Initializing description for {}", this.toString());

            if (this.scriptFile == null) {
                log.warn("Loaded script file is {}", this.scriptFile);
                throw new ExceptionReport("Cannot create process description because R script fill is null",
                                          ExceptionReport.NO_APPLICABLE_CODE);
            }

            rScriptStream = new FileInputStream(this.scriptFile);
            if (this.parser == null)
                this.parser = new RAnnotationParser(this.config); // prevent NullpointerException
            this.annotations = this.parser.parseAnnotationsfromScript(rScriptStream);

            // submits annotation with process informations to
            // ProcessdescriptionCreator:
            RProcessDescriptionCreator creator = new RProcessDescriptionCreator(this.config);
            ProcessDescriptionType doc = creator.createDescribeProcessType(this.annotations,
                                                                           wkn,
                                                                           config.getScriptURL(wkn),
                                                                           config.getSessionInfoURL());

            log.debug("Created process description for {}:\n{}", wkn, doc.xmlText());
            return doc;
        }
        catch (RAnnotationException rae) {
            log.error(rae.getMessage());
            throw new RuntimeException("Annotation error while parsing process description: " + rae.getMessage(), rae);
        }
        catch (IOException ioe) {
            log.error("I/O error while parsing process description: " + ioe.getMessage());
            throw new RuntimeException("I/O error while parsing process description: " + ioe.getMessage(), ioe);
        }
        catch (ExceptionReport e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Error creating process description: " + e.getMessage(), e);
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
        log.info("Running {} \n\tInput data: {}", this.toString(), Arrays.toString(inputData.entrySet().toArray()));

        FilteredRConnection rCon = null;
        try {
            rCon = config.openRConnection();
            RLogger.logGenericRProcess(rCon,
                                       "Running algorithm with input "
                                               + Arrays.deepToString(inputData.entrySet().toArray()));

            RSessionManager session = new RSessionManager(rCon, config);
            session.configureSession(getWellKnownName(), executor);

            RWorkspaceManager workspace = new RWorkspaceManager(rCon, this.iohandler, config);
            String originalWorkDir = workspace.prepareWorkspace(inputData, getWellKnownName());

            List<RAnnotation> resAnnotList = RAnnotation.filterAnnotations(this.annotations, RAnnotationType.RESOURCE);
            workspace.loadResources(resAnnotList);

            List<RAnnotation> inAnnotations = RAnnotation.filterAnnotations(this.annotations, RAnnotationType.INPUT);
            workspace.loadInputValues(inputData, inAnnotations);

            if (log.isDebugEnabled())
                workspace.saveImage("preExecution");

            File scriptFile = config.getScriptFileForWKN(getWellKnownName());

            //add simple status logging functionality
            //log status via updateStatus-method to temp file that is created in GenericRProcess class
            //temp file is read by process if changed and the contents are to the update-method of the ISubject interface      
            
            String script = "tmpStatusFile <- tempfile()";
            rCon.voidEval(script);
            
            File tmpStatusFile;
            
            REXP tmpStatusFileREXP = rCon.eval("tmpStatusFile");
            
            if(tmpStatusFileREXP.isString()){
                    try {
                        tmpStatusFile = new File(tmpStatusFileREXP.asString());
                        
                        StringBuilder statusScriptString = new StringBuilder();
                        
                        statusScriptString.append("writelock = function() {\n  ");
                        statusScriptString.append("file.create(paste0(tmpStatusFile, \"" + R_Config.LOCK_SUFFIX + "\"))\n");
                        statusScriptString.append("}\n  ");
                        
                        statusScriptString.append("removelock = function(i) {\n  ");
                        statusScriptString.append("file.remove(paste0(tmpStatusFile, \"" + R_Config.LOCK_SUFFIX + "\"))\n");
                        statusScriptString.append("}\n  ");
                        
                        statusScriptString.append("updateStatus = function(i) {\n  ");
                        statusScriptString.append("writelock()\n  ");
                        statusScriptString.append("write(as.character(i),file=tmpStatusFile,append=F)\n");
                        statusScriptString.append("removelock()\n");
                        statusScriptString.append("}\n  ");
                        
                        rCon.voidEval(statusScriptString.toString());
                        
                        tmpStatusFile.createNewFile();
                        
                        startUpdateListener(tmpStatusFile);
                        
                    } catch (REXPMismatchException e) {
                        log.debug("Could not parse String generated by R method tempfile() to Java File. No status updates are possible.", e);
                    }
            } 
            
            HashMap<String, IData> result = null;
            boolean success = executor.executeScript(scriptFile, rCon);
            if (success) {
                List<RAnnotation> outAnnotations = RAnnotation.filterAnnotations(this.annotations,
                                                                                 RAnnotationType.OUTPUT);
                result = workspace.saveOutputValues(outAnnotations);
                result = session.saveInfos(result);
            }
            else {
                String msg = "Failure while executing R script. See logs for details";
                log.error(msg);
                throw new ExceptionReport(msg, getClass().getName());
            }

            if (log.isDebugEnabled())
                workspace.saveImage("afterExecution");
            log.debug("RESULT: " + Arrays.toString(result.entrySet().toArray()));

            session.cleanUp();
            workspace.cleanUpInR(originalWorkDir);
            workspace.cleanUpWithWPS();

            return result;
        }
        catch (IOException e) {
            String message = "Attempt to run R script file failed:\n" + e.getClass() + " - " + e.getLocalizedMessage()
                    + "\n" + e.getCause();
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
            throw new ExceptionReport("Rserve problem executing script: " + e.getMessage(),
                                      "R",
                                      ExceptionReport.REMOTE_COMPUTATION_ERROR,
                                      e);
        }
        catch (REXPMismatchException e) {
            String message = "An R Parsing Error occoured:\n" + e.getMessage() + " - " + e.getClass() + " - "
                    + e.getLocalizedMessage() + "\n" + e.getCause();
            log.error(message, e);
            throw new ExceptionReport(message, "R", "R_Connection", e);
        }
        finally {
            if (rCon != null) {
                if (shutdownRServerAfterRun) {
                    log.debug("Shutting down R completely...");
                    try {
                        rCon.serverShutdown();
                    }
                    catch (RserveException e) {
                        String message = "Error during R server shutdown:\n" + e.getMessage() + " - " + e.getClass()
                                + " - " + e.getLocalizedMessage() + "\n" + e.getCause();
                        log.error(message, e);
                        throw new ExceptionReport(message, "R", "R_Connection", e);
                    }
                }
                else
                    rCon.close();
            }
            
            if(updateThread != null && updateThread.isAlive()){
                stopUpdateThread = true;
            }
        }
    }

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

    private void startUpdateListener(final File tmpStatusFile){
        
        updateThread = new Thread("WPS4R-update-thread"){
          
            @Override
            public void run() {
                
                while(true){                    
                    if(stopUpdateThread){
                        break;
                    }
                    
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        log.error("InterruptedException while trying to sleep WPS4R-update-thread.", e);
                    }
                    
                    //lock exists continue
                    if(new File(tmpStatusFile.getAbsolutePath().concat(R_Config.LOCK_SUFFIX)).exists()){
                        continue;
                    }
                    
                    try {
                        String updateMessage = readTmpStatusFile(tmpStatusFile);
                        
                        if(updateMessage == null || updateMessage.isEmpty()){
                            continue;
                        }
                        
                        //try parsing status as integer and update process status if successful
                        try{
                            
                            Integer percentage = Integer.parseInt(updateMessage.trim());
                            
                            update(percentage);
                            
                        }catch(NumberFormatException e){
                            log.info("Status could not be parsed to integer: " + updateMessage);
                            
                            //update status with message (works only for WPS 1.0)
                            update(updateMessage);
                        }
                        
                    } catch (IOException e) {
                        log.error("Could not read status from file: " + tmpStatusFile.getAbsolutePath(), e);
                    }
                                        
                }
                
            }
            
        };
        
        updateThread.start();
    }
    
    private String readTmpStatusFile(File tmpStatusFile) throws IOException{
        
        String content = "";
        
        long statusFileModified = tmpStatusFile.lastModified();
               
        log.debug("File modified: " + (statusFileModified > lastStatusUpdate));
        
        if(lastStatusUpdate == 0 || statusFileModified > lastStatusUpdate){
            
            BufferedReader bufferedReader = new BufferedReader(new FileReader(tmpStatusFile));
            
            String line = "";
            
            while((line = bufferedReader.readLine()) != null){
                content = content.concat(line + "\n");
            }
            
            bufferedReader.close();
            
            lastStatusUpdate = statusFileModified;            
        }
        
        return content;
    }
    
}