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

import org.n52.wps.webapp.api.types.ConfigurationEntry;

public interface ConfigurationModule {
	/**
	 * The name of the module which will appear on the user interface. (e.g. Grass Repository)
	 * 
	 * @return the name of the configuration module
	 */
	String getModuleName();

	/**
	 * Whether the module is active or inactive by default
	 * 
	 * @return the status of the module
	 */
	boolean isActive();

	/**
	 * Identify the category for the configuration module. See {@code ConfigurationCategory} for a list of avaliable
	 * categories.
	 * 
	 * @return the category for the configuration module
	 */
	ConfigurationCategory getCategory();

	/**
	 * List of all configurations entries for this configuration module. Configuration entries can be of type String,
	 * Integer, Boolean, Double, File, and URI
	 * 
	 * @return the list of configuration entries
	 * @see StringConfigurationEntry
	 * @see IntegerConfigurationEntry
	 * @see BooleanConfigurationEntry
	 * @see DoubleConfigurationEntry
	 * @see FileConfigurationEntry
	 * @see URIConfigurationEntry
	 */
	List<? extends ConfigurationEntry<?>> getConfigurationEntries();

	/**
	 * List of algorithms for this configuration module.
	 * 
	 * @return the list of algorithms
	 * @see AlgorithmEntry
	 */
	List<AlgorithmEntry> getAlgorithmEntries();
}
