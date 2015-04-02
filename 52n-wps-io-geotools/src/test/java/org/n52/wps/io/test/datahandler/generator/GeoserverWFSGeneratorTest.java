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
 * As an exception to the terms of the GPL, you may copy, modify,
 * propagate, and distribute a work formed by combining 52°North WPS
 * GeoTools Modules with the Eclipse Libraries, or a work derivative of
 * such a combination, even if such copying, modification, propagation, or
 * distribution would otherwise violate the terms of the GPL. Nothing in
 * this exception exempts you from complying with the GPL in all respects
 * for all of the code used other than the Eclipse Libraries. You may
 * include this exception and its grant of permissions when you distribute
 * 52°North WPS GeoTools Modules. Inclusion of this notice with such a
 * distribution constitutes a grant of such permissions. If you do not wish
 * to grant these permissions, remove this paragraph from your
 * distribution. "52°North WPS GeoTools Modules" means the 52°North WPS
 * modules using GeoTools functionality - software licensed under version 2
 * or any later version of the GPL, or a work based on such software and
 * licensed under the GPL. "Eclipse Libraries" means Eclipse Modeling
 * Framework Project and XML Schema Definition software distributed by the
 * Eclipse Foundation and licensed under the Eclipse Public License Version
 * 1.0 ("EPL"), or a work based on such software and licensed under the EPL.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.wps.io.test.datahandler.generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.junit.Assert;
import org.junit.Test;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.datahandler.generator.GeoserverWFSGenerator;
import org.n52.wps.io.datahandler.parser.GML3BasicParser;
import org.n52.wps.io.test.datahandler.AbstractTestCase;

public class GeoserverWFSGeneratorTest extends AbstractTestCase<GeoserverWFSGenerator> {

	@Test
	public void testGenerator() {

		if(!isDataHandlerActive()){
			return;
		}

		String testFilePath = projectRoot
				+ "/52n-wps-io-geotools/src/test/resources/spearfish_restricted_sites_gml3.xml";

		try {
			testFilePath = URLDecoder.decode(testFilePath, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			Assert.fail(e1.getMessage());
		}

		GML3BasicParser theParser = new GML3BasicParser();

		String[] mimetypes = theParser.getSupportedFormats();

		InputStream input = null;

		try {
			input = new FileInputStream(new File(testFilePath));
		} catch (FileNotFoundException e) {
			Assert.fail(e.getMessage());
		}

		GTVectorDataBinding theBinding = theParser.parse(input,
				"text/xml; subtype=gml/3.2.1",
				"http://schemas.opengis.net/gml/3.2.1/base/feature.xsd");

		Assert.assertTrue(theBinding.getPayload() != null);

		String[] mimetypes2 = dataHandler.getSupportedFormats();

		for (String string : mimetypes2) {
			try {
				InputStream resultStream = dataHandler.generateStream(theBinding, string, null);

				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resultStream));

				String line = "";

				while((line = bufferedReader.readLine()) != null){
					System.out.println(line);
				}

				String request = "http://localhost:8181/geoserver/wms?service=WMS&version=1.1.0&request=GetMap&layers=N52:primary738239570087452915.tif_72e5aa87-5e2e-4c70-b913-53f4bf910245&styles=&bbox=444650.0,4631220.0,451640.0,4640510.0&width=385&height=512&srs=EPSG:26716&format=image/tiff";

			} catch (IOException e) {
				e.printStackTrace();
				Assert.fail(e.getMessage());
			}

		}

	}

	@Override
	protected void initializeDataHandler() {
		dataHandler = new GeoserverWFSGenerator();
	}

}
