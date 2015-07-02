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
import static org.hamcrest.Matchers.not;
import org.junit.After;
import static org.junit.Assert.assertThat;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.n52.wps.server.r.syntax.RAnnotation;
import org.n52.wps.server.r.syntax.RAnnotationType;
import org.n52.wps.server.r.syntax.RAttribute;

/**
 *
 * @author Daniel Nüst
 *
 */
public class Versions {

    private static RAnnotationParser parser;

    private static ScriptFileRepository sr;

    private static String scriptDir = "/versions/multiple";

    private static LocalRAlgorithmRepository repo;

    private static String scriptId = "version";

    private static R_Config mockR_Config;

    @BeforeClass
    public static void prepare() throws FileNotFoundException, IOException, XmlException, ExceptionReport {
        Util.forceInitializeWPSConfig();

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

        repo = new LocalRAlgorithmRepository();
        ReflectionTestUtils.setField(repo, "scriptRepo", sr);
        ReflectionTestUtils.setField(repo, "parser", parser);
        ReflectionTestUtils.setField(repo, "config", mockR_Config);
    }

    @After
    public void clearScriptRegistry() {
        sr.reset();
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void algorithmIsAvailableInRepos() throws IOException, RAnnotationException, ExceptionReport {
        sr.registerScripts(Util.loadFile(scriptDir));

        String id = mockR_Config.getWknPrefix() + scriptId;
        repo.addAlgorithm(id);

        assertThat("script repo contains algorithm", repo.containsAlgorithm(id), is(equalTo(true)));

        IAlgorithm algorithm = repo.getAlgorithm(mockR_Config.getWknPrefix() + scriptId);
        System.out.println(algorithm);
        assertThat("script repo contains algorithm with correct id", algorithm.getDescription().getIdentifier().getStringValue(), is(equalTo(id)));
        assertThat("script repo contains algorithm with correct version", algorithm.getDescription().getProcessVersion(), is(equalTo("3")));
    }

    @Test
    public void latestAlgorithmIsPublic() throws FileNotFoundException, RAnnotationException, IOException, ExceptionReport {
        sr.registerScripts(Util.loadFile(scriptDir));
        String id = mockR_Config.getWknPrefix() + scriptId;
        assertThat("latest version is public, not depending on file name order", sr.getScriptFileForWKN(id).getName(), is(equalTo("scriptB.R")));
        assertThat("latest version is public, not depending on file name order", sr.getScriptFileForWKN(id).getName(), not(is(equalTo("scriptA.R"))));
        assertThat("latest version is public, not depending on file name order", sr.getScriptFileForWKN(id).getName(), not(is(equalTo("scriptC.R"))));
    }

    @Test
    public void scriptDirCanBeRegistered() throws ExceptionReport {
        boolean registered = sr.registerScripts(Util.loadFile(scriptDir));
        assertThat("all scripts are registered", registered, is(equalTo(true)));

        String id = mockR_Config.getWknPrefix() + scriptId;
        File scriptFile = sr.getScriptFileForWKN(id);

        assertThat("latest script file is returned by script repo", scriptFile.getName(), is(equalTo("scriptB.R")));

        Map<Integer, File> versionedFiles = null; //sr.getScriptFileVersionsForWKN(id);
        assertThat("all versions are available", versionedFiles.size(), is(equalTo(3)));
        assertThat("all version numbers are available", versionedFiles.keySet(), Matchers.hasItems(1, 2, 3));
    }

    @Test
    public void wknsForScriptFilesAreResolved() throws ExceptionReport, RAnnotationException, IOException {
        File file1 = Util.loadFile(scriptDir + "/scriptA.R");
        File file2 = Util.loadFile(scriptDir + "/scriptC.R");
        String id = mockR_Config.getWknPrefix() + scriptId;

        assertThat("first script is registered", sr.registerScripts(file1), is(equalTo(true)));
        assertThat("wkn for first script file is correct", sr.getWKNForScriptFile(file1), is(equalTo(id)));

        assertThat("second script is registered", sr.registerScripts(file2), is(equalTo(true)));
        assertThat("wkn for second script file is correct", sr.getWKNForScriptFile(file2), is(equalTo(id)));

        assertThat("wkn for first script file still works", sr.getWKNForScriptFile(file1), is(equalTo(id)));
    }

    @Test
    public void invalidScriptNamesAreNotRegistered() throws ExceptionReport, RAnnotationException {
        boolean registered = sr.registerScripts(Util.loadFile("/versions/invalid"));
        assertThat("not all scripts are registered", registered, is(equalTo(false)));
        assertThat("invalid version with characters is not available", sr.isScriptAvailable(scriptId), is(equalTo(false)));
        assertThat("invalid version text is not available", sr.isScriptAvailable("version-invalid"), is(equalTo(false)));
    }

    @Test
    public void invalidScriptNameWithCharactersIsNotRegistered() throws ExceptionReport, RAnnotationException {
        boolean registered = sr.registerScript(Util.loadFile("/versions/invalid/script-invalid1.R"));
        assertThat("invalid version with characters is not registered", registered, is(equalTo(false)));
    }

    @Test
    public void invalidScriptNameTextIsNotRegistered() throws ExceptionReport, RAnnotationException {
        boolean registered = sr.registerScript(Util.loadFile("/versions/invalid/script-invalid2.R"));
        assertThat("invalid version text is not registered", registered, is(equalTo(false)));
    }

    @Test
    public void doubleRegistration() throws URISyntaxException, RAnnotationException, ExceptionReport, IOException {
        boolean registered_1 = sr.registerScript(Util.loadFile(scriptDir + "/scriptA.R"));
        boolean registered_2 = sr.registerScript(Util.loadFile(scriptDir + "/scriptB.R"));

        assertThat("first register call returns true", registered_1, is(equalTo(true)));
        assertThat("second register call returns true", registered_2, is(equalTo(true)));
    }
    
    @Test
    public void conflictingVersionNumbersGiveException() {
        thrown.expect(ExceptionReport.class);
        thrown.expectMessage(Matchers.containsString("conflicting version"));
        thrown.expectMessage(Matchers.containsString("17"));
        
        sr.registerScripts(Util.loadFile("/version/conflict"));
    }

}
