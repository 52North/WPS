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

	@Override
	public void syncConfigurations() {
		LOGGER.debug("Initializing and syncing configuration modules.");
		for (ConfigurationModule module : getAllConfigurationModules().values()) {
			LOGGER.debug("Initializing and syncing configuration module '" + module.getClass().getName() + "'.");

			if (module.getConfigurationEntries() != null) {
				for (ConfigurationEntry<?> entry : module.getConfigurationEntries()) {
					syncConfigurationEntry(module.getClass().getName(), entry);
				}
			}

			if (module.getAlgorithmEntries() != null) {
				for (AlgorithmEntry entry : module.getAlgorithmEntries()) {
					syncAlgorithmEntry(module.getClass().getName(), entry);
				}
			}

			LOGGER.debug("Done initializing and syncing configuration module '" + module.getClass().getName() + "'.");
		}
		LOGGER.debug("Done initializing and syncing configuration modules.");
	}

	private void syncConfigurationEntry(String moduleClassName, ConfigurationEntry<?> entry) {
		LOGGER.debug("Syncing configuration entry '" + entry.getKey() + "' in module'" + moduleClassName + "'.");

		Object storedValue = configurationDAO.getConfigurationEntryValue(moduleClassName, entry.getKey());
		if (storedValue != null) {
			try {
				setConfigurationEntryValueHelper(moduleClassName, entry.getKey(), storedValue);
				LOGGER.debug("Done syncing configuration value '" + storedValue + "' for entry '" + entry.getKey()
						+ "' in module'" + moduleClassName + "' from the database.");
			} catch (WPSConfigurationException e) {
				// Do nothing, error already logged by the setConfigurationEntryValueHelper method
			}
		} else {
			Object value = null;

			if (entry.getType() == ConfigurationType.FILE || entry.getType() == ConfigurationType.URI) {
				value = entry.getValue().toString();
			} else {
				value = entry.getValue();
			}
			configurationDAO.insertConfigurationEntryValue(moduleClassName, entry.getKey(), value);
			LOGGER.debug("Done writing configuration value '" + value + "' for entry '" + entry.getKey()
					+ "' in module'" + moduleClassName + "' to the database.");
		}
	}

	private void syncAlgorithmEntry(String moduleClassName, AlgorithmEntry entry) {
		LOGGER.debug("Syncing algorithm entry '" + entry.getAlgorithm() + "' in module'" + moduleClassName + "'.");

		AlgorithmEntry storedEntry = configurationDAO.getAlgorithmEntry(moduleClassName, entry.getAlgorithm());
		if (storedEntry != null) {
			setAlgorithmEntryHelper(moduleClassName, entry.getAlgorithm(), storedEntry.isActive());
			LOGGER.debug("Done setting algorithm '" + entry.getAlgorithm() + "' to active status: '"
					+ storedEntry.isActive() + "' in module'" + moduleClassName + "' from the database.");

		} else {
			configurationDAO.insertAlgorithmEntry(moduleClassName, entry.getAlgorithm(), entry.isActive());
			LOGGER.debug("Done writing algorithm '" + entry.getAlgorithm() + "' with active status: '"
					+ entry.isActive() + "' in module'" + moduleClassName + "' to the database.");
		}
	}

	@Override
	public Map<String, ConfigurationModule> getAllConfigurationModules() {
		Map<String, ConfigurationModule> initialModulesMap = listableBeanFactory
				.getBeansOfType(ConfigurationModule.class);

		Map<String, ConfigurationModule> classNameToModuleMap = new HashMap<String, ConfigurationModule>();
		for (ConfigurationModule entry : initialModulesMap.values()) {
			classNameToModuleMap.put(entry.getClass().getName(), entry);
		}
		return classNameToModuleMap;
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
			return activeByCategoryNameToModuleMap;
		} else {
			return byCategoryNameToModuleMap;
		}
	}

	@Override
	public ConfigurationModule getConfigurationModule(String moduleClassName) {
		return getAllConfigurationModules().get(moduleClassName);
	}

	@Override
	public ConfigurationEntry<?> getConfigurationEntry(String moduleClassName, String entryKey) {
		ConfigurationModule module = getConfigurationModule(moduleClassName);
		for (ConfigurationEntry<?> entry : module.getConfigurationEntries()) {
			if (entry.getKey().equals(entryKey)) {
				return entry;
			}
		}
		return null;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getConfigurationEntryValue(String moduleClassName, String entryKey, Class<T> requiredType)
			throws WPSConfigurationException {
		Object value = getConfigurationEntryValueHelper(moduleClassName, entryKey);
		if (requiredType != null && !requiredType.isAssignableFrom(value.getClass())) {
			String errorMessage = "The value '" + value + "' cannot be assigned to a/an '"
					+ requiredType.getSimpleName() + "' type.";
			LOGGER.error(errorMessage);
			throw new WPSConfigurationException(errorMessage);
		}
		return (T) value;
	}

	private Object getConfigurationEntryValueHelper(String moduleClassName, String entryKey) {
		ConfigurationEntry<?> entry = getConfigurationEntry(moduleClassName, entryKey);
		return entry.getValue();
	}

	@Override
	public void setConfigurationEntryValue(String moduleClassName, String entryKey, Object value)
			throws WPSConfigurationException {
		try {
			setConfigurationEntryValueHelper(moduleClassName, entryKey, value);
			configurationDAO.insertConfigurationEntryValue(moduleClassName, entryKey, value);
		} catch (WPSConfigurationException e) {
			throw new WPSConfigurationException(e);
		}
	}

	/*
	 * Used internally to set the value without calling configurationDAO
	 */
	private void setConfigurationEntryValueHelper(String moduleClassName, String entryKey, Object value)
			throws WPSConfigurationException {
		ConfigurationEntry<?> entry = getConfigurationEntry(moduleClassName, entryKey);
		if (entry != null) {
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
				passValueToConfigurationModule(moduleClassName, entryKey);
			} catch (WPSConfigurationException e) {
				String errorMessage = "Unable to set value '" + value + "' in module '" + moduleClassName
						+ "' for entry '" + entryKey + "': " + e.getMessage();
				LOGGER.error(errorMessage);
				throw new WPSConfigurationException(errorMessage);
			}
		}
	}

	@Override
	public AlgorithmEntry getAlgorithmEntry(String moduleClassName, String algorithm) {
		ConfigurationModule module = getConfigurationModule(moduleClassName);
		for (AlgorithmEntry entry : module.getAlgorithmEntries()) {
			if (entry.getAlgorithm().equals(algorithm)) {
				return entry;
			}
		}
		return null;
	}

	@Override
	public void setAlgorithmEntry(String moduleClassName, String algorithm, boolean active) {
		setAlgorithmEntryHelper(moduleClassName, algorithm, active);
		configurationDAO.insertAlgorithmEntry(moduleClassName, algorithm, active);
	}

	/*
	 * Used internally to set the status without calling configurationDAO
	 */
	private void setAlgorithmEntryHelper(String moduleClassName, String algorithm, boolean active) {
		AlgorithmEntry entry = getAlgorithmEntry(moduleClassName, algorithm);
		if (entry != null) {
			entry.setActive(active);
		}
	}
	
	@Override
	public void passValueToConfigurationModule(String moduleClassName, String entryKey)
			throws WPSConfigurationException {
		ConfigurationModule module = getConfigurationModule(moduleClassName);
		for (Method method : module.getClass().getMethods()) {
			if (method.isAnnotationPresent(ConfigurationKey.class)) {
				ConfigurationKey configurationKey = method.getAnnotation(ConfigurationKey.class);
				if (configurationKey.key().equals(entryKey)) {
					Class<?>[] clazz = method.getParameterTypes();
					try {
						if (method.getParameterTypes().length != 1) {
							throw new WPSConfigurationException("The method has more than one parameter.");
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

						Object value = getConfigurationEntryValue(moduleClassName, entryKey, clazz[0]);
						method.invoke(module, value);
						LOGGER.debug("Value '" + value.toString() + "' passed to method '" + method.getName()
								+ "' in module '" + module.getClass().getName() + "'.");
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
							| WPSConfigurationException e) {
						String errorMessage = "Cannot pass value to method '" + method.getName() + "' in module '"
								+ module.getClass().getName() + "' for entry '" + configurationKey.key() + "': "
								+ e.getMessage();
						LOGGER.error(errorMessage);
						throw new WPSConfigurationException(errorMessage);
					}
				}
			}
		}
	}

	private void setStringValue(ConfigurationEntry<String> entry, Object value) throws WPSConfigurationException {
		String parsedValue = String.class.cast(valueParser.parseString(value));
		entry.setValue(parsedValue);
	}

	private void setIntegerValue(ConfigurationEntry<Integer> entry, Object value) throws WPSConfigurationException {
		Integer parsedValue = Integer.class.cast(valueParser.parseInteger(value));
		entry.setValue(parsedValue);
	}

	private void setDoubleValue(ConfigurationEntry<Double> entry, Object value) throws WPSConfigurationException {
		Double parsedValue = Double.class.cast(valueParser.parseDouble(value));
		entry.setValue(parsedValue);
	}

	private void setBooleanValue(ConfigurationEntry<Boolean> entry, Object value) throws WPSConfigurationException {
		Boolean parsedValue = Boolean.class.cast(valueParser.parseBoolean(value));
		entry.setValue(parsedValue);
	}

	private void setFileValue(ConfigurationEntry<File> entry, Object value) throws WPSConfigurationException {
		File parsedValue = File.class.cast(valueParser.parseFile(value));
		entry.setValue(parsedValue);
	}

	private void setURIValue(ConfigurationEntry<URI> entry, Object value) throws WPSConfigurationException {
		URI parsedValue = URI.class.cast(valueParser.parseURI(value));
		entry.setValue(parsedValue);
	}
}
