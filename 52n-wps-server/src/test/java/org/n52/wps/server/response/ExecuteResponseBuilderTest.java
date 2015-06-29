/**
 * ﻿Copyright (C) 2007 - 2014 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.server.response;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;

import net.opengis.wps.x100.DocumentOutputDefinitionType;
import net.opengis.wps.x100.OutputDefinitionType;

import org.apache.xmlbeans.XmlException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.request.ExecuteRequestV100;
import org.n52.wps.webapp.api.ConfigurationManager;
import org.n52.wps.webapp.common.AbstractITClass;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.w3c.dom.Document;

/**
 * This class tests the getMimeType method of the ExecuteResponseBuilder class.
 * TODO: Enhance with multiple in-/output tests
 *
 * @author Benjamin Pross(bpross-52n)
 *
 */
public class ExecuteResponseBuilderTest extends AbstractITClass{

	private ExecuteRequestV100 executeRequest;
	private DocumentBuilderFactory fac;

    @BeforeClass
    public static void setupClass() throws XmlException, IOException {
//        WPSConfigTestUtil.generateMockConfig(InputHandlerTest.class, "/org/n52/wps/io/test/inputhandler/generator/wps_config.xml");
    }

	@Before
	public void setUp() throws Exception {
		System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
				"org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");

		fac = DocumentBuilderFactory.newInstance();
		fac.setNamespaceAware(true);
		MockMvcBuilders.webAppContextSetup(this.wac).build();
//		WPSConfig.getInstance().setConfigurationManager(this.wac.getBean(ConfigurationManager.class));
	}

	@Test
	public void testGetMimeTypeLiteralOutputResponseDoc() {

		try {
			String sampleFileName = "src/test/resources/DTCExecuteLiteralOutputResponseDoc.xml";
			File sampleFile = new File(sampleFileName);

			FileInputStream is = new FileInputStream(sampleFile);

			// parse the InputStream to create a Document
			Document doc = fac.newDocumentBuilder().parse(is);

			is.close();

			executeRequest = new ExecuteRequestV100(doc);

			/*
			 * only one output here
			 */
			OutputDefinitionType definition = executeRequest.getExecute().getResponseForm().getResponseDocument().getOutputArray(0);

			String mimeType = executeRequest.getExecuteResponseBuilder()
					.getMimeType(definition);

			/*
			 * this should be text/plain as LiteralData was requested
			 */
			assertTrue(mimeType.equals("text/plain"));

		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testGetMimeTypeLiteralOutputRawData() {

		try {
			String sampleFileName = "src/test/resources/DTCExecuteLiteralOutputRawData.xml";
			File sampleFile = new File(sampleFileName);

			FileInputStream is = new FileInputStream(sampleFile);

			// parse the InputStream to create a Document
			Document doc = fac.newDocumentBuilder().parse(is);

			is.close();

			executeRequest = new ExecuteRequestV100(doc);

			/*
			 * only one output here
			 */
			OutputDefinitionType definition = executeRequest.getExecute().getResponseForm().getRawDataOutput();

			String mimeType = executeRequest.getExecuteResponseBuilder()
					.getMimeType(definition);

			/*
			 * this should be text/plain as LiteralData was requested
			 */
			assertTrue(mimeType.equals("text/plain"));

		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testGetMimeTypeComplexOutputRawData() {

		try {
			String sampleFileName = "src/test/resources/DTCExecuteComplexOutputRawDataMimeTiff.xml";
			File sampleFile = new File(sampleFileName);

			FileInputStream is;
			is = new FileInputStream(sampleFile);

			// parse the InputStream to create a Document
			Document doc;
			doc = fac.newDocumentBuilder().parse(is);

			is.close();

			executeRequest = new ExecuteRequestV100(doc);

			/*
			 * only one output here
			 */
			OutputDefinitionType definition = executeRequest.getExecute().getResponseForm().getRawDataOutput();
			String originalMimeType = definition.getMimeType();


			String mimeType = executeRequest.getExecuteResponseBuilder()
					.getMimeType(definition);

			/*
			 * this should be the same mime type as requested
			 */
			assertTrue(mimeType.equals(originalMimeType));

		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testGetMimeTypeComplexOutputResponseDoc() {

		try {
			String sampleFileName = "src/test/resources/DTCExecuteComplexOutputResponseDocMimeTiff.xml";
			File sampleFile = new File(sampleFileName);

			FileInputStream is;
			is = new FileInputStream(sampleFile);

			// parse the InputStream to create a Document
			Document doc;
			doc = fac.newDocumentBuilder().parse(is);

			is.close();

			executeRequest = new ExecuteRequestV100(doc);

			/*
			 * only one output here
			 */
			OutputDefinitionType definition = executeRequest.getExecute().getResponseForm().getResponseDocument().getOutputArray(0);
			String originalMimeType = definition.getMimeType();


			String mimeType = executeRequest.getExecuteResponseBuilder()
					.getMimeType(definition);

			/*
			 * this should be the same mime type as requested
			 */
			assertTrue(mimeType.equals(originalMimeType));

		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testGetMimeTypeMultipleComplexOutputsResponseDocPerm1() {

		try {
			String sampleFileName = "src/test/resources/MCIODTCExecuteComplexOutputResponseDocPerm1.xml";
			File sampleFile = new File(sampleFileName);

			FileInputStream is;
			is = new FileInputStream(sampleFile);

			// parse the InputStream to create a Document
			Document doc;
			doc = fac.newDocumentBuilder().parse(is);

			is.close();

			executeRequest = new ExecuteRequestV100(doc);

			DocumentOutputDefinitionType[] outputs = executeRequest.getExecute().getResponseForm().getResponseDocument().getOutputArray();

			for (DocumentOutputDefinitionType documentOutputDefinitionType : outputs) {

				String identifier = documentOutputDefinitionType.getIdentifier().getStringValue();

				String originalMimeType = documentOutputDefinitionType.getMimeType();

				String mimeType = executeRequest.getExecuteResponseBuilder()
						.getMimeType(documentOutputDefinitionType);

				if(identifier.contains("Complex")){
					assertTrue(mimeType.equals(originalMimeType));
				}else{
					assertTrue(mimeType.equals("text/plain") || mimeType.equals("text/xml"));
				}

			}

		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testGetMimeTypeMultipleComplexOutputsResponseDocPerm2() {

		try {
			String sampleFileName = "src/test/resources/MCIODTCExecuteComplexOutputResponseDocPerm2.xml";
			File sampleFile = new File(sampleFileName);

			FileInputStream is;
			is = new FileInputStream(sampleFile);

			// parse the InputStream to create a Document
			Document doc;
			doc = fac.newDocumentBuilder().parse(is);

			is.close();

			executeRequest = new ExecuteRequestV100(doc);

			DocumentOutputDefinitionType[] outputs = executeRequest.getExecute().getResponseForm().getResponseDocument().getOutputArray();

			for (DocumentOutputDefinitionType documentOutputDefinitionType : outputs) {

				String identifier = documentOutputDefinitionType.getIdentifier().getStringValue();

				String originalMimeType = documentOutputDefinitionType.getMimeType();

				String mimeType = executeRequest.getExecuteResponseBuilder()
						.getMimeType(documentOutputDefinitionType);

				if(identifier.contains("Complex")){
					assertTrue(mimeType.equals(originalMimeType));
				}else{
					assertTrue(mimeType.equals("text/plain") || mimeType.equals("text/xml"));
				}

			}

		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testGetMimeTypeBBOXOutputResponseDoc() {

		try {
			String sampleFileName = "src/test/resources/DTCExecuteBBOXOutputResponseDoc.xml";
			File sampleFile = new File(sampleFileName);

			FileInputStream is;
			is = new FileInputStream(sampleFile);

			// parse the InputStream to create a Document
			Document doc;
			doc = fac.newDocumentBuilder().parse(is);

			is.close();

			executeRequest = new ExecuteRequestV100(doc);

			/*
			 * only one output here
			 */
			OutputDefinitionType definition = executeRequest.getExecute().getResponseForm().getResponseDocument().getOutputArray(0);

			String mimeType = executeRequest.getExecuteResponseBuilder()
					.getMimeType(definition);

			/*
			 * this should be text/xml as BBOXData was requested
			 */
			assertTrue(mimeType.equals("text/xml"));

		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testGetMimeTypeBBOXOutputRawData() {

		try {
			String sampleFileName = "src/test/resources/DTCExecuteBBOXOutputRawData.xml";
			File sampleFile = new File(sampleFileName);

			FileInputStream is;
			is = new FileInputStream(sampleFile);

			// parse the InputStream to create a Document
			Document doc;
			doc = fac.newDocumentBuilder().parse(is);

			is.close();

			executeRequest = new ExecuteRequestV100(doc);

			/*
			 * only one output here
			 */
			OutputDefinitionType definition = executeRequest.getExecute().getResponseForm().getRawDataOutput();

			String mimeType = executeRequest.getExecuteResponseBuilder()
					.getMimeType(definition);

			/*
			 * this should be text/xml as BBOXData was requested
			 */
			assertTrue(mimeType.equals("text/xml"));

		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

}
