/**
 * Copyright (C) 2013
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

package org.n52.wps.webapp.api;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.n52.wps.webapp.api.types.ConfigurationEntry;
import org.n52.wps.webapp.service.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("configurationManager")
public class ConfigurationManagerImpl implements ConfigurationManager {

	@Autowired
	private ConfigurationService configurationService;

	private static Logger LOGGER = LoggerFactory.getLogger(ConfigurationManagerImpl.class);

	@Override
	public Map<String, ConfigurationModule> getAllConfigurationModules() {
		return configurationService.getAllConfigurationModules();
	}

	@Override
	public Map<String, ConfigurationModule> getAllConfigurationModulesByCategory(ConfigurationCategory category) {
		return configurationService.getConfigurationModulesByCategory(category);
	}

	@Override
	public Map<String, ConfigurationModule> getActiveConfigurationModulesByCategory(ConfigurationCategory category) {
		return configurationService.getConfigurationModulesByCategory(category, true);
	}

	@Override
	public ConfigurationModule getConfigurationModule(String moduleClassName) {
		return configurationService.getConfigurationModule(moduleClassName);
	}
	
	@Override
	public ConfigurationEntry<?> getConfigurationEntry(String moduleClassName, String entryKey) {
		return configurationService.getConfigurationEntry(moduleClassName, entryKey);
	}
	
	@Override
	public <T> T getConfigurationEntryValue(String moduleClassName, String entryKey, Class<T> requiredType)
			throws WPSConfigurationException {
		try {
			return configurationService.getConfigurationEntryValue(moduleClassName, entryKey, requiredType);
		} catch (WPSConfigurationException e) {
			throw new WPSConfigurationException(e);
		}
	}

	@Override
	public void setConfigurationEntryValue(String moduleClassName, String entryKey, Object value)
			throws WPSConfigurationException {
		try {
			configurationService.setConfigurationEntryValue(moduleClassName, entryKey, value);
		} catch (WPSConfigurationException e) {
			throw new WPSConfigurationException(e);
		}
	}
	
	@Override
	public AlgorithmEntry getAlgorithmEntry(String moduleClassName, String algorithm) {
		return configurationService.getAlgorithmEntry(moduleClassName, algorithm);
	}
	
	@Override
	public void setAlgorithmEntry(String moduleClassName, String algorithm, boolean active) {
		configurationService.setAlgorithmEntry(moduleClassName, algorithm, active);
	}

	@PostConstruct
	private void initializeConfigurations() {
		LOGGER.debug("Initializing configurations...");
		configurationService.syncConfigurations();
		LOGGER.debug("Configurations initialized.");
	}
}
