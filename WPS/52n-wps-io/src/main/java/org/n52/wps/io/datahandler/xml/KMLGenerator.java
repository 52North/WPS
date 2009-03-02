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
package org.n52.wps.io.datahandler.xml;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import net.opengis.kml.x22.BoundaryType;
import net.opengis.kml.x22.DocumentType;
import net.opengis.kml.x22.KmlDocument;
import net.opengis.kml.x22.KmlType;
import net.opengis.kml.x22.LineStringType;
import net.opengis.kml.x22.LinearRingType;
import net.opengis.kml.x22.MultiGeometryType;
import net.opengis.kml.x22.PlacemarkType;
import net.opengis.kml.x22.PointType;
import net.opengis.kml.x22.PolygonType;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlOptions;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.n52.wps.io.IStreamableGenerator;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.datahandler.binary.LargeBufferStream;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.w3c.dom.Node;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class KMLGenerator extends AbstractXMLGenerator implements IStreamableGenerator {
	
	private static final Logger LOGGER = Logger.getLogger(KMLGenerator.class);
	private static final String DEFAULT_CRS = "EPSG:4326";
	private static String[] SUPPORTED_SCHEMAS = new String[]{
		"http://www.opengis.net/kml/2.2"};
	private static String SUPPORTED_MIME_TYPE = "application/vnd.google-earth.kml+xml";

	public String[] getSupportedSchemas() {
		return SUPPORTED_SCHEMAS;
	}

	public boolean isSupportedSchema(String schema) {
		for(String supportedSchema : SUPPORTED_SCHEMAS) {
			if(supportedSchema.equals(schema))
				return true;
		}
		return false;
	}

	public Node generateXML(IData coll, String schema) {
		return generateXMLObj(coll, schema).getDomNode();
	}

	
	private KmlDocument generateXMLObj(IData coll, String schema2) {
		org.geotools.factory.Hints.putSystemDefault(org.geotools.factory.Hints.FORCE_AXIS_ORDER_HONORING, "http");
		KmlDocument doc = KmlDocument.Factory.newInstance();
		KmlType kml = doc.addNewKml();
		DocumentType kmlDoc = (DocumentType)kml.addNewAbstractFeatureGroup().substitute(new QName(SUPPORTED_SCHEMAS[0], "Document") , DocumentType.type);
		if(coll == null) {
			return doc;
		}
		FeatureIterator iter = ((GTVectorDataBinding)coll).getPayload().features();
		boolean crsIsSet = false;
		MathTransform transform = null;
		while(iter.hasNext()) {
			Feature feature = iter.next();
			if(crsIsSet == false) {
				CoordinateReferenceSystem crs = feature.getFeatureType().getDefaultGeometry().getCoordinateSystem();
				String crsString = (String)feature.getDefaultGeometry().getUserData();
				
				if(crsString != null){
					try {
						crs = CRS.decode(crsString, true);
						if(!crs.equals(DefaultGeographicCRS.WGS84)) {
							transform = CRS.findMathTransform(crs, DefaultGeographicCRS.WGS84);
						}
					} catch (NoSuchAuthorityCodeException e) {
						throw new IllegalArgumentException(e);
					} catch (FactoryException e) {
						throw new IllegalArgumentException(e);
					}
				}
				if(crs == null) {
					LOGGER.debug("CRS is not set, assume 4326, do no transformation");
				}
				crsIsSet = true;
			}
			Geometry geom = feature.getDefaultGeometry();
			if (transform != null) {
				try {
					geom = JTS.transform(geom, transform);
				} catch (MismatchedDimensionException e) {
					throw new IllegalArgumentException(e);
				} catch (TransformException e) {
					throw new RuntimeException(e);
				}
			}
			String geomType = geom.getGeometryType();
			PlacemarkType placemark = (PlacemarkType)kmlDoc.addNewAbstractFeatureGroup().substitute(new QName(SUPPORTED_SCHEMAS[0], "Placemark"), PlacemarkType.type);
			if(geomType.equals("Point")) {
				Point point = (Point)geom;
				Coordinate coord = point.getCoordinate();
				if (coord != null) {
					PointType kmlPoint = (PointType)placemark.addNewAbstractGeometryGroup().substitute(new QName(SUPPORTED_SCHEMAS[0], "Point"), PointType.type);
					List pointCoord = new ArrayList();
					pointCoord.add(convertToKMLCoordType(coord));
					kmlPoint.setCoordinates(pointCoord);
				}
			}
			else if(geomType.equals("LineString")) {
				LineString ls = (LineString)geom;
				List coords = convertToKMLCoordType(ls.getCoordinates());
				if(coords != null) {
					LineStringType kmlLine = (LineStringType)placemark.addNewAbstractGeometryGroup().substitute(new QName(SUPPORTED_SCHEMAS[0], "LineString"), LineStringType.type);
					kmlLine.setCoordinates(coords);
				}
			}
			else if(geomType.equals("Polygon")) {
				Polygon polygon = (Polygon)geom;
				PolygonType kmlPolygon = (PolygonType)placemark.addNewAbstractGeometryGroup().substitute(new QName(SUPPORTED_SCHEMAS[0], "Polygon"), PolygonType.type);
				convertToKmlPolygon(polygon, kmlPolygon);
			}
			else if (geomType.equals("MultiPolygon")) {
				MultiPolygon mp = (MultiPolygon)geom;
				MultiGeometryType kmlMultiGeom = (MultiGeometryType)placemark.addNewAbstractGeometryGroup().substitute(new QName(SUPPORTED_SCHEMAS[0], "MultiGeometry"), MultiGeometryType.type);
				for(int i = 0; i < mp.getNumGeometries(); i++) {
					PolygonType kmlPolygon = (PolygonType) kmlMultiGeom.addNewAbstractGeometryGroup().substitute(new QName(SUPPORTED_SCHEMAS[0], "Polygon"), PolygonType.type);
					Polygon p = (Polygon) (geom.getGeometryN(i));
					convertToKmlPolygon(p, kmlPolygon);
				}
			}
			else if(geomType.equals("MultiLineString")) {
				MultiLineString mls = (MultiLineString) geom;
				MultiGeometryType kmlMultiGeom = (MultiGeometryType)placemark.addNewAbstractGeometryGroup().substitute(new QName(SUPPORTED_SCHEMAS[0], "MultiGeometry"), MultiGeometryType.type);
				for(int i = 0; i < mls.getNumGeometries(); i++) {
					LineString l = (LineString) (geom.getGeometryN(i));
					LineStringType kmlLine = (LineStringType)kmlMultiGeom.addNewAbstractGeometryGroup().substitute(new QName(SUPPORTED_SCHEMAS[0], "LineString"), LineStringType.type);
					kmlLine.setCoordinates(convertToKMLCoordType(l.getCoordinates()));
				}
			}
//			}
			else {
				throw new IllegalArgumentException("geometryType not supported: " + geomType);
			}
		}
		
		return doc;
	}
	
	/**
	 * @param polygon
	 * @return
	 */
	private void convertToKmlPolygon(Polygon polygon, PolygonType kmlPolygon) {
		net.opengis.kml.x22.LinearRingType kmlRing = (LinearRingType)kmlPolygon.addNewOuterBoundaryIs().addNewLinearRing().substitute(new QName(SUPPORTED_SCHEMAS[0], "LinearRing"), LinearRingType.type);
		convertToKMLLinearRing(polygon.getExteriorRing(), kmlRing);
		BoundaryType innerBoundary = kmlPolygon.addNewInnerBoundaryIs();
		for(int i = 0; i < polygon.getNumInteriorRing(); i++) {
			net.opengis.kml.x22.LinearRingType innerRing = innerBoundary.addNewLinearRing();
			innerRing.setCoordinates(convertToKMLCoordType(polygon.getInteriorRingN(i).getCoordinates()));
		}
	}

	private void convertToKMLLinearRing(LineString ls, LinearRingType kmlRing) {
		List coords = convertToKMLCoordType(ls.getCoordinates());
		if(coords == null) {
		}
		kmlRing.setCoordinates(coords);
	}
	
	private List convertToKMLCoordType(Coordinate[] coords) {
		ArrayList<String> returnCoords = new ArrayList<String>();
		for(int i = 0; i < coords.length; i++) {
			String tempCoord = convertToKMLCoordType(coords[i]);
			if(tempCoord != null) {
				returnCoords.add(tempCoord);
			}
		}
		if(returnCoords.isEmpty()) {
			return null;
		}
		return returnCoords;
	}
	
	private String convertToKMLCoordType(Coordinate coord) {
		String returnCoord = "";
		if(Double.isNaN(coord.x) || Double.isNaN(coord.y)) {
			return null;
		}
			returnCoord = returnCoord + coord.x;
			returnCoord = returnCoord + "," + coord.y;
		
		if(!Double.isNaN(coord.z)) {
			returnCoord = returnCoord + "," + coord.z;
		}
		return returnCoord;
	}

	public OutputStream generate(IData coll) {
		LargeBufferStream baos = new LargeBufferStream();
		KmlDocument doc = generateXMLObj(coll, null);
		try {
			BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(baos));
			bufferedWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			XmlOptions opts = new XmlOptions();
			opts.setSaveAggressiveNamespaces();
			Map prefixes = new HashMap();
			prefixes.put(SUPPORTED_SCHEMAS[0], "");
			opts.setSaveSuggestedPrefixes(prefixes);
			doc.save(bufferedWriter, opts);
			bufferedWriter.close();
		}
		catch(IOException e) {
			throw new RuntimeException(e);
		}	
		return baos;
	}

	
	public boolean isSupportedEncoding(String encoding) {
		return true;
	}

	

	public String[] getSupportedFormats() {
		return new String[]{SUPPORTED_MIME_TYPE};
	}

	public void writeToStream(IData coll, OutputStream os) {
		KmlDocument doc = generateXMLObj(coll, null);
		try {
			BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(os));
			bufferedWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			XmlOptions opts = new XmlOptions();
			opts.setSaveAggressiveNamespaces();
			Map prefixes = new HashMap();
			prefixes.put(SUPPORTED_SCHEMAS[0], "");
			opts.setSaveSuggestedPrefixes(prefixes);
			doc.save(bufferedWriter, opts);
			bufferedWriter.close();
		}
		catch(IOException e) {
			throw new RuntimeException(e);
		}	
	}
	
	public Class[] getSupportedInternalInputDataType() {
		Class[] supportedClasses = {GTVectorDataBinding.class};
		return supportedClasses;
	
	}

}