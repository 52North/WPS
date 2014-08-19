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

import org.n52.wps.webapp.api.ConfigurationModule;
import org.n52.wps.webapp.entities.Server;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Handles the server configuration module URI requests and mapping.
 */
@Controller
@RequestMapping("server")
public class ServerController extends BaseConfigurationsController {
	
	/**
	 * Display the server configuration module
	 * 
	 * @return The server view
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String display(Model model) {
		ConfigurationModule module = configurationManager.getConfigurationServices().getConfigurationModule(
				Server.class.getName());
		model.addAttribute("configurationModule", module);
		LOGGER.info("Reterived '{}' configuration module.", module.getClass().getName());
		return "server";
	}
}
