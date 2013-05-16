package org.n52.wps.server.request.strategy;

import java.io.FilterInputStream;
import java.io.InputStream;

/**
 *
 * @author tkunicki
 */
public class ReferenceInputStream extends FilterInputStream {
    
    private final String mimeType;
    private final String encoding;
    
    public ReferenceInputStream(InputStream inputStream, String mimeType, String encoding) {
        super(inputStream);
        this.mimeType = mimeType;
        this.encoding = encoding;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public String getEncoding() {
        return encoding;
    }
}
