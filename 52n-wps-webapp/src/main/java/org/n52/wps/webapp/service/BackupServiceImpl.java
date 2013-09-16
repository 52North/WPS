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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.n52.wps.webapp.api.WPSConfigurationException;
import org.n52.wps.webapp.util.ResourcePathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("backupService")
public class BackupServiceImpl implements BackupService {
	private final static String ZIP_FILE_PATH = "/resources";
	private final static String DATABASE_FOLDER_PATH = "WEB-INF/classes/db/data";
	private final static String LOG_PATH = "WEB-INF/classes/logback.xml";
	private final static String WPS_CAPABILITIES_SKELETON_PATH = "config/wpsCapabilitiesSkeleton.xml";

	@Autowired
	private ResourcePathUtil resourcePathUtil;

	private final Logger LOGGER = LoggerFactory.getLogger(BackupService.class);

	@Override
	public String createBackup(String[] itemsToBackup) throws IOException {

		// Zip archive will be saved as WPSConfig_date.zip (e.g. WPSConfig_2013-09-12.zip)
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String zipPath = resourcePathUtil.getWebAppResourcePath(ZIP_FILE_PATH) + File.separator + "WPSBackup_"
				+ format.format(new Date()) + ".zip";
		ZipOutputStream zipOutput = new ZipOutputStream(new FileOutputStream(zipPath));

		String databaseAbsolutePath = resourcePathUtil.getWebAppResourcePath(DATABASE_FOLDER_PATH);
		String logAbsolutePath = resourcePathUtil.getWebAppResourcePath(LOG_PATH);
		String wpsCapabilitiesSkeletonAbsolutePath = resourcePathUtil
				.getWebAppResourcePath(WPS_CAPABILITIES_SKELETON_PATH);

		for (String s : itemsToBackup) {
			if (s.equals("database")) {
				LOGGER.debug("Trying to backup '{}'.", databaseAbsolutePath);
				backupDatabase(new File(databaseAbsolutePath), zipOutput);
			}
			if (s.equals("log")) {
				LOGGER.debug("Trying to backup '{}'.", logAbsolutePath);
				writeToZip(new File(logAbsolutePath), zipOutput);
			}
			if (s.equals("wpscapabilities")) {
				LOGGER.debug("Trying to backup '{}'.", wpsCapabilitiesSkeletonAbsolutePath);
				writeToZip(new File(wpsCapabilitiesSkeletonAbsolutePath), zipOutput);
			}
		}
		zipOutput.close();

		return zipPath;
	}

	@Override
	public void restoreBackup(InputStream zipFile) throws IOException, WPSConfigurationException {
		ZipInputStream zipInput = new ZipInputStream(zipFile);
		ZipEntry entry = null;
		String databaseAbsolutePath = resourcePathUtil.getWebAppResourcePath(DATABASE_FOLDER_PATH);
		String logAbsolutePath = resourcePathUtil.getWebAppResourcePath(LOG_PATH);
		String wpsCapabilitiesSkeletonAbsolutePath = resourcePathUtil
				.getWebAppResourcePath(WPS_CAPABILITIES_SKELETON_PATH);
		boolean itemsFound = false;
		while ((entry = zipInput.getNextEntry()) != null) {
			if (entry.getName().contains("data")) {
				LOGGER.debug("Trying to restore '{}'.", databaseAbsolutePath);
				File destinationFile = new File(databaseAbsolutePath + File.separator + FilenameUtils.getName(entry.getName()));
				extractToFile(destinationFile, zipInput);
				itemsFound = true;
			}
			if (entry.getName().equals("logback.xml")) {
				LOGGER.debug("Trying to restore '{}'.", logAbsolutePath);
				extractToFile(new File(logAbsolutePath), zipInput);
				itemsFound = true;
			}
			if (entry.getName().equals("wpsCapabilitiesSkeleton.xml")) {
				LOGGER.debug("Trying to restore '{}'.", wpsCapabilitiesSkeletonAbsolutePath);
				extractToFile(new File(wpsCapabilitiesSkeletonAbsolutePath), zipInput);
				itemsFound = true;
			}
		}
		zipInput.close();

		// if no items were found, the Zip archive is not a valid WPSBackup
		if (!itemsFound) {
			throw new WPSConfigurationException("Not a valid WPSBackup Zip archive.");
		}
	}

	/*
	 * Backup the database folder located at provided folder path
	 */
	private void backupDatabase(File folder, ZipOutputStream zipOutput) throws IOException {
		// ignore temp subdirectories and lck files
		for (File file : folder.listFiles()) {
			if (file.isDirectory()) {
				continue;
			}
			if (FilenameUtils.getExtension(file.getName()).equals("lck")) {
				continue;
			}
			writeToZip(file, zipOutput);
		}
	}
	
	private void writeToZip(File file, ZipOutputStream zipOutput) throws IOException {
		FileInputStream input = new FileInputStream(file);
		
		// if it is a database file, add it to a data folder within the Zip archive
		if (file.getAbsolutePath().contains("data")) {
			zipOutput.putNextEntry(new ZipEntry("data/" + file.getName()));
		} else {
			zipOutput.putNextEntry(new ZipEntry(file.getName()));
		}

		// write to Zip
		byte[] buffer = new byte[1024];
		int bytesRead;
		while ((bytesRead = input.read(buffer)) > 0) {
			zipOutput.write(buffer, 0, bytesRead);
		}

		input.close();
		zipOutput.closeEntry();

	}

	private void extractToFile(File file, ZipInputStream zipInput) throws IOException {
		FileOutputStream output = new FileOutputStream(file);

		// write to file
		LOGGER.debug("Writing '{}'.", file.getName());
		byte[] buffer = new byte[1024];
		int bytesRead;
		while ((bytesRead = zipInput.read(buffer)) > 0) {
			output.write(buffer, 0, bytesRead);
		}
		output.close();
		zipInput.closeEntry();
	}
}
