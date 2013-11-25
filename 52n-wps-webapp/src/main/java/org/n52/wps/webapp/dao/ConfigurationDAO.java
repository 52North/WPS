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

package org.n52.wps.webapp.dao;

import org.n52.wps.webapp.api.AlgorithmEntry;
import org.n52.wps.webapp.api.ConfigurationModule;

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
}