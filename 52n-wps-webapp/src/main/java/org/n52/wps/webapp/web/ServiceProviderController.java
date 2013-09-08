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

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.n52.wps.webapp.api.ConfigurationManager;
import org.n52.wps.webapp.entities.ServiceProvider;
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

@Controller
@RequestMapping("service_provider")
public class ServiceProviderController {
	@Autowired
	private ConfigurationManager configurationManager;

	private final Logger LOGGER = LoggerFactory.getLogger(ServiceProviderController.class);

	/**
	 * Display the service provider module
	 * 
	 * @return The service provider view
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String display(Model model) {
		ServiceProvider serviceProvider = configurationManager.getCapabilitiesServices().getServiceProvider();
		model.addAttribute("serviceProvider", serviceProvider);
		LOGGER.info("Reterived '{}' configuration module.", serviceProvider.getClass().getName());
		return "service_provider";
	}

	/**
	 * Process form submission
	 * 
	 * @return Success or failure, and all field errors in case of failure
	 */
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public ValidationResponse processPost(@ModelAttribute("serviceProvider") @Valid ServiceProvider serviceProvider,
			BindingResult result, Model model, HttpServletResponse response) {
		ValidationResponse validationResponse = new ValidationResponse();
		if (result.hasErrors()) {
			validationResponse.setErrorMessageList(result.getFieldErrors());
			validationResponse.setStatus("Fail");
			response.setStatus(400);
		} else {
			configurationManager.getCapabilitiesServices().saveServiceProvider(serviceProvider);
			validationResponse.setStatus("Sucess");
		}
		return validationResponse;
	}
}
