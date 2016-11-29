/*
 * Copyright (C) 2007 - 2015 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.server.algorithm.convexhull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.geotools.feature.DefaultFeatureCollections;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.n52.wps.io.GTHelper;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.server.AbstractSelfDescribingAlgorithm;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.algorithm.ConvexHull;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * @author Benjamin Pross (bpross-52n)
 *
 */
public class ConvexHullAlgorithm extends AbstractSelfDescribingAlgorithm {

    Logger LOGGER = LoggerFactory.getLogger(ConvexHullAlgorithm.class);
    private List<String> errors = new ArrayList<String>();

    public List<String> getErrors() {
        return errors;
    }

    public Class getInputDataType(String id) {
        if (id.equalsIgnoreCase("FEATURES")) {
            return GTVectorDataBinding.class;
        }
        return null;
    }

    public Class getOutputDataType(String id) {
        return GTVectorDataBinding.class;
    }

    public Map<String, IData> run(Map<String, List<IData>> inputData) {

        if (inputData == null || !inputData.containsKey("FEATURES")) {
            throw new RuntimeException(
                    "Error while allocating input parameters");
        }

        List<IData> dataList = inputData.get("FEATURES");
        if (dataList == null || dataList.size() != 1) {
            throw new RuntimeException(
                    "Error while allocating input parameters");
        }

        IData firstInputData = dataList.get(0);
        FeatureCollection featureCollection = ((GTVectorDataBinding) firstInputData)
                .getPayload();

        FeatureIterator iter = featureCollection.features();

        List<Coordinate> coordinateList = new ArrayList<Coordinate>();

        int counter = 0;

        Geometry unifiedGeometry = null;

        while (iter.hasNext()) {
            SimpleFeature  feature = (SimpleFeature) iter.next();

            if (feature.getDefaultGeometry() == null) {
                throw new NullPointerException(
                        "defaultGeometry is null in feature id: "
                                + feature.getID());
            }

            Geometry geom = (Geometry) feature.getDefaultGeometry();

            Coordinate[] coordinateArray = geom.getCoordinates();
            for(Coordinate coordinate : coordinateArray){
                coordinateList.add(coordinate);
            }

        }

        Coordinate[] coordinateArray = new Coordinate[coordinateList.size()];

        for(int i = 0; i<coordinateList.size(); i++){
            coordinateArray[i] = coordinateList.get(i);
        }
        ConvexHull convexHull = new ConvexHull(coordinateArray, new GeometryFactory());

        Geometry out = convexHull.getConvexHull();

        SimpleFeature feature = createFeature(out, featureCollection.getSchema().getCoordinateReferenceSystem());

        List<SimpleFeature> featureList = new ArrayList<>();
        featureList.add(feature);

        HashMap<String, IData> result = new HashMap<String, IData>();

        result.put("RESULT",
                new GTVectorDataBinding(GTHelper.createSimpleFeatureCollectionFromSimpleFeatureList(featureList)));
        return result;
    }

    private SimpleFeature createFeature(Geometry geometry, CoordinateReferenceSystem crs) {
        String uuid = UUID.randomUUID().toString();
        SimpleFeatureType featureType = GTHelper.createFeatureType(geometry, uuid, crs);
        GTHelper.createGML3SchemaForFeatureType(featureType);

        SimpleFeature feature = GTHelper.createFeature("0", geometry, featureType);

        return feature;
    }

    @Override
    public List<String> getInputIdentifiers() {
        List<String> identifierList =  new ArrayList<String>();
        identifierList.add("FEATURES");
        return identifierList;
    }

    @Override
    public List<String> getOutputIdentifiers() {
        List<String> identifierList =  new ArrayList<String>();
        identifierList.add("RESULT");
        return identifierList;
    }
}
