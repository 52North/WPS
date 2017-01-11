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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.mockito.Mockito;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.r.syntax.RAnnotationException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Ignore;

/**
 *
 * @author Daniel Nüst
 *
 */
public class ImportAnnotationPublicScriptsIT extends AbstractWps4RTest {

    private final static String SCRIPT_DIR = "/annotations/import";

    private final static String SCRIPT_ID = "import";

    private RAlgorithmRepository openRepo;

    private R_Config openConfig;

    @Before
    public void setup() throws FileNotFoundException, IOException, ExceptionReport {

        openConfig = getConfigSpy();
        openConfig.setWknPrefix("test.");
        Mockito.when(openConfig.getEnableBatchStart()).thenReturn(false);
        Mockito.when(openConfig.isImportDownloadEnabled()).thenReturn(true);
        Mockito.when(openConfig.isResourceDownloadEnabled()).thenReturn(true);
        Mockito.when(openConfig.isScriptDownloadEnabled()).thenReturn(true);
        Mockito.when(openConfig.isSessionInfoLinkEnabled()).thenReturn(true);

        openRepo = getRAlgorithmRepository(openConfig, SCRIPT_DIR);
        openRepo.addAlgorithm(openConfig.getPublicScriptId(SCRIPT_ID));
    }

    @Test
    // XXX to integration test
    public void importedFunctionIsAvailableInRSession() throws IOException, RAnnotationException, ExceptionReport {
        // Trying to connect to Rserve
        try {
            RConnection testcon = openConfig.openRConnection();
            testcon.close();
        }
        catch (RserveException e) {
            return;
        }

        IAlgorithm algorithm = openRepo.getAlgorithm(openConfig.getPublicScriptId(SCRIPT_ID));
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
        IAlgorithm algorithm = openRepo.getAlgorithm(openConfig.getPublicScriptId(SCRIPT_ID));
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

}
