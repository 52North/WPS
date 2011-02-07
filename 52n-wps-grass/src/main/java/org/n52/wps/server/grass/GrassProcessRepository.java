/***************************************************************
Copyright © 2009 52∞North Initiative for Geospatial Open Source Software GmbH

 Author: Benjamin Proﬂ, 52∞North

 Contact: Andreas Wytzisk, 
 52∞North Initiative for Geospatial Open Source SoftwareGmbH, 
 Martin-Luther-King-Weg 24,
 48155 Muenster, Germany, 
 info@52north.org

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 version 2 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; even without the implied WARRANTY OF
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program (see gnu-gpl v2.txt). If not, write to
 the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA 02111-1307, USA or visit the Free
 Software Foundationís web page, http://www.fsf.org.

 ***************************************************************/
package org.n52.wps.server.grass;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.log4j.Logger;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.IAlgorithmRepository;
import org.n52.wps.server.grass.util.GRASSWPSConfigVariables;
import org.n52.wps.server.request.ExecuteRequest;

public class GrassProcessRepository implements IAlgorithmRepository {

	private static Logger LOGGER = Logger
			.getLogger(GrassProcessRepository.class);
	private Map<String, ProcessDescriptionType> registeredProcesses;
	private Map<String, Boolean> processesAddonFlagMap;
	private final String fileSeparator = System.getProperty("file.separator");
	public static String tmpDir;
	public static String grassHome;
	public static String pythonHome;
	public static String pythonPath;
	public static String grassModuleStarterHome;
	public static String gisrcDir;
	public static String addonPath;

	public GrassProcessRepository() {
		registeredProcesses = new HashMap<String, ProcessDescriptionType>();
		processesAddonFlagMap = new HashMap<String, Boolean>();
		// check if the repository is active
		if (WPSConfig.getInstance().isRepositoryActive(
				this.getClass().getCanonicalName())) {
			LOGGER.info("Initializing Grass Repository");

			String dontUseProcesses = "";

			Property[] propertyArray = WPSConfig.getInstance()
					.getPropertiesForRepositoryClass(
							this.getClass().getCanonicalName());
			for (Property property : propertyArray) {
				if (property.getName().equalsIgnoreCase(
						GRASSWPSConfigVariables.DONT_USE_PROCESSES.toString())) {
					dontUseProcesses = property.getStringValue();
				} else if (property.getName().equalsIgnoreCase(
						GRASSWPSConfigVariables.TMP_Dir.toString())) {
					tmpDir = property.getStringValue();
				}
				if (property.getName().equalsIgnoreCase(
						GRASSWPSConfigVariables.Grass_Home.toString())) {
					grassHome = property.getStringValue();
				} else if (property.getName().equalsIgnoreCase(
						GRASSWPSConfigVariables.ModuleStarter_Home.toString())) {
					grassModuleStarterHome = property.getStringValue();
				} else if (property.getName().equalsIgnoreCase(
						GRASSWPSConfigVariables.Python_Home.toString())) {
					pythonHome = property.getStringValue();
				} else if (property.getName().equalsIgnoreCase(
						GRASSWPSConfigVariables.GISRC_Dir.toString())) {
					gisrcDir = property.getStringValue();
				}else if (property.getName().equalsIgnoreCase(
						GRASSWPSConfigVariables.Addon_Dir.toString())) {
					addonPath = property.getStringValue();
				}else if (property.getName().equalsIgnoreCase(
						GRASSWPSConfigVariables.Python_Path.toString())) {
					pythonPath = property.getStringValue();
				}
			}

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

			try {
				
				File n52appDataDir = new File(System.getenv("APPDATA") + fileSeparator + "52nWPS");
				
				if(!n52appDataDir.exists()){
					n52appDataDir.mkdir();
				}
				
				BufferedWriter bwrite = new BufferedWriter(new FileWriter(n52appDataDir + fileSeparator + "config.txt"));
				
				bwrite.write("Python_Home = " + pythonHome + "\n");
				bwrite.write("Addon_Dir = " + addonPath);
				
				bwrite.flush();
				bwrite.close();
				
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			
			// initialize after properties are fetched
			GrassProcessDescriptionCreator creator = new GrassProcessDescriptionCreator();

			File processDirectory = new File(grassHome + fileSeparator + "bin");

//			int count = 0;

			if (processDirectory.isDirectory()) {

				String[] processes = processDirectory.list();

				for (String process : processes) {

					if (process.endsWith(".exe")) {
						process = process.replace(".exe", "");
					}
					if (!dontUseProcesses.contains(process)) {

						ProcessDescriptionType pDescType;
						try {
							pDescType = creator
									.createDescribeProcessType(process, false);
							if (pDescType != null) {
								registeredProcesses.put(process, pDescType);
								processesAddonFlagMap.put(process, false);
								LOGGER.info("GRASS Process " + process
										+ " added.");
							}
						} catch (Exception e) {
							LOGGER.warn("Could not add Grass Process : "
									+ process
									+ ". Errors while creating describe Process");
							LOGGER.error(e);
						}
//						count++;
//						System.out.println("#########################  "
//								+ count);

					} else {
						dontUseProcesses = dontUseProcesses.replace(process
								+ ";", "");
					}

				}

			}
			
			if(addonPath != null){
			
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
					if (!dontUseProcesses.contains(process)) {

						ProcessDescriptionType pDescType;
						try {
							if(registeredProcesses.keySet().contains(process)){
								LOGGER.info("Skipping duplicate Process " + process);
								continue;
							}
							pDescType = creator
									.createDescribeProcessType(process, true);
							if (pDescType != null) {
								registeredProcesses.put(process, pDescType);
								processesAddonFlagMap.put(process, true);
								LOGGER.info("GRASS Addon Process " + process
										+ " added.");
							}
						} catch (Exception e) {
							LOGGER.warn("Could not add Grass Addon Process : "
									+ process
									+ ". Errors while creating describe Process");
							LOGGER.error(e);
						}

					} else {
						dontUseProcesses = dontUseProcesses.replace(process
								+ ";", "");
					}

				}

			}
		}
			
			

		} else {
			LOGGER.debug("Local Algorithm Repository is inactive.");
		}

	}

	public boolean containsAlgorithm(String processID) {
		if (registeredProcesses.containsKey(processID)) {
			return true;
		}
		LOGGER.warn("Could not find Grass Process " + processID, null);
		return false;
	}

	public IAlgorithm getAlgorithm(String processID,
			ExecuteRequest executeRequest) {
		if (!containsAlgorithm(processID)) {
			throw new RuntimeException("Could not allocate Process");
		}
		return new GrassProcessDelegator(processID,
				registeredProcesses.get(processID), executeRequest, processesAddonFlagMap.get(processID));

	}

	public Collection<String> getAlgorithmNames() {
		return registeredProcesses.keySet();
	}

	public Collection<IAlgorithm> getAlgorithms() {
		Collection<IAlgorithm> algorithms = new ArrayList<IAlgorithm>(
				registeredProcesses.size());
		for (String processID : registeredProcesses.keySet()) {
			IAlgorithm algorithm = getAlgorithm(processID, null);
			if (algorithm != null) {
				algorithms.add(algorithm);
			}
		}
		return algorithms;
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

}
