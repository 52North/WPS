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

package org.n52.wps.webapp.dao;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
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
import org.n52.wps.webapp.api.WPSConfigurationException;
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
		doCallRealMethod().when(jDomUtil).write(originalTestLogDocument, testLogDocumentPath);
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
		assertEquals("14", logConfigurations.getWpsfileAppenderMaxHistory());
		assertEquals("%d{ISO8601} [%t] %-5p %c: %m%n", logConfigurations.getWpsfileAppenderEncoderPattern());
		assertEquals("%d{ISO8601} [%t] %-5p %c: %m%n", logConfigurations.getWpsconsoleEncoderPattern());
		assertEquals("INFO", logConfigurations.getRootLevel());
		assertEquals("wpsfile", logConfigurations.getRootAppenderRefs().get(0));
		assertEquals("wpsconsole", logConfigurations.getRootAppenderRefs().get(1));
		assertEquals("INFO", logConfigurations.getLoggers().get("org.n52.wps"));
		assertEquals("DEBUG", logConfigurations.getLoggers().get("org.n52.wps.server.WebProcessingService"));
		assertEquals("OFF", logConfigurations.getLoggers().get("org.apache.axis"));
		assertEquals("ERROR", logConfigurations.getLoggers().get("org.apache.http.headers"));
	}

	@Test
	public void saveLogConfigurations_validLogConfigurations() throws Exception {
		LogConfigurations logConfigurations = logConfigurationsDAO.getLogConfigurations();
		logConfigurations.setWpsfileAppenderFileNamePattern("testFileAppenderFileNamePattern");
		logConfigurations.setWpsfileAppenderMaxHistory("10");
		logConfigurations.setWpsfileAppenderEncoderPattern("testFileAppenderEncoderPattern");
		logConfigurations.setWpsconsoleEncoderPattern("testWpsconsoleEncoderPattern");
		logConfigurations.setRootLevel("DEBUG");
		List<String> appendersRef = new ArrayList<String>();
		appendersRef.add("testWpsFile");
		appendersRef.add("testWpsConsole");
		logConfigurations.setRootAppenderRefs(appendersRef);
		SortedMap<String, String> loggers = new TreeMap<String, String>();
		loggers.put("org.n52.wps", "DEBUG");
		loggers.put("org.test.class", "INFO");
		loggers.put("org.n52.wps.server.WebProcessingService", "ERROR");
		logConfigurations.setLoggers(loggers);
		logConfigurationsDAO.saveLogConfigurations(logConfigurations);

		assertEquals("testFileAppenderFileNamePattern", logConfigurations.getWpsfileAppenderFileNamePattern());
		assertEquals("10", logConfigurations.getWpsfileAppenderMaxHistory());
		assertEquals("testFileAppenderEncoderPattern", logConfigurations.getWpsfileAppenderEncoderPattern());
		assertEquals("testWpsconsoleEncoderPattern", logConfigurations.getWpsconsoleEncoderPattern());
		assertEquals("DEBUG", logConfigurations.getRootLevel());
		assertEquals("testWpsFile", logConfigurations.getRootAppenderRefs().get(0));
		assertEquals("testWpsConsole", logConfigurations.getRootAppenderRefs().get(1));
		assertEquals("DEBUG", logConfigurations.getLoggers().get("org.n52.wps"));
		assertEquals("INFO", logConfigurations.getLoggers().get("org.test.class"));
		assertEquals("ERROR", logConfigurations.getLoggers().get("org.n52.wps.server.WebProcessingService"));
		assertEquals(null, logConfigurations.getLoggers().get("org.apache.http.headers"));
	}

	@Test
	public void saveLogConfigurations_nullLogConfigurations() throws Exception {
		LogConfigurations logConfigurations = null;
		exception.expect(WPSConfigurationException.class);
		exception.expectMessage("NullPointerException");
		logConfigurationsDAO.saveLogConfigurations(logConfigurations);
	}
}
