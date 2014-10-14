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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
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

/**
 * 
 * @author Daniel Nüst
 *
 */
public class ScriptFileRepo {

    private static R_Config config;

    private static RAnnotationParser parser;

    private ScriptFileRepository sr;

    private static String scriptFile = "/uniform.R";

    private String expectedWKN;

    @BeforeClass
    public static void prepare() {
        config = new R_Config();
        parser = new RAnnotationParser();
        ReflectionTestUtils.setField(parser, "config", config);
    }

    @Before
    public void prepareRepo() {
        sr = new ScriptFileRepository();
        ReflectionTestUtils.setField(sr, "annotationParser", parser);
        ReflectionTestUtils.setField(sr, "config", config);
        expectedWKN = config.getPublicScriptId("uniform");
    }

    public String prepareMissingScriptFile() throws IOException, RAnnotationException, ExceptionReport {
        File temp = File.createTempFile("wps4rIT_", ".R");
        Files.copy(Util.loadFile(scriptFile), temp);

        boolean b = sr.registerScript(temp);
        String missingWkn = sr.getWKNForScriptFile(temp);
        assertTrue(b);
        boolean delete = temp.delete();
        assertTrue(delete);
        return missingWkn;
    }

    @Test
    public void scriptIsStored() throws URISyntaxException, RAnnotationException, ExceptionReport, IOException {
        File f = Util.loadFile(scriptFile);

        boolean registerScript = sr.registerScript(f);
        assertThat("script is registered", registerScript, is(equalTo(true)));

        String wkn = sr.getWKNForScriptFile(f);
        assertNotNull(wkn);
        File file = sr.getScriptFileForWKN(wkn);
        assertSame(f.toString(), file.toString());
        assertTrue(sr.isScriptAvailable(wkn));
        RProcessInfo info = new RProcessInfo(wkn, null, null);
        assertTrue(sr.isScriptAvailable(info));
    }

    @Test
    public void registrationReturnValue() throws URISyntaxException, RAnnotationException, ExceptionReport, IOException {
        File f = Util.loadFile(scriptFile);
        assertThat("registration method response is true", sr.registerScript(f), is(equalTo(true)));
    }

    @Test
    public void wkn() throws URISyntaxException, RAnnotationException, ExceptionReport, IOException {
        File f = Util.loadFile(scriptFile);
        sr.registerScript(f);
        assertThat("script wkn is correct", sr.getWKNForScriptFile(f), is(equalTo(expectedWKN)));
    }

    @Test
    public void doubleRegistration() throws URISyntaxException, RAnnotationException, ExceptionReport, IOException {
        File f = Util.loadFile(scriptFile);
        assertThat("first register call returns true", sr.registerScript(f), is(equalTo(true)));
        assertThat("second register call returns false", sr.registerScript(f), is(equalTo(false)));
    }

    @Test
    public void scriptIsNotAvailableAWhenFileMissing() throws IOException, RAnnotationException, ExceptionReport {
        String missingWkn = prepareMissingScriptFile();
        boolean wknAvailable = sr.isScriptAvailable(missingWkn);
        boolean infoAvailable = sr.isScriptAvailable(new RProcessInfo(missingWkn, null, null));
        assertThat("missing script file: script is not available when asking with wkn",
                   wknAvailable,
                   is(equalTo(false)));
        assertThat("missing script file: script is not available when asking with process info",
                   infoAvailable,
                   is(equalTo(false)));
    }

    @Test
    public void scriptIsInvalidWhenFileMissing() throws IOException, RAnnotationException, ExceptionReport {
        boolean valid = sr.isScriptValid(prepareMissingScriptFile());
        assertThat("dmissing script file: script is invalid", valid, is(equalTo(false)));
    }

}
