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
 * As an exception to the terms of the GPL, you may copy, modify,
 * propagate, and distribute a work formed by combining 52°North WPS
 * GeoTools Modules with the Eclipse Libraries, or a work derivative of
 * such a combination, even if such copying, modification, propagation, or
 * distribution would otherwise violate the terms of the GPL. Nothing in
 * this exception exempts you from complying with the GPL in all respects
 * for all of the code used other than the Eclipse Libraries. You may
 * include this exception and its grant of permissions when you distribute
 * 52°North WPS GeoTools Modules. Inclusion of this notice with such a
 * distribution constitutes a grant of such permissions. If you do not wish
 * to grant these permissions, remove this paragraph from your
 * distribution. "52°North WPS GeoTools Modules" means the 52°North WPS
 * modules using GeoTools functionality - software licensed under version 2
 * or any later version of the GPL, or a work based on such software and
 * licensed under the GPL. "Eclipse Libraries" means Eclipse Modeling
 * Framework Project and XML Schema Definition software distributed by the
 * Eclipse Foundation and licensed under the Eclipse Public License Version
 * 1.0 ("EPL"), or a work based on such software and licensed under the EPL.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.wps.server.algorithm;

import java.util.Iterator;
import java.util.UUID;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.geotools.feature.DefaultFeatureCollections;
import org.geotools.feature.FeatureCollection;
import org.n52.wps.io.GTHelper;
import org.n52.wps.io.SchemaRepository;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.ComplexDataInput;
import org.n52.wps.algorithm.annotation.ComplexDataOutput;
import org.n52.wps.algorithm.annotation.LiteralDataInput;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;

@Algorithm(version = "1.1.0")
public class SimpleBufferAlgorithm extends AbstractAnnotatedAlgorithm {

    private static Logger LOGGER = LoggerFactory.getLogger(SimpleBufferAlgorithm.class);
    private Double percentage;

    public SimpleBufferAlgorithm() {
        super();
    }
    
    private FeatureCollection result;
    private FeatureCollection data;
    private double width;

    @ComplexDataOutput(identifier = "result", binding = GTVectorDataBinding.class)
    public FeatureCollection getResult() {
        return result;
    }

    @ComplexDataInput(identifier = "data", binding = GTVectorDataBinding.class)
    public void setData(FeatureCollection data) {
        this.data = data;
    }

    @LiteralDataInput(identifier = "width")
    public void setWidth(double width) {
        this.width = width;
    }

    @Execute
    public void runBuffer() {
        //Collection resultColl = new ArrayList();
        double i = 0;
        int totalNumberOfFeatures = data.size();
        String uuid = UUID.randomUUID().toString();
        result = DefaultFeatureCollections.newCollection();
        SimpleFeatureType featureType = null;
        LOGGER.debug("");
        for (Iterator ia = data.iterator(); ia.hasNext();) {
            /**
             * ******* How to publish percentage results ************
             */
            i = i + 1;
            percentage = (i / totalNumberOfFeatures) * 100;
            this.update(new Integer(percentage.intValue()));

            /**
             * ******************
             */
            SimpleFeature feature = (SimpleFeature) ia.next();
            Geometry geometry = (Geometry) feature.getDefaultGeometry();
            Geometry geometryBuffered = runBuffer(geometry, width);

            if (i == 1) {
                CoordinateReferenceSystem crs = feature.getFeatureType().getCoordinateReferenceSystem();
                if (geometry.getUserData() instanceof CoordinateReferenceSystem) {
                    crs = ((CoordinateReferenceSystem) geometry.getUserData());
                }
                featureType = GTHelper.createFeatureType(feature.getProperties(), geometryBuffered, uuid, crs);
                QName qname = GTHelper.createGML3SchemaForFeatureType(featureType);
                SchemaRepository.registerSchemaLocation(qname.getNamespaceURI(), qname.getLocalPart());

            }

            if (geometryBuffered != null) {
                SimpleFeature createdFeature = (SimpleFeature) GTHelper.createFeature("ID" + new Double(i).intValue(), geometryBuffered, (SimpleFeatureType) featureType, feature.getProperties());
                feature.setDefaultGeometry(geometryBuffered);
                result.add(createdFeature);
            } else {
                LOGGER.warn("GeometryCollections are not supported, or result null. Original dataset will be returned");
            }
        }

    }

    private Geometry runBuffer(Geometry a, double width) {
        Geometry buffered = null;

        try {
            buffered = a.buffer(width);
            return buffered;
        } catch (RuntimeException ex) {
            // simply eat exceptions and report them by returning null
        }
        return null;
    }
}