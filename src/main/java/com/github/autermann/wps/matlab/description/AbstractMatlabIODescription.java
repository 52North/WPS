package com.github.autermann.wps.matlab.description;

import org.n52.wps.io.data.IData;

/**
 * TODO JavaDoc
 * @author Christian Autermann
 */
public abstract class AbstractMatlabIODescription extends AbstractMatlabDescription {

    public abstract Class<? extends IData> getBindingClass();

}
