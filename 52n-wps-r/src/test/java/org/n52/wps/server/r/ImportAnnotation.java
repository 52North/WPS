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
import static org.junit.Assert.assertThat;

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
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.r.data.RDataTypeRegistry;
import org.n52.wps.server.r.metadata.RAnnotationParser;
import org.n52.wps.server.r.syntax.RAnnotationException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 
 * @author Daniel Nüst
 *
 */
public class ImportAnnotation {

    private static RAnnotationParser parser;

    private static ScriptFileRepository sr;

    private static String scriptDir = "/annotations/import";

    private static LocalRAlgorithmRepository openRepo;

    private static String scriptId = "import";

    private static R_Config openMockR_Config;

    private static R_Config closedMockR_Config;

    private static LocalRAlgorithmRepository closedRepo;

    @BeforeClass
    public static void prepare() throws FileNotFoundException, IOException, XmlException, ExceptionReport {
        Util.mockGenericWPSConfig();

        openMockR_Config = Mockito.spy(new R_Config());
        openMockR_Config.setWknPrefix("test.");
        Mockito.when(openMockR_Config.getEnableBatchStart()).thenReturn(true);
        Path p = Files.createTempDirectory("wps4r-it-").toAbsolutePath();
        Mockito.when(openMockR_Config.getBaseDir()).thenReturn(p);
        openMockR_Config.setConfigVariable(RWPSConfigVariables.R_WORK_DIR_NAME, p.toString());
        Mockito.when(openMockR_Config.isImportDownloadEnabled()).thenReturn(true);
        Mockito.when(openMockR_Config.isResourceDownloadEnabled()).thenReturn(true);
        Mockito.when(openMockR_Config.isScriptDownloadEnabled()).thenReturn(true);
        Mockito.when(openMockR_Config.isSessionInfoLinkEnabled()).thenReturn(true);

        closedMockR_Config = Mockito.spy(new R_Config());
        closedMockR_Config.setWknPrefix("test.");
        Mockito.when(closedMockR_Config.getEnableBatchStart()).thenReturn(true);
        Mockito.when(closedMockR_Config.getBaseDir()).thenReturn(p);
        Mockito.when(closedMockR_Config.isImportDownloadEnabled()).thenReturn(false);
        Mockito.when(closedMockR_Config.isResourceDownloadEnabled()).thenReturn(false);
        Mockito.when(closedMockR_Config.isScriptDownloadEnabled()).thenReturn(false);
        Mockito.when(closedMockR_Config.isSessionInfoLinkEnabled()).thenReturn(false);

        parser = new RAnnotationParser();
        ReflectionTestUtils.setField(parser, "config", openMockR_Config);
        ReflectionTestUtils.setField(parser, "dataTypeRegistry", new RDataTypeRegistry());
        sr = new ScriptFileRepository();
        ReflectionTestUtils.setField(sr, "annotationParser", parser);
        ReflectionTestUtils.setField(sr, "config", openMockR_Config);
        sr.registerScripts(Util.loadFile(scriptDir));

        ResourceFileRepository rr = new ResourceFileRepository();

        openRepo = new LocalRAlgorithmRepository();
        ReflectionTestUtils.setField(openRepo, "config", openMockR_Config);
        ReflectionTestUtils.setField(openRepo, "scriptRepo", sr);
        ReflectionTestUtils.setField(openRepo, "resourceRepo", rr);
        ReflectionTestUtils.setField(openRepo, "parser", parser);
        openRepo.addAlgorithm(openMockR_Config.getWknPrefix() + scriptId);

        closedRepo = new LocalRAlgorithmRepository();
        ReflectionTestUtils.setField(closedRepo, "config", closedMockR_Config);
        ReflectionTestUtils.setField(closedRepo, "scriptRepo", sr);
        ReflectionTestUtils.setField(closedRepo, "resourceRepo", rr);
        ReflectionTestUtils.setField(closedRepo, "parser", parser);
        closedRepo.addAlgorithm(closedMockR_Config.getWknPrefix() + scriptId);
    }

    @Test
    public void importedFunctionIsAvailableInRSession() throws IOException, RAnnotationException, ExceptionReport {
        // Trying to connect to Rserve
        try {
            RConnection testcon = openMockR_Config.openRConnection();
            testcon.close();
        }
        catch (RserveException e) {
            return;
        }

        IAlgorithm algorithm = openRepo.getAlgorithm(openMockR_Config.getWknPrefix() + scriptId);
        Map<String, List<IData>> inputData = Maps.newHashMap();
        List<IData> inputlist = Lists.newArrayList();
        inputlist.add(new LiteralIntBinding(Integer.valueOf(17)));
        inputData.put("inputstuff", inputlist);

        Map<String, IData> output = algorithm.run(inputData);

        IData expected = new LiteralIntBinding(Integer.valueOf(17 * 42 + 1));
        assertThat("output is the input processed by the imported function.",
                   output.get("outputstuff").getPayload(),
                   is(equalTo(expected.getPayload())));
    }
    
    @Test
    public void importedScriptIsListedInProcessDescription() {
        IAlgorithm algorithm = openRepo.getAlgorithm(openMockR_Config.getWknPrefix() + scriptId);
        String description = algorithm.getDescription().getProcessDescriptionType("1.0.0").xmlText();
        // System.out.println(description);

        assertThat("imported script name is listed as resource in description",
                   description,
                   containsString("title=\"Import: imported.R\""));
        assertThat("imported script link is in description",
                   description,
                   containsString("import/test.import/imported.R"));
        assertThat("second imported script title is listed as resource in description",
                   description,
                   containsString("title=\"Import: dir/alsoImported.R\""));
        // assertThat("second import script link is in description", description,
        // containsString(RResource.getImportURL(resource)))
    }
    
    @Test
    public void importedScriptAreNotListedInProcessDescription() {
        IAlgorithm algorithm = closedRepo.getAlgorithm(closedMockR_Config.getWknPrefix() + scriptId);
        String description = algorithm.getDescription().getProcessDescriptionType("1.0.0").xmlText();
        // System.out.println(description);

        assertThat("imported script name is not listed as resource in description",
                   description,
                   not(containsString("imported.R\"")));
        assertThat("imported script link is not in description",
                   description,
                   not(containsString("resource/test.import/imported.R")));
    }

}
