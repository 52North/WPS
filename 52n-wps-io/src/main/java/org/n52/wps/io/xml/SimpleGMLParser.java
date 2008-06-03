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
package org.n52.wps.io.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.opengis.examples.packet.GMLPacketDocument;
import net.opengis.examples.packet.StaticFeatureType;
import net.opengis.gml.CoordType;
import net.opengis.gml.LineStringPropertyType;
import net.opengis.gml.LinearRingMemberType;
import net.opengis.gml.LinearRingType;
import net.opengis.gml.PointPropertyType;
import net.opengis.gml.PolygonPropertyType;
import noNamespace.PropertyDocument.Property;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.geotools.feature.AttributeType;
import org.geotools.feature.DefaultFeatureCollections;
import org.geotools.feature.DefaultFeatureTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;


/**
 * This parser handles xml files compliant to gmlpacket.xsd 
 * @author foerster
 *
 */
public class SimpleGMLParser extends AbstractXMLParser {
	
	private static String[] SUPPORTED_SCHEMAS = new String[]{"http://www.opengeospatial.org/gmlpacket.xsd", "http://geoserver.itc.nl:8080/wps/schemas/gml/2.1.2/gmlpacket.xsd"};	
	private static Logger LOGGER = Logger.getLogger(SimpleGMLParser.class);
	//private FeatureType type;
	
	public SimpleGMLParser() {

	}

	public String[] getSupportedSchemas() {
		return SUPPORTED_SCHEMAS;
	}

	public FeatureCollection parseXML(String gml) {
		GMLPacketDocument doc;
		try {
			doc = GMLPacketDocument.Factory.parse(gml);
		} catch (XmlException e) {
			throw new IllegalArgumentException("Error while parsing XML string: " + e.getMessage(), e);
		}
		if(doc != null) {
			return parseXML(doc);
		}
		return null;
	}
	
	public FeatureCollection parseXML(InputStream stream) {
		GMLPacketDocument doc;
		try {
			doc = GMLPacketDocument.Factory.parse(stream);
		}
		catch(XmlException e) {
			throw new IllegalArgumentException("Error while parsing XML", e);
		}
		catch(IOException e) {
			throw new IllegalArgumentException("Error transfering XML", e);
		}
		if(doc != null) {
			return parseXML(doc);
		}
		return null;
	}
	
	private FeatureCollection parseXML(GMLPacketDocument doc) {
		FeatureCollection collection = DefaultFeatureCollections.newCollection();
		int numberOfMembers = doc.getGMLPacket().getPacketMemberArray().length;
		for(int i = 0; i< numberOfMembers; i++) {
			StaticFeatureType feature = doc.getGMLPacket().getPacketMemberArray(i).getStaticFeature();
			Feature newFeature = convertStaticFeature(feature);
			if (newFeature != null) {
				collection.add(newFeature);
			}
			else {
				LOGGER.debug("feature has no geometry, feature will not be included in featureCollection");
			}
		}
		return collection; 
	}
	
	private Feature convertStaticFeature(StaticFeatureType staticFeature) {
		
		Feature feature = null;
		Geometry geom = null;
		if(staticFeature.isSetLineStringProperty()) {
			geom = convertToJTSGeometry(staticFeature.getLineStringProperty());
		}
		else if(staticFeature.isSetPointProperty()) {
			geom = convertToJTSGeometry(staticFeature.getPointProperty());
		}
		else if(staticFeature.isSetPolygonProperty()) {
			geom = convertToJTSGeometry(staticFeature.getPolygonProperty());
		}
		if(geom == null) {
			return null;
		}
		
		FeatureType type = createFeatureType(staticFeature);
		
		try{
			 feature = type.create(new Object[]{geom});
		}
		catch(IllegalAttributeException e) {
			throw new IllegalArgumentException(e);
		}
		return feature;
	}
	
