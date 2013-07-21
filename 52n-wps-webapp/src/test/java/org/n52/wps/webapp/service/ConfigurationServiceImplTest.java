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

package org.n52.wps.webapp.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.n52.wps.webapp.api.AlgorithmEntry;
import org.n52.wps.webapp.api.ConfigurationCategory;
import org.n52.wps.webapp.api.ConfigurationModule;
import org.n52.wps.webapp.api.ConfigurationType;
import org.n52.wps.webapp.api.WPSConfigurationException;
import org.n52.wps.webapp.api.types.ConfigurationEntry;
import org.n52.wps.webapp.common.AbstractTest;
import org.n52.wps.webapp.dao.ConfigurationDAO;
import org.n52.wps.webapp.testmodules.TestConfigurationModule1;
import org.n52.wps.webapp.testmodules.TestConfigurationModule2;
import org.n52.wps.webapp.testmodules.TestConfigurationModule3;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ConfigurationServiceImplTest extends AbstractTest {

	@Autowired
	@InjectMocks
	private ConfigurationService configurationService;

	@Mock
	private ListableBeanFactory listableBeanFactory;

	@Mock
	private ConfigurationDAO configurationDAO;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	private String testModule1ClassName = TestConfigurationModule1.class.getName();
	private String testModule2ClassName = TestConfigurationModule2.class.getName();
	private String testModule3ClassName = TestConfigurationModule3.class.getName();

	private TestConfigurationModule1 testModule1 = new TestConfigurationModule1();
	private TestConfigurationModule2 testModule2 = new TestConfigurationModule2();
	private TestConfigurationModule3 testModule3 = new TestConfigurationModule3();

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		when(listableBeanFactory.getBeansOfType(ConfigurationModule.class)).thenReturn(testMap());
	}

	private Map<String, ConfigurationModule> testMap() {
		Map<String, ConfigurationModule> testModules = new HashMap<String, ConfigurationModule>();
		testModules.put("testConfigurationModule1", testModule1);
		testModules.put("testConfigurationModule2", testModule2);
		testModules.put("testConfigurationModule3", testModule3);
		return testModules;
	}

	@Test
	public void testSyncNonExistingConfigurations() {

		String classModuleNames = "org.n52.wps.webapp.testmodules.TestConfigurationModule";
		when(configurationDAO.getConfigurationEntryValue(startsWith(classModuleNames), contains("key"))).thenReturn(
				null);
		when(configurationDAO.getAlgorithmEntry(startsWith(classModuleNames), anyString())).thenReturn(null);
		configurationService.syncConfigurations();

		/*
		 * verify that every entry value and algorithm are checked for existence. Since values are null (don't exist),
		 * verify that they're inserted
		 */
		for (ConfigurationModule module : testMap().values()) {
			for (ConfigurationEntry<?> entry : module.getConfigurationEntries()) {
				verify(configurationDAO).getConfigurationEntryValue(module.getClass().getName(), entry.getKey());

				Object value = null;

				if (entry.getType() == ConfigurationType.FILE || entry.getType() == ConfigurationType.URI) {
					value = entry.getValue().toString();
				} else {
					value = entry.getValue();
				}
				verify(configurationDAO).insertConfigurationEntryValue(module.getClass().getName(), entry.getKey(),
						value);
			}

			for (AlgorithmEntry entry : module.getAlgorithmEntries()) {
				verify(configurationDAO).getAlgorithmEntry(module.getClass().getName(), entry.getAlgorithm());
				verify(configurationDAO).insertAlgorithmEntry(module.getClass().getName(), entry.getAlgorithm(),
						entry.isActive());
			}
		}

	}

	@Test
	public void testSyncExistingConfigurations() throws URISyntaxException {

		String classModuleNames = "org.n52.wps.webapp.testmodules.TestConfigurationModule";
		when(configurationDAO.getConfigurationEntryValue(startsWith(classModuleNames), eq("test.string.key")))
				.thenReturn("synced string");
		when(configurationDAO.getConfigurationEntryValue(startsWith(classModuleNames), eq("test.integer.key")))
				.thenReturn(99);
		when(configurationDAO.getConfigurationEntryValue(startsWith(classModuleNames), eq("test.double.key")))
				.thenReturn(1.2);
		when(configurationDAO.getConfigurationEntryValue(startsWith(classModuleNames), eq("test.boolean.key")))
				.thenReturn(false);
		when(configurationDAO.getConfigurationEntryValue(startsWith(classModuleNames), eq("test.file.key")))
				.thenReturn(new File("synced_path"));
		when(configurationDAO.getConfigurationEntryValue(startsWith(classModuleNames), eq("test.uri.key"))).thenReturn(
				new URI("synced_path"));
		when(configurationDAO.getConfigurationEntryValue(startsWith(classModuleNames), eq("test.string.key2")))
				.thenReturn("synced string");
		when(configurationDAO.getConfigurationEntryValue(startsWith(classModuleNames), eq("test.integer.key2")))
				.thenReturn(99);

		when(configurationDAO.getAlgorithmEntry(startsWith(classModuleNames), eq("name1"))).thenReturn(
				new AlgorithmEntry("name1", true));

		when(configurationDAO.getAlgorithmEntry(startsWith(classModuleNames), eq("name2"))).thenReturn(
				new AlgorithmEntry("name2", false));

		configurationService.syncConfigurations();

		/*
		 * verify that every entry value and algorithm are checked for existence. Since values do exist, read them to
		 * configuration modules
		 */
		for (ConfigurationModule module : testMap().values()) {
			for (ConfigurationEntry<?> entry : module.getConfigurationEntries()) {
				verify(configurationDAO).getConfigurationEntryValue(module.getClass().getName(), entry.getKey());

				verify(configurationDAO, never()).insertConfigurationEntryValue(eq(module.getClass().getName()),
						eq(entry.getKey()), any());

				Object value = entry.getValue();
				if (entry.getKey().equals("test.string.key")) {
					assertEquals("synced string", value);
				}
				if (entry.getKey().equals("test.integer.key")) {
					assertEquals(99, value);
				}
				if (entry.getKey().equals("test.double.key")) {
					assertEquals(1.2, value);
				}
				if (entry.getKey().equals("test.boolean.key")) {
					assertEquals(false, value);
				}
				if (entry.getKey().equals("test.file.key")) {
					assertEquals(new File("synced_path"), value);
				}
				if (entry.getKey().equals("test.uri.key")) {
					assertEquals(new URI("synced_path"), value);
				}
			}

			for (AlgorithmEntry entry : module.getAlgorithmEntries()) {
				verify(configurationDAO).getAlgorithmEntry(module.getClass().getName(), entry.getAlgorithm());
				verify(configurationDAO, never()).insertAlgorithmEntry(module.getClass().getName(),
						entry.getAlgorithm(), entry.isActive());
				if (entry.getAlgorithm().equals("name1")) {
					assertTrue(entry.isActive());
				}
				if (entry.getAlgorithm().equals("name2")) {
					assertFalse(entry.isActive());
				}
			}
		}

	}

	@Test
	public void testGetAllConfigurationModules() {
		assertNotNull(configurationService.getAllConfigurationModules());
		Assert.assertEquals("Test Module Name 1",
				configurationService.getAllConfigurationModules().get(testModule1ClassName).getModuleName());
		Assert.assertEquals("Test Module Name 2",
				configurationService.getAllConfigurationModules().get(testModule2ClassName).getModuleName());
		Assert.assertEquals("Test Module Name 3",
				configurationService.getAllConfigurationModules().get(testModule3ClassName).getModuleName());
		assertNull(configurationService.getAllConfigurationModules().get("non.existing.module"));
	}

	@Test
	public void testGetConfigurationModulesByCategory() {
		Map<String, ConfigurationModule> processModules = configurationService
				.getConfigurationModulesByCategory(ConfigurationCategory.PROCESS);
		assertNotNull(processModules);
		for (ConfigurationModule module : processModules.values()) {
			assertTrue(module.getCategory() == ConfigurationCategory.PROCESS);
		}

		// No ConfigurationCategory.GENERAL test module in the testmodules package
		Map<String, ConfigurationModule> noModules = configurationService
				.getConfigurationModulesByCategory(ConfigurationCategory.GENERAL);
		assertEquals(0, noModules.size());
	}

	@Test
	public void testGetActiveConfigurationModulesByCategory() {
		Map<String, ConfigurationModule> allProcessModules = configurationService.getConfigurationModulesByCategory(
				ConfigurationCategory.PROCESS, false);
		assertTrue(allProcessModules.size() > 1);

		Map<String, ConfigurationModule> onlyActiveProcessModules = configurationService
				.getConfigurationModulesByCategory(ConfigurationCategory.PROCESS, true);
		assertEquals(1, onlyActiveProcessModules.size());

		for (ConfigurationModule module : onlyActiveProcessModules.values()) {
			assertTrue(module.getCategory() == ConfigurationCategory.PROCESS);
			assertTrue(module.isActive());
		}
	}

	@Test
	public void testGetConfigurationModule() {
		ConfigurationModule module = configurationService.getConfigurationModule(testModule1ClassName);
		assertEquals("Test Module Name 1", module.getModuleName());

		ConfigurationModule nullModule = configurationService.getConfigurationModule("non.existing.module");
		assertNull(nullModule);
	}

	@Test
	public void testGetConfigurationEntry() {
		ConfigurationEntry<?> entry = configurationService.getConfigurationEntry(testModule1ClassName,
				"test.integer.key");
		assertEquals(entry.getType(), ConfigurationType.INTEGER);
		assertEquals(entry.getKey(), "test.integer.key");

		assertNull(configurationService.getConfigurationEntry(testModule1ClassName, "non.existing.entry"));
	}

	@Test
	public void testGetValidConfigurationEntryValue() throws WPSConfigurationException, URISyntaxException {

		String stringValue = configurationService.getConfigurationEntryValue(testModule2ClassName, "test.string.key",
				String.class);
		assertEquals("Initial Value", stringValue);

		int intValue = configurationService.getConfigurationEntryValue(testModule2ClassName, "test.integer.key",
				Integer.class);
		assertEquals(44, intValue);

		double doubleValue = configurationService.getConfigurationEntryValue(testModule2ClassName, "test.double.key",
				Double.class);
		assertEquals(10.4, doubleValue, 0);

		boolean boolValue = configurationService.getConfigurationEntryValue(testModule2ClassName, "test.boolean.key",
				Boolean.class);
		assertEquals(true, boolValue);

		File file = configurationService.getConfigurationEntryValue(testModule2ClassName, "test.file.key", File.class);
		assertEquals(new File("path"), file);

		URI uri = configurationService.getConfigurationEntryValue(testModule2ClassName, "test.uri.key", URI.class);
		assertEquals(new URI("path"), uri);
	}

	@Test
	public void testGetInvalidStringValue() throws WPSConfigurationException {
		exception.expect(WPSConfigurationException.class);
		configurationService.getConfigurationEntryValue(testModule1ClassName, "test.string.key", Integer.class);
	}

	@Test
	public void testGetInvalidIntegerValue() throws WPSConfigurationException {
		exception.expect(WPSConfigurationException.class);
		configurationService.getConfigurationEntryValue(testModule1ClassName, "test.integer.key", Boolean.class);
	}

	@Test
	public void testGetInvalidDoubleValue() throws WPSConfigurationException {
		exception.expect(WPSConfigurationException.class);
		configurationService.getConfigurationEntryValue(testModule1ClassName, "test.double.key", Boolean.class);
	}

	@Test
	public void testGetInvalidBooleanValue() throws WPSConfigurationException {
		exception.expect(WPSConfigurationException.class);
		configurationService.getConfigurationEntryValue(testModule1ClassName, "test.boolean.key", Double.class);
	}

	@Test
	public void testGetInvalidFileValue() throws WPSConfigurationException {
		exception.expect(WPSConfigurationException.class);
		configurationService.getConfigurationEntryValue(testModule1ClassName, "test.file.key", Integer.class);
	}

	@Test
	public void testGetInvalidURIValue() throws WPSConfigurationException, URISyntaxException {
		exception.expect(WPSConfigurationException.class);
		configurationService.getConfigurationEntryValue(testModule1ClassName, "test.uri.key", Integer.class);
	}

	@Test
	public void testSetConfigurationEntryValue() throws WPSConfigurationException, URISyntaxException {
		configurationService.setConfigurationEntryValue(testModule1ClassName, "test.string.key", "test value");
		verify(configurationDAO).updateConfigurationEntryValue(testModule1ClassName, "test.string.key", "test value");
		assertEquals("test value", testModule1.getConfigurationEntries().get(0).getValue());

		configurationService.setConfigurationEntryValue(testModule1ClassName, "test.integer.key", 12);
		verify(configurationDAO).updateConfigurationEntryValue(testModule1ClassName, "test.integer.key", 12);
		assertEquals(12, testModule1.getConfigurationEntries().get(1).getValue());

		configurationService.setConfigurationEntryValue(testModule1ClassName, "test.double.key", 14.2);
		verify(configurationDAO).updateConfigurationEntryValue(testModule1ClassName, "test.double.key", 14.2);
		assertEquals(14.2, testModule1.getConfigurationEntries().get(2).getValue());

		configurationService.setConfigurationEntryValue(testModule1ClassName, "test.boolean.key", true);
		verify(configurationDAO).updateConfigurationEntryValue(testModule1ClassName, "test.boolean.key", true);
		assertEquals(true, testModule1.getConfigurationEntries().get(3).getValue());

		configurationService.setConfigurationEntryValue(testModule1ClassName, "test.file.key", new File("file_path"));
		verify(configurationDAO).updateConfigurationEntryValue(testModule1ClassName, "test.file.key", new File("file_path"));
		assertEquals(new File("file_path"), testModule1.getConfigurationEntries().get(4).getValue());

		configurationService.setConfigurationEntryValue(testModule1ClassName, "test.uri.key", new URI("uri_path"));
		verify(configurationDAO).updateConfigurationEntryValue(testModule1ClassName, "test.uri.key", new URI("uri_path"));
		assertEquals(new URI("uri_path"), testModule1.getConfigurationEntries().get(5).getValue());
		
		configurationService.setConfigurationEntryValue(testModule1ClassName, "non.existing.entry", 12);
		verify(configurationDAO, never()).updateConfigurationEntryValue(testModule1ClassName, "non.existing.entry", 12);
	}

	@Test
	public void testSetInvalidStringalue() throws WPSConfigurationException {
		exception.expect(WPSConfigurationException.class);
		configurationService.setConfigurationEntryValue(testModule1ClassName, "test.string.key", "");
	}

	@Test
	public void testSetInvalidIntegerValue() throws WPSConfigurationException {
		exception.expect(WPSConfigurationException.class);
		configurationService.setConfigurationEntryValue(testModule1ClassName, "test.integer.key", "invalid_integer");
	}

	@Test
	public void testSetInvalidDoubleValue() throws WPSConfigurationException {
		exception.expect(WPSConfigurationException.class);
		configurationService.setConfigurationEntryValue(testModule1ClassName, "test.double.key", "invalid_double");
	}

	@Test
	public void testSetInvalidBooleanValue() throws WPSConfigurationException {
		exception.expect(WPSConfigurationException.class);
		configurationService.setConfigurationEntryValue(testModule1ClassName, "test.boolean.key", "invalid_boolean");
	}

	@Test
	public void testSetInvalidFileValue() throws WPSConfigurationException {
		exception.expect(WPSConfigurationException.class);
		configurationService.setConfigurationEntryValue(testModule1ClassName, "test.file.key", "");
	}

	@Test
	public void testSetInvalidURIValue() throws WPSConfigurationException {
		exception.expect(WPSConfigurationException.class);
		configurationService.setConfigurationEntryValue(testModule1ClassName, "test.uri.key", "");
	}

	@Test
	public void testGetAlgorithmEntry() {
		AlgorithmEntry entry = configurationService.getAlgorithmEntry(testModule1ClassName, "name2");
		assertEquals("name2", entry.getAlgorithm());
		assertTrue(entry.isActive());

		assertNull(configurationService.getAlgorithmEntry(testModule1ClassName, "non.existing.algorithm"));
	}

	@Test
	public void testSetAlgorithmEntry() {
		String algorithm = "name1";
		configurationService.setAlgorithmEntry(testModule1ClassName, algorithm, false);
		assertEquals("name1", testModule1.getAlgorithmEntries().get(0).getAlgorithm());
		assertEquals(false, testModule1.getAlgorithmEntries().get(0).isActive());
		verify(configurationDAO).updateAlgorithmEntry(testModule1ClassName, algorithm, false);

		String nonExisting = "non.existing.algorithm";
		verify(configurationDAO, never()).updateAlgorithmEntry(testModule1ClassName, nonExisting, false);
	}

	@Test
	public void testPassValidValueToSetterMethods() throws WPSConfigurationException, URISyntaxException {
		configurationService.passValueToConfigurationModule(testModule2ClassName, "test.string.key");
		assertEquals("Initial Value", testModule2.getStringMember());

		configurationService.passValueToConfigurationModule(testModule2ClassName, "test.integer.key");
		assertEquals(44, testModule2.getIntMember());

		configurationService.passValueToConfigurationModule(testModule2ClassName, "test.double.key");
		assertEquals(10.4, testModule2.getDoubleMember(), 0);

		configurationService.passValueToConfigurationModule(testModule2ClassName, "test.boolean.key");
		assertEquals(true, testModule2.isBooleanMember());

		configurationService.passValueToConfigurationModule(testModule2ClassName, "test.file.key");
		assertEquals(new File("path"), testModule2.getFileMember());

		configurationService.passValueToConfigurationModule(testModule2ClassName, "test.uri.key");
		assertEquals(new URI("path"), testModule2.getUriMember());
	}

	@Test
	public void testPassInvalidValueToSetterMethod() throws WPSConfigurationException {
		exception.expect(WPSConfigurationException.class);
		configurationService.passValueToConfigurationModule(testModule1ClassName, "test.string.key2");
	}

	@Test
	public void testPassValueToSetterMethodWithMultipleParameters() throws WPSConfigurationException {
		exception.expect(WPSConfigurationException.class);
		configurationService.passValueToConfigurationModule(testModule1ClassName, "test.integer.key2");
	}
}
