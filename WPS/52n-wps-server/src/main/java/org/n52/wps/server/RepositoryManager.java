/***************************************************************
This implementation provides a framework to publish processes to the
web through the  OGC Web Processing Service interface. The framework 
is extensible in terms of processes and data handlers. It is compliant 
to the WPS version 0.4.0 (OGC 05-007r4). 

Copyright (C) 2009 by con terra GmbH

Authors: 
	Bastian Schäffer, University of Muenster



Contact: Albert Remke, con terra GmbH, Martin-Luther-King-Weg 24,
48155 Muenster, Germany, 52n@conterra.de

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program (see gnu-gpl v2.txt); if not, write to
the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA or visit the web page of the Free
Software Foundation, http://www.fsf.org.

***************************************************************/


package org.n52.wps.server;


import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.n52.wps.RepositoryDocument.Repository;
import org.n52.wps.commons.WPSConfig;

public class RepositoryManager {
	
	public static String PROPERTY_NAME_REGISTERED_REPOSITORIES = "registeredAlgorithmRepositories";

	private static RepositoryManager instance;
	private static Logger LOGGER = Logger.getLogger(RepositoryManager.class);
	
	private List<IAlgorithmRepository> repositories;
	
	
	
	private RepositoryManager(){
		repositories = new ArrayList<IAlgorithmRepository>();
		/*if(!WPSConfiguration.getInstance().exists(PROPERTY_NAME_REGISTERED_REPOSITORIES)) {
			LOGGER.warn("Missing " + PROPERTY_NAME_REGISTERED_REPOSITORIES + "Property");
			return;
		}
		String propertyValue = WPSConfiguration.getInstance().getProperty(PROPERTY_NAME_REGISTERED_REPOSITORIES);
		String[] registeredRepositories = propertyValue.split(",");
		*/
		Repository[] repositoryList = WPSConfig.getInstance().getRegisterdAlgorithmRepositories();
		
		for(Repository repository : repositoryList){
			String repositoryClassName = repository.getClassName();
			try {
			
				IAlgorithmRepository algorithmRepository = (IAlgorithmRepository)RepositoryManager.class.getClassLoader().loadClass(repositoryClassName).newInstance();
				repositories.add(algorithmRepository);
				LOGGER.info("Algorithm Repositories initialized");
			} catch (InstantiationException e) {
				LOGGER.warn("An error occured while registering AlgorithmRepository: " + repositoryClassName);
			} catch (IllegalAccessException e) {
				//in case of an singleton
				try {
					
					IAlgorithmRepository algorithmRepository = (IAlgorithmRepository)RepositoryManager.class.getClassLoader().loadClass(repositoryClassName).getMethod("getInstance", new Class[0]).invoke(null, new Object[0]);
					repositories.add(algorithmRepository);
				} catch (IllegalArgumentException e1) {
					LOGGER.warn("An error occured while registering AlgorithmRepository: " + repositoryClassName);
				} catch (SecurityException e1) {
					LOGGER.warn("An error occured while registering AlgorithmRepository: " + repositoryClassName);
				} catch (IllegalAccessException e1) {
					LOGGER.warn("An error occured while registering AlgorithmRepository: " + repositoryClassName);
				} catch (InvocationTargetException e1) {
					LOGGER.warn("An error occured while registering AlgorithmRepository: " + repositoryClassName);
				} catch (NoSuchMethodException e1) {
					LOGGER.warn("An error occured while registering AlgorithmRepository: " + repositoryClassName);
				} catch (ClassNotFoundException e1) {
					LOGGER.warn("An error occured while registering AlgorithmRepository: " + repositoryClassName);
				}				
			
				
			} catch (ClassNotFoundException e) {
				LOGGER.warn("An error occured while registering AlgorithmRepository: " + repositoryClassName + ". Class not found");
			}
		}
	}
	
	public static RepositoryManager getInstance(){
		if(instance==null){
			instance = new RepositoryManager();
		}
		return instance;
	}
	
	/**
	 * Allows to reInitialize the RepositoryManager... This should not be called to often.
	 *
	 */
	public static void reInitialize() {
		instance = new RepositoryManager();
	}
	
	/**
	 * Methods looks for Algorithhm in all Repositories.
	 * The first match is returned.
	 * If no match could be found, null is returned
	 *
	 * @param className
	 * @return IAlgorithm or null
	 * @throws Exception
	 */
	public IAlgorithm getAlgorithm(String className){
		for(IAlgorithmRepository repository : repositories){
			if(repository.containsAlgorithm(className)){
				return repository.getAlgorithm(className);
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @return allAlgorithms
	 */
	public List<String> getAlgorithms(){
		List<String> allAlgorithmNamesCollection = new ArrayList<String>();
		for(IAlgorithmRepository repository : repositories){
			allAlgorithmNamesCollection.addAll(repository.getAlgorithmNames());
		}
		return allAlgorithmNamesCollection;
		
	}

	public boolean containsAlgorithm(String algorithmName) {
		for(IAlgorithmRepository repository : repositories){
			if(repository.containsAlgorithm(algorithmName)){
				return true;
			}
		}
		return false;
	}
	
	public IAlgorithmRepository getRepositoryForAlgorithm(String algorithmName){
		for(IAlgorithmRepository repository : repositories){
			if(repository.containsAlgorithm(algorithmName)){
				return repository;
			}
		}
		return null;
	}
	
	public Class getInputDataTypeForAlgorithm(String algorithmIdentifier, String inputIdentifier){
		IAlgorithm algorithm = getAlgorithm(algorithmIdentifier);
		return algorithm.getInputDataType(inputIdentifier);
		
	}
	
	public Class getOutputDataTypeForAlgorithm(String algorithmIdentifier, String inputIdentifier){
		IAlgorithm algorithm = getAlgorithm(algorithmIdentifier);
		return algorithm.getOutputDataType(inputIdentifier);
		
	}
	

}
