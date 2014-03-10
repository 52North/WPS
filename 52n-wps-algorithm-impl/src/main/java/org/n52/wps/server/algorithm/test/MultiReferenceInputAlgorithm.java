/**
 * ﻿Copyright (C) 2007 - 2014 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.server.algorithm.test;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.geotools.feature.FeatureCollection;
import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.ComplexDataInput;
import org.n52.wps.algorithm.annotation.ComplexDataOutput;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;

@Algorithm(version = "1.1.0", title="for testing multiple inputs by reference")
public class MultiReferenceInputAlgorithm extends AbstractAnnotatedAlgorithm {

    private static Logger LOGGER = LoggerFactory.getLogger(MultiReferenceInputAlgorithm.class);

    public MultiReferenceInputAlgorithm() {
        super();
    }
    
    private FeatureCollection result;
    private List<FeatureCollection> data;
    
    @ComplexDataOutput(identifier = "result", binding = GTVectorDataBinding.class)
    public FeatureCollection getResult() {
        return result;
    }

    @ComplexDataInput(identifier = "data", binding = GTVectorDataBinding.class, minOccurs=1, maxOccurs=2)
    public void setData(List<FeatureCollection> data) {
        this.data = data;
    }

    @Execute
    public void runBuffer() {
    	result = data.get(0);
    }
}