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

}
