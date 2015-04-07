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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.io.File;
import java.io.FileInputStream;

import org.junit.Before;
import org.junit.Test;
import org.n52.wps.webapp.common.AbstractITClassForControllerTests;
import org.n52.wps.webapp.util.ResourcePathUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

public class UploadControllerIntegrationTest extends AbstractITClassForControllerTests {

	private MockMvc mockMvc;

	@Autowired
	private UploadController uploadController;

	@Autowired
	private ResourcePathUtil resourcePathUtil;

	@Before
	public void setup() {
		mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}

	@Test
	public void display() throws Exception {
		RequestBuilder builder = get("/backup").accept(MediaType.TEXT_HTML);
		ResultActions result = this.mockMvc.perform(builder);
		result.andExpect(status().isOk()).andExpect(view().name("backup"));
	}

	@Test
	public void uploadProcess_validJavaFile_validXmlFile_success() throws Exception {
		MockMultipartHttpServletRequest multiRequest = new MockMultipartHttpServletRequest();
		MultipartFile javaFile = getValidJavaFile();
		MultipartFile xmlFile = getValidXmlFile();
		multiRequest.addFile(javaFile);
		multiRequest.addFile(xmlFile);
		multiRequest.setRequestURI("/upload");
		multiRequest.setSession(new MockHttpSession(new MockServletContext()));
		uploadController.upload(multiRequest, new MockHttpServletResponse());

		String uploadDirectory = resourcePathUtil.getWebAppResourcePath("/WEB-INF/classes");
		assertTrue(new File(uploadDirectory + "/uploaded/org/n52/wps/server/algorithm/convexhull/SampleJava.java")
				.exists());
		assertTrue(new File(uploadDirectory + "/uploaded/org/n52/wps/server/algorithm/convexhull/SampleXml.xml")
				.exists());

		// Clean up
		deleteFolder(new File(uploadDirectory));
	}

	@Test
	public void uploadProcess_validJavaFile_noXmlFile_success() throws Exception {
		MockMultipartHttpServletRequest multiRequest = new MockMultipartHttpServletRequest();
		MultipartFile javaFile = getValidJavaFile();
		multiRequest.addFile(javaFile);
		multiRequest.setRequestURI("/upload");
		multiRequest.setSession(new MockHttpSession(new MockServletContext()));
		uploadController.upload(multiRequest, new MockHttpServletResponse());

		String uploadDirectory = resourcePathUtil.getWebAppResourcePath("/WEB-INF/classes");
		assertTrue(new File(uploadDirectory + "/uploaded/org/n52/wps/server/algorithm/convexhull/SampleJava.java")
				.exists());
		assertFalse(new File(uploadDirectory + "/uploaded/org/n52/wps/server/algorithm/convexhull/SampleXml.xml")
				.exists());

		// Clean up
		deleteFolder(new File(uploadDirectory));
	}

	@Test
	public void uploadProcess_validJavaFile_invalidXmlFile_noUpload() throws Exception {
		MockMultipartHttpServletRequest multiRequest = new MockMultipartHttpServletRequest();
		MultipartFile javaFile = getValidJavaFile();
		MultipartFile xmlFile = getInvalidXmlFile();
		multiRequest.addFile(javaFile);
		multiRequest.addFile(xmlFile);
		multiRequest.setRequestURI("/upload");
		multiRequest.setSession(new MockHttpSession(new MockServletContext()));
		uploadController.upload(multiRequest, new MockHttpServletResponse());

		String uploadDirectory = resourcePathUtil.getWebAppResourcePath("/WEB-INF/classes");
		assertFalse(new File(uploadDirectory + "/uploaded/org/n52/wps/server/algorithm/convexhull/SampleJava.java")
				.exists());
		assertFalse(new File(uploadDirectory + "/uploaded/org/n52/wps/server/algorithm/convexhull/SampleXml.xml")
				.exists());
	}

	@Test
	public void uploadProcess_invalidJaveFile_noUpload() throws Exception {
		MockMultipartHttpServletRequest multiRequest = new MockMultipartHttpServletRequest();
		MultipartFile javaFile = getInvalidJavaFile();
		multiRequest.addFile(javaFile);
		multiRequest.setRequestURI("/upload");
		multiRequest.setSession(new MockHttpSession(new MockServletContext()));
		uploadController.upload(multiRequest, new MockHttpServletResponse());

		String uploadDirectory = resourcePathUtil.getWebAppResourcePath("/WEB-INF/classes");
		assertFalse(new File(uploadDirectory + "/uploaded/org/n52/wps/server/algorithm/convexhull/SampleJava.java")
				.exists());
		assertFalse(new File(uploadDirectory + "/uploaded/org/n52/wps/server/algorithm/convexhull/SampleXml.xml")
				.exists());
	}

