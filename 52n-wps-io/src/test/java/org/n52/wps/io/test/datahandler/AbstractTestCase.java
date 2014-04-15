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
package org.n52.wps.io.test.datahandler;
import java.io.File;
import java.io.IOException;

import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.n52.wps.commons.WPSConfig;
import org.n52.wps.commons.WPSConfigTestUtil;
import org.n52.wps.io.AbstractIOHandler;

import junit.framework.TestCase;

public abstract class AbstractTestCase<T  extends AbstractIOHandler> extends TestCase {

	protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractTestCase.class);

	protected String projectRoot;

	protected T dataHandler;

	public AbstractTestCase() {
        try {
            File f = new File(this.getClass().getProtectionDomain().getCodeSource()
                    .getLocation().getFile());

            projectRoot = f.getParentFile().getParentFile().getParent();
            WPSConfigTestUtil.generateMockConfig(getClass(), "/org/n52/wps/io/test/datahandler/generator/wps_config.xml");
            initializeDataHandler();
        } catch (XmlException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

    }

	protected boolean isDataHandlerActive(){

		if(dataHandler == null){
			LOGGER.info("Data handler not initialized in test class " + this.getClass().getName());
			return false;
		}

		String className = dataHandler.getClass().getName();

		if(!(WPSConfig.getInstance().isGeneratorActive(className)||WPSConfig.getInstance().isParserActive(className))){
			LOGGER.info("Skipping inactive data handler: " + className);
			return false;
		}
		return true;
	}

	protected abstract void initializeDataHandler();

}
