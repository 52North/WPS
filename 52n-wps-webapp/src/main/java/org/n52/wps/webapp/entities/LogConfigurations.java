/**
 * Copyright (C) 2013
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
