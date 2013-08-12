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

import org.n52.wps.webapp.entities.ServiceIdentification;
import org.n52.wps.webapp.entities.ServiceProvider;

public interface CapabilitiesDAO {
	/**
	 * Parse wpsCapabilitiesSkeleton.xml and map service identification properties to a {@code ServiceIdentification}
	 * object
	 * 
	 * @return Populated {@code ServiceIdentification} object
	 */
	ServiceIdentification getServiceIdentification();

	/**
	 * Write {@code ServiceIdentification} values to wpsCapabilitiesSkeleton.xml
	 * 
	 * @param {@code ServiceIdentification}
	 */
	void saveServiceIdentification(ServiceIdentification serviceIdentification);

	/**
	 * Parse wpsCapabilitiesSkeleton.xml and map service provider properties to a {@code ServiceProvider} object
	 * 
	 * @return Populated {@code ServiceProvider} object
	 */
	ServiceProvider getServiceProvider();

	/**
	 * Write {@code ServiceProvider} values to wpsCapabilitiesSkeleton.xml
	 * 
	 * @param {@code ServiceProvider}
	 */
	void saveServiceProvider(ServiceProvider serviceProvider);
}
