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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URI;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;

public class ValueParserTest {
	
	@Autowired
	private ValueParser valueParser;
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Before
	public void setup() {
		valueParser = new ValueParser();
	}
	
	@Test
	public void testParseString() throws WPSConfigurationException {
		Object validString = "Test String";
		String parsedString = valueParser.parseString(validString);
		assertEquals(parsedString, "Test String");
		
		Object emptryString = "";
		exception.expect(WPSConfigurationException.class);
		valueParser.parseString(emptryString);
	}
	
	@Test
	public void testParseInteger() throws WPSConfigurationException {
		Object validInteger = "55";
		int parsedInteger = valueParser.parseInteger(validInteger);
		assertEquals(parsedInteger, 55);
		
		Object invalidInteger = "Not integer";
		exception.expect(WPSConfigurationException.class);
		valueParser.parseInteger(invalidInteger);
	}
	
	@Test
	public void testParseDouble() throws WPSConfigurationException {
		Object validDouble = "2.3";
		double parsedDouble = valueParser.parseDouble(validDouble);
		assertEquals(parsedDouble, 2.3, 0);
		
		Object invalidDouble = "Not double";
		exception.expect(WPSConfigurationException.class);
		valueParser.parseDouble(invalidDouble);
	}
	
	@Test
	public void testParseBoolean() throws WPSConfigurationException {
		Object validBoolean = "true";
		boolean parsedBoolean = valueParser.parseBoolean(validBoolean);
		assertEquals(parsedBoolean, true);
		
		Object invalidBoolean = "Not boolean";
		exception.expect(WPSConfigurationException.class);
		valueParser.parseBoolean(invalidBoolean);
	}
	
	@Test
	public void testParseFile() throws WPSConfigurationException {
		Object validPath = "file_path";
		File parsedFile = valueParser.parseFile(validPath);
		assertEquals(parsedFile.getPath(), "file_path");
		
		Object emptyPath = "";
		exception.expect(WPSConfigurationException.class);
		valueParser.parseFile(emptyPath);
	}
	
	@Test
	public void testParseURI() throws WPSConfigurationException {
		Object validPath = "uri_path";
		URI parsedURI = valueParser.parseURI(validPath);
		assertEquals(parsedURI.getPath(), "uri_path");
		
		Object emptyPath = "";
		exception.expect(WPSConfigurationException.class);
		valueParser.parseURI(emptyPath);
	}
}
