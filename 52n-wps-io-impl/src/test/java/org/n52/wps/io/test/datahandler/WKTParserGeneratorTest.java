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
package org.n52.wps.io.test.datahandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Assert;
import org.junit.Test;
import org.n52.wps.io.data.binding.complex.JTSGeometryBinding;
import org.n52.wps.io.datahandler.generator.WKTGenerator;
import org.n52.wps.io.datahandler.parser.WKTParser;

/**
 * Test class for WKT parser and generator
 * @author Benjamin Pross
 *
 */
public class WKTParserGeneratorTest extends AbstractTestCase<WKTGenerator> {

	protected Logger LOGGER = LoggerFactory.getLogger(WKTParserGeneratorTest.class);
	
	@Test
	public void testGenerator() {
		
		if(!isDataHandlerActive()){
			return;
		}
		
		String inputWKTPolygonString = "POLYGON ((30 10, 10 20, 20 40, 40 40, 30 10))";
		
		InputStream in = new ByteArrayInputStream(inputWKTPolygonString.getBytes());
		
		WKTParser theParser = new WKTParser();

		String mimetype = theParser.getSupportedFormats()[0];
		
		LOGGER.info("Trying to parse WKT: " + inputWKTPolygonString);
		
		JTSGeometryBinding theBinding = theParser.parse(in, mimetype,
				null);

		try {
			in.close();
		} catch (IOException e) {
			LOGGER.warn("Failed to close ByteArrayInputStream containing input WKT.");
		}
		
		Assert.assertTrue(theBinding.getPayload() != null);
		
		InputStream generatedStream = null;
		
		try {
			generatedStream = dataHandler.generateStream(theBinding, mimetype, null);
			
		} catch (IOException e) {
			LOGGER.error("Failed to generate result inputstream.");
			Assert.fail();
		}
		
		String outputWKTPolygonString = "";
		
		int bite = -1;
		
		try {
			while ((bite = generatedStream.read()) != -1) {
				outputWKTPolygonString = outputWKTPolygonString.concat(String.valueOf((char)bite));
			}
		} catch (IOException e) {
			LOGGER.error("Failed to read result inputstream.");
			Assert.fail();
		}
		
		try {
			generatedStream.close();
		} catch (IOException e) {
			LOGGER.warn("Failed to close generated stream containing result WKT.");
		}
		
		Assert.assertTrue(inputWKTPolygonString.equals(outputWKTPolygonString));
		
		LOGGER.info("Generated WKT      : " + outputWKTPolygonString);
		
	}

	@Override
	protected void initializeDataHandler() {
		dataHandler = new WKTGenerator();
		
	}

	
}
