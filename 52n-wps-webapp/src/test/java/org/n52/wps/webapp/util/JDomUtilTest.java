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
