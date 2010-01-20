package org.n52.wps.server.algorithm.convexhull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.geotools.factory.FactoryConfigurationError;
import org.geotools.feature.AttributeType;
import org.geotools.feature.DefaultFeature;
import org.geotools.feature.DefaultFeatureCollections;
import org.geotools.feature.DefaultFeatureTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollectionIteration;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeBuilder;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.server.AbstractAlgorithm;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.CoordinateSystem;

import com.vividsolutions.jts.algorithm.ConvexHull;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
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

	@Override
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
		
		Coordinate[] coords = new Coordinate[featureCollection.getNumberOfAttributes()];
		
		ArrayList<Coordinate> coordinateList = new ArrayList<Coordinate>(featureCollection.getNumberOfAttributes());
		
		while (iter.hasNext()) {
			Feature feature = iter.next();

			if (feature.getDefaultGeometry() == null) {
				throw new NullPointerException(
						"defaultGeometry is null in feature id: "
								+ feature.getID());
			}
			
			Geometry geom = feature.getDefaultGeometry();
			
			if(geom instanceof Point){
				coordinateList.add(((Point)geom).getCoordinate());
			}
			
			


		}		
		
		ConvexHull convexHull = new ConvexHull(coordinateList.toArray(new Coordinate[coordinateList.size()]), new GeometryFactory());
		
		
		Geometry out = convexHull.getConvexHull();
		
//		Geometry in = feature.getDefaultGeometry();
//
//		
//		Geometry out = in.convexHull();
//
		
		Feature f = createFeature(out);
		
		FeatureCollection fOut = DefaultFeatureCollections.newCollection();
		
		fOut.add(f);

		HashMap<String, IData> result = new HashMap<String, IData>();

		result.put("CONVEX_HULL",
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
//			 feature = FeatureTypeBuilder.newInstance("Polygon").getFeatureType().create(new Object[]{geometry});
			 
		}
		catch(Exception e) {
			
		}
		return feature;
	}
	
	
}
