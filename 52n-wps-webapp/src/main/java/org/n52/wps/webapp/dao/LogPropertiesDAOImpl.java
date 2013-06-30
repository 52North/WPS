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

package org.n52.wps.webapp.dao;

import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

@Repository
public class LogPropertiesDAOImpl implements LogPropertiesDAO {

	public static String FILE_NAME = "log4j.properties";

	private static Logger LOGGER = Logger.getLogger(LogPropertiesDAOImpl.class);

	@Override
	public PropertiesConfiguration load() {
		PropertiesConfiguration properties = new PropertiesConfiguration();
		try {
			Resource resource = new ClassPathResource(FILE_NAME);
			properties.load(resource.getFile());
			LOGGER.debug("Loaded: " + properties.getPath());
		} catch (ConfigurationException e) {
			LOGGER.error("Cannot load: " + FILE_NAME + ": " + e.getMessage());
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}
		return properties;
	}

	@Override
	public void save(PropertiesConfiguration properties) {
		try {
			properties.save(FILE_NAME);
			LOGGER.debug("Saved file: " + properties.getPath());
		} catch (ConfigurationException e) {
			LOGGER.error("Cannot write to: " + FILE_NAME + ": " + e.getMessage());
		}
	}

}
