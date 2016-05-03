/**
 * ﻿Copyright (C) 2007 - 2016 52°North Initiative for Geospatial Open Source
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.opengis.wps.x100.ProcessDescriptionType;

public class ServiceLoaderAlgorithmRepository implements IAlgorithmRepository {

	private static final Logger logger = LoggerFactory.getLogger(ServiceLoaderAlgorithmRepository.class);
	private Map<String, Class<? extends IAlgorithm>> currentAlgorithms;

	public ServiceLoaderAlgorithmRepository() {
		this.currentAlgorithms = loadAlgorithms();
	}
	
	private Map<String, Class<? extends IAlgorithm>> loadAlgorithms() {
		Map<String, Class<? extends IAlgorithm>> result = new HashMap<String, Class<? extends IAlgorithm>>();
		ServiceLoader<IAlgorithm> loader = ServiceLoader.load(IAlgorithm.class);
		
		for (IAlgorithm ia : loader) {
			logger.debug("Adding algorithm with identifier {} and class {}",
					ia.getWellKnownName(), ia.getClass().getCanonicalName());
			result.put(ia.getWellKnownName(), ia.getClass());
		}
		
		return result;
	}
	
	@Override
	public Collection<String> getAlgorithmNames() {
		return this.currentAlgorithms.keySet();
	}

	@Override
	public IAlgorithm getAlgorithm(String processID) {
		Class<? extends IAlgorithm> clazz = this.currentAlgorithms.get(processID);
		if (clazz != null) {
			try {
				return clazz.newInstance();
			} catch (InstantiationException e) {
				logger.warn(e.getMessage(), e);
			} catch (IllegalAccessException e) {
				logger.warn(e.getMessage(), e);
			}
		}
		return null;
	}

	@Override
	public ProcessDescriptionType getProcessDescription(String processID) {
		IAlgorithm algo = getAlgorithm(processID);
		if (algo != null) {
			return algo.getDescription();
		}
		return null;
	}

	@Override
	public boolean containsAlgorithm(String processID) {
		return this.currentAlgorithms.containsKey(processID);
	}

	@Override
	public void shutdown() {
	}


}
