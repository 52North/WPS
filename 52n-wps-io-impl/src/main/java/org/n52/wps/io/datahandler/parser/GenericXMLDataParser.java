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
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.wps.io.datahandler.parser;

import java.io.IOException;
import java.io.InputStream;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.wps.io.data.binding.complex.GenericXMLDataBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericXMLDataParser extends AbstractParser {
	
	private static Logger LOGGER = LoggerFactory.getLogger(GenericXMLDataParser.class);

	public GenericXMLDataParser(){
		super();
		supportedIDataTypes.add(GenericXMLDataBinding.class);
	}
	
	@Override
	public boolean isSupportedSchema(String schema) {
		//no schema checks
		return true;
	}
	
	@Override
	public GenericXMLDataBinding parse(InputStream input, String mimeType, String schema) {	
		
		XmlObject xmlData = XmlObject.Factory.newInstance();
		
		try {
			xmlData = XmlObject.Factory.parse(input);
		} catch (XmlException|IOException e) {
			LOGGER.error("Could not parse inputstream as XMLObject.", e);
		}	
		
		GenericXMLDataBinding xmlDataBinding = new GenericXMLDataBinding(xmlData);
		
		return xmlDataBinding;
	}

}
