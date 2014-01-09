/**
 * ﻿Copyright (C) 2007
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

package org.n52.wps.server;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.RepositoryDocument.Repository;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.request.ExecuteRequest;

/**
 * @author Bastian Schaeffer, University of Muenster
 *
 */
public class RepositoryManager {
	
	private static RepositoryManager instance;
	private static Logger LOGGER = LoggerFactory.getLogger(RepositoryManager.class);
	private List<IAlgorithmRepository> repositories;
	private ProcessIDRegistry globalProcessIDs = ProcessIDRegistry.getInstance();
	private UpdateThread updateThread;
	
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
        
        Double updateHours = WPSConfig.getInstance().getWPSConfig().getServer().getRepoReloadInterval();
        
        if (updateHours != 0){
        	LOGGER.info("Setting repository update period to " + updateHours + " hours.");
        	updateHours = updateHours * 3600 * 1000; // make milliseconds
            long updateInterval = updateHours.longValue();
            this.updateThread = new UpdateThread(updateInterval);
        	updateThread.start();
        }
        
    	
	}

    private void loadAllRepositories(){
        repositories = new ArrayList<IAlgorithmRepository>();
        System.gc();

		Repository[] repositoryList = WPSConfig.getInstance().getRegisterdAlgorithmRepositories();

		for(Repository repository : repositoryList){
			if(repository.getActive()==false){
				continue;
			}
			String repositoryClassName = repository.getClassName();
			try {
				IAlgorithmRepository algorithmRepository = null;
				Class repositoryClass = RepositoryManager.class.getClassLoader().loadClass(repositoryClassName);
				Constructor[] constructors = repositoryClass.getConstructors();
				for(Constructor constructor : constructors){
				
					if(constructor.getParameterTypes().length==1 && constructor.getParameterTypes()[0].equals(String.class)){
						Property[] properties = repository.getPropertyArray();
						Property formatProperty = WPSConfig.getInstance().getPropertyForKey(properties, "supportedFormat");
						String format = formatProperty.getStringValue();
						algorithmRepository = (IAlgorithmRepository) repositoryClass.getConstructor(String.class).newInstance(format);
					}else{
						algorithmRepository = (IAlgorithmRepository) repositoryClass.newInstance();
					}
				}
				
				
				repositories.add(algorithmRepository);
				LOGGER.info("Algorithm Repository "+ repositoryClassName + " initialized");
			} catch (InstantiationException e) {
				LOGGER.warn("An error occured while registering AlgorithmRepository: " + repositoryClassName);
			} catch (IllegalAccessException e) {
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
				LOGGER.warn("An error occured while registering AlgorithmRepository: " + repositoryClassName + ". Reason " + e.getMessage());
			} catch (IllegalArgumentException e) {
				LOGGER.warn("An error occured while registering AlgorithmRepository: " + repositoryClassName +  ". Reason " + e.getMessage());
			} catch (SecurityException e) {
				LOGGER.warn("An error occured while registering AlgorithmRepository: " + repositoryClassName + ". Reason " + e.getMessage());
			} catch (InvocationTargetException e) {
				LOGGER.warn("An error occured while registering AlgorithmRepository: " + repositoryClassName + ". Reason " + e.getMessage());
			} catch (NoSuchMethodException e) {
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
	 * Allows to reInitialize the Repositories
	 *
	 */
	protected void reloadRepositories() {
		loadAllRepositories();
	}
	
	/**
	 * Methods looks for Algorithm in all Repositories.
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
	  for (IAlgorithmRepository repo : repositories ){
		   if(repo.getClass().getName().equals(name)){
			   return repo;
		  }
	  }
	return null;
	}

	public IAlgorithmRepository getRepositoryForClassName(
			String className) {
		for(IAlgorithmRepository repository : repositories){
			if(repository.getClass().getName().equals(className)){
				return repository;
			}
		}
		return null;
	}
	
	public ProcessDescriptionType getProcessDescription(String processClassName){
		for(IAlgorithmRepository repository : repositories){
			if(repository.containsAlgorithm(processClassName)){
				return repository.getProcessDescription(processClassName);
			}
		}
		return null;
	}
	
    static class UpdateThread extends Thread {
        
    	private final long interval;
    	private boolean firstrun = true;
    	
    	public UpdateThread (long interval){
    		this.interval = interval;
    	}
    	
        @Override
        public void run() {
        	LOGGER.debug("UpdateThread started");
        	
        	try {
        		// never terminate the run method
        		while (true){
        			// do not update on first run!
        			if (!firstrun){
        				LOGGER.info("Reloading repositories - this might take a while ...");
            			long timestamp = System.currentTimeMillis();
            			RepositoryManager.getInstance().reloadRepositories();
            			LOGGER.info("Repositories reloaded - going to sleep. Took " + (System.currentTimeMillis()-timestamp) / 1000 + " seconds.");
        			} else {
        				firstrun = false;
        			}
        			
        			// sleep for a given INTERVAL
        			sleep(interval);
        		}
			} catch (InterruptedException e) {
				LOGGER.debug("Interrupt received - Terminating the UpdateThread.");
			}
        }
       
    }
    
    // shut down the update thread
    public void finalize(){
    	if (updateThread != null){
    		updateThread.interrupt();
    	}
    }

	public void shutdown() {
		for (IAlgorithmRepository repo : repositories) {
			repo.shutdown();
		}
	}

}
