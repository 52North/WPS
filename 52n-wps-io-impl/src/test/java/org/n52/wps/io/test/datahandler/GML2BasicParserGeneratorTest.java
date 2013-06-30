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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.n52.wps.FormatDocument.Format;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.datahandler.generator.GML2BasicGenerator;
import org.n52.wps.io.datahandler.parser.GML2BasicParser;

/**
 * This class is for testing the GML2BasicParser and -Generator.
 * 
 * @author Benjamin Pross(bpross-52n)
 *
 */
public class GML2BasicParserGeneratorTest extends AbstractTestCase<GML2BasicGenerator> {

	public void testParser() {
		
		if(!isDataHandlerActive()){
			return;
		}

		String testFilePath = projectRoot
				+ "/52n-wps-io-impl/src/test/resources/tasmania_roads_gml2.xml";
		
		try {
			testFilePath = URLDecoder.decode(testFilePath, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			fail(e1.getMessage());
		}

		GML2BasicParser parser = new GML2BasicParser();

		Format[] formats = parser.getSupportedFullFormats();
		
		Format format = formats[0];
		
		String mimeType = format.getMimetype();
		String schema = format.getSchema();
		
		InputStream input = null;

		try {
			input = new FileInputStream(new File(testFilePath));
		} catch (FileNotFoundException e) {
			fail(e.getMessage());
		}
		
		GTVectorDataBinding theBinding = parser.parse(input, mimeType, schema);

		assertNotNull(theBinding.getPayload());
		assertTrue(theBinding.getPayloadAsShpFile().exists());
		assertTrue(!theBinding.getPayload().isEmpty());
		
		try {
			InputStream stream = dataHandler.generateStream(theBinding, mimeType, schema);
			
			theBinding = parser.parse(stream, mimeType, schema);

			assertNotNull(theBinding.getPayload());
			assertTrue(theBinding.getPayloadAsShpFile().exists());
			assertTrue(!theBinding.getPayload().isEmpty());
			
		} catch (IOException e) {
			System.err.println(e);
			fail(e.getMessage());
		}

	}

	@Override
	protected void initializeDataHandler() {
		dataHandler = new GML2BasicGenerator();		
	}

}
