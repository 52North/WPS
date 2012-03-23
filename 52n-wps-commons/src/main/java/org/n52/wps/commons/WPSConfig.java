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

// FvK: added Property Change support
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringBufferInputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import org.n52.wps.FormatDocument.Format;
import org.n52.wps.WPSConfigurationDocument;
import org.n52.wps.GeneratorDocument.Generator;
import org.n52.wps.ParserDocument.Parser;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.RepositoryDocument.Repository;
import org.n52.wps.impl.WPSConfigurationDocumentImpl.WPSConfigurationImpl;


public class WPSConfig  implements Serializable {
	private static transient WPSConfig wpsConfig;
	private static transient WPSConfigurationImpl wpsConfigXMLBeans;

	private static transient Logger LOGGER = Logger.getLogger(WPSConfig.class);
    
    // FvK: added Property Change support
    protected final PropertyChangeSupport propertyChangeSupport;
    // constants for the Property change event names
    public static final String WPSCONFIG_PROPERTY_EVENT_NAME = "WPSConfigUpdate";
	public static final String WPSCAPABILITIES_SKELETON_PROPERTY_EVENT_NAME = "WPSCapabilitiesUpdate";

	private WPSConfig(String wpsConfigPath) throws XmlException, IOException {
		wpsConfigXMLBeans= (WPSConfigurationImpl) WPSConfigurationDocument.Factory.parse(new File(wpsConfigPath)).getWPSConfiguration();
		
        // FvK: added Property Change support
        propertyChangeSupport=new PropertyChangeSupport(this);
	}
	
	private WPSConfig(InputStream resourceAsStream) throws XmlException, IOException {
		wpsConfigXMLBeans = (WPSConfigurationImpl) WPSConfigurationDocument.Factory.parse(resourceAsStream).getWPSConfiguration();

        // FvK: added Property Change support
        propertyChangeSupport=new PropertyChangeSupport(this);
	}


