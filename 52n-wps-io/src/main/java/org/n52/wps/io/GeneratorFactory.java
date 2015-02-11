/**
 * ﻿Copyright (C) 2007 - 2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.n52.wps.io;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.n52.wps.commons.WPSConfig;
import org.n52.wps.webapp.api.ClassKnowingModule;
import org.n52.wps.webapp.api.ConfigurationCategory;
import org.n52.wps.webapp.api.ConfigurationModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeneratorFactory {
	
	public static String PROPERTY_NAME_REGISTERED_GENERATORS = "registeredGenerators";
	private static GeneratorFactory factory;
	private static Logger LOGGER = LoggerFactory.getLogger(GeneratorFactory.class);
	
	private List<IGenerator> registeredGenerators;
	
	/**
	 * This factory provides all available {@link Generators} to WPS.
	 * @param generators
	 */
	public static void initialize(Map<String, ConfigurationModule> generatorMap) {
		if (factory == null) {
			factory = new GeneratorFactory(generatorMap);
		}
		else {
			LOGGER.warn("Factory already initialized");
		}
	}
	
	private GeneratorFactory(Map<String, ConfigurationModule> generatorMap) {
		loadAllGenerators(generatorMap);
		
		// FvK: added Property Change Listener support
		// creates listener and register it to the wpsConfig instance.
		org.n52.wps.commons.WPSConfig.getInstance().addPropertyChangeListener(org.n52.wps.commons.WPSConfig.WPSCONFIG_PROPERTY_EVENT_NAME, new PropertyChangeListener() {
			public void propertyChange(
					final PropertyChangeEvent propertyChangeEvent) {
				LOGGER.info(this.getClass().getName() + ": Received Property Change Event: " + propertyChangeEvent.getPropertyName());
				loadAllGenerators(org.n52.wps.commons.WPSConfig.getInstance().getActiveRegisteredGeneratorModules());
			}
		});
	}

    private void loadAllGenerators(Map<String, ConfigurationModule> generatorMap){
        registeredGenerators = new ArrayList<IGenerator>();
		for(String currentGeneratorName : generatorMap.keySet()) {

//			// remove inactive properties
//			Property[] activeProperties = {};
//			ArrayList<Property> activeProps = new ArrayList<Property>();
//			for(int i=0; i< currentGenerator.getPropertyArray().length; i++){
//				if(currentGenerator.getPropertyArray()[i].getActive()){
//					activeProps.add(currentGenerator.getPropertyArray()[i]);
//				}
//			}			
//			currentGenerator.setPropertyArray(activeProps.toArray(activeProperties));
						
			ConfigurationModule currentGenerator = generatorMap.get(currentGeneratorName);
			
			String generatorClass = "";
			
			if(currentGenerator instanceof ClassKnowingModule){
				generatorClass = ((ClassKnowingModule)currentGenerator).getClassName();
			}			
			
			IGenerator generator = null;
			try {
				 generator = (IGenerator) this.getClass().getClassLoader().loadClass(generatorClass).newInstance();
			}
			catch (ClassNotFoundException e) {
				LOGGER.error("One of the generators could not be loaded: " + generatorClass, e);
			}
			catch(IllegalAccessException e) {
				LOGGER.error("One of the generators could not be loaded: " + generatorClass, e);
			}
			catch(InstantiationException e) {
				LOGGER.error("One of the generators could not be loaded: " + generatorClass, e);
			}
			if(generator != null) {
				LOGGER.info("Generator class registered: " + generatorClass);
				registeredGenerators.add(generator);
			}
		}
    }

	public static GeneratorFactory getInstance() {
		if(factory == null){
			Map<String, ConfigurationModule> generatorMap = WPSConfig.getInstance().getConfigurationManager().getConfigurationServices().getActiveConfigurationModulesByCategory(ConfigurationCategory.GENERATOR);
			initialize(generatorMap);
		}
		return factory;
	}
	
	public IGenerator getGenerator(String schema, String format, String encoding, Class<?> outputInternalClass) {
		
		// dealing with NULL encoding
		if (encoding == null){
			encoding = IOHandler.DEFAULT_ENCODING;
		}
		
		for(IGenerator generator : registeredGenerators) {
			Class<?>[] supportedBindings = generator.getSupportedDataBindings();
			for(Class<?> clazz : supportedBindings){
				if(clazz.equals(outputInternalClass)) {
					if(generator.isSupportedSchema(schema) && generator.isSupportedEncoding(encoding) && generator.isSupportedFormat(format)){
						return generator;
					}
				}
			}
		}
		//TODO: try a chaining approach, by calculation all permutations and look for matches.
		return null;
	}

	public List<IGenerator> getAllGenerators() {
		return registeredGenerators;
	}

	
	
}
