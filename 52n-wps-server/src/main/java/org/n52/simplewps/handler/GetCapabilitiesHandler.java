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
package org.n52.simplewps.handler;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Inject;

import org.n52.iceland.binding.Binding;
import org.n52.iceland.binding.BindingRepository;
import org.n52.iceland.coding.OperationKey;
import org.n52.iceland.ds.OperationHandler;
import org.n52.iceland.ds.OperationHandlerKey;
import org.n52.iceland.exception.ows.NoApplicableCodeException;
import org.n52.iceland.exception.ows.OwsExceptionReport;
import org.n52.iceland.ogc.ows.Constraint;
import org.n52.iceland.ogc.ows.DCP;
import org.n52.iceland.ogc.ows.OwsOperation;
import org.n52.iceland.ogc.ows.OwsParameterValuePossibleValues;
import org.n52.iceland.ogc.wps.WpsConstants;
import org.n52.iceland.util.collections.MultiMaps;
import org.n52.iceland.util.collections.SetMultiMap;
import org.n52.iceland.util.http.HTTPHeaders;
import org.n52.iceland.util.http.HTTPMethods;
import org.n52.iceland.util.http.MediaType;

public class GetCapabilitiesHandler implements OperationHandler {

	@Inject
	private BindingRepository bindingRepository;
	
	@Override
	public Set<OperationHandlerKey> getKeys() {
		OperationHandlerKey key = new OperationHandlerKey(WpsConstants.WPS, WpsConstants.Operations.GetCapabilities.name());
		return Collections.singleton(key);
	}

	@Override
	public String getOperationName() {		
		return WpsConstants.Operations.GetCapabilities.name();
	}

	@Override
	public OwsOperation getOperationsMetadata(String service, String version)
			throws OwsExceptionReport {
        Map<String, Set<DCP>> dcp = getDCP(new OperationKey(service, version, WpsConstants.Operations.GetCapabilities.name()));
        if (dcp == null || dcp.isEmpty()) {
//            LOG.debug("Operation {} for Service {} not available due to empty DCP map.", getOperationName(), "WPS");
            return null;
        }
        OwsOperation operation = new OwsOperation();
        operation.setDcp(dcp);
        operation.setOperationName(getOperationName());
//        setOperationsMetadata(operation, service, version);
        return operation;
	}

    /**
     * Get the HTTP DCPs for a operation
     *
     * @param decoderKey
     *            the decoderKey
     * @return Map with DCPs for the service operation
     *
     * @throws OwsExceptionReport
     */
    protected Map<String, Set<DCP>> getDCP(OperationKey decoderKey) throws OwsExceptionReport {
        SetMultiMap<String, DCP> dcps = MultiMaps.newSetMultiMap();
        String serviceURL = "localhost";

        try {
            // TODO support for operation/method specific supported request and
            // response mediatypes
            for (Entry<String, Binding> entry : bindingRepository.getBindingsByPath().entrySet()) {
                String url = serviceURL + entry.getKey();
                Binding binding = entry.getValue();
                Constraint constraint = null;
                if (binding.getSupportedEncodings() != null && !binding.getSupportedEncodings().isEmpty()) {
                    SortedSet<String> ss = new TreeSet<>();
                    for (MediaType mt : binding.getSupportedEncodings()) {
                        ss.add(mt.toString());
                    }
                    constraint = new Constraint(HTTPHeaders.CONTENT_TYPE, new OwsParameterValuePossibleValues(ss));
                }
                if (binding.checkOperationHttpGetSupported(decoderKey)) {
                    dcps.add(HTTPMethods.GET, new DCP(url + "?", constraint));
                }
                if (binding.checkOperationHttpPostSupported(decoderKey)) {
                    dcps.add(HTTPMethods.POST, new DCP(url, constraint));
                }
                if (binding.checkOperationHttpPutSupported(decoderKey)) {
                    dcps.add(HTTPMethods.PUT, new DCP(url, constraint));
                }
                if (binding.checkOperationHttpDeleteSupported(decoderKey)) {
                    dcps.add(HTTPMethods.DELETE, new DCP(url, constraint));
                }
            }
        } catch (Exception e) {
            // FIXME valid exception
            throw new NoApplicableCodeException().causedBy(e);
        }

        return dcps;
    }
	
}
