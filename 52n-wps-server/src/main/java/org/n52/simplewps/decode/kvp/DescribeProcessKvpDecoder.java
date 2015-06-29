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
package org.n52.simplewps.decode.kvp;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.n52.iceland.coding.decode.Decoder;
import org.n52.iceland.coding.decode.DecoderKey;
import org.n52.iceland.coding.decode.OperationDecoderKey;
import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.exception.ows.concrete.UnsupportedDecoderInputException;
import org.n52.iceland.ogc.wps.Wps1Constants;
import org.n52.iceland.ogc.wps.WpsConstants;
import org.n52.iceland.request.AbstractServiceRequest;
import org.n52.iceland.util.http.MediaTypes;
import org.n52.simplewps.request.operator.DescribeProcessRequest;

public class DescribeProcessKvpDecoder implements Decoder<AbstractServiceRequest<?>, Map<String, String>> {
    private static final DecoderKey KVP_DECODER_KEY_TYPE = new OperationDecoderKey(WpsConstants.WPS,
            Wps1Constants.SERVICEVERSION, WpsConstants.Operations.DescribeProcess.name(), MediaTypes.APPLICATION_KVP);

    @Override
    public Set<DecoderKey> getKeys() {
        return Collections.singleton(KVP_DECODER_KEY_TYPE);
    }

	@Override
	public DescribeProcessRequest decode(Map<String, String> objectToDecode)
			throws OwsExceptionReport, UnsupportedDecoderInputException {		
		
		DescribeProcessRequest dscpr = new DescribeProcessRequest();
		
		dscpr.setService(objectToDecode.get("service"));
		dscpr.setVersion(objectToDecode.get("version"));
		dscpr.setProcessIdentifier(objectToDecode.get("identifier"));
		
		return dscpr;
	}

}
