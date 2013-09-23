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

package org.n52.wps.webapp.api.types;

import org.n52.wps.webapp.api.ConfigurationModule;
import org.n52.wps.webapp.api.ConfigurationType;

/**
 * Used by {@link ConfigurationModule} implementations to create type safe configuration entries by using implementing classes.
 * 
 * @see StringConfigurationEntry
 * @see IntegerConfigurationEntry
 * @see BooleanConfigurationEntry
 * @see DoubleConfigurationEntry
 * @see FileConfigurationEntry
 * @see URIConfigurationEntry
 */
public abstract class ConfigurationEntry<T> {
	private T value;
	private String key;
	private String title;
	private String description;
	private boolean required;
	private ConfigurationType type;

	protected ConfigurationEntry(String key, String title, ConfigurationType type) {
		this.key = key;
		this.title = title;
		this.type = type;
	}

	protected ConfigurationEntry(String key, String title, String description, boolean required, T value,
			ConfigurationType type) {
		this.key = key;
		this.title = title;
		this.description = description;
		this.required = required;
		this.value = value;
		this.type = type;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public ConfigurationType getType() {
		return type;
	}
}
