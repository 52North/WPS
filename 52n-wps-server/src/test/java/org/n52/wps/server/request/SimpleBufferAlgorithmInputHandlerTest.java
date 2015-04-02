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
package org.n52.wps.server.request;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.InputType;

import org.apache.xmlbeans.XmlException;
import org.geotools.feature.DefaultFeatureCollection;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.data.IData;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.webapp.api.ConfigurationManager;
import org.n52.wps.webapp.common.AbstractITClass;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 *
 * @author isuftin
 */
public class SimpleBufferAlgorithmInputHandlerTest extends AbstractITClass{

    private static String sampleFileName = null;
    private static File sampleFile = null;
    private static ExecuteDocument execDoc = null;
    private static InputType[] inputArray = null;

    @BeforeClass
    public static void setupClass() throws XmlException, IOException {
        sampleFileName = "src/test/resources/SimpleBufferAlgorithm.xml";
        sampleFile = new File(sampleFileName);

        execDoc = ExecuteDocument.Factory.parse(sampleFile);
        inputArray = execDoc.getExecute().getDataInputs().getInputArray();
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws XmlException, IOException {
		MockMvcBuilders.webAppContextSetup(this.wac).build();
		WPSConfig.getInstance().setConfigurationManager(this.wac.getBean(ConfigurationManager.class));
    }

    @After
    public void tearDown() {
    }

    @Test(expected = ExceptionReport.class)
    public void testInputHandlerInitializationWithIncorrectAlgorithmName() throws ExceptionReport {
        System.out.println("Testing testInputHandlerInitialization...");
        InputHandler instance = new InputHandler.Builder(new Input(inputArray), "this.algorithm.name.does.not.exist").build();
    }

    @Test(expected = ExceptionReport.class)
    public void testInputHandlerInitializationWithNullAlgorithmName() throws ExceptionReport {
        System.out.println("Testing testInputHandlerInitialization...");
        InputHandler instance = new InputHandler.Builder(new Input(inputArray), null).build();
    }

    @Test(expected = NullPointerException.class)
    public void testInputHandlerInitializationWithNullInputsArray() throws ExceptionReport {
        System.out.println("Testing testInputHandlerInitialization...");
        InputHandler instance = new InputHandler.Builder(null, "org.n52.wps.server.algorithm.SimpleBufferAlgorithm").build();
    }

    @Test
    public void testInputHandlerInitializationWithEmptyInputsArray() throws ExceptionReport {
        System.out.println("Testing testInputHandlerInitialization...");
        InputHandler instance = new InputHandler.Builder(new Input(new InputType[]{}), "org.n52.wps.server.algorithm.SimpleBufferAlgorithm").build();

        assertThat(instance, not(nullValue()));
        assertThat(instance.getParsedInputData().isEmpty(), is(true));
    }

    @Test
    public void testInputHandlerInitialization() throws ExceptionReport, XmlException, IOException {
        System.out.println("Testing testInputHandlerInitialization...");
        InputHandler instance = new InputHandler.Builder(new Input(inputArray), "org.n52.wps.server.algorithm.SimpleBufferAlgorithm").build();

        assertThat(instance, not(nullValue()));
    }

    @Test
    public void testGetParsedInputDataWithCorrectInput() throws ExceptionReport, XmlException, IOException {
        System.out.println("Testing testInputHandlerInitialization...");
        InputHandler instance = new InputHandler.Builder(new Input(inputArray), "org.n52.wps.server.algorithm.SimpleBufferAlgorithm").build();

        assertThat(instance.getParsedInputData().isEmpty(), is(false));
        assertThat(instance.getParsedInputData().size(), equalTo(2));
        assertThat(instance.getParsedInputData().keySet().size(), equalTo(2));
        assertThat(instance.getParsedInputData().keySet(), containsInAnyOrder("width", "data"));
        assertThat(instance.getParsedInputData().get("data").size(), equalTo(1));
        assertThat(instance.getParsedInputData().get("width").size(), equalTo(1));

        IData width = instance.getParsedInputData().get("width").get(0);
        IData data = instance.getParsedInputData().get("data").get(0);

        assertThat(data, is(notNullValue()));
        assertThat(data.getSupportedClass().getName(), is(equalToIgnoringCase("org.geotools.feature.FeatureCollection")));
        assertThat(data.getPayload(), is(notNullValue()));
        assertThat(((DefaultFeatureCollection) data.getPayload()).getID(), is(equalToIgnoringCase("featureCollection")));
        assertThat(((DefaultFeatureCollection) data.getPayload()).getSchema().getTypeName(), is(equalToIgnoringCase("tasmania_roads")));
        assertThat(((DefaultFeatureCollection) data.getPayload()).getSchema().getAttributeCount(), equalTo(7));
        assertThat(((DefaultFeatureCollection) data.getPayload()).fids().size(), equalTo(14));
        assertThat(((DefaultFeatureCollection) data.getPayload()).fids().toArray()[0].toString(), is(equalToIgnoringCase("tasmania_roads.1")));
        assertThat(((DefaultFeatureCollection) data.getPayload()).fids().toArray()[13].toString(), is(equalToIgnoringCase("tasmania_roads.9")));
        assertThat(((DefaultFeatureCollection) data.getPayload()).getBounds().toString(), is(equalToIgnoringCase("ReferencedEnvelope[145.19754 : 148.27298000000002, -43.423512 : -40.852802]")));
        assertThat(((DefaultFeatureCollection) data.getPayload()).getBounds().getArea(), equalTo(7.906064362400054d));
        assertThat(((DefaultFeatureCollection) data.getPayload()).getBounds().getDimension(), equalTo(2));

        assertThat(width, is(notNullValue()));
        assertThat(((Double)width.getPayload()), equalTo(20.0d));

    }
}
