/**
 * ﻿Copyright (C) 2006 - 2014 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.commons;

import java.util.LinkedHashMap;
import java.util.Map;

import org.joda.time.Period;
import org.n52.wps.PropertyDocument;
import org.n52.wps.PropertyDocument.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

/**
 *
 * @author tkunicki
 */
public class PropertyUtil {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(PropertyUtil.class);
    
    private final static Joiner JOINER = Joiner.on(".");
    
    private final String systemPropertyRoot;
    private final Map<String, Property> propertyNameMap;
    
    public PropertyUtil(Property[] properties) {
        this(properties, null);
    }
    
    public PropertyUtil(Property[] properties, String systemPropertyRoot) {
        propertyNameMap = new LinkedHashMap<String, Property>();
        if (properties != null) {
            for (Property property : properties) {
                if (property != null) {
                    propertyNameMap.put(property.getName(), property);
                }
            }
        }
        this.systemPropertyRoot = systemPropertyRoot;
    }

    public boolean extractBoolean(final String valueKey, boolean valueDefault) {
        
        String valueAsString;
        
        if (systemPropertyRoot != null) {
            String systemPropertyName = JOINER.join(systemPropertyRoot, valueKey);
            valueAsString = System.getProperty(systemPropertyName);
            if (valueAsString != null) {
               boolean value = Boolean.parseBoolean(valueAsString);
               LOGGER.info("System property \"{}\" exists, using value of: {} ({}) ", systemPropertyName, valueAsString, value);
               return value;
            } else {
                LOGGER.debug("System property \"{}\" not present", systemPropertyName);
            }
        } else {
            LOGGER.debug("System property root not present, skipping system property lookup for {}", valueKey);
        }
        
        Property property = propertyNameMap.get(valueKey);
        if (property != null) {
            if (property.getActive()) {
                valueAsString = property.getStringValue();
                if (valueAsString != null) {
                   boolean value = Boolean.parseBoolean(valueAsString);
                   LOGGER.info("Config property \"{}\" exists, using value of: {} ({}) ", valueKey, valueAsString, value);
                   return value;
                } else {
                   LOGGER.warn("Config property \"{}\" exists but value is null, ignoring", valueKey);
                }
            } else {
                LOGGER.warn("Config property \"{}\" exists but is not active, ignoring", valueKey);
            }
        } else {
            LOGGER.debug("Config property \"{}\" not present", valueKey);
        }
        
        LOGGER.info("Using default value for \"{}\" of {}", valueKey, valueDefault);
        return valueDefault;
    }
    
    public long extractLong(final String valueKey, long valueDefault) {
        
        String valueAsString;
        
        if (systemPropertyRoot != null) {
            String systemPropertyName = JOINER.join(systemPropertyRoot, valueKey);
            valueAsString = System.getProperty(systemPropertyName);
            if (valueAsString != null) {
                try {
                    long value = Long.parseLong(valueAsString);
                    LOGGER.info("System property \"{}\" exists, using value of: {} ({}) ", systemPropertyName, valueAsString, value);
                    return value;
                } catch (NumberFormatException e) {
                    LOGGER.error("System property \"{}\" exists, but value of \"{}\" is invalid", valueKey, valueAsString);
                }
            } else {
                LOGGER.debug("System property \"{}\" not present", systemPropertyName);
            }
        } else {
            LOGGER.debug("System property root not present, skipping system property lookup for {}", valueKey);
        }
        
        Property property = propertyNameMap.get(valueKey);
        if (property != null) {
            if (property.getActive()) {
                valueAsString = property.getStringValue();
                if (valueAsString != null) {
                    try {
                        long value = Long.parseLong(valueAsString);
                        LOGGER.info("System property \"{}\" exists, using value of: {}", valueKey, value);
                        return value;
                    } catch (NumberFormatException e) {
                        LOGGER.error("System property \"{}\" exists, but value of \"{}\" is invalid", valueKey, valueAsString);
                    }
                } else {
                   LOGGER.warn("Config property \"{}\" exists but value is null, ignoring", valueKey);
                }
            } else {
                LOGGER.warn("Config property \"{}\" exists but is not active, ignoring", valueKey);
            }
        } else {
            LOGGER.debug("Config property \"{}\" not present", valueKey);
        }
        
        LOGGER.info("Using default value for \"{}\" of {}", valueKey, valueDefault);
        return valueDefault;
    }
    
