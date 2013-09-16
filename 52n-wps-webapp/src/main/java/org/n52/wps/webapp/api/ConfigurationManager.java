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

import org.n52.wps.webapp.service.BackupService;
import org.n52.wps.webapp.service.CapabilitiesService;
import org.n52.wps.webapp.service.ConfigurationService;
import org.n52.wps.webapp.service.LogConfigurationsService;
import org.n52.wps.webapp.service.UserService;

public interface ConfigurationManager {
	/**
	 * Access configuration services and operations
	 * @return {@code ConfigurationService}
	 */
	ConfigurationService getConfigurationServices();
	
	/**
	 * Access user services and operations
	 * @return {@code UserService}
	 */
	UserService getUserServices();
	
	/**
	 * Access capabilities services and operations
	 * @return {@code CapabilitiesService}
	 */
	CapabilitiesService getCapabilitiesServices();
	
	/**
	 * Access log services and operations
	 * @return {@code LogConfigurationsService}
	 */
	LogConfigurationsService getLogConfigurationsServices();
	
	/**
	 * Access backup and restore services and operations
	 * @return {@code BackupService}
	 */
	BackupService getBackupServices();
}
