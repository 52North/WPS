/**
 * ﻿Copyright (C) 2007 - 2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
