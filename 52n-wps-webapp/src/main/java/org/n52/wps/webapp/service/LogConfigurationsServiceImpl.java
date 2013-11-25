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

import org.n52.wps.webapp.dao.LogConfigurationsDAO;
import org.n52.wps.webapp.entities.LogConfigurations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * An implementation for the {@link LogConfigurationsService} interface. This implementation uses the
 * {@link LogConfigurationsDAO} interface to perform get and save from/to the log configuration file.
 */
@Service("logConfigurationsService")
public class LogConfigurationsServiceImpl implements LogConfigurationsService {

	@Autowired
	private LogConfigurationsDAO logConfigurationsDAO;

	private static Logger LOGGER = LoggerFactory.getLogger(LogConfigurationsServiceImpl.class);

	@Override
	public LogConfigurations getLogConfigurations() {
		return logConfigurationsDAO.getLogConfigurations();
	}

	@Override
	public void saveLogConfigurations(LogConfigurations logConfigurations) {
		if (logConfigurations != null) {
			logConfigurationsDAO.saveLogConfigurations(logConfigurations);
			LOGGER.debug("Log configurations has been updated.");
		}
	}

}