    public double extractDouble(final String valueKey, double valueDefault) {
        
        String valueAsString;
        
        if (systemPropertyRoot != null) {
            String systemPropertyName = JOINER.join(systemPropertyRoot, valueKey);
            valueAsString = System.getProperty(systemPropertyName);
            if (valueAsString != null) {
                try {
                    double value = Double.parseDouble(valueAsString);
                    LOGGER.info("System property \"{}\" exists, using value of: {} ({}) ", systemPropertyName, valueAsString, value);
                    return value;
                } catch (NumberFormatException e) {
                    LOGGER.error("System property \"{}\" exists, but value of \"{}\" is invalid", valueKey, valueAsString);
                }
            } else {
                LOGGER.debug("System property \"{}\" not present", systemPropertyName);
            }
        } else {
            LOGGER.debug("System property root not present, skipping system property lookup for {}", valueKey);
        }
        
        Property property = propertyNameMap.get(valueKey);
        if (property != null) {
            if (property.getActive()) {
                valueAsString = property.getStringValue();
                if (valueAsString != null) {
                    try {
                        double value = Double.parseDouble(valueAsString);
                        LOGGER.info("System property \"{}\" exists, using value of: {}", valueKey, value);
                        return value;
                    } catch (NumberFormatException e) {
                        LOGGER.error("System property \"{}\" exists, but value of \"{}\" is invalid", valueKey, valueAsString);
                    }
                } else {
                   LOGGER.warn("Config property \"{}\" exists but value is null, ignoring", valueKey);
                }
            } else {
                LOGGER.warn("Config property \"{}\" exists but is not active, ignoring", valueKey);
            }
        } else {
            LOGGER.debug("Config property \"{}\" not present", valueKey);
        }
        
        LOGGER.info("Using default value for \"{}\" of {}", valueKey, valueDefault);
        return valueDefault;
    }
    
    public String extractString(String valueKey, String valueDefault) {
        
        String value;

        if (systemPropertyRoot != null) {
            String systemPropertyName = JOINER.join(systemPropertyRoot, valueKey);
            value = System.getProperty(systemPropertyName);
            if (value != null) {
                LOGGER.info("System property \"{}\" exists, using database path of: ", systemPropertyName, value);
                return value;
            } else {
               LOGGER.debug("System property \"{}\" not present", systemPropertyName);
            }
        } else {
            LOGGER.debug("System property root not present, skipping system property lookup for {}", valueKey);
        }
         
        Property property = propertyNameMap.get(valueKey);
        if (property != null) {
            if (property.getActive()) {
                value = property.getStringValue();
                if (value != null) {
                   LOGGER.info("Config property \"{}\" exists, using value of: ", valueKey, value);
                   return property.getStringValue();
                } else {
                    LOGGER.warn("Config property \"{}\" exists but value is null, ignoring", valueKey);
                }
            } else {
                LOGGER.warn("Config property \"{}\" exists but is not active, ignoring", valueKey);
            }
        } else {
           LOGGER.debug("Config property \"{}\" not present", valueKey);
        }
         
        LOGGER.info("Using default value for \"{}\": {}", valueKey, valueDefault);
        return valueDefault;
    }
    
    public long extractPeriodAsMillis(String valueKey, long valueDefault) {
        
        String periodAsString;
        
        if (systemPropertyRoot != null) {
            String systemPropertyName = JOINER.join(systemPropertyRoot, valueKey);
            periodAsString = System.getProperty(systemPropertyName);
            if (periodAsString != null) {
                try {
                    Period period = Period.parse(periodAsString);
                    if (period != null) {
                        long periodMillis = period.toStandardDuration().getMillis();
                        LOGGER.info("System property \"{}\" exists, using value of: {} ({}ms) ", systemPropertyName, periodAsString, periodMillis);
                        return periodMillis;
                    } else {
                        LOGGER.error("System property \"{}\" exists but unable to parse \"{}\" as ISO8601 period", systemPropertyName, periodAsString);
                    }
                } catch (Exception e) {
                    LOGGER.error("System property \"{}\" exists but unable to parse \"{}\" as ISO8601 period", systemPropertyName, periodAsString);
                }
            } else {
                LOGGER.debug("System property \"{}\" not present", systemPropertyName);
            }
        } else {
            LOGGER.debug("System property root not present, skipping system property lookup for {}", valueKey);
        }
        
        PropertyDocument.Property property = propertyNameMap.get(valueKey);
        if (property != null) {
            if (property.getActive()) {
                periodAsString = property.getStringValue();
                if (periodAsString != null) {
                    try {
                        Period period = Period.parse(periodAsString);
                        if (period != null) {
                            long periodMillis = period.toStandardDuration().getMillis();
                            LOGGER.info("Config property for \"{}\" exists, using value of: {} ({}ms) ", valueKey, periodAsString, periodMillis);
                            return periodMillis;
                        } else {
                            LOGGER.error("Config property for \"{}\" exists but unable to parse \"{}\" as ISO8601 period", valueKey, periodAsString);
                        }
                    } catch (Exception e) {
                        LOGGER.error("Config property for \"{}\" exists but unable to parse \"{}\" as ISO8601 period", valueKey, periodAsString);
                    }
                } else {
                    LOGGER.error("Config property for \"{}\" exists but unable to parse \"{}\" as ISO8601 period", valueKey, periodAsString);
                }
            } else {
                LOGGER.warn("Config property for \"{}\" exists but is not active, ignoring", valueKey);
            }
        } else {
            LOGGER.debug("Config property for \"{}\"  not present", valueKey);
        }
        
        LOGGER.info("Using default value for \"{}\" of {}ms", valueKey, valueDefault);
        return valueDefault;
    }
}
