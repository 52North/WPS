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

import java.io.File;
import java.io.IOException;

import org.n52.wps.WPSConfigurationDocument;
import org.n52.wps.WPSConfigurationDocument.WPSConfiguration;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.impl.WPSConfigurationDocumentImpl.WPSConfigurationImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository(value = "configurationDAOXml")
public class ConfigurationDAOXml implements ConfigurationDAO {

	private static Logger LOGGER = LoggerFactory.getLogger(ConfigurationDAOXml.class);
	
	@Autowired
	WPSConfigurationImpl wpsConfigurationImpl;
	
	@Autowired
	private WPSConfigurationDocument wpsConfigurationDocument;
	
	@Autowired
	private WPSConfiguration wpsConfiguration;
	
	@Override
	public void save() {
		String path = WPSConfig.getConfigPath();
		try {
			wpsConfiguration.set(wpsConfigurationImpl);
			File XMLFile = new File(path);
	        wpsConfigurationDocument.save(XMLFile, new org.apache.xmlbeans.XmlOptions().setUseDefaultNamespace().setSavePrettyPrint());
	        WPSConfig.forceInitialization(path);
		} catch (IOException e) {
            LOGGER.error("Could not write configuration to file: "+ e.getMessage());
        } catch (org.apache.xmlbeans.XmlException e){
            LOGGER.error("Could not generate XML File from Data: " + e.getMessage());
        }
	}

}
