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

package org.n52.wps.webapp.service;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.n52.wps.webapp.api.AlgorithmEntry;
import org.n52.wps.webapp.api.ConfigurationCategory;
import org.n52.wps.webapp.api.ConfigurationKey;
import org.n52.wps.webapp.api.ConfigurationModule;
import org.n52.wps.webapp.api.ConfigurationType;
import org.n52.wps.webapp.api.ValueParser;
import org.n52.wps.webapp.api.WPSConfigurationException;
import org.n52.wps.webapp.api.types.BooleanConfigurationEntry;
import org.n52.wps.webapp.api.types.ConfigurationEntry;
import org.n52.wps.webapp.api.types.DoubleConfigurationEntry;
import org.n52.wps.webapp.api.types.FileConfigurationEntry;
import org.n52.wps.webapp.api.types.IntegerConfigurationEntry;
import org.n52.wps.webapp.api.types.StringConfigurationEntry;
import org.n52.wps.webapp.api.types.URIConfigurationEntry;
import org.n52.wps.webapp.dao.ConfigurationDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("configurationService")
public class ConfigurationServiceImpl implements ConfigurationService {

	@Autowired
	private ListableBeanFactory listableBeanFactory;

	@Autowired
	private ConfigurationDAO configurationDAO;

	@Autowired
	private ValueParser valueParser;

	private static Logger LOGGER = LoggerFactory.getLogger(ConfigurationServiceImpl.class);

	private Map<String, ConfigurationModule> allConfigurationModules;

	@Override
	public void syncConfigurations() {
		LOGGER.info("Initializing and syncing configuration modules.");
		for (ConfigurationModule module : getAllConfigurationModules().values()) {
			LOGGER.info("Initializing and syncing configuration module '{}'.", module.getClass().getName());
			
			//sync configuration module status
			Boolean moduleStatus = configurationDAO.getConfigurationModuleStatus(module);
			if (moduleStatus != null) {
				module.setActive(moduleStatus);
			} else {
				configurationDAO.insertConfigurationModule(module);
			}
			
			//sync configuration entries
			if (module.getConfigurationEntries() != null) {
				for (ConfigurationEntry<?> entry : module.getConfigurationEntries()) {
					syncConfigurationEntry(module, entry);
				}
			}

			//sync algorithm entries
			if (module.getAlgorithmEntries() != null) {
				for (AlgorithmEntry entry : module.getAlgorithmEntries()) {
					syncAlgorithmEntry(module, entry);
				}
			}

			LOGGER.info("Done initializing and syncing configuration module '{}'.", module.getClass().getName());
		}
		LOGGER.info("Done initializing and syncing all configuration modules.");
	}

	private void syncConfigurationEntry(ConfigurationModule module, ConfigurationEntry<?> entry) {
		LOGGER.debug("Syncing configuration entry '{}' in module'{}'.", entry.getKey(), module.getClass().getName());

		Object storedValue = configurationDAO.getConfigurationEntryValue(module.getClass().getName(), entry.getKey());
		if (storedValue != null) {
			try {
				setConfigurationEntryValueHelper(module, entry, storedValue);
				LOGGER.debug("Done syncing configuration value '{}' for entry '{}' in module'{}' from the database.",
						storedValue, entry.getKey(), module.getClass().getName());
			} catch (WPSConfigurationException e) {
				LOGGER.error("Error setting value: ", e);
			}
		} else {
			Object value = null;
			if (entry.getValue() == null) {
				configurationDAO.insertConfigurationEntryValue(module.getClass().getName(), entry.getKey(), null);
				LOGGER.debug("Done writing configuration value '{}' for entry '{}' in module'{}' to the database.",
						value, entry.getKey(), module.getClass().getName());
			}
			if ((value = entry.getValue()) != null) {
				if (entry.getType() == ConfigurationType.FILE || entry.getType() == ConfigurationType.URI) {
					value = entry.getValue().toString();
				}
				configurationDAO.insertConfigurationEntryValue(module.getClass().getName(), entry.getKey(), value);
				LOGGER.debug("Done writing configuration value '{}' for entry '{}' in module'{}' to the database.",
						value, entry.getKey(), module.getClass().getName());
			}
		}
	}

	private void syncAlgorithmEntry(ConfigurationModule module, AlgorithmEntry entry) {
		LOGGER.debug("Syncing algorithm entry '{}' in module '{}'.", entry.getAlgorithm(), module.getClass().getName());

		AlgorithmEntry storedEntry = configurationDAO.getAlgorithmEntry(module.getClass().getName(),
				entry.getAlgorithm());
		if (storedEntry != null) {
			setAlgorithmEntryHelper(module, entry, storedEntry.isActive());
			LOGGER.debug("Done setting algorithm '{}' to active status '{}' in module '{}' from the database.",
					entry.getAlgorithm(), storedEntry.isActive(), module.getClass().getName());

		} else {
			configurationDAO.insertAlgorithmEntry(module.getClass().getName(), entry.getAlgorithm(), entry.isActive());
			LOGGER.debug("Done writing algorithm '{}' with active status '{}' in module '{}' to the database.",
					entry.getAlgorithm(), entry.isActive(), module.getClass().getName());
		}
	}