	private FeatureType createFeatureType(StaticFeatureType staticFeature) {
		DefaultFeatureTypeFactory typeFactory = new DefaultFeatureTypeFactory();
		typeFactory.setName("gmlPacketFeatures");
		AttributeType geom;
		if(staticFeature.isSetLineStringProperty()) {
			geom = org.geotools.feature.AttributeTypeFactory.newAttributeType( "LineString",
					LineString.class);
			typeFactory.addType(geom);
		}
		if(staticFeature.isSetPointProperty()) {
			geom = org.geotools.feature.AttributeTypeFactory.newAttributeType( "Point",
					Point.class);
			typeFactory.addType(geom);
		}
		else if(staticFeature.isSetPolygonProperty()) {
			geom = org.geotools.feature.AttributeTypeFactory.newAttributeType( "Polygon",
					Polygon.class);
			typeFactory.addType(geom);
		}
		FeatureType returnType;
		try {
			returnType = typeFactory.getFeatureType();
		}
		catch (SchemaException e) {
			throw new RuntimeException(e);
		}
		return returnType;
		
//		staticFeature.get
	}
	private Geometry convertToJTSGeometry(LineStringPropertyType lineString) {
		Geometry geom;
		if(lineString.getLineString().getCoordArray().length != 0) {
			CoordType[] xmlCoords = lineString.getLineString().getCoordArray();
			Coordinate[] coords = convertToJTSCoordinates(xmlCoords);
			if(coords.length == 0) {
				LOGGER.debug("feature does not include any geometry (LineString)");
				return null;
			}
			geom = geomFactory.createLineString(coords);
		}
		else if (lineString.getLineString().isSetCoordinates()) {
			throw new IllegalArgumentException("Element gml:coordinates is not supported yet");
		}
		else {
			LOGGER.debug("LineString has no coordinates");
			return null;
		}
		return geom;
	}
	
	private Geometry convertToJTSGeometry(PointPropertyType point) {
		Coordinate coord = convertToJTSCoordinate(point.getPoint().getCoord());
		return geomFactory.createPoint(coord);
	}

	private Geometry convertToJTSGeometry(PolygonPropertyType polygon) {
		LinearRingType outerRing = polygon.getPolygon().getOuterBoundaryIs().getLinearRing();
		LinearRing jtsOuterRing = convertToJTSLinearRing(outerRing);
		LinearRingMemberType[] innerRings = polygon.getPolygon().getInnerBoundaryIsArray();
		List<LinearRing> jtsInnerRings = new ArrayList<LinearRing>();
		for(LinearRingMemberType ring : innerRings) {
			if(ring.getLinearRing() != null) {
				jtsInnerRings.add(convertToJTSLinearRing(ring.getLinearRing()));
			}
		}
		return geomFactory.createPolygon(jtsOuterRing, (LinearRing[])jtsInnerRings.toArray(new LinearRing[jtsInnerRings.size()]));
	}
	
	private LinearRing convertToJTSLinearRing(LinearRingType linearRing) {
		Coordinate[] coords = convertToJTSCoordinates(linearRing.getCoordArray());
		return geomFactory.createLinearRing(coords);
	}
	
	/**
	 * expects Coordinates with X & Y or X & Y & Z
	 * @param coords
	 * @return
	 */
	private Coordinate[] convertToJTSCoordinates(CoordType[] coords) {
		List<Coordinate> coordList = new ArrayList<Coordinate>();
		for(CoordType coord : coords) {
			Coordinate coordinate = convertToJTSCoordinate(coord);
			coordList.add(coordinate);
		}
		return coordList.toArray(new Coordinate[coordList.size()]);
	}
	
	private Coordinate convertToJTSCoordinate(CoordType coord) {
		if(!coord.isSetZ()) {
			return new Coordinate(coord.getX().doubleValue(), coord.getY().doubleValue());
		}
		else {
			return new Coordinate(coord.getX().doubleValue(), 
										coord.getY().doubleValue(), 
										coord.getZ().doubleValue());
		}
	}

	public boolean isSupportedSchema(String schema) {
		for(String supportedSchema : SUPPORTED_SCHEMAS) {
			if(supportedSchema.equals(schema)) {
				return true;
			}
		}
		return false;
	}

	public Object parse(InputStream input) {
		return parseXML(input);
	}

	public String[] getSupportedRootClasses() {
		return new String[]{FeatureCollection.class.getName()};
	}

	public boolean isSupportedEncoding(String encoding) {
		return true;
	}

	public boolean isSupportedRootClass(String clazzName) {
		if(clazzName.equals(FeatureCollection.class.getName())) {
			return true;
		}
		return false;
	}

	
}
