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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.n52.iceland.lifecycle.Constructable;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.webapp.api.ClassKnowingModule;
import org.n52.wps.webapp.api.ConfigurationModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeneratorFactory implements Constructable{
	
	public static String PROPERTY_NAME_REGISTERED_GENERATORS = "registeredGenerators";
	private static GeneratorFactory factory;
	private static Logger LOGGER = LoggerFactory.getLogger(GeneratorFactory.class);
	
	private List<IGenerator> registeredGenerators;
	
	@Inject
	private WPSConfig wpsConfig;
	
	public void init() {
		loadAllGenerators(wpsConfig.getActiveRegisteredGeneratorModules());
	}

    private void loadAllGenerators(Map<String, ConfigurationModule> generatorMap){
        registeredGenerators = new ArrayList<IGenerator>();
		for(String currentGeneratorName : generatorMap.keySet()) {
						
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