	@PostConstruct
	private void buildConfigurationModulesMap() {
		Map<String, ConfigurationModule> initialModulesMap = listableBeanFactory
				.getBeansOfType(ConfigurationModule.class);

		allConfigurationModules = new HashMap<String, ConfigurationModule>();
		for (ConfigurationModule entry : initialModulesMap.values()) {
			allConfigurationModules.put(entry.getClass().getName(), entry);
		}
	}

	@Override
	public Map<String, ConfigurationModule> getAllConfigurationModules() {
		return allConfigurationModules;
	}

	@Override
	public Map<String, ConfigurationModule> getConfigurationModulesByCategory(ConfigurationCategory category) {
		Map<String, ConfigurationModule> allModulesMap = getAllConfigurationModules();
		Map<String, ConfigurationModule> byCategoryNameToModuleMap = new HashMap<String, ConfigurationModule>();
		for (Map.Entry<String, ConfigurationModule> entry : allModulesMap.entrySet()) {
			if (entry.getValue().getCategory() == category) {
				byCategoryNameToModuleMap.put(entry.getKey(), entry.getValue());
			}
		}
		LOGGER.debug("Returning '{}' configuration modules under '{}' category.", byCategoryNameToModuleMap.size(),
				category);
		return byCategoryNameToModuleMap;
	}

	@Override
	public Map<String, ConfigurationModule> getConfigurationModulesByCategory(ConfigurationCategory category,
			boolean active) {
		Map<String, ConfigurationModule> byCategoryNameToModuleMap = getConfigurationModulesByCategory(category);
		Map<String, ConfigurationModule> activeByCategoryNameToModuleMap = new HashMap<String, ConfigurationModule>();
		if (active) {
			for (Map.Entry<String, ConfigurationModule> entry : byCategoryNameToModuleMap.entrySet()) {
				if (entry.getValue().getCategory() == category && entry.getValue().isActive()) {
					activeByCategoryNameToModuleMap.put(entry.getKey(), entry.getValue());
				}
			}
			LOGGER.debug("Returning '{}' active configuration modules under '{}' category.",
					activeByCategoryNameToModuleMap.size(), category);
			return activeByCategoryNameToModuleMap;
		} else {
			return byCategoryNameToModuleMap;
		}
	}

	@Override
	public ConfigurationModule getConfigurationModule(String moduleClassName) {
		ConfigurationModule module = getAllConfigurationModules().get(moduleClassName);
		if (module != null) {
			LOGGER.debug("Returning configuration module '{}'.", moduleClassName);
		}
		return module;
	}
	
	@Override
	public void updateConfigurationModule(ConfigurationModule module) {
		configurationDAO.updateConfigurationModule(module);
	}

