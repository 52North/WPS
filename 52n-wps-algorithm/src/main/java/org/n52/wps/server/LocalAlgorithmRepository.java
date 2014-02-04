/**
 * ﻿Copyright (C) 2007 - 2014 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.server;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.commons.WPSConfig;



/**
 * A static repository to retrieve the available algorithms.
 * @author foerster
 *
 */
public class LocalAlgorithmRepository implements ITransactionalAlgorithmRepository{
	
	private static Logger LOGGER = LoggerFactory.getLogger(LocalAlgorithmRepository.class);
	private Map<String, String> algorithmMap;
	private Map<String, ProcessDescriptionType> processDescriptionMap;
	
	public LocalAlgorithmRepository() {
		algorithmMap = new HashMap<String, String>();
		processDescriptionMap = new HashMap<String, ProcessDescriptionType>(); 
		
		// check if the repository is active
		if(WPSConfig.getInstance().isRepositoryActive(this.getClass().getCanonicalName())){
			Property[] propertyArray = WPSConfig.getInstance().getPropertiesForRepositoryClass(this.getClass().getCanonicalName());
			for(Property property : propertyArray){
				// check the name and active state
				if(property.getName().equalsIgnoreCase("Algorithm") && property.getActive()){
					addAlgorithm(property.getStringValue());
				}
			}
		} else {
			LOGGER.debug("Local Algorithm Repository is inactive.");
		}
	}
	
	public boolean addAlgorithms(String[] algorithms)  {
		for(String algorithmClassName : algorithms) {
			addAlgorithm(algorithmClassName);
		}
		LOGGER.info("Algorithms registered!");
		return true;
		
	}
	
	public IAlgorithm getAlgorithm(String className) {
		try {
			return loadAlgorithm(algorithmMap.get(className));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Collection<String> getAlgorithmNames() {
		return new ArrayList<String>(algorithmMap.keySet());
	}
	
	public boolean containsAlgorithm(String className) {
		return algorithmMap.containsKey(className);
	}
	
	private IAlgorithm loadAlgorithm(String algorithmClassName) throws Exception {
        Class<?> algorithmClass = LocalAlgorithmRepository.class.getClassLoader().loadClass(algorithmClassName);
        IAlgorithm algorithm = null;
        if (IAlgorithm.class.isAssignableFrom(algorithmClass)) {
            algorithm = IAlgorithm.class.cast(algorithmClass.newInstance());
        } else if (algorithmClass.isAnnotationPresent(Algorithm.class)) {
            // we have an annotated algorithm that doesn't implement IAlgorithm
            // wrap it in a proxy class
            algorithm = new AbstractAnnotatedAlgorithm.Proxy(algorithmClass);
        }
        else {
            throw new Exception("Could not load algorithm " + algorithmClassName + " does not implement IAlgorithm or have a Algorithm annotation.");
        }
		
		if(!algorithm.processDescriptionIsValid()) {
			LOGGER.warn("Algorithm description is not valid: " + algorithmClassName);
			throw new Exception("Could not load algorithm " +algorithmClassName +". ProcessDescription Not Valid.");
		}
		return algorithm;
	}

	public boolean addAlgorithm(Object processID) {
		if(!(processID instanceof String)){
			return false;
		}
		String algorithmClassName = (String) processID;
				
		algorithmMap.put(algorithmClassName, algorithmClassName);
		LOGGER.info("Algorithm class registered: " + algorithmClassName);
					
			
		
		return true;

	}

	public boolean removeAlgorithm(Object processID) {
		if(!(processID instanceof String)){
			return false;
		}
		String className = (String) processID;
		if(algorithmMap.containsKey(className)){
			algorithmMap.remove(className);
			return true;
		}
		return false;
	}

	@Override
	public ProcessDescriptionType getProcessDescription(String processID) {
		if(!processDescriptionMap.containsKey(processID)){
			processDescriptionMap.put(processID, getAlgorithm(processID).getDescription());
		}
		return processDescriptionMap.get(processID);
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
    }
		
	
	

	

	


	

}
