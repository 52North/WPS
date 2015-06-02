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
package org.n52.wps.server.response;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.opengis.wps.x20.GetStatusDocument;
import net.opengis.wps.x20.StatusInfoDocument;

import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.Test;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.RepositoryManager;
import org.n52.wps.server.algorithm.test.StatusTestingProcess;
import org.n52.wps.server.database.DatabaseFactory;
import org.n52.wps.server.observerpattern.IObserver;
import org.n52.wps.server.observerpattern.ISubject;
import org.n52.wps.server.request.ExecuteRequestV200;
import org.n52.wps.server.request.GetStatusRequestV200;
import org.n52.wps.webapp.api.ConfigurationManager;
import org.n52.wps.webapp.common.AbstractITClass;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import static org.junit.Assert.assertTrue;

public class StatusTest extends AbstractITClass {

    private IAlgorithm algorithm;

    private DocumentBuilderFactory fac;

    @Before
    public void setUp() {
        MockMvcBuilders.webAppContextSetup(this.wac).build();
        WPSConfig.getInstance().setConfigurationManager(this.wac.getBean(ConfigurationManager.class));
        fac = DocumentBuilderFactory.newInstance();
        fac.setNamespaceAware(true);
        algorithm = new StatusTestingProcess();
    }

    @Test
    public void testGetStatusV200() {
        FileInputStream fis;
        try {
            fis = new FileInputStream(new File("src/test/resources/StatusTestingRequestV200.xml"));

            // parse the InputStream to create a Document
            Document doc = fac.newDocumentBuilder().parse(fis);

            final ExecuteRequestV200 executeRequestV200 = new ExecuteRequestV200(doc);

            final String requestID = executeRequestV200.getUniqueId().toString();

            algorithm = RepositoryManager.getInstance().getAlgorithm(executeRequestV200.getAlgorithmIdentifier());

            if (algorithm instanceof ISubject) {
                ISubject subject = (ISubject) algorithm;
                subject.addObserver(new IObserver() {

                    @Override
                    public void update(ISubject o) {
                        try {
                            StatusInfoDocument statusInfoDocument = StatusInfoDocument.Factory.parse(DatabaseFactory.getDatabase().lookupStatus(requestID));
                            assertTrue(statusInfoDocument.getStatusInfo().getStatus() != null);
                            
                        } catch (ExceptionReport | XmlException | IOException e) {
                            e.printStackTrace();
                        }

                    }
                });
            }

            executeRequestV200.call();

            GetStatusDocument statusDoc = GetStatusDocument.Factory.newInstance();

            statusDoc.addNewGetStatus().setJobID(requestID);

            GetStatusRequestV200 statusRequestV200 = new GetStatusRequestV200(fac.newDocumentBuilder().parse(statusDoc.newInputStream()));

            StatusInfoDocument statusInfoDocument = StatusInfoDocument.Factory.parse(statusRequestV200.call().getAsStream());

            System.out.println(statusInfoDocument.xmlText());

        } catch (SAXException | IOException | ParserConfigurationException | ExceptionReport | XmlException e) {
            e.printStackTrace();
        }
    }

}
