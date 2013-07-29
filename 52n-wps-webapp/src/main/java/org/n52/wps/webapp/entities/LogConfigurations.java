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

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class LogConfigurations {
	private String wpsfileAppenderFileNamePattern;
	private String wpsfileAppenderMaxHistory;
	private String wpsfileAppenderEncoderPattern;
	private String wpsconsoleEncoderPattern;
	private SortedMap<String, String> loggers = new TreeMap<String, String>();
	private String rootLevel;
	private List<String> rootAppenderRefs;

	public enum Level {
		OFF, INFO, DEBUG, ERROR
	}

	public String getWpsfileAppenderFileNamePattern() {
		return wpsfileAppenderFileNamePattern;
	}

	public void setWpsfileAppenderFileNamePattern(String wpsfileAppenderFileNamePattern) {
		this.wpsfileAppenderFileNamePattern = wpsfileAppenderFileNamePattern;
	}

	public String getWpsfileAppenderMaxHistory() {
		return wpsfileAppenderMaxHistory;
	}

	public void setWpsfileAppenderMaxHistory(String wpsfileAppenderMaxHistory) {
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

	public List<String> getRootAppenderRefs() {
		return rootAppenderRefs;
	}

	public void setRootAppenderRefs(List<String> rootAppenderRefs) {
		this.rootAppenderRefs = rootAppenderRefs;
	}

}
