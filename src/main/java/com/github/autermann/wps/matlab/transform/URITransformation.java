package com.github.autermann.wps.matlab.transform;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.literal.LiteralAnyURIBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.autermann.matlab.value.MatlabString;
import com.github.autermann.matlab.value.MatlabValue;

/**
 * TODO JavaDoc
 * @author Christian Autermann
 */
public class URITransformation extends LiteralTransformation {

    @Override
    public MatlabValue transformInput(IData data) {
        if (data.getPayload() instanceof URI) {
            return new MatlabString(((URI) data.getPayload()).toString());
        } else if (data.getPayload() instanceof URL) {
            return new MatlabString(((URL) data.getPayload()).toString());
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    protected IData fromString(String value) {
        try {
            return new LiteralAnyURIBinding(new URI(value));
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

}
