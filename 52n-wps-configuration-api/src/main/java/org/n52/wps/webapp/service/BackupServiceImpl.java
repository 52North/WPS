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

import org.hsqldb.lib.tar.DbBackup;
import org.hsqldb.lib.tar.TarMalformatException;
import org.n52.wps.webapp.api.WPSConfigurationException;
import org.n52.wps.webapp.util.ResourcePathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
/**
 * The implementation for the {@link BackupService} interface.
 */
@Service("backupService")
public class BackupServiceImpl implements BackupService {
	public final static String RESOURCES_FOLDER = "static";
	public final static String DATABASE_FOLDER = "WEB-INF/classes/db/data";
	public final static String LOG = "WEB-INF/classes/logback.xml";
	public final static String WPS_CAPABILITIES_SKELETON = "config/wpsCapabilitiesSkeleton.xml";

	@Autowired
	private ResourcePathUtil resourcePathUtil;

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	private final Logger LOGGER = LoggerFactory.getLogger(BackupService.class);

	@Override
	public String createBackup(String[] itemsToBackup) throws IOException {
		LOGGER.debug("Starting backup process.");
		String zipPath = null;
		if (itemsToBackup != null && itemsToBackup.length > 0) {
			// Zip archive will be saved as WPSConfig_{date}.zip (e.g. WPSConfig_2013-09-12.zip)
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			zipPath = getResourcesFolderPath() + File.separator + "WPSBackup_" + format.format(new Date()) + ".zip";
			ZipOutputStream zipOutput = new ZipOutputStream(new FileOutputStream(zipPath));

			String logAbsolutePath = getLogFilePath();
			String wpsCapabilitiesSkeletonAbsolutePath = getCapabilitiesPath();

			for (String s : itemsToBackup) {
				if (s.equals("database")) {
					LOGGER.debug("Trying to backup the database.");
					backupDatabase(zipOutput);
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
			LOGGER.debug("Backup file '{}' is created.", zipPath);
		}
		return zipPath;
	}

	@Override
	public int restoreBackup(InputStream zipFile) throws IOException, WPSConfigurationException {
		LOGGER.debug("Starting restore process.");
		int numberOfItemsRestored = 0;
		if (zipFile != null) {
			ZipInputStream zipInput = new ZipInputStream(zipFile);
			ZipEntry entry = null;
			while ((entry = zipInput.getNextEntry()) != null) {
				if (entry.getName().endsWith(".tar.gz")) {
					LOGGER.debug("Trying to restore the database from '{}'.", entry.getName());
					extractToFile(new File(getResourcesFolderPath() + "/" + entry.getName()), zipInput);
					restoreDatabase(entry.getName());
					numberOfItemsRestored++;
				}
				if (entry.getName().endsWith("logback.xml")) {
					LOGGER.debug("Trying to restore '{}'.", entry.getName());
					extractToFile(new File(getLogFilePath()), zipInput);
					numberOfItemsRestored++;
				}
				if (entry.getName().endsWith("wpsCapabilitiesSkeleton.xml")) {
					LOGGER.debug("Trying to restore '{}'.", entry.getName());
					extractToFile(new File(getCapabilitiesPath()), zipInput);
					numberOfItemsRestored++;
				}
			}
			zipInput.close();

			// if no items were found, the Zip archive is not a valid WPSBackup
			if (numberOfItemsRestored < 1) {
				throw new WPSConfigurationException("Not a valid WPSBackup Zip archive.");
			}
		}
		LOGGER.debug("Restored '{}' items.", numberOfItemsRestored);
		return numberOfItemsRestored;
	}

	/*
	 * Backup the HSQLDB database to a tar.gz file
	 */
	private void backupDatabase(ZipOutputStream zipOutput) throws IOException {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

		// Create a database backup with the following name DbBackup_{date}.tar.gz
		String tarPath = getResourcesFolderPath() + File.separator + "DbBackup_" + format.format(new Date())
				+ ".tar.gz";
		namedParameterJdbcTemplate.getJdbcOperations().execute("BACKUP DATABASE TO '" + tarPath + "'");
		File dbTarFile = new File(tarPath);
		writeToZip(dbTarFile, zipOutput);

		// The tar file has been included in the overall backup zip file, so delete it
		if (dbTarFile.exists()) {
			dbTarFile.delete();
		}
	}

	/*
	 * Restore the database
	 */
	private void restoreDatabase(String tarFileName) throws IOException {
		String tarPath = getResourcesFolderPath() + File.separator + tarFileName;
		String databaseFolder = getDatabaseFolderPath();

		// Must shutdown the database first before restoring
		namedParameterJdbcTemplate.getJdbcOperations().execute("SHUTDOWN SCRIPT");
		try {
			DbBackup.main(new String[] { "--extract", "--overwrite", tarPath, databaseFolder });
		} catch (TarMalformatException e) {
			LOGGER.error("Unable to restore the database from the supplied tar.gz file: ", e);
		} finally {
			// Delete the tag.gz file
			new File(tarPath).delete();
		}
	}

	private void writeToZip(File file, ZipOutputStream zipOutput) throws IOException {
		FileInputStream input = null;
		if (file.exists()) {
			input = new FileInputStream(file);
			zipOutput.putNextEntry(new ZipEntry(file.getName()));

			// write to Zip
			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = input.read(buffer)) > 0) {
				zipOutput.write(buffer, 0, bytesRead);
			}

			input.close();
		}
		zipOutput.closeEntry();
	}

	private void extractToFile(File file, ZipInputStream zipInput) throws IOException {
		if (!file.exists()) {
			file.createNewFile();
		}
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

	private String getResourcesFolderPath() {
		return resourcePathUtil.getWebAppResourcePath(RESOURCES_FOLDER);
	}

	private String getDatabaseFolderPath() {
		return resourcePathUtil.getWebAppResourcePath(DATABASE_FOLDER);
	}

	private String getLogFilePath() {
		return resourcePathUtil.getWebAppResourcePath(LOG);
	}

	private String getCapabilitiesPath() {
		return resourcePathUtil.getWebAppResourcePath(WPS_CAPABILITIES_SKELETON);
	}
}
