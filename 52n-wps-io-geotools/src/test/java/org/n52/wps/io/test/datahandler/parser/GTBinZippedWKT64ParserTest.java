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
package org.n52.wps.io.test.datahandler.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.apache.commons.codec.binary.Base64InputStream;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.junit.Assert;
import org.junit.Test;
import org.opengis.feature.Feature;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.datahandler.parser.GTBinZippedWKT64Parser;
import org.n52.wps.io.test.datahandler.AbstractTestCase;


/**
 * This class is for testing the GTBinZippedWKT64Parser. A base64 encoded zip file containing WKT files will be
 * read into a Base64InputStream. This stream will be handed to the parser.
 * It will be checked, whether the resulting FeatureCollection not null, not empty and whether it can be written to a shapefile.
 * The parsed geometries are printed out.
 *
 * @author BenjaminPross
 *
 */
public class GTBinZippedWKT64ParserTest extends AbstractTestCase<GTBinZippedWKT64Parser> {

	@Test
	public void testParser(){

		if(!isDataHandlerActive()){
			return;
		}

		String testFilePath = projectRoot + "/52n-wps-io-geotools/src/test/resources/wktgeometries.base64.zip";

		try {
			testFilePath = URLDecoder.decode(testFilePath, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			Assert.fail(e1.getMessage());
		}

		String[] mimetypes = dataHandler.getSupportedFormats();

		InputStream input = null;

		for (String mimetype : mimetypes) {

			try {

				input = new Base64InputStream(new FileInputStream(new File(testFilePath)));
			} catch (FileNotFoundException e) {
				Assert.fail(e.getMessage());
			}

			GTVectorDataBinding theBinding = dataHandler.parse(input, mimetype, "");

			Assert.assertNotNull(theBinding.getPayload());
			Assert.assertTrue(!theBinding.getPayload().isEmpty());

			FeatureCollection<?, ?> collection = theBinding.getPayload();

			FeatureIterator<?> featureIterator = collection.features();

			while(featureIterator.hasNext()){
				Feature f = featureIterator.next();

				System.out.println(f.getDefaultGeometryProperty());
			}

			Assert.assertTrue(theBinding.getPayloadAsShpFile().exists());

		}

	}

	@Override
	protected void initializeDataHandler() {
		dataHandler = new GTBinZippedWKT64Parser();
	}

}
