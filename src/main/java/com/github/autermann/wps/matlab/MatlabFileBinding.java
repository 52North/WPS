package com.github.autermann.wps.matlab;

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
