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
package org.n52.wps.webapp.util;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.jdom.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class JDomUtilTest {
	
	private JDomUtil jDomUtil;
	private String path = JDomUtilTest.class.getResource("/testfiles/").getPath();
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Before
	public void setup() {
		jDomUtil = new JDomUtil();
	}
	
	@After
	public void tearDown() {
		jDomUtil = null;
	}
	
	@Test
	public void parse_validXmlFile_validPath() throws Exception {
		Document document = jDomUtil.parse(path + "testdata.xml");
		assertEquals("Products", document.getRootElement().getName());
		assertEquals("Product", document.getRootElement().getChild("Product").getName());
		assertEquals("PC", document.getRootElement().getChild("Product").getChild("Name").getValue());
		assertEquals("HP", document.getRootElement().getChild("Product").getChild("Brand").getValue());
		assertEquals("999", document.getRootElement().getChild("Product").getChild("Price").getValue());
	}
	
	@Test
	public void parse_invalidPath() throws Exception {
		exception.expect(RuntimeException.class);
		exception.expectMessage("Unable to parse");
		jDomUtil.parse(path + "non_existing_file");
	}
	
	@Test
	public void parse_invalidXmlFormat_validPath() throws Exception {
		exception.expect(RuntimeException.class);
		exception.expectMessage("Unable to parse");
		jDomUtil.parse(path + "52n-logo.gif");
	}
	
	@Test
	public void write_validDocument_validPath() throws Exception {
		Document document = jDomUtil.parse(path + "testdata.xml");
		
		assertEquals("PC", document.getRootElement().getChild("Product").getChild("Name").getValue());
		document.getRootElement().getChild("Product").getChild("Name").setText("New PC Name");
		jDomUtil.write(document, path + "testdata2.xml");
		
		Document updatedDocument = jDomUtil.parse(path + "testdata2.xml");
		assertEquals("New PC Name", updatedDocument.getRootElement().getChild("Product").getChild("Name").getValue());
		
		new File(path + "testdata2.xml").delete();
	}
	
	@Test
	public void write_nullDocument_validPath() throws Exception {
		Document document = null;
		exception.expect(NullPointerException.class);
		jDomUtil.write(document, path + "testdata2.xml");
	}
	
	@Test
	public void write_validDocument_invalidPath() throws Exception {
		Document document = jDomUtil.parse(path + "testdata.xml");
		exception.expect(RuntimeException.class);
		exception.expectMessage("Unable to write");
		jDomUtil.write(document, path + "2/" + "testdata2.xml");
	}
}
