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
 * Handles parsers specific URI requests and mapping.
 */
@Controller
@RequestMapping("parsers")
public class ParsersController extends BaseConfigurationsController {

	/**
	 * Display parsers configuration modules
	 * 
	 * @return The parsers view
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String displayRepositories(Model model) {
		ConfigurationCategory category = ConfigurationCategory.PARSER;
		Map<String, ConfigurationModule> configurations = configurationManager.getConfigurationServices()
				.getConfigurationModulesByCategory(category);
		model.addAttribute("configurations", configurations);
		LOGGER.info("Reterived '{}' configurations.", category);
		return "parsers";
	}
	
	/**
	 * TODO update
	 * Add a new algorithm to the repository
	 * 
	 * @param moduleClassName
	 *            The fully qualified name of the module holding the algorithm
	 * @param algorithmName
	 *            The algorithm name
	 */
	@RequestMapping(value = "formats/add_format", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public void addFormat(@RequestParam("moduleClassName") String moduleClassName, @RequestParam("mimeType") String mimeType, @RequestParam("schema") String schema, @RequestParam("encoding") String encoding) {
		configurationManager.getConfigurationServices().addFormatEntry(moduleClassName, mimeType, schema, encoding);
		LOGGER.info("Format '{}', '{}', '{}' has been added to module '{}'", mimeType, schema, encoding, moduleClassName);
	}
	
	/**
	 * TODO update
	 * Delete an algorithm from the repository
	 * 
	 * @param moduleClassName
	 *            The fully qualified name of the module holding the algorithm
	 * @param algorithmName
	 *            The algorithm name
	 */
	@RequestMapping(value = "formats/{moduleClassName}/{mimeType}/{schema}/{encoding}/delete", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public void deleteFormat2(@PathVariable String moduleClassName, @PathVariable String mimeType, @PathVariable String schema, @PathVariable String encoding) {
		mimeType = mimeType.replace("forwardslash", "/");
		
		if(schema.equals("null")){
			schema = "";
		}
		if(encoding.equals("null")){
			encoding = "";
		}
		
		configurationManager.getConfigurationServices().deleteFormatEntry(moduleClassName, mimeType, schema, encoding);
		LOGGER.info("Format '{}', '{}', '{}' of module '{}' has been deleted", mimeType, schema, encoding, moduleClassName);
	}
}
