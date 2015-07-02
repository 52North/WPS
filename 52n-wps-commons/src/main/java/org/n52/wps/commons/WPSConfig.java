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
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.n52.wps.webapp.api.ClassKnowingModule;
import org.n52.wps.webapp.api.ConfigurationCategory;
import org.n52.wps.webapp.api.ConfigurationManager;
import org.n52.wps.webapp.api.ConfigurationModule;
import org.n52.wps.webapp.api.FormatEntry;
import org.n52.wps.webapp.api.types.ConfigurationEntry;
import org.n52.wps.webapp.entities.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Benjamin Pross, Daniel Nüst
 *
 */
public class WPSConfig implements Serializable {

    private static final long serialVersionUID = 3198223084611936675L;
    private static transient WPSConfig wpsConfig;

    private static final Logger LOGGER = LoggerFactory.getLogger(WPSConfig.class);

    // constants for the Property change event names
    public static final String WPSCONFIG_PROPERTY_EVENT_NAME = "WPSConfigUpdate";
    public static final String WPSCAPABILITIES_SKELETON_PROPERTY_EVENT_NAME = "WPSCapabilitiesUpdate";
    public static final String CONFIG_FILE_PROPERTY = "wps.config.file";
    public static final String CONFIG_FILE_NAME = "wps_config.xml";
    private static final String CONFIG_FILE_DIR = "WEB-INF" + File.separator + "config";
    private static final String URL_DECODE_ENCODING = "UTF-8";
    // FvK: added Property Change support
    protected final PropertyChangeSupport propertyChangeSupport = null;

    public static final String SERVLET_PATH = "WebProcessingService";
	public static final String VERSION_100 = "1.0.0";
	public static final String VERSION_200 = "2.0.0";
	public static final List<String> SUPPORTED_VERSIONS = Arrays.asList(new String[]{VERSION_100, VERSION_200});
	
	public static final String JOB_CONTROL_OPTION_SYNC_EXECUTE = "sync-execute";
	public static final String JOB_CONTROL_OPTION_ASYNC_EXECUTE = "async-execute";
	
	public static final String JOB_CONTROL_OPTIONS_SEPARATOR = " ";
	
	public static final String OUTPUT_TRANSMISSION_VALUE = "value";
	public static final String OUTPUT_TRANSMISSION_REFERENCE = "reference";
	
	public static final String OUTPUT_TRANSMISSIONS_SEPARATOR = " ";
    
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

    /**
     * Add an Listener to the wpsConfig
     *
     * @param propertyName
     * @param listener
     */
    public void addPropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
        // FIXME remove property change mechanism
    }

    /**
     * remove a listener from the wpsConfig
     *
     * @param propertyName
     * @param listener
     */
    public void removePropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
        // FIXME remove property change mechanism
    }

    /**
     * returns an instance of the WPSConfig class. WPSConfig is a single. If there is need for
     * reinstantitation, use forceInitialization().
     *
     * @deprecated if possible use ConfigurationModules
     *
     * @return WPSConfig object representing the wps_config.xml from the classpath or webapps folder
     */
    @Deprecated
    public static WPSConfig getInstance() {
        if (wpsConfig == null) {
        	wpsConfig = new WPSConfig();
        }
        return wpsConfig;
    }

    public static WPSConfig getInstance(ServletContext context) {
        LOGGER.debug("Getting WPSConfig instance... with ServletConfig: {}", context == null ? null : context.toString());
        return getInstance();
    }

    public WPSConfig getWPSConfig() {
        return wpsConfig;
    }

    public Map<String, ConfigurationModule> getRegisteredAlgorithmRepositoryConfigModules() {
    	//TODO check, tests need a mocked up webapp in the future
    	return configurationManager == null ? new HashMap<String, ConfigurationModule>() : configurationManager.getConfigurationServices().getConfigurationModulesByCategory(ConfigurationCategory.REPOSITORY);
    }

    public String getServiceBaseUrl() {
        Server server = getServerConfigurationModule();
        String host = server.getHostname();
        String protocol = server.getProtocol();
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
        url.append(protocol).append("://").append(host);
        url.append(':').append(port).append('/');
        url.append(webapppath);
        return url.toString();
    }

    public String getServiceEndpoint() {
        String endpoint = getServiceBaseUrl() + "/" + SERVLET_PATH;
        return endpoint;
    }

	public List<? extends ConfigurationEntry<?>> getConfigurationEntriesForGeneratorClass(
			String name) {	
		ConfigurationModule module = getConfigurationModuleForClass(name, ConfigurationCategory.GENERATOR);
		return  (module == null) ? new ArrayList<ConfigurationEntry<?>>() : module.getConfigurationEntries();
	}

	public List<FormatEntry> getFormatEntriesForGeneratorClass(String name) {	
		ConfigurationModule module = getConfigurationModuleForClass(name, ConfigurationCategory.GENERATOR);
		return  (module == null) ? new ArrayList<FormatEntry>() : module.getFormatEntries();
	}

	public List<? extends ConfigurationEntry<?>> getConfigurationEntriesForParserClass(
			String name) {	
		ConfigurationModule module = getConfigurationModuleForClass(name, ConfigurationCategory.PARSER);
		return  (module == null) ? new ArrayList<ConfigurationEntry<?>>() : module.getConfigurationEntries();
	}

	public List<FormatEntry> getFormatEntriesForParserClass(String name) {	
		ConfigurationModule module = getConfigurationModuleForClass(name, ConfigurationCategory.PARSER);
		return  (module == null) ? new ArrayList<FormatEntry>() : module.getFormatEntries();
	}
	
	public ConfigurationModule getConfigurationModuleForClass(String name, ConfigurationCategory moduleCategorie){
		
		Map<String, ConfigurationModule> activeModules = getActiveConfigurationModules(moduleCategorie);
		
		for (String moduleName : activeModules.keySet()) {
			
			ConfigurationModule tmpModule = activeModules.get(moduleName);
			
			if(!(tmpModule instanceof ClassKnowingModule)){
				continue;
			}
			
			if(((ClassKnowingModule)tmpModule).getClassName().equals(name)){
				return tmpModule;				
			}			
		}
		return null;
	}
	
	private Map<String, ConfigurationModule> getActiveConfigurationModules(ConfigurationCategory moduleCategorie){
		return configurationManager.getConfigurationServices().getActiveConfigurationModulesByCategory(moduleCategorie);
	}

	public boolean isGeneratorActive(String className) {
		ConfigurationModule module = getConfigurationModuleForClass(className, ConfigurationCategory.GENERATOR);
		return module != null ? module.isActive() : false;
	}

	public boolean isParserActive(String className) {
		ConfigurationModule module = getConfigurationModuleForClass(className, ConfigurationCategory.PARSER);
		return module != null ? module.isActive() : false;
	}

	public Map<String, ConfigurationModule> getActiveRegisteredParserModules() {
		return getActiveConfigurationModules(ConfigurationCategory.PARSER);
	}

	public Map<String, ConfigurationModule> getActiveRegisteredGeneratorModules() {
		return getActiveConfigurationModules(ConfigurationCategory.GENERATOR);
	}
}
