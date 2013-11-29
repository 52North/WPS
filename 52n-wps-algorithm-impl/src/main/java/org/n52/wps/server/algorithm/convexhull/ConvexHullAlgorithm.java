/***************************************************************
 This implementation provides a framework to publish processes to the
web through the  OGC Web Processing Service interface. The framework 
is extensible in terms of processes and data handlers. It is compliant 
to the WPS version 0.4.0 (OGC 05-007r4). 

 Copyright (C) 2006 by con terra GmbH

 Authors: 
	Theodor Foerster, ITC, Enschede, the Netherlands
	Carsten Priess, Institute for geoinformatics, University of
Muenster, Germany
	Timon Ter Braak, University of Twente, the Netherlands,
	Benjamin Proﬂ, Institute for geoinformatics, University of
Muenster, Germany


 Contact: Albert Remke, con terra GmbH, Martin-Luther-King-Weg 24,
 48155 Muenster, Germany, 52n@conterra.de

 This program is free software; you can redistribute it and/or
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

		Feature feature = createFeature(out, featureCollection.getSchema().getCoordinateReferenceSystem());
		
		FeatureCollection fOut = DefaultFeatureCollections.newCollection();
		
		fOut.add(feature);

		HashMap<String, IData> result = new HashMap<String, IData>();

		result.put("RESULT",
				new GTVectorDataBinding(fOut));
		return result;
	}
	
	private Feature createFeature(Geometry geometry, CoordinateReferenceSystem crs) {
		String uuid = UUID.randomUUID().toString();
		SimpleFeatureType featureType = GTHelper.createFeatureType(geometry, uuid, crs);
		GTHelper.createGML3SchemaForFeatureType(featureType);
		
		Feature feature = GTHelper.createFeature("0", geometry, featureType);
		
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
