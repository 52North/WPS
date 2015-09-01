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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.ITransactionalAlgorithmRepository;
import org.n52.wps.server.ProcessDescription;
import org.n52.wps.server.r.data.RDataTypeRegistry;
import org.n52.wps.server.r.info.RProcessInfo;
import org.n52.wps.server.r.metadata.RAnnotationParser;
import org.n52.wps.server.r.syntax.RAnnotation;
import org.n52.wps.server.r.syntax.RAnnotationException;
import org.n52.wps.server.r.syntax.RAnnotationType;
import org.n52.wps.webapp.api.AlgorithmEntry;
import org.n52.wps.webapp.api.ConfigurationCategory;
import org.n52.wps.webapp.api.ConfigurationModule;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A repository to retrieve the available algorithms.
 * 
 * @author Matthias Hinz, Daniel Nüst
 * 
 */
@Component(LocalRAlgorithmRepository.COMPONENT_NAME)
public class LocalRAlgorithmRepository implements ITransactionalAlgorithmRepository {

    private static Logger LOGGER = LoggerFactory.getLogger(LocalRAlgorithmRepository.class);

    public static final String COMPONENT_NAME = "RAlgorithmRepository";

    private static final String DESCRPTION_VERSION_FOR_VALIDATION = WPSConfig.VERSION_100;

    private Map<String, GenericRProcess> algorithms = new HashMap<String, GenericRProcess>();

    /*
     * if set to true an error during one algorithm load stops subsequent algorithms to be loaded
     */
    private boolean exceptionOnAlgorithmLoad = false;

    @Autowired
    private R_Config config;

    @Autowired
    private ScriptFileRepository scriptRepo;

    @Autowired
    private RAnnotationParser parser;

    @Autowired
    private ResourceFileRepository resourceRepo;

    private Map<String, RProcessInfo> processInfos = new HashMap<String, RProcessInfo>();

    private boolean addInvalidScripts = true;

    @Autowired
    private RDataTypeRegistry dataTypeRegistry;

    private ConfigurationModule configModule;

    public LocalRAlgorithmRepository() {
        LOGGER.info("NEW {}", this);
    }

    @PostConstruct
    public void init() {
        LOGGER.info("Initializing Local*R*AlgorithmRepository..");

        configModule = WPSConfig.getInstance()
				.getConfigurationModuleForClass(this.getClass().getName(),
						ConfigurationCategory.REPOSITORY);

        if (configModule != null && configModule.isActive()) {
            boolean rServeIsAvailable = checkRServe();
            LOGGER.debug("R serve is available: {}", rServeIsAvailable);

            if (rServeIsAvailable) {
                Collection<Path> resourceDirectory = config.getResourceDirectory();
                for (Path rd : resourceDirectory) {
                    resourceRepo.addResourceDirectory(rd);
                }

                // changeManager.updateRepositoryConfiguration(); // updates the config file
                addAllAlgorithmsToRepository(); // add the algorithms based on the config file

                LOGGER.info("Initialized Local*R*AlgorithmRepository");
            }
            else
                LOGGER.error("RServe is not available, not adding ANY algorithms!");
        }
        else {
            LOGGER.info("Local*R*AlgorithmRepository is INACTIVE.");
        }
    }

    /**
     * Check if repository is active, which requires a check if the repo is wrapped.
     */
    // private boolean checkActivation() {
    // LOGGER.debug("Checking activation state to verify startup conditions.");
    // // check if the repository is active: ignored when wrapper is present (DNU)
    // String className = this.getClass().getCanonicalName();
    // WPSConfig wpsConfig = WPSConfig.getInstance();
    // if ( !wpsConfig.isRepositoryActive(className)) {
    // LOGGER.debug("Local R Algorithm Repository is inactive.");
    // Repository[] registeredAlgorithmRepositories =
    // WPSConfig.getInstance().getRegisterdAlgorithmRepositories();
    // boolean isWrapped =
    // AbstractWrapperAlgorithmRepository.wrapperRepositoryActiveAndConfiguredForRepo(this,
    // registeredAlgorithmRepositories);
    // if (isWrapped)
    // LOGGER.debug("Ignoring 'inactive' configuration value because a wrapper repo is active!");
    // else {
    // LOGGER.warn("Local R Algorithm Repository is inactive and no wrapper is defined - if you do not want to use R then you can safely ignore this.");
    // return false;
    // }
    // }
    //
    // return true;
    // }