    /**
     * Add an Listener to the wpsConfig
     * @param propertyName
     * @param listener
     */
    public void addPropertyChangeListener(final String propertyName,
            final PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName,listener);
    }

    /**
     * remove a listener from the wpsConfig
     * @param propertyName
     * @param listener
     */
    public void removePropertyChangeListener(final String propertyName,
            final PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(propertyName,listener);
    }

    //For Testing purpose only
    public void notifyListeners(){
        propertyChangeSupport.firePropertyChange(WPSCONFIG_PROPERTY_EVENT_NAME, null,null);
    }

	private synchronized void writeObject(java.io.ObjectOutputStream oos) throws IOException
	{
		oos.writeObject(wpsConfigXMLBeans.xmlText());
	}
	
	private synchronized void readObject(java.io.ObjectInputStream oos) throws IOException, ClassNotFoundException
	{		
		try
		{
			String wpsConfigXMLBeansAsXml = (String) oos.readObject();
			XmlObject configXmlObject = XmlObject.Factory.parse(wpsConfigXMLBeansAsXml);
			WPSConfigurationDocument configurationDocument = WPSConfigurationDocument.Factory.newInstance();
			configurationDocument.addNewWPSConfiguration().set(configXmlObject);
			wpsConfig = new WPSConfig(new StringBufferInputStream(configurationDocument.xmlText()));
		}
		catch (XmlException e)
		{
			LOGGER.error(e.getMessage());
			throw new IOException(e.getMessage());
		}
	}
	
	/**
	 * WPSConfig is a singleton. If there is a need for reinitialization, use this path.
	 * @param configPathp path to the wps_config.xml
	 * @throws XmlException
	 * @throws IOException
	 */
	public static void forceInitialization(String configPath) throws XmlException, IOException{
		// temporary save all registered listeners
        PropertyChangeListener[] listeners = {};
        if (wpsConfig != null){
            listeners = wpsConfig.propertyChangeSupport.getPropertyChangeListeners();
        }
        wpsConfig = new WPSConfig(configPath);
        
        //register all saved listeners to new wpsConfig Instance
        //reversed order to keep original order of the registration!!!
        for (int i=listeners.length-1;i>=0;i--){
            wpsConfig.propertyChangeSupport.addPropertyChangeListener(listeners[i]);
        }

        // fire event
        wpsConfig.propertyChangeSupport.firePropertyChange(WPSCONFIG_PROPERTY_EVENT_NAME, null,wpsConfig);
        LOGGER.info("Configuration Reloaded, Listeners informed");
	}
	
	/**
	 * WPSConfig is a singleton. If there is a need for reinitialization, use this path.
	 * @param stream stream containing the wps_config.xml
	 * @throws XmlException
	 * @throws IOException
	 */
	public static void forceInitialization(InputStream stream) throws XmlException, IOException {
        // temporary save all registered listeners
        PropertyChangeListener[] listeners = {};
        if (wpsConfig != null){
            listeners = wpsConfig.propertyChangeSupport.getPropertyChangeListeners();
        }

		wpsConfig = new WPSConfig(stream);

        //register all saved listeners to new wpsConfig Instance
        //reversed order to keep original order of the registration!!!
        for (int i=listeners.length-1;i>=0;i--){
            wpsConfig.propertyChangeSupport.addPropertyChangeListener(listeners[i]);
        }

        // fire event
        wpsConfig.propertyChangeSupport.firePropertyChange(WPSCONFIG_PROPERTY_EVENT_NAME, null,wpsConfig);
        LOGGER.info("Configuration Reloaded, Listeners informed");
	}
    
	/**
	 * returns an instance of the WPSConfig class. WPSCofnig is a single. If there is need for reinstantitation, use forceInitialization().
	 * @return WPSConfig object representing the wps_config.xml from the classpath or webapps folder
	 */
	public static WPSConfig getInstance()
	{
		if (wpsConfig == null)
		{
			
			return getInstance(getConfigPath());
			
		}
		return wpsConfig;
	}
	
	/**
	 * returns an instance of the WPSConfig class. WPSCofnig is a single. If there is need for reinstantitation, use forceInitialization().
	 * @param path path to the wps_config.xml
	 * @return WPSConfig object representing the wps_config.xml from the given path
	 */
	public static WPSConfig getInstance(String path)
	{
		if (wpsConfig == null)
		{
			try
			{
				wpsConfig = new WPSConfig(path);
			}
			catch (XmlException e)
			{
				LOGGER.error("Failed to initialize WPS. Reason: " + e.getMessage());
				throw new RuntimeException("Failed to initialize WPS. Reason: " + e.getMessage());
			}
			catch (IOException e)
			{
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
	public static String getConfigPath() {
		
		String configPath = tryToGetPathFromClassPath();
		if(configPath!=null){
			return configPath;
		}
		configPath = tryToGetPathFromWebAppTarget();
		if(configPath!=null){
			return configPath;
		}
		configPath = tryToGetPathFromWebAppSource();
		if(configPath!=null){
			return configPath;
		}
		configPath = tryToGetPathViaWebAppPath();
		if(configPath!=null){
			return configPath;
		}
		configPath = tryToGetPathLastResort();
		if(configPath!=null){
			return configPath;
		}
		
		throw new RuntimeException("Could find and load wps_config.xml");
	}
		
	public static String tryToGetPathFromClassPath(){
		URL configPathURL = WPSConfig.class.getClassLoader().getResource("wps_config.xml");
		if(configPathURL!=null){
			String config = configPathURL.getFile();
			config = URLDecoder.decode(config);
			return config;
		}
		return null;
	}
	
	public static String tryToGetPathFromWebAppTarget(){
		String domain = WPSConfig.class.getProtectionDomain().getCodeSource().getLocation().getFile();
		int index1 = domain.indexOf("52n-wps-parent");
		if(index1>0){
			//try to load from classpath
			String path = URLDecoder.decode(domain.substring(0,index1+14));
			path = path + File.separator+"52n-wps-webapp"+File.separator+"target";
			File f = new File(path);
			String[] dirs = f.getAbsoluteFile().list();
			if(dirs!=null){
				for(String dir : dirs){
					if(dir.startsWith("52n-wps-webapp") && !dir.endsWith(".war")){
						path = path+File.separator+dir+File.separator+ "config/wps_config.xml";
					}
				}					
				return path;
			}
		}
		return null;
	}
	
	public static String tryToGetPathFromWebAppSource(){
		String domain = WPSConfig.class.getProtectionDomain().getCodeSource().getLocation().getFile();
		int index1 = domain.indexOf("52n-wps-parent");
		if(index1>0){
			//try to load from classpath
			String path = URLDecoder.decode(domain.substring(0,index1+14));
			path = path + File.separator+"52n-wps-webapp";
			File f = new File(path);
			String[] dirs = f.getAbsoluteFile().list();
			if(dirs!=null){
				for(String dir : dirs){
					if(dir.equals("src") ){
						path = path+File.separator+dir+File.separator+"main"+File.separator+"webapp"+File.separator+"config"+File.separator+"wps_config.xml";
					}
				}
				if(!(new File(path)).exists()){
					return null;
				}
				return path;
			}
		}
		return null;
	}
	
	public static String tryToGetPathViaWebAppPath(){
		String domain = WPSConfig.class.getProtectionDomain().getCodeSource().getLocation().getFile();
		int index = domain.indexOf("WEB-INF");
		if(index>0){
			String substring = domain.substring(0,index);
			if(!substring.endsWith("/")){
				substring = substring + "/";
			}
			substring = substring + "config/wps_config.xml";
			
			return substring;
		}
		return null;
	}
	
	public static String tryToGetPathLastResort(){
		String domain = WPSConfig.class.getProtectionDomain().getCodeSource().getLocation().getFile();
		/*
		 * domain should always be 52n-wps-commons/target/classes
		 * so we just go three directories up
		 */		
		File classDir = new File(domain);
		
		File projectRoot = classDir.getParentFile().getParentFile().getParentFile();

		String path = projectRoot.getAbsolutePath(); 
		
		String[] dirs = projectRoot.getAbsoluteFile().list();
		for(String dir : dirs){
			if(dir.startsWith("52n-wps-webapp") && !dir.endsWith(".war")){
				path = path+File.separator+dir+File.separator+ "src"+File.separator+"main"+File.separator+"webapp"+File.separator+"config"+File.separator+"wps_config.xml";
			}
		}
		LOGGER.info(path);
		return path;
	}
		

	public WPSConfigurationImpl getWPSConfig(){
		return wpsConfigXMLBeans;
	}
	
	public Parser[] getRegisteredParser(){
		return wpsConfigXMLBeans.getDatahandlers().getParserList().getParserArray();		
	}
	
	public Parser[] getActiveRegisteredParser(){
		Parser[] parsers = getRegisteredParser();
		ArrayList<Parser> activeParsers = new ArrayList<Parser>();
		for(int i=0; i<parsers.length; i++){
			if(parsers[i].getActive()){
				activeParsers.add(parsers[i]);
			}
		}		
		Parser[] parArr = {};
		return activeParsers.toArray(parArr);
	}
	
	public Generator[] getRegisteredGenerator(){
		return wpsConfigXMLBeans.getDatahandlers().getGeneratorList().getGeneratorArray();
	}
	
	public Generator[] getActiveRegisteredGenerator(){
		Generator[] generators = getRegisteredGenerator();
		ArrayList<Generator> activeGenerators = new ArrayList<Generator>(); 
		for(int i=0; i<generators.length; i++){
			if(generators[i].getActive()){
				activeGenerators.add(generators[i]);
			}
		}			
		Generator[] genArr = {};
		return activeGenerators.toArray(genArr);
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
		return (Property[]) Array.newInstance(Property.class,0);
		
	}
	
	public Format[] getFormatsForGeneratorClass(String className){
		Generator[] generators = wpsConfigXMLBeans.getDatahandlers().getGeneratorList().getGeneratorArray();
		for(int i = 0; i<generators.length; i++) {
			Generator generator = generators[i];
			if(generator.getClassName().equals(className)){
				return generator.getFormatArray();
			}
		}
		return (Format[]) Array.newInstance(Format.class,0);
		
	}
	
	public Property[] getPropertiesForParserClass(String className){
		Parser[] parsers = wpsConfigXMLBeans.getDatahandlers().getParserList().getParserArray();
		for(int i = 0; i<parsers.length; i++) {
			Parser parser = parsers[i];
			if(parser.getClassName().equals(className)){
				return parser.getPropertyArray();
			}
		}
		return (Property[]) Array.newInstance(Property.class,0);
		
	}
	
	public Format[] getFormatsForParserClass(String className){
		Parser[] parsers = wpsConfigXMLBeans.getDatahandlers().getParserList().getParserArray();
		for(int i = 0; i<parsers.length; i++) {
			Parser parser = parsers[i];
			if(parser.getClassName().equals(className)){
				return parser.getFormatArray();
			}
		}
		return (Format[]) Array.newInstance(Format.class,0);
		
	}
	
	public boolean isParserActive(String className){
		Parser[] activeParser = getActiveRegisteredParser();
		for(int i = 0; i<activeParser.length; i++) {
			Parser parser = activeParser[i];
			if(parser.getClassName().equals(className)){
				return parser.getActive();
			}
		}
		return false;
	}
	
	public boolean isGeneratorActive(String className){
		Generator[] generators = getActiveRegisteredGenerator();
		for(int i = 0; i<generators.length; i++) {
			Generator generator = generators[i];
			if(generator.getClassName().equals(className)){
				return generator.getActive();
			}
		}
		return false;
	}
	
	public boolean isRepositoryActive(String className){
		Repository[] repositories = getRegisterdAlgorithmRepositories();
		for(int i = 0; i<repositories.length; i++) {
			Repository repository = repositories[i];
			if(repository.getClassName().equals(className)){
				return repository.getActive();
			}
		}

		return false;
	}
	
	public Property[] getPropertiesForRepositoryClass(String className){
		Repository[] repositories = getRegisterdAlgorithmRepositories();
		for(int i = 0; i<repositories.length; i++) {
			Repository repository = repositories[i];
			if(repository.getClassName().equals(className)){
				return repository.getPropertyArray();
			}
		}
		
		return (Property[]) Array.newInstance(Property.class,0);
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
