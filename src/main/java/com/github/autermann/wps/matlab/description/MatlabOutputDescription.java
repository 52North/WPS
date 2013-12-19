package com.github.autermann.wps.matlab.description;

import com.github.autermann.matlab.value.MatlabType;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public abstract class MatlabOutputDescription extends AbstractMatlabIODescription {

    private MatlabType type;

    public MatlabType getMatlabType() {
        return type;
    }

    public void setMatlabType(MatlabType type) {
        this.type = type;
    }

}
