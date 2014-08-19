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
package org.n52.wps.webapp.util;

import java.io.IOException;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.ServletContextResource;
/**
 * Return the absolute path for the supplied relative web or class path.
 */
@Component
public class ResourcePathUtil {

	@Autowired
	private ServletContext servletContext;

	private static Logger LOGGER = LoggerFactory.getLogger(ResourcePathUtil.class);

	/**
	 * Returns the absolute path for web app resources and directories.
	 * 
	 * @param relativePath
	 *            the relative path of a resource or a directory
	 * @return The absolute path of a resource or a directory
	 * @throws RuntimeException
	 *             if the path cannot be resolved
	 */
	public String getWebAppResourcePath(String relativePath) {
		Resource resource = new ServletContextResource(servletContext, relativePath);
		try {
			String absolutePath = resource.getFile().getAbsolutePath();
			LOGGER.info("Resolved webapp resource'{}' to '{}'", relativePath, absolutePath);
			return absolutePath;
		} catch (IOException e) {
			throw new RuntimeException("Unable to resolve '{" + relativePath + "}' to a webapp resource:", e);
		}
	}

	/**
	 * Returns the absolute path for classpath resources and directories.
	 * 
	 * @param relativePath
	 *            the relative path of a resource or a directory
	 * @return The absolute path of a resource or a directory
	 * @throws RuntimeException
	 *             if the path cannot be resolved
	 */
	public String getClassPathResourcePath(String relativePath) {
		Resource resource = new ClassPathResource(relativePath);
		try {
			String absolutePath = resource.getFile().getAbsolutePath();
			LOGGER.info("Resolved classpath resource '{}' to '{}'", relativePath, absolutePath);
			return absolutePath;
		} catch (IOException e) {
			throw new RuntimeException("Unable to resolve '{" + relativePath + "}' to a calsspath resource:", e);
		}
	}

}
