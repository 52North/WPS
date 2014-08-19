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
package org.n52.wps.webapp.api;

import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.n52.wps.webapp.service.BackupService;
import org.n52.wps.webapp.service.CapabilitiesService;
import org.n52.wps.webapp.service.ConfigurationService;
import org.n52.wps.webapp.service.LogConfigurationsService;
import org.n52.wps.webapp.service.UserService;

public class ConfigurationManagerImplTest {

	@InjectMocks
	private ConfigurationManager configurationManager;
	
	@Mock
	private ConfigurationService configurationService;

	@Mock
	private UserService userService;

	@Mock
	private CapabilitiesService capabilitiesService;

	@Mock
	private LogConfigurationsService logConfigurationsService;
	
	@Mock
	private BackupService backupService;
		
	@Before
	public void setup() {
		configurationManager = new ConfigurationManagerImpl();
		MockitoAnnotations.initMocks(this);
	}
	
	@After
	public void tearDown() {
		configurationManager = null;
	}

	@Test
	public void getConfigurationServices() {
		assertNotNull(configurationManager.getConfigurationServices());
	}
	
	@Test
	public void getUserServices() {
		assertNotNull(configurationManager.getUserServices());
	}
	
	@Test
	public void getCapabilitiesServices() {
		assertNotNull(configurationManager.getCapabilitiesServices());
	}
	
	@Test
	public void getLogConfigurationsServices() {
		assertNotNull(configurationManager.getLogConfigurationsServices());
	}
	
	@Test
	public void getLogBackupServices() {
		assertNotNull(configurationManager.getBackupServices());
	}
}
