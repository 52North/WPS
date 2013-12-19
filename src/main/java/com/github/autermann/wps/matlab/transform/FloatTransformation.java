package com.github.autermann.wps.matlab.transform;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.literal.LiteralFloatBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.autermann.matlab.value.MatlabScalar;
import com.github.autermann.matlab.value.MatlabValue;

/**
 * TODO JavaDoc
 * @author Christian Autermann
 */
public class FloatTransformation extends LiteralTransformation {

    @Override
    public MatlabValue transformInput(IData value) {
        if (value.getPayload() instanceof Float) {
            return new MatlabScalar((Float) value.getPayload());
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    protected IData fromScalar(double value) {
        return new LiteralFloatBinding(Double.valueOf(value).floatValue());
    }

}
