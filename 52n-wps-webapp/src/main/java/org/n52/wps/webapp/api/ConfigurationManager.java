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

import java.util.List;
import java.util.Map;

import org.n52.wps.webapp.api.types.ConfigurationEntry;
import org.n52.wps.webapp.entities.LogConfigurations;
import org.n52.wps.webapp.entities.ServiceIdentification;
import org.n52.wps.webapp.entities.ServiceProvider;
import org.n52.wps.webapp.entities.User;

public interface ConfigurationManager {

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
	Map<String, ConfigurationModule> getAllConfigurationModulesByCategory(ConfigurationCategory category);

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
	 * Get a configuration entry.
	 * 
	 * @param moduleClassName
	 *            the fully qualified name of the module holding the configuration entry
	 * @param entryKey
	 *            the configuration entry key
	 * @return The configuration entry or {@code null} if no entry is found.
	 */
	ConfigurationEntry<?> getConfigurationEntry(String moduleClassName, String entryKey);

	/**
	 * Get the configuration entry value and return it as the expected type.
	 * 
	 * @param module
	 *            the fully qualified name of the module holding the configuration entry
	 * @param entryKey
	 *            the configuration entry key
	 * @param requiredType
	 *            the required type of the return value
	 * @return The entry value in the required type
	 * @throws WPSConfigurationException
	 *             if the entry's value type cannot be be parsed to the required type
	 */
	<T> T getConfigurationEntryValue(String moduleClassName, String entryKey, Class<T> requiredType)
			throws WPSConfigurationException;

	/**
	 * Set the value of a configuration entry. The {@code Object} value will be parsed to the entry type.
	 * 
	 * @param moduleClassName
	 *            the fully qualified name of the module holding the configuration entry
	 * @param entryKey
	 *            the entry key
	 * @param value
	 *            the value to be set
	 * @throws WPSConfigurationException
	 *             if the value cannot be parsed to the correct entry type
	 */
	void setConfigurationEntryValue(String moduleClassName, String entryKey, Object value)
			throws WPSConfigurationException;

	/**
	 * Get an algorithm entry.
	 * 
	 * @param moduleClassName
	 *            the fully qualified name of the module holding the algorithm entry
	 * @param algorithm
	 *            the algorithm name
	 * @return The algorithm entry or {@code null} if no entry is found.
	 */
	AlgorithmEntry getAlgorithmEntry(String moduleClassName, String algorithm);

	/**
	 * Set the value of an algorithm entry.
	 * 
	 * @param moduleClassName
	 *            the fully qualified name of the module holding the algorithm entry
	 * @param algorithm
	 *            the algorithm name
	 * @param active
	 *            the algorithm status
	 */
	void setAlgorithmEntry(String moduleClassName, String algorithm, boolean active);
	
	/**
	 * Get user by user id
	 * 
	 * @param userId
	 *            the id of the user
	 * @return The user specified by the id
	 */
	User getUser(int userId);

	/**
	 * Get user by username
	 * 
	 * @param username
	 *            the username of the user
	 * @return The user specified by the username
	 */
	User getUser(String username);

	/**
	 * Get all users
	 * 
	 * @return The list of all users
	 */
	List<User> getAllUsers();

	/**
	 * Insert new user
	 * 
	 * @param user
	 */
	void insertUser(User user);

	/**
	 * Update existing user
	 * 
	 * @param user
	 */
	void updateUser(User user);

	/**
	 * Delete user
	 * 
	 * @param userId
	 *            the id of the user to be deleted
	 */
	void deleteUser(int userId);
	
	/**
	 * Get the service identification information
	 * 
	 * @return service identification information
	 * @throws WPSConfigurationException
	 *             if the information cannot be retrieved
	 */
	ServiceIdentification getServiceIdentification() throws WPSConfigurationException;

	/**
	 * Get the service provider information
	 * 
	 * @return service provider information
	 * @throws WPSConfigurationException
	 *             if the information cannot be retrieved
	 */
	ServiceProvider getServiceProvider() throws WPSConfigurationException;

	/**
	 * Save the service identification information to file
	 * 
	 * @param serviceIdentification
	 * @throws WPSConfigurationException
	 *             if the file cannot be written
	 */
	void saveServiceIdentification(ServiceIdentification serviceIdentification) throws WPSConfigurationException;

	/**
	 * Save the service provider information to file
	 * 
	 * @param serviceProvider
	 * @throws WPSConfigurationException
	 *             if the file cannot be written
	 */
	void saveServiceProvider(ServiceProvider serviceProvider) throws WPSConfigurationException;
	
	/**
	 * Get the {@code LogConfigurations} object
	 * 
	 * @return Log configurations object
	 * @throws WPSConfigurationException
	 *             if the file cannot be retrieved
	 */
	LogConfigurations getLogConfigurations() throws WPSConfigurationException;
	
	/**
	 * Save a {@code LogConfigurations} object to log file
	 * 
	 * @param logConfigurations
	 *            the log configurations object
	 * @throws WPSConfigurationException
	 *             if the file cannot be saved
	 */
	void saveLogConfigurations(LogConfigurations logConfigurations) throws WPSConfigurationException;
}
