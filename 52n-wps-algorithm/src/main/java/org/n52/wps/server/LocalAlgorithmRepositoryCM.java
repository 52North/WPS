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
import java.util.List;

import org.n52.wps.webapp.api.AlgorithmEntry;
import org.n52.wps.webapp.api.ConfigurationCategory;
import org.n52.wps.webapp.api.ConfigurationModule;
import org.n52.wps.webapp.api.FormatEntry;
import org.n52.wps.webapp.api.types.ConfigurationEntry;

public class LocalAlgorithmRepositoryCM implements ConfigurationModule{

	private boolean isActive = true;

//	private AlgorithmEntry algorithmEntry = new AlgorithmEntry("org.n52.wps.server.algorithm.JTSConvexHullAlgorithm", true);
//	private AlgorithmEntry algorithmEntry1 = new AlgorithmEntry("org.n52.wps.server.algorithm.test.DummyTestClass", true);
//	private AlgorithmEntry algorithmEntry2 = new AlgorithmEntry("org.n52.wps.server.algorithm.test.LongRunningDummyTestClass", true);
//	private AlgorithmEntry algorithmEntry3 = new AlgorithmEntry("org.n52.wps.server.algorithm.test.MultipleComplexInAndOutputsDummyTestClass", true);
//	private AlgorithmEntry algorithmEntry4 = new AlgorithmEntry("org.n52.wps.server.algorithm.test.MultiReferenceInputAlgorithm", true);
//	private AlgorithmEntry algorithmEntry5 = new AlgorithmEntry("org.n52.wps.server.algorithm.test.MultiReferenceBinaryInputAlgorithm", true);
//	private AlgorithmEntry algorithmEntry6 = new AlgorithmEntry("org.n52.wps.server.algorithm.test.EchoProcess", true);

	private List<AlgorithmEntry> algorithmEntries;

	private List<? extends ConfigurationEntry<?>> configurationEntries = new ArrayList<>();
	
	public LocalAlgorithmRepositoryCM() {
		algorithmEntries = new ArrayList<>();
//		algorithmEntries.addAll(Arrays.asList(algorithmEntry, algorithmEntry1, algorithmEntry2, algorithmEntry3, algorithmEntry4, algorithmEntry5, algorithmEntry6));
	}
	
	@Override
	public String getModuleName() {
		return "LocalAlgorithmRepository Configuration Module";
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
		// TODO Auto-generated method stub
		return null;
	}

}
