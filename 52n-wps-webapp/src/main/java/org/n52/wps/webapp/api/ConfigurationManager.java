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

import org.n52.wps.webapp.api.types.ConfigurationEntry;

public interface ConfigurationManager {

	/**
	 * Get all the configuration modules and configuration entries for each module. Configuration entries are mapped by
	 * their keys.
	 * <p>
	 * To get a specific configuration entry {@code getAllConfigurationModule().get(module).get(entryKey)}.
	 * </p>
	 * 
	 * @return the map of map for all configuration modules and entries
	 */
	Map<ConfigurationModule, Map<String, ConfigurationEntry<?>>> getAllConfigurationModules();

	/**
	 * Get the configuration module instance by passing the class of the configuration module.
	 * 
	 * @param clazz
	 *            the class of the module required
	 * @return the instance of the configuration module, or {@code null} if no module is found.
	 */
	ConfigurationModule getConfigurationModule(Class<? extends ConfigurationModule> clazz);

	/**
	 * Get the configuration module instance by passing the fully qualified name of the module's class.
	 * 
	 * @param moduleClassName
	 *            the fully qualified name of the module required
	 * @return the instance of the configuration module, or {@code null} if no module is found.
	 */
	ConfigurationModule getConfigurationModuleByName(String moduleClassName);

	/**
	 * Set the value of a configuration entry. The method retrieves the entry using the entry key, checks the entry
	 * type, and attempts to parse the value before setting it.
	 * 
	 * @param module
	 *            the configuration module which holds the configuration entry
	 * @param entryKey
	 *            the entry key
	 * @param value
	 *            the value to be set
	 * @throws WPSConfigurationException
	 *             if the value cannot be parsed to the correct entry type
	 */
	void setValue(ConfigurationModule module, String entryKey, Object value) throws WPSConfigurationException;

	/**
	 * Sync the value of a configuration entry from the configurations datasource during initialization. The method is
	 * similar to {@link #setValue} except it doesn't store the value after updating the entry (since the value is
	 * already coming from the stored data). This method is used by the internally sync configuration entries. Use
	 * {@link #setValue} if you have a value passed from a client (i.e. controller).
	 * 
	 * @param module
	 *            the configuration module which holds the configuration entry
	 * @param entryKey
	 *            the configuration entry key
	 * @param value
	 *            the value to be set
	 * @throws WPSConfigurationException
	 *             if the value cannot be parsed to the correct entry type;
	 */
	void syncValue(ConfigurationModule module, String entryKey, Object value) throws WPSConfigurationException;;

	/**
	 * Get the configuration entry value and return it as the expected type. Example:
	 * {@code Boolean value = getValue(module, "key",
	 * Boolean.class);}. This method is used internally by the {@link passValueToConfigurationModule} method.
	 * 
	 * @param module
	 *            the configuration module which holds the configuration entry
	 * @param entryKey
	 *            the configuration entry key
	 * @param requiredType
	 *            the required type of the return value
	 * @return the entry value in the required type
	 * @throws WPSConfigurationException
	 *             if the entry's value type cannot be be parsed to the required type
	 */
	<T> T getValue(ConfigurationModule module, String entryKey, Class<T> requiredType) throws WPSConfigurationException;

	/**
	 * Pass the value of the configuration entry to an annotated set method in a configuration module. To have a value
	 * passed to a member variable in the configuration module, annotate its setter method with
	 * {@code ConfigurationKey(key="entry.key")}. The member variable type and the configuration entry must be of the
	 * same type.
	 * 
	 * @param module
	 *            the configuration module which holds the setter method
	 * @param entryKey
	 *            the configuration entry key
	 * @throws WPSConfigurationException
	 *             if the entry value type is not compatible with the member variable type, or if the setter method have more
	 *             than one parameter
	 */
	void passValueToConfigurationModule(ConfigurationModule module, String entryKey) throws WPSConfigurationException;

}
