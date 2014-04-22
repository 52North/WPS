package com.github.autermann.wps.matlab.description;

import java.util.LinkedHashSet;
import java.util.Set;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.bbox.BoundingBoxData;

public class MatlabBoundingBoxInputDescription extends MatlabInputDescripton {
    private LinkedHashSet<String> crs;

    @Override
    public Class<? extends IData> getBindingClass() {
        return BoundingBoxData.class;
    }

    public Set<String> getCRS() {
        return crs;
    }

    public void setCRS(Set<String> crs) {
        this.crs = new LinkedHashSet<String>(crs);
    }

    public boolean hasCRS() {
        return this.crs != null && !this.crs.isEmpty();
    }
}
