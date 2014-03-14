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
