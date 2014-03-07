/**
 * ﻿Copyright (C) 2007
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
