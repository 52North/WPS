/**
 * ﻿Copyright (C) 2006
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

package org.n52.wps.commons;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;

import javax.servlet.ServletConfig;

import org.apache.xmlbeans.XmlException;
import org.n52.wps.FormatDocument.Format;
import org.n52.wps.GeneratorDocument.Generator;
import org.n52.wps.ParserDocument.Parser;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.RepositoryDocument.Repository;
import org.n52.wps.WPSConfigurationDocument;
import org.n52.wps.impl.WPSConfigurationDocumentImpl.WPSConfigurationImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WPSConfig implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 3198223084611936675L;
    private static transient WPSConfig wpsConfig;
    private static transient WPSConfigurationImpl wpsConfigXMLBeans;

    private static transient Logger LOGGER = LoggerFactory.getLogger(WPSConfig.class);

    // FvK: added Property Change support
    protected final PropertyChangeSupport propertyChangeSupport;
    // constants for the Property change event names
    public static final String WPSCONFIG_PROPERTY_EVENT_NAME = "WPSConfigUpdate";
    public static final String WPSCAPABILITIES_SKELETON_PROPERTY_EVENT_NAME = "WPSCapabilitiesUpdate";

    public static final String CONFIG_FILE_NAME = "wps_config.xml";
    private static final String CONFIG_FILE_DIR = "config";
    private static final String URL_DECODE_ENCODING = "UTF-8";

    private WPSConfig(String wpsConfigPath) throws XmlException, IOException {
        wpsConfigXMLBeans = (WPSConfigurationImpl) WPSConfigurationDocument.Factory.parse(new File(wpsConfigPath)).getWPSConfiguration();

        // FvK: added Property Change support
        this.propertyChangeSupport = new PropertyChangeSupport(this);
    }

    private WPSConfig(InputStream resourceAsStream) throws XmlException, IOException {
        wpsConfigXMLBeans = (WPSConfigurationImpl) WPSConfigurationDocument.Factory.parse(resourceAsStream).getWPSConfiguration();

        // FvK: added Property Change support
        this.propertyChangeSupport = new PropertyChangeSupport(this);
    }

    /**
     * Add an Listener to the wpsConfig
     * 
     * @param propertyName
     * @param listener
     */
    public void addPropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
        this.propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * remove a listener from the wpsConfig
     * 
     * @param propertyName
     * @param listener
     */
    public void removePropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
        this.propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
    }

    // For Testing purpose only
    public void notifyListeners() {
        this.propertyChangeSupport.firePropertyChange(WPSCONFIG_PROPERTY_EVENT_NAME, null, null);
    }

    public void firePropertyChange(String event) {
    	propertyChangeSupport.firePropertyChange(event, null, null);
    }
    
    // private synchronized static void writeObject(java.io.ObjectOutputStream oos) throws IOException {
    // oos.writeObject(wpsConfigXMLBeans.xmlText());
    // }
    //
    // private synchronized static void readObject(java.io.ObjectInputStream oos) throws IOException,
    // ClassNotFoundException {
    // try {
    // String wpsConfigXMLBeansAsXml = (String) oos.readObject();
    // XmlObject configXmlObject = XmlObject.Factory.parse(wpsConfigXMLBeansAsXml);
    // WPSConfigurationDocument configurationDocument = WPSConfigurationDocument.Factory.newInstance();
    // configurationDocument.addNewWPSConfiguration().set(configXmlObject);
    // wpsConfig = new WPSConfig(new ByteArrayInputStream(configurationDocument.xmlText().getBytes()));
    // }
    // catch (XmlException e) {
    // LOGGER.error(e.getMessage());
    // throw new IOException(e.getMessage());
    // }
    // }

    /**
     * WPSConfig is a singleton. If there is a need for reinitialization, use this path.
     * 
     * @param configPathp
     *        path to the wps_config.xml
     * @throws XmlException
     * @throws IOException
     */
    public static void forceInitialization(String configPath) throws XmlException, IOException {
        // temporary save all registered listeners
        PropertyChangeListener[] listeners = {};
        if (wpsConfig != null) {
            listeners = wpsConfig.propertyChangeSupport.getPropertyChangeListeners();
        }
        wpsConfig = new WPSConfig(configPath);

        // register all saved listeners to new wpsConfig Instance
        // reversed order to keep original order of the registration!!!
        for (int i = listeners.length - 1; i >= 0; i--) {
            wpsConfig.propertyChangeSupport.addPropertyChangeListener(listeners[i]);
        }

        // fire event
        wpsConfig.propertyChangeSupport.firePropertyChange(WPSCONFIG_PROPERTY_EVENT_NAME, null, wpsConfig);
        LOGGER.info("Configuration Reloaded, Listeners informed");
    }

    /**
     * WPSConfig is a singleton. If there is a need for reinitialization, use this path.
     * 
     * @param stream
     *        stream containing the wps_config.xml
     * @throws XmlException
     * @throws IOException
     */
    public static void forceInitialization(InputStream stream) throws XmlException, IOException {
        // temporary save all registered listeners
        PropertyChangeListener[] listeners = {};
        if (wpsConfig != null) {
            listeners = wpsConfig.propertyChangeSupport.getPropertyChangeListeners();
        }

        wpsConfig = new WPSConfig(stream);

        // register all saved listeners to new wpsConfig Instance
        // reversed order to keep original order of the registration!!!
        for (int i = listeners.length - 1; i >= 0; i--) {
            wpsConfig.propertyChangeSupport.addPropertyChangeListener(listeners[i]);
        }

        // fire event
        wpsConfig.propertyChangeSupport.firePropertyChange(WPSCONFIG_PROPERTY_EVENT_NAME, null, wpsConfig);
        LOGGER.info("Configuration Reloaded, Listeners informed");
    }

    /**
     * returns an instance of the WPSConfig class. WPSConfig is a single. If there is need for
     * reinstantitation, use forceInitialization().
     * 
     * @return WPSConfig object representing the wps_config.xml from the classpath or webapps folder
     */
    public static WPSConfig getInstance() {
        // if (LOGGER.isDebugEnabled())
        // LOGGER.debug("Getting WPSConfig instance... without input.");

        if (wpsConfig == null) {
            String path = getConfigPath();
            WPSConfig config = getInstance(path);
            wpsConfig = config;
        }

        return wpsConfig;
    }

    /**
     * returns an instance of the WPSConfig class. WPSCofnig is a single. If there is need for
     * reinstantitation, use forceInitialization().
     * 
     * @param path
     *        path to the wps_config.xml
     * @return WPSConfig object representing the wps_config.xml from the given path
     */
    public static WPSConfig getInstance(String path) {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Getting WPSConfig instance... from path: " + path);

        if (wpsConfig == null) {
            try {
                wpsConfig = new WPSConfig(path);
            }
            catch (XmlException e) {
                LOGGER.error("Failed to initialize WPS. Reason: " + e.getMessage());
                throw new RuntimeException("Failed to initialize WPS. Reason: " + e.getMessage());
            }
            catch (IOException e) {
                LOGGER.error("Failed to initialize WPS. Reason: " + e.getMessage());
                throw new RuntimeException("Failed to initialize WPS. Reason: " + e.getMessage());
            }
        }
        return wpsConfig;
    }

    public static WPSConfig getInstance(ServletConfig config) {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Getting WPSConfig instance... with ServletConfig: " + config.toString());

        String path = getConfigPath(config);

        if (path == null)
            path = getConfigPath();
        else
            LOGGER.debug("Found config file under " + path);

        return getInstance(path);
    }

    public static String getConfigPath(ServletConfig config) {
        return config.getServletContext().getRealPath(CONFIG_FILE_DIR + File.separator + CONFIG_FILE_NAME);
    }

    /**
     * This method retrieves the full path for the file (wps_config.xml), searching in WEB-INF/config. This is
     * only applicable for webapp applications. To customize this, please use directly
     * {@link WPSConfig#forceInitialization(String)} and then getInstance().
     * 
     * @return
     * @throws IOException
     */
    public static String getConfigPath() {
        String configPath = tryToGetPathFromClassPath();
        File file = null;
        if (configPath != null) {
            file = new File(configPath);
            if (file.exists()) {
                return configPath;
            }
        }

        configPath = tryToGetPathFromWebAppTarget();
        if (configPath != null) {
            file = new File(configPath);
            if (configPath != null && file.exists()) {
                return configPath;
            }
        }

        configPath = tryToGetPathFromWebAppSource();
        if (configPath != null) {
            file = new File(configPath);
            if (configPath != null && file.exists()) {
                return configPath;
            }
        }

        configPath = tryToGetPathViaWebAppPath();
        if (configPath != null) {
            file = new File(configPath);
            if (configPath != null && file.exists()) {
                return configPath;
            }
        }

        configPath = tryToGetPathLastResort();
        if (configPath != null) {
            file = new File(configPath);
            if (configPath != null && file.exists()) {
                return configPath;
            }
        }

        throw new RuntimeException("Could not find and load wps_config.xml");
    }

    public static String tryToGetPathFromClassPath() {
        URL configPathURL = WPSConfig.class.getClassLoader().getResource(CONFIG_FILE_NAME);
        if (configPathURL != null) {
            String config = configPathURL.getFile();
            try {
                config = URLDecoder.decode(config, URL_DECODE_ENCODING);
            }
            catch (UnsupportedEncodingException e) {
                LOGGER.error("Could not devode URL to get config from class path.", e);
                return null;
            }
            return config;
        }
        return null;
    }

    public static String tryToGetPathFromWebAppTarget() {
        String domain = WPSConfig.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        int index1 = domain.indexOf("52n-wps-parent");
        if (index1 > 0) {
            // try to load from classpath
            String ds = domain.substring(0, index1 + 14);
            String path;
            try {
                path = URLDecoder.decode(ds, URL_DECODE_ENCODING);
            }
            catch (UnsupportedEncodingException e) {
                LOGGER.error("could not decode URL", e);
                return null;
            }
            
            path = path + File.separator + "52n-wps-webapp" + File.separator + "target";
            File f = new File(path);
            String[] dirs = f.getAbsoluteFile().list();
            if (dirs != null) {
                for (String dir : dirs) {
                    if (dir.startsWith("52n-wps-webapp") && !dir.endsWith(".war")) {
                        path = path + File.separator + dir + File.separator + CONFIG_FILE_DIR + "/" + CONFIG_FILE_NAME;
                    }
                }
                return path;
            }
        }
        return null;

    }

    public static String tryToGetPathFromWebAppSource() {
        String domain = WPSConfig.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        int index1 = domain.indexOf("52n-wps-parent");
        if (index1 > 0) {
            // try to load from classpath
            String ds = domain.substring(0, index1 + 14);
            String path;
            try {
                path = URLDecoder.decode(ds, URL_DECODE_ENCODING);
            }
            catch (UnsupportedEncodingException e) {
                LOGGER.error("could not decode URL", e);
                return null;
            }
            
            path = path + File.separator + "52n-wps-webapp";
            File f = new File(path);
            String[] dirs = f.getAbsoluteFile().list();
            if (dirs != null) {
                for (String dir : dirs) {
                    if (dir.equals("src")) {
                        path = path + File.separator + dir + File.separator + "main" + File.separator + "webapp"
                                + File.separator + CONFIG_FILE_DIR + File.separator + CONFIG_FILE_NAME;
                    }
                }
                if ( ! (new File(path)).exists()) {
                    return null;
                }
                return path;
            }
        }
        return null;
    }

    public static String tryToGetPathViaWebAppPath() {
    	//XXX: any objections against using getResource("/") instead?
//        String domain = WPSConfig.class.getProtectionDomain().getCodeSource().getLocation().getFile();
    	String domain;
		try {
			domain = new File(WPSConfig.class.getResource("/").toURI()).toString();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
        int index = domain.indexOf("WEB-INF");
        if (index > 0) {
            String substring = domain.substring(0, index);
//            if ( !substring.endsWith("/")) {
//                substring = substring + "/";
//            }
//            substring = substring + CONFIG_FILE_DIR + File.separator + CONFIG_FILE_NAME;
            File configDir = new File(new File(substring), CONFIG_FILE_DIR);
            if (configDir.exists() && configDir.isDirectory()) {
            	return new File(configDir, CONFIG_FILE_NAME).getAbsolutePath();
            }
        }
        return null;
    }

    public static String tryToGetPathLastResort() {
        String domain = WPSConfig.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        /*
         * domain should always be 52n-wps-commons/target/classes so we just go three directories up
         */
        File classDir = new File(domain);

        File projectRoot = classDir.getParentFile().getParentFile().getParentFile();

        String path = projectRoot.getAbsolutePath();

        String[] dirs = projectRoot.getAbsoluteFile().list();
        for (String dir : dirs) {
            if (dir.startsWith("52n-wps-webapp") && !dir.endsWith(".war")) {
                path = path + File.separator + dir + File.separator + "src" + File.separator + "main" + File.separator
                        + "webapp" + File.separator + CONFIG_FILE_DIR + File.separator + CONFIG_FILE_NAME;
            }
        }
        LOGGER.info(path);
        return path;
    }

    public WPSConfigurationImpl getWPSConfig() {
        return wpsConfigXMLBeans;
    }

    public Parser[] getRegisteredParser() {
        return wpsConfigXMLBeans.getDatahandlers().getParserList().getParserArray();
    }

    public Parser[] getActiveRegisteredParser() {
        Parser[] parsers = getRegisteredParser();
        ArrayList<Parser> activeParsers = new ArrayList<Parser>();
        for (int i = 0; i < parsers.length; i++) {
            if (parsers[i].getActive()) {
                activeParsers.add(parsers[i]);
            }
        }
        Parser[] parArr = {};
        return activeParsers.toArray(parArr);
    }

    public Generator[] getRegisteredGenerator() {
        return wpsConfigXMLBeans.getDatahandlers().getGeneratorList().getGeneratorArray();
    }

    public Generator[] getActiveRegisteredGenerator() {
        Generator[] generators = getRegisteredGenerator();
        ArrayList<Generator> activeGenerators = new ArrayList<Generator>();
        for (int i = 0; i < generators.length; i++) {
            if (generators[i].getActive()) {
                activeGenerators.add(generators[i]);
            }
        }
        Generator[] genArr = {};
        return activeGenerators.toArray(genArr);
    }

    public Repository[] getRegisterdAlgorithmRepositories() {
        return wpsConfigXMLBeans.getAlgorithmRepositoryList().getRepositoryArray();

    }

    public Property[] getPropertiesForGeneratorClass(String className) {
        Generator[] generators = wpsConfigXMLBeans.getDatahandlers().getGeneratorList().getGeneratorArray();
        for (int i = 0; i < generators.length; i++) {
            Generator generator = generators[i];
            if (generator.getClassName().equals(className)) {
                return generator.getPropertyArray();
            }
        }
        return (Property[]) Array.newInstance(Property.class, 0);

    }

    public Format[] getFormatsForGeneratorClass(String className) {
        Generator[] generators = wpsConfigXMLBeans.getDatahandlers().getGeneratorList().getGeneratorArray();
        for (int i = 0; i < generators.length; i++) {
            Generator generator = generators[i];
            if (generator.getClassName().equals(className)) {
                return generator.getFormatArray();
            }
        }
        return (Format[]) Array.newInstance(Format.class, 0);

    }

    public Property[] getPropertiesForParserClass(String className) {
        Parser[] parsers = wpsConfigXMLBeans.getDatahandlers().getParserList().getParserArray();
        for (int i = 0; i < parsers.length; i++) {
            Parser parser = parsers[i];
            if (parser.getClassName().equals(className)) {
                return parser.getPropertyArray();
            }
        }
        return (Property[]) Array.newInstance(Property.class, 0);

    }

    public Format[] getFormatsForParserClass(String className) {
        Parser[] parsers = wpsConfigXMLBeans.getDatahandlers().getParserList().getParserArray();
        for (int i = 0; i < parsers.length; i++) {
            Parser parser = parsers[i];
            if (parser.getClassName().equals(className)) {
                return parser.getFormatArray();
            }
        }
        return (Format[]) Array.newInstance(Format.class, 0);

    }

    public boolean isParserActive(String className) {
        Parser[] activeParser = getActiveRegisteredParser();
        for (int i = 0; i < activeParser.length; i++) {
            Parser parser = activeParser[i];
            if (parser.getClassName().equals(className)) {
                return parser.getActive();
            }
        }
        return false;
    }

    public boolean isGeneratorActive(String className) {
        Generator[] generators = getActiveRegisteredGenerator();
        for (int i = 0; i < generators.length; i++) {
            Generator generator = generators[i];
            if (generator.getClassName().equals(className)) {
                return generator.getActive();
            }
        }
        return false;
    }

    public boolean isRepositoryActive(String className) {
        Repository[] repositories = getRegisterdAlgorithmRepositories();
        for (int i = 0; i < repositories.length; i++) {
            Repository repository = repositories[i];
            if (repository.getClassName().equals(className)) {
                return repository.getActive();
            }
        }

        return false;
    }

    public Property[] getPropertiesForRepositoryClass(String className) {
        Repository[] repositories = getRegisterdAlgorithmRepositories();
        for (int i = 0; i < repositories.length; i++) {
            Repository repository = repositories[i];
            if (repository.getClassName().equals(className)) {
                return repository.getPropertyArray();
            }
        }

        return (Property[]) Array.newInstance(Property.class, 0);
    }

    public Property getPropertyForKey(Property[] properties, String key) {
        for (Property property : properties) {
            if (property.getName().equalsIgnoreCase(key)) {
                return property;
            }
        }
        return null;
    }

    /**
     * 
     * @return directory of the configuration folder
     */
    public static final String getConfigDir() {
        String dir = getConfigPath();
        return dir.substring(0, dir.lastIndexOf(CONFIG_FILE_NAME));
    }

}
