/**
 * ﻿Copyright (C) 2006 - 2014 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.commons;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;

import org.apache.xmlbeans.XmlException;
import org.n52.wps.FormatDocument.Format;
import org.n52.wps.GeneratorDocument.Generator;
import org.n52.wps.ParserDocument.Parser;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.RepositoryDocument.Repository;
import org.n52.wps.WPSConfigurationDocument;
import org.n52.wps.impl.WPSConfigurationDocumentImpl.WPSConfigurationImpl;
import org.n52.wps.webapp.api.ConfigurationManager;
import org.n52.wps.webapp.entities.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

/**
 * 
 * @author Benjamin Pross, Daniel Nüst
 *
 */
public class WPSConfig implements Serializable {

    private static final long serialVersionUID = 3198223084611936675L;
    private static transient WPSConfig wpsConfig;
    private static transient WPSConfigurationImpl wpsConfigXMLBeans;

    private static final Logger LOGGER = LoggerFactory.getLogger(WPSConfig.class);

    // constants for the Property change event names
    public static final String WPSCONFIG_PROPERTY_EVENT_NAME = "WPSConfigUpdate";
    public static final String WPSCAPABILITIES_SKELETON_PROPERTY_EVENT_NAME = "WPSCapabilitiesUpdate";
    public static final String CONFIG_FILE_PROPERTY = "wps.config.file";
    public static final String CONFIG_FILE_NAME = "wps_config.xml";
    private static final String CONFIG_FILE_DIR = "WEB-INF" + File.separator + "config";
    private static final String URL_DECODE_ENCODING = "UTF-8";
    // FvK: added Property Change support
    protected final PropertyChangeSupport propertyChangeSupport;

    public static final String SERVLET_PATH = "WebProcessingService";

    private static String configPath;
    
	private ConfigurationManager configurationManager;
    private Server serverConfigurationModule;	

	public Server getServerConfigurationModule() {

		if (serverConfigurationModule == null) {
			serverConfigurationModule = (Server) configurationManager
					.getConfigurationServices().getConfigurationModule(
							Server.class.getName());
		}
		return serverConfigurationModule;
	}

    public ConfigurationManager getConfigurationManager() {
		return configurationManager;
	}

	public void setConfigurationManager(ConfigurationManager configurationManager) {
		this.configurationManager = configurationManager;
	}
    
