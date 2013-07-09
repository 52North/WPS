/***************************************************************
This implementation provides a framework to publish processes to the
web through the  OGC Web Processing Service interface. The framework 
is extensible in terms of processes and data handlers. It is compliant 
to the WPS version 0.4.0 (OGC 05-007r4). 

Copyright (C) 2006 by con terra GmbH

Authors: 
	Bastian Schaeffer, Institute for Geoinformatics, University of
	Muenster, Germany
	


Contact: Albert Remke, con terra GmbH, Martin-Luther-King-Weg 24,
48155 Muenster, Germany, 52n@conterra.de

This printersectionogram is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
version 2 as published by the Free Software Foundation.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program (see gnu-gpl v2.txt); if not, write to
the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA or visit the web page of the Free
Software Foundation, http://www.fsf.org.

Created on: 13.06.2006
***************************************************************/
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