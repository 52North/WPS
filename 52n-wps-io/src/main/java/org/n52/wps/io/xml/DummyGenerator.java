/***************************************************************
 This implementation provides a framework to publish processes to the
web through the  OGC Web Processing Service interface. The framework 
is extensible in terms of processes and data handlers. It is compliant 
to the WPS version 0.4.0 (OGC 05-007r4). 

 Copyright (C) 2006 by con terra GmbH

 Authors: 
	Janne Kovanen, Finnish Geodetic Institute, Finland

 Contact: Albert Remke, con terra GmbH, Martin-Luther-King-Weg 24,
 48155 Muenster, Germany, 52n@conterra.de

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 version 2 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program (see gnu-gpl v2.txt); if not, write to
 the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA  02111-1307, USA or visit the web page of the Free
 Software Foundation, http://www.fsf.org.

 ***************************************************************/
package org.n52.wps.io.xml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.n52.wps.PropertyDocument.Property;

import org.apache.log4j.Logger;
import org.n52.wps.io.IStreamableGenerator;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/** This class can be used to bypass the other generators that 
 * create from XMLBeans objects XML data. The methods return 
 * the given object stringified.
 * 
 * @author janne
 */
public class DummyGenerator extends AbstractXMLStringGenerator 
		implements IStreamableGenerator {
	private static Logger LOGGER = Logger.getLogger(DummyGenerator.class);
	private static String[] SUPPORTED_SCHEMAS = new String[]{
		"http://www.w3.org/2001/XMLSchema#String"};
	
	public String[] getSupportedSchemas() {
		return SUPPORTED_SCHEMAS;
	}

	/**
	 * The generator supports all schemas.
	 */
	public boolean isSupportedSchema(String schema) {
		//for(int i=0;i<SUPPORTED_SCHEMAS.length;i++)
		//	if(SUPPORTED_SCHEMAS[i].equals(schema))
		//		return true;
		//return false;
		return true;
	}
	
	/**
	 * Called if output is given straight in the return XML.
	 * @note The method is supposed to convert an single object to a 
	 * single node (element, comment etc.).
	 */ 
	public String generateXML(Object stringifyable) {
		return stringifyable.toString();
	}
	
	/** 
	 * Called if output is given straight in the return XML.
	 * @note The method is supposed to convert an single object to a 
	 * single node (element, comment etc.).
	 */ 
	public Node generateXML(Object stringifyable, String schema) {
		File f = null;
		FileWriter writer = null;
		try {
			f = File.createTempFile("dummy", "xml");
			writer = new FileWriter(f);
			this.write(stringifyable, writer);
			writer.close();
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			factory.setIgnoringComments(false); // Comments are great... though in this case they might tell some false things after the data has been altered!
			DocumentBuilder builder = factory.newDocumentBuilder();

			Node node = builder.parse(f); // Error
			return node;
		}
		catch (IOException ex){
			ex.printStackTrace();
			LOGGER.error("The dummy generator threw a IO exception. " + ex.getMessage());
			throw new RuntimeException(ex);
		} catch(ParserConfigurationException pc_ex) {
			pc_ex.printStackTrace();
			LOGGER.error("The dummy generator threw a parser configuration exception. " + pc_ex.getMessage());
			throw new RuntimeException(pc_ex);
		} catch(SAXException sax_ex) {
			LOGGER.error("The dummy generator threw a SAX exception. " + sax_ex.getMessage());
			throw new RuntimeException(sax_ex);
		}
		finally {
			if (writer != null) try { writer.close(); } catch (Exception e) { }
			if (f != null) f.delete();
		}
	}

	/** 
	 * Called if output is given as reference.
	 */
	public void write(Object coll, Writer writer) {
		try {
			writer.append(coll.toString() + "\n");
			writer.flush();
		} catch(IOException io_ex) {
			LOGGER.error("The dummy generator could not stringify the given object.");
			throw new RuntimeException("The dummy generator could not stringify the given object.");
		}
	}

	/**
	 * 
	 */
	public OutputStream generate(Object coll) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Writer writer = new OutputStreamWriter(baos);
		this.write(coll, writer);
		try {
			writer.close();
		} catch(IOException ioe) {
			LOGGER.warn("Could not close the output stream writer in dummy generator.");
		}
		return baos;
	}

	public String[] getSupportedRootClasses() {
		return new String[]{};
	}
	
	// TODO Check method purpose.
	public boolean isSupportedEncoding(String encoding) {
		return true;
	}

	// TODO Check method purpose.
	public boolean isSupportedRootClass(String clazzName) {
		return false;
	}

	
}