package com.github.autermann.wps.matlab.description;

import com.github.autermann.matlab.value.MatlabType;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public abstract class MatlabInputDescripton extends AbstractMatlabIODescription {

    private long minOccurs;
    private long maxOccurs;

    private MatlabType type;

    public long getMinOccurs() {
        return minOccurs;
    }

    public void setMinOccurs(long minOccurs) {
        this.minOccurs = minOccurs;
    }

    public long getMaxOccurs() {
        return maxOccurs;
    }

    public void setMaxOccurs(long maxOccurs) {
        this.maxOccurs = maxOccurs;
    }

    public MatlabType getMatlabType() {
        return type;
    }

    public void setMatlabType(MatlabType type) {
        this.type = type;
    }

}
