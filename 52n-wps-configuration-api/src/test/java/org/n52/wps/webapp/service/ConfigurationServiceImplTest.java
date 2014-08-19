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
package org.n52.wps.webapp.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mvel2.ast.AssertNode;
import org.n52.wps.webapp.api.AlgorithmEntry;
import org.n52.wps.webapp.api.ConfigurationCategory;
import org.n52.wps.webapp.api.ConfigurationModule;
import org.n52.wps.webapp.api.ConfigurationType;
import org.n52.wps.webapp.api.ValueParser;
import org.n52.wps.webapp.api.WPSConfigurationException;
import org.n52.wps.webapp.api.types.ConfigurationEntry;
import org.n52.wps.webapp.dao.ConfigurationDAO;
import org.n52.wps.webapp.testmodules.TestConfigurationModule1;
import org.n52.wps.webapp.testmodules.TestConfigurationModule2;
import org.n52.wps.webapp.testmodules.TestConfigurationModule3;
import org.n52.wps.webapp.testmodules.TestConfigurationModuleParser1;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.test.util.ReflectionTestUtils;

public class ConfigurationServiceImplTest {

	@InjectMocks
	private ConfigurationService configurationService;

	@Mock
	private ListableBeanFactory listableBeanFactory;

	@Mock
	private ConfigurationDAO configurationDAO;

	@Mock
	private ValueParser valueParser;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	private String testModule1ClassName = TestConfigurationModule1.class.getName();
	private String testModule2ClassName = TestConfigurationModule2.class.getName();
	private String testModule3ClassName = TestConfigurationModule3.class.getName();
	private String testParserModuleClassName = TestConfigurationModuleParser1.class.getName();

	private TestConfigurationModule1 testModule1;
	private TestConfigurationModule2 testModule2;
	private TestConfigurationModule3 testModule3;
	private TestConfigurationModuleParser1 testParserConfigurationModule1;

	private String stringKey = "test.string.key";
	private String integerKey = "test.integer.key";
	private String doubleKey = "test.double.key";
	private String booleanKey = "test.boolean.key";
	private String fileKey = "test.file.key";
	private String uriKey = "test.uri.key";
	private String invalidStringKey = "test.string.key2";
	private String invalidIntegerKey = "test.integer.key2";

	private String testStringValue = "test value";
	private int testIntValue = 12;
	private double testDoubleValue = 14.2;
	private boolean testBooleanValue = true;
	private File testFileValue = new File("file_path");
	private URI testUriValue = URI.create("uri_path");

	@Before
	public void setup() throws WPSConfigurationException, URISyntaxException {
		configurationService = new ConfigurationServiceImpl();
		testModule1 = new TestConfigurationModule1();
		testModule2 = new TestConfigurationModule2();
		testModule3 = new TestConfigurationModule3();
		testParserConfigurationModule1 = new TestConfigurationModuleParser1();
		MockitoAnnotations.initMocks(this);
		when(listableBeanFactory.getBeansOfType(ConfigurationModule.class)).thenReturn(getTestMap());
		ReflectionTestUtils.invokeMethod(configurationService, "buildConfigurationModulesMap");
	}

	@After
	public void tearDown() {
		testModule1 = null;
		testModule2 = null;
		testModule3 = null;
		testParserConfigurationModule1 = null;
		configurationService = null;
	}

	private Map<String, ConfigurationModule> getTestMap() {
		Map<String, ConfigurationModule> testModules = new HashMap<String, ConfigurationModule>();
		testModules.put("testConfigurationModule1", testModule1);
		testModules.put("testConfigurationModule2", testModule2);
		testModules.put("testConfigurationModule3", testModule3);
		testModules.put("testParserConfigurationModule1", testParserConfigurationModule1);
		return testModules;
	}