	@Test
	public void uploadScript_validRScript_withProcessName_success() throws Exception {
		MockMultipartHttpServletRequest multiRequest = new MockMultipartHttpServletRequest();
		MultipartFile rScript = getValidRScriptFile();
		multiRequest.addFile(rScript);
		multiRequest.addParameter("rScriptProcessName", "UserProcessName");
		multiRequest.setRequestURI("/upload");
		multiRequest.setSession(new MockHttpSession(new MockServletContext()));
		uploadController.upload(multiRequest, new MockHttpServletResponse());

		/*
		 * Currently, the test is disabled. An R module is needed to assert the upload. The function will throw a
		 * NullPointerException because it cannot find the LocalRAlgorithmRepository
		 */

		// String uploadDirectory = resourcePathUtil.getWebAppResourcePath("/R");
		// assertTrue(new File(uploadDirectory + "/scripts/UserProcessName.R").exists());

		// Clean up
		// deleteFoler(uploadDirectory + "/scripts/SampleRScript.R");
	}

	@Test
	public void uploadScript_validRScript_withoutProcessName_success() throws Exception {
		MockMultipartHttpServletRequest multiRequest = new MockMultipartHttpServletRequest();
		MultipartFile rScript = getValidRScriptFile();
		multiRequest.addFile(rScript);
		multiRequest.setRequestURI("/upload");
		multiRequest.setSession(new MockHttpSession(new MockServletContext()));
		uploadController.upload(multiRequest, new MockHttpServletResponse());

		/*
		 * Currently, the test is disabled. An R module is needed to assert the upload. The function will throw a
		 * NullPointerException because it cannot find the LocalRAlgorithmRepository
		 */

		// String uploadDirectory = resourcePathUtil.getWebAppResourcePath("/R");
		// assertTrue(new File(uploadDirectory + "/scripts/SampleRScript.R").exists());

		// Clean up
		// deleteFoler(uploadDirectory + "/scripts/SampleRScript.R");
	}

	@Test
	public void uploadScript_invalidRScript_noUpload() throws Exception {
		MockMultipartHttpServletRequest multiRequest = new MockMultipartHttpServletRequest();
		MultipartFile rScript = getInvalidRScriptFile();
		multiRequest.addFile(rScript);
		multiRequest.setRequestURI("/upload");
		multiRequest.setSession(new MockHttpSession(new MockServletContext()));
		uploadController.upload(multiRequest, new MockHttpServletResponse());

		/*
		 * Currently, the test is disabled. An R module is needed to assert the upload. The function will throw a
		 * NullPointerException because it cannot find the LocalRAlgorithmRepository
		 */

		// String uploadDirectory = resourcePathUtil.getWebAppResourcePath("/R");
		// assertFalse(new File(uploadDirectory + "/scripts/SampleRScript.R").exists());
	}

	private MockMultipartFile getValidJavaFile() throws Exception {
		File file = new File("src/test/resources/testfiles/uploadtests/SampleJava.java");
		return new MockMultipartFile("javaFile", file.getName(), null, new FileInputStream(file));
	}

	private MockMultipartFile getInvalidJavaFile() throws Exception {
		File file = new File("src/test/resources/testfiles/uploadtests/InvalidFormat.gif");
		return new MockMultipartFile("javaFile", file.getName(), null, new FileInputStream(file));
	}

	private MockMultipartFile getValidXmlFile() throws Exception {
		File file = new File("src/test/resources/testfiles/uploadtests/SampleXml.xml");
		return new MockMultipartFile("xmlFile", file.getName(), null, new FileInputStream(file));
	}

	private MockMultipartFile getInvalidXmlFile() throws Exception {
		File file = new File("src/test/resources/testfiles/uploadtests/InvalidFormat.gif");
		return new MockMultipartFile("xmlFile", file.getName(), null, new FileInputStream(file));
	}

	private MockMultipartFile getValidRScriptFile() throws Exception {
		File file = new File("src/test/resources/testfiles/uploadtests/SampleRScript.R");
		return new MockMultipartFile("rScript", file.getName(), null, new FileInputStream(file));
	}

	private MockMultipartFile getInvalidRScriptFile() throws Exception {
		File file = new File("src/test/resources/testfiles/uploadtests/InvalidFormat.gif");
		return new MockMultipartFile("rScript", file.getName(), null, new FileInputStream(file));
	}

	private void deleteFolder(File folder) {
		for (File file : folder.listFiles()) {
			if (file.isDirectory()) {
				deleteFolder(file);
			}
			file.delete();
		}
		folder.delete();
	}
}
