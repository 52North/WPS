/**
 * ﻿Copyright (C) 2010
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