	@Override
	public ConfigurationEntry<?> getConfigurationEntry(ConfigurationModule module, String entryKey) {
		for (ConfigurationEntry<?> entry : module.getConfigurationEntries()) {
			if (entry.getKey().equals(entryKey)) {
				LOGGER.debug("Returning configuration entry '{}' in module '{}'.", entryKey, module.getClass()
						.getName());
				return entry;
			}
		}
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getConfigurationEntryValue(ConfigurationModule module, ConfigurationEntry<?> entry,
			Class<T> requiredType) throws WPSConfigurationException {
		Object value = entry.getValue();
		if (value == null || (requiredType != null && !requiredType.isAssignableFrom(value.getClass()))) {
			String errorMessage = "The value '" + value + "' cannot be assigned to a/an '"
					+ requiredType.getSimpleName() + "' type.";
			throw new WPSConfigurationException(errorMessage);
		}
		LOGGER.debug("Returning value '{}' of type '{}' for configuration entry '{}' in module '{}'.", value,
				requiredType.getSimpleName(), entry.getKey(), module.getClass().getName());
		return (T) value;
	}

	@Override
	public void setConfigurationEntryValue(String moduleClassName, String entryKey, Object value)
			throws WPSConfigurationException {
		ConfigurationModule module = getConfigurationModule(moduleClassName);
		ConfigurationEntry<?> entry = getConfigurationEntry(module, entryKey);
		if (entry != null) {
			setConfigurationEntryValueHelper(module, entry, value);
			configurationDAO.updateConfigurationEntryValue(moduleClassName, entryKey, value);
			LOGGER.debug("Value '{}' for entry '{}' in module '{}' has been saved to the database.", value, entryKey,
					moduleClassName);
		}
	}

	/*
	 * Used internally to set the value without calling configurationDAO
	 */
	private void setConfigurationEntryValueHelper(ConfigurationModule module, ConfigurationEntry<?> entry, Object value)
			throws WPSConfigurationException {
		try {
			switch (entry.getType()) {
			case STRING:
				setStringValue((StringConfigurationEntry) entry, value);
				break;
			case BOOLEAN:
				setBooleanValue((BooleanConfigurationEntry) entry, value);
				break;
			case DOUBLE:
				setDoubleValue((DoubleConfigurationEntry) entry, value);
				break;
			case FILE:
				setFileValue((FileConfigurationEntry) entry, value);
				break;
			case INTEGER:
				setIntegerValue((IntegerConfigurationEntry) entry, value);
				break;
			case URI:
				setURIValue((URIConfigurationEntry) entry, value);
				break;
			default:
				break;
			}
			LOGGER.debug("Value '{}' has been set for entry '{}' in module '{}'.", value, entry.getKey(), module.getClass()
					.getName());
			passValueToConfigurationModule(module, entry);
		} catch (WPSConfigurationException e) {
			// only throw the null exception if the entry is required "not allowed to be null"
			if (e.getCause() != null && e.getCause().toString().contains("NullPointerException")) {
				if (entry.isRequired()) {
					throw e;
				} 
			} else {
				throw e;
			}
		}
	}

	@Override
	public AlgorithmEntry getAlgorithmEntry(ConfigurationModule module, String algorithm) {
		for (AlgorithmEntry entry : module.getAlgorithmEntries()) {
			if (entry.getAlgorithm().equals(algorithm)) {
				LOGGER.debug("Returning algorithm '{}' with status '{}' in module '{}'.", entry.getAlgorithm(),
						entry.isActive(), module.getClass().getName());
				return entry;
			}
		}
		return null;
	}

	@Override
	public void setAlgorithmEntry(String moduleClassName, String algorithm, boolean active) {
		ConfigurationModule module = getConfigurationModule(moduleClassName);
		AlgorithmEntry entry = getAlgorithmEntry(module, algorithm);
		if (entry != null) {
			setAlgorithmEntryHelper(module, entry, active);
			configurationDAO.updateAlgorithmEntry(moduleClassName, algorithm, active);
			LOGGER.debug("Algorithm '{}' in module '{}' with status {} has been saved to the database.", algorithm,
					moduleClassName, active);
		}
	}

	/*
	 * Used internally to set algorithm status without calling configurationDAO
	 */
	private void setAlgorithmEntryHelper(ConfigurationModule module, AlgorithmEntry entry, boolean active) {
		entry.setActive(active);
		LOGGER.debug("Algorithm '{}' in module '{}' has been set to {}.", entry.getAlgorithm(), module.getClass()
				.getName(), active);
	}

	@Override
	public void passValueToConfigurationModule(ConfigurationModule module, ConfigurationEntry<?> entry) {
		for (Method method : module.getClass().getMethods()) {
			if (method.isAnnotationPresent(ConfigurationKey.class)) {
				ConfigurationKey configurationKey = method.getAnnotation(ConfigurationKey.class);
				if (configurationKey.key().equals(entry.getKey())) {
					Class<?>[] clazz = method.getParameterTypes();
					try {
						if (method.getParameterTypes().length != 1) {
							throw new WPSConfigurationException(
									"The method has the wrong number of parameters, it must be 1.");
						}

						if (clazz[0].isPrimitive()) {
							if (clazz[0].toString().equals("int")) {
								clazz[0] = Integer.class;
							}
							if (clazz[0].toString().equals("double")) {
								clazz[0] = Double.class;
							}
							if (clazz[0].toString().equals("boolean")) {
								clazz[0] = Boolean.class;
							}
						}

						Object value = getConfigurationEntryValue(module, entry, clazz[0]);
						if (value != null) {
							method.invoke(module, value);
							LOGGER.debug("Value '{}' passed to method '{}' in module '{}'.", value.toString(),
									method.getName(), module.getClass().getName());
						}

					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
							| WPSConfigurationException e) {
						LOGGER.error("Cannot pass value to method '{}' in module '{}' for entry '{}': ",
								method.getName(), module.getClass().getName(), configurationKey.key(), e);
					}
				}
			}
		}
	}

	private void setStringValue(ConfigurationEntry<String> entry, Object value) throws WPSConfigurationException {
		String parsedValue = valueParser.parseString(value);
		entry.setValue(parsedValue);
	}

	private void setIntegerValue(ConfigurationEntry<Integer> entry, Object value) throws WPSConfigurationException {
		Integer parsedValue = valueParser.parseInteger(value);
		entry.setValue(parsedValue);
	}

	private void setDoubleValue(ConfigurationEntry<Double> entry, Object value) throws WPSConfigurationException {
		Double parsedValue = valueParser.parseDouble(value);
		entry.setValue(parsedValue);
	}

	private void setBooleanValue(ConfigurationEntry<Boolean> entry, Object value) throws WPSConfigurationException {
		Boolean parsedValue = valueParser.parseBoolean(value);
		entry.setValue(parsedValue);
	}

	private void setFileValue(ConfigurationEntry<File> entry, Object value) throws WPSConfigurationException {
		File parsedValue = valueParser.parseFile(value);
		entry.setValue(parsedValue);
	}

	private void setURIValue(ConfigurationEntry<URI> entry, Object value) throws WPSConfigurationException {
		URI parsedValue = valueParser.parseURI(value);
		entry.setValue(parsedValue);
	}
}
