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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.n52.wps.webapp.api.types.ConfigurationEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConfigurationModules {

	@Autowired
	private ListableBeanFactory listableBeanFactory;

	private static Logger LOGGER = LoggerFactory.getLogger(ConfigurationModules.class);
	
	private Map<ConfigurationModule, Map<String, ConfigurationEntry<?>>> configurationModules = new HashMap<ConfigurationModule, Map<String, ConfigurationEntry<?>>>();

	public Map<ConfigurationModule, Map<String, ConfigurationEntry<?>>> getAllConfigurationModules() {
		return configurationModules;
	}

	public ConfigurationModule getConfigurationModule(Class<? extends ConfigurationModule> clazz) {
		return listableBeanFactory.getBean(clazz);
	}

	public ConfigurationModule getConfigurationModuleByName(String moduleClassName) {
		Set<ConfigurationModule> modules = getAllConfigurationModules().keySet();
		for (ConfigurationModule module : modules) {
			if (module.getClass().getName().equals(moduleClassName)) {
				return module;
			}
		}
		return null;
	}

	@PostConstruct
	private void buildMap() {

		for (ConfigurationModule module : listableBeanFactory.getBeansOfType(ConfigurationModule.class).values()) {

			Map<String, ConfigurationEntry<?>> keyEntryMap = new HashMap<String, ConfigurationEntry<?>>();
			List<ConfigurationEntry<?>> moduleConfigurationEntries = module.getConfigurationEntries();

			for (int i = 0; i < moduleConfigurationEntries.size(); i++) {
				if (keyEntryMap.get(moduleConfigurationEntries.get(i).getKey()) != null) {
					LOGGER.error("Entry '"
							+ moduleConfigurationEntries.get(i).getKey() + "' in module '"
									+ module.getClass().getName() + "' already exists, it has been skipped. To add it, change to a unique key and restart.");
				} else {
					keyEntryMap.put(moduleConfigurationEntries.get(i).getKey(), moduleConfigurationEntries.get(i));
				}
			}

			configurationModules.put(module, keyEntryMap);
			LOGGER.debug("Configuration entries mapped for: " + module.getClass().getName());
		}
	}
}
