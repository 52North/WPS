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
package org.n52.wps.server.grass;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.IAlgorithmRepository;
import org.n52.wps.server.ProcessDescription;
import org.n52.wps.server.grass.configurationmodule.GrassProcessRepositoryCM;
import org.n52.wps.server.grass.util.GRASSWPSConfigVariables;
import org.n52.wps.webapp.api.AlgorithmEntry;
import org.n52.wps.webapp.api.ConfigurationCategory;
import org.n52.wps.webapp.api.ConfigurationModule;
import org.n52.wps.webapp.api.types.ConfigurationEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Benjamin Pross (bpross-52n)
 *
 */
public class GrassProcessRepository implements IAlgorithmRepository {

	private static Logger LOGGER = LoggerFactory.getLogger(GrassProcessRepository.class);
	private Map<String, ProcessDescription> registeredProcesses;
	private Map<String, Boolean> processesAddonFlagMap;
	private final String fileSeparator = System.getProperty("file.separator");
	private ConfigurationModule grassConfigModule;
	public static String tmpDir;
	public static String grassHome;
	public static String pythonHome;
	public static String pythonPath;
	public static String grassModuleStarterHome;
	public static String gisrcDir;
	public static String addonPath;

	public GrassProcessRepository() {
		registeredProcesses = new HashMap<String, ProcessDescription>();
		processesAddonFlagMap = new HashMap<String, Boolean>();
		// check if the repository is active
		
		grassConfigModule = WPSConfig.getInstance().getConfigurationModuleForClass(this.getClass().getName(), ConfigurationCategory.REPOSITORY);
		
		if (grassConfigModule.isActive()) {
			LOGGER.info("Initializing Grass Repository");

			List<? extends ConfigurationEntry<?>> propertyArray = grassConfigModule.getConfigurationEntries();
			
			/*
			 * get properties of Repository
			 *
			 * check whether process is amongst them and active
			 * 
			 * if properties are empty (not initialized yet)
			 * 		add all valid processes to WPSConfig
			 */
			
			for (ConfigurationEntry<?> property : propertyArray) {
				if (property.getKey().equalsIgnoreCase(
						GrassProcessRepositoryCM.tmpDirKey)) {
					tmpDir = property.getValue().toString();
				}
				if (property.getKey().equalsIgnoreCase(
						GrassProcessRepositoryCM.grassHomeKey)) {
					grassHome = property.getValue().toString();
				} else if (property.getKey().equalsIgnoreCase(
						GrassProcessRepositoryCM.moduleStarterHomeKey)) {
					grassModuleStarterHome = property.getValue().toString();
				} else if (property.getKey().equalsIgnoreCase(
						GrassProcessRepositoryCM.pythonHomeKey)) {
					pythonHome = property.getValue().toString();
				} else if (property.getKey().equalsIgnoreCase(
						GrassProcessRepositoryCM.gisrcDirKey)) {
					gisrcDir = property.getValue().toString();
				}else if (property.getKey().equalsIgnoreCase(
						GrassProcessRepositoryCM.addonDirKey)) {
					addonPath = property.getValue().toString();
				}else if (property.getKey().equalsIgnoreCase(
						GrassProcessRepositoryCM.pythonPathKey)) {
					pythonPath = property.getValue().toString();
				}
			}
			
			List<AlgorithmEntry> algorithmEntries = grassConfigModule.getAlgorithmEntries();			
			ArrayList<String> processList = new ArrayList<String>(algorithmEntries.size());
			
			for (AlgorithmEntry algorithmEntry : algorithmEntries) {
				if(algorithmEntry.isActive()){
					processList.add(algorithmEntry.getAlgorithm());
				}
			}

			//TODO check
			HashMap<String, String> variableMap = new HashMap<String, String>();

			variableMap.put(GRASSWPSConfigVariables.TMP_Dir.toString(), tmpDir);
			variableMap.put(GRASSWPSConfigVariables.Grass_Home.toString(),
					grassHome);
			variableMap.put(
					GRASSWPSConfigVariables.ModuleStarter_Home.toString(),
					grassModuleStarterHome);
			variableMap.put(GRASSWPSConfigVariables.Python_Home.toString(),
					pythonHome);
			variableMap.put(GRASSWPSConfigVariables.GISRC_Dir.toString(),
					gisrcDir);
			variableMap.put(GRASSWPSConfigVariables.Python_Path.toString(),
					pythonPath);

			for (String variable : variableMap.keySet()) {
				if (variableMap.get(variable) == null) {
					throw new RuntimeException("Variable " + variable
							+ " not initialized.");
				}
			}			
			
			File tmpDirectory = new File(tmpDir);

			if (tmpDirectory.exists()) {

				File[] filesToDelete = tmpDirectory.listFiles();

				for (File file : filesToDelete) {
					try {

						if (file.isDirectory()) {
							deleteFiles(file);
						} else {
							file.delete();
						}

					} catch (Exception e) {
						/*
						 * ignore
						 */
					}
				}
			}			
			
			// initialize after properties are fetched
			GrassProcessDescriptionCreator creator = new GrassProcessDescriptionCreator();

			File processDirectory = new File(grassHome + fileSeparator + "bin");

			if (processDirectory.isDirectory()) {

				String[] processes = processDirectory.list();

				for (String process : processes) {

					if (process.endsWith(".exe")) {
						process = process.replace(".exe", "");
					}
					if (processList.contains(process)) {

						ProcessDescription pDescType;
						try {
							pDescType = creator
									.createDescribeProcessType(process, false);
							if (pDescType != null) {
								registeredProcesses.put(process, pDescType);
								processesAddonFlagMap.put(process, false);
								LOGGER.info("GRASS process " + process
										+ " added.");
							}
						} catch (Exception e) {
							LOGGER.warn("Could not add Grass process : "
									+ process
									+ ". Errors while creating process description");
							LOGGER.error(e.getMessage(), e);
						}

					} else {
						LOGGER.info("Did not add GRASS process : " + process +". Not in Repository properties or not active.");
					}

				}

			}
			
			if(addonPath != null && !addonPath.equalsIgnoreCase("N/A")){
			
			File addonDirectory = new File(addonPath);

			if (addonDirectory.isDirectory()) {

				String[] processes = addonDirectory.list();

				for (String process : processes) {

					if (process.endsWith(".py")) {
						process = process.replace(".py", "");
					}
					if (process.endsWith(".bat")) {
						process = process.replace(".bat", "");
					}
					if (process.endsWith(".exe")) {
						process = process.replace(".exe", "");
					}
					if (processList.contains(process)) {

						ProcessDescription pDescType;
						try {
							if(registeredProcesses.keySet().contains(process)){
								LOGGER.info("Skipping duplicate process " + process);
								continue;
							}
							pDescType = creator
									.createDescribeProcessType(process, true);
							if (pDescType != null) {
								registeredProcesses.put(process, pDescType);
								processesAddonFlagMap.put(process, true);
								LOGGER.info("GRASS Addon process " + process
										+ " added.");
							}
						} catch (Exception e) {
							LOGGER.warn("Could not add Grass Addon process : "
									+ process
									+ ". Errors while creating process description");
							LOGGER.error(e.getMessage(), e);
						}

					} else {
						LOGGER.info("Did not add GRASS Addon process : " + process +". Not in Repository properties or not active.");
					}

				}

			}
		}

		} else {
			LOGGER.debug("GRASS Algorithm Repository is inactive.");
		}

	}

