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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.n52.wps.FormatDocument.Format;
import org.n52.wps.io.IGenerator;
import org.n52.wps.io.IParser;
import org.n52.wps.io.data.IData;

import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteStreams;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class MatlabFileHandler implements IParser, IGenerator {
    @Override
    public IData parse(InputStream input, String mimeType, String schema) {
        try {
            return new MatlabFileBinding(ByteStreams.toByteArray(input),
                                         mimeType, schema);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public IData parseBase64(InputStream input, String mimeType, String schema) {
        return parse(BaseEncoding.base64()
                .decodingStream(new InputStreamReader(input)), mimeType, schema);
    }

    @Override
    public InputStream generateStream(IData data, String mimeType, String schema)
            throws IOException {
        return new ByteArrayInputStream(((MatlabFileBinding) data).getPayload());
    }

    @Override
    public InputStream generateBase64Stream(IData data, String mimeType,
                                            String schema) throws IOException {
        return new ByteArrayInputStream(BaseEncoding.base64()
                .encode(((MatlabFileBinding) data).getPayload()).getBytes());
    }

    @Override
    public boolean isSupportedSchema(String schema) {
        return true;
    }

    @Override
    public boolean isSupportedFormat(String format) {
        return true;
    }

    @Override
    public boolean isSupportedEncoding(String encoding) {
        return true;
    }

    @Override
    public boolean isSupportedDataBinding(Class<?> clazz) {
        return clazz.equals(MatlabFileBinding.class);
    }

    @Override
    public String[] getSupportedSchemas() {
        return null;
    }

    @Override
    public String[] getSupportedFormats() {
        return null;
    }

    @Override
    public String[] getSupportedEncodings() {
        return null;
    }

    @Override
    public Format[] getSupportedFullFormats() {
        return null;
    }

    @Override
    public Class<?>[] getSupportedDataBindings() {
        return new Class<?>[] { MatlabFileBinding.class };
    }

}
