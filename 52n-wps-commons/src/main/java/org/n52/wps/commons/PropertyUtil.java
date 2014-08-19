/**
 * ﻿Copyright (C) 2006 - 2014 52°North Initiative for Geospatial Open Source
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
import java.util.Map;

import org.joda.time.Period;
import org.n52.wps.PropertyDocument;
import org.n52.wps.PropertyDocument.Property;
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
    
    private final static Logger LOGGER = LoggerFactory.getLogger(PropertyUtil.class);
    
    private final static Joiner JOINER = Joiner.on(".");
    
    private final String systemPropertyRoot;
    private final Map<String, Property> propertyNameMap;
    private ConfigurationModule configurationModule;
    private ConfigurationManager configurationManager;
    
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
    
    public PropertyUtil(ConfigurationModule configurationModule) {
    	this.configurationModule = configurationModule;
    	configurationManager = WPSConfig.getInstance().getConfigurationManager();
    	systemPropertyRoot = null;
    	propertyNameMap = null;
    }
    
    public PropertyUtil(ConfigurationModule configurationModule, String systemPropertyRoot) {
    	this.configurationModule = configurationModule;
    	configurationManager = WPSConfig.getInstance().getConfigurationManager();
    	this.systemPropertyRoot = systemPropertyRoot;
    	propertyNameMap = null;
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
        
        Property property = propertyNameMap != null ? propertyNameMap.get(valueKey) : null;
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
        
        if(configurationModule != null && configurationManager != null){
        	ConfigurationEntry<?> configurationEntry = configurationManager.getConfigurationServices().getConfigurationEntry(configurationModule, valueKey);
        	
        	if(configurationEntry != null && configurationEntry.getValue() instanceof Boolean){
        		return (Boolean) configurationEntry.getValue();
        	}        	
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
        
        Property property = propertyNameMap != null ? propertyNameMap.get(valueKey) : null;
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
        
        if(configurationModule != null && configurationManager != null){
        	ConfigurationEntry<?> configurationEntry = configurationManager.getConfigurationServices().getConfigurationEntry(configurationModule, valueKey);
        	
        	if(configurationEntry != null && configurationEntry.getValue() instanceof Long){
        		return (Long) configurationEntry.getValue();
        	}        	
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
        
        Property property = propertyNameMap != null ? propertyNameMap.get(valueKey) : null;
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
        
        if(configurationModule != null && configurationManager != null){
        	ConfigurationEntry<?> configurationEntry = configurationManager.getConfigurationServices().getConfigurationEntry(configurationModule, valueKey);
        	
        	if(configurationEntry != null && configurationEntry.getValue() instanceof Double){
        		return (Double) configurationEntry.getValue();
        	}        	
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
         
        Property property = propertyNameMap != null ? propertyNameMap.get(valueKey) : null;
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
        
        if(configurationModule != null && configurationManager != null){
        	ConfigurationEntry<?> configurationEntry = configurationManager.getConfigurationServices().getConfigurationEntry(configurationModule, valueKey);
        	
        	if(configurationEntry != null && configurationEntry.getValue() instanceof String){
        		return (String) configurationEntry.getValue();
        	}        	
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
        
        PropertyDocument.Property property = propertyNameMap != null ? propertyNameMap.get(valueKey) : null;
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
        
        if(configurationModule != null && configurationManager != null){
        	ConfigurationEntry<?> configurationEntry = configurationManager.getConfigurationServices().getConfigurationEntry(configurationModule, valueKey);
        	
        	if(configurationEntry != null && configurationEntry.getValue() instanceof String){
        		periodAsString = (String) configurationEntry.getValue();
                
        		if (periodAsString != null) {
                    try {
                        Period period = Period.parse(periodAsString);
                        if (period != null) {
                            long periodMillis = period.toStandardDuration().getMillis();
                            LOGGER.info("ConfigurationModule entry for \"{}\" exists, using value of: {} ({}ms) ", valueKey, periodAsString, periodMillis);
                            return periodMillis;
                        } else {
                            LOGGER.error("ConfigurationModule entry for \"{}\" exists but unable to parse \"{}\" as ISO8601 period", valueKey, periodAsString);
                        }
                    } catch (Exception e) {
                        LOGGER.error("ConfigurationModule entry for \"{}\" exists but unable to parse \"{}\" as ISO8601 period", valueKey, periodAsString);
                    }
                } else {
                    LOGGER.error("ConfigurationModule entry for \"{}\" exists but unable to parse \"{}\" as ISO8601 period", valueKey, periodAsString);
                }       		
        	}        	
        }
        
        LOGGER.info("Using default value for \"{}\" of {}ms", valueKey, valueDefault);
        return valueDefault;
    }
}
