/*
 * Copyright (C) 2013 Christian Autermann
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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
