package org.n52.wps.matlab.description;

import org.n52.matlab.connector.value.MatlabType;
import org.n52.wps.io.data.IData;


/**
 * @author Christian Autermann
 */
public interface MatlabTyped {
    Class<? extends IData> getBindingClass();

    MatlabType getMatlabType();
}
