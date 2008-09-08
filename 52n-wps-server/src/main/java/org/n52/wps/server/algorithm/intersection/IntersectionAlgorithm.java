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
package org.n52.wps.server.algorithm.intersection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.geotools.feature.AttributeType;
import org.geotools.feature.DefaultFeatureCollections;
import org.geotools.feature.DefaultFeatureTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.n52.wps.server.AbstractAlgorithm;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

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

public class IntersectionAlgorithm extends AbstractAlgorithm {
	
	private static Logger LOGGER = Logger.getLogger(IntersectionAlgorithm.class);
	
	public IntersectionAlgorithm() {
		super();
	}

	public String errors = "";
	public String getErrors() {
		return errors;
	}
	
	
	
	public HashMap run(Map layers, Map parameters) {
		FeatureCollection polygons = (FeatureCollection)layers.get("Polygons");
		FeatureCollection lineStrings = (FeatureCollection)layers.get("LineStrings");
		System.out.println("****************************************************************");
		System.out.println("intersection started");
		System.out.println("polygons size = " + polygons.size());
		System.out.println("lineStrings size = " + lineStrings.size());
		
		FeatureCollection featureCollection = DefaultFeatureCollections.newCollection();
		
		Iterator polygonIterator = polygons.iterator();
		int j = 1;
		while(polygonIterator.hasNext()){
			Feature polygon = (Feature) polygonIterator.next();
			Iterator lineStringIterator = lineStrings.iterator();
			int i = 1;
			System.out.println("Polygon = " + j +"/"+ polygons.size());
			while(lineStringIterator.hasNext()){
				//System.out.println("Polygon = " + j + "LineString =" + i +"/"+lineStrings.size());
				
				Feature lineString = (Feature) lineStringIterator.next();
				Geometry lineStringGeometry = null;
				if(lineString.getDefaultGeometry()==null && lineString.getNumberOfAttributes()>0 &&lineString.getAttribute(0) instanceof Geometry){
					lineStringGeometry = (Geometry)lineString.getAttribute(0);
				}else{
					lineStringGeometry = lineString.getDefaultGeometry();
				}
				try{
					Geometry intersection = polygon.getDefaultGeometry().intersection(lineStringGeometry);
					Feature resultFeature = createFeature(intersection);
					if(resultFeature!=null){
					//	Iterator featureCollectionIterator = featureCollection.iterator();
					//	while(featureCollectionIterator.hasNext()){
					//		Feature existsingFeature = (Feature) featureCollectionIterator.next();
						/*	if(!existsingFeature.getDefaultGeometry().covers(intersection)){
								featureCollection.add(resultFeature);
							}
							if(existsingFeature.getDefaultGeometry().coveredBy(intersection)){
								featureCollectionIterator = null;
								featureCollection.remove(existsingFeature);
								featureCollection.add(resultFeature);
								break;
							}*/
							
					//	}
						
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
		System.out.println("preresult");
		HashMap<String,Object> resulthash = new HashMap<String,Object>();
		resulthash.put("result", featureCollection);
		System.out.println("result = " + featureCollection.size());
		return resulthash;
	}
	
	private Feature createFeature(Geometry geometry) {
		DefaultFeatureTypeFactory typeFactory = new DefaultFeatureTypeFactory();
		typeFactory.setName("gmlPacketFeatures");
		AttributeType pointType = org.geotools.feature.AttributeTypeFactory.newAttributeType( "LineString", LineString.class);
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
		catch(IllegalAttributeException e) {
			
		}
		return feature;
	}
	
	
}