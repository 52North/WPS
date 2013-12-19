package com.github.autermann.wps.matlab.transform;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.autermann.matlab.value.MatlabScalar;
import com.github.autermann.matlab.value.MatlabValue;

/**
 * TODO JavaDoc
 * @author Christian Autermann
 */
public class IntegerTransformation extends LiteralTransformation {

    @Override
    public MatlabValue transformInput(IData value) {
        if (value.getPayload() instanceof Integer) {
            return new MatlabScalar((Integer) value.getPayload());
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    protected IData fromScalar(double value) {
        Double v = Double.valueOf(value);
        return new LiteralIntBinding(v.intValue());
    }

}
