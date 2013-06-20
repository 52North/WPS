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
 */package org.n52.wps.io.test.datahandler.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.datahandler.parser.GML3BasicParser4Files;
import org.n52.wps.io.test.datahandler.AbstractTestCase;

/**
 * This class is for testing the GML3BasicParser4Files.
 * 
 * @author Benjamin Pross(bpross-52n)
 *
 */
public class GML3BasicParser4FilesTest extends AbstractTestCase<GML3BasicParser4Files> {

	public void testParser() {
		
		if(!isDataHandlerActive()){
			return;
		}

		String testFilePath = projectRoot
				+ "/52n-wps-io-impl/src/test/resources/spearfish_restricted_sites_gml3.xml";
		
		try {
			testFilePath = URLDecoder.decode(testFilePath, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			System.err.println(e1);
			fail(e1.getMessage());
		}

		InputStream input = null;

		try {
			input = new FileInputStream(new File(testFilePath));
		} catch (FileNotFoundException e) {
			fail(e.getMessage());
		}

		GenericFileDataBinding theBinding = dataHandler.parse(input,
				"text/xml; subtype=gml/3.1.1",
				"http://schemas.opengis.net/gml/3.1.1/base/feature.xsd");

		assertNotNull(theBinding.getPayload());
		assertNotNull(theBinding.getPayload().getBaseFile(true).exists());

	}

	@Override
	protected void initializeDataHandler() {
		dataHandler = new GML3BasicParser4Files();		
	}

}
