package com.github.autermann.wps.matlab.description;

import org.n52.wps.io.data.IData;

import com.github.autermann.wps.matlab.MatlabFileBinding;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class MatlabComplexInputDescription extends MatlabInputDescripton {
    private String schema;
    private String mimeType;
    private String encoding;

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    @Override
    public Class<? extends IData> getBindingClass() {
        return MatlabFileBinding.class;
    }
}
