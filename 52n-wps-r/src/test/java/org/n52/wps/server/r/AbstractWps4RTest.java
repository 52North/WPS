/*
 * Copyright (C) 2010-2017 52°North Initiative for Geospatial Open Source
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

import org.apache.xmlbeans.XmlException;
import org.junit.BeforeClass;
import org.mockito.Mockito;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.r.data.RDataTypeRegistry;
import org.n52.wps.server.r.metadata.RAnnotationParser;
import org.springframework.test.util.ReflectionTestUtils;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.n52.wps.commons.SpringIntegrationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 *
 * @author Daniel Nüst
 *
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration(value = "src/test/webapp")
@ContextConfiguration(locations = "classpath:r-applicationContext.xml")
public abstract class AbstractWps4RTest {

    private Path workingDir;

    @Autowired
    private RAnnotationParser parser;

    @Autowired
    private ScriptFileRepository scriptFileRepository;

    @Autowired
    private ResourceFileRepository resourceFileRepository;

    @Autowired
    private R_Config config;

    @BeforeClass
    public static void prepare() throws IOException, FileNotFoundException, XmlException {
        TestUtil.mockGenericWPSConfig();
    }

    @Before
    public void createWorkingDir() throws ExceptionReport, IOException {
        workingDir = Files.createTempDirectory("wps4r-it-").toAbsolutePath();
        config.getConfigModule().setWdName(workingDir.toString());
    }

    protected R_Config getConfigSpy() {
        final R_Config spy = Mockito.spy(config);
        Mockito.when(spy.getBaseDir()).thenReturn(workingDir);
        return spy;
    }

    protected RAlgorithmRepository getRAlgorithmRepository(R_Config config, String scriptDir) {
        ReflectionTestUtils.setField(parser, "config", config);
        ReflectionTestUtils.setField(parser, "dataTypeRegistry", new RDataTypeRegistry());

        ReflectionTestUtils.setField(scriptFileRepository, "annotationParser", parser);
        ReflectionTestUtils.setField(scriptFileRepository, "config", config);
        if (scriptDir != null) {
            // TODO use RAlgorithmRepository#addAlgorithm(Object)
            scriptFileRepository.registerScriptFiles(TestUtil.loadFile(scriptDir));
        }

        RAlgorithmRepository repo = new RAlgorithmRepository();
        ReflectionTestUtils.setField(repo, "config", config);
        ReflectionTestUtils.setField(repo, "scriptRepo", scriptFileRepository);
        ReflectionTestUtils.setField(repo, "resourceRepo", resourceFileRepository);
        ReflectionTestUtils.setField(repo, "parser", parser);

//        repo.init();
        return repo;
    }

    protected ScriptFileRepository getScriptFileRepository() {
        return scriptFileRepository;
    }

    protected ResourceFileRepository getResoureFileRepository() {
        return resourceFileRepository;
    }

    protected RAnnotationParser getAnnotationParser() {
        return parser;
    }

}
