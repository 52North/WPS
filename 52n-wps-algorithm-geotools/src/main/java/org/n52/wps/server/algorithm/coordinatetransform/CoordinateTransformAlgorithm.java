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
package org.n52.wps.server.algorithm.coordinatetransform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.geotools.feature.DefaultFeatureCollections;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.n52.wps.io.GTHelper;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractSelfDescribingAlgorithm;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class CoordinateTransformAlgorithm extends
		AbstractSelfDescribingAlgorithm {
	
	private static Logger LOGGER = LoggerFactory.getLogger(CoordinateTransformAlgorithm.class);

	private final String inputIdentifierFeatures = "InputData";
	private final String inputIdentifierTransformation = "Transformation";
	private final String inputIdentifierTargetReferenceSystem = "TargetCRS";
	private final String inputIdentifierSourceReferenceSystem = "SourceCRS";
	private final String outputIdentifierResult = "TransformedData";
	private SimpleFeatureType featureType;

	@Override
	public List<String> getInputIdentifiers() {
		List<String> identifierList = new ArrayList<String>();
		identifierList.add(inputIdentifierFeatures);
		identifierList.add(inputIdentifierSourceReferenceSystem);
		identifierList.add(inputIdentifierTargetReferenceSystem);
		identifierList.add(inputIdentifierTransformation);
		return identifierList;
	}

	@Override
	public List<String> getOutputIdentifiers() {
		List<String> identifierList = new ArrayList<String>();
		identifierList.add(outputIdentifierResult);
		return identifierList;
	}

	@Override
	public Class<?> getInputDataType(String id) {
		if (id.equalsIgnoreCase(inputIdentifierFeatures)) {
			return GTVectorDataBinding.class;
		} else if (id.equals(inputIdentifierTargetReferenceSystem)||
				id.equals(inputIdentifierSourceReferenceSystem)||
				id.equals(inputIdentifierTransformation)) {
			return LiteralStringBinding.class;
		}
		return null;
	}
	
	@Override
	public Class<?> getOutputDataType(String id) {
		return GTVectorDataBinding.class;
	}

	@SuppressWarnings( { "unchecked" })
	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData) {

		if (inputData == null
				|| !inputData.containsKey(inputIdentifierFeatures)
				|| !inputData.containsKey(inputIdentifierTargetReferenceSystem)) {
			LOGGER.error("Error while allocating input parameters");
			throw new RuntimeException(					
					"Error while allocating input parameters");
		}

		List<IData> dataList = inputData.get(inputIdentifierFeatures);
		if (dataList == null || dataList.size() != 1) {
			throw new RuntimeException(
					"Error while allocating input parameters");
		}

		IData firstInputData = dataList.get(0);
		FeatureCollection<?, ?> featureCollection = ((GTVectorDataBinding) firstInputData)
				.getPayload();

		FeatureIterator<?> featureIterator = featureCollection.features();

		List<IData> secondDataList = inputData
				.get(inputIdentifierTargetReferenceSystem);		
		if (secondDataList == null || secondDataList.size() != 1) {
			throw new RuntimeException(
					"Error while allocating input parameters");
		}

		IData secondInputData = secondDataList.get(0);

		// crs in epsg code
		String crs = ((LiteralStringBinding) secondInputData).getPayload();

		CoordinateReferenceSystem toCRS = null;

		try {

			toCRS = CRS.decode(crs);

		} catch (Exception e) {
			throw new RuntimeException(
					"Could not determine target CRS. Valid EPSG code needed.",
					e);
		}

		if (toCRS == null) {
			throw new RuntimeException(
					"Could not determine target CRS. Valid EPSG code needed.");
		}
		
		List<IData> thirdDataList = inputData
				.get(inputIdentifierSourceReferenceSystem);
		if (thirdDataList == null || thirdDataList.size() != 1) {
			throw new RuntimeException(
					"Error while allocating input parameters");
		}
		
		IData thirdInputData = thirdDataList.get(0);

		// crs in epsg code
		String fromCRSString = ((LiteralStringBinding) thirdInputData).getPayload();

		CoordinateReferenceSystem fromCRS = null;

		try {

			fromCRS = CRS.decode(fromCRSString);

		} catch (Exception e) {
			throw new RuntimeException(
					"Could not determine target CRS. Valid EPSG code needed.",
					e);
		}

		if (fromCRS == null) {
			throw new RuntimeException(
					"Could not determine target CRS. Valid EPSG code needed.");
		}
		

		FeatureCollection fOut = DefaultFeatureCollections.newCollection();

		try {

			MathTransform tx = CRS.findMathTransform(fromCRS, toCRS, true);

			int coordinates = 0;
			
			while (featureIterator.hasNext()) {

				SimpleFeature feature = (SimpleFeature) featureIterator.next();

				Geometry geometry = (Geometry) feature.getDefaultGeometry();

				coordinates = coordinates + geometry.getCoordinates().length;
				
				Coordinate[] coords = geometry.getCoordinates();
				
				for (Coordinate coordinate : coords) {
					Coordinate k = new Coordinate();
					k = JTS.transform(coordinate, k, tx);
//					System.out.println(k);
				}
				
				Geometry newGeometry = JTS.transform(geometry, tx);

				Feature newFeature = createFeature(feature.getID(),
						newGeometry, toCRS, feature.getProperties());

				fOut.add(newFeature);
			}
						

		} catch (Exception e) {
			throw new RuntimeException("Error while transforming", e);
		}

		HashMap<String, IData> result = new HashMap<String, IData>();

		result.put(outputIdentifierResult, new GTVectorDataBinding(fOut));
		return result;
	}

	private Feature createFeature(String id, Geometry geometry,
			CoordinateReferenceSystem crs, Collection<Property> properties) {
		String uuid = UUID.randomUUID().toString();
		
		if(featureType == null){
		featureType = GTHelper.createFeatureType(properties,
				geometry, uuid, crs);
		GTHelper.createGML3SchemaForFeatureType(featureType);
		}

		Feature feature = GTHelper.createFeature(id, geometry, featureType,
				properties);

		return feature;
	}

}
