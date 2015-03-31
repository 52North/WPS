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
package org.n52.wps.io.modules.parser;

import java.util.ArrayList;
import java.util.List;

import org.n52.wps.io.datahandler.parser.WCPSQueryParser;
import org.n52.wps.webapp.api.AlgorithmEntry;
import org.n52.wps.webapp.api.ClassKnowingModule;
import org.n52.wps.webapp.api.ConfigurationCategory;
import org.n52.wps.webapp.api.FormatEntry;
import org.n52.wps.webapp.api.types.ConfigurationEntry;

public class WCPSQueryParserCM extends ClassKnowingModule{

	private boolean isActive = true;

	private List<? extends ConfigurationEntry<?>> configurationEntries;
	
	private List<FormatEntry> formatEntries;
	
	public WCPSQueryParserCM(){
		formatEntries = new ArrayList<>();
		configurationEntries = new ArrayList<>();
	}
	
	@Override
	public String getModuleName() {
		return "WCPSQueryParser";
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
		return ConfigurationCategory.PARSER;
	}

	@Override
	public List<? extends ConfigurationEntry<?>> getConfigurationEntries() {
		return configurationEntries;
	}

	@Override
	public List<AlgorithmEntry> getAlgorithmEntries() {
		return null;
	}

	@Override
	public List<FormatEntry> getFormatEntries() {
		return formatEntries;
	}

	@Override
	public String getClassName() {
		return WCPSQueryParser.class.getName();
	}

}
