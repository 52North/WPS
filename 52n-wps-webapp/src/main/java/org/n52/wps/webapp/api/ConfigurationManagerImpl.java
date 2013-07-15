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

package org.n52.wps.webapp.api;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.n52.wps.webapp.api.types.BooleanConfigurationEntry;
import org.n52.wps.webapp.api.types.ConfigurationEntry;
import org.n52.wps.webapp.api.types.DoubleConfigurationEntry;
import org.n52.wps.webapp.api.types.FileConfigurationEntry;
import org.n52.wps.webapp.api.types.IntegerConfigurationEntry;
import org.n52.wps.webapp.api.types.StringConfigurationEntry;
import org.n52.wps.webapp.api.types.URIConfigurationEntry;
import org.n52.wps.webapp.service.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("configurationManager")
public class ConfigurationManagerImpl implements ConfigurationManager {

	@Autowired
	private ConfigurationModules configurationModules;

	@Autowired
	private RepositoryService repositoryService;

	@Autowired
	private ValueParser valueParser;

	private static Logger LOGGER = LoggerFactory.getLogger(ConfigurationManagerImpl.class);

	@Override
	public Map<ConfigurationModule, Map<String, ConfigurationEntry<?>>> getAllConfigurationModules() {
		return configurationModules.getAllConfigurationModules();
	}

	@Override
	public ConfigurationModule getConfigurationModule(Class<? extends ConfigurationModule> clazz) {
		return configurationModules.getConfigurationModule(clazz);
	}

	@Override
	public ConfigurationModule getConfigurationModuleByName(String moduleClassName) {
		return configurationModules.getConfigurationModuleByName(moduleClassName);
	}

	@Override
	public void setValue(ConfigurationModule module, String entryKey, Object value) throws WPSConfigurationException {
		try {
			setValueHelper(module, entryKey, value);
		} catch (WPSConfigurationException e) {
			String errorMessage = "Unable to set value '" + value + "' in module '" + module.getClass().getName()
					+ "' for entry '" + entryKey + "': " + e.getMessage();
			LOGGER.error(errorMessage);
			throw new WPSConfigurationException(errorMessage);
		}
		repositoryService.storeValue(module, entryKey, value);
		passValueToConfigurationModule(module, entryKey);
	}

	@Override
	public void syncValue(ConfigurationModule module, String entryKey, Object value) throws WPSConfigurationException {
		try {
			setValueHelper(module, entryKey, value);
		} catch (WPSConfigurationException e) {
			String errorMessage = "Unable to sync value '" + value + "' in module '" + module.getClass().getName()
					+ "' for entry '" + entryKey + "': " + e.getMessage();
			LOGGER.error(errorMessage);
			throw new WPSConfigurationException(errorMessage);
		}
	}

	private void setValueHelper(ConfigurationModule module, String entryKey, Object value)
			throws WPSConfigurationException {
		ConfigurationEntry<?> entry = getAllConfigurationModules().get(module).get(entryKey);
		switch (entry.getType()) {
		case STRING:
			setStringValue((StringConfigurationEntry) entry, value);
			break;
		case BOOLEAN:
			setBooleanValue((BooleanConfigurationEntry) entry, value);
			break;
		case DOUBLE:
			setDoubleValue((DoubleConfigurationEntry) entry, value);
			break;
		case FILE:
			setFileValue((FileConfigurationEntry) entry, value);
			break;
		case INTEGER:
			setIntegerValue((IntegerConfigurationEntry) entry, value);
			break;
		case URI:
			setURIValue((URIConfigurationEntry) entry, value);
			break;
		default:
			break;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getValue(ConfigurationModule module, String entryKey, Class<T> requiredType) throws WPSConfigurationException {
		ConfigurationEntry<?> entry = configurationModules.getAllConfigurationModules().get(module).get(entryKey);
		Object value = entry.getValue();
		if (requiredType != null && !requiredType.isAssignableFrom(value.getClass())) {
			String errorMessage = "The required type is '" + requiredType.getSimpleName()
					+ "' but the entry value type is '" + entry.getType().toString() + "'.";
			LOGGER.error(errorMessage);
			throw new WPSConfigurationException(errorMessage);
		}
		return (T) value;
	}

	@PostConstruct
	public void initializeConfigurations() {
		LOGGER.debug("Initializing configurations...");
		repositoryService.syncRepositories();
		LOGGER.debug("Configurations initialized.");
	}

	@Override
	public void passValueToConfigurationModule(ConfigurationModule module, String entryKey) throws WPSConfigurationException {

		for (Method method : module.getClass().getMethods()) {
			if (method.isAnnotationPresent(ConfigurationKey.class)) {
				ConfigurationKey configurationKey = method.getAnnotation(ConfigurationKey.class);
				if (configurationKey.key().equals(entryKey)) {
					Class<?>[] clazz = method.getParameterTypes();
					try {
						if (method.getParameterTypes().length != 1) {
							throw new WPSConfigurationException("The method has more than one parameter.");
						}
						
						if (clazz[0].isPrimitive()) {
							if(clazz[0].toString().equals("int")) {
								clazz[0] = Integer.class;
							}
							if(clazz[0].toString().equals("double")) {
								clazz[0] = Double.class;
							}
							if(clazz[0].toString().equals("boolean")) {
								clazz[0] = Boolean.class;
							}
						}
						
						Object value = getValue(module, entryKey, clazz[0]);
						method.invoke(module, value);
						LOGGER.debug("Value '" + value.toString() + "' passed to method '" + method.getName() + "' in module '" + module.getClass().getName() + "'." );
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
							| WPSConfigurationException e) {
						String errorMessage = "Cannot pass value to method '" + method.getName() + "' in module '"
								+ module.getClass().getName() + "' for entry '" + configurationKey.key() + "': "
								+ e.getMessage();
						LOGGER.error(errorMessage);
						throw new WPSConfigurationException(errorMessage);
					}
				}
			}
		}
	}

	private void setStringValue(ConfigurationEntry<String> entry, Object value) throws WPSConfigurationException {
		String parsedValue = String.class.cast(valueParser.parseString(value));
		entry.setValue(parsedValue);
	}

	private void setIntegerValue(ConfigurationEntry<Integer> entry, Object value) throws WPSConfigurationException {
		Integer parsedValue = Integer.class.cast(valueParser.parseInteger(value));
		entry.setValue(parsedValue);
	}

	private void setDoubleValue(ConfigurationEntry<Double> entry, Object value) throws WPSConfigurationException {
		Double parsedValue = Double.class.cast(valueParser.parseDouble(value));
		entry.setValue(parsedValue);
	}

	private void setBooleanValue(ConfigurationEntry<Boolean> entry, Object value) throws WPSConfigurationException {
		Boolean parsedValue = Boolean.class.cast(valueParser.parseBoolean(value));
		entry.setValue(parsedValue);
	}

	private void setFileValue(ConfigurationEntry<File> entry, Object value) throws WPSConfigurationException {
		File parsedValue = File.class.cast(valueParser.parseFile(value));
		entry.setValue(parsedValue);
	}

	private void setURIValue(ConfigurationEntry<URI> entry, Object value) throws WPSConfigurationException {
		URI parsedValue = URI.class.cast(valueParser.parseURI(value));
		entry.setValue(parsedValue);
	}
}
