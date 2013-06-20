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
	Matthias Mueller, TU Dresden


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

package org.n52.wps.io.datahandler.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.ArrayList;

import net.opengis.examples.packet.DataType;
import net.opengis.examples.packet.GMLPacketDocument;
import net.opengis.examples.packet.GMLPacketType;
import net.opengis.examples.packet.PropertyType;
import net.opengis.examples.packet.StaticFeatureType;
import net.opengis.examples.packet.PropertyType.Value;
import net.opengis.gml.CoordType;
import net.opengis.gml.LineStringPropertyType;
import net.opengis.gml.LinearRingMemberType;
import net.opengis.gml.LinearRingType;
import net.opengis.gml.PointPropertyType;
import net.opengis.gml.PolygonType;

import org.geotools.feature.FeatureIterator;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.opengis.feature.simple.SimpleFeature;
import org.w3c.dom.Node;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class SimpleGMLGenerator extends AbstractGenerator {
	
	
	public SimpleGMLGenerator() {
		super();
		supportedIDataTypes.add(GTVectorDataBinding.class);
	}
	
	@Override
	public InputStream generateStream(IData data, String mimeType, String schema) throws IOException {
		
		File tempFile = null;
		InputStream stream = null;
		
		try {
			tempFile = File.createTempFile("gml", "xml");
			this.finalizeFiles.add(tempFile);
			FileOutputStream outputStream = new FileOutputStream(tempFile);
			this.writeToStream(data, outputStream);
			outputStream.flush();
			outputStream.close();
			
			stream = new FileInputStream(tempFile);
		} catch (IOException e){
			throw new IOException("Unable to generate GML");
		}
		
		return stream;
	}

	public Node generateXML(IData coll, String schema) {
		return generateXMLObj(coll, schema).getDomNode();
	}

	public void write(IData coll, Writer writer) {
		GMLPacketDocument doc = generateXMLObj(coll, null);
		try {
			BufferedWriter bufferedWriter = new BufferedWriter(writer);
			bufferedWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			doc.save(bufferedWriter);
			bufferedWriter.close();
		}
		catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	private GMLPacketDocument generateXMLObj(IData coll, String schema2) {
		GMLPacketDocument doc = GMLPacketDocument.Factory.newInstance();
		GMLPacketType packet = doc.addNewGMLPacket();
		if(coll == null) {
			return doc;
		}
		FeatureIterator<?> iter = ((GTVectorDataBinding)coll).getPayload().features();
		while(iter.hasNext()) {
			SimpleFeature feature = (SimpleFeature) iter.next();
			StaticFeatureType staticFeature = packet.addNewPacketMember().addNewStaticFeature();
			Geometry geom = (Geometry) feature.getDefaultGeometry();
			String geomType = geom.getGeometryType();
			if(geomType.equals("Point")) {
				Point point = (Point)geom;
				Coordinate coord = point.getCoordinate();
				if (coord != null) {
					PointPropertyType pointType = staticFeature.addNewPointProperty();
					pointType.addNewPoint().setCoord(convertToXMLCoordType(coord));
					generateAttribute(feature, staticFeature);					
					}
			}
			else if(geomType.equals("LineString")) {
				LineString ls = (LineString)geom;
				CoordType[] coords = convertToXMLCoordType(ls.getCoordinates());
				if(coords != null) {
					ls.getCoordinates();
					LineStringPropertyType lsType = staticFeature.addNewLineStringProperty();
					lsType.addNewLineString().setCoordArray(coords);
					
					generateAttribute(feature, staticFeature);	
				}
			}
			else if(geomType.equals("Polygon")) {
				Polygon polygon = (Polygon)geom;
				PolygonType xmlPolygon = staticFeature.addNewPolygonProperty().addNewPolygon();
				xmlPolygon.setOuterBoundaryIs(convertToXMLLinearRing(polygon.getExteriorRing()));
				LinearRingMemberType innerBoundary = xmlPolygon.addNewInnerBoundaryIs();
				for(int i = 0; i < polygon.getNumInteriorRing(); i++) {
					LinearRingType innerRing = innerBoundary.addNewLinearRing();
					innerRing.setCoordArray(convertToXMLCoordType(polygon.getInteriorRingN(i).getCoordinates()));
				}
				generateAttribute(feature, staticFeature);	
			}
			else if (geomType.equals("MultiPolygon")) {
				MultiPolygon mp = (MultiPolygon)geom;
				for(int i = 0; i < mp.getNumGeometries(); i++) {
					if(i > 0) {
						staticFeature = packet.addNewPacketMember().addNewStaticFeature();
					}
					Polygon p = (Polygon) (geom.getGeometryN(i));
					
					PolygonType pType = staticFeature.addNewPolygonProperty().addNewPolygon();
					pType.setOuterBoundaryIs(convertToXMLLinearRing(p.getExteriorRing()));
					LinearRingMemberType innerBoundary = pType.addNewInnerBoundaryIs();
					for(int j = 0; j < p.getNumInteriorRing(); j++) {
						LinearRingType innerRing = innerBoundary.addNewLinearRing();
						innerRing.setCoordArray(convertToXMLCoordType(p.getInteriorRingN(j).getCoordinates()));
					}
					
				}
				generateAttribute(feature, staticFeature);	
			}
			// THE MULTILINESTRING WILL BE DEVIDED INTO NORMAL LINESTRINGs, 
			else if(geomType.equals("MultiLineString")) {
				MultiLineString mls = (MultiLineString)geom;
				for(int i = 0; i < mls.getNumGeometries(); i++) {
					if(i > 0) {
						staticFeature = packet.addNewPacketMember().addNewStaticFeature();
					}
					LineString ls = (LineString) (geom.getGeometryN(i));
					LineStringPropertyType lsType = staticFeature.addNewLineStringProperty();
					lsType.addNewLineString().setCoordArray(convertToXMLCoordType(ls.getCoordinates()));
				}
				generateAttribute(feature, staticFeature);	
			}
//			else if(geomType.equals("GeometryCollection")) {
//				GeometryCollection geomColl = (GeometryCollection)geom;
//				geomColl.get
//			}
			else if(geom.isEmpty()) {
				//GEOMETRY is empty, do nothing
				
			}
			else {
				throw new IllegalArgumentException("geometryType not supported: " + geomType);
			}
		}
		return doc;
	}

	private void generateAttribute(SimpleFeature feature,
			StaticFeatureType staticFeature) {
		if(feature.getFeatureType().getAttributeCount()>1){
			
			PropertyType propertyType;
			Value value;
			int attributePosCounter=0;
			for (Object o: feature.getAttributes()) {
				DataType.Enum dataType;
				if(o instanceof Integer){
					dataType = DataType.INTEGER;
				}else if(o instanceof String){
					dataType = DataType.STRING;
				}else if(o instanceof Boolean){
					dataType = DataType.BOOLEAN;
				}else if(o instanceof Long){
					dataType = DataType.LONG;
				}else if(o instanceof Double){
					dataType = DataType.DECIMAL;
				}
				else continue;	//Don't create anything
				
				propertyType = staticFeature.addNewProperty();
				propertyType.setPropertyName(feature.getFeatureType().getAttributeDescriptors().get(attributePosCounter).getLocalName());
				value = propertyType.addNewValue();
				value.setDataType(dataType);
				value.setStringValue(String.valueOf(o));
				attributePosCounter++;
			}
		}
	}
	
	private LinearRingMemberType convertToXMLLinearRing(LineString ls) {
		LinearRingMemberType ringMember = LinearRingMemberType.Factory.newInstance();
		LinearRingType ring = LinearRingType.Factory.newInstance();
		CoordType[] coords = convertToXMLCoordType(ls.getCoordinates());
		if(coords == null) {
			return null;
		}
		ring.setCoordArray(coords);
		ringMember.setLinearRing(ring);
		return ringMember;
	}
	
	private CoordType[] convertToXMLCoordType(Coordinate[] coords) {
		ArrayList<CoordType> coordsList = new ArrayList<CoordType>();
		for(int i = 0; i < coords.length; i++) {
			CoordType tempCoord = convertToXMLCoordType(coords[i]);
			if(tempCoord != null) {
				coordsList.add(tempCoord);
			}
		}
		if(coordsList.isEmpty()) {
			return null;
		}
		CoordType[] returnCoords = new CoordType[coordsList.size()];
		returnCoords = coordsList.toArray(returnCoords);
		return returnCoords;
	}
	
	private CoordType convertToXMLCoordType(Coordinate coord) {
		if(Double.isNaN(coord.x) || Double.isNaN(coord.y)) {
			return null;
		}
		CoordType xmlCoord = CoordType.Factory.newInstance();
		try {
			xmlCoord.setX(new BigDecimal(Double.toString(coord.x)));
			xmlCoord.setY(new BigDecimal(Double.toString(coord.y)));
		}
		catch(NumberFormatException e) {
			throw new IllegalArgumentException(e);
		}
		if(!Double.isNaN(coord.z)) {
			xmlCoord.setZ(BigDecimal.valueOf(coord.z));
		}
		return xmlCoord;
	}

	public void writeToStream(IData coll, OutputStream os) {
		OutputStreamWriter w = new OutputStreamWriter(os);
		write (coll, w);		
	}

}