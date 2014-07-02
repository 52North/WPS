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
 * propagate, and distribute a work formed by combining 52n-wps-io-geotools
 * with the Eclipse Libraries, or a work derivative of such a combination,
 * even if such copying, modification, propagation, or distribution would
 * otherwise violate the terms of the GPL. Nothing in this exception exempts
 * you from complying with the GPL in all respects for all of the code used
 * other than the Eclipse Libraries. You may include this exception and its
 * grant of permissions when you distribute 52n-wps-io-geotools. Inclusion
 * of this notice with such a distribution constitutes a grant of such
 * permissions. If you do not wish to grant these permissions, remove this
 * paragraph from your distribution. "52n-wps-io-geotools" means the
 * 52°North WPS module using GeoTools functionality - software licensed
 * under version 2 or any later version of the GPL, or a work based on such
 * software and licensed under the GPL. "Eclipse Libraries" means Eclipse
 * Modeling Framework Project and XML Schema Definition software
 * distributed by the Eclipse Foundation and licensed under the Eclipse
 * Public License Version 1.0 ("EPL"), or a work based on such software and
 * licensed under the EPL.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.wps.io.test.datahandler.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.datahandler.parser.KMLParser;
import org.n52.wps.io.test.datahandler.AbstractTestCase;

public class KMLParserTest extends AbstractTestCase<KMLParser> {

	public void testParser(){

		if(!isDataHandlerActive()){
			return;
		}

//		String testFilePath = projectRoot + "/52n-wps-io/src/test/resources/streams.kml";//from geoserver, fail
//		String testFilePath = projectRoot + "/52n-wps-io/src/test/resources/shape.kml";//can be read by grass gis, fail
//		String testFilePath = projectRoot + "/52n-wps-io/src/test/resources/states.kml";//geotools example kml, fail
		String testFilePath = projectRoot + "/52n-wps-io-geotools/src/test/resources/x4.kml";//returned by our own generator, fail

		try {
			testFilePath = URLDecoder.decode(testFilePath, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			fail(e1.getMessage());
		}

		String[] mimetypes = dataHandler.getSupportedFormats();

		InputStream input = null;

		for (String mimetype : mimetypes) {

			try {
				input = new FileInputStream(new File(testFilePath));
			} catch (FileNotFoundException e) {
				fail(e.getMessage());
			}

			GTVectorDataBinding theBinding = dataHandler.parse(input, mimetype, "");

			assertNotNull(theBinding.getPayload());

			try {
				File f = theBinding.getPayloadAsShpFile();
				assertTrue(f.exists());
			} catch (Exception e) {
				e.printStackTrace();
				fail(e.getMessage());
			}
			assertTrue(!theBinding.getPayload().isEmpty());

		}

	}

	@Override
	protected void initializeDataHandler() {
		dataHandler = new KMLParser();
	}

}
