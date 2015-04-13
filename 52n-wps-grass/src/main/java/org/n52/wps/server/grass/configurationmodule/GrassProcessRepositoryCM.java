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
package org.n52.wps.server.grass.configurationmodule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.n52.wps.server.grass.GrassProcessRepository;
import org.n52.wps.webapp.api.AlgorithmEntry;
import org.n52.wps.webapp.api.ClassKnowingModule;
import org.n52.wps.webapp.api.ConfigurationCategory;
import org.n52.wps.webapp.api.ConfigurationKey;
import org.n52.wps.webapp.api.FormatEntry;
import org.n52.wps.webapp.api.types.ConfigurationEntry;
import org.n52.wps.webapp.api.types.StringConfigurationEntry;

/**
 * @author Benjamin Pross (bpross-52n)
 *
 */
public class GrassProcessRepositoryCM extends ClassKnowingModule{
	
	public static final String grassHomeKey = "grass_home";
	public static final String pythonHomeKey = "python_home";
	public static final String pythonPathKey = "python_path";
	public static final String moduleStarterHomeKey = "moduleStarter_home";
	public static final String tmpDirKey = "tmp_dir";
	public static final String gisrcDirKey = "gisrc_dir";
	public static final String addonDirKey = "addon_dir";
	
	private ConfigurationEntry<String> grassHomeEntry = new StringConfigurationEntry(grassHomeKey, "Grass 7 Home", "Path to GRASS 7 installation, e.g. 'C:\\Program Files (x86)\\GRASS GIS 7.0.0' or '/usr/lib/grass70'",
			true, "C:\\Program Files (x86)\\GRASS GIS 7.0.0");	
	private ConfigurationEntry<String> pythonHomeEntry = new StringConfigurationEntry(pythonHomeKey, "Python Home", "Path to python executable, e.g. 'C:\\python27' or '/usr/bin'",
			true, "C:\\python27");
	private ConfigurationEntry<String> pythonPathEntry = new StringConfigurationEntry(pythonPathKey, "Python Path", "Path to python installation, e.g. 'C:\\python27' or '/usr/lib/python2.7'",
			true, "C:\\python27");
	private ConfigurationEntry<String> moduleStarterHomeEntry = new StringConfigurationEntry(moduleStarterHomeKey, "ModuleStarter Home", "Path to GRASSModuleStarter (wps-grass-bridge), e.g. 'D:\\dev\\grass\\wps-grass-bridge-patched\\gms' or '/home/user/wps-grass-bridge-patched/gms'",
			true, "D:\\dev\\grass\\wps-grass-bridge-patched\\gms");
	private ConfigurationEntry<String> tmpDirEntry = new StringConfigurationEntry(tmpDirKey, "TMP Directory", "Path to TMP Directory, e.g. 'D:\\tmp\\grass_tmp' or '/tmp/grass_tmp'. Note: This directory will be emptied after each WPS start!",
			true, "D:\\tmp\\grass_tmp");
	private ConfigurationEntry<String> gisrcDirEntry = new StringConfigurationEntry(gisrcDirKey, "GISRC File", "Path to GISRC file, e.g. 'C:\\Program Files (x86)\\GRASS GIS 7.0.0\\demolocation\\.grassrc70' or '/home/user/grassdata/.grassrc70'",
			true, "C:\\Program Files (x86)\\GRASS GIS 7.0.0\\demolocation\\.grassrc70");
	private ConfigurationEntry<String> addonDirEntry = new StringConfigurationEntry(addonDirKey, "Addon Directory", "Path to addon Directory, optional.",
			false, "N/A");
	
	private List<? extends ConfigurationEntry<?>> configurationEntries = Arrays.asList(grassHomeEntry, pythonHomeEntry, pythonPathEntry, moduleStarterHomeEntry, tmpDirEntry, gisrcDirEntry, addonDirEntry);

	private String grassHome;
	private String pythonHome;
	private String pythonPath;
	private String moduleStarterHome;
	private String tmpDir;
	private String gisrcDir;
	private String addonDir;
	
	private boolean isActive = true;

	private List<AlgorithmEntry> algorithmEntries;
	
	public GrassProcessRepositoryCM() {
		algorithmEntries = new ArrayList<>();
	}
	
	@Override
	public String getModuleName() {
		return "GRASS 7 Algorithm Repository";
	}

	@Override
	public boolean isActive() {
		return isActive;
	}

	@Override
	public void setActive(boolean active) {
		this.isActive = active;
	}

	@Override
	public ConfigurationCategory getCategory() {
		return ConfigurationCategory.REPOSITORY;
	}

	@Override
	public List<? extends ConfigurationEntry<?>> getConfigurationEntries() {
		return configurationEntries;
	}

	@Override
	public List<AlgorithmEntry> getAlgorithmEntries() {
		return algorithmEntries;
	}

	@Override
	public List<FormatEntry> getFormatEntries() {
		return null;
	}

	@Override
	public String getClassName() {
		return GrassProcessRepository.class.getName();
	}

	public String getGrassHome() {
		return grassHome;
	}

	@ConfigurationKey(key = grassHomeKey)
	public void setGrassHome(String grassHome) {
		this.grassHome = grassHome;
	}

	public String getPythonHome() {
		return pythonHome;
	}

	@ConfigurationKey(key = pythonHomeKey)
	public void setPythonHome(String pythonHome) {
		this.pythonHome = pythonHome;
	}

	public String getPythonPath() {
		return pythonPath;
	}

	@ConfigurationKey(key = pythonPathKey)
	public void setPythonPath(String pythonPath) {
		this.pythonPath = pythonPath;
	}

	public String getModuleStarterHome() {
		return moduleStarterHome;
	}

	@ConfigurationKey(key = moduleStarterHomeKey)
	public void setModuleStarterHome(String moduleStarterHome) {
		this.moduleStarterHome = moduleStarterHome;
	}

	public String getTmpDir() {
		return tmpDir;
	}

	@ConfigurationKey(key = tmpDirKey)
	public void setTmpDir(String tmpDir) {
		this.tmpDir = tmpDir;
	}

	public String getGisrcDir() {
		return gisrcDir;
	}

	@ConfigurationKey(key = gisrcDirKey)
	public void setGisrcDir(String gisrcDir) {
		this.gisrcDir = gisrcDir;
	}

	public String getAddonDir() {
		return addonDir;
	}

	@ConfigurationKey(key = addonDirKey)
	public void setAddonDir(String addonDir) {
		this.addonDir = addonDir;
	}

}
