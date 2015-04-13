/**
 * ﻿Copyright (C) 2007 - 2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.n52.wps.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.webapp.api.AlgorithmEntry;
import org.n52.wps.webapp.api.ConfigurationCategory;
import org.n52.wps.webapp.api.ConfigurationModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A static repository to retrieve the available algorithms.
 * 
 * @author foerster
 *
 */
public class LocalAlgorithmRepository implements
		ITransactionalAlgorithmRepository {

	private static Logger LOGGER = LoggerFactory
			.getLogger(LocalAlgorithmRepository.class);
	private Map<String, ProcessDescription> processDescriptionMap;
	private Map<String, IAlgorithm> algorithmMap;
	private ConfigurationModule localAlgorithmRepoConfigModule;

	public LocalAlgorithmRepository() {
		processDescriptionMap = new HashMap<String, ProcessDescription>();
		algorithmMap = new HashMap<String, IAlgorithm>();

		localAlgorithmRepoConfigModule = WPSConfig.getInstance()
				.getConfigurationModuleForClass(this.getClass().getName(),
						ConfigurationCategory.REPOSITORY);

		// check if the repository is active
		if (localAlgorithmRepoConfigModule.isActive()) {

			List<AlgorithmEntry> algorithmEntries = localAlgorithmRepoConfigModule
					.getAlgorithmEntries();

			for (AlgorithmEntry algorithmEntry : algorithmEntries) {
				if (algorithmEntry.isActive()) {
					addAlgorithm(algorithmEntry.getAlgorithm());
				}
			}
		} else {
			LOGGER.debug("Local Algorithm Repository is inactive.");
		}
	}

	public boolean addAlgorithms(String[] algorithms) {
		throw new NotImplementedException();
	}

	public IAlgorithm getAlgorithm(String className) {
		if(getAlgorithmNames().contains(className)){
			return algorithmMap.get(className);
		}
		return null;
	}

	public Collection<String> getAlgorithmNames() {

		Collection<String> algorithmNames = new ArrayList<>();

		List<AlgorithmEntry> algorithmEntries = localAlgorithmRepoConfigModule
				.getAlgorithmEntries();

		for (AlgorithmEntry algorithmEntry : algorithmEntries) {
			if (algorithmEntry.isActive()) {
				algorithmNames.add(algorithmEntry.getAlgorithm());
			}
		}

		return algorithmNames;
	}

	public boolean containsAlgorithm(String className) {
		return getAlgorithmNames().contains(className);
	}

	private IAlgorithm loadAlgorithm(String algorithmClassName)
			throws Exception {
		Class<?> algorithmClass = LocalAlgorithmRepository.class
				.getClassLoader().loadClass(algorithmClassName);
		IAlgorithm algorithm = null;
		if (IAlgorithm.class.isAssignableFrom(algorithmClass)) {
			algorithm = IAlgorithm.class.cast(algorithmClass.newInstance());
		} else if (algorithmClass.isAnnotationPresent(Algorithm.class)) {
			// we have an annotated algorithm that doesn't implement IAlgorithm
			// wrap it in a proxy class
			algorithm = new AbstractAnnotatedAlgorithm.Proxy(algorithmClass);
		} else {
			throw new Exception(
					"Could not load algorithm "
							+ algorithmClassName
							+ " does not implement IAlgorithm or have a Algorithm annotation.");
		}

		boolean isNoProcessDescriptionValid = false;

		for (String supportedVersion : WPSConfig.SUPPORTED_VERSIONS) {
			isNoProcessDescriptionValid = isNoProcessDescriptionValid
					&& !algorithm.processDescriptionIsValid(supportedVersion);
		}

		if (isNoProcessDescriptionValid) {
			LOGGER.warn("Algorithm description is not valid: "
					+ algorithmClassName);// TODO add version to exception/log
			throw new Exception("Could not load algorithm "
					+ algorithmClassName + ". ProcessDescription Not Valid.");
		}

		return algorithm;
	}

	public boolean addAlgorithm(Object processID) {
		if (!(processID instanceof String)) {
			return false;
		}
		String algorithmClassName = (String) processID;

		try {
			
			IAlgorithm algorithm = loadAlgorithm(algorithmClassName);
			
			processDescriptionMap.put(algorithmClassName,
					algorithm.getDescription());
			algorithmMap.put(algorithmClassName, algorithm);
			LOGGER.info("Algorithm class registered: " + algorithmClassName);

			return true;
		} catch (Exception e) {
			LOGGER.error("Exception while trying to add algorithm {}",
					algorithmClassName);
			LOGGER.error(e.getMessage());

		}

		return false;

	}

	public boolean removeAlgorithm(Object processID) {
		throw new NotImplementedException();
	}

	@Override
	public ProcessDescription getProcessDescription(String processID) {
		if (getAlgorithmNames().contains(processID)) {
			return processDescriptionMap.get(processID);
		}
		return null;
	}

	@Override
	public void shutdown() {}

}
