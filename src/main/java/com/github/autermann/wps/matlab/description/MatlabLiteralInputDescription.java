package com.github.autermann.wps.matlab.description;

import java.util.List;

import org.n52.wps.io.data.IData;

import com.github.autermann.wps.matlab.transform.LiteralType;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class MatlabLiteralInputDescription extends MatlabInputDescripton {
    private LiteralType type;
    private String defaultValue;
    private List<String> allowedValues;
    private String unit;

    public LiteralType getType() {
        return type;
    }

    public void setType(LiteralType type) {
        this.type = type;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean hasDefaultValue() {
        return this.defaultValue != null;
    }

    public List<String> getAllowedValues() {
        return allowedValues;
    }

    public void setAllowedValues(List<String> allowedValues) {
        this.allowedValues = allowedValues;
    }

    public boolean hasAllowedValues() {
        return this.allowedValues != null;
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
