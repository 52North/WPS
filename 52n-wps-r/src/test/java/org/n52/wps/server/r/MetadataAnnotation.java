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

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.xmlbeans.XmlException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.r.data.RDataTypeRegistry;
import org.n52.wps.server.r.metadata.RAnnotationParser;
import org.n52.wps.server.r.syntax.RAnnotation;
import org.n52.wps.server.r.syntax.RAnnotationException;
import org.n52.wps.server.r.syntax.RAnnotationType;
import org.n52.wps.server.r.syntax.RAttribute;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 
 * @author Daniel Nüst
 *
 */
public class MetadataAnnotation {

    private static RAnnotationParser parser;

    private static ScriptFileRepository sr;

    private static String scriptDir = "/annotations/metadata";

    private static LocalRAlgorithmRepository repo;

    private static String scriptId = "metadata";

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
        sr = new ScriptFileRepository();
        ReflectionTestUtils.setField(sr, "annotationParser", parser);
        ReflectionTestUtils.setField(sr, "config", mockR_Config);

        sr.registerScripts(Util.loadFile(scriptDir));
        repo = new LocalRAlgorithmRepository();
        ReflectionTestUtils.setField(repo, "scriptRepo", sr);
        ReflectionTestUtils.setField(repo, "parser", parser);
        ReflectionTestUtils.setField(repo, "config", mockR_Config);

        repo.addAlgorithm(mockR_Config.getWknPrefix() + scriptId);
    }

    @Test
    public void metadataLinksAreInAnnotations() throws IOException, RAnnotationException {
        try (FileInputStream fis = new FileInputStream(Util.loadFile(scriptDir + "/script.R"));) {
            List<RAnnotation> annotations = parser.parseAnnotationsfromScript(fis);

            annotations = RAnnotation.filterAnnotations(annotations, RAnnotationType.METADATA);

            assertThat("two annotations are parsed", annotations.size(), is(equalTo(2)));

            for (RAnnotation annotation : annotations) {
                assertThat("title of metadata annotation is available",
                        annotation.containsKey(RAttribute.TITLE),
                        is(equalTo(true)));
                assertThat("href of metadata annotation is available",
                        annotation.containsKey(RAttribute.HREF),
                        is(equalTo(true)));
                assertThat("title of metadata annotation is not empty",
                        annotation.getStringValue(RAttribute.HREF),
                        not(isEmptyString()));
                assertThat("href of metadata annotation is not empty",
                        annotation.getStringValue(RAttribute.HREF),
                        not(isEmptyString()));
            }

        }
    }

    @Test
    public void missingTitleGivesError() throws IOException, RAnnotationException, ExceptionReport {
        boolean scriptValid = sr.isScriptValid(mockR_Config.getWknPrefix() + "invalid-title");
        assertThat("repo says script is invalid", scriptValid, is(equalTo(false)));
    }

    @Test
    public void missingHrefGivesError() throws IOException, RAnnotationException, ExceptionReport {
        boolean scriptValid = sr.isScriptValid(mockR_Config.getWknPrefix() + "invalid-href");
        assertThat("repo says script is invalid", scriptValid, is(equalTo(false)));
    }

    @Test
    public void malformedHrefGivesError() throws IOException, RAnnotationException, ExceptionReport {
        boolean scriptValid = sr.isScriptValid(mockR_Config.getWknPrefix() + "invalid-href-url");
        assertThat("repo says script is invalid", scriptValid, is(equalTo(false)));
    }

    @Test
    public void metadataLinksAreListedInProcessDescription() {
        IAlgorithm algorithm = repo.getAlgorithm(mockR_Config.getWknPrefix() + scriptId);
        String description = algorithm.getDescription().getProcessDescriptionType("1.0.0").xmlText();

        assertThat("metadata 1 title is in description", description, containsString("title=\"detailed manual\""));
        assertThat("metadata 2 title is in description",
                description,
                containsString("title=\"scientific publication\""));

        assertThat("metadata 1 link is in description",
                description,
                containsString("href=\"http://my.url/information.pdf\""));
        assertThat("metadata 2 link is in description",
                description,
                containsString("href=\"http://my.other.url/journal-publication.pdf\""));
    }

    @Test
    public void invalidMetadataLinksAreNotListedInProcessDescription() {
        IAlgorithm algorithm = repo.getAlgorithm(mockR_Config.getWknPrefix() + "invalid-title");
        String description = algorithm.getDescription().getProcessDescriptionType("1.0.0").xmlText();

        assertThat("metadata 1 title is NOT in description", description, not(containsString("detailed manual")));
        assertThat("metadata 1 link is NOT in description",
                description,
                not(containsString("http://my.url/information.pdf")));
    }

}
