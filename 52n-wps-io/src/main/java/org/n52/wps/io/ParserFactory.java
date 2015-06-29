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

/**
 * XMLParserFactory. Will be initialized within each Framework. 
 * @author foerster
 *
 */

public class ParserFactory implements Constructable{
	
	public static String PROPERTY_NAME_REGISTERED_PARSERS = "registeredParsers";
	private static ParserFactory factory;
	private static Logger LOGGER = LoggerFactory.getLogger(ParserFactory.class);
	
	private List<IParser> registeredParsers;
	
	@Inject
	private WPSConfig wpsConfig;
	
	public void init() {
		loadAllParsers(wpsConfig.getActiveRegisteredParserModules());
	}
    
    private void loadAllParsers(Map<String, ConfigurationModule> parserMap){
        registeredParsers = new ArrayList<IParser>();
		for(String currentParserName : parserMap.keySet()) {
			
			ConfigurationModule currentParser = parserMap.get(currentParserName);
			
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