	@Test
	public void syncConfigurations_newConfigurations() {
		String classModuleNames = "org.n52.wps.webapp.testmodules.TestConfigurationModule";
		when(configurationDAO.getConfigurationModuleStatus(any(ConfigurationModule.class))).thenReturn(null);
		when(configurationDAO.getConfigurationEntryValue(startsWith(classModuleNames), anyString())).thenReturn(null);
		when(configurationDAO.getAlgorithmEntry(startsWith(classModuleNames), anyString())).thenReturn(null);

		// syncConfigurations is a private @PostConstruct, invoking via reflection
		ReflectionTestUtils.invokeMethod(configurationService, "syncConfigurations");

		/*
		 * verify that every entry value and algorithm are checked for existence. Since values are null (don't exist),
		 * verify that they're inserted
		 */
		for (ConfigurationModule module : getTestMap().values()) {
			verify(configurationDAO).getConfigurationModuleStatus(module);
			verify(configurationDAO).insertConfigurationModule(module);

			for (ConfigurationEntry<?> entry : module.getConfigurationEntries()) {
				verify(configurationDAO).getConfigurationEntryValue(module.getClass().getName(), entry.getKey());

				Object value = null;
				if ((value = entry.getValue()) != null) {
					if (entry.getType() == ConfigurationType.FILE || entry.getType() == ConfigurationType.URI) {
						value = entry.getValue().toString();
					}
					verify(configurationDAO).insertConfigurationEntryValue(module.getClass().getName(), entry.getKey(),
							value);
				}
			}

			for (AlgorithmEntry entry : module.getAlgorithmEntries()) {
				verify(configurationDAO).getAlgorithmEntry(module.getClass().getName(), entry.getAlgorithm());
				verify(configurationDAO).insertAlgorithmEntry(module.getClass().getName(), entry.getAlgorithm(),
						entry.isActive());
			}
		}

	}

