/**
 * ﻿Copyright (C) 2013 - 2014 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.matlab.transform;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import org.n52.matlab.connector.value.MatlabArray;
import org.n52.matlab.connector.value.MatlabCell;
import org.n52.matlab.connector.value.MatlabFile;
import org.n52.matlab.connector.value.MatlabMatrix;
import org.n52.matlab.connector.value.MatlabString;
import org.n52.matlab.connector.value.MatlabStruct;
import org.n52.matlab.connector.value.MatlabValue;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.bbox.BoundingBoxData;
import org.n52.wps.server.ExceptionReport;

import org.n52.wps.matlab.MatlabFileBinding;

import org.n52.wps.matlab.description.MatlabBoundingBoxOutputDescription;
import org.n52.wps.matlab.description.MatlabComplexOutputDescription;
import org.n52.wps.matlab.description.MatlabLiteralInputDescription;
import org.n52.wps.matlab.description.MatlabLiteralOutputDescription;
import org.n52.wps.matlab.description.MatlabProcessInputDescription;
import org.n52.wps.matlab.description.MatlabProcessOutputDescription;

import com.google.common.collect.Lists;

public class MatlabValueTransformer {

    public MatlabValue transform(MatlabProcessInputDescription desc,
                                 List<? extends IData> data) throws
            ExceptionReport {
        if (desc == null) {
            throw new ExceptionReport("No input defintion",
                                      ExceptionReport.NO_APPLICABLE_CODE);
        }
        if (data == null) {
            throw new ExceptionReport(String
                    .format("No data to convert for input %s", desc.getId()),
                                      ExceptionReport.NO_APPLICABLE_CODE);
        }
        if (!desc.getOccurence().isInBounds(BigInteger.valueOf(data.size()))) {
            throw new ExceptionReport(String
                    .format("Invalid occurence of input %s", desc.getId()),
                                      ExceptionReport.INVALID_PARAMETER_VALUE);
        }
        if (data.isEmpty()) {
            return null;
        } else if (data.size() == 1) {
            return transformSingleInput(desc, data.get(0));
        } else {
            MatlabCell cell = transformMultiInput(desc, data);
            if (desc instanceof MatlabLiteralInputDescription &&
                ((MatlabLiteralInputDescription) desc).getLiteralType().isNumber()) {

                int i = 0;
                double[] values = new double[data.size()];
                for (MatlabValue v : cell) {
                    values[i++] = v.asScalar().value();
                }
                return new MatlabArray(values);
            } else {
                return cell;
            }
        }
    }

    private MatlabCell transformMultiInput(MatlabProcessInputDescription definition,
                                           List<? extends IData> data)
            throws ExceptionReport {
        List<MatlabValue> values = Lists.newLinkedList();
        for (IData d : data) {
            values.add(transformSingleInput(definition, d));
        }
        return new MatlabCell(values);
    }

    private MatlabValue transformSingleInput(MatlabProcessInputDescription definition,
                                             IData data) throws ExceptionReport {
        if (definition instanceof MatlabLiteralInputDescription) {
            MatlabLiteralInputDescription name
                    = (MatlabLiteralInputDescription) definition;
            try {
                return name.getLiteralType().getTransformation().transformInput(data);
            } catch (IllegalArgumentException e) {
                throw new ExceptionReport(String
                        .format("Can not convert %s for input %s", data, definition
                                .getId()), ExceptionReport.INVALID_PARAMETER_VALUE, e);
            }
        } else if (data instanceof BoundingBoxData) {
            BoundingBoxData boundingBoxData = (BoundingBoxData) data;
            MatlabStruct val = new MatlabStruct();
            val.set("crs", new MatlabString(boundingBoxData.getCRS()));
            val.set("bbox", new MatlabMatrix(new double[][] {
                boundingBoxData.getLowerCorner(),
                boundingBoxData.getUpperCorner()
            }));
            return val;
        } else if (data instanceof MatlabFileBinding) {
            return new MatlabFile(((MatlabFileBinding) data).getPayload());
        } else {
            throw new ExceptionReport("Can not convert input " + data,
                                      ExceptionReport.INVALID_PARAMETER_VALUE);
        }
    }

    public IData transform(MatlabProcessOutputDescription definition,
                           MatlabValue value)
            throws ExceptionReport {
        if (definition == null) {
            throw new ExceptionReport("No output defintion",
                                      ExceptionReport.NO_APPLICABLE_CODE);
        }

        if (value == null) {
            throw new ExceptionReport(
                    String.format("No data to convert for output %s", definition.getId()),
                    ExceptionReport.NO_APPLICABLE_CODE);
        }

        if (definition instanceof MatlabComplexOutputDescription) {
            return transformComplexOutput(definition, value);
        } else if (definition instanceof MatlabBoundingBoxOutputDescription) {
            return transformBoundingBoxOutput(definition, value);
        } else {
            return transformLiteralOutput(definition, value);
        }
    }

    private IData transformLiteralOutput(MatlabProcessOutputDescription definition,
                                         MatlabValue value)
            throws ExceptionReport {
        try {
            MatlabLiteralOutputDescription literalDefinition = (MatlabLiteralOutputDescription) definition;
            return literalDefinition.getLiteralType().getTransformation().transformOutput(value);
        } catch (IllegalArgumentException e) {
            throw new ExceptionReport(String
                    .format("Can not convert %s for output %s", value, definition
                            .getId()), ExceptionReport.NO_APPLICABLE_CODE, e);
        }
    }

    private IData transformBoundingBoxOutput(MatlabProcessOutputDescription definition,
                                             MatlabValue value)
            throws ExceptionReport {
        if (value.isMatrix()) {
            String crs = value.asStruct().get("crs").asString().value();
            double[][] bbox = value.asStruct().get("bbox").asMatrix().value();
            return new BoundingBoxData(bbox[0], bbox[1], crs);
        } else {
            throw new ExceptionReport(String
                    .format("Can not convert %s for output %s", value, definition
                            .getId()), ExceptionReport.NO_APPLICABLE_CODE);
        }
    }

    private IData transformComplexOutput(MatlabProcessOutputDescription definition,
                                         MatlabValue value)
            throws ExceptionReport {
        MatlabComplexOutputDescription complexDefinition
                = (MatlabComplexOutputDescription) definition;
        if (!value.isFile()) {
            throw new ExceptionReport(String
                    .format("Can not convert %s for output %s", value, definition
                            .getId()), ExceptionReport.NO_APPLICABLE_CODE);
        } else {
            try {
                return new MatlabFileBinding(value.asFile().getContent(),
                        complexDefinition.getDefaultFormat().getMimeType().orElse(null),
                        complexDefinition.getDefaultFormat().getSchema().orElse(null));
            } catch (IOException ex) {
                throw new ExceptionReport(String.format(
                        "Error loading file for output %s", definition
                                .getId()), ExceptionReport.NO_APPLICABLE_CODE);
            }
        }
    }
}
