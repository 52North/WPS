package com.github.autermann.wps.matlab.description;

import org.n52.wps.io.data.IData;

import com.github.autermann.matlab.value.MatlabType;

/**
 * @author Christian Autermann
 */
public interface MatlabTyped {
    Class<? extends IData> getBindingClass();

    MatlabType getMatlabType();
}
