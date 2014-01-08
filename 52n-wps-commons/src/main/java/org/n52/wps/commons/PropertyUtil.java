/**
 * ﻿Copyright (C) 2006
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
