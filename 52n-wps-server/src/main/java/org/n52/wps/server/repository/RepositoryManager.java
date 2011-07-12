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


package org.n52.wps.server.repository;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.log4j.Logger;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.RepositoryDocument.Repository;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.ProcessIDRegistry;
import org.n52.wps.server.request.ExecuteRequest;

public class RepositoryManager {
	

	private static RepositoryManager instance;
	private static Logger LOGGER = Logger.getLogger(RepositoryManager.class);
	
	private List<IAlgorithmRepository> repositories;
	
	private ProcessIDRegistry globalProcessIDs = ProcessIDRegistry.getInstance();
	
	private RepositoryManager(){
		
		// clear registry
		globalProcessIDs.clearRegistry();
		
        // initialize all Repositories
        loadAllRepositories();

        // FvK: added Property Change Listener support
        // creates listener and register it to the wpsConfig instance.
        WPSConfig.getInstance().addPropertyChangeListener(WPSConfig.WPSCONFIG_PROPERTY_EVENT_NAME, new PropertyChangeListener() {
            public void propertyChange(
                    final PropertyChangeEvent propertyChangeEvent) {
                LOGGER.info(this.getClass().getName() + ": Received Property Change Event: " + propertyChangeEvent.getPropertyName());
                loadAllRepositories();
            }
        });

	}

    private void loadAllRepositories(){
        repositories = new ArrayList<IAlgorithmRepository>();

		Repository[] repositoryList = WPSConfig.getInstance().getRegisterdAlgorithmRepositories();

		for(Repository repository : repositoryList){
			if(repository.getActive()==false){
				continue;
			}
			String repositoryClassName = repository.getClassName();
			LOGGER.info("repository className:"+repositoryClassName);
			try {
				IAlgorithmRepository algorithmRepository = null;
				Class repositoryClass = RepositoryManager.class.getClassLoader().loadClass(repositoryClassName);
				LOGGER.info("class repository:"+repositoryClass);
				Constructor[] constructors = repositoryClass.getConstructors();
				for(Constructor constructor : constructors){
				
					if(constructor.getParameterTypes().length==1 && constructor.getParameterTypes()[0].equals(String.class)){
						Property[] properties = repository.getPropertyArray();
						Property formatProperty = WPSConfig.getInstance().getPropertyForKey(properties, "supportedFormat");
						String format = formatProperty.getStringValue();
						LOGGER.info("format:"+format);
						algorithmRepository = (IAlgorithmRepository) repositoryClass.getConstructor(String.class).newInstance(format);
					}else{
						algorithmRepository = (IAlgorithmRepository) repositoryClass.newInstance();
					}
				}
				
				
				repositories.add(algorithmRepository);
				LOGGER.info("Algorithm Repositories initialized");
			} catch (InstantiationException e) {
				LOGGER.warn("An error occured while registering AlgorithmRepository: " + repositoryClassName);
				 e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				//in case of an singleton
//				try {
//
//					IAlgorithmRepository algorithmRepository = (IAlgorithmRepository)RepositoryManager.class.getClassLoader().loadClass(repositoryClassName).getMethod("getInstance", new Class[0]).invoke(null, new Object[0]);
//					repositories.add(algorithmRepository);
//				} catch (IllegalArgumentException e1) {
//					LOGGER.warn("An error occured while registering AlgorithmRepository: " + repositoryClassName);
//				} catch (SecurityException e1) {
//					LOGGER.warn("An error occured while registering AlgorithmRepository: " + repositoryClassName);
//				} catch (IllegalAccessException e1) {
//					LOGGER.warn("An error occured while registering AlgorithmRepository: " + repositoryClassName);
//				} catch (InvocationTargetException e1) {
//					LOGGER.warn("An error occured while registering AlgorithmRepository: " + repositoryClassName);
//				} catch (NoSuchMethodException e1) {
//					LOGGER.warn("An error occured while registering AlgorithmRepository: " + repositoryClassName);
//				} catch (ClassNotFoundException e1) {
//					LOGGER.warn("An error occured while registering AlgorithmRepository: " + repositoryClassName);
//				}
				LOGGER.warn("An error occured while registering AlgorithmRepository: " + repositoryClassName);

			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				LOGGER.warn("An error occured while registering AlgorithmRepository: " + repositoryClassName + ". Reason " + e.getMessage());
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				LOGGER.warn("An error occured while registering AlgorithmRepository: " + repositoryClassName +  ". Reason " + e.getMessage());
			} catch (SecurityException e) {
				e.printStackTrace();
				LOGGER.warn("An error occured while registering AlgorithmRepository: " + repositoryClassName + ". Reason " + e.getMessage());
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				LOGGER.warn("An error occured while registering AlgorithmRepository: " + repositoryClassName + ". Reason " + e.getMessage());
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
				LOGGER.warn("An error occured while registering AlgorithmRepository: " + repositoryClassName + ". Reason " + e.getMessage());
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
	public IAlgorithm getAlgorithm(String className, ExecuteRequest executeRequest){
		for(IAlgorithmRepository repository : repositories){
			if(repository.containsAlgorithm(className)){
				return repository.getAlgorithm(className, executeRequest);
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
		LOGGER.info(algorithmName);
		for(IAlgorithmRepository repository : repositories){
			if(repository.containsAlgorithm(algorithmName)){
				return repository;
			}
		}
		return null;
	}
	
	public Class getInputDataTypeForAlgorithm(String algorithmIdentifier, String inputIdentifier){
		IAlgorithm algorithm = getAlgorithm(algorithmIdentifier, null);
		return algorithm.getInputDataType(inputIdentifier);
		
	}
	
	public Class getOutputDataTypeForAlgorithm(String algorithmIdentifier, String inputIdentifier){
		IAlgorithm algorithm = getAlgorithm(algorithmIdentifier, null);
		return algorithm.getOutputDataType(inputIdentifier);
		
	}
	
	public boolean registerAlgorithm(String id, IAlgorithmRepository repository){
		if (globalProcessIDs.addID(id)){
			return true;
		}
		else return false;
	}
	
	public boolean unregisterAlgorithm(String id){
		if (globalProcessIDs.removeID(id)){
			return true;
		}
		else return false;
	}
	
	public IAlgorithmRepository getAlgorithmRepository(String name){
		LOGGER.info(name);
	  for (IAlgorithmRepository repo : repositories ){
		   if(repo.getClass().getName().equals(name)){
			   return repo;
		  }
	  }
	return null;
	}

	public IAlgorithmRepository getRepositoryForClassName(
			String className) {
		LOGGER.info("getRepositoryForClassName:"+className);
		for(IAlgorithmRepository repository : repositories){
			if(repository.getClass().getName().equals(className)){
				return repository;
			}
			LOGGER.info("getRepositoryForClassName - current:"+repository.getClass().getName());
		}
		LOGGER.info("getRepositoryForClassName - not equal found");
		return null;
	}
	
	public ProcessDescriptionType getProcessDescription(String processClassName){
		LOGGER.info("getProcessDescription:"+processClassName);
		for(IAlgorithmRepository repository : repositories){
			if(repository.containsAlgorithm(processClassName)){
				return repository.getProcessDescription(processClassName);
			}
		}
		return null;
	}

}
