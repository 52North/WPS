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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.Before;
import org.junit.Test;
import org.n52.wps.webapp.common.AbstractITClass;
import org.n52.wps.webapp.testmodules.TestConfigurationModule1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class RepositoriesControllerIntegrationTest extends AbstractITClass {

	private MockMvc mockMvc;

	@Autowired
	private TestConfigurationModule1 module1;

	@Before
	public void setup() {
		mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}

	@Test
	public void displayRepositories() throws Exception {
		RequestBuilder builder = get("/repositories").accept(MediaType.TEXT_HTML);
		ResultActions result = this.mockMvc.perform(builder);
		result.andExpect(status().isOk()).andExpect(view().name("repositories"))
				.andExpect(model().attributeExists("configurations"));
	}

	@Test
	public void processPost_success() throws Exception {
		RequestBuilder request = post("/repositories").param("key", "test.string.key")
				.param("value", "new posted value").param("module", module1.getClass().getName());
		ResultActions result = this.mockMvc.perform(request);
		result.andExpect(status().isOk());
		assertEquals("new posted value", module1.getStringMember());
		assertEquals("new posted value", module1.getConfigurationEntries().get(0).getValue());
	}

	@Test
	public void processPost_failure() throws Exception {
		RequestBuilder request = post("/repositories").param("key", "test.integer.key")
				.param("value", "invalid integer").param("module", module1.getClass().getName());
		ResultActions result = this.mockMvc.perform(request);
		result.andExpect(status().isBadRequest());
	}

	@Test
	public void toggleModuleStatus() throws Exception {
		assertTrue(module1.isActive());
		RequestBuilder request = post("/repositories/activate/{moduleClassName}/false", module1.getClass().getName());
		ResultActions result = this.mockMvc.perform(request);
		result.andExpect(status().isOk());
		assertFalse(module1.isActive());
	}

	@Test
	public void toggleAlgorithmStatus() throws Exception {
		assertTrue(module1.getAlgorithmEntries().get(0).isActive());
		RequestBuilder request = post("/repositories/algorithms/activate/{moduleClassName}/{algorithm}/false",
				module1.getClass().getName(), module1.getAlgorithmEntries().get(0).getAlgorithm());
		ResultActions result = this.mockMvc.perform(request);
		result.andExpect(status().isOk());
		assertFalse(module1.getAlgorithmEntries().get(0).isActive());
	}
}
