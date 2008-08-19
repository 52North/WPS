/***************************************************************
 This implementation provides a framework to publish processes to the
 web through the  OGC Web Processing Service interface. The framework 
 is extensible in terms of processes and data handlers. It is compliant 
 to the WPS version 0.4.0 (OGC 05-007r4). 

 Copyright (C) 2007 by con terra GmbH

 Authors:
 	Bastian Schaeffer, Institute for geoinformatics, University of Muenster, Germany
	

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

 ***************************************************************/

package org.n52.wps.commons;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.URL;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;

import org.n52.wps.WPSConfigurationDocument;
import org.n52.wps.GeneratorDocument.Generator;
import org.n52.wps.ParserDocument.Parser;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.RepositoryDocument.Repository;
import org.n52.wps.impl.WPSConfigurationDocumentImpl.WPSConfigurationImpl;


public class WPSConfig {
	private static WPSConfig wpsConfig;
	private static WPSConfigurationImpl wpsConfigXMLBeans;
	
	private static Logger LOGGER = Logger.getLogger(WPSConfig.class);
	
		
	private WPSConfig(String wpsConfigPath) throws XmlException, IOException {
		wpsConfigXMLBeans= (WPSConfigurationImpl) WPSConfigurationDocument.Factory.parse(new File(wpsConfigPath)).getWPSConfiguration();
		
	}
	
	private WPSConfig(InputStream resourceAsStream) throws XmlException, IOException {
		wpsConfigXMLBeans = (WPSConfigurationImpl) WPSConfigurationDocument.Factory.parse(resourceAsStream).getWPSConfiguration();
	}

	public static void forceInitialization(String configPath) throws XmlException, IOException{
		wpsConfig = new WPSConfig(configPath);
	}

	public static WPSConfig getInstance() {
		if(wpsConfig==null){
			try {
				wpsConfig = new WPSConfig(getConfigPath());
			} catch (XmlException e) {
				LOGGER.error("Failed to initialize WPS. Reason: " + e.getMessage());
				throw new RuntimeException("Failed to initialize WPS. Reason: " + e.getMessage());
			} catch (IOException e) {
				LOGGER.error("Failed to initialize WPS. Reason: " + e.getMessage());
				throw new RuntimeException("Failed to initialize WPS. Reason: " + e.getMessage());
			}
		}
		return wpsConfig;
	}
	/**
	 * This method retrieves the full path for the file (wps_config.xml), searching in WEB-INF/config. This is only applicable for webapp applications. To customize this, please use directly {@link WPSConfig#forceInitialization(String)} and then getInstance().
	 * @return
	 * @throws IOException
	 */
	public static String getConfigPath() throws IOException {
		String domain = WPSConfig.class.getProtectionDomain().getCodeSource().getLocation().getFile();
		//truncate
		int index = domain.indexOf("WEB-INF");
		if(index<0){
			throw new IOException("Could not find wps_config.xml");
		}
		String substring = domain.substring(0,index);
		if(!substring.endsWith("/")){
			substring = substring + "/";
		}
		substring = substring + "config/wps_config.xml";
		
		return substring;
	}

	public WPSConfigurationImpl getWPSConfig(){
		return wpsConfigXMLBeans;
	}
	
	public Parser[] getRegisteredParser(){
		return wpsConfigXMLBeans.getDatahandlers().getParserList().getParserArray();
		
	}
	
	public Generator[] getRegisteredGenerators(){
		return wpsConfigXMLBeans.getDatahandlers().getGeneratorList().getGeneratorArray();
		
	}
	
	public Repository[] getRegisterdAlgorithmRepositories(){
		return wpsConfigXMLBeans.getAlgorithmRepositoryList().getRepositoryArray();
			
		
	}
	
	public Property[] getPropertiesForGeneratorClass(String className){
		Generator[] generators = wpsConfigXMLBeans.getDatahandlers().getGeneratorList().getGeneratorArray();
		for(int i = 0; i<generators.length; i++) {
			Generator generator = generators[i];
			if(generator.getClassName().equals(className)){
				return generator.getPropertyArray();
			}
		}
		return (Property[]) Array.newInstance(Property.Factory.class,0);
		
	}
	
	public Property[] getPropertiesForParserClass(String className){
		Parser[] parsers = wpsConfigXMLBeans.getDatahandlers().getParserList().getParserArray();
		for(int i = 0; i<parsers.length; i++) {
			Parser parser = parsers[i];
			if(parser.getClassName().equals(className)){
				return parser.getPropertyArray();
			}
		}
		return (Property[]) Array.newInstance(Property.Factory.class,0);
		
	}
	
	public Property[] getPropertiesForRepositoryClass(String className){
		Repository[] repositories = getRegisterdAlgorithmRepositories();
		for(int i = 0; i<repositories.length; i++) {
			Repository repository = repositories[i];
			if(repository.getClassName().equals(className)){
				return repository.getPropertyArray();
			}
		}
		
		return (Property[]) Array.newInstance(Property.Factory.class,0);
	}
	
	public Property getPropertyForKey(Property[] properties, String key){
		for(Property property: properties){
			if(property.getName().equalsIgnoreCase(key)){
				return property;
			}
		}
		return null;
	}
	
}
