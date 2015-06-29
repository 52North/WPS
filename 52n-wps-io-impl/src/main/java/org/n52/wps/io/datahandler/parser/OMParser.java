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

import java.io.InputStream;

import org.n52.iceland.ogc.om.OmConstants;
import org.n52.iceland.util.http.MediaTypes;
import org.n52.wps.io.data.binding.complex.OMBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 * 
 * @since 4.0.0
 *
 */
public class OMParser extends AbstractParser {

	private static Logger LOGGER = LoggerFactory.getLogger(GenericXMLDataParser.class);

	public OMParser(){
		super();
		supportedIDataTypes.add(OMBinding.class);
	}
	
	@Override
	public OMBinding parse(InputStream input, String mimeType, String schema) {	
		if (!validateInput(mimeType,schema) ){
			return null;
		}
		// TODO Implement
		return null;
	}

	private boolean validateInput(String mimeType, String schema) {
		if (mimeType != null && 
				!mimeType.isEmpty() &&
				mimeType.equals(MediaTypes.APPLICATION_OM_20.toString()) &&
				schema != null &&
				!schema.isEmpty() & 
				schema.equals(OmConstants.NS_OM_2)) {
			return true;
		}
		LOGGER.debug("Input not valid: Mimetype: '{}', Schema: '{}'",
				mimeType, schema);
		return false;
	}
	
}
