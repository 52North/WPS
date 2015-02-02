/**
 * ﻿Copyright (C) 2014 - 2014 52°North Initiative for Geospatial Open Source
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
package de.tudresden.gis.fusion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.ITransactionalAlgorithmRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tudresden.gis.fusion.manage.Operations;
import de.tudresden.gis.fusion.operation.IMeasurementOperation;
import de.tudresden.gis.fusion.operation.IOperation;

public class FusionAlgorithmRepository implements ITransactionalAlgorithmRepository {
	
	private static Logger LOGGER = LoggerFactory.getLogger(FusionAlgorithmRepository.class);
	
	private Map<String, String> operationMap;
	private Map<String, ProcessDescriptionType> operationDescriptionMap;
	private Map<String,FusionAlgorithm> fusionOperations;
	
	public FusionAlgorithmRepository(){	
		
		//init operation maps
		operationMap = new HashMap<String, String>();
		operationDescriptionMap = new HashMap<String, ProcessDescriptionType>();
		fusionOperations = new HashMap<String,FusionAlgorithm>();
		
		// check if the repository is active
		if(WPSConfig.getInstance().isRepositoryActive(this.getClass().getCanonicalName())){
			
			//load operations from fusion package
			Set<Class<? extends IMeasurementOperation>> operationClasses = Operations.getAvalaibleMeasurementOperations();
			for(Class<? extends IOperation> operationClass : operationClasses){
				try {
					FusionAlgorithm operation = new FusionAlgorithm(operationClass.newInstance());
					fusionOperations.put(operation.getWellKnownName(), operation);
					addAlgorithm(operation.getWellKnownName());
				} catch (Exception e) {
					LOGGER.debug("Could not load algorithm " + operationClass);
				} 
				
			}
			
		} else {
			LOGGER.debug("Local Algorithm Repository is inactive.");
		}
	}

	@Override
	public Collection<String> getAlgorithmNames() {
		return new ArrayList<String>(this.operationMap.keySet());
	}

	@Override
	public IAlgorithm getAlgorithm(String operationID) {
		IAlgorithm algorithm = null;
		try {
			
			algorithm = fusionOperations.get(operationID);
			
			if(!algorithm.processDescriptionIsValid()) {
				LOGGER.warn("Could not load algorithm " + operationID + ". Algorithm description is not valid.");
				return null;
			}
		} catch (Exception e) {
			return null;
		}
		return algorithm;
	}

	@Override
	public ProcessDescriptionType getProcessDescription(String operationID) {
		if(!operationDescriptionMap.containsKey(operationID)){
			operationDescriptionMap.put(operationID, getAlgorithm(operationID).getDescription());
		}
		return operationDescriptionMap.get(operationID);
	}

	@Override
	public boolean containsAlgorithm(String operationID) {
		return operationMap.containsKey(operationID);
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean addAlgorithm(Object operationID) {
		if(!(operationID instanceof String)){
			return false;
		}
		String algorithmClassName = (String) operationID;
				
		operationMap.put(algorithmClassName, algorithmClassName);
		LOGGER.info("Algorithm class registered: " + algorithmClassName);
		return true;
	}

	@Override
	public boolean removeAlgorithm(Object operationID) {
		if(!(operationID instanceof String)){
			return false;
		}
		String className = (String) operationID;
		if(operationMap.containsKey(className)){
			operationMap.remove(className);
			return true;
		}
		return false;
	}

}