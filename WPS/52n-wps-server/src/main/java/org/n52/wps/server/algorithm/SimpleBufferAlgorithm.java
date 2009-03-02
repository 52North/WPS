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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.IllegalAttributeException;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.server.AbstractAlgorithm;
import org.n52.wps.server.AbstractObservableAlgorithm;

import com.vividsolutions.jts.geom.Geometry;

public class SimpleBufferAlgorithm extends AbstractObservableAlgorithm {
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
		  for (Iterator ia = fcA.iterator(); ia.hasNext(); ) {
			/********* How to publish percentage results *************/
			i= i+1;
//			percentage = (i/totalNumberOfFeatures)*100;
//			this.setChanged();
//			this.notifyObservers(percentage.intValue());
			/*********************/
			Feature fa = (Feature) ia.next();
			Geometry ga = fa.getDefaultGeometry();
			Geometry result = null;
			result = runBuffer(ga, width);
			if (result != null || !result.getGeometryType().equals("MultiPolygon")) {
				try {
					fa.setDefaultGeometry(result);
				}
				catch(IllegalAttributeException e) {
					throw new RuntimeException("resultGeometry is not compliant to featureType", e);
				}
			}
			else {
				LOGGER.warn("GeometryCollections are not supported, or result null. Original dataset will be returned");
			}
		  }
		  return fcA;
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

		
}