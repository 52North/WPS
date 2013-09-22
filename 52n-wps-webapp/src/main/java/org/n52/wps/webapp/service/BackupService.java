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

import java.io.IOException;
import java.io.InputStream;

import org.n52.wps.webapp.api.WPSConfigurationException;

public interface BackupService {
	/**
	 * Create a Zip archive of the items provided.
	 * 
	 * @param itemsToBackup
	 *            a list of items to back up
	 * @return the location of the created Zip archive
	 * @throws IOException
	 *             if an item cannot be found in the expected path
	 */
	String createBackup(String[] itemsToBackup) throws IOException;

	/**
	 * Extract a created Zip archive and overwrite configuration files
	 * 
	 * @param zipFile
	 *            the backup Zip archive inputstream
	 * @return the number of items restored
	 * @throws IOException
	 *             if the archive cannot be extracted or the content cannot be written
	 * @throws WPSConfigurationException
	 *             if the archive contains invalid configuration files
	 */
	int restoreBackup(InputStream zipFile) throws IOException, WPSConfigurationException;
}
