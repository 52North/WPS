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

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.IAlgorithm;

/**
 *
 * @author Daniel Nüst
 *
 */
//public class ImportAnnotationClosedScriptsTest extends AbstractWps4RTest {
//
//    private final static String SCRIPT_DIR = "/annotations/import";
//
//    private final static String SCRIPT_ID = "import";
//
//    private R_Config closedConfig;
//
//    private RAlgorithmRepository closedRepo;
//
//    @Before
//    public void setup() throws FileNotFoundException, IOException, ExceptionReport {
//
//        closedConfig = getConfigSpy();
//        closedConfig.setWknPrefix("test.");
//        Mockito.when(closedConfig.getEnableBatchStart()).thenReturn(true);
//        Mockito.when(closedConfig.isImportDownloadEnabled()).thenReturn(false);
//        Mockito.when(closedConfig.isResourceDownloadEnabled()).thenReturn(false);
//        Mockito.when(closedConfig.isScriptDownloadEnabled()).thenReturn(false);
//        Mockito.when(closedConfig.isSessionInfoLinkEnabled()).thenReturn(false);
//
//        closedRepo = getRAlgorithmRepository(closedConfig, SCRIPT_DIR);
//        closedRepo.addAlgorithm(closedConfig.getPublicScriptId(SCRIPT_ID));
//    }
//
//    @Test
//    public void importedScriptAreNotListedInProcessDescription() {
//        IAlgorithm algorithm = closedRepo.getAlgorithm(closedConfig.getPublicScriptId(SCRIPT_ID));
//        String description = algorithm.getDescription().getProcessDescriptionType("1.0.0").xmlText();
//        // System.out.println(description);
//
//        assertThat("imported script name is not listed as resource in description",
//                   description,
//                   not(containsString("imported.R\"")));
//        assertThat("imported script link is not in description",
//                   description,
//                   not(containsString("resource/test.import/imported.R")));
//    }
//
//}
