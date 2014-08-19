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

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.n52.wps.webapp.api.ConfigurationManager;
import org.n52.wps.webapp.entities.ServiceIdentification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Handles the service identification module URI requests and mapping.
 */
@Controller
@RequestMapping("service_identification")
public class ServiceIdentificationController {

	@Autowired
	private ConfigurationManager configurationManager;

	private final Logger LOGGER = LoggerFactory.getLogger(ServiceIdentificationController.class);

	/**
	 * Display the service identification module
	 * 
	 * @param model
	 * @return The service identification view
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String display(Model model) {
		ServiceIdentification serviceIdentification = configurationManager.getCapabilitiesServices()
				.getServiceIdentification();
		model.addAttribute("serviceIdentification", serviceIdentification);
		LOGGER.info("Reterived '{}' configuration module.", serviceIdentification.getClass().getName());
		return "service_identification";
	}

	/**
	 * Process form submission. The method will return an HTTP 200 status code if there
	 * are no errors, else, it will return a 400 status code.
	 * 
	 * @param serviceIdentification
	 *            The model holding the service identification values
	 * @param result
	 * @param model
	 * @param response
	 * @return A {@code ValidationResponse} object with the list of form errors which can be empty if there are no
	 *         errors.
	 */
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public ValidationResponse processPost(
			@ModelAttribute("serviceIdentification") @Valid ServiceIdentification serviceIdentification,
			BindingResult result, Model model, HttpServletResponse response) {
		ValidationResponse validationResponse = new ValidationResponse();
		if (result.hasErrors()) {
			validationResponse.setErrorMessageList(result.getFieldErrors());
			validationResponse.setStatus("Fail");
			response.setStatus(400);
		} else {
			configurationManager.getCapabilitiesServices().saveServiceIdentification(serviceIdentification);
			validationResponse.setStatus("Sucess");
		}
		return validationResponse;
	}
}
