/*
 * Copyright (C) 2007-2018 52°North Initiative for Geospatial Open Source
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
 * An implementation for the {@link LogConfigurationsDAO} interface. This
 * implementation uses {@code JDom} to parse the {@code lobback.xml} file.
 */
@Repository
public class XmlLogConfigurationsDAO implements LogConfigurationsDAO {

    public static final String FILE_NAME = "logback.xml";

    private static final String APPENDER = "appender";

    private static final String ROLLING_POLICY = "rollingPolicy";

    private static final String FILE_NAME_PATTERN = "fileNamePattern";

    private static final String MAX_HISTORY = "maxHistory";

    private static final String ENCODER = "encoder";

    private static final String PATTERN = "pattern";

    private static final String LOGGER_STRING = "logger";

    private static final String NAME = "name";

    private static final String LEVEL = "level";

    private static final String ROOT = "root";

    private static final String APPENDER_REF = "appender-ref";

    private static final String REF = "ref";

    private static final String WPS_FILE = "wpsfile";

    private static final String WPS_CONSOLE = "wpsconsole";

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
        List<Element> appenders = root.getChildren(APPENDER);

        Element fileAppenderFileNamePatternElement =
                appenders.get(0).getChild(ROLLING_POLICY).getChild(FILE_NAME_PATTERN);
        logConfigurations.setWpsfileAppenderFileNamePattern(getValue(fileAppenderFileNamePatternElement));

        Element fileAppenderMaxHistoryElement = appenders.get(0).getChild(ROLLING_POLICY).getChild(MAX_HISTORY);
        logConfigurations.setWpsfileAppenderMaxHistory(Integer.parseInt(getValue(fileAppenderMaxHistoryElement)));

        Element fileAppenderEncoderPatternElement = appenders.get(0).getChild(ENCODER).getChild(PATTERN);
        logConfigurations.setWpsfileAppenderEncoderPattern(getValue(fileAppenderEncoderPatternElement));

        Element consoleAppenderEncoderPatternElement = appenders.get(1).getChild(ENCODER).getChild(PATTERN);
        logConfigurations.setWpsconsoleEncoderPattern(getValue(consoleAppenderEncoderPatternElement));

        @SuppressWarnings("unchecked")
        List<Element> loggersElements = root.getChildren(LOGGER_STRING);
        SortedMap<String, String> loggersMap = new TreeMap<String, String>();

        for (Element element : loggersElements) {
            loggersMap.put(element.getAttributeValue(NAME), element.getAttributeValue(LEVEL));
        }
        logConfigurations.setLoggers(loggersMap);

        Element rootLevelElement = root.getChild(ROOT);
        logConfigurations.setRootLevel(rootLevelElement.getAttributeValue(LEVEL));

        @SuppressWarnings("unchecked")
        List<Element> rootAppenderRefsElements =
                rootLevelElement.getChildren(APPENDER_REF);
        for (Element element : rootAppenderRefsElements) {
            String value = element.getAttributeValue(REF);
            if (value.equals(WPS_FILE)) {
                logConfigurations.setFileAppenderEnabled(true);
            } else if (value.equals(WPS_CONSOLE)) {
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
        List<Element> appenders = root.getChildren(APPENDER);

        Element fileAppenderFileNamePatternElement =
                appenders.get(0).getChild(ROLLING_POLICY).getChild(FILE_NAME_PATTERN);
        setElement(fileAppenderFileNamePatternElement, logConfigurations.getWpsfileAppenderFileNamePattern());

        Element fileAppenderMaxHistoryElement = appenders.get(0).getChild(ROLLING_POLICY).getChild(MAX_HISTORY);
        setElement(fileAppenderMaxHistoryElement, String.valueOf(logConfigurations.getWpsfileAppenderMaxHistory()));

        Element fileAppenderEncoderPatternElement = appenders.get(0).getChild(ENCODER).getChild(PATTERN);
        setElement(fileAppenderEncoderPatternElement, logConfigurations.getWpsfileAppenderEncoderPattern());

        Element consoleAppenderEncoderPatternElement = appenders.get(1).getChild(ENCODER).getChild(PATTERN);
        setElement(consoleAppenderEncoderPatternElement, logConfigurations.getWpsconsoleEncoderPattern());

        root.removeChildren(LOGGER_STRING);
        SortedMap<String, String> loggersMap = logConfigurations.getLoggers();

        if (loggersMap != null) {
            for (Map.Entry<String, String> entry : loggersMap.entrySet()) {
                Element element = new Element(LOGGER_STRING);
                element.setAttribute(NAME, entry.getKey());
                element.setAttribute(LEVEL, entry.getValue());
                root.addContent(element);
            }
        }

        Element rootLevelElement = root.getChild(ROOT);
        rootLevelElement.setAttribute(LEVEL, logConfigurations.getRootLevel());

        rootLevelElement.removeChildren(APPENDER_REF);
        if (logConfigurations.isFileAppenderEnabled()) {
            setAppender(rootLevelElement, WPS_FILE);
        }

        if (logConfigurations.isConsoleAppenderEnabled()) {
            setAppender(rootLevelElement, WPS_CONSOLE);
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

    private void setElement(Element element,
            String value) {
        if (element != null) {
            element.setText(value);
        }
    }

    private void setAppender(Element rootLevelElement,
            String appender) {
        Element appenderElement = new Element(APPENDER_REF);
        appenderElement.setAttribute(REF, appender);
        rootLevelElement.addContent(appenderElement);
    }

}
