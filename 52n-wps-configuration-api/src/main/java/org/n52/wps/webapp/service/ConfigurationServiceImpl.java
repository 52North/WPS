/**
 * ﻿Copyright (C) 2007 - 2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *       • Apache License, version 2.0
 *       • Apache Software License, version 1.0
 *       • GNU Lesser General Public License, version 3
 *       • Mozilla Public License, versions 1.0, 1.1 and 2.0
 *       • Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.wps.webapp.service;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.n52.wps.webapp.api.AlgorithmEntry;
import org.n52.wps.webapp.api.ConfigurationCategory;
import org.n52.wps.webapp.api.ConfigurationKey;
import org.n52.wps.webapp.api.ConfigurationModule;
import org.n52.wps.webapp.api.ConfigurationType;
import org.n52.wps.webapp.api.FormatEntry;
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

/**
 * An implementation for the {@link ConfigurationService} interface. This implementation initialize and sync the
 * configurations, register configuration modules with Spring, and pass configuration entries values to configuration
 * modules setter methods.
 * <p>
 * The class uses the {@link ConfigurationDAO} for database operations and {@link ValueParser} for values validation and
 * parsing.
 * </p>
 */
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

	/*
	 * Sync configuration modules entries and values with the database
	 */
	@PostConstruct
	private void syncConfigurations() {

		buildConfigurationModulesMap();

		LOGGER.info("Initializing and syncing configuration modules.");
		for (ConfigurationModule module : getAllConfigurationModules().values()) {
			LOGGER.info("Initializing and syncing configuration module '{}'.", module.getClass().getName());

			Boolean moduleStatus = configurationDAO.getConfigurationModuleStatus(module);
			if (moduleStatus != null) {
				// module exist, set values from the database
				module.setActive(moduleStatus);
				setConfigurationModuleValuesFromDatabase(module);
			} else {
				// new module, save to the database
				configurationDAO.insertConfigurationModule(module);
				saveConfigurationModuleValuesToDatabase(module);
			}

			passConfigurationModuleValuesToMembers(module);
			syncConfigurationModuleAlgorithmEntries(module);
			syncConfigurationModuleFormatEntries(module);

			LOGGER.info("Done initializing and syncing configuration module '{}'.", module.getClass().getName());
		}
		LOGGER.info("Done initializing and syncing all configuration modules.");
	}

	/*
	 * Scan Spring context and register all beans that implement the {@code ConfigurationModule} interface
	 */
	private void buildConfigurationModulesMap() {
		Map<String, ConfigurationModule> initialModulesMap = listableBeanFactory
				.getBeansOfType(ConfigurationModule.class);

		allConfigurationModules = new HashMap<String, ConfigurationModule>();

		// build a map with the full class name as the key, and the object as the value
		for (ConfigurationModule module : initialModulesMap.values()) {
			allConfigurationModules.put(module.getClass().getName(), module);
		}
	}

	@Override
	public Map<String, ConfigurationModule> getAllConfigurationModules() {
		return allConfigurationModules;
	}

	@Override
	public Map<String, ConfigurationModule> getConfigurationModulesByCategory(ConfigurationCategory category) {
		Map<String, ConfigurationModule> allModulesMap = getAllConfigurationModules();
		Map<String, ConfigurationModule> categoryModuleMap = new HashMap<String, ConfigurationModule>();
		for (Map.Entry<String, ConfigurationModule> entry : allModulesMap.entrySet()) {
			if (entry.getValue().getCategory() == category) {
				categoryModuleMap.put(entry.getKey(), entry.getValue());
			}
		}
		LOGGER.debug("'{}' configuration modules under '{}' category are retrieved.", categoryModuleMap.size(),
				category);
		return categoryModuleMap;
	}

	@Override
	public Map<String, ConfigurationModule> getActiveConfigurationModulesByCategory(ConfigurationCategory category) {
		Map<String, ConfigurationModule> categoryModuleMap = getConfigurationModulesByCategory(category);
		Map<String, ConfigurationModule> activeCategoryModuleMap = new HashMap<String, ConfigurationModule>();
		for (Map.Entry<String, ConfigurationModule> entry : categoryModuleMap.entrySet()) {
			if (entry.getValue().getCategory() == category && entry.getValue().isActive()) {
				activeCategoryModuleMap.put(entry.getKey(), entry.getValue());
			}
		}
		LOGGER.debug("'{}' active configuration modules under '{}' category are retrieved.",
				activeCategoryModuleMap.size(), category);
		return activeCategoryModuleMap;
	}

	@Override
	public ConfigurationModule getConfigurationModule(String moduleClassName) {
		ConfigurationModule module = getAllConfigurationModules().get(moduleClassName);
		if (module != null) {
			LOGGER.debug("Module '{}' is retrieved.", moduleClassName);
		}
		return module;
	}

	@Override
	public void updateConfigurationModuleStatus(String moduleClassName, boolean status) {
		ConfigurationModule module = getConfigurationModule(moduleClassName);
		module.setActive(status);
		configurationDAO.updateConfigurationModuleStatus(module);
		LOGGER.debug("Module '{}' with status '{}' has been set and saved to the database.", moduleClassName, status);
	}

	@Override
	public ConfigurationEntry<?> getConfigurationEntry(ConfigurationModule module, String entryKey) {
		for (ConfigurationEntry<?> entry : module.getConfigurationEntries()) {
			if (entry.getKey().equals(entryKey)) {
				LOGGER.debug("Configuration entry '{}' in module '{}' is retrieved.", entryKey, module.getClass()
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
		LOGGER.debug("Value '{}' of type '{}' for configuration entry '{}' in module '{}' is retrieved.", value,
				requiredType.getSimpleName(), entry.getKey(), module.getClass().getName());
		return (T) value;
	}

	@Override
	public void setConfigurationModuleValues(String moduleClassName, String[] entryKeys, Object[] values)
			throws WPSConfigurationException {
		ConfigurationModule module = getConfigurationModule(moduleClassName);
		try {
			for (int i = 0; i < entryKeys.length; i++) {
				ConfigurationEntry<?> entry = getConfigurationEntry(module, entryKeys[i]);
				setConfigurationEntryValue(module, entry, values[i]);
			}
		} catch (WPSConfigurationException e) {
			// reset module values from the database
			setConfigurationModuleValuesFromDatabase(module);
			throw e;
		}

		saveConfigurationModuleValuesToDatabase(module);
		passConfigurationModuleValuesToMembers(module);

	}

	/*
	 * Insert or update the values of a module to the database.
	 */
	private void saveConfigurationModuleValuesToDatabase(ConfigurationModule module) {
		if (module.getConfigurationEntries() != null) {
			for (ConfigurationEntry<?> entry : module.getConfigurationEntries()) {
				saveConfigurationEntryValueToDatabase(module, entry);
			}
		}
	}

	/*
	 * Set the values of a module from the database. If the module contains an entry that it doesn't exist in the
	 * database, the method will insert the new entry into the database
	 */
	private void setConfigurationModuleValuesFromDatabase(ConfigurationModule module) {
		if (module.getConfigurationEntries() != null) {
			for (ConfigurationEntry<?> entry : module.getConfigurationEntries()) {
				Object storedValue = configurationDAO.getConfigurationEntryValue(module.getClass().getName(),
						entry.getKey());
				if (storedValue != null) {
					try {
						setConfigurationEntryValue(module, entry, storedValue);
						LOGGER.debug("Entry '{}' in module'{}' has been set with the value '{}' from the database.",
								entry.getKey(), module.getClass().getName(), storedValue);
					} catch (WPSConfigurationException e) {
						LOGGER.error("Error setting value from the database: ", e);
					}
				} else {
					// save a new entry which has been added to an existing module, but not yet saved in the database
					saveConfigurationEntryValueToDatabase(module, entry);
				}
			}
		}
	}

	/*
	 * Process and save an entry value to the database. The method will convert file and URI values to string for
	 * database storage
	 */
	private void saveConfigurationEntryValueToDatabase(ConfigurationModule module, ConfigurationEntry<?> entry) {
		Object value = entry.getValue();
		if (value != null) {
			if (entry.getType() == ConfigurationType.FILE || entry.getType() == ConfigurationType.URI) {
				value = entry.getValue().toString();
			}
		}
		if (configurationDAO.getConfigurationEntryValue(module.getClass().getName(), entry.getKey()) == null) {
			// entry doesn't exist, insert
			configurationDAO.insertConfigurationEntryValue(module.getClass().getName(), entry.getKey(), value);
		} else {
			// entry exist, update
			configurationDAO.updateConfigurationEntryValue(module.getClass().getName(), entry.getKey(), value);
		}
		LOGGER.debug("Value '{}' for entry '{}' in module'{}' has been saved to the database.", value, entry.getKey(),
				module.getClass().getName());
	}

	/*
	 * If the entry exists in the database, update the module's entry from the database, otherwise, insert the module's
	 * entry into the database
	 */
	private void syncConfigurationModuleAlgorithmEntries(ConfigurationModule module) {
		
		List<String> moduleAlgorithmNames = new ArrayList<>();
		
		if (module.getAlgorithmEntries() != null) {			
			
			for (AlgorithmEntry entry : module.getAlgorithmEntries()) {
				AlgorithmEntry storedEntry = configurationDAO.getAlgorithmEntry(module.getClass().getName(),
						entry.getAlgorithm());
				if (storedEntry != null) {		
					entry.setActive(storedEntry.isActive());
					moduleAlgorithmNames.add(entry.getAlgorithm());
					LOGGER.debug("Algorithm '{}' in module '{}' has been set to '{}' from the database.",
							entry.getAlgorithm(), module.getClass().getName(), storedEntry.isActive());
				} else {
					// save a new entry to the database
					configurationDAO.insertAlgorithmEntry(module.getClass().getName(), entry.getAlgorithm(),
							entry.isActive());
					LOGGER.debug(
							"Algorithm '{}' with active status '{}' in module '{}' has been saved to the database.",
							entry.getAlgorithm(), entry.isActive(), module.getClass().getName());
				}
			}
			
		}
		for (AlgorithmEntry entry : configurationDAO.getAlgorithmEntries(module.getClass().getName())) {				
			
			if(!moduleAlgorithmNames.contains(entry.getAlgorithm())){
				try {
					module.getAlgorithmEntries().add(entry);					
				} catch (Exception e) {
					LOGGER.info("Could not add algorithm " + entry.getAlgorithm() + " to repository " + module.getClass().getName(), e.getClass());
				}
			}
		}
	}

	/*
	 * If the entry exists in the database, update the module's entry from the database, otherwise, insert the module's
	 * entry into the database
	 */
	private void syncConfigurationModuleFormatEntries(ConfigurationModule module) {
		
		List<FormatEntry> moduleFormats = new ArrayList<>();
		
		if (module.getFormatEntries() != null) {			
			
			for (FormatEntry entry : module.getFormatEntries()) {
				FormatEntry storedEntry = configurationDAO.getFormatEntry(module.getClass().getName(),
						entry.getMimeType(), entry.getSchema(), entry.getEncoding());
				if (storedEntry != null) {		
					entry.setActive(storedEntry.isActive());
					moduleFormats.add(entry);
					LOGGER.debug("Format '{}', '{}', '{}' in module '{}' has been set to '{}' from the database.",
							entry.getMimeType(), entry.getSchema(), entry.getEncoding(), module.getClass().getName(), storedEntry.isActive());
				} else {
					// save a new entry to the database
					configurationDAO.insertFormatEntry(module.getClass().getName(), entry.getMimeType(), entry.getSchema(), entry.getEncoding(), 
							entry.isActive());
					LOGGER.debug(
							"Format '{}', '{}', '{}' with active status '{}' in module '{}' has been saved to the database.",
							entry.getMimeType(), entry.getSchema(), entry.getEncoding(), entry.isActive(), module.getClass().getName());
				}
			}
			
		}
		for (FormatEntry entry : configurationDAO.getFormatEntries(module.getClass().getName())) {				
			
			if(!moduleFormats.contains(entry)){
				module.getFormatEntries().add(entry);
			}
		}
	}
	
	/*
	 * Loop through a module configuration entries and pass the values to setter methods annotated with the entry's key
	 */
	private void passConfigurationModuleValuesToMembers(ConfigurationModule module) {
		if (module.getConfigurationEntries() != null) {
			for (ConfigurationEntry<?> entry : module.getConfigurationEntries()) {
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
		}
	}

	/*
	 * Cast an entry to the correct configuration entry type, parse the passed value, and set the entry with the parsed
	 * value.
	 */
	private void setConfigurationEntryValue(ConfigurationModule module, ConfigurationEntry<?> entry, Object value)
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
			LOGGER.debug("Value '{}' has been set for entry '{}' in module '{}'.", value, entry.getKey(), module
					.getClass().getName());
		} catch (WPSConfigurationException e) {
			// only show an error if the entry is required "not allowed to be empty"
			if (e.getMessage() != null && e.getMessage().equals("The field cannot be empty.")) {
				if (entry.isRequired()) {
					e.setField(entry.getKey());
					throw e;
				} else {
					entry.setValue(null);
					LOGGER.debug("Entry '{}' in module '{}' has been cleared and set to null.", entry.getKey(), module
							.getClass().getName());
				}
			} else {
				e.setField(entry.getKey());
				throw e;
			}
		}
	}

	@Override
	public AlgorithmEntry getAlgorithmEntry(ConfigurationModule module, String algorithm) {
		for (AlgorithmEntry entry : module.getAlgorithmEntries()) {
			if (entry.getAlgorithm().equals(algorithm)) {
				LOGGER.debug("Algorithm '{}' with status '{}' in module '{}' is retrieved.", entry.getAlgorithm(),
						entry.isActive(), module.getClass().getName());
				return entry;
			}
		}
		return null;
	}

	@Override
	public void setAlgorithmEntry(String moduleClassName, String algorithm, boolean status) {
		ConfigurationModule module = getConfigurationModule(moduleClassName);
		AlgorithmEntry entry = getAlgorithmEntry(module, algorithm);
		if (entry != null) {
			entry.setActive(status);
			configurationDAO.updateAlgorithmEntry(moduleClassName, algorithm, status);
			LOGGER.debug("Algorithm '{}' in module '{}' with status '{}' has been set and saved to the database.",
					algorithm, moduleClassName, status);
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

	@Override
	public void addAlgorithmEntry(String moduleClassName, String algorithmName) {
		boolean status = true;
		configurationDAO.insertAlgorithmEntry(moduleClassName, algorithmName,
				status);
		AlgorithmEntry algorithmEntry = new AlgorithmEntry(algorithmName, status);		
		ConfigurationModule module = getConfigurationModule(moduleClassName);		
		module.getAlgorithmEntries().add(algorithmEntry);		
		LOGGER.debug(
				"Algorithm '{}' with status '{}' has been added to module '{}' and saved to the database.",
				algorithmName, status, moduleClassName);
	}

	@Override
	public void deleteAlgorithmEntry(String moduleClassName,
			String algorithmName) {
		configurationDAO.deleteAlgorithmEntry(moduleClassName, algorithmName);
		ConfigurationModule module = getConfigurationModule(moduleClassName);
		Iterator<AlgorithmEntry> algorithmEntryIterator = module.getAlgorithmEntries().iterator();	
		
		while (algorithmEntryIterator.hasNext()) {
			AlgorithmEntry algorithmEntry = (AlgorithmEntry) algorithmEntryIterator
					.next();
			if(algorithmEntry.getAlgorithm().equals(algorithmName)){
				algorithmEntryIterator.remove();
				break;
			}
		}
	}

	@Override
	public void setFormatEntry(String moduleClassName, String mimeType, String schema,
			String encoding, boolean status) {
		configurationDAO.updateFormatEntry(moduleClassName, mimeType, schema, encoding, status);
		LOGGER.debug(
				"Format with mime type '{}', schema '{}', encoding '{}' and status '{}' of module '{}' has been updated.",
				mimeType, schema, encoding, status, moduleClassName);	
	}

	@Override
	public void addFormatEntry(String moduleClassName, String mimeType, String schema,
			String encoding) {
		boolean status = true;
		configurationDAO.insertFormatEntry(moduleClassName, mimeType, schema, encoding, status);
		FormatEntry formatEntry = new FormatEntry(mimeType, schema, encoding, status);	
		ConfigurationModule module = getConfigurationModule(moduleClassName);		
		module.getFormatEntries().add(formatEntry);
		LOGGER.debug(
				"Format with mime type '{}', schema '{}', encoding '{}' and status '{}' has been added to module '{}' and saved to the database.",
				mimeType, schema, encoding, status, moduleClassName);	
	}

	@Override
	public void deleteFormatEntry(String moduleClassName, String mimeType, String schema, String encoding) {
		configurationDAO.deleteFormatEntry(moduleClassName, mimeType, schema, encoding);
		ConfigurationModule module = getConfigurationModule(moduleClassName);
		Iterator<FormatEntry> formatEntryIterator = module.getFormatEntries().iterator();	
		/*
		 * FormatEntry class has a built-in method for comparison including checks for null values
		 */		
		FormatEntry tempFormatEntryForComparison = new FormatEntry(mimeType, schema, encoding, true);
		while (formatEntryIterator.hasNext()) {
			FormatEntry formatEntry = (FormatEntry) formatEntryIterator
					.next();
			if(formatEntry.equals(tempFormatEntryForComparison)){
				formatEntryIterator.remove();
				break;
			}
		}
	}
}
