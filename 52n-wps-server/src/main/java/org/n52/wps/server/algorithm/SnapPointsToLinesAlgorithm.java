/***************************************************************
Copyright (C) 2012
by 52 North Initiative for Geospatial Open Source Software GmbH

Contact: Andreas Wytzisk
52 North Initiative for Geospatial Open Source Software GmbH
Martin-Luther-King-Weg 24
48155 Muenster, Germany
info@52north.org

This program is free software; you can redistribute and/or modify it under 
the terms of the GNU General Public License version 2 as published by the 
Free Software Foundation.

This program is distributed WITHOUT ANY WARRANTY; even without the implied
WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License along with
this program (see gnu-gpl v2.txt). If not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
visit the Free Software Foundation web page, http://www.fsf.org.
***************************************************************/

package org.n52.wps.server.algorithm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.geotools.feature.DefaultFeatureCollections;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.NullProgressListener;
import org.n52.wps.io.GTHelper;
import org.n52.wps.io.SchemaRepository;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.server.AbstractSelfDescribingAlgorithm;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.strtree.STRtree;
import com.vividsolutions.jts.linearref.LinearLocation;
import com.vividsolutions.jts.linearref.LocationIndexedLine;

/**
 * Algorithm based on GeoTools User's Guide: 
 * 		http://docs.geotools.org/latest/userguide/library/jts/snap.html
 * @author German Carrillo
 *
 */
public class SnapPointsToLinesAlgorithm extends AbstractSelfDescribingAlgorithm {

