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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.n52.wps.webapp.api.ConfigurationManager;
import org.n52.wps.webapp.api.ConfigurationModule;
import org.n52.wps.webapp.api.WPSConfigurationException;
import org.n52.wps.webapp.api.types.ConfigurationEntry;
import org.n52.wps.webapp.util.ResourcePathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * Handles upload requests and mapping.
 */
@Controller
public class UploadController {

	private final Logger LOGGER = LoggerFactory.getLogger(UploadController.class);

	@Autowired
	private ConfigurationManager configurationManager;

	@Autowired
	private ResourcePathUtil resourcePathUtil;

	/**
	 * Process all upload requests; forward to the appropriate method based on the type of file uploaded. The method
	 * will return an HTTP 200 status code if there are no errors, else, it will return a 400 status code.
	 * 
	 * @param request
	 * @param response
	 * @return A {@code ValidationResponse} object which contains the list of errors, if any.
	 */
	@RequestMapping(value = "upload", method = RequestMethod.POST)
	@ResponseBody
	public ValidationResponse upload(MultipartHttpServletRequest request, HttpServletResponse response) {

		ValidationResponse validationResponse = new ValidationResponse();
		List<FieldError> listOfErros = new ArrayList<FieldError>();
		validationResponse.setErrorMessageList(listOfErros);

		@SuppressWarnings("unused")
		MultipartFile file = null;

		if ((file = request.getFile("javaFile")) != null) {
			validationResponse = uploadProcess(request, response);
		} else if ((file = request.getFile("rScript")) != null) {
			validationResponse = uploadRScript(request, response);
		} else {
			// if nothing is uploaded, return an error status
			response.setStatus(400);
			FieldError error = new FieldError("alert", "alert", "Please select a file to upload.");
			listOfErros.add(error);
		}

		return validationResponse;
	}

	/*
	 * Handle the uploading of process and process description.
	 */
	private ValidationResponse uploadProcess(MultipartHttpServletRequest request, HttpServletResponse response) {
		ValidationResponse validationResponse = new ValidationResponse();
		List<FieldError> listOfErros = new ArrayList<FieldError>();
		validationResponse.setErrorMessageList(listOfErros);
		MultipartFile java = request.getFile("javaFile");
		MultipartFile xml = request.getFile("xmlFile");
		String savePath = null;

		if (java != null) {
			if (java.isEmpty() || !checkExtension(java, "java")) {
				response.setStatus(400);
				FieldError error = new FieldError(java.getOriginalFilename(), java.getName(),
						"Only Java source files are accepted.");
				listOfErros.add(error);
				return validationResponse;
			}
		}

		if (xml != null) {
			if (xml.isEmpty() || !checkExtension(xml, "xml")) {
				response.setStatus(400);
				FieldError error = new FieldError(xml.getOriginalFilename(), xml.getName(),
						"Only XML files are accepted.");
				listOfErros.add(error);
				return validationResponse;
			}
		}

		// save the java file and return the save location
		try {
			savePath = saveJava(java, request);
			saveXml(xml, savePath);
		} catch (IOException e) {
			response.setStatus(400);
			FieldError error = new FieldError("alert", "alert",
					"Unable to upload file due to IO error. Please check the log for the exception detials.");
			listOfErros.add(error);
			LOGGER.error("Unable to save Java file:", e);
		}

		return validationResponse;
	}

	/*
	 * Handle the uploading of RScript
	 */
	private ValidationResponse uploadRScript(MultipartHttpServletRequest request, HttpServletResponse response) {

		ValidationResponse validationResponse = new ValidationResponse();
		List<FieldError> listOfErros = new ArrayList<FieldError>();
		validationResponse.setErrorMessageList(listOfErros);

		MultipartFile rScript = request.getFile("rScript");
		// check if the file is not empty and the extension is correct
		if (!rScript.isEmpty() && checkExtension(rScript, "R")) {
			try {
				saveRScript(rScript, request);
			} catch (WPSConfigurationException e) {
				response.setStatus(400);
				FieldError error = new FieldError(
						"alert",
						"alert",
						"Unable to load the script directory for the LocalRAlgorithmRepository module. "
								+ "Please check that the module is loaded correctly and the configuration entry is set.");
				listOfErros.add(error);
				LOGGER.error("Unable to load script directory for LocalRAlgorithmRepository module: ", e);
			} catch (IOException e) {
				response.setStatus(400);
				FieldError error = new FieldError("alert", "alert",
						"Unable to upload file due to IO error. Please check the log for the exception detials.");
				listOfErros.add(error);
				LOGGER.error("Unable to save RScript:", e);
			}
		} else {
			response.setStatus(400);
			FieldError error = new FieldError(rScript.getOriginalFilename(), rScript.getName(),
					"Only R scripts are accepted.");
			listOfErros.add(error);
		}

		return validationResponse;
	}

