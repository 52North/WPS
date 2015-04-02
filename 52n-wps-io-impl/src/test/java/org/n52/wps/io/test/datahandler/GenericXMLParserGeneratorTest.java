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

import org.junit.Assert;
import org.junit.Test;
import org.n52.wps.io.data.binding.complex.GenericXMLDataBinding;
import org.n52.wps.io.datahandler.generator.GenericXMLDataGenerator;
import org.n52.wps.io.datahandler.parser.GenericXMLDataParser;

public class GenericXMLParserGeneratorTest extends AbstractTestCase<GenericXMLDataParser> {

	@Test
	public void testParseGenerateXML(){
		
		String mimeType = "text/xml";
		
		String inputXMLString = "<testElement>testStringValue</testElement>";
					
		GenericXMLDataBinding xmlDataBinding = dataHandler.parse(new ByteArrayInputStream(inputXMLString.getBytes()), mimeType, "");
			
		Assert.assertTrue(xmlDataBinding.getPayload() != null);
		
		InputStream generatedStream = null;
		
		try {
			generatedStream = new GenericXMLDataGenerator().generateStream(xmlDataBinding, mimeType, "");
			
		} catch (IOException e) {
			System.err.println("Failed to generate result inputstream.");
			Assert.fail();
		}
		
		String outputXMLString = "";
		
		int bite = -1;
		
		try {
			while ((bite = generatedStream.read()) != -1) {
				outputXMLString = outputXMLString.concat(String.valueOf((char)bite));
			}
		} catch (IOException e) {
			System.err.println("Failed to read result inputstream.");
			Assert.fail();
		}
		
		try {
			generatedStream.close();
		} catch (IOException e) {
			System.out.println("Failed to close generated stream containing result XML.");
		}
		
		Assert.assertTrue(inputXMLString.equals(outputXMLString));
		
		System.out.println("Generated XML      : " + outputXMLString);
		
	}
	
	@Override
	protected void initializeDataHandler() {
		dataHandler = new GenericXMLDataParser();
		
	}

}
