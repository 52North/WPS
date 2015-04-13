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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import net.opengis.wps.x100.ComplexDataDescriptionType;
import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.InputType;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.xmlbeans.XmlException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.RepositoryManager;
import org.n52.wps.server.handler.DataInputInterceptors.InterceptorInstance;
import org.n52.wps.util.XMLBeansHelper;

/**
 *
 * @author isuftin
 */
public class InputHandlerTest {

    private static File simpleBufferAlgorithmFile = null;
    private static File dummyTestClassAlgorithmFile = null;
    private static ExecuteDocument simpleBufferAlgorithmExecDoc = null;
    private static ExecuteDocument dummyTestClassAlgorithmExecDoc = null;
    private static InputType[] simpleBufferAlgorithmInputArray = null;
    private static InputType[] dummyTestClassAlgorithmInputArray = null;

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws XmlException, IOException {
        simpleBufferAlgorithmFile = new File("src/test/resources/SimpleBufferAlgorithm.xml");
        simpleBufferAlgorithmExecDoc = ExecuteDocument.Factory.parse(simpleBufferAlgorithmFile);
        simpleBufferAlgorithmInputArray = simpleBufferAlgorithmExecDoc.getExecute().getDataInputs().getInputArray();

        dummyTestClassAlgorithmFile = new File("src/test/resources/DummyTestClass.xml");
        dummyTestClassAlgorithmExecDoc = ExecuteDocument.Factory.parse(dummyTestClassAlgorithmFile);
        dummyTestClassAlgorithmInputArray = dummyTestClassAlgorithmExecDoc.getExecute().getDataInputs().getInputArray();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testInputHandlerInitialization() throws ExceptionReport, XmlException, IOException {
        System.out.println("Testing testInputHandlerInitialization...");
        InputHandler instance = new InputHandler.Builder(new Input(simpleBufferAlgorithmInputArray), "org.n52.wps.server.algorithm.SimpleBufferAlgorithm").build();

        assertThat(instance, not(nullValue()));
    }

    @Test
    public void testInputHandlerResolveInputInterceptors() throws ExceptionReport, XmlException, IOException {
        System.out.println("Testing testInputHandlerResolveInputInterceptors...");
        InputHandler instance = new InputHandler.Builder(new Input(simpleBufferAlgorithmInputArray), "org.n52.wps.server.algorithm.SimpleBufferAlgorithm").build();

        Map<String, InterceptorInstance> resolveInputInterceptors = instance.resolveInputInterceptors("org.n52.wps.server.algorithm.SimpleBufferAlgorithm");
        assertThat(resolveInputInterceptors.size(), equalTo(0));

        instance = new InputHandler.Builder(new Input(dummyTestClassAlgorithmInputArray), "org.n52.wps.server.algorithm.test.DummyTestClass").build();
        resolveInputInterceptors = instance.resolveInputInterceptors("org.n52.wps.server.algorithm.SimpleBufferAlgorithm");
        assertThat(resolveInputInterceptors.size(), equalTo(0));
    }

    @Test
    public void testInputHandlerResolveInputDescriptionTypes() throws ExceptionReport, XmlException, IOException {
        System.out.println("Testing testInputHandlerResolveInputDescriptionTypes...");

        new InputHandler.Builder(new Input(simpleBufferAlgorithmInputArray), "org.n52.wps.server.algorithm.SimpleBufferAlgorithm").build();
        
        ProcessDescriptionType processDescriptionType = (ProcessDescriptionType) RepositoryManager.getInstance().getProcessDescription("org.n52.wps.server.algorithm.SimpleBufferAlgorithm").getProcessDescriptionType(WPSConfig.VERSION_100);
                
        InputDescriptionType idt = XMLBeansHelper.findInputByID("data", processDescriptionType.getDataInputs());
        assertThat(idt, is(notNullValue()));
        assertThat(idt.getMaxOccurs().intValue(), equalTo(1));
        assertThat(idt.getMinOccurs().intValue(), equalTo(1));

        new InputHandler.Builder(new Input(dummyTestClassAlgorithmInputArray), "org.n52.wps.server.algorithm.test.DummyTestClass").build();

        processDescriptionType = (ProcessDescriptionType) RepositoryManager.getInstance().getProcessDescription("org.n52.wps.server.algorithm.test.DummyTestClass").getProcessDescriptionType(WPSConfig.VERSION_100);
                
        idt = XMLBeansHelper.findInputByID("BBOXInputData", processDescriptionType.getDataInputs());
        assertThat(idt, is(notNullValue()));
        assertThat(idt.getMaxOccurs().intValue(), equalTo(1));
        assertThat(idt.getMinOccurs().intValue(), equalTo(0));
    }

    @Test
    public void testInputHandlerGetNonDefaultFormat() throws ExceptionReport, XmlException, IOException {
        System.out.println("Testing testInputHandlerGetNonDefaultFormat...");

        InputHandler instance = new InputHandler.Builder(new Input(simpleBufferAlgorithmInputArray), "org.n52.wps.server.algorithm.SimpleBufferAlgorithm").build();
        ProcessDescriptionType processDescriptionType = (ProcessDescriptionType) RepositoryManager.getInstance().getProcessDescription("org.n52.wps.server.algorithm.SimpleBufferAlgorithm").getProcessDescriptionType(WPSConfig.VERSION_100);
        
        InputDescriptionType idt = XMLBeansHelper.findInputByID("data", processDescriptionType.getDataInputs());
        String dataMimeType = "text/xml; subtype=gml/3.1.0";
        String dataSchema = "http://schemas.opengis.net/gml/3.1.0/base/feature.xsd";
        String dataEncoding = null;
        ComplexDataDescriptionType cddt = instance.getNonDefaultFormat(idt, dataMimeType, dataSchema, dataEncoding);
        assertThat(cddt, is(notNullValue()));
        assertThat(cddt.getEncoding(), is(nullValue()));
        assertThat(cddt.getMimeType(), is(equalTo("text/xml; subtype=gml/3.1.0")));
        assertThat(cddt.getSchema(), is(equalTo("http://schemas.opengis.net/gml/3.1.0/base/feature.xsd")));

        instance = new InputHandler.Builder(new Input(dummyTestClassAlgorithmInputArray), "org.n52.wps.server.algorithm.test.DummyTestClass").build();
        processDescriptionType = (ProcessDescriptionType) RepositoryManager.getInstance().getProcessDescription("org.n52.wps.server.algorithm.test.DummyTestClass").getProcessDescriptionType(WPSConfig.VERSION_100);
        
        idt = XMLBeansHelper.findInputByID("BBOXInputData", processDescriptionType.getDataInputs());
        cddt = instance.getNonDefaultFormat(idt, dataMimeType, dataSchema, dataEncoding);
        assertThat(cddt, is(nullValue()));
    }
    
    @Test
    public void testInputHandlerGetComplexValueNodeString() throws ExceptionReport, XmlException, IOException {
        System.out.println("Testing testInputHandlerGetComplexValueNodeString...");

        InputHandler instance = new InputHandler.Builder(new Input(simpleBufferAlgorithmInputArray), "org.n52.wps.server.algorithm.SimpleBufferAlgorithm").build();
        String result = instance.getComplexValueNodeString(simpleBufferAlgorithmInputArray[0].getDomNode());
        assertThat(result, not(isEmptyOrNullString()));
        assertThat(result, containsString("ows:Identifier"));
        assertThat(result, containsString("147.25674400000003 -42.778393 147.22018400000002 -42.824776 147.179596 -42.82143 147.11132800000001 -42.795731 147.057098 -42.741581 147.00347900000003 -42.704803 146.91909800000002 -42.622734 146.91053799999997 -42.610928 146.88998400000003 -42.585396 146.83844 -42.572792 146.78569 -42.539352 146.724335 -42.485966 146.695023 -42.469582 146.64987200000002 -42.450371 146.604965 -42.432274 146.578781 -42.408531 146.539307 -42.364208 146.525055 -42.30883 146.558044 -42.275948 146.57624800000002 -42.23777 146.58146699999998 -42.203426 146.490005 -42.180222 146.3797 -42.146332 146.33406100000002 -42.138741 146.270966 -42.165703 146.197296 -42.224072 146.167908 -42.244835 146.16493200000002 -42.245171 146.111023 -42.265202 146.03747600000003 -42.239738 145.981628 -42.187851 145.85391199999998 -42.133492 145.819611 -42.129154 145.72052000000002 -42.104084 145.61857600000002 -42.056023 145.541718 -42.027241 145.48628200000002 -41.983326 145.452744 -41.926544 145.494034 -41.896477 145.59173600000003 -41.860214 145.64211999999998 -41.838398 145.669449 -41.830734 145.680923 -41.795753 145.68296800000002 -41.743221 145.67515600000002 -41.710377 145.680115 -41.688908 145.70106500000003 -41.648228 145.71479799999997 -41.609509 145.62919599999998 -41.462051 145.64889499999998 -41.470337 145.633423 -41.420902 145.631866 -41.36528 145.640854 -41.301533 145.700424 -41.242611 145.77242999999999 -41.193897 145.80233800000002 -41.161488 145.856018 -41.08007"));

        instance = new InputHandler.Builder(new Input(dummyTestClassAlgorithmInputArray), "org.n52.wps.server.algorithm.test.DummyTestClass").build();
        result = instance.getComplexValueNodeString(dummyTestClassAlgorithmInputArray[0].getDomNode());
        assertThat(result, not(isEmptyOrNullString()));
        assertThat(result, containsString("ows:Identifier"));
        assertThat(result, containsString("46.75 13.05"));

    }
}
