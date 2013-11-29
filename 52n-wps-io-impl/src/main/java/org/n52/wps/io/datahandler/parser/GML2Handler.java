/***************************************************************
Copyright © 2007 52°North Initiative for Geospatial Open Source Software GmbH

 Author: Theodor Foerster, ITC

 Contact: Andreas Wytzisk, 
 52°North Initiative for Geospatial Open Source SoftwareGmbH, 
 Martin-Luther-King-Weg 24,
 48155 Muenster, Germany, 
 info@52north.org

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 version 2 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; even without the implied WARRANTY OF
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program (see gnu-gpl v2.txt). If not, write to
 the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA 02111-1307, USA or visit the Free
 Software Foundation’s web page, http://www.fsf.org.

 ***************************************************************/
package org.n52.wps.io.datahandler.parser;

import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class GML2Handler extends DefaultHandler {
	
	private Logger LOGGER = LoggerFactory.getLogger(GML2Handler.class);
	// private static String SCHEMA = "http://www.opengis.net/wfs";
	private String  schemaUrl;
	private String nameSpaceURI;
	private boolean rootVisited = false;
	private Map<String, String> namespaces = new HashMap<String, String>();
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException { 
		super.startElement(uri, localName, qName, attributes);
		if(rootVisited) {
			return;
		}
		// check if root is a xml-beans element.
		if(localName.equals("xml-fragment")) {
			return;
		}
		rootVisited = true;
		String schemaLocationAttr = attributes.getValue(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation");
		if(schemaLocationAttr == null) {
			LOGGER.debug("schemaLocation attribute is not set correctly with namespace");
			schemaLocationAttr = attributes.getValue("xsi:schemaLocation");
			if(schemaLocationAttr == null){
				schemaLocationAttr = attributes.getValue("schemaLocation");
			}
		}
		String[] locationStrings = schemaLocationAttr.replace("  ", " ").split(" ");
		if(locationStrings.length % 2 != 0) {
			LOGGER.debug("schemaLocation does not reference locations correctly, odd number of whitespace separated addresses");
			return;
		}
		for(int i = 0; i< locationStrings.length; i++) {
			if(i % 2 == 0 && !locationStrings[i].equals("http://www.opengis.net/wfs") && !locationStrings[i].equals("http://www.opengis.net/gml") && !locationStrings[i].equals("")){
				nameSpaceURI = locationStrings[i];
				schemaUrl = locationStrings[i + 1];
				return;
			}
				
		}
	}

	public String getSchemaUrl(){
		return schemaUrl;
	}

	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		super.startPrefixMapping(prefix, uri);
		namespaces.put(prefix, uri);
		
	}

	public String getNameSpaceURI() {
		return nameSpaceURI;
	}

	
	

}
