/*
 * Copyright (C) 2013 Christian Autermann
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.github.autermann.wps.matlab;

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

    public InputStream generateStream(IData data, String mimeType, String schema)
            throws IOException {
        return new ByteArrayInputStream(((MatlabFileBinding) data).getPayload());
    }

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
