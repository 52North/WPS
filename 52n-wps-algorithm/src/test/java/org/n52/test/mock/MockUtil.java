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
package org.n52.test.mock;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlException;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.GeneratorFactory;
import org.n52.wps.io.IGenerator;
import org.n52.wps.io.IParser;
import org.n52.wps.io.ParserFactory;
import org.n52.wps.webapp.api.types.ConfigurationEntry;

/**
 *
 * @author tkunicki
 */
public class MockUtil {

    public final static String SUPPORTED_SCHEMA = "supportedSchema";
    public final static String SUPPORTED_FORMAT = "supportedFormat";
    public final static String SUPPORTED_ENCODING = "supportedEncoding";


    private static WPSConfig MOCK_CONFIG;
    public synchronized static WPSConfig getMockConfig() throws XmlException, IOException {
        if (MOCK_CONFIG == null) {
            InputStream configInputStream = null;
            try {
                configInputStream = MockUtil.class.getResourceAsStream(
                        "/org/n52/test/mock/wps_config.xml");
                WPSConfig.forceInitialization(configInputStream);
                MOCK_CONFIG = WPSConfig.getInstance();
                ParserFactory.initialize(MOCK_CONFIG.getActiveRegisteredParserModules());
                GeneratorFactory.initialize(MOCK_CONFIG.getActiveRegisteredGeneratorModules());
            } finally {
                IOUtils.closeQuietly(configInputStream);
            }
        }
        return MOCK_CONFIG;
    }

    public static Collection<String> getParserSupportedSchemas(Class<? extends IParser> clazz) {
        return getParserPropertyValues(clazz, SUPPORTED_SCHEMA);
    }

    public static Collection<String> getParserSupportedFormats(Class<? extends IParser> clazz) {
        return getParserPropertyValues(clazz, SUPPORTED_FORMAT);
    }

    public static Collection<String> getParserSupportedEncodings(Class<? extends IParser> clazz) {
        return getParserPropertyValues(clazz, SUPPORTED_ENCODING);
    }

    public static Collection<String> getGeneratorSupportedSchemas(Class<? extends IGenerator> clazz) {
        return getGeneratorPropertyValues(clazz, SUPPORTED_SCHEMA);
    }

    public static Collection<String> getGeneratorSupportedFormats(Class<? extends IGenerator> clazz) {
        return getGeneratorPropertyValues(clazz, SUPPORTED_FORMAT);
    }

    public static Collection<String> getGeneratorSupportedEncodings(Class<? extends IGenerator> clazz) {
        return getGeneratorPropertyValues(clazz, SUPPORTED_ENCODING);
    }

    public static Collection<String> getParserPropertyValues(Class<? extends IParser> clazz, String propertyName) {
        String clazzName = clazz.getName();
        ArrayList<String> propertyList = new ArrayList<String>();
        try {
            WPSConfig mockConfig = MockUtil.getMockConfig();
            List<? extends ConfigurationEntry<?>> properties =
                    mockConfig.getConfigurationEntriesForParserClass(clazzName);
            for (ConfigurationEntry<?> property : properties) {
                if (propertyName.equals(property.getKey())) {
                    propertyList.add(property.getValue().toString());
                }
            }
            propertyList.trimToSize();
        } catch (Exception e) {
            System.err.println("ERROR parsing property " + propertyName + " for Parser class " + clazzName);
        }
        return propertyList;
    }

    public static Collection<String> getGeneratorPropertyValues(Class<? extends IGenerator> clazz, String propertyName) {
        String clazzName = clazz.getName();
        ArrayList<String> propertyList = new ArrayList<String>();
        try {
            WPSConfig mockConfig = MockUtil.getMockConfig();
            List<? extends ConfigurationEntry<?>> properties =
                    mockConfig.getConfigurationEntriesForParserClass(clazzName);
            for (ConfigurationEntry<?> property : properties) {
                if (propertyName.equals(property.getKey())) {
                    propertyList.add(property.getValue().toString());
                }
            }
        } catch (Exception e) {
            System.err.println("ERROR parsing property " + propertyName + " for Generator class " + clazzName);
        }
        propertyList.trimToSize();
        return propertyList;
    }

}
