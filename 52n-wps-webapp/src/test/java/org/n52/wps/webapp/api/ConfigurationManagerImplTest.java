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

import static org.mockito.Mockito.verify;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.n52.wps.webapp.common.AbstractTest;
import org.n52.wps.webapp.service.ConfigurationService;
import org.n52.wps.webapp.testmodules.TestConfigurationModule1;
import org.springframework.beans.factory.annotation.Autowired;

public class ConfigurationManagerImplTest extends AbstractTest {

	@Autowired
	@InjectMocks
	ConfigurationManager configurationManager;

	@Mock
	ConfigurationService configurationService;

	@Rule
	public ExpectedException exception = ExpectedException.none();
		
	private String testModuleClassName = TestConfigurationModule1.class.getName();

	@Before
	public void initModule() throws URISyntaxException {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testGetAllConfigurationModules() {
		configurationManager.getAllConfigurationModules();
		verify(configurationService).getAllConfigurationModules();
	}

	@Test
	public void testGetAllConfigurationModulesByCategory() {
		configurationManager.getAllConfigurationModulesByCategory(ConfigurationCategory.GENERATOR);
		verify(configurationService).getConfigurationModulesByCategory(ConfigurationCategory.GENERATOR);
	}

	@Test
	public void testActiveGetConfigurationModulesByCategory() {
		configurationManager.getActiveConfigurationModulesByCategory(ConfigurationCategory.GENERATOR);
		verify(configurationService).getConfigurationModulesByCategory(ConfigurationCategory.GENERATOR, true);
	}
	
	@Test
	public void getConfigurationModule() {
		configurationManager.getConfigurationModule(testModuleClassName);
		verify(configurationService).getConfigurationModule(testModuleClassName);
	}
	
	@Test
	public void testGetConfigurationEntry() {
		configurationManager.getConfigurationEntry(testModuleClassName, "test.string.key");
		verify(configurationService).getConfigurationEntry(testModuleClassName, "test.string.key");
	}

	@Test
	public void testSetConfigurationEntryValue() throws WPSConfigurationException, URISyntaxException {
		configurationManager.setConfigurationEntryValue(testModuleClassName, "test.string.key", "test value");
		verify(configurationService).setConfigurationEntryValue(testModuleClassName, "test.string.key", "test value");

		configurationManager.setConfigurationEntryValue(testModuleClassName, "test.integer.key", 12);
		verify(configurationService).setConfigurationEntryValue(testModuleClassName, "test.integer.key", 12);

		configurationManager.setConfigurationEntryValue(testModuleClassName, "test.double.key", 14.2);
		verify(configurationService).setConfigurationEntryValue(testModuleClassName, "test.double.key", 14.2);

		configurationManager.setConfigurationEntryValue(testModuleClassName, "test.boolean.key", true);
		verify(configurationService).setConfigurationEntryValue(testModuleClassName, "test.boolean.key", true);

		configurationManager.setConfigurationEntryValue(testModuleClassName, "test.file.key", new File("file_path"));
		verify(configurationService).setConfigurationEntryValue(testModuleClassName, "test.file.key", new File("file_path"));

		configurationManager.setConfigurationEntryValue(testModuleClassName, "test.uri.key", new URI("uri_path"));
		verify(configurationService).setConfigurationEntryValue(testModuleClassName, "test.uri.key", new URI("uri_path"));
	}
	
	@Test
	public void testGetConfigurationEntryValue() throws WPSConfigurationException {
		configurationManager.getConfigurationEntryValue(testModuleClassName, "test.boolean.key", Boolean.class);
		verify(configurationService).getConfigurationEntryValue(testModuleClassName, "test.boolean.key", Boolean.class);
	}
	
	@Test
	public void testGetAlgorithmEntry() {
		configurationManager.getAlgorithmEntry(testModuleClassName, "algorithmName");
		verify(configurationService).getAlgorithmEntry(testModuleClassName, "algorithmName");
	}
	
	@Test
	public void testSetAlgorithmEntry() {
		configurationManager.setAlgorithmEntry(testModuleClassName, "algorithmName", false);
		verify(configurationService).setAlgorithmEntry(testModuleClassName, "algorithmName", false);
	}
}
