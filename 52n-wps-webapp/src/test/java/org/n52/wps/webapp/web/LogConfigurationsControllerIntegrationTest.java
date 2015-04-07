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

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.jdom.Document;
import org.junit.Before;
import org.junit.Test;
import org.n52.wps.webapp.api.ConfigurationManager;
import org.n52.wps.webapp.common.AbstractITClassForControllerTests;
import org.n52.wps.webapp.dao.XmlLogConfigurationsDAO;
import org.n52.wps.webapp.entities.LogConfigurations;
import org.n52.wps.webapp.util.JDomUtil;
import org.n52.wps.webapp.util.ResourcePathUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class LogConfigurationsControllerIntegrationTest extends AbstractITClassForControllerTests {

	private MockMvc mockMvc;

	@Autowired
	ConfigurationManager configurationManager;
	
	@Autowired
	private JDomUtil jDomUtil;
	
	@Autowired
	private ResourcePathUtil resourcePathUtil;
	
	@Before
	public void setup() {
		mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}

	@Test
	public void display() throws Exception {
		RequestBuilder builder = get("/log").accept(MediaType.TEXT_HTML);
		ResultActions result = this.mockMvc.perform(builder);
		result.andExpect(status().isOk()).andExpect(view().name("log"))
				.andExpect(model().attributeExists("logConfigurations"));
	}

	@Test
	public void processPost_success() throws Exception {
		String path = resourcePathUtil.getClassPathResourcePath(XmlLogConfigurationsDAO.FILE_NAME);
		Document originalDoc = jDomUtil.parse(path);
		
		RequestBuilder request = post("/log")
				.param("wpsfileAppenderFileNamePattern", "testFileAppenderFileNamePattern")
		.param("wpsfileAppenderEncoderPattern", "testFileAppenderFileNamePattern")
		.param("wpsconsoleEncoderPattern", "testFileAppenderFileNamePattern")
		.param("wpsfileAppenderMaxHistory", "10")
		.param("rootLevel", "DEBUG")
		.param("fileAppenderEnabled", "true")
		.param("consoleAppenderEnabled", "true")
		.param("loggers['org.apache.axiom']", "ERROR")
		.param("loggers['org.apache.http.wire']", "OFF");
		ResultActions result = this.mockMvc.perform(request);
		result.andExpect(status().isOk());
		LogConfigurations logConfigurations = configurationManager.getLogConfigurationsServices().getLogConfigurations();
		assertEquals("testFileAppenderFileNamePattern", logConfigurations.getWpsfileAppenderFileNamePattern());
		
		//reset document to original state
		jDomUtil.write(originalDoc, path);
	}

	@Test
	public void processPost_failure() throws Exception {
		RequestBuilder request = post("/log").param("wpsfileAppenderFileNamePattern", "");
		ResultActions result = this.mockMvc.perform(request);
		result.andExpect(status().isBadRequest());
	}
}
