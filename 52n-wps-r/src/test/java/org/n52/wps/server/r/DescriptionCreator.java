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
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.opengis.ows.x11.MetadataType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionsDocument;

import org.apache.xmlbeans.XmlException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.r.data.RDataTypeRegistry;
import org.n52.wps.server.r.metadata.RAnnotationParser;
import org.n52.wps.server.r.metadata.RProcessDescriptionCreator;
import org.n52.wps.server.r.syntax.RAnnotation;
import org.n52.wps.server.r.syntax.RAnnotationException;
import org.n52.wps.server.r.syntax.RAnnotationType;
import org.n52.wps.server.r.syntax.RAttribute;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.collect.Lists;

public class DescriptionCreator {

    private static R_Config config;
    private List<RAnnotation> annotations;
    private RDataTypeRegistry registry;

    @BeforeClass
    public static void initConfig() throws FileNotFoundException, XmlException, IOException {
        config = new R_Config();
        Util.mockGenericWPSConfig();
    }

    @Before
    public void loadAnnotations() throws IOException, RAnnotationException {
        File scriptFile = Util.loadFile("/uniform.R");

        // GenericRProcess process = new GenericRProcess("R_andom");
        FileInputStream fis = new FileInputStream(scriptFile);
        RAnnotationParser parser = new RAnnotationParser();
        ReflectionTestUtils.setField(parser, "config", config);
        registry = new RDataTypeRegistry();
        ReflectionTestUtils.setField(parser, "dataTypeRegistry", registry);

        this.annotations = parser.parseAnnotationsfromScript(fis);
        fis.close();
    }

    @Test
    public void uniform() throws ExceptionReport, RAnnotationException, IOException, XmlException {
        File descriptionFile = Util.loadFile("/uniform.xml");

        RProcessDescriptionCreator creator = new RProcessDescriptionCreator("org.n52.wps.server.r.uniform",
                                                                            true,
                                                                            true,
                                                                            true,
                                                                            true);
        ProcessDescriptionType testType = creator.createDescribeProcessType(this.annotations,
                                                                            "R_andom",
                                                                            new URL("http://my.url/myScript.R"),
                                                                            new URL("http://my.url/to_the_session_info"));
        ProcessDescriptionsDocument testDoc = ProcessDescriptionsDocument.Factory.newInstance();
        testDoc.addNewProcessDescriptions().addNewProcessDescription().set(testType);
        // System.out.println(testDoc.xmlText());

        ProcessDescriptionsDocument.Factory.parse(descriptionFile);

        // test process description manually
        String abstractString = null;
        String identifierString = null;
        String titleString = null;
        for (RAnnotation anno : this.annotations) {
            if (anno.getType().equals(RAnnotationType.DESCRIPTION)) {
                abstractString = anno.getStringValue(RAttribute.ABSTRACT);
                identifierString = config.getPublicScriptId(anno.getStringValue(RAttribute.IDENTIFIER));
                titleString = anno.getStringValue(RAttribute.TITLE);
            }
        }

        Assert.assertEquals(testType.getAbstract().getStringValue(), abstractString);
        Assert.assertEquals(testType.getIdentifier().getStringValue(), identifierString);
        Assert.assertEquals(testType.getTitle().getStringValue(), titleString);

        // test full document > some namespace issues! FIXME
        // Document controlDocument = (Document) control.getDomNode();
        // Document testDocument = (Document) testDoc.getDomNode();
        // XMLAssert.assertXMLEqual("Comparing process descriptions for uniform.",
        // controlDocument,
        // testDocument);
    }

    @Test
    public void sessionInfoLinkEnablingWorks() throws MalformedURLException, ExceptionReport, RAnnotationException {
        RProcessDescriptionCreator creator = new RProcessDescriptionCreator("org.n52.wps.server.r.uniform",
                                                                            true,
                                                                            true,
                                                                            true,
                                                                            true);
        ProcessDescriptionType testType = creator.createDescribeProcessType(this.annotations,
                                                                            "R_andom",
                                                                            new URL("http://my.url/myScript.R"),
                                                                            new URL("http://my.url/to_the_session_info"));

        MetadataType[] metadataArray = testType.getMetadataArray();
        List<String> titles = Lists.newArrayList();
        for (MetadataType metadata : metadataArray) {
            titles.add(metadata.getTitle());
        }
        assertThat("session link title is in the metadata elements",
                   titles,
                   hasItem(RProcessDescriptionCreator.SESSION_INFO_TITLE));

        creator = new RProcessDescriptionCreator("org.n52.wps.server.r.uniform", true, true, true, false);
        testType = creator.createDescribeProcessType(this.annotations,
                                                     "R_andom",
                                                     new URL("http://my.url/myScript.R"),
                                                     new URL("http://my.url/to_the_session_info"));

        metadataArray = testType.getMetadataArray();
        titles = Lists.newArrayList();
        for (MetadataType metadata : metadataArray) {
            titles.add(metadata.getTitle());
        }
        assertThat("session link title NOT in in the metadata elements",
                   titles,
                   not(hasItem(RProcessDescriptionCreator.SESSION_INFO_TITLE)));
    }

