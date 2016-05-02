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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.ITransactionalAlgorithmRepository;
import org.n52.wps.server.r.data.CustomDataTypeManager;
import org.n52.wps.server.r.info.RProcessInfo;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A static repository to retrieve the available algorithms.
 * 
 * @author Matthias Hinz
 * 
 */
public class LocalRAlgorithmRepository implements ITransactionalAlgorithmRepository {

    private static Logger LOGGER = LoggerFactory.getLogger(LocalRAlgorithmRepository.class);

    // registered processes
    private Map<String, IAlgorithm> algorithms;

    private R_Config rConfig;

    public LocalRAlgorithmRepository() {
        LOGGER.info("Initializing LocalRAlgorithmRepository");
        this.algorithms = new HashMap<String, IAlgorithm>();
        this.rConfig = R_Config.getInstance();

        // Check WPS Config properties:
        RPropertyChangeManager changeManager = RPropertyChangeManager.getInstance();
        // unregistered scripts from repository folder will be added as
        // Algorithm to WPSconfig
        changeManager.updateRepositoryConfiguration();

        CustomDataTypeManager.getInstance().update();
        checkStartUpConditions();

        // finally add all available algorithms from the R config
        addAllAlgorithms();
    }

    /**
     * Check if repository is active and Rserve can be found
     * 
     * @return
     */
    private boolean checkStartUpConditions() {
        // check if the repository is active:
        String className = this.getClass().getCanonicalName();
        if ( !WPSConfig.getInstance().isRepositoryActive(className)) {
            LOGGER.debug("Local R Algorithm Repository is inactive.");
            return false;
        }

        // Try to build up a connection to Rserve. If it is refused, a new instance of Rserve will be opened
        LOGGER.debug("Trying to connect to Rserve.");
        try {
            RConnection testcon = rConfig.openRConnection();
            LOGGER.info("WPS successfully connected to Rserve.");
            testcon.close();
        }
        catch (RserveException e) {
            // try to start Rserve via batchfile if enabled
            LOGGER.error("[Rserve] Could not connect to Rserve. Rserve may not be available or may not be ready at the current time.",
                         e);
            return false;
        }
        return true;
    }

    private void addAllAlgorithms() {
        // add algorithms from config file to repository
        List<RProcessInfo> processInfoList = new ArrayList<RProcessInfo>();
        Property[] propertyArray = WPSConfig.getInstance().getPropertiesForRepositoryClass(this.getClass().getCanonicalName());

        for (Property property : propertyArray) {
            RProcessInfo processInfo = null;
            String algorithm_wkn = property.getStringValue();

            if (property.getName().equalsIgnoreCase(RWPSConfigVariables.ALGORITHM_PROPERTY_NAME.toString())) {
                processInfo = new RProcessInfo(algorithm_wkn, this.rConfig);
                processInfoList.add(processInfo);
            }
            else
                continue;

            if (property.getActive()) {
                if ( !processInfo.isAvailable()) {
                    // property.setActive(false);
                    // propertyChanged=true;
                    LOGGER.error("Missing R script for process '{}'. Process ignored - check WPS configuration.",
                                 algorithm_wkn);
                    continue;
                }

                if ( !processInfo.isValid()) {
                    // property.setActive(false);
                    // propertyChanged=true;
                    LOGGER.error("Invalid R script for process '{}'. You may enable/disable it manually from the Web Admin console. Check logs for details.",
                                 algorithm_wkn);
                }

                addAlgorithm(algorithm_wkn);

                // //unavailable algorithms get an unavailable suffix in the
                // properties and will be deactivated
                // String unavailable_suffix = " (unavailable)";
                //
                // if(!rConfig.isScriptAvailable(algorithm_wkn)){
                // if(!algorithm_wkn.endsWith(unavailable_suffix)){
                // property.setName(algorithm_wkn+ unavailable_suffix);
                // }
                // property.setActive(false);
                // LOGGER.error("[WPS4R] Missing R script for process "+algorithm_wkn+". Property has been set inactive. Check WPS config.");
                //
                // }else{
                // if(algorithm_wkn.endsWith(unavailable_suffix)){
                // algorithm_wkn = algorithm_wkn.replace(unavailable_suffix,
                // "");
                // property.setName(algorithm_wkn);
                // }
                // addAlgorithm(algorithm_wkn);
                // }

            }
        }

        RProcessInfo.setRProcessInfoList(processInfoList);
    }

    public boolean addAlgorithms(String[] algorithms) {
        for (String algorithmClassName : algorithms) {
            addAlgorithm(algorithmClassName);
        }
        LOGGER.info("Algorithms registered!");
        return true;

    }

    @Override
    public IAlgorithm getAlgorithm(String algorithmName) {
        if ( !this.rConfig.getCacheProcesses()) {
            LOGGER.debug("Process cache disabled, creating new process for id '{}'", algorithmName);
            boolean b = addAlgorithm(algorithmName);
            if ( !b)
                LOGGER.warn("Problem adding algorithm for deactivated cache.");
        }

        if ( !this.algorithms.containsKey(algorithmName))
            throw new RuntimeException("This repository does not contain an algorithm '" + algorithmName + "'");
        else
            return this.algorithms.get(algorithmName);
    }

    public Collection<String> getAlgorithmNames() {
        return new ArrayList<String>(this.algorithms.keySet());
    }

    @Override
    public boolean containsAlgorithm(String className) {
        return this.algorithms.containsKey(className);
    }

    private IAlgorithm loadAlgorithmAndValidate(String wellKnownName) throws Exception {
        LOGGER.debug("Loading algorithm '{}'", wellKnownName);

        IAlgorithm algorithm = new GenericRProcess(wellKnownName);

        if ( !algorithm.processDescriptionIsValid()) {
            // collect the errors
            ProcessDescriptionType description = algorithm.getDescription();
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

            throw new Exception("Could not load algorithm " + wellKnownName + ". ProcessDescription not valid: "
                    + validationMessages.toString());
        }

        return algorithm;
    }

    @Override
    public boolean addAlgorithm(Object processID) {
        if (processID instanceof String) {
            String algorithmName = (String) processID;

            try {
                IAlgorithm a = loadAlgorithmAndValidate(algorithmName);
                this.algorithms.put(algorithmName, a);
                LOGGER.info("Algorithm under name '{}' added: {}", algorithmName, a);

                return true;
            }
            catch (Exception e) {
                String message = "Could not load algorithm for class name '" + algorithmName + "'";
                LOGGER.error(message, e);
                throw new RuntimeException(message + ": " + e.getMessage(), e);
            }
        }
        else
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
    public ProcessDescriptionType getProcessDescription(String processID) {
        return getAlgorithm(processID).getDescription();
    }

    @Override
    public void shutdown() {
        LOGGER.info("Shutting down ...");
        this.algorithms.clear();
    }

}
