/**
 * ﻿Copyright (C) 2013 - 2014 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.matlab;

import org.n52.wps.io.data.IComplexData;

import com.google.common.base.Preconditions;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class MatlabFileBinding implements IComplexData {
    private static final long serialVersionUID = 1L;
    private final String mimeType;
    private final String schema;
    private final byte[] payload;

    public MatlabFileBinding(byte[] payload, String mimeType, String schema) {
        this.mimeType = Preconditions.checkNotNull(mimeType);
        this.payload = Preconditions.checkNotNull(payload);
        this.schema = schema;
    }

    public void dispose() {
    }

    public byte[] getPayload() {
        return this.payload;
    }

    public Class<?> getSupportedClass() {
        return byte[].class;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getSchema() {
        return schema;
    }

}
