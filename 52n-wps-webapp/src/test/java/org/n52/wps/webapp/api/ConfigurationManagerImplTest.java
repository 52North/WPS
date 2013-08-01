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

import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
}
