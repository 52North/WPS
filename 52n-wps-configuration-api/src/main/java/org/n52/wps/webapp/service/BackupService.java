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
package org.n52.wps.webapp.service;

import java.io.IOException;
import java.io.InputStream;

import org.n52.wps.webapp.api.WPSConfigurationException;

/**
 * Used to backup and restore configurations database and files.
 */
public interface BackupService {
	/**
	 * Create a backup archive for the items provided. Supported items are:
	 * <p>
	 * "database" to backup the database.
	 * </p>
	 * <p>
	 * "log" to backup the logback.xml file.
	 * </p>
	 * <p>
	 * "wpscapabilities" to backup the wpsCapabilitiesSkeleton.xml file.
	 * </p>
	 * 
	 * @param itemsToBackup
	 *            a list of items to back up
	 * @return the location of the created backup archive
	 * @throws IOException
	 *             if an item cannot be found in the expected path
	 */
	String createBackup(String[] itemsToBackup) throws IOException;

	/**
	 * Extract and restore a backup archive
	 * 
	 * @param zipFile
	 *            the backup archive inputstream
	 * @return the number of items restored
	 * @throws IOException
	 *             if the archive cannot be extracted or the content cannot be written
	 * @throws WPSConfigurationException
	 *             if the archive contains invalid configuration files
	 */
	int restoreBackup(InputStream zipFile) throws IOException, WPSConfigurationException;
}
