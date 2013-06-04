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
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.datahandler.parser.GTBinZippedWKT64Parser;
import org.n52.wps.io.test.datahandler.AbstractTestCase;
import org.opengis.feature.Feature;


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


	public void testParser(){	
		
		if(!isDataHandlerActive()){
			return;
		}
		
		String testFilePath = projectRoot + "/52n-wps-io-impl/src/test/resources/wktgeometries.base64.zip";
		
		try {
			testFilePath = URLDecoder.decode(testFilePath, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			fail(e1.getMessage());
		}
				
		String[] mimetypes = dataHandler.getSupportedFormats();
		
		InputStream input = null;
		
		for (String mimetype : mimetypes) {
			
			try {
				
				input = new Base64InputStream(new FileInputStream(new File(testFilePath)));
			} catch (FileNotFoundException e) {
				fail(e.getMessage());
			} 
			
			GTVectorDataBinding theBinding = dataHandler.parse(input, mimetype, "");
			
			assertNotNull(theBinding.getPayload());
			assertTrue(!theBinding.getPayload().isEmpty());	
			
			FeatureCollection<?, ?> collection = theBinding.getPayload();
			
			FeatureIterator<?> featureIterator = collection.features();
			
			while(featureIterator.hasNext()){
				Feature f = featureIterator.next();
				
				System.out.println(f.getDefaultGeometryProperty());
			}
			
			assertTrue(theBinding.getPayloadAsShpFile().exists());		
			
		}
		
	}

	@Override
	protected void initializeDataHandler() {
		dataHandler = new GTBinZippedWKT64Parser();		
	}
	
}
