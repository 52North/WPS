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

import java.util.Map;

import org.n52.wps.webapp.api.AlgorithmEntry;
import org.n52.wps.webapp.api.ConfigurationCategory;
import org.n52.wps.webapp.api.ConfigurationModule;
import org.n52.wps.webapp.api.WPSConfigurationException;
import org.n52.wps.webapp.api.types.ConfigurationEntry;

/**
 * Retrieves and sets configuration modules, configurations entries, algorithm entries, and values. This interface is
 * responsible for managing standard configurations in a type safe manner.
 * <p>
 * Different service interfaces are used to handle special configurations such as log or user configurations.
 * </p>
 * 
 * @see UserService
 * @see LogConfigurationsService
 * @see CapabilitiesService
 * @see LogConfigurationsService
 */
public interface ConfigurationService {

	/**
	 * Get all classes that implements the {@code ConfigurationModule} interface. Modules are mapped by their fully
	 * qualified name.
	 * 
	 * @return A map of all configuration modules.
	 */
	Map<String, ConfigurationModule> getAllConfigurationModules();

	/**
	 * Get all configuration modules of a particular category.
	 * 
	 * @param category
	 *            the category of the modules
	 * @return A map of all configuration modules of the specified category.
	 * @see ConfigurationCategory
	 */
	Map<String, ConfigurationModule> getConfigurationModulesByCategory(ConfigurationCategory category);

	/**
	 * Get only active configuration modules of a particular category.
	 * 
	 * @param category
	 *            the category of the modules
	 * @return A map of all active configuration modules of the specified category.
	 * @see ConfigurationCategory
	 */
	Map<String, ConfigurationModule> getActiveConfigurationModulesByCategory(ConfigurationCategory category);

	/**
	 * Get a configuration module by its fully qualified name.
	 * 
	 * @param moduleClassName
	 *            the fully qualified name of the module required
	 * @return The configuration module or {@code null} if no module is found.
	 */
	ConfigurationModule getConfigurationModule(String moduleClassName);

	/**
	 * Update a configuration module activation status
	 * 
	 * @param moduleClassName
	 *            the fully qualified name of the module to be updated
	 * @param status
	 *            the new status
	 */
	void updateConfigurationModuleStatus(String moduleClassName, boolean status);

	/**
	 * Get a configuration entry.
	 * 
	 * @param module
	 *            the configuration module holding the configuration entry
	 * @param entryKey
	 *            the configuration entry key
	 * @return The configuration entry or {@code null} if no entry is found.
	 */
	ConfigurationEntry<?> getConfigurationEntry(ConfigurationModule module, String entryKey);

	/**
	 * Get the configuration entry value and return it as the required type.
	 * 
	 * @param module
	 *            the configuration module holding the configuration entry
	 * @param entry
	 *            the configuration entry
	 * @param requiredType
	 *            the required type
	 * @return The entry value in the required type
	 * @throws WPSConfigurationException
	 *             if the entry value cannot be be parsed to the required type
	 */
	<T> T getConfigurationEntryValue(ConfigurationModule module, ConfigurationEntry<?> entry, Class<T> requiredType)
			throws WPSConfigurationException;

	/**
	 * Set the values for a configuration module. The {@code Object} values will be parsed to the entry types.
	 * 
	 * @param moduleClassName
	 *            the fully qualified name of the module holding the configuration entry
	 * @param entryKeys
	 *            the keys of the entries to be set
	 * @param values
	 *            the values to be set
	 * @throws WPSConfigurationException
	 *             if the value cannot be parsed to the correct entry type
	 */
	void setConfigurationModuleValues(String moduleClassName, String[] entryKeys, Object[] values)
			throws WPSConfigurationException;

	/**
	 * Get an algorithm entry.
	 * 
	 * @param module
	 *            the configuration module holding the algorithm entry
	 * @param algorithm
	 *            the algorithm name
	 * @return The algorithm entry or {@code null} if no entry is found.
	 */
	AlgorithmEntry getAlgorithmEntry(ConfigurationModule module, String algorithm);

	/**
	 * Set the value of an algorithm entry.
	 * 
	 * @param moduleClassName
	 *            the fully qualified name of the module holding the algorithm entry
	 * @param algorithm
	 *            the algorithm name
	 * @param status
	 *            the algorithm active status
	 */
	void setAlgorithmEntry(String moduleClassName, String algorithm, boolean status);

	/**
	 * Adds a new algorithm entry.
	 * 
	 * @param moduleClassName
	 *            the fully qualified name of the module holding the algorithm entry
	 * @param algorithmName
	 *            the algorithm name
	 */
	void addAlgorithmEntry(String moduleClassName, String algorithmName);
	
	/**
	 * Delete an algorithm entry.
	 * 
	 * @param moduleClassName
	 *            the fully qualified name of the module holding the algorithm entry
	 * @param algorithmName
	 *            the algorithm name
	 */
	void deleteAlgorithmEntry(String moduleClassName, String algorithmName);
	
	/**
	 * Set the value of an format entry.
	 *  
	 * @param mimeType
	 *            the mime type of the format entry	
	 * @param schema
	 *            the schema of the format entry	
	 * @param encoding
	 *            the encoding of the format entry
	 * @param status
	 *            the format active status
	 */
	void setFormatEntry(String mimeType, String schema, String encoding, boolean status);

	/**
	 * Adds a new format entry.
	 * 
	 * @param moduleClassName
	 *            the fully qualified name of the module holding the format entry
	 * @param mimeType
	 *            the mime type of the format entry	
	 * @param schema
	 *            the schema of the format entry	
	 * @param encoding
	 *            the encoding of the format entry
	 */
	void addFormatEntry(String moduleClassName, String mimeType, String schema, String encoding);
	
	/**
	 * Delete an format entry.
	 * 
	 * @param moduleClassName
	 *            the fully qualified name of the module holding the format entry
	 * @param mimeType
	 *            the mime type of the format entry	
	 * @param schema
	 *            the schema of the format entry	
	 * @param encoding
	 *            the encoding of the format entry
	 */
	void deleteFormatEntry(String moduleClassName, String mimeType, String schema, String encoding);
}
