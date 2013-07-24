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
	ValueParser valueParser;
	
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
