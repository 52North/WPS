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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.n52.wps.PropertyDocument.Property;

/**
 * This parser can be used to bypass the other parsers that 
 * create from XML data XMLBeans objects. The methods return 
 * the same data that was given to them.
 * 
 * @author Janne Kovanen
 */
public class DummyParser extends AbstractXMLParser {
	
	private static String[] SUPPORTED_SCHEMAS = new String[]{
		"http://www.w3.org/2001/XMLSchema#String"
	};

	public DummyParser() {
	}

	/**
	 * Returns supported schemas.
	 */
	public String[] getSupportedSchemas() {
		return SUPPORTED_SCHEMAS;
	}

	/** 
	 * Dummy parser. Returns the original XML document.
	 */
	public String parseXML(String gml) throws ClassCastException {
		return gml;
	}
	
	/** 
	 * Dummy parser. Returns the original XML document read from the stream.
	 */
	public String parseXML(InputStream stream) {
		BufferedReader br = new BufferedReader(new InputStreamReader(stream));
		StringBuilder sb = new StringBuilder();
		String line = null;
		try {		
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
			br.close();
		} catch(IOException io_ex) {
			return null; // Null should never be returned? Empty collection? Exception?
		}
		return sb.toString();
	}
	
	/**
	 * Returns supported schemas - All schemas are supported.
	 */
	public boolean isSupportedSchema(String schema) {
		//for(int i=0;i<SUPPORTED_SCHEMAS.length;i++)
		//	if(SUPPORTED_SCHEMAS[i].equals(schema))
		//		return true;
		//return false;
		return true;
	}

	public Object parse(InputStream input) {
		return parseXML(input);
	}

	// TODO Check the root names.
	public String[] getSupportedRootClasses() {
		return new String[]{};
	}

	public boolean isSupportedEncoding(String encoding) {
		return true;
	}

	// TODO Check the root names.
	public boolean isSupportedRootClass(String clazzName) {
		return false;
	}

	
}