	@Test
	public void syncConfigurations_existingConfigurations() throws Exception {

		String classModuleNames = "org.n52.wps.webapp.testmodules.TestConfigurationModule";
		when(configurationDAO.getConfigurationModuleStatus(any(ConfigurationModule.class))).thenReturn(true);
		when(configurationDAO.getConfigurationEntryValue(startsWith(classModuleNames), eq(stringKey))).thenReturn(
				"synced string");
		when(configurationDAO.getConfigurationEntryValue(startsWith(classModuleNames), eq(integerKey))).thenReturn(99);
		when(configurationDAO.getConfigurationEntryValue(startsWith(classModuleNames), eq(doubleKey))).thenReturn(1.2);
		when(configurationDAO.getConfigurationEntryValue(startsWith(classModuleNames), eq(booleanKey))).thenReturn(
				false);
		when(configurationDAO.getConfigurationEntryValue(startsWith(classModuleNames), eq(fileKey))).thenReturn(
				new File("synced_path"));
		when(configurationDAO.getConfigurationEntryValue(startsWith(classModuleNames), eq(uriKey))).thenReturn(
				new URI("synced_path"));
		when(configurationDAO.getConfigurationEntryValue(startsWith(classModuleNames), eq(invalidStringKey)))
				.thenReturn("synced string");
		when(configurationDAO.getConfigurationEntryValue(startsWith(classModuleNames), eq(invalidIntegerKey)))
				.thenReturn(99);

		when(configurationDAO.getAlgorithmEntry(startsWith(classModuleNames), eq("name1"))).thenReturn(
				new AlgorithmEntry("name1", true));

		when(configurationDAO.getAlgorithmEntry(startsWith(classModuleNames), eq("name2"))).thenReturn(
				new AlgorithmEntry("name2", false));

		when(valueParser.parseString("synced string")).thenReturn("synced string");
		when(valueParser.parseInteger(99)).thenReturn(99);
		when(valueParser.parseDouble(1.2)).thenReturn(1.2);
		when(valueParser.parseBoolean(false)).thenReturn(false);
		when(valueParser.parseFile(new File("synced_path"))).thenReturn(new File("synced_path"));
		when(valueParser.parseURI(new URI("synced_path"))).thenReturn(new URI("synced_path"));

		// syncConfigurations is a private @PostConstruct, invoking via reflection
		ReflectionTestUtils.invokeMethod(configurationService, "syncConfigurations");

		/*
		 * verify that every entry value and algorithm are checked for existence. Since values do exist, read them to
		 * configuration modules
		 */
		for (ConfigurationModule module : getTestMap().values()) {
			verify(configurationDAO).getConfigurationModuleStatus(module);
			verify(configurationDAO, never()).insertConfigurationModule(module);

			for (ConfigurationEntry<?> entry : module.getConfigurationEntries()) {
				verify(configurationDAO).getConfigurationEntryValue(module.getClass().getName(), entry.getKey());
				verify(configurationDAO, never()).insertConfigurationEntryValue(eq(module.getClass().getName()),
						eq(entry.getKey()), any());
				verify(configurationDAO, never()).updateConfigurationEntryValue(eq(module.getClass().getName()),
						eq(entry.getKey()), any());

				Object value = entry.getValue();
				if (entry.getKey().equals(stringKey)) {
					assertEquals("synced string", value);
				}
				if (entry.getKey().equals(integerKey)) {
					assertEquals(99, value);
				}
				if (entry.getKey().equals(doubleKey)) {
					assertEquals(1.2, value);
				}
				if (entry.getKey().equals(booleanKey)) {
					assertEquals(false, value);
				}
				if (entry.getKey().equals(fileKey)) {
					assertEquals(new File("synced_path"), value);
				}
				if (entry.getKey().equals(uriKey)) {
					assertEquals(new URI("synced_path"), value);
				}
			}

			for (AlgorithmEntry entry : module.getAlgorithmEntries()) {
				verify(configurationDAO).getAlgorithmEntry(module.getClass().getName(), entry.getAlgorithm());
				verify(configurationDAO, never()).insertAlgorithmEntry(module.getClass().getName(),
						entry.getAlgorithm(), entry.isActive());
				verify(configurationDAO, never()).updateAlgorithmEntry(module.getClass().getName(),
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
	public void getAllConfigurationModules_existingModules() {
		assertNotNull(configurationService.getAllConfigurationModules());
		Assert.assertEquals("Test Module Name 1",
				configurationService.getAllConfigurationModules().get(testModule1ClassName).getModuleName());
		Assert.assertEquals("Test Module Name 2",
				configurationService.getAllConfigurationModules().get(testModule2ClassName).getModuleName());
		Assert.assertEquals("Test Module Name 3",
				configurationService.getAllConfigurationModules().get(testModule3ClassName).getModuleName());
	}

	@Test
	public void getAllConfigurationModules_nonExistingModules() {
		assertNull(configurationService.getAllConfigurationModules().get("non.existing.module"));
	}

	@Test
	public void getConfigurationModulesByCategory_existingModules() {
		Map<String, ConfigurationModule> processModules = configurationService
				.getConfigurationModulesByCategory(ConfigurationCategory.REPOSITORY);
		assertNotNull(processModules);
		for (ConfigurationModule module : processModules.values()) {
			assertTrue(module.getCategory() == ConfigurationCategory.REPOSITORY);
		}
	}

	@Test
	public void getConfigurationModulesByCategory_nonExistingModules() {
		// No ConfigurationCategory.GENERAL test module in the testmodules package
		Map<String, ConfigurationModule> noModules = configurationService
				.getConfigurationModulesByCategory(ConfigurationCategory.GENERAL);
		assertEquals(0, noModules.size());
	}

	@Test
	public void getActiveConfigurationModulesByCategory() {
		Map<String, ConfigurationModule> onlyActiveRepositoryModules = configurationService
				.getActiveConfigurationModulesByCategory(ConfigurationCategory.REPOSITORY);

		for (ConfigurationModule module : onlyActiveRepositoryModules.values()) {
			assertTrue(module.getCategory() == ConfigurationCategory.REPOSITORY);
			assertTrue(module.isActive());
		}
	}

	@Test
	public void updateConfigurationModuleStatus() {
		assertTrue(testModule1.isActive());
		configurationService.updateConfigurationModuleStatus(testModule1ClassName, false);
		assertFalse(testModule1.isActive());
		verify(configurationDAO).updateConfigurationModuleStatus(testModule1);
	}

	@Test
	public void getConfigurationModule_existingModule() {
		ConfigurationModule module = configurationService.getConfigurationModule(testModule1ClassName);
		assertEquals("Test Module Name 1", module.getModuleName());
	}

	@Test
	public void getConfigurationModule_nonExistingModule() {
		ConfigurationModule nullModule = configurationService.getConfigurationModule("non.existing.module");
		assertNull(nullModule);
	}

	@Test
	public void getConfigurationEntry_existingEntry() {
		ConfigurationEntry<?> entry = configurationService.getConfigurationEntry(testModule1, integerKey);
		assertEquals(ConfigurationType.INTEGER, entry.getType());
		assertEquals(integerKey, entry.getKey());
	}

	@Test
	public void getConfigurationEntry_nonExistingEntry() {
		assertNull(configurationService.getConfigurationEntry(testModule1, "non.existing.entry"));
	}

	@Test
	public void getConfigurationEntryValue_validStringValue() throws Exception {
		String stringValue = configurationService.getConfigurationEntryValue(testModule2, testModule2
				.getConfigurationEntries().get(0), String.class);
		assertEquals("Initial Value", stringValue);
	}

	@Test
	public void getConfigurationEntryValue_validIntegerValue() throws Exception {
		int intValue = configurationService.getConfigurationEntryValue(testModule2, testModule2
				.getConfigurationEntries().get(1), Integer.class);
		assertEquals(44, intValue);
	}

	@Test
	public void getConfigurationEntryValue_validDoubleValue() throws Exception {
		double doubleValue = configurationService.getConfigurationEntryValue(testModule2, testModule2
				.getConfigurationEntries().get(2), Double.class);
		assertEquals(10.4, doubleValue, 0);
	}

	@Test
	public void getConfigurationEntryValue_validBooleanValue() throws Exception {
		boolean boolValue = configurationService.getConfigurationEntryValue(testModule2, testModule2
				.getConfigurationEntries().get(3), Boolean.class);
		assertEquals(true, boolValue);
	}

	@Test
	public void getConfigurationEntryValue_validFileValue() throws Exception {
		File file = configurationService.getConfigurationEntryValue(testModule2, testModule2.getConfigurationEntries()
				.get(4), File.class);
		assertEquals(new File("path"), file);
	}

	@Test
	public void getConfigurationEntryValue_validURIValue() throws Exception {
		URI uri = configurationService.getConfigurationEntryValue(testModule2, testModule2.getConfigurationEntries()
				.get(5), URI.class);
		assertEquals(new URI("path"), uri);
	}

	@Test
	public void getConfigurationEntryValue_invalidStringValue() throws Exception {
		exception.expect(WPSConfigurationException.class);
		configurationService.getConfigurationEntryValue(testModule1, testModule1.getConfigurationEntries().get(0),
				Integer.class);
	}

	@Test
	public void getConfigurationEntryValue_invalidIntegerValue() throws Exception {
		exception.expect(WPSConfigurationException.class);
		configurationService.getConfigurationEntryValue(testModule1, testModule1.getConfigurationEntries().get(1),
				Boolean.class);
	}

	@Test
	public void getConfigurationEntryValue_invalidDoubleValue() throws Exception {
		exception.expect(WPSConfigurationException.class);
		configurationService.getConfigurationEntryValue(testModule1, testModule1.getConfigurationEntries().get(2),
				Boolean.class);
	}

	@Test
	public void getConfigurationEntryValue_invalidBooleanValue() throws Exception {
		exception.expect(WPSConfigurationException.class);
		configurationService.getConfigurationEntryValue(testModule1, testModule1.getConfigurationEntries().get(3),
				Double.class);
	}

	@Test
	public void getConfigurationEntryValue_invalidFileValue() throws Exception {
		exception.expect(WPSConfigurationException.class);
		configurationService.getConfigurationEntryValue(testModule1, testModule1.getConfigurationEntries().get(4),
				Integer.class);
	}

	@Test
	public void getConfigurationEntryValue_invalidUriValue() throws Exception {
		exception.expect(WPSConfigurationException.class);
		configurationService.getConfigurationEntryValue(testModule1, testModule1.getConfigurationEntries().get(5),
				Integer.class);
	}

	@Test
	public void setConfigurationModuleValues_validValues_newModule() throws Exception {
		when(valueParser.parseString(testStringValue)).thenReturn(testStringValue);
		when(valueParser.parseInteger(testIntValue)).thenReturn(testIntValue);
		when(valueParser.parseDouble(testDoubleValue)).thenReturn(testDoubleValue);
		when(valueParser.parseBoolean(testBooleanValue)).thenReturn(testBooleanValue);
		when(valueParser.parseFile(testFileValue)).thenReturn(testFileValue);
		when(valueParser.parseURI(testUriValue)).thenReturn(testUriValue);

		String[] keys = { stringKey, integerKey, doubleKey, booleanKey, fileKey, uriKey };
		Object[] values = { testStringValue, testIntValue, testDoubleValue, testBooleanValue, testFileValue,
				testUriValue };
		configurationService.setConfigurationModuleValues(testModule1ClassName, keys, values);

		verify(configurationDAO).insertConfigurationEntryValue(testModule1ClassName, stringKey, testStringValue);
		assertEquals(testStringValue, testModule1.getConfigurationEntries().get(0).getValue());

		verify(configurationDAO).insertConfigurationEntryValue(testModule1ClassName, integerKey, testIntValue);
		assertEquals(testIntValue, testModule1.getConfigurationEntries().get(1).getValue());

		verify(configurationDAO).insertConfigurationEntryValue(testModule1ClassName, doubleKey, testDoubleValue);
		assertEquals(testDoubleValue, testModule1.getConfigurationEntries().get(2).getValue());

		verify(configurationDAO).insertConfigurationEntryValue(testModule1ClassName, booleanKey, testBooleanValue);
		assertEquals(testBooleanValue, testModule1.getConfigurationEntries().get(3).getValue());

		verify(configurationDAO).insertConfigurationEntryValue(testModule1ClassName, fileKey, testFileValue.toString());
		assertEquals(testFileValue, testModule1.getConfigurationEntries().get(4).getValue());

		verify(configurationDAO).insertConfigurationEntryValue(testModule1ClassName, uriKey, testUriValue.toString());
		assertEquals(testUriValue, testModule1.getConfigurationEntries().get(5).getValue());
	}

	@Test
	public void setConfigurationModuleValues_validValues_existingModule() throws Exception {
		when(valueParser.parseString(testStringValue)).thenReturn(testStringValue);
		when(valueParser.parseInteger(testIntValue)).thenReturn(testIntValue);
		when(valueParser.parseDouble(testDoubleValue)).thenReturn(testDoubleValue);
		when(valueParser.parseBoolean(testBooleanValue)).thenReturn(testBooleanValue);
		when(valueParser.parseFile(testFileValue)).thenReturn(testFileValue);
		when(valueParser.parseURI(testUriValue)).thenReturn(testUriValue);
		when(configurationDAO.getConfigurationEntryValue(eq(testModule1ClassName), anyString())).thenReturn(
				new Object());

		String[] keys = { stringKey, integerKey, doubleKey, booleanKey, fileKey, uriKey };
		Object[] values = { testStringValue, testIntValue, testDoubleValue, testBooleanValue, testFileValue,
				testUriValue };
		configurationService.setConfigurationModuleValues(testModule1ClassName, keys, values);

		verify(configurationDAO).updateConfigurationEntryValue(testModule1ClassName, stringKey, testStringValue);
		assertEquals(testStringValue, testModule1.getConfigurationEntries().get(0).getValue());

		verify(configurationDAO).updateConfigurationEntryValue(testModule1ClassName, integerKey, testIntValue);
		assertEquals(testIntValue, testModule1.getConfigurationEntries().get(1).getValue());

		verify(configurationDAO).updateConfigurationEntryValue(testModule1ClassName, doubleKey, testDoubleValue);
		assertEquals(testDoubleValue, testModule1.getConfigurationEntries().get(2).getValue());

		verify(configurationDAO).updateConfigurationEntryValue(testModule1ClassName, booleanKey, testBooleanValue);
		assertEquals(testBooleanValue, testModule1.getConfigurationEntries().get(3).getValue());

		verify(configurationDAO).updateConfigurationEntryValue(testModule1ClassName, fileKey, testFileValue.toString());
		assertEquals(testFileValue, testModule1.getConfigurationEntries().get(4).getValue());

		verify(configurationDAO).updateConfigurationEntryValue(testModule1ClassName, uriKey, testUriValue.toString());
		assertEquals(testUriValue, testModule1.getConfigurationEntries().get(5).getValue());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void setConfigurationModuleValues_invalidValues() throws Exception {
		when(valueParser.parseString(testStringValue)).thenReturn(testStringValue);
		when(valueParser.parseInteger("twelve")).thenThrow(WPSConfigurationException.class);
		when(valueParser.parseDouble(testDoubleValue)).thenReturn(testDoubleValue);
		when(valueParser.parseBoolean(testBooleanValue)).thenReturn(testBooleanValue);
		when(valueParser.parseFile(testFileValue)).thenReturn(testFileValue);
		when(valueParser.parseURI(testUriValue)).thenReturn(testUriValue);

		String[] keys = { stringKey, integerKey, doubleKey, booleanKey, fileKey, uriKey };
		Object[] values = { testStringValue, "twelve", testDoubleValue, testBooleanValue, testFileValue, testUriValue };

		exception.expect(WPSConfigurationException.class);
		configurationService.setConfigurationModuleValues(testModule1ClassName, keys, values);
		verifyZeroInteractions(configurationDAO);
	}

	@Test
	public void passConfigurationModuleValuesToMembers() throws Exception {
		when(valueParser.parseString(testStringValue)).thenReturn(testStringValue);
		when(valueParser.parseInteger(testIntValue)).thenReturn(testIntValue);
		when(valueParser.parseDouble(testDoubleValue)).thenReturn(testDoubleValue);
		when(valueParser.parseBoolean(testBooleanValue)).thenReturn(testBooleanValue);
		when(valueParser.parseFile(testFileValue)).thenReturn(testFileValue);
		when(valueParser.parseURI(testUriValue)).thenReturn(testUriValue);

		String[] keys = { stringKey, integerKey, doubleKey, booleanKey, fileKey, uriKey, invalidStringKey,
				invalidIntegerKey };
		Object[] values = { testStringValue, testIntValue, testDoubleValue, testBooleanValue, testFileValue,
				testUriValue, testStringValue, testIntValue };
		configurationService.setConfigurationModuleValues(testModule1ClassName, keys, values);

		assertEquals(testStringValue, testModule1.getStringMember());
		assertEquals(testIntValue, testModule1.getIntMember());
		assertEquals(testDoubleValue, testModule1.getDoubleMember(), 0);
		assertEquals(testBooleanValue, testModule1.isBooleanMember());
		assertEquals(testFileValue, testModule1.getFileMember());
		assertEquals(testUriValue, testModule1.getUriMember());

		// the module is trying to pass string to integer module
		assertNotEquals(testStringValue, testModule1.getIntInvalidMember());

		// the module is trying to pass integer to a method with the wrong signature (more than 1 parameter)
		assertNotEquals(testIntValue, testModule1.getIntInvalidMember());
	}

	@Test
	public void getAlgorithmEntry_existingEntry() {
		AlgorithmEntry entry = configurationService.getAlgorithmEntry(testModule1, "name2");
		assertEquals("name2", entry.getAlgorithm());
		assertTrue(entry.isActive());
	}

	@Test
	public void getAlgorithmEntry_nullEntry() {
		assertNull(configurationService.getAlgorithmEntry(testModule1, "non.existing.algorithm"));
	}

	@Test
	public void setAlgorithmEntry_validEntry() {
		String algorithm = "name1";
		configurationService.setAlgorithmEntry(testModule1ClassName, algorithm, false);
		assertEquals("name1", testModule1.getAlgorithmEntries().get(0).getAlgorithm());
		assertEquals(false, testModule1.getAlgorithmEntries().get(0).isActive());
		verify(configurationDAO).updateAlgorithmEntry(testModule1ClassName, algorithm, false);
	}

	@Test
	public void setAlgorithmEntry_nonExistingEntry() {
		String nonExisting = "non.existing.algorithm";
		configurationService.setAlgorithmEntry(testModule1ClassName, nonExisting, false);
		verify(configurationDAO, never()).updateAlgorithmEntry(testModule1ClassName, nonExisting, false);
	}
	
	@Test
	public void addFormatEntry_validEntry() {
		configurationService.addFormatEntry(testParserModuleClassName, "text/xml", "http://xyz", "UTF-8");
		assertTrue(!configurationService.getConfigurationModule(testParserModuleClassName).getFormatEntries().isEmpty());
//		verify(configurationDAO.getFormatEntries(testParserModuleClassName).get(0));
	}
}
