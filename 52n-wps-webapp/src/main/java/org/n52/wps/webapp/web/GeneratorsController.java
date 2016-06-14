/**
 * Copyright (C) 2007-2015 52°North Initiative for Geospatial Open Source
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
 * Handles generators specific URI requests and mapping.
 */
@Controller
@RequestMapping("generators")
public class GeneratorsController extends BaseConfigurationsController {

	/**
	 * Display generators configuration modules
	 * 
	 * @return The generators view
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String displayGenerators(Model model) {
		ConfigurationCategory category = ConfigurationCategory.GENERATOR;
		Map<String, ConfigurationModule> configurations = configurationManager.getConfigurationServices()
				.getConfigurationModulesByCategory(category);
		model.addAttribute("configurations", configurations);
		LOGGER.info("Reterived '{}' configurations.", category);
		return "generators";
	}
	
	/**
	 * Add a new format to the module
	 * 
	 * @param moduleClassName
	 *            The fully qualified name of the module holding the format
         * @param mimeType
         *            The format mimeType
         * @param schema
         *            The format schema
         * @param encoding
         *            The format encoding
	 */
	@RequestMapping(value = "formats/add_format", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public void addFormat(@RequestParam("moduleClassName") String moduleClassName, @RequestParam("mimeType") String mimeType, @RequestParam("schema") String schema, @RequestParam("encoding") String encoding) {
		configurationManager.getConfigurationServices().addFormatEntry(moduleClassName, mimeType, schema, encoding);
		LOGGER.info("Format '{}', '{}', '{}' has been added to module '{}'", mimeType, schema, encoding, moduleClassName);
	}
	
        /**
         * Delete a format from the module
         * 
         * @param moduleClassName
         *            The fully qualified name of the module holding the format
         * @param mimeType
         *            The format mimeType
         * @param schema
         *            The format schema
         * @param encoding
         *            The format encoding
         */
	@RequestMapping(value = "formats/{moduleClassName}/{mimeType}/{schema}/{encoding}/delete", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public void deleteFormat(@PathVariable String moduleClassName, @PathVariable String mimeType, @PathVariable String schema, @PathVariable String encoding) {
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

	/**
	 * Set the status of a format to active/inactive
	 * 
	 * @param moduleClassName
	 *            The fully qualified name of the module holding the format
	 * @param mimeType
	 *            The format mimeType
	 * @param schema
	 *            The format schema
	 * @param encoding
	 *            The format encoding
	 * @param status
	 *            The new status
	 */
	@RequestMapping(value = "formats/activate/{moduleClassName}/{mimeType}/{schema}/{encoding}/{status}", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public void toggleFormatStatus(@PathVariable String moduleClassName, @PathVariable String mimeType, @PathVariable String schema, @PathVariable String encoding,
			@PathVariable boolean status) {
		
		mimeType = mimeType.replace("forwardslash", "/");
		
		if(schema.equals("null")){
			schema = "";
		}
		if(encoding.equals("null")){
			encoding = "";
		}
		
		configurationManager.getConfigurationServices().setFormatEntry(moduleClassName, mimeType, schema, encoding, status);
//		LOGGER.info("Algorithm '{}' status in module '{}' has been updated to '{}'", algorithm, moduleClassName, status);
	}

        
        /**
         * Update a format
         * 
         * @param moduleClassName
         *            The fully qualified name of the module holding the format
         * @param oldMimetype
         *            The old format mimetype
         * @param oldSchema
         *            The old format schema
         * @param oldEncoding
         *            The old format encoding
         * @param newMimetype
         *            The new format mimetype
         * @param newSchema
         *            The new format schema
         * @param newEncoding
         *            The new format encoding
         */
        @RequestMapping(value = "formats/edit_format", method = RequestMethod.POST)
        @ResponseStatus(value = HttpStatus.OK)
        public void editFormat(@RequestParam("moduleClassName") String moduleClassName, @RequestParam("old_mimetype") String oldMimetype, @RequestParam("old_schema") String oldSchema, @RequestParam("old_encoding") String oldEncoding, @RequestParam("new_mimetype") String newMimetype, @RequestParam("new_schema") String newSchema, @RequestParam("new_encoding") String newEncoding) {
            
            if(oldSchema.equals("undefined")){
                oldSchema = "";
            }
            if(oldEncoding.equals("undefined")){
                oldEncoding = "";
            }
            configurationManager.getConfigurationServices().updateFormatEntry(moduleClassName, oldMimetype, oldSchema, oldEncoding, newMimetype, newSchema, newEncoding);
            LOGGER.info("Format '{}', '{}', '{}' of module '{}' has been changed to '{}', '{}', '{}'", oldMimetype, oldSchema, oldEncoding, moduleClassName, newMimetype, newSchema, newEncoding);
        }
}