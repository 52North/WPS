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
package org.n52.wps.server.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.InputType;

import org.apache.xmlbeans.XmlException;
import org.junit.Assert;
import org.junit.Test;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.request.strategy.DefaultReferenceStrategy;
import org.n52.wps.server.request.strategy.IReferenceStrategy;

public class HttpInputTest {
	
	static final String sampleFileName = "src/test/resources/InputTestExecuteSample.xml";
	
	@Test
	public void testHttpInput() throws XmlException, IOException{
		
		
		// Arrange
		File sample = new File(sampleFileName);
		ExecuteDocument execDoc = ExecuteDocument.Factory.parse(sample);
		InputType[] inputArray = execDoc.getExecute().getDataInputs().getInputArray();
		
		// Act & Assert
		for (InputType currentInput : inputArray){
			System.out.println("Testing input " + currentInput.getIdentifier().getStringValue());
			
			IReferenceStrategy strategy = new DefaultReferenceStrategy();
			try {
				InputStream is = strategy.fetchData(currentInput);
				Assert.assertNotNull(is);
				printStream(is);
				
			} catch (ExceptionReport e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private static void printStream (InputStream is) throws IOException{
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		String line = null;
		while((line = in.readLine()) != null) {
		  System.out.println(line);
		}
	}
}
