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
package org.n52.wps.webapp.entities;

import java.util.SortedMap;
import java.util.TreeMap;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

/**
 * Holds parsed log configuration values.
 * 
 * @see LogConfigurationsService
 * @see LogConfigurationsDAO
 */
public class LogConfigurations {
	@NotBlank(message = "File  name pattern cannot be empty.")
	private String wpsfileAppenderFileNamePattern;

	@Digits(integer = 10, fraction = 0, message = "Max history must be an integer.")
	@NotNull(message = "Max history cannot be empty.")
	@Min(value = 1, message = "Minimum value is 1.")
	private int wpsfileAppenderMaxHistory;

	@NotBlank(message = "File encoder pattern cannot be empty.")
	private String wpsfileAppenderEncoderPattern;

	@NotBlank(message = "Console encoder pattern cannot be empty.")
	private String wpsconsoleEncoderPattern;

	private SortedMap<String, String> loggers = new TreeMap<String, String>();
	private String rootLevel;
	private boolean fileAppenderEnabled;
	private boolean consoleAppenderEnabled;

	public String getWpsfileAppenderFileNamePattern() {
		return wpsfileAppenderFileNamePattern;
	}

	public void setWpsfileAppenderFileNamePattern(String wpsfileAppenderFileNamePattern) {
		this.wpsfileAppenderFileNamePattern = wpsfileAppenderFileNamePattern;
	}

	public int getWpsfileAppenderMaxHistory() {
		return wpsfileAppenderMaxHistory;
	}

	public void setWpsfileAppenderMaxHistory(int wpsfileAppenderMaxHistory) {
		this.wpsfileAppenderMaxHistory = wpsfileAppenderMaxHistory;
	}

	public String getWpsfileAppenderEncoderPattern() {
		return wpsfileAppenderEncoderPattern;
	}

	public void setWpsfileAppenderEncoderPattern(String wpsfileAppenderEncoderPattern) {
		this.wpsfileAppenderEncoderPattern = wpsfileAppenderEncoderPattern;
	}

	public String getWpsconsoleEncoderPattern() {
		return wpsconsoleEncoderPattern;
	}

	public void setWpsconsoleEncoderPattern(String wpsconsoleEncoderPattern) {
		this.wpsconsoleEncoderPattern = wpsconsoleEncoderPattern;
	}

	public SortedMap<String, String> getLoggers() {
		return loggers;
	}

	public void setLoggers(SortedMap<String, String> loggers) {
		this.loggers = loggers;
	}

	public String getRootLevel() {
		return rootLevel;
	}

	public void setRootLevel(String rootLevel) {
		this.rootLevel = rootLevel;
	}

	public boolean isFileAppenderEnabled() {
		return fileAppenderEnabled;
	}

	public void setFileAppenderEnabled(boolean fileAppenderEnabled) {
		this.fileAppenderEnabled = fileAppenderEnabled;
	}

	public boolean isConsoleAppenderEnabled() {
		return consoleAppenderEnabled;
	}

	public void setConsoleAppenderEnabled(boolean consoleAppenderEnabled) {
		this.consoleAppenderEnabled = consoleAppenderEnabled;
	}

}
