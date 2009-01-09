package org.n52.wps.server;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;



/**
 * A static repository to retrieve the available algorithms.
 * @author foerster
 *
 */
public class LocalAlgorithmRepository implements ITransactionalAlgorithmRepository{
	
	private static String PROPERTY_NAME_REGISTERED_LOCAL_ALGORITHMS = "registeredLocalAlgorithms";
	private static Logger LOGGER = Logger.getLogger(LocalAlgorithmRepository.class);
	private static Map<String, IAlgorithm> algorithmMap;
	
	private static LocalAlgorithmRepository instance;
	
	private LocalAlgorithmRepository() {
		algorithmMap = new HashMap<String, IAlgorithm>();
		/*if(!WPSConfiguration.getInstance().exists(PROPERTY_NAME_REGISTERED_LOCAL_ALGORITHMS)) {
			LOGGER.warn("Missing " + PROPERTY_NAME_REGISTERED_LOCAL_ALGORITHMS + "Property");
			return;
		}
		String propertyValue = WPSConfiguration.getInstance().getProperty(PROPERTY_NAME_REGISTERED_LOCAL_ALGORITHMS);
		String[] registeredAlgorithms = propertyValue.split(",");
		addAlgorithms(registeredAlgorithms);*/
		
		Property[] propertyArray = WPSConfig.getInstance().getPropertiesForRepositoryClass(this.getClass().getCanonicalName());
		for(Property property : propertyArray){
			if(property.getName().equalsIgnoreCase("Algorithm")){
				addAlgorithm(property.getStringValue());
			}
		}
		
	}
	
	public static LocalAlgorithmRepository getInstance(){
		if(instance==null){
			instance = new LocalAlgorithmRepository();
		}
		return instance;
	}
	
	public boolean addAlgorithms(String[] algorithms)  {
		for(String algorithmClassName : algorithms) {
			addAlgorithm(algorithmClassName);
		}
		LOGGER.info("Algorithms registered!");
		return true;
		
	}
	
	public IAlgorithm getAlgorithm(String className) {
		return algorithmMap.get(className);
	}
	
	public Collection<IAlgorithm> getAlgorithms() {
		return algorithmMap.values();
	}
	
	public Collection<String> getAlgorithmNames() {
		return new ArrayList<String>(algorithmMap.keySet());
	}
	
	public boolean containsAlgorithm(String className) {
		return algorithmMap.containsKey(className);
	}

	public boolean addAlgorithm(Object processID) {
		if(!(processID instanceof String)){
			return false;
		}
		String algorithmClassName = (String) processID;
		try {
			IAlgorithm algorithm = (IAlgorithm)LocalAlgorithmRepository.class.getClassLoader().loadClass(algorithmClassName).newInstance();
			if(!algorithm.processDescriptionIsValid()) {
				LOGGER.warn("Algorithm description is not valid: " + algorithmClassName);
				return false;
			}
			algorithmMap.put(algorithmClassName, algorithm);
			LOGGER.info("Algorithm class registered: " + algorithmClassName);
			
			
			if(algorithm.getWellKnownName().length()!=0) {
				algorithmMap.put(algorithm.getWellKnownName(), algorithm);
			}
		}
		catch(ClassNotFoundException e) {
			LOGGER.warn("Could not find algorithm class: " + algorithmClassName, e);
			return false;
		}
		catch(IllegalAccessException e) {
			LOGGER.warn("Access error occured while registering algorithm: " + algorithmClassName);
			return false;
		}
		catch(InstantiationException e) {
			LOGGER.warn("Could not instantiate algorithm: " + algorithmClassName);
			return false;
		}
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

	
	

	

	


	

}
