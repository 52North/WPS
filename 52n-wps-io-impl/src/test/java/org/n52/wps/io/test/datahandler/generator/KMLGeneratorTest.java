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

import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.datahandler.generator.KMLGenerator;
import org.n52.wps.io.datahandler.parser.GTBinZippedSHPParser;
import org.n52.wps.io.datahandler.parser.KMLParser;
import org.n52.wps.io.test.datahandler.AbstractTestCase;

public class KMLGeneratorTest extends AbstractTestCase<KMLGenerator> {

	public void testGenerator(){
		
		if(!isDataHandlerActive()){
			return;
		}
		
		String testFilePath = projectRoot + "/52n-wps-io-impl/src/test/resources/states.zip";
		
		try {
			testFilePath = URLDecoder.decode(testFilePath, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			fail(e1.getMessage());
		}
		
		GTBinZippedSHPParser theParser = new GTBinZippedSHPParser();
		
		KMLParser kmlParser = new KMLParser();
		
		String[] mimetypes1 = theParser.getSupportedFormats();
		
		InputStream input = null;
		
		try {
			input = new FileInputStream(new File(testFilePath));
		} catch (FileNotFoundException e) {
			fail(e.getMessage());
		}
		
		String mimetype = mimetypes1[0];
		
		GTVectorDataBinding theBinding = theParser.parse(input, mimetype, "");
			
		assertNotNull(theBinding.getPayload());		
		assertTrue(!theBinding.getPayload().isEmpty());	
		
		String[] mimetypes2 = dataHandler.getSupportedFormats();
		String[] schemas2 = dataHandler.getSupportedSchemas();
		
		for (String string : mimetypes2) {
			
			for (String schema : schemas2) {
				try {
					InputStream in = dataHandler.generateStream(theBinding, string, schema);
					
					GTVectorDataBinding generatedParsedBinding = kmlParser.parse(in, kmlParser.getSupportedFormats()[0], kmlParser.getSupportedSchemas()[0]);
					
					assertNotNull(generatedParsedBinding.getPayload());	
					assertTrue(generatedParsedBinding.getPayloadAsShpFile().exists());		
					assertTrue(!generatedParsedBinding.getPayload().isEmpty());			
					
				} catch (IOException e) {
					e.printStackTrace();
					fail(e.getMessage());
				}
			}
			
		}
		
	}

	@Override
	protected void initializeDataHandler() {
		dataHandler = new KMLGenerator();
	}
	
}
