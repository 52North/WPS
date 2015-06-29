/**
 * ﻿Copyright (C) 2007 - 2015 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.server.algorithm.difference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.geotools.feature.DefaultFeatureCollections;
import org.geotools.feature.FeatureCollection;
import org.n52.wps.io.GTHelper;
import org.n52.wps.io.SchemaRepository;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.server.AbstractSelfDescribingAlgorithm;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Geometry;



public class DifferenceAlgorithm extends AbstractSelfDescribingAlgorithm {
	
	private static Logger LOGGER = LoggerFactory.getLogger(DifferenceAlgorithm.class);
	
	public DifferenceAlgorithm() {
		super();
	}

	private List<String> errors = new ArrayList<String>();
	public List<String> getErrors() {
		return errors;
	}
	
	
	
	public Map<String, IData> run(Map<String, List<IData>> inputData) {
		/*----------------------Polygons Input------------------------------------------*/
		if(inputData==null || !inputData.containsKey("Polygons1")){
			throw new RuntimeException("Error while allocating input parameters");
		}
		List<IData> dataList = inputData.get("Polygons1");
		if(dataList == null || dataList.size() != 1){
			throw new RuntimeException("Error while allocating input parameters");
		}
		IData firstInputData = dataList.get(0);
				
		FeatureCollection polygons = ((GTVectorDataBinding) firstInputData).getPayload();
		
		/*----------------------LineStrings Input------------------------------------------*/
		if(inputData==null || !inputData.containsKey("Polygons2")){
			throw new RuntimeException("Error while allocating input parameters");
		}
		List<IData> dataListLS = inputData.get("Polygons2");
		if(dataListLS == null || dataListLS.size() != 1){
			throw new RuntimeException("Error while allocating input parameters");
		}
		IData secondInputData = dataListLS.get(0);
				
		FeatureCollection lineStrings = ((GTVectorDataBinding) secondInputData).getPayload();
		
		
		System.out.println("****************************************************************");
		System.out.println("difference algorithm started");
		System.out.println("polygons size = " + polygons.size());
		System.out.println("lineStrings size = " + lineStrings.size());
		
		FeatureCollection featureCollection = DefaultFeatureCollections.newCollection();
		
		Iterator polygonIterator = polygons.iterator();
		int j = 1;
		
		String uuid = UUID.randomUUID().toString();
		while(polygonIterator.hasNext()){
			SimpleFeature polygon = (SimpleFeature) polygonIterator.next();

		
			Iterator lineStringIterator = lineStrings.iterator();
			int i = 1;
			System.out.println("Polygon = " + j +"/"+ polygons.size());
			SimpleFeatureType featureType = null; 
			while(lineStringIterator.hasNext()){
				SimpleFeature lineString = (SimpleFeature) lineStringIterator.next();
				Geometry lineStringGeometry = null;
				lineStringGeometry = (Geometry) lineString.getDefaultGeometry();
				
				try{	
					Geometry polygonGeometry = (Geometry) polygon.getDefaultGeometry();
					Geometry intersection = polygonGeometry.difference(lineStringGeometry);
					if(i==1){
						 featureType = GTHelper.createFeatureType(polygon.getProperties(), intersection, uuid, polygon.getFeatureType().getCoordinateReferenceSystem());
						 QName qname = GTHelper.createGML3SchemaForFeatureType(featureType);
						 SchemaRepository.registerSchemaLocation(qname.getNamespaceURI(), qname.getLocalPart());
					}
					
				
					Feature resultFeature = GTHelper.createFeature(""+j+"_"+i, intersection,featureType, polygon.getProperties());
					if(resultFeature!=null){
								
						featureCollection.add(resultFeature);
						System.out.println("result feature added. resultCollection = " + featureCollection.size());
					}
				}catch(Exception e){
						e.printStackTrace();
					}
				
				i++;
			}
			j++;
			//if(featureCollection.size()>10){
			//	break;
			//}
		}
		
		
		HashMap<String,IData> resulthash = new HashMap<String,IData>();
		resulthash.put("result", new GTVectorDataBinding(featureCollection));
		return resulthash;
	}
	

	
	
	
	public Class getInputDataType(String id) {
		return GTVectorDataBinding.class;
	
	}

	public Class getOutputDataType(String id) {
		return GTVectorDataBinding.class;
	}
	
	@Override
	public List<String> getInputIdentifiers() {
		List<String> identifierList =  new ArrayList<String>();
		identifierList.add("Polygons1");
		identifierList.add("Polygons2");
		return identifierList;
	}

	@Override
	public List<String> getOutputIdentifiers() {
		List<String> identifierList =  new ArrayList<String>();
		identifierList.add("result");
		return identifierList;
	}
	
}