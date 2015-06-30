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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.ogc.om.OmConstants;
import org.n52.iceland.util.http.MediaTypes;
import org.n52.sos.decode.OmDecoderv20;
import org.n52.sos.ogc.om.OmObservation;
import org.n52.wps.io.data.binding.complex.GenericXMLDataBinding;
import org.n52.wps.io.data.binding.complex.OMObservationBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.opengis.om.x20.NamedValuePropertyType;
import net.opengis.om.x20.OMObservationType;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 * 
 * @since 4.0.0
 * 
 * TODO add javadoc
 * TODO remove return null and throw exceptions
 *
 */
public class OMParser extends AbstractParser {

	private static Logger LOGGER = LoggerFactory.getLogger(GenericXMLDataParser.class);

	public OMParser(){
		super();
		supportedIDataTypes.add(OMObservationBinding.class);
	}
	
	@Override
	public OMObservationBinding parse(InputStream stream, String mimeType, String schema) {	
		if (!validateInput(mimeType,schema,stream) ){
			return null;
		}
		FileOutputStream fos = null;
		File tempFile;
		try {
			tempFile = File.createTempFile("wps", "tmp");
			finalizeFiles.add(tempFile); // mark for final delete
			fos = new FileOutputStream(tempFile);
			int i = stream.read();
			while (i != -1) {
				fos.write(i);
				i = stream.read();
			}
			fos.flush();
			fos.close();
		} catch (IOException e) {
			LOGGER.error("Problems while reading stream and write to tmp file. Exception thrown!", e);
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					LOGGER.error("Could not close FileOutputStream. Exception thrown!", e);
				}
			}
		}
		
		GenericXMLDataBinding xmlData = new GenericXMLDataParser().parse(stream, mimeType, schema);
		
		if (xmlData == null || xmlData.getPayload() == null) {
			LOGGER.error("Input stream could not be parsed to generic XML.");
			return null;
		}
		
		if (xmlData.getPayload() instanceof OMObservationType || 
				xmlData.getPayload() instanceof NamedValuePropertyType) {
			try {
				Object parsedObject = new OmDecoderv20().decode(xmlData.getPayload());
				if (parsedObject instanceof OmObservation) {
					return new OMObservationBinding((OmObservation) parsedObject);
				}
				LOGGER.error("O&M decoder output not supported. Type received: '{}'.", parsedObject.getClass().getName());
				
			} catch (OwsExceptionReport e) {
				LOGGER.error("O&M data could not be parsed. Exception thrown!", e);
				return null;
			}
		}
		LOGGER.error("XML document type not supported: '{}'.", xmlData.getPayload().getClass().getName());
		
		return null;
	}

	private boolean validateInput(String mimeType, String schema, InputStream stream) {
		if (mimeType != null && 
				!mimeType.isEmpty() &&
				mimeType.equals(MediaTypes.APPLICTION_OM_20.toString()) &&
				schema != null &&
				!schema.isEmpty() && 
				schema.equals(OmConstants.NS_OM_2) &&
				stream != null) {
			return true;
		}
		LOGGER.debug("Input not valid: Mimetype: '{}', Schema: '{}'",
				mimeType, schema);
		return false;
	}
	
}
