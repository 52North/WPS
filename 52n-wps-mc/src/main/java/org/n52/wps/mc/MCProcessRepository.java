/**
 * ﻿Copyright (C) 2012 - 2014 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.mc;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.n52.movingcode.runtime.GlobalRepositoryManager;
import org.n52.movingcode.runtime.codepackage.MovingCodePackage;
import org.n52.movingcode.runtime.coderepository.MovingCodeRepository;
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
	private static final String REPO_FEED_REPO_PARAM = "REMOTE_REPOSITORY";
	private static final String LOCAL_ZIP_REPO_PARAM = "LOCAL_REPOSITORY";
	private static final String CACHED_REMOTE_REPO_PARAM = "CACHED_REMOTE_REPOSITORY";

	// use the GlobalRepoManager from mc-runtime for the process inventory
	private GlobalRepositoryManager rm = GlobalRepositoryManager.getInstance();

	// valid functionIDs
	// needs to be volatile since this field may be updated by #updateContent()
	private volatile Collection<String> supportedFunctionIDs = Collections.emptyList();

	private static Logger LOGGER = LoggerFactory.getLogger(MCProcessRepository.class);

	public MCProcessRepository() {
		super();

		// register a change listener with the GlobalRepositoryManager
		// to listen for content updates
		rm.addRepositoryChangeListener(new RepositoryChangeListener() {

			@Override
			public void onRepositoryUpdate(MovingCodeRepository updatedRepo) {
				// trigger a content update of this repo
				updateContent();
				// and notify the WPS framework to trigger a Capabilities update
				LOGGER.info("Moving Code repository content has changed. Capabilities update required.");
				WPSConfig.getInstance().firePropertyChange(WPSConfig.WPSCAPABILITIES_SKELETON_PROPERTY_EVENT_NAME);
			}
		});

		// check if the repository is active
		if (WPSConfig.getInstance().isRepositoryActive(this.getClass().getCanonicalName())) {

			// trigger remote repo init in separate thread
			Thread tLoadRemote = new LoadRepoThread();
			tLoadRemote.start();
		}
		else {
			LOGGER.debug("MCProcessRepository is inactive.");
		}
	}

	@Override
	public Collection<String> getAlgorithmNames() {
		return supportedFunctionIDs;
	}

	private synchronized void updateContent(){

		// run this block if validFunctionIDs are not yet available
		// checks which available functions can be executed with current configuration

		// 1. get all available functionIDs
		String[] fids = rm.getFunctionIDs();
		ArrayList<String> exFIDs = new ArrayList<String>();

		// 2. for each function ID
		for (String currentFID : fids) {
			// 2.a retrieve implementing packages
			MovingCodePackage[] mcps = rm.getPackageByFunction(currentFID);
			// 2.b check whether each one of them can be executed
			//     by the current processor configuration
			for (MovingCodePackage currentMCP : mcps) {
				boolean supported = ProcessorFactory.getInstance().supportsPackage(currentMCP);
				if (supported) {
					exFIDs.add(currentFID);
					break;
				}
			}
		}

		supportedFunctionIDs = exFIDs;
	}

	@Override
	public IAlgorithm getAlgorithm(String processID) {
		return new MCProcessDelegator(processID);
	}

	@Override
	public ProcessDescriptionType getProcessDescription(String processID) {
		return filterProcessDescription(rm.getProcessDescription(processID));
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

	static ProcessDescriptionType filterProcessDescription(ProcessDescriptionType description){
		description.setStatusSupported(true);
		description.setStoreSupported(true);
		return description;
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
						LOGGER.info("Added MovingCode Repository: " + property.getName() + " - "
								+ property.getStringValue());
					}
					catch (MalformedURLException e) {
						LOGGER.warn("MovingCode Repository is not a valid URL: " + property.getName() + " - "
								+ property.getStringValue());
					}
					catch (Exception e) {
						// catch any unexpected error; if we get here this is probably an indication for a
						// bug/flaw in mc-runtime ...
						LOGGER.error("Error invoking MovingCode Runtime for feed URL : " + property.getName() + " - "
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
						LOGGER.info("Added MovingCode Repository: " + property.getName() + " - "
								+ property.getStringValue());
					}
					catch (Exception e) {
						// catch any unexpected error; if we get here this is probably an indication for a
						// bug/flaw in mc-runtime ...
						LOGGER.error("Error invoking MovingCode Runtime for feed URL : " + property.getName() + " - "
								+ property.getStringValue());
						e.printStackTrace();
					}

				}
			}


			LOGGER.info("The following repositories have been loaded:\n{}", Arrays.toString(rm.getRegisteredRepositories()));
		}
	}

}