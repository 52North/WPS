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

import org.apache.log4j.Logger;
import org.geotools.feature.AttributeType;
import org.geotools.feature.DefaultFeatureCollections;
import org.geotools.feature.DefaultFeatureTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.SchemaException;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.server.AbstractAlgorithm;

import com.vividsolutions.jts.algorithm.ConvexHull;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class ConvexHullAlgorithm extends AbstractAlgorithm {

	Logger LOGGER = Logger.getLogger(ConvexHullAlgorithm.class);
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

		Coordinate[] coordinateArray = new Coordinate[featureCollection.size()];
		
		int counter = 0;
		
		while (iter.hasNext()) {
			Feature feature = iter.next();

			if (feature.getDefaultGeometry() == null) {
				throw new NullPointerException(
						"defaultGeometry is null in feature id: "
								+ feature.getID());
			}
			
			Geometry geom = feature.getDefaultGeometry();
			
			if(geom instanceof Point){
				coordinateArray[counter] = ((Point)geom).getCoordinate();
				counter++;
			}
			
		}	
		
		ConvexHull convexHull = new ConvexHull(coordinateArray, new GeometryFactory());		
		
		Geometry out = convexHull.getConvexHull();

		Feature feature = createFeature(out);
		
		FeatureCollection fOut = DefaultFeatureCollections.newCollection();
		
		fOut.add(feature);

		HashMap<String, IData> result = new HashMap<String, IData>();

		result.put("RESULT",
				new GTVectorDataBinding(fOut));
		return result;
	}
	
	private Feature createFeature(Geometry geometry) {
		DefaultFeatureTypeFactory typeFactory = new DefaultFeatureTypeFactory();
			typeFactory.setName("gmlPacketFeatures");
		AttributeType pointType = org.geotools.feature.AttributeTypeFactory.newAttributeType( "Polygon", Polygon.class);
		typeFactory.addType(pointType);
		
		FeatureType featureType;
		try {
			featureType = typeFactory.getFeatureType();
			
		}
		catch (SchemaException e) {
			throw new RuntimeException(e);
		}
		Feature feature = null;		
		
		try{
			 feature = featureType.create(new Object[]{geometry});	 
		}
		catch(Exception e) {
			LOGGER.debug(e);
		}
		return feature;
	}	
	
}
