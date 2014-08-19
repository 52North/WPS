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
package org.n52.wps.webapp.dao;

import java.util.List;

import org.n52.wps.webapp.api.AlgorithmEntry;
import org.n52.wps.webapp.api.ConfigurationModule;
import org.n52.wps.webapp.api.FormatEntry;

/**
 * CRUD operations on configuration modules, entries, and values to the underlying datastore.
 */
public interface ConfigurationDAO {

	/**
	 * Insert a new module
	 * 
	 * @param module
	 *            the module to be inserted
	 * 
	 */
	void insertConfigurationModule(ConfigurationModule module);

	/**
	 * Update an existing module status
	 * 
	 * @param module
	 *            the module to be updated
	 */
	void updateConfigurationModuleStatus(ConfigurationModule module);
	
	/**
	 * Get the active/inactive status of a configuration module
	 * 
	 * @param module
	 * @return The module status
	 */
	Boolean getConfigurationModuleStatus(ConfigurationModule module);

	/**
	 * Get the stored configuration entry value
	 * 
	 * @param moduleClassName
	 *            the fully qualified name of the module holding the configuration entry
	 * @param entryKey
	 *            the configuration entry key
	 * @return The stored configuration entry value or {@code null} if no entry is found.
	 */
	Object getConfigurationEntryValue(String moduleClassName, String entryKey);

	/**
	 * Insert new configuration entry value
	 * 
	 * @param moduleClassName
	 *            the fully qualified name of the module holding the configuration entry
	 * @param entryKey
	 *            the configuration entry key
	 * @param value
	 *            the value to be stored
	 */
	void insertConfigurationEntryValue(String moduleClassName, String entryKey, Object value);

	/**
	 * Update a configuration entry value
	 * 
	 * @param moduleClassName
	 *            the fully qualified name of the module holding the configuration entry
	 * @param entryKey
	 *            the configuration entry key
	 * @param value
	 *            the value to be stored
	 */
	void updateConfigurationEntryValue(String moduleClassName, String entryKey, Object value);

	/**
	 * Get the stored algorithm entry value
	 * 
	 * @param moduleClassName
	 *            the fully qualified name of the module holding the algorithm entry
	 * @param algorithm
	 *            the algorithm name
	 * @return The algorithm entry or {@code null} if no entry is found.
	 */
	AlgorithmEntry getAlgorithmEntry(String moduleClassName, String algorithm);

	/**
	 * Insert new algorithm entry value
	 * 
	 * @param moduleClassName
	 *            the fully qualified name of the module holding the algorithm entry
	 * @param algorithm
	 *            the algorithm name
	 * @param active
	 *            the algorithm status
	 */
	void insertAlgorithmEntry(String moduleClassName, String algorithm, boolean active);

	/**
	 * Update an algorithm entry
	 * 
	 * @param moduleClassName
	 *            the fully qualified name of the module holding the algorithm entry
	 * @param algorithm
	 *            the algorithm name to be updated
	 * @param active
	 *            the new algorithm status
	 */
	void updateAlgorithmEntry(String moduleClassName, String algorithm, boolean active);

	/**
	 * Get all algorithm entries
	 * 
	 * @param moduleClassName
	 *            the fully qualified name of the module holding the algorithm entries
	 * @return All algorithm entries of the module.
	 */	
	List<AlgorithmEntry> getAlgorithmEntries(String moduleClassName);

	/**
	 * Delete an algorithm entries
	 * 
	 * @param moduleClassName
	 *            the fully qualified name of the module holding the algorithm entries
	 * @param algorithm
	 *            the algorithm to be deleted	 
	 */
	void deleteAlgorithmEntry(String moduleClassName, String algorithmName);
	
	/**
	 * Get the stored format entry value
	 * 
	 * @param moduleClassName
	 *            the fully qualified name of the module holding the format entry
	 * @param format TODO: update
	 *            the format name
	 * @return The format entry or {@code null} if no entry is found.
	 */
	FormatEntry getFormatEntry(String moduleClassName, String mimeType, String schema, String encoding);

	/**
	 * TODO: update
	 * Insert new format entry value
	 * 
	 * @param moduleClassName
	 *            the fully qualified name of the module holding the format entry
	 * @param format
	 *            the format name
	 * @param active
	 *            the format status
	 */
	void insertFormatEntry(String moduleClassName, String mimeType, String schema, String encoding, boolean active);

	/**TODO: update
	 * Update an format entry
	 * 
	 * @param moduleClassName
	 *            the fully qualified name of the module holding the format entry
	 * @param format
	 *            the format name to be updated
	 * @param active
	 *            the new format status
	 */
	void updateFormatEntry(String moduleClassName, String mimeType, String schema, String encoding, boolean active);

	/**
	 * Get all format entries
	 * 
	 * @param moduleClassName
	 *            the fully qualified name of the module holding the format entries
	 * @return All format entries of the module.
	 */	
	List<FormatEntry> getFormatEntries(String moduleClassName);

	/**TODO: update
	 * Delete an format entries
	 * 
	 * @param format
	 *            the format to be deleted	 
	 */
	void deleteFormatEntry(String moduleClassName, String mimeType, String schema, String encoding);
}