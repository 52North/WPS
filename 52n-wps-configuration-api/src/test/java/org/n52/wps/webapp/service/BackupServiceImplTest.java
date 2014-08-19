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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.contains;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.zip.ZipFile;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.n52.wps.webapp.api.WPSConfigurationException;
import org.n52.wps.webapp.util.ResourcePathUtil;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class BackupServiceImplTest {

	@InjectMocks
	private BackupService backupService;

	@Mock
	private ResourcePathUtil resourcePathUtil;
	
	@Mock
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate; 

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Before
	public void setup() {
		backupService = new BackupServiceImpl();
		MockitoAnnotations.initMocks(this);
		when(namedParameterJdbcTemplate.getJdbcOperations()).thenReturn(Mockito.mock(JdbcOperations.class));
	}

	@After
	public void tearDown() {
		backupService = null;
	}

	@Test
	public void createBackup_validItemsToBackup() throws Exception {
		when(resourcePathUtil.getWebAppResourcePath(BackupServiceImpl.RESOURCES_FOLDER)).thenReturn(
				"src/test/resources/testfiles");
		when(resourcePathUtil.getWebAppResourcePath(BackupServiceImpl.LOG)).thenReturn(
				"src/test/resources/testfiles/testlogback.xml");
		when(resourcePathUtil.getWebAppResourcePath(BackupServiceImpl.WPS_CAPABILITIES_SKELETON)).thenReturn(
				"src/test/resources/testfiles/wpsCapabilitiesSkeleton.xml");
		
		String[] itemsToBackup = { "database", "log", "wpscapabilities" };
		String zipPath = backupService.createBackup(itemsToBackup);
		File zipFile = new File(zipPath);
		ZipFile zipArchive = new ZipFile(zipFile);
		assertTrue(zipFile.exists());
		assertThat(zipFile.getName(), containsString("WPSBackup"));
		assertThat(zipFile.getName(), endsWith(".zip"));
		
		verify(namedParameterJdbcTemplate.getJdbcOperations()).execute(contains("BACKUP DATABASE TO"));
		assertEquals("testlogback.xml", zipArchive.getEntry("testlogback.xml").getName());
		assertEquals("wpsCapabilitiesSkeleton.xml", zipArchive.getEntry("wpsCapabilitiesSkeleton.xml").getName());
		
		// clean up
		zipArchive.close();
		zipFile.delete();
	}

	@Test
	public void createBackup_emptyItemsToBackup() throws Exception {
		String[] itemsToBackup = {};
		String zipPath = backupService.createBackup(itemsToBackup);
		assertNull(zipPath);
	}

	@Test
	public void restore_validZipFile() throws Exception {
		when(resourcePathUtil.getWebAppResourcePath(BackupServiceImpl.RESOURCES_FOLDER)).thenReturn(
				"src/test/resources/testfiles/backuptest");
		when(resourcePathUtil.getWebAppResourcePath(BackupServiceImpl.DATABASE_FOLDER)).thenReturn(
				"src/test/resources/testfiles/backuptest/data");
		when(resourcePathUtil.getWebAppResourcePath(BackupServiceImpl.LOG)).thenReturn(
				"src/test/resources/testfiles/backuptest/testlogback.xml");
		when(resourcePathUtil.getWebAppResourcePath(BackupServiceImpl.WPS_CAPABILITIES_SKELETON)).thenReturn(
				"src/test/resources/testfiles/backuptest/wpsCapabilitiesSkeleton.xml");
		
		// Create test folder
		File folder = new File("src/test/resources/testfiles/backuptest/");
		folder.mkdir();
		
		// Create temp data folder
		File data = new File("src/test/resources/testfiles/backuptest/data");
		data.mkdir();

		// Get the test zip file input stream
		File zipFile = new File("src/test/resources/testfiles/WPSBackup_Valid_Test.zip");
		InputStream is = new FileInputStream(zipFile);
		
		backupService.restoreBackup(is);
		assertTrue(new File("src/test/resources/testfiles/backuptest/data/wpsconfig.script").exists());
		assertTrue(new File("src/test/resources/testfiles/backuptest/data/wpsconfig.properties").exists());
		assertTrue(new File("src/test/resources/testfiles/backuptest/testlogback.xml").exists());
		assertTrue(new File("src/test/resources/testfiles/backuptest/wpsCapabilitiesSkeleton.xml").exists());

		// Cleanup
		deleteFolder(folder);
	}

	@Test
	public void restore_invalidZipFile() throws Exception {
		File zipFile = new File("src/test/resources/testfiles/WPSBackup_Empty_Test.zip");
		InputStream is = new FileInputStream(zipFile);
		exception.expect(WPSConfigurationException.class);
		backupService.restoreBackup(is);
	}
	
	private void deleteFolder(File folder) {
		for (File file : folder.listFiles()) {
			if (file.isDirectory()) {
				deleteFolder(file);
			}
			file.delete();
		}
		folder.delete();
	}
}
