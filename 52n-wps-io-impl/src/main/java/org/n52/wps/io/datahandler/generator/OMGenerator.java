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
package org.n52.wps.io.datahandler.generator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import org.apache.xmlbeans.XmlObject;
import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.ogc.om.OmConstants;
import org.n52.iceland.ogc.ows.OWSConstants;
import org.n52.iceland.util.http.MediaTypes;
import org.n52.sos.encode.OmEncoderv20;
import org.n52.sos.ogc.om.OmObservation;
import org.n52.sos.util.SosConfiguration;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GenericXMLDataBinding;
import org.n52.wps.io.data.binding.complex.OMObservationBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates XML from {@link OmObservation}.
 * 
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 * 
 * @since 4.0.0
 *
 */
public class OMGenerator extends AbstractGenerator {
	
	private static Logger LOGGER = LoggerFactory.getLogger(OMGenerator.class);
	
	public OMGenerator() {
		super();
		supportedIDataTypes.add(OMObservationBinding.class);
	}

	@Override
	public InputStream generateStream(IData data, String mimeType, String schema)
			throws IOException {
		if (!validateInput(mimeType,schema,data) ){
			return null;
		}
		OmObservation omObservation = (OmObservation) data.getPayload();

		try {
			SosConfiguration.init();
			XmlObject encode = new OmEncoderv20().encode(omObservation, Collections.singletonMap(OWSConstants.HelperValues.DOCUMENT, "42"));

			return new GenericXMLDataGenerator().generateStream(new GenericXMLDataBinding(encode), mimeType, schema);
		} catch (OwsExceptionReport e) {
			LOGGER.error(
					String.format("Could not encode type '%s'. Received object: '%s'. Exception thrown!",
							OmObservation.class.getName(),
							omObservation.toString()), e);
		}
		
		return null;
	}
	
	private boolean validateInput(String mimeType, String schema, IData data) {
		if (mimeType != null && 
				!mimeType.isEmpty() &&
				mimeType.equals(MediaTypes.APPLICATION_OM_20.toString()) &&
				schema != null &&
				!schema.isEmpty() && 
				schema.equals(OmConstants.NS_OM_2) &&
				data != null &&
				data instanceof OMObservationBinding &&
				data.getPayload() instanceof OmObservation) {
			return true;
		}
		LOGGER.debug("Input not valid: Mimetype: '{}', Schema: '{}', Data: '{}'.",
				mimeType, schema, data);
		return false;
	}

}
