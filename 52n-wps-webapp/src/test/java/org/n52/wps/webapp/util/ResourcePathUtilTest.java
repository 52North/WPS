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

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.util.ReflectionTestUtils;

public class ResourcePathUtilTest {
	
	private ResourcePathUtil resourcePathUtil;
	
	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	private MockServletContext mockServletContext;
	
	@Before
	public void setup() {
		resourcePathUtil = new ResourcePathUtil();
		mockServletContext = new MockServletContext();
		ReflectionTestUtils.setField(resourcePathUtil, "servletContext", mockServletContext);
	}
	
	@After
	public void tearDown() {
		resourcePathUtil = null;
		mockServletContext = null;
	}
	
	@Test
	public void getWebAppResourcePath_validPath() {
		String releativePath = "testfiles/wpsCapabilitiesSkeleton.xml";
		String absoultPath = resourcePathUtil.getWebAppResourcePath(releativePath);
		assertTrue(new File(absoultPath).exists());
	}
	
	@Test
	public void getWebAppResourcePath_invalidPath() {
		exception.expect(RuntimeException.class);
		exception.expectMessage("nonExistingResource");
		resourcePathUtil.getWebAppResourcePath("nonExistingResource");
	}
	
	@Test
	public void getClassPathResourcePath_validPath() throws Exception {
		String releativePath = "logback.xml";
		String absoultPath = resourcePathUtil.getClassPathResourcePath(releativePath);
		assertTrue(new File(absoultPath).exists());
	}
	
	@Test
	public void getClassPathResourcePath_invalidPath() {
		exception.expect(RuntimeException.class);
		exception.expectMessage("nonExistingResource");
		resourcePathUtil.getClassPathResourcePath("nonExistingResource");
	}
}
