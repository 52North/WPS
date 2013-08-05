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
package org.n52.wps.webapp.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.n52.wps.webapp.api.ConfigurationCategory;
import org.n52.wps.webapp.api.ConfigurationManager;
import org.n52.wps.webapp.api.ConfigurationModule;
import org.n52.wps.webapp.api.WPSConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("repositories")
public class RepositoriesController {

	@Autowired
	private ConfigurationManager configurationManager;

	private static Logger LOGGER = LoggerFactory.getLogger(RepositoriesController.class);

	/**
	 * Get all repository configuration modules
	 * 
	 * @return The repositories view
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String displayRepositories(Model model) {
		ConfigurationCategory category = ConfigurationCategory.REPOSITORY;
		Map<String, ConfigurationModule> configurations = configurationManager.getConfigurationServices()
				.getConfigurationModulesByCategory(category);
		model.addAttribute("configurations", configurations);
		LOGGER.info("Reterived '{}' configurations.", category);
		return "repositories";
	}

	@ResponseBody
	@RequestMapping(value = "/json", method = RequestMethod.GET)
	public Map<String, ConfigurationModule> getJSON() {
		ConfigurationCategory category = ConfigurationCategory.REPOSITORY;
		Map<String, ConfigurationModule> configurations = configurationManager.getConfigurationServices()
				.getConfigurationModulesByCategory(category);
		return configurations;
	}

	/**
	 * Process a repository configuration module form post
	 * 
	 * @throws WPSConfigurationException
	 *             if a value cannot be set
	 */
	@RequestMapping(method = RequestMethod.POST)
	public void processPost(HttpServletRequest request) throws WPSConfigurationException {
		String[] keys = request.getParameterValues("key");
		String[] values = request.getParameterValues("value");
		String moduleClassName = request.getParameter("module");

		LOGGER.debug("Processing module '{}' values.", moduleClassName);
		for (int i = 0; i < keys.length; i++) {
			LOGGER.debug("Setting entry '{}' in module '{}' to value '{}'", keys[i], moduleClassName, values[i]);
			try {
				configurationManager.getConfigurationServices().setConfigurationEntryValue(moduleClassName, keys[i],
						values[i]);
			} catch (WPSConfigurationException e) {
				throw new WPSConfigurationException("Cannot set entry '" + keys[i] + "' in module '" + moduleClassName
						+ "': " + e.getMessage());
			}
		}
		LOGGER.info("Configuration module '{}' values has been saved.", moduleClassName);
	}

	@RequestMapping(value = "repositories/activate/{moduleClassName}", method = RequestMethod.POST)
	public String toggleRepositoryStatus(@PathVariable String moduleClassName) {
		// TODO
		return "redirect:/repositories";
	}

	/**
	 * Toggle the status of an algorithm
	 */
	@RequestMapping(value = "algorithms/activate/{moduleClassName}/{algorithm}", method = RequestMethod.POST)
	public String toggleAlgorithmStatus(@PathVariable String moduleClassName, @PathVariable String algorithm) {
		ConfigurationModule module = configurationManager.getConfigurationServices().getConfigurationModule(
				moduleClassName);
		boolean currentStatus = configurationManager.getConfigurationServices().getAlgorithmEntry(module, algorithm)
				.isActive();
		configurationManager.getConfigurationServices().setAlgorithmEntry(moduleClassName, algorithm, !currentStatus);
		LOGGER.info("Algorithm '{}' status in module '{}' has been updated to '{}'", algorithm, moduleClassName,
				!currentStatus);
		return "redirect:/repositories";
	}

	@ExceptionHandler(WPSConfigurationException.class)
	@ResponseBody
	public String exceptionMessage(WPSConfigurationException e, HttpServletResponse response) {
		response.setStatus(400);
		LOGGER.error("Error setting entry value:", e);
		return e.getMessage();
	}
}
