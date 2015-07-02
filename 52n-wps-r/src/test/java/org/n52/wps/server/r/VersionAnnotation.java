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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.apache.xmlbeans.XmlException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.r.data.RDataTypeRegistry;
import org.n52.wps.server.r.metadata.RAnnotationParser;
import org.n52.wps.server.r.syntax.RAnnotationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.net.URISyntaxException;
import static org.hamcrest.CoreMatchers.is;
import org.hamcrest.Matchers;
import static org.hamcrest.Matchers.equalTo;
import org.junit.After;
import static org.junit.Assert.assertThat;
import org.n52.wps.server.r.syntax.RAnnotation;
import org.n52.wps.server.r.syntax.RAnnotationType;
import org.n52.wps.server.r.syntax.RAttribute;

/**
 *
 * @author Daniel Nüst
 *
 */
public class VersionAnnotation {

    private static RAnnotationParser parser;

    private static R_Config mockR_Config;

    @BeforeClass
    public static void prepare() throws FileNotFoundException, IOException, XmlException, ExceptionReport {
        Util.mockGenericWPSConfig();

        mockR_Config = Mockito.spy(new R_Config());
        mockR_Config.setWknPrefix("test.");
        Mockito.when(mockR_Config.getEnableBatchStart()).thenReturn(true);
        Path p = Files.createTempDirectory("wps4r-it-").toAbsolutePath();
        Mockito.when(mockR_Config.getBaseDir()).thenReturn(p);

        parser = new RAnnotationParser();
        ReflectionTestUtils.setField(parser, "config", mockR_Config);
        ReflectionTestUtils.setField(parser, "dataTypeRegistry", new RDataTypeRegistry());
    }
    
    @Test
    public void versionIsParsed() throws FileNotFoundException, RAnnotationException, IOException {
        try (FileInputStream fis = new FileInputStream(Util.loadFile("/annotations/version/script.R"));) {
            List<RAnnotation> annotations = parser.parseAnnotationsfromScript(fis);

            RAnnotation annotation = RAnnotation.filterFirstMatchingAnnotation(annotations, RAnnotationType.DESCRIPTION);
            String versionString = annotation.getStringValue(RAttribute.VERSION);
            int version = Integer.parseInt(versionString);

            assertThat("version string is correctly parsed", version, is(equalTo(42)));
        }
    }

}
