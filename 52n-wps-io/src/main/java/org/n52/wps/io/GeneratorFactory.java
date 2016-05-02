/**
 * ﻿Copyright (C) 2007 - 2016 52°North Initiative for Geospatial Open Source
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

import org.n52.wps.GeneratorDocument.Generator;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeneratorFactory {
	
	public static String PROPERTY_NAME_REGISTERED_GENERATORS = "registeredGenerators";
	private static GeneratorFactory factory;
	private static Logger LOGGER = LoggerFactory.getLogger(GeneratorFactory.class);
	
	private List<IGenerator> registeredGenerators;

	/**
	 * This factory provides all available {@link AbstractXMLGenerator} to WPS.
	 * @param generators
	 */
	public static void initialize(Generator[] generators) {
		if (factory == null) {
			factory = new GeneratorFactory(generators);
		}
		else {
			LOGGER.warn("Factory already initialized");
		}
	}
	
	private GeneratorFactory(Generator[] generators) {
		loadAllGenerators(generators);

        // FvK: added Property Change Listener support
        // creates listener and register it to the wpsConfig instance.
        org.n52.wps.commons.WPSConfig.getInstance().addPropertyChangeListener(org.n52.wps.commons.WPSConfig.WPSCONFIG_PROPERTY_EVENT_NAME, new PropertyChangeListener() {
            public void propertyChange(
                    final PropertyChangeEvent propertyChangeEvent) {
                LOGGER.info(this.getClass().getName() + ": Received Property Change Event: " + propertyChangeEvent.getPropertyName());
                loadAllGenerators(org.n52.wps.commons.WPSConfig.getInstance().getActiveRegisteredGenerator());
            }
        });
	}

    private void loadAllGenerators(Generator[] generators){
        registeredGenerators = new ArrayList<IGenerator>();
		for(Generator currentGenerator : generators) {

			// remove inactive properties
			Property[] activeProperties = {};
			ArrayList<Property> activeProps = new ArrayList<Property>();
			for(int i=0; i< currentGenerator.getPropertyArray().length; i++){
				if(currentGenerator.getPropertyArray()[i].getActive()){
					activeProps.add(currentGenerator.getPropertyArray()[i]);
				}
			}			
			currentGenerator.setPropertyArray(activeProps.toArray(activeProperties));
			
			IGenerator generator = null;
			String generatorClass = currentGenerator.getClassName();
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
			Generator[] generators = WPSConfig.getInstance().getActiveRegisteredGenerator();
			initialize(generators);
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
