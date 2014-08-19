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
package org.n52.wps.webapp.api.types;

import org.n52.wps.webapp.api.ConfigurationModule;
import org.n52.wps.webapp.api.ConfigurationType;

/**
 * Used by {@link ConfigurationModule} implementations to create type safe configuration entries using the extending classes.
 * 
 * @see StringConfigurationEntry
 * @see IntegerConfigurationEntry
 * @see BooleanConfigurationEntry
 * @see DoubleConfigurationEntry
 * @see FileConfigurationEntry
 * @see URIConfigurationEntry
 * 
 * TODO: add isActive here?!
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
