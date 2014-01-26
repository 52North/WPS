package com.github.autermann.wps.matlab.description;

import org.n52.wps.io.data.IData;

import com.github.autermann.wps.matlab.transform.LiteralType;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class MatlabLiteralOutputDescription extends MatlabOutputDescription {
    private LiteralType type;
    private String unit;

    public LiteralType getType() {
        return type;
    }

    public void setType(LiteralType type) {
        this.type = type;
    }

    @Override
    public Class<? extends IData> getBindingClass() {
        return getType().getBindingClass();
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public boolean hasUnit() {
        return this.unit != null && !this.unit.isEmpty();
    }
}
