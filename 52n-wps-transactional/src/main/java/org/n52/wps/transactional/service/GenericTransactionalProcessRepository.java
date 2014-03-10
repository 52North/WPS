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
package org.n52.wps.transactional.service;




import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.ITransactionalAlgorithmRepository;
import org.n52.wps.server.request.ExecuteRequest;
import org.n52.wps.transactional.algorithm.GenericTransactionalAlgorithm;
import org.n52.wps.transactional.deploy.AbstractProcessManager;
import org.n52.wps.transactional.deploy.IProcessManager;
import org.n52.wps.transactional.request.DeployProcessRequest;
import org.n52.wps.transactional.request.UndeployProcessRequest;




public class GenericTransactionalProcessRepository implements ITransactionalAlgorithmRepository{
	private static Logger LOGGER = LoggerFactory.getLogger(GenericTransactionalProcessRepository.class);
	protected Map<String, ProcessDescriptionType> processDescriptionMap;
	
	protected IProcessManager deployManager;
	
	
	public GenericTransactionalProcessRepository(String format){
		Property[] properties = WPSConfig.getInstance().getPropertiesForRepositoryClass(this.getClass().getName());
		//TODO think of multiple instance of this class registered (yet not possible since singleton)
		Property deployManagerXML = WPSConfig.getInstance().getPropertyForKey(properties, "DeployManager");
		if(deployManagerXML==null){
			throw new RuntimeException("Error. Could not find matching DeployManager");
		}
		processDescriptionMap = new HashMap<String, ProcessDescriptionType>();
		String className = deployManagerXML.getStringValue();
		try {
			
			Class deployManagerClass = Class.forName(className);
			if(deployManagerClass.asSubclass(AbstractProcessManager.class).equals(deployManagerClass)){
				Constructor constructor = deployManagerClass.getConstructor(ITransactionalAlgorithmRepository.class);
				deployManager = (IProcessManager) constructor.newInstance(this);
			}else{
				deployManager = (IProcessManager) deployManagerClass.newInstance();
			}
			
		} catch (InstantiationException e) {
			e.printStackTrace();
			throw new RuntimeException("Error. Could not find matching DeployManager");
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new RuntimeException("Error. Could not find matching DeployManager");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("Error. Could not find matching DeployManager");
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
		
	public boolean addAlgorithm(Object process) {
		if(!(process instanceof DeployProcessRequest)){
			return false;
		}
		DeployProcessRequest request = (DeployProcessRequest) process;
		try {
			deployManager.deployProcess(request);
		} catch (Exception e) {
			LOGGER.warn("Could not instantiate algorithm: " + request);
			e.printStackTrace();
			return false;
		}
		return true;

	}

	
	public boolean containsAlgorithm(String processID) {
		try {
			return deployManager.containsProcess(processID);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} 
	}

	public IAlgorithm getAlgorithm(String processID) {
		return new GenericTransactionalAlgorithm(processID, this.getClass());
		
	}

	public Collection<String> getAlgorithmNames() {
		try {
			return deployManager.getAllProcesses();
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<String>();
			
		}
	}

	public Collection<IAlgorithm> getAlgorithms() {
		Collection<IAlgorithm> result = new ArrayList<IAlgorithm>();
		Collection<String> allAlgorithms;
		try {
			allAlgorithms = deployManager.getAllProcesses();
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<IAlgorithm>();
		} 
		for(String processID : allAlgorithms){
			result.add(new GenericTransactionalAlgorithm(processID, this.getClass()));
		}
		return result;
	}

	public boolean removeAlgorithm(Object process) {
		if(!(process instanceof UndeployProcessRequest)){
			return false;
		}
		UndeployProcessRequest request = (UndeployProcessRequest) process;
		try {
			deployManager.unDeployProcess(request);
		} catch (Exception e) {
			LOGGER.warn("Could not remove algorithm: " + request);
			e.printStackTrace();
			return false;
		}
		processDescriptionMap.remove(request.getProcessID());
		return true;
		
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
