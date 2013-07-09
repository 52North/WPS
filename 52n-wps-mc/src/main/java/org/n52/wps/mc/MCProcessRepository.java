/**
 * ﻿Copyright (C) 2012
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

package org.n52.wps.mc;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.n52.movingcode.runtime.GlobalRepositoryManager;
import org.n52.movingcode.runtime.ProcessorConfig;
import org.n52.movingcode.runtime.codepackage.MovingCodePackage;
import org.n52.movingcode.runtime.coderepository.IMovingCodeRepository;
import org.n52.movingcode.runtime.coderepository.RepositoryChangeListener;
import org.n52.movingcode.runtime.processors.ProcessorFactory;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.IAlgorithmRepository;

/**
 * This class implements an {@link IAlgorithmRepository} for MovingCode Packages. This repository maintains a
 * {@link GlobalRepositoryManager} internally and fires notification events to {@link WPSConfig}.
 * 
 * This class will also load the basic configuration for the MC runtime subsystem (Available Processors,
 * Repository URLs and Folders)
 * 
 * @author Matthias Mueller, TU Dresden
 * 
 */
public class MCProcessRepository implements IAlgorithmRepository {

	// static string definitions
	private static final String CONFIG_FILE_NAME = "processors.xml";
	private static final String REPO_FEED_REPO_PARAM = "REMOTE_REPOSITORY";
	private static final String LOCAL_ZIP_REPO_PARAM = "LOCAL_REPOSITORY";
	private static final String CACHED_REMOTE_REPO_PARAM = "CACHED_REMOTE_REPOSITORY";

	// use the GlobalRepoManager from mc-runtime for the process inventory
	private GlobalRepositoryManager rm = GlobalRepositoryManager.getInstance();

	// valid functionIDs
	private String[] validFunctionIDs = null;

	private static Logger logger = LoggerFactory.getLogger(MCProcessRepository.class);

	public MCProcessRepository() {
		super();

		// check if the repository is active
		if (WPSConfig.getInstance().isRepositoryActive(this.getClass().getCanonicalName())) {
			// configure the runtime
			configureMCRuntime();
			
			// trigger remote repo init in separate thread
			Thread tLoadRemote = new LoadRepoThread();
			tLoadRemote.start();
		}
		else {
			logger.debug("MCProcessRepository is inactive.");
		}
	}

	@Override
	public Collection<String> getAlgorithmNames() {

		// run this block if validFunctionIDs are not yet available
		// checks which available functions can be executed with current configuration
		if (validFunctionIDs == null) {
			// 1. get all available functionIDs
			String[] fids = rm.getFunctionIDs();
			ArrayList<String> exFIDs = new ArrayList<String>();

			// 2. for each function ID
			for (String currentFID : fids) {
				// 2.a retrieve implementing packages
				MovingCodePackage[] mcps = rm.getPackageByFunction(currentFID);
				// 2.b check whether one of them can be executed
				for (MovingCodePackage currentMCP : mcps) {
					boolean supported = ProcessorFactory.getInstance().supportsPackage(currentMCP);
					if (supported) {
						exFIDs.add(currentFID);
						break;
					}
				}
			}

			validFunctionIDs = exFIDs.toArray(new String[exFIDs.size()]);
		}

		return Arrays.asList(validFunctionIDs);
	}

	@Override
	public IAlgorithm getAlgorithm(String processID) {
		return new MCProcessDelegator(processID);
	}

	@Override
	public ProcessDescriptionType getProcessDescription(String processID) {
		return rm.getProcessDescription(processID);
	}

	@Override
	public boolean containsAlgorithm(String processID) {
		return rm.providesFunction(processID);
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		// we probably do not need any logic here
	}

	// ----------------------------------------------------------------
	// methods and logic for processor configuration
	private static void configureMCRuntime() {
		String configFilePath = WPSConfig.getConfigDir() + CONFIG_FILE_NAME;
		File configFile = new File(configFilePath);
		boolean loaded = ProcessorConfig.getInstance().setConfig(configFile);
		if ( !loaded) {
			logger.error("Could not load processor configuration from " + configFilePath);
		}
	}

	/**
	 * 
	 * @author Matthias Mueller
	 *
	 */
	private final class LoadRepoThread extends Thread {

		@Override
		public void run() {

			// get properties to find out which remote repositories we shall invoke
			Property[] propertyArray = WPSConfig.getInstance().getPropertiesForRepositoryClass(MCProcessRepository.class.getCanonicalName());

			// for each remote repository: add to RepoManager
			for (Property property : propertyArray) {
				if (property.getName().equalsIgnoreCase(REPO_FEED_REPO_PARAM) && property.getActive()) {
					// convert to URL, check and register
					try {
						URL repoURL = new URL(property.getStringValue());
						rm.addRepository(repoURL);
						logger.info("Added MovingCode Repository: " + property.getName() + " - "
								+ property.getStringValue());
					}
					catch (MalformedURLException e) {
						logger.warn("MovingCode Repository is not a valid URL: " + property.getName() + " - "
								+ property.getStringValue());
					}
					catch (Exception e) {
						// catch any unexpected error; if we get here this is probably an indication for a
						// bug/flaw in mc-runtime ...
						logger.error("Error invoking MovingCode Runtime for feed URL : " + property.getName() + " - "
								+ property.getStringValue());
					}

				}
			}

			// for each remote repository: add to RepoManager
			for (Property property : propertyArray) {
				if (property.getName().equalsIgnoreCase(LOCAL_ZIP_REPO_PARAM) && property.getActive()) {
					// identify Folder, check and register
					try {
						String repoFolder = property.getStringValue();
						rm.addLocalZipPackageRepository(repoFolder);
						logger.info("Added MovingCode Repository: " + property.getName() + " - "
								+ property.getStringValue());
					}
					catch (Exception e) {
						// catch any unexpected error; if we get here this is probably an indication for a
						// bug/flaw in mc-runtime ...
						logger.error("Error invoking MovingCode Runtime for feed URL : " + property.getName() + " - "
								+ property.getStringValue());
					}

				}
			}

			// add a change listener to the GlobalRepositoryManager rm
			rm.addRepositoryChangeListener(new RepositoryChangeListener() {
				@Override
				public void onRepositoryUpdate(IMovingCodeRepository updatedRepo) {
					// clear validFunctionIDs
					validFunctionIDs = null;

					// trigger Capabilities update
					logger.info("Moving Code repository content has changed. Capabilities update required.");
					WPSConfig.getInstance().firePropertyChange(WPSConfig.WPSCAPABILITIES_SKELETON_PROPERTY_EVENT_NAME);
				}
			});
			
			// trigger Capabilities update
			logger.info("Moving Code repositories have been loaded. Capabilities update required.");
			WPSConfig.getInstance().firePropertyChange(WPSConfig.WPSCAPABILITIES_SKELETON_PROPERTY_EVENT_NAME);

// 			-- old code --			
//			try {
//				CapabilitiesConfiguration.reloadSkeleton();
//			} catch (XmlException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
		}
	}

}