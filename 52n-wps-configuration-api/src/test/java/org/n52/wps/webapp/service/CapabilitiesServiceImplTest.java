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

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.n52.wps.webapp.dao.CapabilitiesDAO;
import org.n52.wps.webapp.entities.ServiceIdentification;
import org.n52.wps.webapp.entities.ServiceProvider;

public class CapabilitiesServiceImplTest {
	@InjectMocks
	private CapabilitiesService capabilitiesService;
	
	@Mock
	private CapabilitiesDAO capabilitiesDAO;
	
	@Before
	public void setup() {
		capabilitiesService = new CapabilitiesServiceImpl();
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testGetServiceIdentification() throws Exception {
		when(capabilitiesDAO.getServiceIdentification()).thenReturn(new ServiceIdentification());
		ServiceIdentification serviceIdentification = capabilitiesService.getServiceIdentification();
		assertNotNull(serviceIdentification);
	}
	
	@Test
	public void testGetServiceProvider() throws Exception {
		when(capabilitiesDAO.getServiceProvider()).thenReturn(new ServiceProvider());
		ServiceProvider serviceProvider = capabilitiesService.getServiceProvider();
		assertNotNull(serviceProvider);
	}
	
	@Test
	public void testSaveServiceIdentification_validServiceIdentification() throws Exception {
		ServiceIdentification serviceIdentification = new ServiceIdentification();
		capabilitiesService.saveServiceIdentification(serviceIdentification);
		verify(capabilitiesDAO).saveServiceIdentification(serviceIdentification);
	}
	
	@Test
	public void testSaveServiceProvider_validServiceProvider() throws Exception {
		ServiceProvider serviceProvider = new ServiceProvider();
		capabilitiesService.saveServiceProvider(serviceProvider);
		verify(capabilitiesDAO).saveServiceProvider(serviceProvider);
	}
}
