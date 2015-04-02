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

/**
 * XMLParserFactory. Will be initialized within each Framework. 
 * @author foerster
 *
 */

public class ParserFactory {
	
	public static String PROPERTY_NAME_REGISTERED_PARSERS = "registeredParsers";
	private static ParserFactory factory;
	private static Logger LOGGER = LoggerFactory.getLogger(ParserFactory.class);
	
	private List<IParser> registeredParsers;
	
	/**
	 * This factory provides all available {@link IParser} to WPS.
	 * @param parsers
	 */
	public static void initialize(Map<String, ConfigurationModule> parserMap) {
		if (factory == null) {
			factory = new ParserFactory(parserMap);
		}
		else {
			LOGGER.warn("Factory already initialized");
		}
	}
	
	private ParserFactory(Map<String, ConfigurationModule> parserMap) {
		loadAllParsers(parserMap);

        // FvK: added Property Change Listener support
        // creates listener and register it to the wpsConfig instance.
        org.n52.wps.commons.WPSConfig.getInstance().addPropertyChangeListener(org.n52.wps.commons.WPSConfig.WPSCONFIG_PROPERTY_EVENT_NAME, new PropertyChangeListener() {
            public void propertyChange(
                    final PropertyChangeEvent propertyChangeEvent) {
                LOGGER.info(this.getClass().getName() + ": Received Property Change Event: " + propertyChangeEvent.getPropertyName());
                loadAllParsers(org.n52.wps.commons.WPSConfig.getInstance().getActiveRegisteredParserModules());
            }
        });
	}
    
    private void loadAllParsers(Map<String, ConfigurationModule> parserMap){
        registeredParsers = new ArrayList<IParser>();
		for(String currentParserName : parserMap.keySet()) {
			
			ConfigurationModule currentParser = parserMap.get(currentParserName);
			
//			// remove inactive parser
//			Property[] activeProperties = {};
//			ArrayList<Property> activePars = new ArrayList<Property>();
//			for(int i=0; i<currentParser.getPropertyArray().length; i++){
//				if(currentParser.getPropertyArray()[i].getActive()){
//					activePars.add(currentParser.getPropertyArray()[i]);					
//				}
//			}
//			currentParser.setPropertyArray(activePars.toArray(activeProperties));
			
			String parserClass = "";
			
			if(currentParser instanceof ClassKnowingModule){
				parserClass = ((ClassKnowingModule)currentParser).getClassName();
			}
			
			IParser parser = null;
			try {
				 parser = (IParser) this.getClass().getClassLoader().loadClass(parserClass).newInstance();

			}
			catch (ClassNotFoundException e) {
				LOGGER.error("One of the parsers could not be loaded: " + parserClass, e);
			}
			catch(IllegalAccessException e) {
				LOGGER.error("One of the parsers could not be loaded: " + parserClass, e);
			}
			catch(InstantiationException e) {
				LOGGER.error("One of the parsers could not be loaded: " + parserClass, e);
			}

			if(parser != null) {
				
				LOGGER.info("Parser class registered: " + parserClass);
				registeredParsers.add(parser);
			}
		}
    }

	public static ParserFactory getInstance() {
		if(factory == null){			
			Map<String, ConfigurationModule> parserMap = WPSConfig.getInstance().getConfigurationManager().getConfigurationServices().getActiveConfigurationModulesByCategory(ConfigurationCategory.PARSER);
			initialize(parserMap);
		}
		return factory;
	}
	
	public IParser getParser(String schema, String format, String encoding, Class<?> requiredInputClass) {
		
		// dealing with NULL encoding
		if (encoding == null){
			encoding = IOHandler.DEFAULT_ENCODING;
		}
		
		//first, look if we can find a direct way		
		for(IParser parser : registeredParsers) {
			Class<?>[] supportedClasses = parser.getSupportedDataBindings();
			for(Class<?> clazz : supportedClasses){
				if(clazz.equals(requiredInputClass)) {
					if(parser.isSupportedSchema(schema) &&	parser.isSupportedEncoding(encoding) && parser.isSupportedFormat(format)) {
						LOGGER.info("Matching parser found: " + parser);
						return parser;
					}
				}
			}
		}
		
		//no parser could be found
		//try an indirect way by creating all permutations and look if one matches
		//TODO
		return null;
	}

	public List<IParser> getAllParsers() {
		return registeredParsers;
	}
}