    @Test
    public void scriptDownloaodEnablingWorks() throws MalformedURLException, ExceptionReport, RAnnotationException {
        RProcessDescriptionCreator creator = new RProcessDescriptionCreator("org.n52.wps.server.r.uniform",
                                                                            true,
                                                                            true,
                                                                            true,
                                                                            true);
        ProcessDescriptionType testType = creator.createDescribeProcessType(this.annotations,
                                                                            "R_andom",
                                                                            new URL("http://my.url/myScript.R"),
                                                                            new URL("http://my.url/to_the_session_info"));

        MetadataType[] metadataArray = testType.getMetadataArray();
        List<String> titles = Lists.newArrayList();
        for (MetadataType metadata : metadataArray) {
            titles.add(metadata.getTitle());
        }
        assertThat("script link title in in the metadata elements",
                   titles,
                   hasItem(RProcessDescriptionCreator.SCRIPT_LINK_TITLE));

        creator = new RProcessDescriptionCreator("org.n52.wps.server.r.uniform", true, true, false, true);
        testType = creator.createDescribeProcessType(this.annotations,
                                                     "R_andom",
                                                     new URL("http://my.url/myScript.R"),
                                                     new URL("http://my.url/to_the_session_info"));

        metadataArray = testType.getMetadataArray();
        titles = Lists.newArrayList();
        for (MetadataType metadata : metadataArray) {
            titles.add(metadata.getTitle());
        }
        assertThat("script link title NOT in in the metadata elements",
                   titles,
                   not(hasItem(RProcessDescriptionCreator.SCRIPT_LINK_TITLE)));
    }

    @Test
    public void resourceDownloadEnablingWorks() throws MalformedURLException, ExceptionReport, RAnnotationException {
        RProcessDescriptionCreator creator = new RProcessDescriptionCreator("org.n52.wps.server.r.uniform",
                                                                            true,
                                                                            true,
                                                                            true,
                                                                            true);
        ProcessDescriptionType testType = creator.createDescribeProcessType(this.annotations,
                                                                            "R_andom",
                                                                            new URL("http://my.url/myScript.R"),
                                                                            new URL("http://my.url/to_the_session_info"));

        MetadataType[] metadataArray = testType.getMetadataArray();
        List<String> titles = Lists.newArrayList();
        for (MetadataType metadata : metadataArray) {
            titles.add(metadata.getTitle());
        }
        assertThat("resource link title in in the metadata elements", titles, hasItem("Resource: test.file.txt"));

        creator = new RProcessDescriptionCreator("org.n52.wps.server.r.uniform", false, true, true, true);
        testType = creator.createDescribeProcessType(this.annotations,
                                                     "R_andom",
                                                     new URL("http://my.url/myScript.R"),
                                                     new URL("http://my.url/to_the_session_info"));

        metadataArray = testType.getMetadataArray();
        titles = Lists.newArrayList();
        for (MetadataType metadata : metadataArray) {
            titles.add(metadata.getTitle());
        }
        for (String t : titles) {
            assertThat("resource link title NOT in in the metadata elements",
                       t,
                       not(startsWith(RProcessDescriptionCreator.RESOURCE_TITLE_PREFIX)));
        }
    }

    @Test
    public void importDownloadEnablingWorks() throws MalformedURLException, ExceptionReport, RAnnotationException {
        RProcessDescriptionCreator creator = new RProcessDescriptionCreator("org.n52.wps.server.r.uniform",
                                                                            true,
                                                                            true,
                                                                            true,
                                                                            true);
        ProcessDescriptionType testType = creator.createDescribeProcessType(this.annotations,
                                                                            "R_andom",
                                                                            new URL("http://my.url/myScript.R"),
                                                                            new URL("http://my.url/to_the_session_info"));

        MetadataType[] metadataArray = testType.getMetadataArray();
        List<String> titles = Lists.newArrayList();
        for (MetadataType metadata : metadataArray) {
            titles.add(metadata.getTitle());
        }
        assertThat("resource link title in in the metadata elements",
                   titles,
                   hasItem("Import: annotations/import/imported.R"));

        creator = new RProcessDescriptionCreator("org.n52.wps.server.r.uniform", true, false, true, true);
        testType = creator.createDescribeProcessType(this.annotations,
                                                     "R_andom",
                                                     new URL("http://my.url/myScript.R"),
                                                     new URL("http://my.url/to_the_session_info"));

        metadataArray = testType.getMetadataArray();
        titles = Lists.newArrayList();
        for (MetadataType metadata : metadataArray) {
            titles.add(metadata.getTitle());
        }
        for (String t : titles) {
            assertThat("resource link title NOT in in the metadata elements",
                       t,
                       not(startsWith(RProcessDescriptionCreator.IMPORT_TITLE_PREFIX)));
        }
    }

