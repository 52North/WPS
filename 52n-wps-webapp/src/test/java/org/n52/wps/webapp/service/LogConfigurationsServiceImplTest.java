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

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.n52.wps.webapp.dao.LogConfigurationsDAO;
import org.n52.wps.webapp.entities.LogConfigurations;


public class LogConfigurationsServiceImplTest {
	@InjectMocks
	private LogConfigurationsService logConfigurationsService;
	
	@Mock
	LogConfigurationsDAO logConfigurationsDAO;
	
	@Before
	public void setup() {
		logConfigurationsService = new LogConfigurationsServiceImpl();
		MockitoAnnotations.initMocks(this);
	}
	
	@After
	public void tearDown() {
		logConfigurationsService = null;
	}
	
	@Test
	public void getLogConfigurations() throws Exception {
		when(logConfigurationsDAO.getLogConfigurations()).thenReturn(new LogConfigurations());
		LogConfigurations logConfigurations = logConfigurationsService.getLogConfigurations();
		assertNotNull(logConfigurations);
	}
	
	@Test
	public void saveLogConfigurations_validLogConfigurations() throws Exception {
		LogConfigurations logConfigurations = new LogConfigurations();
		logConfigurationsService.saveLogConfigurations(logConfigurations);
		verify(logConfigurationsDAO).saveLogConfigurations(logConfigurations);
	}
	
	@Test
	public void saveLogConfigurations_nullLogConfigurations() throws Exception {
		LogConfigurations logConfigurations = null;
		logConfigurationsService.saveLogConfigurations(logConfigurations);
		verify(logConfigurationsDAO, never()).saveLogConfigurations(logConfigurations);
	}
}
