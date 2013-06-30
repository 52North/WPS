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

package org.n52.wps.webapp.dao;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.n52.wps.WPSConfigurationDocument;
import org.n52.wps.WPSConfigurationDocument.WPSConfiguration;
import org.n52.wps.impl.WPSConfigurationDocumentImpl.WPSConfigurationImpl;
import org.n52.wps.webapp.common.AbstracTest;
import org.springframework.beans.factory.annotation.Autowired;

public class ConfigurationDAOXmlTest extends AbstracTest {
	
	@Autowired
	@InjectMocks
	private ConfigurationDAO configurationDAO;
	
	@Autowired
	private WPSConfigurationImpl wpsConfigurationImpl;
	
	@Mock
	private WPSConfiguration wpsConfiguration;
	
	@Mock
	private WPSConfigurationDocument wpsConfigurationDocument;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testSave() throws IOException, XmlException {
		configurationDAO.save();
		verify(wpsConfiguration).set(wpsConfigurationImpl);
		verify(wpsConfigurationDocument).save(any(File.class), any(XmlOptions.class));
	}
}
