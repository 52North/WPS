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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.n52.wps.webapp.api.ConfigurationManager;
import org.n52.wps.webapp.api.WPSConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Abstract configuration controller used by standard configuration modules. The class respond to form posts, set
 * configuration modules status, and provide standard way for error handling.
 * 
 * @see RepositoriesController
 * @see GeneratorsController
 * @see ParsersController
 */
public class BaseConfigurationsController {
	@Autowired
	protected ConfigurationManager configurationManager;

	protected final Logger LOGGER = LoggerFactory.getLogger(getClass().getName());

	/**
	 * Process form post for standard configuration modules (repositories, generators, and parsers). If there is an
	 * error in the form values, an exception will be thrown and handled by the
	 * {@link #displayError(WPSConfigurationException) displayError} method.
	 * 
	 * @param request
	 * @throws WPSConfigurationException
	 *             if form values parsing and validation fails
	 */
	@RequestMapping(method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	protected void processPost(HttpServletRequest request) throws WPSConfigurationException {
		String[] keys = request.getParameterValues("key");
		String[] values = request.getParameterValues("value");
		String moduleClassName = request.getParameter("module");

		LOGGER.debug("Processing module '{}' submitted values.", moduleClassName);
		configurationManager.getConfigurationServices().setConfigurationModuleValues(moduleClassName, keys, values);
		LOGGER.info("Configuration module '{}' values has been saved.", moduleClassName);
	}

	/**
	 * Set the status of a configuration module to active/inactive
	 * 
	 * @param moduleClassName
	 *            the fully qualified name of the module to be set
	 * @param status
	 *            the new status
	 */
	@RequestMapping(value = "activate/{moduleClassName}/{status}", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	protected void toggleModuleStatus(@PathVariable String moduleClassName, @PathVariable boolean status) {
		configurationManager.getConfigurationServices().updateConfigurationModuleStatus(moduleClassName, status);
		LOGGER.info("Module '{}' status has been updated to '{}'", moduleClassName, status);
	}

	/**
	 * Handle exceptions thrown by form processing methods. This method will get the field causing the exception along
	 * with the error message, and return the error as a JSON object to the client.
	 * 
	 * @param e
	 *            the exception thrown
	 * @return an object containing the field error and message
	 * @see ValidationResponse
	 */
	@ExceptionHandler(WPSConfigurationException.class)
	@ResponseBody
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	protected ValidationResponse displayError(WPSConfigurationException e) {
		ValidationResponse validationResponse = new ValidationResponse();
		List<FieldError> listOfErros = new ArrayList<FieldError>();
		FieldError error = new FieldError("ConfigurationEntry", e.getField(), e.getMessage());
		listOfErros.add(error);
		validationResponse.setErrorMessageList(listOfErros);
		return validationResponse;
	}
}
