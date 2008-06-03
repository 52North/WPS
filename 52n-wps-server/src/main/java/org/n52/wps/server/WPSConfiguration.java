/***************************************************************
 This implementation provides a framework to publish processes to the
web through the  OGC Web Processing Service interface. The framework 
is extensible in terms of processes and data handlers. It is compliant 
to the WPS version 0.4.0 (OGC 05-007r4). 

 Copyright (C) 2006 by con terra GmbH

 Authors: 
	Theodor Foerster, ITC, Enschede, the Netherlands
	Carsten Priess, Institute for geoinformatics, University of
Muenster, Germany


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
package org.n52.wps.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * This class contains a basic configuration for the WPS.
 * @author foerster
 */
public class WPSConfiguration {
	
	private static Logger LOGGER = Logger.getLogger(WPSConfiguration.class);
	private static WPSConfiguration wpsConfiguration;
	private Properties properties;
	
	private WPSConfiguration(Properties properties){
		this.properties = properties;
	}
	
	public static boolean initalize() {
		String propertiesPath = WPSConfiguration.class.getPackage().getName().replace(".", "/") + "/wps.properties";
		InputStream is = WPSConfiguration.class.getClassLoader().getResourceAsStream("/" + propertiesPath);
		Properties props = new Properties();
		if(is == null) {
			LOGGER.error("Could not find properties file, inputStream is null.");
			return false;
		} try {
			props.load(is);
			initializeAlgorithmMappings(props);	
		} catch(IOException e) {
			return false;
		}
		wpsConfiguration = new WPSConfiguration(props);
		return true;
	}
	
	/** Adds to the given properties file mappings for algorithms to define which parsers and generators they support.
	 * 
	 * @param properties
	 */
	private static void initializeAlgorithmMappings(Properties properties) {
		String propertiesPath = WPSConfiguration.class.getPackage().getName().replace(".", "/") 
				+ "/algorithmMappings.properties";
		InputStream is = WPSConfiguration.class.getClassLoader().getResourceAsStream("/" + propertiesPath);
		// If the mappings file does not exist, then the additional properties are just not loaded.
		if(is==null) return;
		try {
			properties.load(is);
		} catch(IOException e) {
			return;
		}
	}
	
	public static WPSConfiguration getInstance() {
		if(! configurationSuccessful() && ! initalize()) {
			LOGGER.warn("Configuration was not initialized correctly");
		}
		return wpsConfiguration;
	}
	
	public static boolean configurationSuccessful() {
		return wpsConfiguration != null; 
	}
	
	public String getProperty(String propertyName) {
		return properties.getProperty(propertyName);
	}
	
	/**
	 * Tells if a certain propertyName exists.
	 * @param propertyName
	 * @return
	 */
	public boolean exists(String propertyName) {
		String value = properties.getProperty(propertyName);
		if (value == null) {
			return false;
		}
		if (value.equals("")) {
			return false;
		}
		return true;
	}
}