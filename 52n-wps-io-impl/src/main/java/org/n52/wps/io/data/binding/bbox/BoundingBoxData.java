/*
 * Copyright (C) 2007-2015 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *       • Apache License, version 2.0
 *       • Apache Software License, version 1.0
 *       • GNU Lesser General Public License, version 3
 *       • Mozilla Public License, versions 1.0, 1.1 and 2.0
 *       • Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
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