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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
		
		assertTrue(theBinding.getPayload() != null);
		
		InputStream generatedStream = null;
		
		try {
			generatedStream = dataHandler.generateStream(theBinding, mimetype, null);
			
		} catch (IOException e) {
			LOGGER.error("Failed to generate result inputstream.");
			fail();
		}
		
		String outputWKTPolygonString = "";
		
		int bite = -1;
		
		try {
			while ((bite = generatedStream.read()) != -1) {
				outputWKTPolygonString = outputWKTPolygonString.concat(String.valueOf((char)bite));
			}
		} catch (IOException e) {
			LOGGER.error("Failed to read result inputstream.");
			fail();
		}
		
		try {
			generatedStream.close();
		} catch (IOException e) {
			LOGGER.warn("Failed to close generated stream containing result WKT.");
		}
		
		assertTrue(inputWKTPolygonString.equals(outputWKTPolygonString));
		
		LOGGER.info("Generated WKT      : " + outputWKTPolygonString);
		
	}

	@Override
	protected void initializeDataHandler() {
		dataHandler = new WKTGenerator();
		
	}

	
}
