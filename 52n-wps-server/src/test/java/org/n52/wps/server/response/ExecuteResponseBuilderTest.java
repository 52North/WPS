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
package org.n52.wps.server.response;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import net.opengis.wps.x100.DocumentOutputDefinitionType;
import net.opengis.wps.x100.OutputDefinitionType;

import org.junit.Before;
import org.junit.Test;
import org.n52.wps.server.request.ExecuteRequest;
import org.w3c.dom.Document;

/**
 * This class tests the getMimeType method of the ExecuteResponseBuilder class.
 * TODO: Enhance with multiple in-/output tests
 * 
 * @author Benjamin Pross(bpross-52n)
 *
 */
public class ExecuteResponseBuilderTest {

	ExecuteRequest executeRequest;
	private DocumentBuilderFactory fac;

	@Before
	public void setUp() throws Exception {
		System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
				"org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");

		fac = DocumentBuilderFactory.newInstance();
		fac.setNamespaceAware(true);
	}

	@Test
	public void testGetMimeTypeLiteralOutputResponseDoc() {

		try {
			String sampleFileName = "src/test/resources/DTCExecuteLiteralOutputResponseDoc.xml";
			File sampleFile = new File(sampleFileName);

			FileInputStream is = new FileInputStream(sampleFile);

			// parse the InputStream to create a Document
			Document doc = fac.newDocumentBuilder().parse(is);

			is.close();

			executeRequest = new ExecuteRequest(doc);

			/*
			 * only one output here
			 */
			OutputDefinitionType definition = executeRequest.getExecute().getResponseForm().getResponseDocument().getOutputArray(0);
			
			String mimeType = executeRequest.getExecuteResponseBuilder()
					.getMimeType(definition);

			/*
			 * this should be text/plain as LiteralData was requested
			 */
			assertTrue(mimeType.equals("text/plain"));

		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testGetMimeTypeLiteralOutputRawData() {
		
		try {
			String sampleFileName = "src/test/resources/DTCExecuteLiteralOutputRawData.xml";
			File sampleFile = new File(sampleFileName);
			
			FileInputStream is = new FileInputStream(sampleFile);
			
			// parse the InputStream to create a Document
			Document doc = fac.newDocumentBuilder().parse(is);
			
			is.close();
			
			executeRequest = new ExecuteRequest(doc);
			
			/*
			 * only one output here
			 */
			OutputDefinitionType definition = executeRequest.getExecute().getResponseForm().getRawDataOutput();
			
			String mimeType = executeRequest.getExecuteResponseBuilder()
					.getMimeType(definition);
			
			/*
			 * this should be text/plain as LiteralData was requested
			 */
			assertTrue(mimeType.equals("text/plain"));
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testGetMimeTypeComplexOutputRawData() {

		try {
			String sampleFileName = "src/test/resources/DTCExecuteComplexOutputRawDataMimeTiff.xml";
			File sampleFile = new File(sampleFileName);

			FileInputStream is;
			is = new FileInputStream(sampleFile);

			// parse the InputStream to create a Document
			Document doc;
			doc = fac.newDocumentBuilder().parse(is);

			is.close();

			executeRequest = new ExecuteRequest(doc);
			
			/*
			 * only one output here
			 */
			OutputDefinitionType definition = executeRequest.getExecute().getResponseForm().getRawDataOutput();
			String originalMimeType = definition.getMimeType();

			
			String mimeType = executeRequest.getExecuteResponseBuilder()
					.getMimeType(definition);

			/*
			 * this should be the same mime type as requested
			 */
			assertTrue(mimeType.equals(originalMimeType));

		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testGetMimeTypeComplexOutputResponseDoc() {
		
		try {
			String sampleFileName = "src/test/resources/DTCExecuteComplexOutputResponseDocMimeTiff.xml";
			File sampleFile = new File(sampleFileName);
			
			FileInputStream is;
			is = new FileInputStream(sampleFile);
			
			// parse the InputStream to create a Document
			Document doc;
			doc = fac.newDocumentBuilder().parse(is);
			
			is.close();
			
			executeRequest = new ExecuteRequest(doc);
			
			/*
			 * only one output here
			 */
			OutputDefinitionType definition = executeRequest.getExecute().getResponseForm().getResponseDocument().getOutputArray(0);
			String originalMimeType = definition.getMimeType();
			
			
			String mimeType = executeRequest.getExecuteResponseBuilder()
					.getMimeType(definition);
			
			/*
			 * this should be the same mime type as requested
			 */
			assertTrue(mimeType.equals(originalMimeType));
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testGetMimeTypeMultipleComplexOutputsResponseDocPerm1() {
		
		try {
			String sampleFileName = "src/test/resources/MCIODTCExecuteComplexOutputResponseDocPerm1.xml";
			File sampleFile = new File(sampleFileName);
			
			FileInputStream is;
			is = new FileInputStream(sampleFile);
			
			// parse the InputStream to create a Document
			Document doc;
			doc = fac.newDocumentBuilder().parse(is);
			
			is.close();
			
			executeRequest = new ExecuteRequest(doc);
			
			DocumentOutputDefinitionType[] outputs = executeRequest.getExecute().getResponseForm().getResponseDocument().getOutputArray();
			
			for (DocumentOutputDefinitionType documentOutputDefinitionType : outputs) {
				
				String identifier = documentOutputDefinitionType.getIdentifier().getStringValue(); 
				
				String originalMimeType = documentOutputDefinitionType.getMimeType();
							
				String mimeType = executeRequest.getExecuteResponseBuilder()
						.getMimeType(documentOutputDefinitionType);				
				
				if(identifier.contains("Complex")){				
					assertTrue(mimeType.equals(originalMimeType));
				}else{
					assertTrue(mimeType.equals("text/plain"));
				}
				
			}
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testGetMimeTypeMultipleComplexOutputsResponseDocPerm2() {
		
		try {
			String sampleFileName = "src/test/resources/MCIODTCExecuteComplexOutputResponseDocPerm2.xml";
			File sampleFile = new File(sampleFileName);
			
			FileInputStream is;
			is = new FileInputStream(sampleFile);
			
			// parse the InputStream to create a Document
			Document doc;
			doc = fac.newDocumentBuilder().parse(is);
			
			is.close();
			
			executeRequest = new ExecuteRequest(doc);
			
			DocumentOutputDefinitionType[] outputs = executeRequest.getExecute().getResponseForm().getResponseDocument().getOutputArray();
			
			for (DocumentOutputDefinitionType documentOutputDefinitionType : outputs) {
				
				String identifier = documentOutputDefinitionType.getIdentifier().getStringValue(); 
				
				String originalMimeType = documentOutputDefinitionType.getMimeType();
				
				String mimeType = executeRequest.getExecuteResponseBuilder()
						.getMimeType(documentOutputDefinitionType);				
				
				if(identifier.contains("Complex")){				
					assertTrue(mimeType.equals(originalMimeType));
				}else{
					assertTrue(mimeType.equals("text/plain"));
				}
				
			}
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testGetMimeTypeBBOXOutputResponseDoc() {

		try {
			String sampleFileName = "src/test/resources/DTCExecuteBBOXOutputResponseDoc.xml";
			File sampleFile = new File(sampleFileName);

			FileInputStream is;
			is = new FileInputStream(sampleFile);

			// parse the InputStream to create a Document
			Document doc;
			doc = fac.newDocumentBuilder().parse(is);

			is.close();

			executeRequest = new ExecuteRequest(doc);

			/*
			 * only one output here
			 */
			OutputDefinitionType definition = executeRequest.getExecute().getResponseForm().getResponseDocument().getOutputArray(0);
			
			String mimeType = executeRequest.getExecuteResponseBuilder()
					.getMimeType(definition);

			/*
			 * this should be text/plain as BBOXData was requested
			 */
			assertTrue(mimeType.equals("text/plain"));

		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testGetMimeTypeBBOXOutputRawData() {
		
		try {
			String sampleFileName = "src/test/resources/DTCExecuteBBOXOutputRawData.xml";
			File sampleFile = new File(sampleFileName);
			
			FileInputStream is;
			is = new FileInputStream(sampleFile);
			
			// parse the InputStream to create a Document
			Document doc;
			doc = fac.newDocumentBuilder().parse(is);
			
			is.close();
			
			executeRequest = new ExecuteRequest(doc);
			
			/*
			 * only one output here
			 */
			OutputDefinitionType definition = executeRequest.getExecute().getResponseForm().getRawDataOutput();
			
			String mimeType = executeRequest.getExecuteResponseBuilder()
					.getMimeType(definition);
			
			/*
			 * this should be text/plain as BBOXData was requested
			 */
			assertTrue(mimeType.equals("text/plain"));
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

}
