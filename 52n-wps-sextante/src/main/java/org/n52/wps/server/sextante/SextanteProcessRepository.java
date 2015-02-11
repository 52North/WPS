/**
 * ﻿Copyright (C) 2008 - 2014 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.server.sextante;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.IAlgorithmRepository;
import org.n52.wps.server.ProcessDescription;
import org.n52.wps.server.sextante.SextanteProcessDescriptionCreator.UnsupportedGeoAlgorithmException;
import org.n52.wps.webapp.api.AlgorithmEntry;
import org.n52.wps.webapp.api.ConfigurationCategory;
import org.n52.wps.webapp.api.ConfigurationModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;


/*
 * A container, which allows the 52n WPS to recognize the sextante library.
 * Basic initialization is performed here.
 * 
 * Whenever a getcapabilities request comes in, the process names are extraced based on the available process description documents for sextante processes.
 * This should be changed in the future, when process descriptions should be generated automatically. When a execute process request comes in, a generic GenericSextanteProcessDelegator is created. 
 */


public class SextanteProcessRepository implements IAlgorithmRepository{
	private static Logger LOGGER = LoggerFactory.getLogger(SextanteProcessRepository.class);
	private Map<String, ProcessDescription> registeredProcesses;
	 
	
	public SextanteProcessRepository(){
		LOGGER.info("Initializing Sextante Repository");
		registeredProcesses = new HashMap<String, ProcessDescription>();
		
		/*
		 * get properties of Repository
		 *
		 * check whether process is amongst them and active
		 * 
		 * if properties are empty (not initialized yet)
		 * 		add all valid processes to WPSConfig
		 */
		
		ConfigurationModule sextanteAlgorithmRepoConfigModule = WPSConfig.getInstance().getConfigurationModuleForClass(this.getClass().getName(), ConfigurationCategory.REPOSITORY);
			
		List<AlgorithmEntry> algorithmEntries = sextanteAlgorithmRepoConfigModule.getAlgorithmEntries();	
		
		ArrayList<String> processList = new ArrayList<String>(algorithmEntries.size());		
		
		for (AlgorithmEntry algorithmEntry : algorithmEntries) {
			if(algorithmEntry.isActive()){
				processList.add(algorithmEntry.getAlgorithm());
			}else{
				LOGGER.info("Sextante Process : " + algorithmEntry.getAlgorithm() + " not active.");				
			}
		}		
		
		Sextante.initialize();
		HashMap<String, HashMap<String, GeoAlgorithm>> sextanteMap = Sextante.getAlgorithms();
		HashMap<String, GeoAlgorithm> algorithmMap = sextanteMap.get("SEXTANTE");
		Set<String> keys = algorithmMap.keySet();
		SextanteProcessDescriptionCreator descriptionCreator = new SextanteProcessDescriptionCreator();
		for(Object keyObject : keys){
			String key = (String) keyObject;
			if(!processList.contains(key)){
				LOGGER.info("Did not add Sextante Process : " + key +". Not in Repository properties or not active.");
				continue;
			}
			GeoAlgorithm sextanteProcess = Sextante.getAlgorithmFromCommandLineName(key);
			ProcessDescription processDescription;
			try {
				processDescription = descriptionCreator.createDescribeProcessType(sextanteProcess);
			} catch (NullParameterAdditionalInfoException e) {
				LOGGER.warn("Could not add Sextante Process : " + key +". Errors while creating describe Process");
				continue;
			} catch (UnsupportedGeoAlgorithmException e) {
				LOGGER.warn("Could not add Sextante Process : " + key + ". Errors while creating describe Process");
				continue;
			}
		
			registeredProcesses.put(key, processDescription);
			LOGGER.info("Sextante Process " + key + " added.");
		}
		
		
		LOGGER.info("Initialization of Sextante Repository successfull");
	}

	public boolean containsAlgorithm(String processID) {
		if(registeredProcesses.containsKey(processID)){
			return true;
		}
		LOGGER.warn("Could not find Sextante Process " + processID);
		return false;
	}

	public IAlgorithm getAlgorithm(String processID) {
		if(!containsAlgorithm(processID)){
			throw new RuntimeException("Could not allocate Process");
		}
		return new GenericSextanteProcessDelegator(processID, registeredProcesses.get(processID));
				
		
	}

	public Collection<String> getAlgorithmNames() {
		return registeredProcesses.keySet();
	}

	public boolean removeAlgorithm(Object className) {
		//not implemented
		return false;
	}
	
	@Override
	public ProcessDescription getProcessDescription(String processID) {
		if(!registeredProcesses.containsKey(processID)){
			registeredProcesses.put(processID, getAlgorithm(processID).getDescription());
		}
		return registeredProcesses.get(processID);
	}


	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
	}

}
