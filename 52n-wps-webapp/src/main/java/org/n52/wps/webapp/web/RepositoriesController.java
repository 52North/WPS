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
package org.n52.wps.webapp.web;

import java.util.Map;

import org.n52.wps.webapp.api.ConfigurationCategory;
import org.n52.wps.webapp.api.ConfigurationModule;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Handles repositories specific URI requests and mapping.
 */
@Controller
@RequestMapping("repositories")
public class RepositoriesController extends BaseConfigurationsController {

	/**
	 * Display repositories configuration modules
	 * 
	 * @return The repositories view
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String displayRepositories(Model model) {
		ConfigurationCategory category = ConfigurationCategory.REPOSITORY;
		Map<String, ConfigurationModule> configurations = configurationManager.getConfigurationServices()
				.getConfigurationModulesByCategory(category);
		model.addAttribute("configurations", configurations);
		LOGGER.info("Retrived '{}' configurations.", category);
		return "repositories";
	}

	/**
	 * Set the status of a configuration algorithm to active/inactive
	 * 
	 * @param moduleClassName
	 *            The fully qualified name of the module holding the algorithm
	 * @param algorithm
	 *            The algorithm name
	 * @param status
	 *            The new status
	 */
	@RequestMapping(value = "algorithms/activate/{moduleClassName}/{algorithm}/{status}", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public void toggleAlgorithmStatus(@PathVariable String moduleClassName, @PathVariable String algorithm,
			@PathVariable boolean status) {
		configurationManager.getConfigurationServices().setAlgorithmEntry(moduleClassName, algorithm, status);
		LOGGER.info("Algorithm '{}' status in module '{}' has been updated to '{}'", algorithm, moduleClassName, status);
	}
	
	/**
	 * Add a new algorithm to the repository
	 * 
	 * @param moduleClassName
	 *            The fully qualified name of the module holding the algorithm
	 * @param algorithmName
	 *            The algorithm name
	 */
	@RequestMapping(value = "algorithms/add_algorithm", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public void addAlgorithm(@RequestParam("moduleClassName") String moduleClassName, @RequestParam("algorithmName") String algorithmName) {
		configurationManager.getConfigurationServices().addAlgorithmEntry(moduleClassName, algorithmName);
		LOGGER.info("Algorithm '{}' has been added to module '{}'", algorithmName, moduleClassName);
	}
	
	/**
	 * Delete an algorithm from the repository
	 * 
	 * @param moduleClassName
	 *            The fully qualified name of the module holding the algorithm
	 * @param algorithmName
	 *            The algorithm name
	 */
	@RequestMapping(value = "algorithms/{moduleClassName}/{algorithmName}/delete", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public void deleteAlgorithm(@PathVariable String moduleClassName, @PathVariable String algorithmName) {
		configurationManager.getConfigurationServices().deleteAlgorithmEntry(moduleClassName, algorithmName);
		LOGGER.info("Algorithm '{}' has been deleted from module '{}'", algorithmName, moduleClassName);
	}
}
