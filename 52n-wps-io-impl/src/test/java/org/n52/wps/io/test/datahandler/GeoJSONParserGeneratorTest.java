/**
 * Copyright (C) 2013
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 * 
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 * 
 * This program is free software; you can redistribute and/or modify it under 
 * the terms of the GNU General Public License version 2 as published by the 
 * Free Software Foundation.
 * 
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 * 
 */
package org.n52.wps.io.test.datahandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.n52.wps.io.data.binding.complex.JTSGeometryBinding;
import org.n52.wps.io.datahandler.generator.GeoJSONGenerator;
import org.n52.wps.io.datahandler.parser.GeoJSONParser;

/**
 * Test class for GeoJSON parser and generator
 * @author BenjaminPross(bpross-52n)
 *
 */
public class GeoJSONParserGeneratorTest extends AbstractTestCase<GeoJSONGenerator> {

	protected Logger LOGGER = Logger.getLogger(GeoJSONGenerator.class);
	
	public void testGenerator() {
		
		if(!isDataHandlerActive()){
			return;
		}
		
		String inputGeoJSONPointString = "{\"type\":\"Point\",\"coordinates\":[100,0.0]}";
		
		InputStream in = new ByteArrayInputStream(inputGeoJSONPointString.getBytes());
		
		GeoJSONParser theParser = new GeoJSONParser();

		String mimetype = theParser.getSupportedFormats()[0];
		
		LOGGER.info("Trying to parse GeoJSON: " + inputGeoJSONPointString);
		
		JTSGeometryBinding theBinding = theParser.parse(in, mimetype,
				null);

		try {
			in.close();
		} catch (IOException e) {
			LOGGER.warn("Failed to close ByteArrayInputStream containing input GeoJSON.");
		}
		
		assertTrue(theBinding.getPayload() != null);
		
		InputStream generatedStream = null;
		
		try {
			generatedStream = dataHandler.generateStream(theBinding, mimetype, null);
			
		} catch (IOException e) {
			LOGGER.error("Failed to generate result inputstream.");
			fail();
		}
		
		String outputGeoJSONPointString = "";
		
		int bite = -1;
		
		try {
			while ((bite = generatedStream.read()) != -1) {
				outputGeoJSONPointString = outputGeoJSONPointString.concat(String.valueOf((char)bite));
			}
		} catch (IOException e) {
			LOGGER.error("Failed to read result inputstream.");
			fail();
		}
		
		try {
			generatedStream.close();
		} catch (IOException e) {
			LOGGER.warn("Failed to close generated stream containing result GeoJSON.");
		}
		
		assertTrue(inputGeoJSONPointString.equals(outputGeoJSONPointString));
		
		LOGGER.info("Generated GeoJSON     : " + outputGeoJSONPointString);
		
	}

	@Override
	protected void initializeDataHandler() {
		dataHandler = new GeoJSONGenerator();		
	}

	
}
