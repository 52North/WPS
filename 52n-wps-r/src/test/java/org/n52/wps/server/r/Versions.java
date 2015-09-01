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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.xmlbeans.XmlException;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.r.data.RDataTypeRegistry;
import org.n52.wps.server.r.metadata.RAnnotationParser;
import org.n52.wps.server.r.syntax.RAnnotationException;
import org.springframework.test.util.ReflectionTestUtils;

/**
 *
 * @author Daniel Nüst
 *
 */
public class Versions {

    private static RAnnotationParser parser;

    private static ScriptFileRepository sr;

    private static final String scriptDirMultiple = "/versions/multiple";

    private static final String scriptDirSpread = "/versions/multiple-spread";

    private static LocalRAlgorithmRepository repo;

    private static final String scriptId = "version";

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
        sr.registerScripts(Util.loadFile(scriptDirMultiple));

        String id = mockR_Config.getWknPrefix() + scriptId;
        repo.addAlgorithm(id);

        assertThat("script repo contains algorithm", repo.containsAlgorithm(id), is(equalTo(true)));

        IAlgorithm algorithm = repo.getAlgorithm(mockR_Config.getWknPrefix() + scriptId);
        // System.out.println(algorithm);

        ProcessDescriptionType descr = (ProcessDescriptionType) algorithm.getDescription().getProcessDescriptionType("1.0.0");
        assertThat("script repo contains algorithm with correct id",
                   descr.getIdentifier().getStringValue(),
                   is(equalTo(id)));
        assertThat("script repo contains algorithm with correct version", descr.getProcessVersion(), is(equalTo("3")));
    }

    @Test
    public void latestAlgorithmIsPublic() throws FileNotFoundException,
            RAnnotationException,
            IOException,
            ExceptionReport {
        sr.registerScripts(Util.loadFile(scriptDirMultiple));
        String id = mockR_Config.getWknPrefix() + scriptId;
        assertThat("latest version is public, not depending on file name order",
                   sr.getScriptFileForWKN(id).getName(),
                   is(equalTo("scriptB.R")));
        assertThat("latest version is public, not depending on file name order",
                   sr.getScriptFileForWKN(id).getName(),
                   not(is(equalTo("scriptA.R"))));
        assertThat("latest version is public, not depending on file name order",
                   sr.getScriptFileForWKN(id).getName(),
                   not(is(equalTo("scriptC.R"))));
    }

    @Test
    public void scriptDirCanBeRegistered() throws ExceptionReport {
        boolean registered = sr.registerScripts(Util.loadFile(scriptDirMultiple));
        assertThat("all scripts are registered", registered, is(equalTo(true)));

        String id = mockR_Config.getWknPrefix() + scriptId;
        File scriptFile = sr.getScriptFileForWKN(id);

        assertThat("latest script file is returned by script repo", scriptFile.getName(), is(equalTo("scriptB.R")));

        Map<Integer, File> versionedFiles = sr.getScriptFileVersionsForWKN(id);
        assertThat("all versions are available", versionedFiles.size(), is(equalTo(3)));
        assertThat("all version numbers are available", versionedFiles.keySet(), Matchers.hasItems(1, 2, 3));
    }

    @Test
    public void wknsForScriptFilesAreResolved() throws ExceptionReport, RAnnotationException, IOException {
        File dir1 = Util.loadFile(scriptDirSpread + "/1");
        File dir2 = Util.loadFile(scriptDirSpread + "/2");
        String id = mockR_Config.getWknPrefix() + scriptId;

        assertThat("first script is registered", sr.registerScripts(dir1), is(equalTo(true)));
        File file1 = dir1.toPath().resolve("script.R").toFile();
        assertThat("wkn for first script file is correct", sr.getWKNForScriptFile(file1), is(equalTo(id)));

        assertThat("second script is registered", sr.registerScripts(dir2), is(equalTo(true)));
        assertThat("wkn for second script file is correct",
                   sr.getWKNForScriptFile(dir2.toPath().resolve("script.R").toFile()),
                   is(equalTo(id)));

        assertThat("wkn for first script file still works", sr.getWKNForScriptFile(file1), is(equalTo(id)));
    }

    @Test
    public void invalidScriptVersionsAreNotRegistered() {
        boolean registered = sr.registerScripts(Util.loadFile("/versions/invalid"));
        assertThat("not all scripts are registered", registered, is(equalTo(false)));
        assertThat("invalid version with characters is not available",
                   sr.isScriptAvailable(scriptId),
                   is(equalTo(false)));
        assertThat("invalid version text is not available", sr.isScriptAvailable("version-invalid"), is(equalTo(false)));
    }

    @Test
    public void invalidScriptVersionWithCharactersIsNotRegistered() throws ExceptionReport, RAnnotationException {
        thrown.expect(ExceptionReport.class);
        thrown.expectMessage(Matchers.containsString("integer"));
        thrown.expectMessage(Matchers.containsString("1.1-2"));
        boolean registered = sr.registerScript(Util.loadFile("/versions/invalid/script-invalid1.R"));
        assertThat("invalid version with characters is not registered", registered, is(equalTo(false)));
    }

    @Test
    public void invalidScriptVersionTextIsNotRegistered() throws ExceptionReport, RAnnotationException {
        thrown.expect(ExceptionReport.class);
        thrown.expectMessage(Matchers.containsString("cannot be parsed"));
        thrown.expectMessage(Matchers.containsString("one"));
        boolean registered = sr.registerScript(Util.loadFile("/versions/invalid/script-invalid2.R"));
        assertThat("invalid version text is not registered", registered, is(equalTo(false)));
    }

    @Test
    public void doubleRegistration() throws URISyntaxException, RAnnotationException, ExceptionReport, IOException {
        boolean registered_1 = sr.registerScript(Util.loadFile(scriptDirMultiple + "/scriptA.R"));
        boolean registered_2 = sr.registerScript(Util.loadFile(scriptDirMultiple + "/scriptB.R"));

        assertThat("first register call returns true", registered_1, is(equalTo(true)));
        assertThat("second register call returns true", registered_2, is(equalTo(true)));
    }

    @Test
    public void conflictingVersionNumbersGiveException() throws ExceptionReport {
        // conflicts are merely logged at the moment
        // thrown.expect(ExceptionReport.class);
        // thrown.expectMessage(Matchers.containsString("Conflicting version"));
        // thrown.expectMessage(Matchers.containsString("17"));
        String id = mockR_Config.getWknPrefix() + scriptId;

        boolean registered = sr.registerScripts(Util.loadFile("/versions/conflict"));
        assertThat("not all scripts are registered", registered, is(equalTo(false)));
        assertThat("only one script file is registered", sr.getScriptFileVersionsForWKN(id).size(), is(equalTo(1)));
        assertThat("the first file is registered", sr.getScriptFileForWKN(id).getName(), is(equalTo("scriptA.R")));
    }

}
