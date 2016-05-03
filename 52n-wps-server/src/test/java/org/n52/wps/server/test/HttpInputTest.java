/**
 * ﻿Copyright (C) 2007 - 2016 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.server.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.InputType;

import org.apache.xmlbeans.XmlException;
import org.junit.Assert;
import org.junit.Test;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.request.strategy.DefaultReferenceStrategy;
import org.n52.wps.server.request.strategy.IReferenceStrategy;

public class HttpInputTest {
	
	static final String sampleFileName = "src/test/resources/InputTestExecuteSample.xml";
	
	@Test
	public void testHttpInput() throws XmlException, IOException{
		
		
		// Arrange
		File sample = new File(sampleFileName);
		ExecuteDocument execDoc = ExecuteDocument.Factory.parse(sample);
		InputType[] inputArray = execDoc.getExecute().getDataInputs().getInputArray();
		
		// Act & Assert
		for (InputType currentInput : inputArray){
			System.out.println("Testing input " + currentInput.getIdentifier().getStringValue());
			
			IReferenceStrategy strategy = new DefaultReferenceStrategy();
			try {
				InputStream is = strategy.fetchData(currentInput);
				Assert.assertNotNull(is);
				printStream(is);
				
			} catch (ExceptionReport e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private static void printStream (InputStream is) throws IOException{
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		String line = null;
		while((line = in.readLine()) != null) {
		  System.out.println(line);
		}
	}
}
