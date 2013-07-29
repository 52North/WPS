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
import static org.mockito.Mockito.never;
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
	public void testSaveServiceIdentification_nullServiceIdentification() throws Exception {
		ServiceIdentification serviceIdentification = null;
		capabilitiesService.saveServiceIdentification(serviceIdentification);
		verify(capabilitiesDAO, never()).saveServiceIdentification(serviceIdentification);
	}
	
	@Test
	public void testSaveServiceProvider_validServiceProvider() throws Exception {
		ServiceProvider serviceProvider = new ServiceProvider();
		capabilitiesService.saveServiceProvider(serviceProvider);
		verify(capabilitiesDAO).saveServiceProvider(serviceProvider);
	}
	
	@Test
	public void testSaveServiceProvider_nullServiceProvider() throws Exception {
		ServiceProvider serviceProvider = null;
		capabilitiesService.saveServiceProvider(serviceProvider);
		verify(capabilitiesDAO, never()).saveServiceProvider(serviceProvider);
	}
}
