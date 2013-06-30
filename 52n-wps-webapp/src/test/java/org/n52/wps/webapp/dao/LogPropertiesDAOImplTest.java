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

import static org.junit.Assert.assertNotNull;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Test;
import org.mockito.Mockito;
import org.n52.wps.webapp.common.AbstracTest;
import org.springframework.beans.factory.annotation.Autowired;

public class LogPropertiesDAOImplTest extends AbstracTest {
	
	@Autowired
	LogPropertiesDAO logPropertiesDAO;
	
	@Test
	public void testLoad() {
		PropertiesConfiguration props = null;
		props = logPropertiesDAO.load();
		assertNotNull(props);
		assertNotNull(props.getProperty("log4j.rootLogger"));
	}
	
	@Test
	public void testSave() throws ConfigurationException {
		PropertiesConfiguration props = Mockito.mock(PropertiesConfiguration.class);
		logPropertiesDAO.save(props);
		Mockito.verify(props).save("log4j.properties");
	}
}
