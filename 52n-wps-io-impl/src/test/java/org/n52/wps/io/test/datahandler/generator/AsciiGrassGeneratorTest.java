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
package org.n52.wps.io.test.datahandler.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.n52.wps.io.data.binding.complex.AsciiGrassDataBinding;
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;
import org.n52.wps.io.datahandler.generator.AsciiGrassGenerator;
import org.n52.wps.io.datahandler.parser.AsciiGrassParser;
import org.n52.wps.io.datahandler.parser.GeotiffParser;
import org.n52.wps.io.test.datahandler.AbstractTestCase;

public class AsciiGrassGeneratorTest extends AbstractTestCase<AsciiGrassGenerator> {

	public void testGenerator() {
		
		if(!isDataHandlerActive()){
			return;
		}
		
		String testFilePath = projectRoot
				+ "/52n-wps-io-impl/src/test/resources/6_UTM2GTIF.TIF";

		try {
			testFilePath = URLDecoder.decode(testFilePath, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			fail(e1.getMessage());
		}
		
		GeotiffParser theParser = new GeotiffParser();

		String[] mimetypes = theParser.getSupportedFormats();

		InputStream input = null;

		try {
			input = new FileInputStream(new File(testFilePath));
		} catch (FileNotFoundException e) {
			fail(e.getMessage());
		}

		GTRasterDataBinding theBinding = theParser.parse(input, mimetypes[0],
				null);

		assertTrue(theBinding.getPayload() != null);
		
		String[] mimetypes2 = dataHandler.getSupportedFormats();

		AsciiGrassParser asciiGrassParser = new AsciiGrassParser();
		
		for (String string : mimetypes2) {
			try {
				InputStream resultStream = dataHandler.generateStream(theBinding, string, null);
				
				AsciiGrassDataBinding rasterBinding = asciiGrassParser.parse(resultStream, mimetypes[0], null);
				
				assertTrue(rasterBinding.getPayload() != null);
				assertTrue(rasterBinding.getPayload().getDimension() != 0);
				assertTrue(rasterBinding.getPayload().getEnvelope() != null);
				
				InputStream resultStreamBase64 = dataHandler.generateBase64Stream(theBinding, string, null);
				
				AsciiGrassDataBinding rasterBindingBase64 = (AsciiGrassDataBinding) asciiGrassParser.parseBase64(resultStreamBase64, mimetypes[0], null);
				
				assertTrue(rasterBindingBase64.getPayload() != null);
				assertTrue(rasterBindingBase64.getPayload().getDimension() != 0);
				assertTrue(rasterBindingBase64.getPayload().getEnvelope() != null);
				
			} catch (IOException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}

		}

	}

	@Override
	protected void initializeDataHandler() {
		dataHandler = new AsciiGrassGenerator();
		
	}

	
}
