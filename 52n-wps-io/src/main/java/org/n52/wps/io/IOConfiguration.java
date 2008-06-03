/***************************************************************
 This implementation provides a framework to publish processes to the
web through the  OGC Web Processing Service interface. The framework 
is extensible in terms of processes and data handlers. It is compliant 
to the WPS version 0.4.0 (OGC 05-007r4). 

 Copyright (C) 2006 by con terra GmbH

 Authors: 
	Janne Kovanen, Finnish Geodetic Institute, Finland

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

 Created on: 13.06.2006
 ***************************************************************/
package org.n52.wps.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * This class contains the IO configuration for the WPS.
 * 
 * @author janne
 */
public class IOConfiguration {
	
	private static Logger LOGGER = Logger.getLogger(IOConfiguration.class);
	private static IOConfiguration ioConfiguration;
	private Properties properties;
	
	private IOConfiguration(Properties properties){
		this.properties = properties;
	}
	
	/** Initialization of the IO configuration
	 * 
	 * @return
	 */
	public static boolean initalize() {
		String propertiesPath = IOConfiguration.class.getPackage().getName().replace(".", "/") + "/io.properties";
		InputStream is = IOConfiguration.class.getClassLoader().getResourceAsStream("/" + propertiesPath);
		Properties props = new Properties();
		if(is == null) {
			LOGGER.debug("Could not find IO properties file, inputStream is null.");
		} else {
			try {
				props.load(is);
			} catch(IOException e) {}
		}
		IOConfiguration.initializeAlgorithmMappings(props);
			
		ioConfiguration = new IOConfiguration(props);
		return true;
	}
	
	/** Adds to the given properties file mappings for algorithms to define which parsers and generators they support.
	 * 
	 * @param properties
	 */
	private static void initializeAlgorithmMappings(Properties properties) {
		String propertiesPath = IOConfiguration.class.getPackage().getName().replace(".", "/") 
				+ "/algorithmMappings.properties";
		InputStream is = IOConfiguration.class.getClassLoader().getResourceAsStream("/" + propertiesPath);
		// If the mappings file does not exist, then the additional properties are just not loaded.
		if(is==null) return;
		try {
			properties.load(is);
		} catch(IOException e) {
			return;
		}
	}
	
	public static IOConfiguration getInstance() {
		if(!configurationSuccessful() && !initalize()) {
			LOGGER.warn("Configuration was not initialized correctly");
		}
		return ioConfiguration;
	}
	
	public static boolean configurationSuccessful() {
		return ioConfiguration != null; 
	}
	
	public String getProperty(String propertyName) {
		return properties.getProperty(propertyName);
	}
}