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

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

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
    
	public static WPSConfig getInstance()
	{
		if (wpsConfig == null)
		{
			try
			{
				return getInstance(getConfigPath());
			}
			catch (IOException e)
			{
				LOGGER.error("Failed to initialize WPS. Reason: " + e.getMessage());
				throw new RuntimeException("Failed to initialize WPS. Reason: " + e.getMessage());
			}
		}
		return wpsConfig;
	}
	
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
	public static String getConfigPath() throws IOException {
		String domain = WPSConfig.class.getProtectionDomain().getCodeSource().getLocation().getFile();
		//truncate
		int index = domain.indexOf("WEB-INF");
		if(index<0){
			//try to load from classpath
			URL configPath = WPSConfig.class.getClassLoader().getResource("wps_config.xml");
			if(configPath==null){
				throw new IOException("Could not find wps_config.xml");
			}else{
				return configPath.getFile();
			}
			
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
		return (Property[]) Array.newInstance(Property.class,0);
		
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
