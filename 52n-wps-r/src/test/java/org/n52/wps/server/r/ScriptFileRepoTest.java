/**
 * Copyright (C) 2010-2015 52°North Initiative for Geospatial Open Source
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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.r.info.RProcessInfo;
import org.n52.wps.server.r.metadata.RAnnotationParser;
import org.n52.wps.server.r.syntax.RAnnotationException;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.io.Files;
import java.io.FileNotFoundException;
import static org.hamcrest.CoreMatchers.is;
import org.hamcrest.Matchers;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.n52.wps.commons.SpringIntegrationHelper;
import org.n52.wps.server.r.util.InvalidRScriptException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author Daniel Nüst
 *
 */
public class ScriptFileRepoTest extends AbstractWps4RTest{

    private ScriptFileRepository sr;

    private static String scriptFile = "/uniform.R";

    private String expectedWKN;
    
    @Autowired
    private RAnnotationParser annotationParser;

    @Autowired
    R_Config config;

    @Before
    public void prepareRepo() {
        sr = new ScriptFileRepository();
        ReflectionTestUtils.setField(annotationParser, "config", config);
        ReflectionTestUtils.setField(sr, "annotationParser", annotationParser);
        ReflectionTestUtils.setField(sr, "config", config);
        expectedWKN = config.getPublicScriptId("uniform");
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public String prepareMissingScriptFile() throws RAnnotationException, ExceptionReport, IOException {
        File temp = File.createTempFile("wps4rIT_", ".R");
        Files.copy(TestUtil.loadFile(scriptFile), temp);

        boolean b = sr.registerScriptFile(temp);
        String missingWkn = sr.getWKNForScriptFile(temp);
        assertTrue(b);
        boolean delete = temp.delete();
        assertTrue(delete);
        return missingWkn;
    }

    @Test
    public void scriptIsStored() throws URISyntaxException, RAnnotationException, InvalidRScriptException, ExceptionReport {
        File f = TestUtil.loadFile(scriptFile);

        boolean registerScript = sr.registerScriptFile(f);
        assertThat("script is registered", registerScript, is(equalTo(true)));

        String wkn = sr.getWKNForScriptFile(f);
        assertNotNull(wkn);
        File file = sr.getScriptFile(wkn);
        assertSame(f.toString(), file.toString());
        assertTrue(sr.hasReadableScriptFile(wkn));
        RProcessInfo info = new RProcessInfo(wkn, null, null);
        assertTrue(sr.hasReadableScriptFile(info));
    }

    @Test
    public void registrationReturnValue() throws URISyntaxException, RAnnotationException, ExceptionReport, IOException {
        File f = TestUtil.loadFile(scriptFile);
        assertThat("registration method response is true", sr.registerScriptFile(f), is(equalTo(true)));
    }

    @Test
    public void wkn() throws URISyntaxException, RAnnotationException, ExceptionReport {
        File f = TestUtil.loadFile(scriptFile);
        sr.registerScriptFile(f);
        assertThat("script wkn is correct", sr.getWKNForScriptFile(f), is(equalTo(expectedWKN)));
    }

    @Test
    public void doubleRegistration() throws URISyntaxException, RAnnotationException, ExceptionReport, IOException {
        File f = TestUtil.loadFile(scriptFile);
        assertThat("first register call returns true", sr.registerScriptFile(f), is(equalTo(true)));
        assertThat("second register call returns false", sr.registerScriptFile(f), is(equalTo(false)));
    }

    @Test
    public void scriptIsNotAvailableAWhenFileMissing() throws IOException, RAnnotationException, ExceptionReport {
        String missingWkn = prepareMissingScriptFile();
        boolean wknAvailable = sr.hasReadableScriptFile(missingWkn);
        boolean infoAvailable = sr.hasReadableScriptFile(new RProcessInfo(missingWkn, null, null));
        assertThat("missing script file: script is not available when asking with wkn",
                   wknAvailable,
                   is(equalTo(false)));
        assertThat("missing script file: script is not available when asking with process info",
                   infoAvailable,
                   is(equalTo(false)));
    }

    @Test(expected = InvalidRScriptException.class)
    public void exceptionWhenFileIsMissing() throws Exception {
        String missingWkn = prepareMissingScriptFile();
        sr.validateScriptFile(sr.getScriptFile(missingWkn), missingWkn);
    }

    @Test
    public void scriptIsInvalidWhenFileMissing() throws IOException, RAnnotationException, ExceptionReport {
        boolean valid = sr.isValidScriptFile(prepareMissingScriptFile());
        assertThat("missing script file: script is invalid", valid, is(equalTo(false)));
    }

    @Test
    public void importedScriptIsFound() throws IOException, RAnnotationException, ExceptionReport, InvalidRScriptException {
        assertTrue(sr.registerScriptFile(TestUtil.loadFile("/annotations/import/script.R")));

        File file = sr.getImportedFileForWKN(config.getPublicScriptId("import"), "imported.R");
        File fileInSubdir = sr.getImportedFileForWKN(config.getPublicScriptId("import"), "dir/alsoImported.R");

        assertThat("imported script file in same dir can be resolved", file.exists(), is(equalTo(true)));
        assertThat("imported script file in subdir can be resolved", fileInSubdir.exists(), is(equalTo(true)));
        assertThat("imported script file in subdir is absolute", fileInSubdir.isAbsolute(), is(equalTo(true)));
        assertThat("imported script file in same dir is absolute", file.exists(), is(equalTo(true)));
    }

    @Test
    public void errorOnSameIdentifier() throws FileNotFoundException, RAnnotationException, IOException, ExceptionReport {
        boolean registered = sr.registerScriptFiles(TestUtil.loadFile("/annotations/identifier"));
        assertThat("not all scripts registered", registered, is(equalTo(false)));

        thrown.expect(ExceptionReport.class);
        thrown.expectMessage(Matchers.containsString("Conflicting identifier"));
        thrown.expectMessage(Matchers.containsString("notunique"));
        sr.registerScriptFile(TestUtil.loadFile("/annotations/identifier/script2.R"));
    }

}
