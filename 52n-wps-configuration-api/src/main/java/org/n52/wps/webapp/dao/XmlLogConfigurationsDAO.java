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
package org.n52.wps.webapp.dao;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jdom.Document;
import org.jdom.Element;
import org.n52.wps.webapp.entities.LogConfigurations;
import org.n52.wps.webapp.util.JDomUtil;
import org.n52.wps.webapp.util.ResourcePathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * An implementation for the {@link LogConfigurationsDAO} interface. This implementation uses {@code JDom} to parse the
 * {@code lobback.xml} file.
 */
@Repository
public class XmlLogConfigurationsDAO implements LogConfigurationsDAO {

	public static String FILE_NAME = "logback.xml";
	private static Logger LOGGER = LoggerFactory.getLogger(XmlLogConfigurationsDAO.class);

	@Autowired
	private JDomUtil jDomUtil;

	@Autowired
	private ResourcePathUtil resourcePathUtil;

	@Override
	public LogConfigurations getLogConfigurations() {
		Document document = null;
		LogConfigurations logConfigurations = new LogConfigurations();
		String absolutePath = resourcePathUtil.getClassPathResourcePath(FILE_NAME);
		document = jDomUtil.parse(absolutePath);
		Element root = document.getRootElement();

		@SuppressWarnings("unchecked")
		List<Element> appenders = root.getChildren("appender");

		Element fileAppenderFileNamePatternElement = appenders.get(0).getChild("rollingPolicy")
				.getChild("fileNamePattern");
		logConfigurations.setWpsfileAppenderFileNamePattern(getValue(fileAppenderFileNamePatternElement));

		Element fileAppenderMaxHistoryElement = appenders.get(0).getChild("rollingPolicy").getChild("maxHistory");
		logConfigurations.setWpsfileAppenderMaxHistory(Integer.parseInt(getValue(fileAppenderMaxHistoryElement)));

		Element fileAppenderEncoderPatternElement = appenders.get(0).getChild("encoder").getChild("pattern");
		logConfigurations.setWpsfileAppenderEncoderPattern(getValue(fileAppenderEncoderPatternElement));

		Element consoleAppenderEncoderPatternElement = appenders.get(1).getChild("encoder").getChild("pattern");
		logConfigurations.setWpsconsoleEncoderPattern(getValue(consoleAppenderEncoderPatternElement));

		@SuppressWarnings("unchecked")
		List<Element> loggersElements = root.getChildren("logger");
		SortedMap<String, String> loggersMap = new TreeMap<String, String>();

		for (Element element : loggersElements) {
			loggersMap.put(element.getAttributeValue("name"), element.getAttributeValue("level"));
		}
		logConfigurations.setLoggers(loggersMap);

		Element rootLevelElement = root.getChild("root");
		logConfigurations.setRootLevel(rootLevelElement.getAttributeValue("level"));

		@SuppressWarnings("unchecked")
		List<Element> rootAppenderRefsElements = rootLevelElement.getChildren("appender-ref");
		for (Element element : rootAppenderRefsElements) {
			String value = element.getAttributeValue("ref");
			if (value.equals("wpsfile")) {
				logConfigurations.setFileAppenderEnabled(true);
			} else if (value.equals("wpsconsole")) {
				logConfigurations.setConsoleAppenderEnabled(true);
			}
		}
		LOGGER.info("'{}' is parsed and a LogConfigurations object is returned", absolutePath);
		return logConfigurations;
	}

	@Override
	public void saveLogConfigurations(LogConfigurations logConfigurations) {
		Document document = null;
		String absolutePath = resourcePathUtil.getClassPathResourcePath(FILE_NAME);
		document = jDomUtil.parse(absolutePath);

		Element root = document.getRootElement();

		@SuppressWarnings("unchecked")
		List<Element> appenders = root.getChildren("appender");

		Element fileAppenderFileNamePatternElement = appenders.get(0).getChild("rollingPolicy")
				.getChild("fileNamePattern");
		setElement(fileAppenderFileNamePatternElement, logConfigurations.getWpsfileAppenderFileNamePattern());

		Element fileAppenderMaxHistoryElement = appenders.get(0).getChild("rollingPolicy").getChild("maxHistory");
		setElement(fileAppenderMaxHistoryElement, String.valueOf(logConfigurations.getWpsfileAppenderMaxHistory()));

		Element fileAppenderEncoderPatternElement = appenders.get(0).getChild("encoder").getChild("pattern");
		setElement(fileAppenderEncoderPatternElement, logConfigurations.getWpsfileAppenderEncoderPattern());

		Element consoleAppenderEncoderPatternElement = appenders.get(1).getChild("encoder").getChild("pattern");
		setElement(consoleAppenderEncoderPatternElement, logConfigurations.getWpsconsoleEncoderPattern());

		root.removeChildren("logger");
		SortedMap<String, String> loggersMap = logConfigurations.getLoggers();

		if (loggersMap != null) {
			for (Map.Entry<String, String> entry : loggersMap.entrySet()) {
				Element element = new Element("logger");
				element.setAttribute("name", entry.getKey());
				element.setAttribute("level", entry.getValue());
				root.addContent(element);
			}
		}

		Element rootLevelElement = root.getChild("root");
		rootLevelElement.setAttribute("level", logConfigurations.getRootLevel());

		rootLevelElement.removeChildren("appender-ref");
		if (logConfigurations.isFileAppenderEnabled()) {
			setAppender(rootLevelElement, "wpsfile");
		}
		
		if (logConfigurations.isConsoleAppenderEnabled()) {
			setAppender(rootLevelElement, "wpsconsole");
		}
		jDomUtil.write(document, absolutePath);
		LOGGER.info("LogConfigurations values written to '{}'", absolutePath);
	}
	
	private String getValue(Element element) {
		if (element != null) {
			return element.getValue();
		}
		return null;
	}

	private void setElement(Element element, String value) {
		if (element != null) {
			element.setText(value);
		}
	}
	
	private void setAppender(Element rootLevelElement, String appender) {
		Element appenderElement = new Element("appender-ref");
		appenderElement.setAttribute("ref", appender);
		rootLevelElement.addContent(appenderElement);
	}

}
