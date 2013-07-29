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

import org.n52.wps.webapp.api.WPSConfigurationException;
import org.n52.wps.webapp.dao.CapabilitiesDAO;
import org.n52.wps.webapp.entities.ServiceIdentification;
import org.n52.wps.webapp.entities.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("capabilitiesService")
public class CapabilitiesServiceImpl implements CapabilitiesService {

	@Autowired
	private CapabilitiesDAO capabilitiesDAO;

	private static Logger LOGGER = LoggerFactory.getLogger(CapabilitiesServiceImpl.class);

	@Override
	public ServiceIdentification getServiceIdentification() throws WPSConfigurationException {
		return capabilitiesDAO.getServiceIdentification();
	}

	@Override
	public ServiceProvider getServiceProvider() throws WPSConfigurationException {
		return capabilitiesDAO.getServiceProvider();
	}

	@Override
	public void saveServiceIdentification(ServiceIdentification serviceIdentification) throws WPSConfigurationException {
		if (serviceIdentification != null) {
			capabilitiesDAO.saveServiceIdentification(serviceIdentification);
			LOGGER.debug("Service identification information has been updated");
		}
	}

	@Override
	public void saveServiceProvider(ServiceProvider serviceProvider) throws WPSConfigurationException {
		if (serviceProvider != null) {
			capabilitiesDAO.saveServiceProvider(serviceProvider);
			LOGGER.debug("Service provider information has been updated");
		}
	}
}