    /**
     * Check if Rserve can be found
     */
    private boolean checkRServe() {
        LOGGER.debug("Trying to connect to Rserve to verify startup conditions.");
        try {
            RConnection testcon = config.openRConnection();
            LOGGER.info("WPS successfully connected to Rserve.");
            testcon.close();
        }
        catch (RserveException e) {
            LOGGER.error("[Rserve] Could not connect to Rserve. Rserve may not be available or may not be ready at the current time.",
                         e);
            return false;
        }

        return true;
    }

    private void addAllAlgorithmsToRepository() {
        List<AlgorithmEntry> algorithmEntries = configModule.getAlgorithmEntries();

        LOGGER.debug("Adding algorithms for properties: {}", Arrays.deepToString(algorithmEntries.toArray()));

        for (AlgorithmEntry entry : algorithmEntries) {
            LOGGER.debug("Adding algorithm: {}", entry);

            if ( !entry.isActive()) {
                LOGGER.warn("Algorithm not added: ", entry);
                continue;
            }

            addAlgorithm(entry.getAlgorithm());
        }
    }

    private void addResourcesForAlgorithm(String algorithm_wkn) throws ExceptionReport {
        LOGGER.debug("Adding resources for algorithm {}", algorithm_wkn);
        GenericRProcess process = algorithms.get(algorithm_wkn);

        // resources
        List<RAnnotation> resourceAnnotations = null;
        try {
            resourceAnnotations = RAnnotation.filterAnnotations(process.getAnnotations(), RAnnotationType.RESOURCE);
        }
        catch (RAnnotationException e) {
            LOGGER.error("Could not get resoure annotations for algorithm  {}", algorithm_wkn);
        }

        for (RAnnotation rAnnotation : resourceAnnotations) {
            boolean b = resourceRepo.registerResources(rAnnotation);
            if (b)
                LOGGER.debug("Registered resources for algorithm {} based on annotation: {}",
                             algorithm_wkn,
                             rAnnotation);
            else
                LOGGER.warn("Could not register resources based on annotation {}", rAnnotation);
        }

        // imports
        try {
            resourceAnnotations = RAnnotation.filterAnnotations(process.getAnnotations(), RAnnotationType.IMPORT);
        }
        catch (RAnnotationException e) {
            LOGGER.error("Could not get import annotations for algorithm  {}", algorithm_wkn);
        }

        Path scriptParent = scriptRepo.getScriptFileForWKN(algorithm_wkn).toPath().getParent();

        for (RAnnotation rAnnotation : resourceAnnotations) {
            boolean b = resourceRepo.registerImport(rAnnotation, scriptParent);
            if (b)
                LOGGER.debug("Registered import as resource for algorithm {} based on annotation: {}",
                             algorithm_wkn,
                             rAnnotation);
            else
                LOGGER.warn("Could not register resources based on annotation {}", rAnnotation);
        }

    }

    @Override
    public IAlgorithm getAlgorithm(String algorithmName) {
        if ( !this.config.getCacheProcesses()) {
            LOGGER.debug("Process cache disabled, creating new process for id '{}'", algorithmName);
            boolean b = addAlgorithm(algorithmName);
            if ( !b)
                LOGGER.warn("Problem adding algorithm for deactivated cache.");
        }

        if ( !this.algorithms.containsKey(algorithmName))
            throw new RuntimeException("This repository does not contain an algorithm '" + algorithmName + "'");

        return this.algorithms.get(algorithmName);
    }

    @Override
    public Collection<String> getAlgorithmNames() {
        return new ArrayList<>(this.algorithms.keySet());
    }

    @Override
    public boolean containsAlgorithm(String processID) {
        return this.algorithms.containsKey(processID);
    }

