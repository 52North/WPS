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

package org.n52.wps.io;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.n52.wps.ParserDocument.Parser;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;

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
	public static void initialize(Parser[] parsers) {
		if (factory == null) {
			factory = new ParserFactory(parsers);
		}
		else {
			LOGGER.warn("Factory already initialized");
		}
	}
	
	private ParserFactory(Parser[] parsers) {
		loadAllParsers(parsers);

        // FvK: added Property Change Listener support
        // creates listener and register it to the wpsConfig instance.
        org.n52.wps.commons.WPSConfig.getInstance().addPropertyChangeListener(org.n52.wps.commons.WPSConfig.WPSCONFIG_PROPERTY_EVENT_NAME, new PropertyChangeListener() {
            public void propertyChange(
                    final PropertyChangeEvent propertyChangeEvent) {
                LOGGER.info(this.getClass().getName() + ": Received Property Change Event: " + propertyChangeEvent.getPropertyName());
                loadAllParsers(org.n52.wps.commons.WPSConfig.getInstance().getActiveRegisteredParser());
            }
        });
	}

    private void loadAllParsers(Parser[] parsers){
        registeredParsers = new ArrayList<IParser>();
		for(Parser currentParser : parsers) {
			
			// remove inactive parser
			Property[] activeProperties = {};
			ArrayList<Property> activePars = new ArrayList<Property>();
			for(int i=0; i<currentParser.getPropertyArray().length; i++){
				if(currentParser.getPropertyArray()[i].getActive()){
					activePars.add(currentParser.getPropertyArray()[i]);					
				}
			}
			currentParser.setPropertyArray(activePars.toArray(activeProperties));
			
			String parserClass = currentParser.getClassName();
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
			Parser[] parsers = WPSConfig.getInstance().getActiveRegisteredParser();
			initialize(parsers);
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
