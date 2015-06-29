/**
 * ﻿Copyright (C) 2007 - 2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *       • Apache License, version 2.0
 *       • Apache Software License, version 1.0
 *       • GNU Lesser General Public License, version 3
 *       • Mozilla Public License, versions 1.0, 1.1 and 2.0
 *       • Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * As an exception to the terms of the GPL, you may copy, modify,
 * propagate, and distribute a work formed by combining 52°North WPS
 * GeoTools Modules with the Eclipse Libraries, or a work derivative of
 * such a combination, even if such copying, modification, propagation, or
 * distribution would otherwise violate the terms of the GPL. Nothing in
 * this exception exempts you from complying with the GPL in all respects
 * for all of the code used other than the Eclipse Libraries. You may
 * include this exception and its grant of permissions when you distribute
 * 52°North WPS GeoTools Modules. Inclusion of this notice with such a
 * distribution constitutes a grant of such permissions. If you do not wish
 * to grant these permissions, remove this paragraph from your
 * distribution. "52°North WPS GeoTools Modules" means the 52°North WPS
 * modules using GeoTools functionality - software licensed under version 2
 * or any later version of the GPL, or a work based on such software and
 * licensed under the GPL. "Eclipse Libraries" means Eclipse Modeling
 * Framework Project and XML Schema Definition software distributed by the
 * Eclipse Foundation and licensed under the Eclipse Public License Version
 * 1.0 ("EPL"), or a work based on such software and licensed under the EPL.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.wps.io.datahandler.parser;

import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;

import org.n52.iceland.w3c.W3CConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Theodor Foerster, ITC
 *
 */
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
		String schemaLocationAttr = attributes.getValue(W3CConstants.NS_XSI, W3CConstants.SCHEMA_LOCATION);
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