    private GenericRProcess loadAlgorithmAndValidate(String wellKnownName) {
        LOGGER.debug("Loading algorithm '{}'", wellKnownName);

        LOGGER.trace("Loading file for '{}'", wellKnownName);
        File f = null;
        try {
            f = scriptRepo.getScriptFileForWKN(wellKnownName);
        }
        catch (ExceptionReport e) {
            LOGGER.error("Could not load file for algorithm {}", wellKnownName, e);
            return null; // FIXME
        }

        RProcessInfo processInfo = new RProcessInfo(wellKnownName, f, parser);
        processInfos.put(wellKnownName, processInfo);
        LOGGER.trace("Added internal info: '{}'", processInfo);

        if ( !scriptRepo.isScriptAvailable(processInfo)) {
            LOGGER.error("Missing R script for process '{}'. Process ignored - check WPS configuration.", wellKnownName);
            throw new RuntimeException("Could not load algorithm " + wellKnownName + ". Missing R script.");
        }

        if ( !processInfo.isValid()) {
            if (addInvalidScripts)
                LOGGER.warn("Invalid R script for process '{}'. Process still trying to be added, check admin interface.",
                            wellKnownName);
            else
                throw new RuntimeException("Could not load algorithm " + wellKnownName + ". Invalid R script.");
        }

        GenericRProcess algorithm = new GenericRProcess(wellKnownName,
                                                        config,
                                                        parser,
                                                        scriptRepo,
                                                        resourceRepo,
                                                        dataTypeRegistry);


        if ( !algorithm.processDescriptionIsValid(DESCRPTION_VERSION_FOR_VALIDATION)) {
            // collect the errors
            ProcessDescriptionType description = (ProcessDescriptionType) algorithm.getDescription().getProcessDescriptionType(DESCRPTION_VERSION_FOR_VALIDATION);
            XmlOptions validateOptions = new XmlOptions();
            ArrayList<XmlError> errorList = new ArrayList<XmlError>();
            validateOptions.setErrorListener(errorList);
            // run validation again
            description.validate(validateOptions);
            StringBuilder validationMessages = new StringBuilder();
            validationMessages.append("\n");

            for (XmlError e : errorList) {
                validationMessages.append("[");
                validationMessages.append(e.getLine());
                validationMessages.append(" | ");
                validationMessages.append(e.getErrorCode());
                validationMessages.append("] ");
                validationMessages.append(e.getMessage());
                validationMessages.append("\n");
            }
            LOGGER.warn("Algorithm description is not valid {}. Errors: {}",
                        wellKnownName,
                        validationMessages.toString());

            throw new RuntimeException("Could not load algorithm " + wellKnownName + ". ProcessDescription not valid: "
                    + validationMessages.toString());
        }

        return algorithm;
    }

    @Override
    public boolean addAlgorithm(Object processID) {
        if (processID instanceof String) {
            String algorithmName = (String) processID;

            try {
                GenericRProcess p = loadAlgorithmAndValidate(algorithmName);
                this.algorithms.put(algorithmName, p);
                LOGGER.info("ADDED algorithm under name '{}': {}", algorithmName, p);

                addResourcesForAlgorithm(algorithmName);

                return true;
            }
            catch (RuntimeException | ExceptionReport e) {
                String message = "Could not load algorithm for class name '" + algorithmName + "'";
                LOGGER.error(message, e);
                if (exceptionOnAlgorithmLoad)
                    throw new RuntimeException(message + ": " + e.getMessage(), e);
            }
        }

        LOGGER.error("Unsupported process id {} of class {}", processID, processID.getClass());
        return false;
    }

    @Override
    public boolean removeAlgorithm(Object processID) {
        if ( ! (processID instanceof String)) {
            LOGGER.debug("Could not remove algorithm with processID {}", processID);
            return false;
        }

        String id = (String) processID;
        if (this.algorithms.containsKey(id))
            this.algorithms.remove(id);

        LOGGER.info("Removed algorithm: {}", id);
        return true;
    }

    @Override
    public ProcessDescription getProcessDescription(String processID) {
        return getAlgorithm(processID).getDescription();
    }

    public RProcessInfo getProcessInfo(String processID) {
        return this.processInfos.get(processID);
    }

    @Override
    public void shutdown() {
        LOGGER.info("Shutting down ...");
        this.algorithms.clear();
        this.processInfos.clear();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("LocalRAlgorithmRepository [");
        if (algorithms != null)
            builder.append("algorithm count=").append(algorithms.size()).append(", ");
        if (config != null)
            builder.append("config=").append(config).append(", ");
        // if (changeManager != null)
        // builder.append("changeManager=").append(changeManager).append(", ");
        // if (repo != null)
        // builder.append("repo=").append(repo).append(", ");
        // if (parser != null)
        // builder.append("parser=").append(parser).append(", ");
        // if (processInfos != null)
        // builder.append("processInfos=").append(processInfos).append(", ");
        builder.append("skipInvalidScripts=").append(addInvalidScripts).append("]");
        return builder.toString();
    }

}
