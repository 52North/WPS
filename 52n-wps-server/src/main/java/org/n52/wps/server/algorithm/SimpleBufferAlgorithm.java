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
package org.n52.wps.server.algorithm;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.geotools.feature.DefaultFeatureCollections;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.n52.wps.ServerDocument.Server;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.SchemaRepository;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.server.AbstractSelfDescribingAlgorithm;
import org.n52.wps.server.WebProcessingService;
import org.n52.wps.server.algorithm.ows7.GTHelper;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.geometry.aggregate.MultiCurve;
import org.opengis.geometry.aggregate.MultiSurface;
import org.opengis.geometry.primitive.Curve;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class SimpleBufferAlgorithm extends AbstractSelfDescribingAlgorithm {
	private static Logger LOGGER = Logger.getLogger(SimpleBufferAlgorithm.class);
	private Double percentage;
	
	public SimpleBufferAlgorithm() {
		super();
		
	}

	private List<String> errors = new ArrayList<String>();
	public List<String> getErrors() {
		return errors;
	}
	
	public Map<String, IData> run(Map<String, List<IData>> inputData) {
		if(inputData==null || !inputData.containsKey("data")){
			throw new RuntimeException("Error while allocating input parameters");
		}
		
		List<IData> dataList = inputData.get("data");
		if(dataList == null || dataList.size() != 1){
			throw new RuntimeException("Error while allocating input parameters");
		}
		IData firstInputData = dataList.get(0);
				
		FeatureCollection featureCollection = ((GTVectorDataBinding) firstInputData).getPayload();
		
		if( !inputData.containsKey("width")){
			throw new RuntimeException("Error while allocating input parameters");
		}
		List<IData> widthDataList = inputData.get("width");
		if(widthDataList == null || widthDataList.size() != 1){
			throw new RuntimeException("Error while allocating input parameters");
		}
		Double width = ((LiteralDoubleBinding) widthDataList.get(0)).getPayload();
		
		FeatureCollection fcnew = runBuffer(featureCollection, width);
		HashMap<String,IData> resulthash = new HashMap<String,IData>();
		resulthash.put("result", new GTVectorDataBinding(fcnew));
		return resulthash;
	}
	
	private FeatureCollection runBuffer(FeatureCollection fcA, double width)	{
		  //Collection resultColl = new ArrayList();
		  double i = 0;
		  int totalNumberOfFeatures = fcA.size();
		  String uuid = UUID.randomUUID().toString();
		  FeatureCollection featureCollection = DefaultFeatureCollections.newCollection();
		  SimpleFeatureType featureType = null;
		  for (Iterator ia = fcA.iterator(); ia.hasNext(); ) {
			/********* How to publish percentage results *************/
			i= i+1;
//			percentage = (i/totalNumberOfFeatures)*100;
//			this.setChanged();
//			this.notifyObservers(percentage.intValue());
			/*********************/
			SimpleFeature fa = (SimpleFeature) ia.next();
			Geometry geometry = (Geometry) fa.getDefaultGeometry();
			Geometry result = runBuffer(geometry, width);;
		
			if(i==1){
				 featureType = GTHelper.createFeatureType(fa, result, uuid);
				 QName qname = GTHelper.createSchemaForFeatureType(featureType);
				 SchemaRepository.registerSchemaLocation(qname.getNamespaceURI(), qname.getLocalPart());
				
			}

			if (result != null) {
				SimpleFeature feature = (SimpleFeature) GTHelper.createFeature("ID"+new Double(i).intValue(),result,(SimpleFeatureType) featureType,fa);
				featureCollection.add(feature);
			}
				
			
			else {
				LOGGER.warn("GeometryCollections are not supported, or result null. Original dataset will be returned");
			}
		  }
		  
		  return featureCollection;
		}
	
	
		private Geometry runBuffer(Geometry a, double width) {
		  Geometry result = null;
		  
		  try {
			  
			result = a.buffer(width);
			return result;
		  }
		  catch (RuntimeException ex) {
			// simply eat exceptions and report them by returning null
		  }
		  return null;
		}

		

		public Class getInputDataType(String id) {
			if(id.equalsIgnoreCase("data")){
				return GTVectorDataBinding.class;
			}else if(id.equalsIgnoreCase("width")){
					return LiteralDoubleBinding.class;
			}
			throw new RuntimeException("Could not find datatype for id " + id);
		
		}

		public Class getOutputDataType(String id) {
			return GTVectorDataBinding.class;
		}

		@Override
		public List<String> getInputIdentifiers() {
			List<String> identifierList =  new ArrayList<String>();
			identifierList.add("data");
			identifierList.add("width");
			return identifierList;
		}

		@Override
		public List<String> getOutputIdentifiers() {
			List<String> identifierList =  new ArrayList<String>();
			identifierList.add("result");
			return identifierList;
		}

		
}