    @Test
    public void metadataAnnotationParsing() throws MalformedURLException, ExceptionReport, RAnnotationException {
        RProcessDescriptionCreator creator = new RProcessDescriptionCreator("org.n52.wps.server.r.uniform",
                                                                            true,
                                                                            true,
                                                                            true,
                                                                            true);
        ArrayList<RAnnotation> testAnnotations = new ArrayList<RAnnotation>(this.annotations);
        HashMap<RAttribute, Object> attributeHash = new HashMap<RAttribute, Object>();
        String t = "metadatatitle";
        String h = "http://url.to/metadata.doc";
        attributeHash.put(RAttribute.TITLE, t);
        attributeHash.put(RAttribute.HREF, h);
        testAnnotations.add(new RAnnotation(RAnnotationType.METADATA, attributeHash, registry));

        ProcessDescriptionType testType = creator.createDescribeProcessType(testAnnotations,
                                                                            "R_andom",
                                                                            new URL("http://my.url/myScript.R"),
                                                                            new URL("http://my.url/to_the_session_info"));

        MetadataType[] metadataArray = testType.getMetadataArray();
        List<String> titles = Lists.newArrayList();
        List<String> hrefs = Lists.newArrayList();
        for (MetadataType metadata : metadataArray) {
            titles.add(metadata.getTitle());
            hrefs.add(metadata.getHref());
        }
        assertThat("metadata title in in the metadata elements", titles, hasItem(t));
        assertThat("metadata href in in the metadata elements", hrefs, hasItem(h));
    }

    @Test
    public void invalidMetadataHrefAnnotationParsing() throws MalformedURLException,
            ExceptionReport,
            RAnnotationException {
        RProcessDescriptionCreator creator = new RProcessDescriptionCreator("org.n52.wps.server.r.meta",
                                                                            true,
                                                                            true,
                                                                            true,
                                                                            true);
        ArrayList<RAnnotation> testAnnotations = new ArrayList<RAnnotation>(this.annotations);
        HashMap<RAttribute, Object> attributeHash = new HashMap<RAttribute, Object>();
        String t = "metadatatitle";
        String h = "http://url.to/metadata.doc";
        attributeHash.put(RAttribute.TITLE, t);
        // attributeHash.put(RAttribute.HREF, h);
        testAnnotations.add(new RAnnotation(RAnnotationType.METADATA, attributeHash, registry));

        ProcessDescriptionType testType = creator.createDescribeProcessType(testAnnotations,
                                                                            "Meta",
                                                                            new URL("http://my.url/myScript.R"),
                                                                            new URL("http://my.url/to_the_session_info"));

        MetadataType[] metadataArray = testType.getMetadataArray();
        List<String> titles = Lists.newArrayList();
        List<String> hrefs = Lists.newArrayList();
        for (MetadataType metadata : metadataArray) {
            titles.add(metadata.getTitle());
            hrefs.add(metadata.getHref());
        }
        assertThat("metadata title NOT in in the metadata elements", titles, not(hasItem(t)));
        assertThat("metadata href NOT in in the metadata elements", hrefs, not(hasItem(h)));
    }

    @Test
    public void invalidMetadataTitleAnnotationParsing() throws MalformedURLException,
            ExceptionReport,
            RAnnotationException {
        RProcessDescriptionCreator creator = new RProcessDescriptionCreator("org.n52.wps.server.r.meta",
                                                                            true,
                                                                            true,
                                                                            true,
                                                                            true);
        ArrayList<RAnnotation> testAnnotations = new ArrayList<RAnnotation>(this.annotations);
        HashMap<RAttribute, Object> attributeHash = new HashMap<RAttribute, Object>();
        String t = "metadatatitle";
        String h = "http://url.to/metadata.doc";
        // attributeHash.put(RAttribute.TITLE, t);
        attributeHash.put(RAttribute.HREF, h);
        testAnnotations.add(new RAnnotation(RAnnotationType.METADATA, attributeHash, registry));

        ProcessDescriptionType testType = creator.createDescribeProcessType(testAnnotations,
                                                                            "Meta",
                                                                            new URL("http://my.url/myScript.R"),
                                                                            new URL("http://my.url/to_the_session_info"));

        MetadataType[] metadataArray = testType.getMetadataArray();
        List<String> titles = Lists.newArrayList();
        List<String> hrefs = Lists.newArrayList();
        for (MetadataType metadata : metadataArray) {
            titles.add(metadata.getTitle());
            hrefs.add(metadata.getHref());
        }
        assertThat("metadata title NOT in in the metadata elements", titles, not(hasItem(t)));
        assertThat("metadata href NOT in in the metadata elements", hrefs, not(hasItem(h)));
    }

}