	/*
	 * Check the extension of the passed file.
	 */
	private boolean checkExtension(MultipartFile file, String requiredExtension) {
		String extension = FilenameUtils.getExtension(file.getOriginalFilename());
		if (!extension.equals(requiredExtension)) {
			return false;
		}
		return true;
	}

	/*
	 * Write & save the Java file
	 */
	private String saveJava(MultipartFile java, HttpServletRequest request) throws IOException {
		LOGGER.debug("Trying to upload '{}'.", java.getOriginalFilename());

		// base directory
		StringBuilder directoryPath = new StringBuilder(
				resourcePathUtil.getWebAppResourcePath("/WEB-INF/classes/uploaded"));

		// try to get the package name from the file, read line by line
		File tempFile = new File(java.getOriginalFilename());
		String packageName = null;

		FileUtils.writeByteArrayToFile(tempFile, java.getBytes());
		List<String> lines = FileUtils.readLines(tempFile);

		// find the first line with the word package at it's beginning
		for (String line : lines) {
			if (line.indexOf("package") == 0) {
				packageName = line.replace("package", "").replace(";", "").trim();
				break;
			}
		}

		// create a subdirectory for each package
		if (packageName != null && !packageName.isEmpty()) {
			String[] splitNames = packageName.split("\\.");
			for (String subdirectory : splitNames) {
				directoryPath.append("/" + subdirectory);
			}
		}

		// copy the file to the final directory
		FileUtils.copyFileToDirectory(tempFile, new File(directoryPath.toString()));
		LOGGER.info("Uploaded file saved in '{}'.", directoryPath.toString());
		tempFile.delete();
		return directoryPath.toString();
	}

	/*
	 * Write & save the XML file
	 */
	private void saveXml(MultipartFile xml, String savePath) throws IOException {
		if (xml != null) {
			LOGGER.debug("Trying to upload '{}'.", xml.getOriginalFilename());
			String xmlPath = savePath + "/" + xml.getOriginalFilename();
			if (xml != null) {
				FileUtils.writeByteArrayToFile(new File(xmlPath), xml.getBytes());
			}
			LOGGER.info("Uploaded file saved in '{}'.", xmlPath);
		}
	}

	/*
	 * Write & save the R file
	 */
	private void saveRScript(MultipartFile rScript, HttpServletRequest request) throws WPSConfigurationException,
			IOException {
		LOGGER.debug("Trying to upload '{}'.", rScript.getOriginalFilename());

		// check if the user entered a process name
		String processName = null;
		if (request.getParameter("rScriptProcessName") != null
				&& !request.getParameter("rScriptProcessName").trim().isEmpty()) {
			processName = request.getParameter("rScriptProcessName").trim();
		}

		// set the filename
		String fileName = null;
		if (processName != null) {
			fileName = processName + ".R";
		} else {
			fileName = rScript.getOriginalFilename();
		}

		/*
		 * try to get the directory path for the R module. First get the configuration module, and then try to get the
		 * entry with the directory path
		 */
		String directoryPath = null;

		/*
		 * if the module or the entry are null, catch and rethrow to alert the user that the LocalRAlgorithmRepository
		 * module is not loaded
		 */
		try {
			ConfigurationModule module = configurationManager.getConfigurationServices().getConfigurationModule(
					"org.n52.wps.server.r.LocalRAlgorithmRepository");
			ConfigurationEntry<?> entry = configurationManager.getConfigurationServices().getConfigurationEntry(module,
					"Script_Dir");
			String scriptDirectory = configurationManager.getConfigurationServices().getConfigurationEntryValue(module,
					entry, String.class);
			directoryPath = resourcePathUtil.getWebAppResourcePath(scriptDirectory);
		} catch (NullPointerException ex) {
			throw new WPSConfigurationException(ex);
		}

		String rScriptPath = directoryPath + "/" + fileName;
		File file = new File(rScriptPath);
		FileUtils.writeByteArrayToFile(file, rScript.getBytes());

		LOGGER.info("Uploaded file saved in '{}'.", rScriptPath);
	}
}
