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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.stereotype.Component;

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
			throw new WPSConfigurationException(new NullPointerException("The field cannot be empty."));
		}
	}
}
