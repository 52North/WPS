package org.n52.wps.io.datahandler.generator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * 
 * This generator generates JSON Geometries (points, polylines, polygons,
 * see this website: 
 * http://help.arcgis.com/en/webapi/javascript/arcgis/help/jsapi_start.htm)
 * @author Merret Buurman, 52°north / ifgi
 * June 2012
 * 
 * So far, only one Geometry can be handled at a time (no collections).
 * 
 * Geometries can be created from from Shapefiles. Support for GTVectorBindings is
 * almost ready, but not complete yet.
 * 
 * GeometryCollections: Each Geometry will be transformed to a JSON geometry separately,
 * they will be put in an array [geometry, geometry, geometry]
 * 
 * MultiLineStrings and MultiPolygons:
 * These geometries are not defined in http://help.arcgis.com/en/webapi/javascript/
 * arcgis/help/jsapi_start.htm, only Point, Polyline, Polygon and MultiPoint are defined.
 * MultiLineStrings and MultiPolygons are (by default) transformed to normal Polygons 
 * and Polylines, having multiple "paths"/"rings".
 * As an alternative, MultiLineStrings and MultiPolygons can be handled as several 
 * linestrings / polygons in an array (MultiLineString => [linestring, linestring, 
 * linestring], etc.). For this, set "multiGeometriesToArray" to false.
 *
 */

public class JSONGeometryGenerator extends AbstractGenerator {
	
	private static Logger LOGGER = LoggerFactory.getLogger(JSONGeometryGenerator.class); 
	
	boolean isShapefile = false; // needed for a workaround: if the input is gml without a specified crs,
								 // the coordinates usually are swapped, compared to the shapefile
	
	boolean multiGeometriesToArray = false; // if you prefer the multipolygons or multilinestrings as an 
										    // array [polygon, polygon, polygon], set this to true. If you prefer
										    // them as multiple rings/paths in one linestring or polygon, set it to
										    // false
	
	
	
	public JSONGeometryGenerator(){
		super();
		//supportedIDataTypes.add(GTVectorDataBinding.class);
		supportedIDataTypes.add(GenericFileDataBinding.class);
	}

	@Override
	public InputStream generateStream(IData data, String mimeType, String schema)
			throws IOException {
		
		LOGGER.info("Starting to generate json geometry out of the process result");
		
		// Extract input data (as feature collection)
		GTVectorDataBinding gvdb = null;
		if (data instanceof GenericFileDataBinding){
			LOGGER.debug("The data passed from the algorithm to the generator is GenericFileDataBinding");
			try {
				gvdb = ((GenericFileData) data.getPayload()).getAsGTVectorDataBinding();
				isShapefile = true;
			} catch (Exception e){
				throw new IOException("The data passed from the algorithm to the generator is a file, but no shapefile");
			}
		} else if (data instanceof GTVectorDataBinding){
			LOGGER.debug("The data passed from the algorithm to the generator is GTVectorDataBinding");
			gvdb = (GTVectorDataBinding) data;
			isShapefile = false;
		} else {
			throw new IOException("The data passed from the algorithm to the generator has to be a file (shapefile)!");
		}
		FeatureCollection fc = gvdb.getPayload();
		
		if(fc == null || fc.size() == 0) {
			throw new IOException("No feature was passed to the generator!");
		}
		
		// Transform features to JSON
		FeatureIterator fi = fc.features();
		String jsonString = "";
		if (fc.size() == 1){
			// only one feature: make a simple json geometry
			SimpleFeature f = (SimpleFeature) fi.next();
			Geometry geom = this.getGeometry(f);
			jsonString = this.transformOneFeature(geom);
		} else {
			// several features: put the geometries into an array
			jsonString = "[";
			while (fi.hasNext()) {
				SimpleFeature f = (SimpleFeature) fi.next();
				Geometry geom = this.getGeometry(f);
				String oneFeatureString = this.transformOneFeature(geom);
				jsonString = jsonString.concat(oneFeatureString + ", ");
			}
			jsonString = jsonString.concat("]");
		}
		
		// Make stream out of it and return stream
		InputStream is = new ByteArrayInputStream(jsonString.getBytes());
		return is;
	}
	
	
	
	
	
	
	/**
	 * Get and return a feature's geometry.
	 * @param SimpleFeature
	 * @return Geometry
	 * @throws IOException
	 */
	private Geometry getGeometry(SimpleFeature simpleFeature) throws IOException {
		
		Geometry geom = null;
		if(simpleFeature.getDefaultGeometry()==null && simpleFeature.getAttributeCount()>0 
				&& simpleFeature.getAttribute(0) instanceof Geometry){
			geom = (Geometry)simpleFeature.getAttribute(0);
		}else{
			geom = (Geometry)simpleFeature.getDefaultGeometry();
		}
		if ((geom == null) || !(geom instanceof Geometry)){
			LOGGER.error("Geometry could not be extracted");
			throw new IOException("Geometry could not be extracted!");
		}
		LOGGER.debug("Geometry extracted");
		return geom;
		
	}
	
	/**
	 * Transform a feature to a JSON string
	 * 
	 * The feature that is passed to this method is transformed to a JSON string according
	 * to this site (http://help.arcgis.com/en/webapi/javascript/arcgis/help/jsapi_start.htm).
	 * 
	 * @param geom
	 * @return
	 * @throws IOException
	 */
	private String transformOneFeature(Geometry geom) throws IOException {
		
		// Depending on geometry's type, transform to json geometry
		String jsonString = "";
		
		// simple geometries:
		if (geom instanceof Point){
			jsonString = transformPointToJsonPoint((Point) geom);
		} else if (geom instanceof LineString){
			jsonString = transformLineStringToJsonLineString((LineString) geom);
		} else if (geom instanceof Polygon){
			jsonString = transformPolygonToJsonPolygon((Polygon) geom);
		}
		
		// multipoint
		else if (geom instanceof MultiPoint){
			MultiPoint multipoint = (MultiPoint) geom;
			
			// if only one point inside, make normal point out of it
			if (multipoint.getNumGeometries() == 1){
				Point point = (Point) multipoint.getGeometryN(0);
				jsonString = transformPointToJsonPoint(point);
			} else {
				jsonString = transformMultiPointToJsonMultiPoint(multipoint);
			}
			
		
		// multilinestring
		} else if (geom instanceof MultiLineString){
			MultiLineString multiline = (MultiLineString) geom;
			
			// if only one linestring inside, make normal linestring out of it
			if (multiline.getNumGeometries() == 1){
				LineString line = (LineString) multiline.getGeometryN(0);
				jsonString = transformLineStringToJsonLineString(line);
			}
			// real multilinestring
			else {
				if (multiGeometriesToArray){
					jsonString = transformMultiLineStringToJsonLineStringArray(multiline);
				} else {
					jsonString = transformMultiLineStringToJsonLineString(multiline);
				}
			}
			
		
		// multipolygon
		} else if (geom instanceof MultiPolygon){
			MultiPolygon multipoly = (MultiPolygon) geom;
			
			// if only one polygon inside, make normal polygon out of it
			if (multipoly.getNumGeometries() == 1){
				Polygon poly = (Polygon) multipoly.getGeometryN(0);
				jsonString = transformPolygonToJsonPolygon(poly);
			}
			
			// real multi polygon
			else {
				if (multiGeometriesToArray){
					jsonString = transformMultiPolygonToJsonPolygonArray(multipoly);
				} else {
					jsonString = transformMultiPolygonToJsonPolygon(multipoly);
				}
				
			}
		} else {
			LOGGER.error("Feature has no recognized geometry type");
		}
		
		return jsonString;
	}
	

	
	/*
	 * Private methods that do the actual transformation:
	 * 
	 */
	
	
	/* *** Transformations for points *** */
	// Simple point
	private String transformPointToJsonPoint(Point p){
		
		LOGGER.info("Transforming point to JSON point.");
		
		/* Points are like this: {"x": -122.65, "y": 45.53, "spatialReference": {" wkid": 4326 } }
		 * We will assemble it in five parts:
		 * 		(1)  {
		 * 		(2)  "x": -122.65,
		 * 		(3)  "y": 45.53,
		 * 		(4)  "spatialReference": {" wkid": 4326 }
		 * 		(5)  }
		 */

		// Get coordinates
		Coordinate coordinatePair = p.getCoordinate();
		double x = coordinatePair.x;
		double y = coordinatePair.y;
		LOGGER.debug("Point's coordinates: " + x + ", " + y);
		
		// Define which one is east-west and which one is north-south
		// TODO There should not be this difference!! I think the error is that 
		// x and y were wrongly given to GML input!
		double eastwest;
		double northsouth;
		if (isShapefile){
			// In GTVectorBindings made of shapefiles, 
			// the x seems to stand for north-south
			// and the y seems to stand for east-west
			eastwest = x;
			northsouth = y;
		} else {
			// In GML content (without given CRS)
			// the y seems to stand for north-south
			// and the x seems to stand for east-west
			northsouth = x;
			eastwest = y;
		}
		
		// Get CRS, part (4)
		String wkid;
		try {
			wkid = crsToJsonSpatialReference(p);
		} catch (IOException e) {
			wkid = "\"spatialReference\":{\"wkid\":0000}";
		}
		LOGGER.info("Point's CRS: " + wkid);
		
		// Assemble json geometry point
		/* In jsongeometry, x stands for EAST/WEST coordinate, y for NORTH/SOUTH. */
		String jsonString = "{\"x\": " + eastwest + ", \"y\": " + northsouth + ", " + wkid +"}";
		LOGGER.info("Finished JSON point: " + jsonString);
		
		return jsonString;
	}
	// Multipoint
	private String transformMultiPointToJsonMultiPoint(MultiPoint multipoint){
		
		LOGGER.info("Transforming multipoint to json multipoint");
		
		/* Multipoints are like this:
		 * {"points":[[-122.63,45.51],[-122.56,45.51],[-122.56,45.55]],"spatialReference":({" wkid":4326 })}
		 * 		(1)   {"points":[
		 * 		(2)   [coords],[coords],[coords],
		 * 		(4)   ]
		 * 		(5)   "spatialReference":{" wkid":4326 }
		 * 		(6)   }
		 * */
		
		// Get CRS in this form: "spatialReference":{" wkid":4326 }
		String wkid;
		try {
			wkid = crsToJsonSpatialReference(multipoint);
		} catch (IOException e) {
			wkid = "\"spatialReference\":{\"wkid\":0000}";
		}
		LOGGER.debug("Point's CRS: " + wkid);
		
		// Step (1)
		String jsonString = "{\"points\":[";
		
		// Step (2)
		boolean firstElement = true;
		for (int i = 0; i < multipoint.getNumGeometries(); i++){
			
			/* Treat one point of the multipoint: */
			Point point = (Point) multipoint.getGeometryN(i);
			String onePointString = "[";
			// Get coordinates
			Coordinate coordinatePair = point.getCoordinate();
			double x = coordinatePair.x;
			double y = coordinatePair.y;
			LOGGER.info("Point's coordinates: " + x + ", " + y);
			// Define which one is east-west and which one is north-soutn
			// TODO There should not be this difference!! I think the error is that 
			// x and y were wrongly given to GML input!
			double eastwest;
			double northsouth;
			if (isShapefile){
				// In GTVectorBindings made of shapefiles, 
				// the x seems to stand for north-south
				// and the y seems to stand for east-west
				eastwest = x;
				northsouth = y;
			} else {
				// In GML content (without given CRS)
				// the y seems to stand for north-south
				// and the x seems to stand for east-west
				northsouth = x;
				eastwest = y;
			}
			/* In JSONGEOMETRY, x stands for EAST/WEST coordinate, y for NORTH/SOUTH.
			 * Lines are like this [eastwest, northsouth] */
			onePointString = onePointString.concat(eastwest + "," + northsouth + "]");
			
			if (firstElement){
				jsonString = jsonString.concat(onePointString);
				firstElement = false;
			} else {
				jsonString = jsonString.concat("," + onePointString);
			}
		}
		// Step (3), (4), (5)
		jsonString = jsonString.concat("], " + wkid + "}");
		
		return jsonString;
	}	
	
	/* *** Transformations for lines *** */
	// Simple linestring
	private String transformLineStringToJsonLineString(LineString linestring){
		
		LOGGER.info("Transforming linestring to JSON linestring");
		
		/* Lines are like this: {"paths":[[[-122.68,45.53], [-122.58,45.55],[-122.57,45.58],[-122.53,45.6]]],
		"spatialReference":{"wkid":4326}}
		* For a simple line, we put only one path.
		* We will assemble it in several parts:
		* 		(a)  {"paths":[
		* 		(b)  one path, like this: [  [coords],[coords],[coords]  ]  (it has square brackets)
		*       (c)  ],
		*       (d)  "spatialReference":{"wkid":4326}
		*       (e)  }
		*/
		
		// (1) make path
		String aPath = linestringToJsonPath(linestring); // like this: [  [coords],[coords],[coords]  ]
		
		// (2) assemble to line
		String aJsonLine = assembleLine(aPath, linestring);
		
		return aJsonLine;
	}
	// Multilinestring
	private String transformMultiLineStringToJsonLineString(MultiLineString multilinestring){
		
		LOGGER.info("Transforming multilinestring to JSON linestring with several paths in it.");
		
		/* Lines are like this: {"paths":[[[-122.68,45.53], [-122.58,45.55],[-122.57,45.58],[-122.53,45.6]]],
		"spatialReference":{"wkid":4326}}
		* For a multi line, we put several paths.
		* We will assemble it in several parts:
		* 		(a)  {"paths":[
		* 		(b)  several paths, comma separated, each has square brackets:
		* 		(b)  		[  [coords],[coords],[coords]  ]   <== this is one path, with square brackets!
		* 		(b)  		[  [coords],[coords],[coords]  ],  <== this is one path, with square brackets!
		* 		(b)  		[  [coords],[coords],[coords]  ],  <== this is one path, with square brackets!
		*       (c)  ],
		*       (d)  "spatialReference":{"wkid":4326}
		*       (e)  }
		*/
		
		// (1) make paths, and concatenate them, comma separated
		String allPaths = "";
		boolean firstElement = true;
		for (int i = 0; i < multilinestring.getNumGeometries(); i++){
			LineString linestring = (LineString) multilinestring.getGeometryN(i);
			String aPath = linestringToJsonPath(linestring);
			if (firstElement){
				allPaths = allPaths.concat(aPath);
				firstElement = false;
			} else {
				allPaths = allPaths.concat(", " + aPath);
			}
		}
		
		// (2) assemble to line
		String jsonString = assembleLine(allPaths, multilinestring);
		return jsonString;
	}
	private String transformMultiLineStringToJsonLineStringArray(MultiLineString multilinestring){
		
		LOGGER.info("Transforming multilinestring to JSON linestring array");
		
		/* 
		 * For a multi line, we make several simple lines and put them into an array
		 */
		
		String lineArray = "[";
		boolean firstElement = true;
		for (int i = 0; i < multilinestring.getNumGeometries(); i++){
			LineString linestring = (LineString) multilinestring.getGeometryN(i);
			String aLine = transformLineStringToJsonLineString(linestring);
			if (firstElement){
				lineArray = lineArray.concat(aLine);
				firstElement = false;
			} else {
				lineArray = lineArray.concat(", " + aLine);
			}
		}
		
		return lineArray;
	}
	// Helpers
	private String assembleLine(String aPathOrSeveralPaths, Geometry geometry){
		
		/* Lines are like this: {"paths":[[[-122.68,45.53], [-122.58,45.55],[-122.57,45.58],[-122.53,45.6]]],
		"spatialReference":{"wkid":4326}}
		
		* We will assemble it in several parts:
		* 		(a)  {"paths":[
		* 		(b)  either a path or several paths (comma separated)
		*       (c)  ],
		*       (d)  "spatialReference":{"wkid":4326}
		*       (e)  }
		*/
		
		// (1) make spatial reference
		String spatialReference;
		try {
			spatialReference = crsToJsonSpatialReference(geometry);
		} catch (IOException e) {
			spatialReference = "\"spatialReference\":{\"wkid\":0000}";
		}
		
		// (2) Assemble json geometry polyline
		String jsonString = "{\"paths\":[" + aPathOrSeveralPaths + "]," + spatialReference + "}";
		return jsonString;
		
		
	}
	private String linestringToJsonPath(LineString ls){
		
		/* A path is like this: 
		 * 	[
		 * 		[-122.68,45.53], [-122.58,45.55],[-122.57,45.58],[-122.53,45.6]
		 * 	]
		 * 
		 * It is returned WITH the surrounding square brackets!
		 */
		
		// Iterate over all coordinate pairs in the linestring
		Coordinate[] coordinateArray = ls.getCoordinates();
		String aPath = "[";
		boolean firstElement = true;
		for (Coordinate coordinatePair : coordinateArray){
			
			// Assign x and y to eastwest or northeast, depending on input
			// TODO There should not be this difference!! I think the error is that 
			// x and y were wrongly given to GML input!
			double eastwest = 0.0;
			double northsouth = 0.0;
			if (isShapefile){
				// In GTVectorBindings made of shapefiles, 
				// the x seems to stand for north-south
				// and the y seems to stand for east-west
				eastwest = coordinatePair.x;
				northsouth = coordinatePair.y;
			} else {
				// In GML content (without given CRS)
				// the y seems to stand for north-south
				// and the x seems to stand for east-west
				eastwest = coordinatePair.y;
				northsouth = coordinatePair.x;
			}
			
			// Make coordinate pair string
			String coordinatePairString = "";
			/* In JSONGEOMETRY, x stands for EAST/WEST coordinate, y for NORTH/SOUTH.
			 * Lines are like this [eastwest, northsouth] */
			if (firstElement){
				coordinatePairString = "[" + eastwest + "," + northsouth + "]";
				firstElement = false;
			} else {
				coordinatePairString = ", [" + eastwest + "," + northsouth + "]";
			}
			
			// Stick all coordinate pair strings together
			aPath = aPath.concat(coordinatePairString);
		}
		aPath = aPath.concat("]");
		return aPath;
		
	}
	
	/* *** Transformations for polygons *** */
	// Simple polygon
	private String transformPolygonToJsonPolygon(Polygon polygon){
		
		LOGGER.info("Transforming simple polygon to JSON polygon");
		
		/* Polygons are like this:
		 * {"rings":[[[-122.63,45.52],[-122.57,45.53],[-122.52,45.50],[-122.49,45.48],
  		   [-122.64,45.49],[-122.63,45.52],[-122.63,45.52]]],"spatialReference":{" wkid":4326 }}
		 * 
		 * For a simple polygon, we take only one ring:
		 * 
		 * 		(a) {"rings":[
		 * 		(b) a ring, like this: [[coords],[coords],[coords]]    <== this is one ring, with square brackets
		 * 		(c) ],
		 * 		(d) "spatialReference":{" wkid":4326 }
		 * 		(e) }
		 */
		
		// (1) make ring
		String aRing = getPathFromPolygon(polygon);
		
		
		// (2) assemble polygon
		String jsonString = assemblePolygon(aRing, polygon);
		return jsonString;
		
		
	}
	// Multipolygon
	private String transformMultiPolygonToJsonPolygon(MultiPolygon multipolygon){
		
	
		LOGGER.info("Transforming multipolygon to JSON polygon with several rings in it.");
		
		/* Lines are like this: {"paths":[[[-122.68,45.53], [-122.58,45.55],[-122.57,45.58],[-122.53,45.6]]],
		"spatialReference":{"wkid":4326}}
		* For a multi polygon, we put several paths.
		* We will assemble it in several parts:
		* 		(a)  {"paths":[
		* 		(b)  several paths, comma separated, each has square brackets:
		* 		(b)  		[  [coords],[coords],[coords]  ]   <== this is one path, with square brackets!
		* 		(b)  		[  [coords],[coords],[coords]  ],  <== this is one path, with square brackets!
		* 		(b)  		[  [coords],[coords],[coords]  ],  <== this is one path, with square brackets!
		*       (c)  ],
		*       (d)  "spatialReference":{"wkid":4326}
		*       (e)  }
		*/
		
		// (1) make paths, and concatenate them, comma separated
		String allRings = "";
		boolean firstElement = true;
		for (int i = 0; i < multipolygon.getNumGeometries(); i++){
			Polygon polygon = (Polygon) multipolygon.getGeometryN(i);
			String aRing = getPathFromPolygon(polygon);
			if (firstElement){
				allRings = allRings.concat(aRing);
				firstElement = false;
			} else {
				allRings = allRings.concat(", " + aRing);
			}
		}
		
		// (2) assemble polygon
		String jsonString = assemblePolygon(allRings, multipolygon);
		return jsonString;
	}
	private String transformMultiPolygonToJsonPolygonArray(MultiPolygon multipolygon){
		
		LOGGER.info("Transforming multipolygon to JSON polygon array");
		
		/* 
		 * For a multi polygon, we make several simple polygons and put them into an array
		 */
		
		String polygonArray = "[";
		boolean firstElement = true;
		for (int i = 0; i < multipolygon.getNumGeometries(); i++){
			Polygon polygon = (Polygon) multipolygon.getGeometryN(i);
			String aPolygon = transformPolygonToJsonPolygon(polygon);
			if (firstElement){
				polygonArray = polygonArray.concat(aPolygon);
				firstElement = false;
			} else {
				polygonArray = polygonArray.concat(", " + aPolygon);
			}
		}
		
		return polygonArray;
	}
	// Helpers
	private String getPathFromPolygon(Polygon polygon){
		
		// Make a path (=ring) from a simple polygon
		
		// Does the polygon have holes?
		int holes = polygon.getNumInteriorRing();
		if (holes < 0){
			LOGGER.info("The polygon that is being transformed to an json geometry " +
					"has holes. Holes are not accepted. Only the outer ring will be " +
					"transformed.");
		}
		
		// Get the outer ring
		LineString outer = polygon.getExteriorRing();
		
		// Make an json path out of this linestring
		String aRing = linestringToJsonPath(outer);
		
		return aRing;
	}
	private String assemblePolygon(String aRingOrSeveralRings, Geometry geometry){
		
		/* Polygons are like this:
		 * 		(a) {"rings":[
		 * 		(b) a ring or several rings (comma separated)
		 * 		(c) ],
		 * 		(d) "spatialReference":{" wkid":4326 }
		 * 		(e) }
		 */
		
		// (1) make spatial reference
		String spatialReference;
		try {
			spatialReference = crsToJsonSpatialReference(geometry);
		} catch (IOException e) {
			spatialReference = "\"spatialReference\":{\"wkid\":0000}";
		}
		
		String jsonString = "{\"rings\": [" + aRingOrSeveralRings + "], " + spatialReference + "}";
		return jsonString;
	}
	
	// General helpers
	private String crsToJsonSpatialReference(Geometry geom) throws IOException{
		
		// Get CRS from geometry
		String wkid = "";
		int refsys = geom.getSRID();
		if (refsys != 0){
			Integer i = new Integer(refsys);
			wkid = i.toString();
		} else {
			throw new IOException("The GML geometry that is being transformed to a json polyline does not have a spatial reference (srid)");
		}
		wkid = "\"spatialReference\":{\"wkid\":" + wkid + "}";
		return wkid;
	}
	

}
