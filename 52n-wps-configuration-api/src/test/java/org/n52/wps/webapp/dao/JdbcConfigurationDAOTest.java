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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.n52.wps.webapp.api.AlgorithmEntry;
import org.n52.wps.webapp.testmodules.TestConfigurationModule1;
import org.n52.wps.webapp.testmodules.TestConfigurationModule2;
import org.n52.wps.webapp.testmodules.TestConfigurationModule3;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@TransactionConfiguration(defaultRollback = true)
public class JdbcConfigurationDAOTest {

	private ConfigurationDAO configurationDAO;
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	private EmbeddedDatabaseBuilder builder;
	private EmbeddedDatabase db;

	private String module1ClassName = TestConfigurationModule1.class.getName();
	private String module2ClassName = TestConfigurationModule2.class.getName();
	
	private TestConfigurationModule1 testModule1;
	private TestConfigurationModule2 testModule2;
	private TestConfigurationModule3 testModule3;

	@Before
	public void setup() {
		builder = new EmbeddedDatabaseBuilder();
		db = builder.setType(EmbeddedDatabaseType.HSQL).addScript("db" + File.separator + "schema.sql")
				.addScript("test-data-simple.sql").build();
		namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(db);
		configurationDAO = new JdbcConfigurationDAO();
		testModule1 = new TestConfigurationModule1();
		testModule2 = new TestConfigurationModule2();
		testModule3 = new TestConfigurationModule3();
		ReflectionTestUtils.setField(configurationDAO, "namedParameterJdbcTemplate", namedParameterJdbcTemplate);
	}

	@After
	public void tearDown() {
		db.shutdown();
		testModule1 =  null;
		testModule2 = null;
		testModule3 = null;
		configurationDAO = null;
	}

	@Test
	public void getConfigurationModuleStatus_existingModule() {
		boolean statusOfModule1 = configurationDAO.getConfigurationModuleStatus(testModule1);
		assertTrue(statusOfModule1);
		boolean statusOfModule2 = configurationDAO.getConfigurationModuleStatus(testModule2);
		assertFalse(statusOfModule2);
	}
	
	@Test
	public void getConfigurationModuleStatus_nonExistingModule() {
		Boolean statusOfModule = configurationDAO.getConfigurationModuleStatus(testModule3);
		assertNull(statusOfModule);
	}
	
	@Test
	public void insertConfigurationModule() {
		Boolean statusOfModule3 = configurationDAO.getConfigurationModuleStatus(testModule3);
		assertNull(statusOfModule3);
		configurationDAO.insertConfigurationModule(testModule3);
		statusOfModule3 = configurationDAO.getConfigurationModuleStatus(testModule3);
		assertTrue(statusOfModule3);
	}
	
	@Test
	public void updateConfigurationModule() {
		boolean statusOfModule1 = configurationDAO.getConfigurationModuleStatus(testModule1);
		assertTrue(statusOfModule1);
		testModule1.setActive(false);
		configurationDAO.updateConfigurationModuleStatus(testModule1);
		statusOfModule1 = configurationDAO.getConfigurationModuleStatus(testModule1);
		assertFalse(statusOfModule1);
	}
	
	@Test
	public void getConfigurationEntryValue_validEntry() throws Exception {
		Object stringValue = configurationDAO.getConfigurationEntryValue(module1ClassName, "test.string.key");
		assertEquals("Test Value", stringValue);

		Object integerValue = configurationDAO.getConfigurationEntryValue(module1ClassName, "test.integer.key");
		assertEquals(23, Integer.parseInt(integerValue.toString()));

		Object doubleValue = configurationDAO.getConfigurationEntryValue(module1ClassName, "test.double.key");
		assertEquals(11.3, Double.parseDouble(doubleValue.toString()), 0);

		Object booleanValue = configurationDAO.getConfigurationEntryValue(module1ClassName, "test.boolean.key");
		assertEquals(true, Boolean.parseBoolean(booleanValue.toString()));

		Object fileValue = configurationDAO.getConfigurationEntryValue(module1ClassName, "test.file.key");
		assertEquals(new File("test_path"), new File(fileValue.toString()));

		Object uriValue = configurationDAO.getConfigurationEntryValue(module1ClassName, "test.uri.key");
		assertEquals(new URI("test_path"), new URI(uriValue.toString()));
	}

	@Test
	public void getConfigurationEntryValue_nullEntry() throws Exception {
		Object nullValue = configurationDAO.getConfigurationEntryValue(module1ClassName, "non.existing.entry");
		assertNull(nullValue);
	}

