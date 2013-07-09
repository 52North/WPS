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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.ITransactionalAlgorithmRepository;
import org.n52.wps.server.r.data.CustomDataTypeManager;
import org.n52.wps.server.r.info.RProcessInfo;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

/**
 * A static repository to retrieve the available algorithms.
 * 
 * @author Matthias Hinz
 * 
 */
public class LocalRAlgorithmRepository implements ITransactionalAlgorithmRepository {

    private static Logger LOGGER = LoggerFactory.getLogger(LocalRAlgorithmRepository.class);

    // registered Processes
    private Map<String, String> algorithmMap;

    // local cache for algorithm descriptions
    private Map<String, GenericRProcess> algorithmDescriptionMap = new HashMap<String, GenericRProcess>();

    public LocalRAlgorithmRepository() {
        LOGGER.info("Initializing LocalRAlgorithmRepository");
        this.algorithmMap = new HashMap<String, String>();

        //Check WPS Config properties:
        RPropertyChangeManager changeManager = RPropertyChangeManager.getInstance();
        // unregistered scripts from repository folder will be added as Algorithm to WPSconfig
        changeManager.updateRepositoryConfiguration();
        CustomDataTypeManager.getInstance().update();
        checkStartUpConditions();
        
        //finally add all available algorithms from the R config
        addAllAlgorithms();
    }

    /**
     * Check if repository is active and Rserve can be found
     * @return
     */
	private boolean checkStartUpConditions() {
		// check if the repository is active:
        String className = this.getClass().getCanonicalName();
        if ( !WPSConfig.getInstance().isRepositoryActive(className)) {
            LOGGER.debug("Local R Algorithm Repository is inactive.");
            return false;
        }
        
        // Try to build up a connection to Rserve
        // If it is refused, a new instance of Rserve will be opened
        LOGGER.debug("[Rserve] Trying to connect to Rserve.");
        R_Config rConfig = R_Config.getInstance();
        try {
            RConnection testcon = rConfig.openRConnection();
            LOGGER.info("[Rserve] WPS successfully connected to Rserve.");
            testcon.close();
        }
        catch (RserveException e) {
            // try to start Rserve via batchfile if enabled
            LOGGER.error("[Rserve] Could not connect to Rserve. Rserve may not be available or may not be ready at the current time.", e);
            e.printStackTrace();
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
			
        	if (property.getName().equalsIgnoreCase(RWPSConfigVariables.ALGORITHM.toString())){
        		processInfo  = new RProcessInfo(algorithm_wkn);
        		processInfoList.add(processInfo);
        	}else
        		continue;
        	
            if (property.getActive()) {
				if (!processInfo.isAvailable()) {
					//property.setActive(false);
					//propertyChanged=true;
					LOGGER.error("[WPS4R] Missing R script for process "
							+ algorithm_wkn
							+ ". Process ignored. Check WPS configuration.");
					continue;
				}
				
				if (!processInfo.isValid()) {
					//property.setActive(false);
					//propertyChanged=true;
					LOGGER.error("[WPS4R] Invalid R script for process "
							+ algorithm_wkn
							//+ ". Process ignored. Check previous logs.");
							+ ". You may enable/disable it manually from the Web Admin console. Check previous logs for details.");
				}
				
				addAlgorithm(algorithm_wkn);
				
				
//            	//unavailable algorithms get an unavailable suffix in the properties and will be deactivated
//            	String unavailable_suffix = " (unavailable)";
//            	
//            	if(!rConfig.isScriptAvailable(algorithm_wkn)){
//            		if(!algorithm_wkn.endsWith(unavailable_suffix)){
//            			property.setName(algorithm_wkn+ unavailable_suffix);
//            		}
//            		property.setActive(false);
//            		LOGGER.error("[WPS4R] Missing R script for process "+algorithm_wkn+". Property has been set inactive. Check WPS config.");
//  
//            	}else{           	
//	            	if(algorithm_wkn.endsWith(unavailable_suffix)){
//	            		algorithm_wkn = algorithm_wkn.replace(unavailable_suffix, "");
//	            		property.setName(algorithm_wkn);
//	            	}
//	            	addAlgorithm(algorithm_wkn);
//            	}
            	
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

    public IAlgorithm getAlgorithm(String className) {
        try {
            return loadAlgorithm(this.algorithmMap.get(className));
        }
        catch (Exception e) {
            String message = "Could not load algorithm for class name " + className;
            LOGGER.error(message, e);
            throw new RuntimeException(message+'\n' + e.getMessage(), e);
        }
    }

    public Collection<IAlgorithm> getAlgorithms() {
        Collection<IAlgorithm> resultList = new ArrayList<IAlgorithm>();
        try {
            for (String algorithmClasses : this.algorithmMap.values()) {
                String algName = this.algorithmMap.get(algorithmClasses);
                IAlgorithm algorithm = loadAlgorithm(algName);
                resultList.add(algorithm);
            }
        }
        catch (Exception e) {
            String message = "Could not load algorithms.";
            LOGGER.error(message, e);
            throw new RuntimeException(message, e);

        }
        return resultList;
    }

    public Collection<String> getAlgorithmNames() {
        return new ArrayList<String>(this.algorithmMap.keySet());
    }

    public boolean containsAlgorithm(String className) {
        return this.algorithmMap.containsKey(className);
    }

    private static IAlgorithm loadAlgorithm(String wellKnownName) throws Exception {
        LOGGER.debug("Loading algorithm '" + wellKnownName + "'");
        
        IAlgorithm algorithm = new GenericRProcess(wellKnownName);
        if ( !algorithm.processDescriptionIsValid()) {
            LOGGER.warn("Algorithm description is not valid: " + wellKnownName);
            throw new Exception("Could not load algorithm " + wellKnownName + ". ProcessDescription Not Valid.");
        }
        return algorithm;
    }

    public boolean addAlgorithm(Object processID) {
        if ( ! (processID instanceof String)) {
            return false;
        }
        String algorithmClassName = (String) processID;

        this.algorithmMap.put(algorithmClassName, algorithmClassName);
        LOGGER.info("Algorithm class registered: " + algorithmClassName);

        return true;
    }

    /**
     * Removes algorithm from AlgorithmMap
     */
    public boolean removeAlgorithm(Object processID) {
        if ( ! (processID instanceof String)) {
            return false;
        }
        String processName = (String) processID;
        if (this.algorithmMap.containsKey(processName)) {
            this.algorithmMap.remove(processName);
        }

        return true;
    }

    @Override
    public ProcessDescriptionType getProcessDescription(String processID) {
        if(this.algorithmMap.containsKey(processID)) {
            LOGGER.debug("Creating new process to get the description for " + processID);
            GenericRProcess process = new GenericRProcess(processID);
            this.algorithmDescriptionMap.put(processID, process);
        }
        
        LOGGER.debug("Returning  process description from cache: " + processID);
        return this.algorithmDescriptionMap.get(processID).getDescription();
    }

    @Override
    public void shutdown() {
        LOGGER.info("Shutting down ...");
    }

}
