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

import javax.servlet.http.HttpServletRequest;

import org.n52.wps.webapp.api.ConfigurationManager;
import org.n52.wps.webapp.api.ConfigurationModule;
import org.n52.wps.webapp.api.WPSConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

public class BaseConfigurationsController {
	@Autowired
	protected ConfigurationManager configurationManager;

	protected final Logger LOGGER = LoggerFactory.getLogger(getClass().getName());

	@RequestMapping(method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	protected void processPost(HttpServletRequest request) throws WPSConfigurationException {
		String[] keys = request.getParameterValues("key");
		String[] values = request.getParameterValues("value");
		String moduleClassName = request.getParameter("module");

		LOGGER.debug("Processing module '{}' values.", moduleClassName);
		for (int i = 0; i < keys.length; i++) {
			LOGGER.debug("Setting entry '{}' in module '{}' to value '{}'.", keys[i], moduleClassName, values[i]);
			try {
				configurationManager.getConfigurationServices().setConfigurationEntryValue(moduleClassName, keys[i],
						values[i]);
			} catch (WPSConfigurationException e) {
				throw new WPSConfigurationException("Cannot set entry '" + keys[i] + "' in module '" + moduleClassName
						+ "': " + e.getMessage());
			}
			LOGGER.info("Configuration module '{}' values has been saved.", moduleClassName);
		}
		if (getClass() == ServiceProviderController.class || getClass() == ServiceIdentificationController.class) {
			configurationManager.getCapabilitiesServices().updateServiceIdentification();
		}
	}

	/**
	 * Toggle the status of a configuration module
	 */
	// {moduleClassName:.+} is used in case the name has dots, otherwise, it will be truncated
	@RequestMapping(value = "activate/{moduleClassName:.+}", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public void toggleRepositoryStatus(@PathVariable String moduleClassName) {
		ConfigurationModule module = configurationManager.getConfigurationServices().getConfigurationModule(
				moduleClassName);
		boolean currentStatus = module.isActive();
		module.setActive(!currentStatus);
		configurationManager.getConfigurationServices().updateConfigurationModule(module);
		LOGGER.info("Module '{}' status has been updated to '{}'", moduleClassName, !currentStatus);
	}

	@ExceptionHandler(WPSConfigurationException.class)
	@ResponseBody
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	protected String displayError(WPSConfigurationException e) {
		LOGGER.error("Error setting entry value:", e);
		return e.getMessage();
	}
}
