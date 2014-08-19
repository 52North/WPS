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
package org.n52.wps.webapp.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;

import java.util.SortedMap;
import java.util.TreeMap;

import org.jdom.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.n52.wps.webapp.entities.LogConfigurations;
import org.n52.wps.webapp.util.JDomUtil;
import org.n52.wps.webapp.util.ResourcePathUtil;

public class XmlLogConfigurationsDAOTest {

	@InjectMocks
	private LogConfigurationsDAO logConfigurationsDAO;

	@Mock
	private ResourcePathUtil resourcePathUtil;

	@Mock
	private JDomUtil jDomUtil;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	private Document originalTestLogDocument;
	private String testLogDocumentPath;

	@Before
	public void setUp() throws Exception {
		logConfigurationsDAO = new XmlLogConfigurationsDAO();
		MockitoAnnotations.initMocks(this);
		testLogDocumentPath = XmlLogConfigurationsDAOTest.class.getResource("/testfiles/testlogback.xml").getPath();
		when(resourcePathUtil.getClassPathResourcePath(XmlLogConfigurationsDAO.FILE_NAME)).thenReturn(
				testLogDocumentPath);
		when(jDomUtil.parse(testLogDocumentPath)).thenCallRealMethod();
		doCallRealMethod().when(jDomUtil).write(any(Document.class), eq(testLogDocumentPath));
		originalTestLogDocument = jDomUtil.parse(testLogDocumentPath);
	}

	@After
	public void resetTestDocument() throws Exception {
		jDomUtil.write(originalTestLogDocument, testLogDocumentPath);
		logConfigurationsDAO = null;
	}

	@Test
	public void getLogConfigurations() throws Exception {
		LogConfigurations logConfigurations = logConfigurationsDAO.getLogConfigurations();
		assertEquals("${logFile}/%d{yyyy-MM-dd}.log", logConfigurations.getWpsfileAppenderFileNamePattern());
		assertEquals(14, logConfigurations.getWpsfileAppenderMaxHistory());
		assertEquals("%d{ISO8601} [%t] %-5p %c: %m%n", logConfigurations.getWpsfileAppenderEncoderPattern());
		assertEquals("%d{ISO8601} [%t] %-5p %c: %m%n", logConfigurations.getWpsconsoleEncoderPattern());
		assertEquals("INFO", logConfigurations.getRootLevel());
		assertTrue(logConfigurations.isFileAppenderEnabled());
		assertTrue(logConfigurations.isConsoleAppenderEnabled());
		assertEquals("INFO", logConfigurations.getLoggers().get("org.n52.wps"));
		assertEquals("DEBUG", logConfigurations.getLoggers().get("org.n52.wps.server.WebProcessingService"));
		assertEquals("OFF", logConfigurations.getLoggers().get("org.apache.axis"));
		assertEquals("ERROR", logConfigurations.getLoggers().get("org.apache.http.headers"));
	}

	@Test
	public void saveLogConfigurations_validLogConfigurations() throws Exception {
		LogConfigurations logConfigurations = new LogConfigurations();
		logConfigurations.setWpsfileAppenderFileNamePattern("testFileAppenderFileNamePattern");
		logConfigurations.setWpsfileAppenderMaxHistory(10);
		logConfigurations.setWpsfileAppenderEncoderPattern("testFileAppenderEncoderPattern");
		logConfigurations.setWpsconsoleEncoderPattern("testWpsconsoleEncoderPattern");
		logConfigurations.setRootLevel("DEBUG");
		logConfigurations.setFileAppenderEnabled(true);
		logConfigurations.setConsoleAppenderEnabled(true);
		SortedMap<String, String> loggers = new TreeMap<String, String>();
		loggers.put("org.n52.wps", "DEBUG");
		loggers.put("org.test.class", "INFO");
		loggers.put("org.n52.wps.server.WebProcessingService", "ERROR");
		logConfigurations.setLoggers(loggers);
		logConfigurationsDAO.saveLogConfigurations(logConfigurations);

		logConfigurations = logConfigurationsDAO.getLogConfigurations();
		assertEquals("testFileAppenderFileNamePattern", logConfigurations.getWpsfileAppenderFileNamePattern());
		assertEquals(10, logConfigurations.getWpsfileAppenderMaxHistory());
		assertEquals("testFileAppenderEncoderPattern", logConfigurations.getWpsfileAppenderEncoderPattern());
		assertEquals("testWpsconsoleEncoderPattern", logConfigurations.getWpsconsoleEncoderPattern());
		assertEquals("DEBUG", logConfigurations.getRootLevel());
		assertTrue(logConfigurations.isFileAppenderEnabled());
		assertTrue(logConfigurations.isConsoleAppenderEnabled());
		assertEquals("DEBUG", logConfigurations.getLoggers().get("org.n52.wps"));
		assertEquals("INFO", logConfigurations.getLoggers().get("org.test.class"));
		assertEquals("ERROR", logConfigurations.getLoggers().get("org.n52.wps.server.WebProcessingService"));
		assertEquals(null, logConfigurations.getLoggers().get("org.apache.http.headers"));
	}

	@Test
	public void saveLogConfigurations_nullLogConfigurations() throws Exception {
		LogConfigurations logConfigurations = null;
		exception.expect(NullPointerException.class);
		logConfigurationsDAO.saveLogConfigurations(logConfigurations);
	}
}
