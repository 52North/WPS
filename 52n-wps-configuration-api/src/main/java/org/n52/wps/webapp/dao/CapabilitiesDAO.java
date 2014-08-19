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
package org.n52.wps.webapp.dao;

import org.n52.wps.webapp.entities.ServiceIdentification;
import org.n52.wps.webapp.entities.ServiceProvider;

/**
 * Read and write to/from the capabilities configuration file to/from {@link ServiceIdentification} and
 * {@link ServiceProvider} objects.
 */
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
