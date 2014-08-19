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
package org.n52.wps.request;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.StatusType;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlValidationError;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.database.DatabaseFactory;
import org.n52.wps.server.request.ExecuteRequest;
import org.n52.wps.webapp.common.AbstractITClass;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author bpross-52n
 */
public class ExecuteRequestTest extends AbstractITClass {

    private DocumentBuilderFactory fac;

    @BeforeClass
    public static void setUpClass()
            throws XmlException, IOException {
//        WPSConfig.forceInitialization("src/test/resources/org/n52/wps/io/test/inputhandler/generator/wps_config.xml");
    }

	@Before
    public void setUp(){
		System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");

		fac = DocumentBuilderFactory.newInstance();
		fac.setNamespaceAware(true);
		MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

	@Test
    public void testUpdateStatusError() throws ExceptionReport, XmlException, IOException, SAXException, ParserConfigurationException {

		FileInputStream fis = new FileInputStream(new File("src/test/resources/LRDTCCorruptInputResponseDocStatusTrue.xml"));

		// parse the InputStream to create a Document
		Document doc = fac.newDocumentBuilder().parse(fis);

    	ExecuteRequest request = new ExecuteRequest(doc);

    	String exceptionText = "TestError";

    	request.updateStatusError(exceptionText);

    	File response = DatabaseFactory.getDatabase().lookupResponseAsFile(request.getUniqueId().toString());

    	ExecuteResponseDocument responseDoc = ExecuteResponseDocument.Factory.parse(response);

    	StatusType statusType = responseDoc.getExecuteResponse().getStatus();

    	assertTrue(validateExecuteResponse(responseDoc));
    	assertTrue(statusType.isSetProcessFailed());
    	assertTrue(statusType.getProcessFailed().getExceptionReport().getExceptionArray(0).getExceptionTextArray(0).equals(exceptionText));

    }

    private boolean validateExecuteResponse(ExecuteResponseDocument responseDoc) {
        XmlOptions xmlOptions = new XmlOptions();
        List<XmlValidationError> xmlValidationErrorList = new ArrayList<XmlValidationError>();
            xmlOptions.setErrorListener(xmlValidationErrorList);
        boolean valid = responseDoc.validate(xmlOptions);
        if (!valid) {
            System.err.println("Error validating process description for " + getClass().getCanonicalName());
            for (XmlValidationError xmlValidationError : xmlValidationErrorList) {
                System.err.println("\tMessage: " +  xmlValidationError.getMessage());
                System.err.println("\tLocation of invalid XML: " +
                     xmlValidationError.getCursorLocation().xmlText());
            }
        }
        return valid;
    }
}
