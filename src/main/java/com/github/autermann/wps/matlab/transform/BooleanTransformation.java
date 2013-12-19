package com.github.autermann.wps.matlab.transform;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.autermann.matlab.value.MatlabBoolean;
import com.github.autermann.matlab.value.MatlabValue;

/**
 * TODO JavaDoc
 * @author Christian Autermann
 */
public class BooleanTransformation extends LiteralTransformation {

    @Override
    public MatlabValue transformInput(IData value) {
        if (value.getPayload() instanceof Boolean) {
            return MatlabBoolean.fromBoolean((Boolean) value.getPayload());
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    protected IData fromBoolean(boolean value) {
        return new LiteralBooleanBinding(value);
    }

    @Override
    protected IData fromScalar(double value) {
        return new LiteralBooleanBinding(value > 0);
    }

}
