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
package org.n52.wps.server.r;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionsDocument;

import org.apache.xmlbeans.XmlException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.r.metadata.RAnnotationParser;
import org.n52.wps.server.r.metadata.RProcessDescriptionCreator;
import org.n52.wps.server.r.syntax.RAnnotation;
import org.n52.wps.server.r.syntax.RAnnotationException;
import org.n52.wps.server.r.syntax.RAnnotationType;
import org.n52.wps.server.r.syntax.RAttribute;

public class DescriptionCreator {

    private static R_Config config;
    private List<RAnnotation> annotations;

    @BeforeClass
    public static void initConfig() {
        config = R_Config.getInstance();
    }

    @Before
    public void loadAnnotations() throws IOException, RAnnotationException
    {
        File scriptFile = Util.loadFile("/uniform.R");

        // GenericRProcess process = new GenericRProcess("R_andom");
        FileInputStream fis = new FileInputStream(scriptFile);
        RAnnotationParser parser = new RAnnotationParser(config);
        this.annotations = parser.parseAnnotationsfromScript(fis);
        fis.close();
    }

    @Test
    public void uniform() throws ExceptionReport, RAnnotationException, IOException, XmlException
    {
        File descriptionFile = Util.loadFile("/uniform.xml");

        // GenericRProcess process = new GenericRProcess("R_andom");
        FileInputStream fis = new FileInputStream(descriptionFile);
        RProcessDescriptionCreator creator = new RProcessDescriptionCreator(config);
        ProcessDescriptionType testType = creator.createDescribeProcessType(this.annotations,
                                                                            "R_andom",
                                                                            new URL("http://my.url/myScript.R"),
                                                                            new URL("http://my.url/to_the_session_info"));
        ProcessDescriptionsDocument testDoc = ProcessDescriptionsDocument.Factory.newInstance();
        testDoc.addNewProcessDescriptions().addNewProcessDescription().set(testType);
        // System.out.println(testDoc.xmlText());

        ProcessDescriptionsDocument control = ProcessDescriptionsDocument.Factory.parse(descriptionFile);

        // test process description manually
        String abstractString = null;
        String identifierString = null;
        String titleString = null;
        for (RAnnotation anno : this.annotations) {
            if (anno.getType().equals(RAnnotationType.DESCRIPTION)) {
                abstractString = anno.getStringValue(RAttribute.ABSTRACT);
                identifierString = R_Config.WKN_PREFIX + anno.getStringValue(RAttribute.IDENTIFIER);
                titleString = anno.getStringValue(RAttribute.TITLE);
            }
        }

        Assert.assertEquals(testType.getAbstract().getStringValue(), abstractString);
        Assert.assertEquals(testType.getIdentifier().getStringValue(), identifierString);
        Assert.assertEquals(testType.getTitle().getStringValue(), titleString);

        // test full document > some namespace issues! FIXME
        // Document controlDocument = (Document) control.getDomNode();
        // Document testDocument = (Document) testDoc.getDomNode();
        // XMLAssert.assertXMLEqual("Comparing process descriptions for uniform.",
        // controlDocument,
        // testDocument);

        fis.close();
    }

}
