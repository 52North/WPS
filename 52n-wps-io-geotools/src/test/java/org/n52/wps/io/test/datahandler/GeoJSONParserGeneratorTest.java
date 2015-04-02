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
package org.n52.wps.io.test.datahandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.JTSGeometryBinding;
import org.n52.wps.io.datahandler.generator.GeoJSONGenerator;
import org.n52.wps.io.datahandler.parser.GeoJSONParser;

/**
 * Test class for GeoJSON parser and generator
 * @author Benjamin Pross(bpross-52n)
 *
 */
public class GeoJSONParserGeneratorTest extends AbstractTestCase<GeoJSONGenerator> {
	
	@Test
	public void testParseWriteGeoJSONPoint() {
		
		if(!isDataHandlerActive()){
			return;
		}
		
		String inputGeoJSONPointString = "{\"type\":\"Point\",\"coordinates\":[100,0.0]}";
		
		InputStream in = new ByteArrayInputStream(inputGeoJSONPointString.getBytes());
		
		GeoJSONParser theParser = new GeoJSONParser();

		String mimetype = theParser.getSupportedFormats()[0];
		
		System.out.println("Trying to parse GeoJSON: " + inputGeoJSONPointString);
		
		JTSGeometryBinding theBinding = (JTSGeometryBinding) theParser.parse(in, mimetype,
				null);

		try {
			in.close();
		} catch (IOException e) {
			System.out.println("Failed to close ByteArrayInputStream containing input GeoJSON.");
		}
		
		Assert.assertTrue(theBinding.getPayload() != null);
		
		InputStream generatedStream = null;
		
		try {
			generatedStream = dataHandler.generateStream(theBinding, mimetype, null);
			
		} catch (IOException e) {
			System.err.println("Failed to generate result inputstream.");
			Assert.fail();
		}
		
		String outputGeoJSONPointString = "";
		
		int bite = -1;
		
		try {
			while ((bite = generatedStream.read()) != -1) {
				outputGeoJSONPointString = outputGeoJSONPointString.concat(String.valueOf((char)bite));
			}
		} catch (IOException e) {
			System.err.println("Failed to read result inputstream.");
			Assert.fail();
		}
		
		try {
			generatedStream.close();
		} catch (IOException e) {
			System.out.println("Failed to close generated stream containing result GeoJSON.");
		}
		
		Assert.assertTrue(inputGeoJSONPointString.equals(outputGeoJSONPointString));
		
		System.out.println("Generated GeoJSON      : " + outputGeoJSONPointString);
		
	}
	
	@Test
	public void testParseWriteGeoJSONFeatureCollection(){
		
		String featureCollectionString = "{ \"type\": \"FeatureCollection\",                                       "+
				"  \"features\": [                                                        "+
				"    { \"type\": \"Feature\",                                             "+
				"      \"geometry\": {\"type\": \"Point\", \"coordinates\": [102.0, 0.5]},"+
				"      \"properties\": {\"prop0\": \"value0\"}                            "+
				"      },                                                                 "+
				"    { \"type\": \"Feature\",                                             "+
				"      \"geometry\": {                                                    "+
				"        \"type\": \"LineString\",                                        "+
				"        \"coordinates\": [                                               "+
				"          [102.0, 0.0], [103.0, 1.0], [104.0, 0.0], [105.0, 1.0]         "+
				"          ]                                                              "+
				"        },                                                               "+
				"      \"properties\": {                                                  "+
				"        \"prop0\": \"value0\"                                           "+
				"        }                                                                "+
				"      },                                                                 "+
				"    { \"type\": \"Feature\",                                             "+
				"       \"geometry\": {                                                   "+
				"         \"type\": \"Polygon\",                                          "+
				"         \"coordinates\": [                                              "+
				"           [ [100.0, 0.0], [101.0, 0.0], [101.0, 1.0],                   "+
				"             [100.0, 1.0], [100.0, 0.0] ]                                "+
				"           ]                                                             "+
				"       },                                                                "+
				"       \"properties\": {                                                 "+
				"         \"prop0\": \"value0\"                              "+
				"         }                                                               "+
				"       }                                                                 "+
				"     ]                                                                   "+
				"   }                                                                     ";
				
		InputStream in = new ByteArrayInputStream(featureCollectionString.getBytes());
		
		GeoJSONParser theParser = new GeoJSONParser();

		String mimetype = theParser.getSupportedFormats()[0];
		
		System.out.println("Trying to parse GeoJSON: " + featureCollectionString);
		
		IData theBinding = theParser.parse(in, mimetype,
				null);
		
		if(!(theBinding instanceof GTVectorDataBinding)){
			Assert.fail();
		}

		try {
			in.close();
		} catch (IOException e) {
			System.out.println("Failed to close ByteArrayInputStream containing input GeoJSON.");
		}
		
		Assert.assertTrue(theBinding.getPayload() != null);
		
		try {
			InputStream is = dataHandler.generateStream(theBinding, mimetype, null);
			
			IData theGeneratedParsedBinding = theParser.parse(is, mimetype,
					null);
			
			if(!(theGeneratedParsedBinding instanceof GTVectorDataBinding)){
				Assert.fail();
			}
			
			try {
				is.close();
			} catch (IOException e) {
				System.err.println("Failed to close InputStream containing input GeoJSON.");
			}
			
			Assert.assertTrue(theBinding.getPayload() != null);
			
		} catch (IOException e) {
			System.err.println("Failed to generate stream from GTVectorDataBinding.");
		}
		
	}	

	@Test
	public void testParseWriteGeoJSONFeature(){
		
		String featureCollectionString = "{\"type\":\"Feature\", \"properties\":{}, \"geometry\":{\"type\":\"Polygon\", \"coordinates\":[[[56.390622854233, 29.90625500679], [67.640622854233, 49.59375500679], [82.406247854233, 39.75000500679], [69.749997854233, 23.57813000679], [56.390622854233, 29.90625500679]]]}, \"crs\":{\"type\":\"name\", \"properties\":{\"name\":\"EPSG:4326\"}}}";
				
		InputStream in = new ByteArrayInputStream(featureCollectionString.getBytes());
		
		GeoJSONParser theParser = new GeoJSONParser();

		String mimetype = theParser.getSupportedFormats()[0];
		
		System.out.println("Trying to parse GeoJSON: " + featureCollectionString);
		
		IData theBinding = theParser.parse(in, mimetype,
				null);
		
		if(!(theBinding instanceof GTVectorDataBinding)){
			Assert.fail();
		}

		try {
			in.close();
		} catch (IOException e) {
			System.out.println("Failed to close ByteArrayInputStream containing input GeoJSON.");
		}
		
		Assert.assertTrue(theBinding.getPayload() != null);
		
		try {
			InputStream is = dataHandler.generateStream(theBinding, mimetype, null);
			
			IData theGeneratedParsedBinding = theParser.parse(is, mimetype,
					null);
			
			if(!(theGeneratedParsedBinding instanceof GTVectorDataBinding)){
				Assert.fail();
			}
			
			try {
				is.close();
			} catch (IOException e) {
				System.err.println("Failed to close InputStream containing input GeoJSON.");
			}
			
			Assert.assertTrue(theBinding.getPayload() != null);
			
		} catch (IOException e) {
			System.err.println("Failed to generate stream from GTVectorDataBinding.");
		}
		
	}	
	
	@Override
	protected void initializeDataHandler() {
		dataHandler = new GeoJSONGenerator();		
	}

	
}
