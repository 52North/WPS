/**
 * ﻿Copyright (C) 2010 - 2014 52°North Initiative for Geospatial Open Source
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

package org.n52.wps.server.r.syntax;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.r.R_Config;
import org.n52.wps.server.r.Util;
import org.n52.wps.server.r.data.RDataTypeRegistry;
import org.n52.wps.server.r.data.R_Resource;
import org.n52.wps.server.r.metadata.RAnnotationParser;
import org.springframework.test.util.ReflectionTestUtils;

public class AnnotationParser {

    private static R_Config config;

    private List<RAnnotation> annotations;

    private RAnnotationParser parser;

    @BeforeClass
    public static void initConfig() throws FileNotFoundException, XmlException, IOException {
        config = Util.getConfig();
        Util.mockGenericWPSConfig();
    }

    @Before
    public void loadAnnotations() throws IOException, RAnnotationException {
        File scriptFile = Util.loadFile("/uniform.R");

        FileInputStream fis = new FileInputStream(scriptFile);

        RAnnotationParser parser = new RAnnotationParser();
        ReflectionTestUtils.setField(parser, "config", config);

        this.annotations = parser.parseAnnotationsfromScript(fis);
        fis.close();
    }

    @Before
    public void loadParser() {
        this.parser = new RAnnotationParser();
        ReflectionTestUtils.setField(this.parser, "config", config);
        ReflectionTestUtils.setField(this.parser, "dataTypeRegistry", new RDataTypeRegistry());
    }

    @Test
    public void description() throws RAnnotationException {
        for (RAnnotation rAnnotation : this.annotations) {
            if (rAnnotation.getType().equals(RAnnotationType.DESCRIPTION)) {
                Assert.assertEquals("42", rAnnotation.getStringValue(RAttribute.VERSION));
                Assert.assertEquals("Random number generator", rAnnotation.getStringValue(RAttribute.TITLE));
                Assert.assertEquals("MC++", rAnnotation.getStringValue(RAttribute.AUTHOR));
                Assert.assertEquals("Generates random numbers with uniform distribution",
                                    rAnnotation.getStringValue(RAttribute.ABSTRACT));
                Assert.assertEquals("uniform", rAnnotation.getStringValue(RAttribute.IDENTIFIER));
            }
            else if (rAnnotation.getType().equals(RAnnotationType.OUTPUT)) {
                // output, text, Random number list,
                Assert.assertEquals("output", rAnnotation.getStringValue(RAttribute.IDENTIFIER));
                Assert.assertEquals("text", rAnnotation.getStringValue(RAttribute.TYPE));
                Assert.assertEquals("Random number list", rAnnotation.getStringValue(RAttribute.TITLE));
                Assert.assertEquals("Text file with list of n random numbers in one column",
                                    rAnnotation.getStringValue(RAttribute.ABSTRACT));
            }
            else if (rAnnotation.getType().equals(RAnnotationType.INPUT)) {
                String identifier = rAnnotation.getStringValue(RAttribute.IDENTIFIER);
                if ("n".equals(identifier)) {
                    Assert.assertEquals("integer", rAnnotation.getStringValue(RAttribute.TYPE));
                    Assert.assertEquals("amount of random numbers", rAnnotation.getStringValue(RAttribute.TITLE));
                    Assert.assertEquals("100", rAnnotation.getStringValue(RAttribute.DEFAULT_VALUE));
                    Assert.assertEquals("0", rAnnotation.getStringValue(RAttribute.MIN_OCCURS));
                }
            }
        }
    }

    @Test
    public void resource() throws RAnnotationException {
        int resourceAnnotationCounter = 0;
        Collection<String> foundResources = 
                new ArrayList<String>();
        for (RAnnotation rAnnotation : this.annotations) {
            if (rAnnotation.getType().equals(RAnnotationType.RESOURCE)) {
                resourceAnnotationCounter++;
                
                ResourceAnnotation resourceAnnotation = (ResourceAnnotation) rAnnotation;
                Collection<R_Resource> resources = resourceAnnotation.getResources();

                for (R_Resource resource : resources) {
                    foundResources.add(resource.getResourceValue());
                    assertThat("resources are all public", resource.isPublic(), is(equalTo(true)));
                }
            }
        }
        
        assertThat("two resource annotations are found", resourceAnnotationCounter, is(equalTo(2)));
        assertThat("all resources are found",
                   foundResources,
                   containsInAnyOrder("test.file.txt", "uniform.xml", "dir/folder"));
    }

    @Test
    public void validateValidAlgorithm() throws FileNotFoundException,
            RAnnotationException,
            IOException,
            ExceptionReport {
        File scriptFile = Util.loadFile("/uniform.R");

        try (FileInputStream fis = new FileInputStream(scriptFile);) {
            boolean b = parser.validateScript(fis, "test.id.only");
            assertThat("validation result is positive", b, is(equalTo(true)));
        }
    }

    @Test
    public void validAlgorithmReturnsNoErrorsDuringValidation() throws FileNotFoundException,
            RAnnotationException,
            IOException,
            ExceptionReport {
        File scriptFile = Util.loadFile("/uniform.R");

        try (FileInputStream fis = new FileInputStream(scriptFile);) {
            Collection<Object> b = parser.validateScriptWithErrors(fis, "test.id.only");
            assertThat("error list is empty", b, is(empty()));
        }
    }

    @Test
    public void validateInvalidAlgorithm() throws FileNotFoundException,
            RAnnotationException,
            IOException,
            ExceptionReport {
        String input = "# wps.d: id = R_andom, author = MC++, ;";

        try (InputStream is = IOUtils.toInputStream(input);) {
            boolean b = parser.validateScript(is, "test.id.only");
            assertThat("validation result is negative", b, is(equalTo(false)));
        }
    }

    @Test
    public void validateInvalidWithErrors() throws IOException, RAnnotationException {
        String input = "# wps.d: nothing";

        try (InputStream is = IOUtils.toInputStream(input);) {
            Collection<Object> errors = parser.validateScriptWithErrors(is, "test.id.only");
            assertThat("error list is not empty", errors, is(not(empty())));
            assertThat("error list contains one message", errors.size(), is(equalTo(1)));
            assertThat("first error elements is an annotation exception",
                       errors.iterator().next(),
                       is(instanceOf(RAnnotationException.class)));
        }
    }

}
