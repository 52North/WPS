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

package org.n52.wps.webapp.util;

import java.io.IOException;

import javax.servlet.ServletContext;

import org.n52.wps.webapp.api.WPSConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.ServletContextResource;

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
	 * @return The absoulte path of a resource or a directory
	 * @throws WPSConfigurationException
	 *             if the path cannot be resolved
	 */
	public String getWebAppResourcePath(String relativePath) throws WPSConfigurationException {
		Resource resource = new ServletContextResource(servletContext, relativePath);
		try {
			String absolutePath = resource.getFile().getAbsolutePath();
			LOGGER.info("Resolved webapp resource'{}' to '{}'", relativePath, absolutePath);
			return absolutePath;
		} catch (IOException e) {
			LOGGER.error("Unable to resolve '{}' to a webapp resource path:", relativePath, e);
			throw new WPSConfigurationException(e);
		}
	}

	/**
	 * Returns the absolute path for classpath resources and directories.
	 * 
	 * @param relativePath
	 *            the relative path of a resource or a directory
	 * @return The absoulte path of a resource or a directory
	 * @throws WPSConfigurationException
	 *             if the path cannot be resolved
	 */
	public String getClassPathResourcePath(String relativePath) throws WPSConfigurationException {
		Resource resource = new ClassPathResource(relativePath);
		try {
			String absolutePath = resource.getFile().getAbsolutePath();
			LOGGER.info("Resolved classpath resource '{}' to '{}'", relativePath, absolutePath);
			return absolutePath;
		} catch (IOException e) {
			LOGGER.error("Unable to resolve '{}' to a calsspath resource:", relativePath, e);
			throw new WPSConfigurationException(e);
		}
	}

}
