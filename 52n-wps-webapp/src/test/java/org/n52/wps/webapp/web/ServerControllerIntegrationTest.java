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

import org.junit.Before;
import org.junit.Test;
import org.n52.wps.webapp.common.AbstractITClassForControllerTests;
import org.n52.wps.webapp.entities.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class ServerControllerIntegrationTest extends AbstractITClassForControllerTests {

	private MockMvc mockMvc;

	@Autowired
	private Server server;
	
	@Before
	public void setup() {
		mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}

	@Test
	public void display() throws Exception {
		RequestBuilder builder = get("/server").accept(MediaType.TEXT_HTML);
		ResultActions result = this.mockMvc.perform(builder);
		result.andExpect(status().isOk()).andExpect(view().name("server"))
				.andExpect(model().attributeExists("configurationModule"));
	}

	@Test
	public void processPost_success() throws Exception {
		RequestBuilder request = post("/server").param("key", "hostname")
				.param("value", "testhost").param("module", server.getClass().getName());
		ResultActions result = this.mockMvc.perform(request);
		result.andExpect(status().isOk());
		assertEquals("testhost", server.getHostname());
		assertEquals("testhost", server.getConfigurationEntries().get(1).getValue());
	}

	@Test
	public void processPost_failure() throws Exception {
		RequestBuilder request = post("/server").param("key", "hostname")
				.param("value", "").param("module", server.getClass().getName());
		ResultActions result = this.mockMvc.perform(request);
		result.andExpect(status().isBadRequest());
	}
}
