/**
 * ﻿Copyright (C) 2007
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
 */
package org.n52.wps.io.test.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import junit.framework.TestCase;

import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.GenericFileDataConstants;

public class GenericFileDataTest extends TestCase{

	public void testUnzipData(){
		
		File f = new File(this.getClass().getProtectionDomain().getCodeSource()
				.getLocation().getFile());

		String projectRoot = f.getParentFile().getParentFile().getParent();	
		
		String testFilePath = projectRoot + "/52n-wps-io-impl/src/test/resources/tasmania_roads.zip";
		
		try {
			testFilePath = URLDecoder.decode(testFilePath, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			fail(e1.getMessage());
		}
		
		InputStream input = null;
		
		/*
		 * create a GenericFileData instance out of a zipped shapefile
		 */
		try {
			input = new FileInputStream(new File(testFilePath));
		} catch (FileNotFoundException e) {
			fail(e.getMessage());
		}
		
		
		GenericFileData genericFileData = new GenericFileData(input, GenericFileDataConstants.MIME_TYPE_ZIPPED_SHP);
		
		String unzippedFilePath = genericFileData.writeData(new File(System.getProperty("java.io.tmpdir")));
		
		assertTrue(unzippedFilePath != null && !unzippedFilePath.equals(""));
		
		
	}
	
}
