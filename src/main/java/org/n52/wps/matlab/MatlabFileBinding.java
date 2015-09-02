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
