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
