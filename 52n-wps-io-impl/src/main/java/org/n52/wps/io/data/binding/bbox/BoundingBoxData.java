/**
 * ﻿Copyright (C) 2007
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */
package org.n52.wps.io.data.binding.bbox;

import static com.google.common.base.Preconditions.checkArgument;

import org.n52.wps.io.data.IBBOXData;

/**
 * TODO JavaDoc
 * @author Christian Autermann
 */
public class BoundingBoxData implements IBBOXData {
    private static final long serialVersionUID = -3000224272877489674L;
    private final double[] lowerCorner;
    private final double[] upperCorner;
    private final int dimensions;
    private final String crs;

    public BoundingBoxData(double[] lowerCorner,
                           double[] upperCorner, String crs) {
        checkArgument(lowerCorner.length == upperCorner.length);
        this.lowerCorner = lowerCorner;
        this.upperCorner = upperCorner;
        this.dimensions = lowerCorner.length;
        this.crs = crs;
    }

    @Override
    public String getCRS() {
        return crs;
    }

    @Override
    public int getDimension() {
        return dimensions;
    }

    @Override
    public double[] getLowerCorner() {
        return lowerCorner;
    }

    @Override
    public double[] getUpperCorner() {
        return upperCorner;
    }

    @Override
    public BoundingBoxData getPayload() {
        return this;
    }

    @Override
    public Class<BoundingBoxData> getSupportedClass() {
        return BoundingBoxData.class;
    }
}