	protected static Logger LOGGER = Logger.getLogger(SnapPointsToLinesAlgorithm.class);

	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData) {
		if(inputData==null || !inputData.containsKey("Points")){
			throw new RuntimeException("Error while allocating input parameters");
		}
		
		List<IData> firstDataList = inputData.get("Points");
		if(firstDataList == null || firstDataList.size() != 1){
			throw new RuntimeException("Error while allocating input parameters");
		}
		IData firstInputData = firstDataList.get(0);
				
		FeatureCollection<?,?> pointCollection = ((GTVectorDataBinding) firstInputData).getPayload();

		if(inputData==null || !inputData.containsKey("Lines")){
			throw new RuntimeException("Error while allocating input parameters");
		}
		
		List<IData> secondDataList = inputData.get("Lines");
		if(secondDataList == null || secondDataList.size() != 1){
			throw new RuntimeException("Error while allocating input parameters");
		}
		IData secondInputData = secondDataList.get(0);
				
		FeatureCollection<?,?> lineCollection = ((GTVectorDataBinding) secondInputData).getPayload();
		
		if( !inputData.containsKey("MaximumDistance")){
			throw new RuntimeException("Error while allocating input parameters");
		}
		List<IData> distanceDataList = inputData.get("MaximumDistance");
		Double maxDistance = null;
		if(distanceDataList != null && distanceDataList.size() == 1){
			maxDistance = ((LiteralDoubleBinding) distanceDataList.get(0)).getPayload();
			if (maxDistance <= 0){
				throw new RuntimeException("The parameter 'MaximumDistance' must be greater than zero.");
			}
		}
		
		FeatureCollection<?,?> fcnew = snapPointsToLines(pointCollection, lineCollection, maxDistance);
		HashMap<String,IData> resulthash = new HashMap<String,IData>();
		resulthash.put("result", new GTVectorDataBinding(fcnew));
		return resulthash;
	}
	
	public FeatureCollection<?,?> snapPointsToLines(FeatureCollection<?,?> pointCollection, FeatureCollection<?,?> lineCollection, Double maxDistance){
		final SpatialIndex index = new STRtree();
		try {
			lineCollection.accepts(new FeatureVisitor() {
				@Override
				public void visit(Feature feature) {
					SimpleFeature simpleFeature = (SimpleFeature) feature;
					Geometry geom = (LineString) simpleFeature.getDefaultGeometry();
					// Just in case: check for  null or empty geometry
					if (geom != null) {
						Envelope env = geom.getEnvelopeInternal();
						if (!env.isNull()) {
							index.insert(env, new LocationIndexedLine(geom));
						}
					}
				}
			}, new NullProgressListener());
		} catch (IOException e) {
			e.printStackTrace();
		}

		/*
		 * If not provided, the maximum distance that a line can be from a point
		 * to be a candidate for snapping is set to the diagonal distance of the
		 * lines BBOX (as implemented now, this parameter is always given, since 
		 * it is mandatory)
		 */
		if (maxDistance == null){
			ReferencedEnvelope bounds = lineCollection.getBounds();
			maxDistance = Math.sqrt(Math.pow(bounds.getMaxX()-bounds.getMinX(),2) + Math.pow(bounds.getMaxX()-bounds.getMinX(),2));
		}

		String uuid = UUID.randomUUID().toString();
		FeatureCollection<?, SimpleFeature> featureCollection = DefaultFeatureCollections.newCollection();
		SimpleFeatureType featureType = null;
		GeometryFactory factory = JTSFactoryFinder.getGeometryFactory(null);
		FeatureIterator<?> featIterator = pointCollection.features();
		Coordinate[] points = new Coordinate[pointCollection.size()];
		int count = 0 ;
		
		try{			
			while ( featIterator.hasNext() ) {
				
				SimpleFeature feature = (SimpleFeature) featIterator.next();
				Point pointGeometry = (Point) feature.getDefaultGeometry();				
				Coordinate pointCoords = pointGeometry.getCoordinate();
				
				Envelope search = new Envelope(pointCoords);
				search.expandBy(maxDistance);

				/*
				 * Query the spatial index for objects within the search envelope.
				 * Note that this just compares the point envelope to the line envelopes
				 * so it is possible that the point is actually more distant than
				 * MAX_SEARCH_DISTANCE from a line.
				 */
				List<LocationIndexedLine> lines = index.query(search);
	
				// Initialize the minimum distance found to our maximum acceptable
				// distance plus a little bit
				double minDist = maxDistance + 1.0e-6;
				Coordinate minDistPoint = null;
	
				for (LocationIndexedLine line : lines) {
					LinearLocation here = line.project(pointCoords);
					Coordinate point = line.extractPoint(here);
					double dist = point.distance(pointCoords);
					if (dist < minDist) {
						minDist = dist;
						minDistPoint = point;
					}
				}
	
				if (minDistPoint == null) {
					// No line close enough to snap the point to
					LOGGER.info(pointCoords + "- X");
				} else {					
					Point snappedPoint = factory.createPoint(minDistPoint);
					if (snappedPoint != null) {
						if(count==0){
							CoordinateReferenceSystem crs = feature.getFeatureType().getCoordinateReferenceSystem();
							if(pointGeometry.getUserData() instanceof CoordinateReferenceSystem){
								crs = ((CoordinateReferenceSystem) pointGeometry.getUserData());
							}
							featureType = GTHelper.createFeatureType(feature.getProperties(), snappedPoint, uuid, crs);
							QName qname = GTHelper.createGML3SchemaForFeatureType(featureType);
							SchemaRepository.registerSchemaLocation(qname.getNamespaceURI(), qname.getLocalPart());				
						}
						SimpleFeature createdFeature = (SimpleFeature) GTHelper.createFeature("ID"+new Double(count).intValue(),snappedPoint,(SimpleFeatureType) featureType,feature.getProperties());
						createdFeature.setDefaultGeometry(snappedPoint);
						featureCollection.add(createdFeature);
					}		
				}
				
				count += 1;
			}
		}
		finally {
			featIterator.close();
		}
		
		return featureCollection;
	}

	@Override
	public Class<?> getInputDataType(String id) {
		if(id.equalsIgnoreCase("Points") || id.equalsIgnoreCase("Lines")){
			return GTVectorDataBinding.class;
		}else if(id.equalsIgnoreCase("MaximumDistance")){
			return LiteralDoubleBinding.class;
		}
		throw new RuntimeException("Could not find datatype for id " + id);
	}

	@Override
	public Class<?> getOutputDataType(String id) {
		return GTVectorDataBinding.class;
	}

	@Override
	public List<String> getInputIdentifiers() {
		List<String> identifierList =  new ArrayList<String>();
		identifierList.add("Points");
		identifierList.add("Lines");
		identifierList.add("MaximumDistance");
		return identifierList;
	}

	@Override
	public List<String> getOutputIdentifiers() {
		List<String> identifierList =  new ArrayList<String>();
		identifierList.add("result");
		return identifierList;
	}

}
