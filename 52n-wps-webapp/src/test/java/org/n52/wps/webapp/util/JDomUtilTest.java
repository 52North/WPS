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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.n52.wps.webapp.common.AbstracTest;
import org.springframework.beans.factory.annotation.Autowired;

public class JDomUtilTest extends AbstracTest {
	
	@Autowired
	private ResourcePathUtil resourcePathUtil;
	
	@Autowired
	private JDomUtil jDomUtil;
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	@Test
	public void testLoad() throws IOException, JDOMException {
		String releativePath = "config" + File.separator +  "wpsCapabilitiesSkeleton.xml";
		String absoultPath = resourcePathUtil.getWebAppResourcePath(releativePath);
		Document document = jDomUtil.load(absoultPath);
		assertNotNull(document);
		assertEquals(document.getRootElement().getName(), "Capabilities");
		assertEquals(document.getRootElement().getNamespaceURI(), "http://www.opengis.net/wps/1.0.0");
		assertEquals(document.getRootElement().getAttributeValue("service"), "WPS");
	}
	
	@Test
	public void testNonExistingFileLoad() throws JDOMException, IOException {
		String releativePath = "nonExistingFile.xml";
		String absoultPath = resourcePathUtil.getWebAppResourcePath(releativePath);
		assertFalse(new File(absoultPath).exists());
		exception.expect(FileNotFoundException.class);
		jDomUtil.load(absoultPath);
	}
	
	@Test
	public void testWrongFormatLoad() throws JDOMException, IOException {
		String releativePath = "webAdmin" + File.separator + "images" + File.separator + "add.png";
		String absoultPath = resourcePathUtil.getWebAppResourcePath(releativePath);
		exception.expect(IOException.class);
		jDomUtil.load(absoultPath);
	}
	
	@Test
	public void testWrite() throws JDOMException, IOException {
		
		//load file and check the value of service attribute
		String releativePath = "config" + File.separator +  "wpsCapabilitiesSkeleton.xml";
		String absoultPath = resourcePathUtil.getWebAppResourcePath(releativePath);
		Document document = jDomUtil.load(absoultPath);
		assertEquals(document.getRootElement().getAttributeValue("service"), "WPS");
		
		//edit service attribute and write to a new file
		document.getRootElement().setAttribute("service", "TestValue@^45");
		String newReleativePath = "config" + File.separator +  "wpsCapabilitiesSkeleton2.xml";
		String newAbsoultPath = resourcePathUtil.getWebAppResourcePath(newReleativePath);
		jDomUtil.write(document, newAbsoultPath);
		
		//check new file
		document = jDomUtil.load(newAbsoultPath);
		assertEquals(document.getRootElement().getAttributeValue("service"), "TestValue@^45");
		
		//delete test file
		new File(newAbsoultPath).delete();
	}
	
}