	@Test
	public void insertConfigurationEntryValue() throws Exception {
		assertNull(configurationDAO.getConfigurationEntryValue(module2ClassName, "test.string.key"));
		configurationDAO.insertConfigurationEntryValue(module2ClassName, "test.string.key", "inserted string");
		Object stringValue = configurationDAO.getConfigurationEntryValue(module2ClassName, "test.string.key");
		assertEquals("inserted string", stringValue);

		assertNull(configurationDAO.getConfigurationEntryValue(module2ClassName, "test.integer.key"));
		configurationDAO.insertConfigurationEntryValue(module2ClassName, "test.integer.key", 99);
		Object integerValue = configurationDAO.getConfigurationEntryValue(module2ClassName, "test.integer.key");
		assertEquals(99, Integer.parseInt(integerValue.toString()));

		assertNull(configurationDAO.getConfigurationEntryValue(module2ClassName, "test.double.key"));
		configurationDAO.insertConfigurationEntryValue(module2ClassName, "test.double.key", 99.9);
		Object doubleValue = configurationDAO.getConfigurationEntryValue(module2ClassName, "test.double.key");
		assertEquals(99.9, Double.parseDouble(doubleValue.toString()), 0);

		assertNull(configurationDAO.getConfigurationEntryValue(module2ClassName, "test.boolean.key"));
		configurationDAO.insertConfigurationEntryValue(module2ClassName, "test.boolean.key", false);
		Object booleanValue = configurationDAO.getConfigurationEntryValue(module2ClassName, "test.boolean.key");
		assertEquals(false, Boolean.parseBoolean(booleanValue.toString()));

		assertNull(configurationDAO.getConfigurationEntryValue(module2ClassName, "test.file.key"));
		configurationDAO.insertConfigurationEntryValue(module2ClassName, "test.file.key", "inserted_path");
		Object fileValue = configurationDAO.getConfigurationEntryValue(module2ClassName, "test.file.key");
		assertEquals(new File("inserted_path"), new File(fileValue.toString()));

		assertNull(configurationDAO.getConfigurationEntryValue(module2ClassName, "test.uri.key"));
		configurationDAO.insertConfigurationEntryValue(module2ClassName, "test.uri.key", "inserted_path");
		Object uriValue = configurationDAO.getConfigurationEntryValue(module2ClassName, "test.uri.key");
		assertEquals(new URI("inserted_path"), new URI(uriValue.toString()));
	}

	@Test
	public void updateConfigurationEntryValue() throws Exception {
		Object stringValue = configurationDAO.getConfigurationEntryValue(module1ClassName, "test.string.key");
		assertEquals("Test Value", stringValue);
		configurationDAO.updateConfigurationEntryValue(module1ClassName, "test.string.key", "inserted string");
		stringValue = configurationDAO.getConfigurationEntryValue(module1ClassName, "test.string.key");
		assertEquals("inserted string", stringValue);

		Object integerValue = configurationDAO.getConfigurationEntryValue(module1ClassName, "test.integer.key");
		assertEquals(23, Integer.parseInt(integerValue.toString()));
		configurationDAO.updateConfigurationEntryValue(module1ClassName, "test.integer.key", 99);
		integerValue = configurationDAO.getConfigurationEntryValue(module1ClassName, "test.integer.key");
		assertEquals(99, Integer.parseInt(integerValue.toString()));

		Object doubleValue = configurationDAO.getConfigurationEntryValue(module1ClassName, "test.double.key");
		assertEquals(11.3, Double.parseDouble(doubleValue.toString()), 0);
		configurationDAO.updateConfigurationEntryValue(module1ClassName, "test.double.key", 99.9);
		doubleValue = configurationDAO.getConfigurationEntryValue(module1ClassName, "test.double.key");
		assertEquals(99.9, Double.parseDouble(doubleValue.toString()), 0);

		Object booleanValue = configurationDAO.getConfigurationEntryValue(module1ClassName, "test.boolean.key");
		assertEquals(true, Boolean.parseBoolean(booleanValue.toString()));
		configurationDAO.updateConfigurationEntryValue(module1ClassName, "test.boolean.key", false);
		booleanValue = configurationDAO.getConfigurationEntryValue(module1ClassName, "test.boolean.key");
		assertEquals(false, Boolean.parseBoolean(booleanValue.toString()));

		Object fileValue = configurationDAO.getConfigurationEntryValue(module1ClassName, "test.file.key");
		assertEquals(new File("test_path"), new File(fileValue.toString()));
		configurationDAO.updateConfigurationEntryValue(module1ClassName, "test.file.key", "inserted_path");
		fileValue = configurationDAO.getConfigurationEntryValue(module1ClassName, "test.file.key");
		assertEquals(new File("inserted_path"), new File(fileValue.toString()));

		Object uriValue = configurationDAO.getConfigurationEntryValue(module1ClassName, "test.uri.key");
		assertEquals(new URI("test_path"), new URI(uriValue.toString()));
		configurationDAO.updateConfigurationEntryValue(module1ClassName, "test.uri.key", "inserted_path");
		uriValue = configurationDAO.getConfigurationEntryValue(module1ClassName, "test.uri.key");
		assertEquals(new URI("inserted_path"), new URI(uriValue.toString()));
	}

	@Test
	public void getAlgorithmEntry_validEntry() {
		AlgorithmEntry entry1 = configurationDAO.getAlgorithmEntry(module1ClassName, "name1");
		assertEquals("name1", entry1.getAlgorithm());
		assertTrue(entry1.isActive());

		AlgorithmEntry entry2 = configurationDAO.getAlgorithmEntry(module1ClassName, "name2");
		assertEquals("name2", entry2.getAlgorithm());
		assertTrue(entry2.isActive());
	}

	@Test
	public void getAlgorithmEntry_nullEntry() {
		AlgorithmEntry entry = configurationDAO.getAlgorithmEntry(module1ClassName, "non.existing.entry");
		assertNull(entry);
	}

	@Test
	public void insertAlgorithmEntry() {
		assertNull(configurationDAO.getAlgorithmEntry(module2ClassName, "name1"));
		configurationDAO.insertAlgorithmEntry(module2ClassName, "name1", false);
		AlgorithmEntry entry1 = configurationDAO.getAlgorithmEntry(module2ClassName, "name1");
		assertEquals("name1", entry1.getAlgorithm());
		assertFalse(entry1.isActive());
	}

	@Test
	public void updateAlgorithmEntry() {
		AlgorithmEntry entry1 = configurationDAO.getAlgorithmEntry(module1ClassName, "name1");
		assertTrue(entry1.isActive());
		configurationDAO.updateAlgorithmEntry(module1ClassName, "name1", false);
		entry1 = configurationDAO.getAlgorithmEntry(module1ClassName, "name1");
		assertFalse(entry1.isActive());
	}
}
