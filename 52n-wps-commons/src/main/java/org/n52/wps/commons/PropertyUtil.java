/*
 * Copyright (C) 2006-2018 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.commons;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.Period;
import org.n52.wps.webapp.api.ConfigurationManager;
import org.n52.wps.webapp.api.ConfigurationModule;
import org.n52.wps.webapp.api.types.ConfigurationEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

/**
 *
 * @author tkunicki
 */
public class PropertyUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyUtil.class);

    private static final Joiner JOINER = Joiner.on(".");

    private static final String MESSAGE_ISO_PERIOD_PROPERTY =
            "Config property for \"{}\" exists but unable to parse \"{}\" as ISO8601 period";

    private static final String MESSAGE_ISO_PERIOD_MODULE =
            "ConfigurationModule entry for \"{}\" exists but unable to parse \"{}\" as ISO8601 period";

    private static final String SYSTEM_PROPERTY_EXISTS = "System property \"{}\" exists, using value of: {} ({}) ";

    private static final String SYSTEM_PROPERTY_EXISTS_INVALID =
            "System property \"{}\" exists, but value of \"{}\" is invalid";

    private static final String SYSTEM_PROPERTY_NOT_PRESENT = "System property \"{}\" not present";

    private static final String SYSTEM_PROPERTY_NOT_PRESENT_SKIPPING_LOOKUP =
            "System property root not present, skipping system property lookup for {}";

    private static final String CONFIG_PROPERTY_EXISTS = "Config property \"{}\" exists, using value of: {} ({}) ";

    private static final String CONFIG_PROPERTY_EXISTS_INVALID =
            "Config property \"{}\" exists but value is null, ignoring";

    private static final String CONFIG_PROPERTY_NOT_PRESENT = "Config property \"{}\" not present";

    private static final String USING_DEFAULT_VALUE = "Using default value for \"{}\" of {}";

    private static final String SYSTEM_PROPERTY_EXISTS_ISO =
            "System property \"{}\" exists but unable to parse \"{}\" as ISO8601 period";

    private final String systemPropertyRoot;

    private final Map<String, ConfigurationEntry<?>> propertyNameMap;

    private ConfigurationModule configurationModule;

    private ConfigurationManager configurationManager;

    public PropertyUtil(ConfigurationModule configurationModule) {
        this.configurationModule = configurationModule;
        configurationManager = WPSConfig.getInstance().getConfigurationManager();
        systemPropertyRoot = null;
        propertyNameMap = new LinkedHashMap<String, ConfigurationEntry<?>>();
        fillPropertyNameMap();
    }

    public PropertyUtil(ConfigurationModule configurationModule, String systemPropertyRoot) {
        this.configurationModule = configurationModule;
        configurationManager = WPSConfig.getInstance().getConfigurationManager();
        this.systemPropertyRoot = systemPropertyRoot;
        propertyNameMap = new LinkedHashMap<String, ConfigurationEntry<?>>();
        fillPropertyNameMap();
    }

    private void fillPropertyNameMap() {
        List<? extends ConfigurationEntry<?>> properties = configurationModule.getConfigurationEntries();
        if (properties != null) {
            for (ConfigurationEntry<?> property : properties) {
                if (property != null) {
                    propertyNameMap.put(property.getKey(), property);
                }
            }
        }

    }

    public boolean extractBoolean(final String valueKey,
            boolean valueDefault) {

        String valueAsString;

        if (systemPropertyRoot != null) {
            String systemPropertyName = JOINER.join(systemPropertyRoot, valueKey);
            valueAsString = System.getProperty(systemPropertyName);
            if (valueAsString != null) {
                boolean value = Boolean.parseBoolean(valueAsString);
                LOGGER.info(SYSTEM_PROPERTY_EXISTS, systemPropertyName,
                        valueAsString, value);
                return value;
            } else {
                LOGGER.debug(SYSTEM_PROPERTY_NOT_PRESENT, systemPropertyName);
            }
        } else {
            LOGGER.debug(SYSTEM_PROPERTY_NOT_PRESENT_SKIPPING_LOOKUP, valueKey);
        }

        ConfigurationEntry<?> property = propertyNameMap != null ? propertyNameMap.get(valueKey) : null;
        if (property != null) {
            valueAsString = property.getValue().toString();
            boolean value = Boolean.parseBoolean(valueAsString);
            LOGGER.info(CONFIG_PROPERTY_EXISTS, valueKey, valueAsString, value);
            return value;
        } else {
            LOGGER.debug(CONFIG_PROPERTY_NOT_PRESENT, valueKey);
        }

        if (configurationModule != null && configurationManager != null) {
            ConfigurationEntry<?> configurationEntry = configurationManager.getConfigurationServices()
                    .getConfigurationEntry(configurationModule, valueKey);

            if (configurationEntry != null && configurationEntry.getValue() instanceof Boolean) {
                return (Boolean) configurationEntry.getValue();
            }
        }

        LOGGER.info(USING_DEFAULT_VALUE, valueKey, valueDefault);
        return valueDefault;
    }

    public long extractLong(final String valueKey,
            long valueDefault) {

        String valueAsString;

        if (systemPropertyRoot != null) {
            String systemPropertyName = JOINER.join(systemPropertyRoot, valueKey);
            valueAsString = System.getProperty(systemPropertyName);
            if (valueAsString != null) {
                try {
                    long value = Long.parseLong(valueAsString);
                    LOGGER.info(SYSTEM_PROPERTY_EXISTS, systemPropertyName,
                            valueAsString, value);
                    return value;
                } catch (NumberFormatException e) {
                    LOGGER.error(SYSTEM_PROPERTY_EXISTS_INVALID, valueKey,
                            valueAsString);
                }
            } else {
                LOGGER.debug(SYSTEM_PROPERTY_NOT_PRESENT, systemPropertyName);
            }
        } else {
            LOGGER.debug(SYSTEM_PROPERTY_NOT_PRESENT_SKIPPING_LOOKUP, valueKey);
        }

        ConfigurationEntry<?> property = propertyNameMap != null ? propertyNameMap.get(valueKey) : null;
        if (property != null) {
            valueAsString = property.getValue().toString();
            try {
                long value = Long.parseLong(valueAsString);
                LOGGER.info(SYSTEM_PROPERTY_EXISTS, valueKey, value);
                return value;
            } catch (NumberFormatException e) {
                LOGGER.error(SYSTEM_PROPERTY_EXISTS_INVALID, valueKey, valueAsString);
            }
        } else {
            LOGGER.debug(CONFIG_PROPERTY_NOT_PRESENT, valueKey);
        }

        if (configurationModule != null && configurationManager != null) {
            ConfigurationEntry<?> configurationEntry = configurationManager.getConfigurationServices()
                    .getConfigurationEntry(configurationModule, valueKey);

            if (configurationEntry != null && configurationEntry.getValue() instanceof Long) {
                return (Long) configurationEntry.getValue();
            }
        }

        LOGGER.info(USING_DEFAULT_VALUE, valueKey, valueDefault);
        return valueDefault;
    }

    public double extractDouble(final String valueKey,
            double valueDefault) {

        String valueAsString;

        if (systemPropertyRoot != null) {
            String systemPropertyName = JOINER.join(systemPropertyRoot, valueKey);
            valueAsString = System.getProperty(systemPropertyName);
            if (valueAsString != null) {
                try {
                    double value = Double.parseDouble(valueAsString);
                    LOGGER.info(SYSTEM_PROPERTY_EXISTS, systemPropertyName,
                            valueAsString, value);
                    return value;
                } catch (NumberFormatException e) {
                    LOGGER.error(SYSTEM_PROPERTY_EXISTS_INVALID, valueKey,
                            valueAsString);
                }
            } else {
                LOGGER.debug(SYSTEM_PROPERTY_NOT_PRESENT, systemPropertyName);
            }
        } else {
            LOGGER.debug(SYSTEM_PROPERTY_NOT_PRESENT_SKIPPING_LOOKUP, valueKey);
        }

        ConfigurationEntry<?> property = propertyNameMap != null ? propertyNameMap.get(valueKey) : null;
        if (property != null) {
            valueAsString = property.getValue().toString();
            try {
                double value = Double.parseDouble(valueAsString);
                LOGGER.info(SYSTEM_PROPERTY_EXISTS, valueKey, value);
                return value;
            } catch (NumberFormatException e) {
                LOGGER.error(SYSTEM_PROPERTY_EXISTS_INVALID, valueKey, valueAsString);
            }
        } else {
            LOGGER.debug(CONFIG_PROPERTY_NOT_PRESENT, valueKey);
        }

        if (configurationModule != null && configurationManager != null) {
            ConfigurationEntry<?> configurationEntry = configurationManager.getConfigurationServices()
                    .getConfigurationEntry(configurationModule, valueKey);

            if (configurationEntry != null && configurationEntry.getValue() instanceof Double) {
                return (Double) configurationEntry.getValue();
            }
        }

        LOGGER.info(USING_DEFAULT_VALUE, valueKey, valueDefault);
        return valueDefault;
    }

    public String extractString(String valueKey,
            String valueDefault) {

        String value;

        if (systemPropertyRoot != null) {
            String systemPropertyName = JOINER.join(systemPropertyRoot, valueKey);
            value = System.getProperty(systemPropertyName);
            if (value != null) {
                LOGGER.info("System property \"{}\" exists, using database path of: ", systemPropertyName, value);
                return value;
            } else {
                LOGGER.debug(SYSTEM_PROPERTY_NOT_PRESENT, systemPropertyName);
            }
        } else {
            LOGGER.debug(SYSTEM_PROPERTY_NOT_PRESENT_SKIPPING_LOOKUP, valueKey);
        }

        ConfigurationEntry<?> property = propertyNameMap != null ? propertyNameMap.get(valueKey) : null;
        if (property != null) {
            value = property.getValue().toString();
            LOGGER.info("Config property \"{}\" exists, using value of: ", valueKey, value);
            return property.getValue().toString();
        } else {
            LOGGER.debug(CONFIG_PROPERTY_NOT_PRESENT, valueKey);
        }

        if (configurationModule != null && configurationManager != null) {
            ConfigurationEntry<?> configurationEntry = configurationManager.getConfigurationServices()
                    .getConfigurationEntry(configurationModule, valueKey);

            if (configurationEntry != null && configurationEntry.getValue() instanceof String) {
                return (String) configurationEntry.getValue();
            }
        }

        LOGGER.info("Using default value for \"{}\": {}", valueKey, valueDefault);
        return valueDefault;
    }

    public long extractPeriodAsMillis(String valueKey,
            long valueDefault) {

        String periodAsString;

        if (systemPropertyRoot != null) {
            String systemPropertyName = JOINER.join(systemPropertyRoot, valueKey);
            periodAsString = System.getProperty(systemPropertyName);
            if (periodAsString != null) {
                try {
                    Period period = Period.parse(periodAsString);
                    if (period != null) {
                        long periodMillis = period.toStandardDuration().getMillis();
                        LOGGER.info("System property \"{}\" exists, using value of: {} ({}ms) ", systemPropertyName,
                                periodAsString, periodMillis);
                        return periodMillis;
                    } else {
                        LOGGER.error(SYSTEM_PROPERTY_EXISTS_ISO,
                                systemPropertyName, periodAsString);
                    }
                } catch (Exception e) {
                    LOGGER.error(SYSTEM_PROPERTY_EXISTS_ISO,
                            systemPropertyName, periodAsString);
                }
            } else {
                LOGGER.debug(SYSTEM_PROPERTY_NOT_PRESENT, systemPropertyName);
            }
        } else {
            LOGGER.debug(SYSTEM_PROPERTY_NOT_PRESENT_SKIPPING_LOOKUP, valueKey);
        }

        ConfigurationEntry<?> property = propertyNameMap != null ? propertyNameMap.get(valueKey) : null;
        if (property != null) {
            periodAsString = property.getValue().toString();
            try {
                Period period = Period.parse(periodAsString);
                if (period != null) {
                    long periodMillis = period.toStandardDuration().getMillis();
                    LOGGER.info("Config property for \"{}\" exists, using value of: {} ({}ms) ", valueKey,
                            periodAsString, periodMillis);
                    return periodMillis;
                } else {
                    LOGGER.error(MESSAGE_ISO_PERIOD_PROPERTY, valueKey, periodAsString);
                }
            } catch (Exception e) {
                LOGGER.error(MESSAGE_ISO_PERIOD_PROPERTY, valueKey, periodAsString);
            }
        } else {
            LOGGER.debug("Config property for \"{}\"  not present", valueKey);
        }

        if (configurationModule != null && configurationManager != null) {
            ConfigurationEntry<?> configurationEntry = configurationManager.getConfigurationServices()
                    .getConfigurationEntry(configurationModule, valueKey);

            if (configurationEntry != null && configurationEntry.getValue() instanceof String) {
                periodAsString = (String) configurationEntry.getValue();

                if (periodAsString != null) {
                    try {
                        Period period = Period.parse(periodAsString);
                        if (period != null) {
                            long periodMillis = period.toStandardDuration().getMillis();
                            LOGGER.info("ConfigurationModule entry for \"{}\" exists, using value of: {} ({}ms) ",
                                    valueKey, periodAsString, periodMillis);
                            return periodMillis;
                        } else {
                            LOGGER.error(
                                    MESSAGE_ISO_PERIOD_MODULE,
                                    valueKey, periodAsString);
                        }
                    } catch (Exception e) {
                        LOGGER.error(
                                MESSAGE_ISO_PERIOD_MODULE,
                                valueKey, periodAsString);
                    }
                } else {
                    LOGGER.error(
                            MESSAGE_ISO_PERIOD_MODULE,
                            valueKey);
                }
            }
        }

        LOGGER.info("Using default value for \"{}\" of {}ms", valueKey, valueDefault);
        return valueDefault;
    }
}
