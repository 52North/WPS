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

package org.n52.wps.webapp.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.RepositoryDocument.Repository;
import org.n52.wps.impl.WPSConfigurationDocumentImpl.WPSConfigurationImpl;
import org.n52.wps.webapp.api.ConfigurationManager;
import org.n52.wps.webapp.api.ConfigurationModule;
import org.n52.wps.webapp.api.WPSConfigurationException;
import org.n52.wps.webapp.api.types.ConfigurationEntry;
import org.n52.wps.webapp.dao.ConfigurationDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("repositoryService")
public class RepositoryServiceXml implements RepositoryService {

	@Autowired
	ConfigurationManager configurationManager;

	@Autowired
	private WPSConfigurationImpl wpsConfigurationImpl;

	@Autowired
	private ConfigurationDAO configurationDAO;

	private static Logger LOGGER = LoggerFactory.getLogger(RepositoryServiceXml.class);

	@Override
	public void syncRepositories() {
		LOGGER.debug("Initializing repositories' configurations...");
		removeDeletedRepositories();
		storeConfigurationModules();
		configurationDAO.save();
		LOGGER.debug("Repositories configurations initialized.");
	}

	@Override
	public void removeDeletedRepositories() {
		LOGGER.debug("Checking for deleted repositories to remove...");
		boolean repositoryExisits = false;
		Set<ConfigurationModule> modules = configurationManager.getAllConfigurationModules().keySet();
		List<Integer> modulesToRemove = new ArrayList<Integer>();
		for (int i = 0; i < wpsConfigurationImpl.getAlgorithmRepositoryList().getRepositoryArray().length; i++) {
			for (ConfigurationModule module : modules) {
				if (wpsConfigurationImpl.getAlgorithmRepositoryList().getRepositoryArray(i).getClassName()
						.equals(module.getClass().getName())) {
					repositoryExisits = true;
					break;
				}
			}

			if (!repositoryExisits) {
				modulesToRemove.add(i);
			}
			repositoryExisits = false;
		}
		for (Integer index : modulesToRemove) {
			String name = wpsConfigurationImpl.getAlgorithmRepositoryList().getRepositoryArray(index).getClassName();
			wpsConfigurationImpl.getAlgorithmRepositoryList().removeRepository(index);
			LOGGER.debug("Removed non-existing module '" + name + "'.");
		}
		LOGGER.debug("Done checking for deleted repositories to remove.");
	}

	@Override
	public void storeConfigurationModules() {
		LOGGER.debug("Writing and syncing repositories configuration modules...");
		
		for (ConfigurationModule module : configurationManager.getAllConfigurationModules().keySet()) {
			LOGGER.debug("Writing and syncing: " + module.getClass().getName());
			boolean moduleExists = false;

			for (Repository repository : wpsConfigurationImpl.getAlgorithmRepositoryList().getRepositoryArray()) {
				if (module.getClass().getName().equals(repository.getClassName())) {
					LOGGER.debug("Module '" + module.getClass().getName() + "' already exists, skip writing, start syncing configuration entries.");
					moduleExists = true;
					removeConfigurationEntries(module, repository);
					addConfigurationEntries(module, repository);
				}
			}
			if (!moduleExists) {
				Repository repository = wpsConfigurationImpl.getAlgorithmRepositoryList().addNewRepository();
				repository.setName(module.getModuleName());
				repository.setClassName(module.getClass().getName());
				repository.setActive(module.isActive());
				for (ConfigurationEntry<?> entry : configurationManager.getAllConfigurationModules().get(module)
						.values()) {
					Property property = repository.addNewProperty();
					writeConfigurationEntry(entry, property);
				}
			}
			LOGGER.debug("Done writing and syncing: " + module.getClass().getName());
		}
		LOGGER.debug("Done writing and syncing repositories configuration modules.");
	}

	private void addConfigurationEntries(ConfigurationModule module, Repository repository) {
		LOGGER.debug("Syncing configuration entries for module: " + module.getClass().getName());
		for (ConfigurationEntry<?> entry : configurationManager.getAllConfigurationModules().get(module).values()) {
			LOGGER.debug("Syncing configuration entry '" + entry.getKey() + "' in module '" + module.getClass().getName() + "'.");
			boolean entryExists = false;
			for (Property property : repository.getPropertyArray()) {
				if ((property.getName().equals(entry.getKey()))) {
					LOGGER.debug("Configuration entry '" + entry.getKey() + "' already exists, syncing value and passing it to module '" + module.getClass().getName() + "' annotated setter method...");
					try {
						configurationManager.syncValue(module, entry.getKey(), property.getStringValue());
						configurationManager.passValueToConfigurationModule(module, entry.getKey());
						entryExists = true;
						LOGGER.debug("Done syncing and passing the value for configuration entry '" + entry.getKey() + "'.");
					} catch (WPSConfigurationException e) {
						//do nothing, the error already logged by the syncValue method.
					}
				}
			}
			if (!entryExists) {
				LOGGER.debug("Configuration entry '" + entry.getKey() + " in module '" + module.getClass().getName() + "' is new, writing it to file...");
				Property property = repository.addNewProperty();
				writeConfigurationEntry(entry, property);
				LOGGER.debug("Done writing new configuration entry '" + entry.getKey() + "'.");
			}
		}
		LOGGER.debug("Done syncing configuration entries for module: " + module.getClass().getName());
	}

	private void removeConfigurationEntries(ConfigurationModule module, Repository repository) {
		LOGGER.debug("Checking for non-existing configuration entries to remove in module: " + module.getClass().getName());
		boolean propertyExists = false;
		Collection<ConfigurationEntry<?>> entries = configurationManager.getAllConfigurationModules().get(module).values();
		List<Integer> entriesToRemove = new ArrayList<Integer>();

		for (int i = 0; i < repository.getPropertyArray().length; i++) {
			for (ConfigurationEntry<?> entry : entries) {
				if (repository.getPropertyArray(i).getName().equals(entry.getKey())) {
					propertyExists = true;
					break;
				}
			}
			if (!propertyExists) {
				entriesToRemove.add(i);
			}
			propertyExists = false;
		}
		for (Integer index : entriesToRemove) {
			String name = repository.getPropertyArray(index).getName();
			repository.removeProperty(index);
			LOGGER.debug("Removed non-existing configuration entry '" + name + "'.");
		}
		LOGGER.debug("Done checking for non-existing configuration entries in module: " + module.getClass().getName());
	}

	private void writeConfigurationEntry(ConfigurationEntry<?> entry, Property property) {
		property.setName(entry.getKey());
		property.setStringValue(entry.getValue().toString());
		property.setActive(true);
	}

	@Override
	public void storeValue(ConfigurationModule module, String entryKey, Object value) {
		LOGGER.debug("Storing value '" + value.toString() + "' for entry '" + entryKey + "' in module '" + module.getClass().getName() + "'...");
		for (Repository repository : wpsConfigurationImpl.getAlgorithmRepositoryList().getRepositoryArray()) {
			if (module.getClass().getName().equals(repository.getClassName())) {
				for (Property property : repository.getPropertyArray()) {
					if ((property.getName().equals(entryKey))) {
						property.setStringValue(value.toString());
					}
				}
			}
		}
		configurationDAO.save();
		LOGGER.debug("Stored value '" + value.toString() + "' for entry '" + entryKey + "' in module '" + module.getClass().getName() + "'.");
	}
}