    private WPSConfig(String wpsConfigPath) throws XmlException, IOException {
    	configPath = wpsConfigPath;    	
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

    /**
     * For Testing purpose only
     */
    public void notifyListeners() {
        this.propertyChangeSupport.firePropertyChange(WPSCONFIG_PROPERTY_EVENT_NAME, null, null);
    }

    public void firePropertyChange(String event) {
    	propertyChangeSupport.firePropertyChange(event, null, null);
    }

    /**
     * WPSConfig is a singleton. If there is a need for reinitialization, use this path.
     *
     * @param configPath path to the wps_config.xml
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
        LOGGER.debug("Getting WPSConfig instance... from path: {}", path);
        if (wpsConfig == null) {
            try {
                wpsConfig = new WPSConfig(path);
                configPath = path;
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

    public static WPSConfig getInstance(ServletContext context) {
        LOGGER.debug("Getting WPSConfig instance... with ServletConfig: {}", context == null ? null : context.toString());
        String path = getConfigPath(context);
        LOGGER.debug("Found config file under " + path);
        return getInstance(path);
    }

    public static String getConfigPath(ServletContext context) {
        Optional<ServletContext> servletContext = Optional.fromNullable(context);
        for (WPSConfigFileStrategy strategy : getWPSConfigFileStrategies()) {
            Optional<File> file = strategy.find(servletContext);
            if (file.isPresent()) {
                String path = file.get().getAbsolutePath();
                LOGGER.info("Found config file at {} using the strategy {}", path, strategy.getClass().getName());
                return path;
            }
        }
        throw new RuntimeException("Could not find and load wps_config.xml");
    }

    private static List<WPSConfigFileStrategy> getWPSConfigFileStrategies() {
        return ImmutableList.of(new SystemPropertyStrategy(),
                                new JNDIContextStrategy(),
                                new HomeFolderStrategy(),
                                new InitParameterStrategy(),
                                new RelativeInitParameterStrategy(),
                                new DefaultPathStrategy(),
                                new ClassPathStrategy(),
                                new WebAppTargetStrategy(),
                                new WebAppSourceStrategy(),
                                new WebAppPathStrategy(),
                                new LastResortStrategy());
    }

    /**
     * This method retrieves the full path for the file (wps_config.xml), searching in several locations. This
     * is only applicable for webapp applications. To customize this, please use directly
     * {@link WPSConfig#forceInitialization(String)} and then getInstance().
     *
     * @return
     */
	public static String getConfigPath() {
		if (configPath == null) {
			return getConfigPath(null);
		}
		return configPath;
	}

    public WPSConfigurationImpl getWPSConfig() {
        return wpsConfigXMLBeans;
    }

    public Parser[] getRegisteredParser() {
        return wpsConfigXMLBeans.getDatahandlers().getParserList().getParserArray();
    }

    public Parser[] getActiveRegisteredParser() {
        Parser[] parsers = getRegisteredParser();
        ArrayList<Parser> activeParsers = new ArrayList<Parser>(parsers.length);
        for (Parser parser : parsers) {
            if (parser.getActive()) {
                activeParsers.add(parser);
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
        ArrayList<Generator> activeGenerators = new ArrayList<Generator>(generators.length);
        for (Generator generator : generators) {
            if (generator.getActive()) {
                activeGenerators.add(generator);
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
        for (Generator generator : generators) {
            if (generator.getClassName().equals(className)) {
                return generator.getPropertyArray();
            }
        }
        return (Property[]) Array.newInstance(Property.class, 0);

    }

    public Format[] getFormatsForGeneratorClass(String className) {
        Generator[] generators = wpsConfigXMLBeans.getDatahandlers().getGeneratorList().getGeneratorArray();
        for (Generator generator : generators) {
            if (generator.getClassName().equals(className)) {
                return generator.getFormatArray();
            }
        }
        return (Format[]) Array.newInstance(Format.class, 0);

    }

    public Property[] getPropertiesForParserClass(String className) {
        Parser[] parsers = wpsConfigXMLBeans.getDatahandlers().getParserList().getParserArray();
        for (Parser parser : parsers) {
            if (parser.getClassName().equals(className)) {
                return parser.getPropertyArray();
            }
        }
        return (Property[]) Array.newInstance(Property.class, 0);

    }

    public Format[] getFormatsForParserClass(String className) {
        Parser[] parsers = wpsConfigXMLBeans.getDatahandlers().getParserList().getParserArray();
        for (Parser parser : parsers) {
            if (parser.getClassName().equals(className)) {
                return parser.getFormatArray();
            }
        }
        return (Format[]) Array.newInstance(Format.class, 0);

    }

    public boolean isParserActive(String className) {
        Parser[] activeParser = getActiveRegisteredParser();
        for (Parser parser : activeParser) {
            if (parser.getClassName().equals(className)) {
                return parser.getActive();
            }
        }
        return false;
    }

    public boolean isGeneratorActive(String className) {
        Generator[] generators = getActiveRegisteredGenerator();
        for (Generator generator : generators) {
            if (generator.getClassName().equals(className)) {
                return generator.getActive();
            }
        }
        return false;
    }

    public boolean isRepositoryActive(String className) {
        Repository[] repositories = getRegisterdAlgorithmRepositories();
        for (Repository repository : repositories) {
            if (repository.getClassName().equals(className)) {
                return repository.getActive();
            }
        }

        return false;
    }

    public Property[] getPropertiesForRepositoryClass(String className) {
        Repository[] repositories = getRegisterdAlgorithmRepositories();
        for (Repository repository : repositories) {
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

    public String getServiceBaseUrl() {
        Server server = getServerConfigurationModule();
        String host = server.getHostname();
        if (host == null) {
            try {
                host = InetAddress.getLocalHost().getCanonicalHostName();
            }
            catch (UnknownHostException e) {
                LOGGER.warn("Could not derive host name automatically", e);
            }
        }
        int port = server.getHostport();
        String webapppath = server.getWebappPath();

        StringBuilder url = new StringBuilder();
        // TODO what if this service runs on HTTPS? TODO: do not construct endpoint URL as string
        url.append("http").append("://").append(host);
        url.append(':').append(port).append('/');
        url.append(webapppath);
        return url.toString();
    }

    public String getServiceEndpoint() {
        String endpoint = getServiceBaseUrl() + "/" + SERVLET_PATH;
        return endpoint;
    }

    /**
     *
     * @return directory of the configuration folder
     */
    public static String getConfigDir() {
        String dir = getConfigPath();
        return dir.substring(0, dir.lastIndexOf(CONFIG_FILE_NAME));
    }

    public static abstract class WPSConfigFileStrategy {
        public Optional<File> find(Optional<ServletContext> servletContext) {
            String p = getPath(servletContext);
            return checkPath(p);
        }

        private Optional<File> checkPath(String path) {
            if (path != null && !path.isEmpty()) {
                LOGGER.debug("Checking {} for WPS config", path);
                File file = new File(path);
                if (!file.exists()) {
                    LOGGER.debug("{} does not exist", path);
                } else if (!file.isFile()) {
                    LOGGER.debug("{} is not a file", path);
                } else if (!file.canRead()) {
                    LOGGER.debug("{} is not readable", path);
                } else {
                    return Optional.of(file);
                }
            }
            return Optional.absent();
        }

        protected abstract String getPath(Optional<ServletContext> servletContext);
    }

    private static class SystemPropertyStrategy extends WPSConfigFileStrategy {
        @Override
        protected String getPath(Optional<ServletContext> servletContext) {
            return System.getProperty(CONFIG_FILE_PROPERTY);
        }
    }

    private static class JNDIContextStrategy extends WPSConfigFileStrategy {
        @Override
        protected String getPath(Optional<ServletContext> servletContext) {
            try {
                Context ctx = (Context) new InitialContext().lookup("java:comp/env");
                if (ctx == null) {
                    return null;
                }
                return (String) ctx.lookup(CONFIG_FILE_PROPERTY);
            } catch (NamingException ex) {
                LOGGER.info("Can not get java:comp/env context: {} : {}", ex.getClass(), ex.getMessage());
                return null;
            }
        }
    }

    private static class InitParameterStrategy extends WPSConfigFileStrategy {
        @Override
        protected String getPath(Optional<ServletContext> servletContext) {
            return servletContext.isPresent() ? servletContext.get().getInitParameter(CONFIG_FILE_PROPERTY) : null;
        }
    }

    private static class RelativeInitParameterStrategy extends WPSConfigFileStrategy {
        @Override
        protected String getPath(Optional<ServletContext> servletContext) {
            if (servletContext.isPresent()) {
                String path = servletContext.get().getInitParameter(CONFIG_FILE_PROPERTY);
                if (path != null) {
                    return servletContext.get().getRealPath(path);
                }
            }
            return null;
        }
    }

    private static class DefaultPathStrategy extends WPSConfigFileStrategy {
        @Override
        protected String getPath(Optional<ServletContext> servletContext) {
            return servletContext.isPresent()? servletContext.get().getRealPath(CONFIG_FILE_DIR + File.separator + CONFIG_FILE_NAME) : null;
        }
    }

    private static class ClassPathStrategy extends WPSConfigFileStrategy {
        @Override
        protected String getPath(Optional<ServletContext> servletContext) {
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
    }

    private static class WebAppTargetStrategy extends WPSConfigFileStrategy {
        @Override
        protected String getPath(Optional<ServletContext> servletContext) {
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
    }

    private static class WebAppSourceStrategy extends WPSConfigFileStrategy {
        @Override
        protected String getPath(Optional<ServletContext> servletContext) {
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
    }

    private static class WebAppPathStrategy extends WPSConfigFileStrategy {
        @Override
        protected String getPath(Optional<ServletContext> servletContext) {
            String domain;
            try {
                domain = new File(WPSConfig.class.getResource("/").toURI()).toString();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            int index = domain.indexOf("WEB-INF");
            if (index > 0) {
                String substring = domain.substring(0, index);
                // if ( !substring.endsWith("/")) {
                //     substring = substring + "/";
                // }
                // substring = substring + CONFIG_FILE_DIR + File.separator + CONFIG_FILE_NAME;
                File configDir = new File(new File(substring), CONFIG_FILE_DIR);
                if (configDir.exists() && configDir.isDirectory()) {
                    String configFile = new File(configDir, CONFIG_FILE_NAME).getAbsolutePath();
                    return configFile;
                }
            }
            return null;
        }
    }

    private static class LastResortStrategy extends WPSConfigFileStrategy {
        @Override
        protected String getPath(Optional<ServletContext> servletContext) {
            String domain = WPSConfig.class.getProtectionDomain().getCodeSource().getLocation().getFile();

            try {
                domain = URLDecoder.decode(domain, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                LOGGER.warn("Could not decode URL of WPSConfig class, continuing.");
            }

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
    }

    private static class HomeFolderStrategy extends WPSConfigFileStrategy {
        @Override
        protected String getPath(Optional<ServletContext> servletContext) {
            return System.getProperty("user.home") + File.separator + CONFIG_FILE_NAME;
        }
    }
}
