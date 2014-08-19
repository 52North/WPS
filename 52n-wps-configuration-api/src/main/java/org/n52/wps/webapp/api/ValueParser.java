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
package org.n52.wps.webapp.api;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.stereotype.Component;

/**
 * Used by the {@link ConfigurationService} to validate and parse values to the correct configuration entry type.
 */
@Component
public class ValueParser {

	public String parseString(Object value) throws WPSConfigurationException {
		nullOrEmptyCheck(value);
		try {
			return String.valueOf(value);
		} catch (IllegalArgumentException e) {
			throw new WPSConfigurationException(e);
		}
	}

	public Integer parseInteger(Object value) throws WPSConfigurationException {
		nullOrEmptyCheck(value);
		try {
			return Integer.parseInt(String.valueOf(value));
		} catch (IllegalArgumentException e) {
			throw new WPSConfigurationException("'" + value + "' is not a valid integer value.");
		}
	}

	public Double parseDouble(Object value) throws WPSConfigurationException {
		nullOrEmptyCheck(value);
		try {
			return Double.parseDouble(String.valueOf(value));
		} catch (IllegalArgumentException e) {
			throw new WPSConfigurationException("'" + value + "' is not a valid double value.");
		}
	}

	public Boolean parseBoolean(Object value) throws WPSConfigurationException {
		nullOrEmptyCheck(value);
		String stringValue = value.toString();
		if ((stringValue.trim().equalsIgnoreCase("true")) || (stringValue.trim().equalsIgnoreCase("false"))) {
			return Boolean.valueOf(String.valueOf(value));
		} else {
			throw new WPSConfigurationException("'" + value + "' is not a valid boolean value.");
		}

	}

	public File parseFile(Object value) throws WPSConfigurationException {
		nullOrEmptyCheck(value);
		try {
			return new File(String.valueOf(value));
		} catch (IllegalArgumentException e) {
			throw new WPSConfigurationException("'" + value + "' is not a valid file path.");
		}

	}

	public URI parseURI(Object value) throws WPSConfigurationException {
		nullOrEmptyCheck(value);
		try {
			return new URI(String.valueOf(value));
		} catch (URISyntaxException e) {
			throw new WPSConfigurationException("'" + value + "' is not a valid URI path.");
		}
	}

	private void nullOrEmptyCheck(Object value) throws WPSConfigurationException {
		if (value == null || value.toString().trim().isEmpty()) {
			throw new WPSConfigurationException("The field cannot be empty.");
		}
	}
}
