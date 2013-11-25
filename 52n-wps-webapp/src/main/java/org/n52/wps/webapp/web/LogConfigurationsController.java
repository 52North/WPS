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

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.n52.wps.webapp.api.ConfigurationManager;
import org.n52.wps.webapp.entities.LogConfigurations;
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
 * Handles the log configuration module URI requests and mapping.
 */
@Controller
@RequestMapping("log")
public class LogConfigurationsController {

	@Autowired
	private ConfigurationManager configurationManager;

	private final Logger LOGGER = LoggerFactory.getLogger(LogConfigurationsController.class);

	/**
	 * Display the log configuration module
	 * 
	 * @param model
	 * @return The log view
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String display(Model model) {
		LogConfigurations logConfigurations = configurationManager.getLogConfigurationsServices()
				.getLogConfigurations();
		List<String> logLevel = new ArrayList<String>();
		logLevel.add("DEBUG");
		logLevel.add("INFO");
		logLevel.add("WARN");
		model.addAttribute("logLevel", logLevel);
		model.addAttribute("logConfigurations", logConfigurations);
		LOGGER.info("Reterived '{}' configuration module.", logConfigurations.getClass().getName());
		return "log";
	}

	/**
	 * Process form submission. The method will return an HTTP 200 status code if there are no errors, else, it will
	 * return a 400 status code.
	 * 
	 * @param logConfigurations
	 *            The model holding the log configuration values
	 * @param result
	 * @param model
	 * @param response
	 * @return A {@code ValidationResponse} object with the list of form errors which can be empty if there are no
	 *         errors.
	 */
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public ValidationResponse processPost(
			@ModelAttribute("logConfigurations") @Valid LogConfigurations logConfigurations, BindingResult result,
			Model model, HttpServletResponse response) {
		ValidationResponse res = new ValidationResponse();
		if (result.hasErrors()) {
			model.addAttribute("logConfigurations", logConfigurations);
			res.setErrorMessageList(result.getFieldErrors());
			res.setStatus("Fail");
			response.setStatus(400);
		} else {
			configurationManager.getLogConfigurationsServices().saveLogConfigurations(logConfigurations);
			res.setStatus("Sucess");
		}
		return res;
	}
}