	public boolean containsAlgorithm(String processID) {
		return getAlgorithmNames().contains(processID);
	}

	public IAlgorithm getAlgorithm(String processID) {
		if (!containsAlgorithm(processID)) {
			throw new RuntimeException("Could not allocate process");
		}
		return new GrassProcessDelegator(processID,
				registeredProcesses.get(processID), processesAddonFlagMap.get(processID));

	}

	public Collection<String> getAlgorithmNames() {

		Collection<String> algorithmNames = new ArrayList<>();

		List<AlgorithmEntry> algorithmEntries = grassConfigModule
				.getAlgorithmEntries();

		for (AlgorithmEntry algorithmEntry : algorithmEntries) {
			if (algorithmEntry.isActive()) {
				algorithmNames.add(algorithmEntry.getAlgorithm());
			}
		}

		return algorithmNames;
	}

	private void deleteFiles(File tmpDirectory) {

		File[] filesToDelete = tmpDirectory.listFiles();

		for (File file : filesToDelete) {
			try {

				if (file.isDirectory()) {
					deleteFiles(file);
				} else {
					file.delete();
				}

			} catch (Exception e) {
				/*
				 * ignore
				 */
			}
		}

		tmpDirectory.delete();

	}

	@Override
	public ProcessDescription getProcessDescription(String processID) {
		if (getAlgorithmNames().contains(processID)) {
			return registeredProcesses.get(processID);
		}
		return null;
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
	}
	
}
