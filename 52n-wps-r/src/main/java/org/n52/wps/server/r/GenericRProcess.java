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
import java.io.FileInputStream;
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
import org.n52.wps.server.r.workspace.RWorkspaceManager;
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

    public GenericRProcess(String wellKnownName) {
        super(wellKnownName);

        log.info("NEW {}", this);
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

    /**
     * This method should be overwritten, in case you want to have a way of initializing.
     * 
     * In detail it looks for a xml descfile, which is located in the same directory as the implementing class
     * and has the same name as the class, but with the extension XML.
     * 
     * @return
     */
    protected ProcessDescriptionType initializeDescription() {
        log.info("Initializing description for {}", this.toString());

        this.config = R_Config.getInstance();

        // Reading process information from script annotations:
        InputStream rScriptStream = null;
        try {
            String wkn = getWellKnownName();
            log.debug("Loading file for {}", wkn);

            this.scriptFile = config.getScriptFileForWKN(wkn);
            log.debug("File loaded: {}", this.scriptFile.getAbsolutePath());

            if (this.scriptFile == null) {
                log.warn("Loaded script file is {}", this.scriptFile);
                throw new ExceptionReport("Cannot create process description because R script fill is null",
                                          ExceptionReport.NO_APPLICABLE_CODE);
            }

            rScriptStream = new FileInputStream(this.scriptFile);
            if (this.parser == null)
                this.parser = new RAnnotationParser(this.config); // prevents
            // NullpointerException
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
        log.info("Running {}", this.toString());
        log.debug("inputData: {}", Arrays.toString(inputData.entrySet().toArray()));

        FilteredRConnection rCon = null;
        try {
            rCon = config.openRConnection();
            RLogger.logGenericRProcess(rCon,
                                       "Running algorithm with input "
                                               + Arrays.deepToString(inputData.entrySet().toArray()));

            RWorkspaceManager manager = new RWorkspaceManager(rCon, this.iohandler, config);
            String originalWorkDir = manager.prepareWorkspace(inputData, getWellKnownName());

            List<RAnnotation> resAnnotList = RAnnotation.filterAnnotations(this.annotations, RAnnotationType.RESOURCE);
            manager.loadResources(resAnnotList);

            List<RAnnotation> inAnnotations = RAnnotation.filterAnnotations(this.annotations, RAnnotationType.INPUT);
            manager.loadInputValues(inputData, inAnnotations);

            if (log.isDebugEnabled())
                manager.saveImage("preExecution");

            File scriptFile = config.getScriptFileForWKN(getWellKnownName());

            HashMap<String, IData> result = null;
            boolean success = executor.executeScript(scriptFile, rCon);
            if (success) {
                List<RAnnotation> outAnnotations = RAnnotation.filterAnnotations(this.annotations,
                                                                                 RAnnotationType.OUTPUT);
                result = manager.saveOutputValues(outAnnotations);
            }
            else {
                String msg = "Failure while executing R script. See logs for details";
                log.error(msg);
                throw new ExceptionReport(msg, getClass().getName());
            }

            if (log.isDebugEnabled())
                manager.saveImage("afterExecution");
            log.debug("RESULT: " + Arrays.toString(result.entrySet().toArray()));

            manager.cleanUpWithWPS();
            manager.cleanUpInR(originalWorkDir);

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
            String message = "An R Parsing Error occoured:\n" + e.getMessage() + e.getClass() + " - "
                    + e.getLocalizedMessage() + "\n" + e.getCause();
            log.error(message, e);
            throw new ExceptionReport(message, "R", "R_Connection", e);
        }
        finally {
            if (rCon != null)
                rCon.close();
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

}