/***************************************************************
Copyright © 2011 52°North Initiative for Geospatial Open Source Software GmbH

 Author:

 Contact: Andreas Wytzisk, 
 52°North Initiative for Geospatial Open Source SoftwareGmbH, 
 Martin-Luther-King-Weg 24,
 48155 Muenster, Germany, 
 info@52north.org

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 version 2 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; even without the implied WARRANTY OF
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program (see gnu-gpl v2.txt). If not, write to
 the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA 02111-1307, USA or visit the Free
 Software Foundation’s web page, http://www.fsf.org.

 ***************************************************************/

package org.n52.wps.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Collection;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.n52.wps.ServerDocument.Server;
import org.n52.wps.commons.WPSConfig;
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
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class GTHelper {
	private static Logger LOGGER = LoggerFactory.getLogger(GTHelper.class);
	
	public static SimpleFeatureType createFeatureType(Collection<Property> attributes, Geometry newGeometry, String uuid, CoordinateReferenceSystem coordinateReferenceSystem){
		String namespace = "http://www.52north.org/"+uuid;
		
		SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
		if(coordinateReferenceSystem==null){
			coordinateReferenceSystem= getDefaultCRS();			
		}
		typeBuilder.setCRS(coordinateReferenceSystem);
		typeBuilder.setNamespaceURI(namespace);
		Name nameType = new NameImpl(namespace, "Feature-"+uuid);
		typeBuilder.setName(nameType);
		
		for(Property property : attributes){
			if(property.getValue()!=null){ 
				String name = property.getName().getLocalPart();
				Class<?> binding = property.getType().getBinding();
				if(binding.equals(Envelope.class)){
					continue;
				}
				if( 
				   (binding.equals(Geometry.class) ||
				    binding.equals(GeometryCollection.class) ||
				   binding.equals(MultiCurve.class) || 
				   binding.equals(MultiLineString.class) ||
				   binding.equals(Curve.class) ||
				   binding.equals(MultiPoint.class) ||
				   binding.equals(MultiPolygon.class) ||
				   binding.equals(MultiSurface.class) ||
				   binding.equals(LineString.class) ||
				   binding.equals(Point.class) ||
				   binding.equals(LineString.class) ||
				   binding.equals(Polygon.class)) 				  
				 &&!name.equals("location")){
									   
					
					if(newGeometry.getClass().equals(Point.class) && (!name.equals("location"))){
						typeBuilder.add("GEOMETRY", MultiPoint.class);
					}else if(newGeometry.getClass().equals(LineString.class) && (!name.equals("location"))){
					
						typeBuilder.add("GEOMETRY", MultiLineString.class);
					}else if( newGeometry.getClass().equals(Polygon.class) && (!name.equals("location"))){
					
						typeBuilder.add("GEOMETRY", MultiPolygon.class);
					}else if(!binding.equals(Object.class)){
						typeBuilder.add("GEOMETRY", newGeometry.getClass());
					}
				}else{
					if(!name.equals("location") && binding.equals(Object.class)){
						try{
							Geometry g = (Geometry)property.getValue();
							if(g.getClass().equals(Point.class) && (!name.equals("location"))){
								typeBuilder.add("GEOMETRY", MultiPoint.class);
							}else if(g.getClass().equals(LineString.class) && (!name.equals("location"))){
							
								typeBuilder.add("GEOMETRY", MultiLineString.class);
							}else if( g.getClass().equals(Polygon.class) && (!name.equals("location"))){
							
								typeBuilder.add("GEOMETRY", MultiPolygon.class);
							}else{
								typeBuilder.add("GEOMETRY", g.getClass());
							}
							
						}catch(ClassCastException e){
							
						}
						
					}else if(!name.equals("location")){
						typeBuilder.add(name, binding);
					}
				}
			}
		
		 
		}
		
		SimpleFeatureType featureType;
		
		featureType = typeBuilder.buildFeatureType();
		return featureType;
	}
	
	
	
	public static SimpleFeatureType createFeatureType(Geometry newGeometry, String uuid, CoordinateReferenceSystem coordinateReferenceSystem){
		String namespace = "http://www.52north.org/"+uuid;
		
		SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
		if(coordinateReferenceSystem==null){
			coordinateReferenceSystem= getDefaultCRS();
		}
		typeBuilder.setCRS(coordinateReferenceSystem);
		typeBuilder.setNamespaceURI(namespace);
		Name nameType = new NameImpl(namespace, "Feature-"+uuid);
		typeBuilder.setName(nameType);		
					
		typeBuilder.add("GEOMETRY", newGeometry.getClass());
					
		SimpleFeatureType featureType;
		
		featureType = typeBuilder.buildFeatureType();
		return featureType;
	}
	
	public static SimpleFeature createFeature(String id, Geometry geometry, SimpleFeatureType featureType, Collection<Property> originalAttributes) {
		
			if(geometry==null || geometry.isEmpty()){
				return null;
			}
			
			SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
			SimpleFeature feature = null;
			Collection<PropertyDescriptor> featureTypeAttributes = featureType.getDescriptors();
						
			Object[] newData = new Object[featureType.getDescriptors().size()];
			
			int i = 0;
			for(PropertyDescriptor propertyDescriptor : featureTypeAttributes){
				for(Property originalProperty : originalAttributes){
					if(propertyDescriptor.getName().getLocalPart().equals(originalProperty.getName().getLocalPart())){
						if(propertyDescriptor instanceof GeometryDescriptor){
							newData[i] = geometry;
						}else{
							newData[i] = originalProperty.getValue();
						}
					}
				}
				
				if(propertyDescriptor instanceof GeometryDescriptor){
					if(geometry.getGeometryType().equals("Point")){
						Point[] points = new Point[1];
						points[0] = (Point)geometry;
						newData[i] = geometry.getFactory().createMultiPoint(points);
					}else
						if(geometry.getGeometryType().equals("LineString")){
							LineString[] lineString = new LineString[1];
							lineString[0] = (LineString)geometry;
							newData[i] = geometry.getFactory().createMultiLineString(lineString);
						}else
							if(geometry.getGeometryType().equals("Polygon")){
							Polygon[] polygons = new Polygon[1];
							polygons[0] = (Polygon)geometry;
							newData[i] = geometry.getFactory().createMultiPolygon(polygons);
							}else{
								newData[i] = geometry;
							}
					
				}
				i++;
			}
				
		
			
			feature = featureBuilder.buildFeature(id, newData);
		
			return feature;
	}
	
	public static Feature createFeature(String id, Geometry geometry, SimpleFeatureType featureType) {
		
		if(geometry==null || geometry.isEmpty()){
			return null;
		}
		
		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
		SimpleFeature feature = null;
					
		Object[] newData = new Object[featureType.getDescriptors().size()];
		
		int i = 0;
	
		if(geometry.getGeometryType().equals("Point")){
			Point[] points = new Point[1];
			points[0] = (Point)geometry;
			newData[i] = geometry.getFactory().createMultiPoint(points);
		}else
			if(geometry.getGeometryType().equals("LineString")){
				LineString[] lineString = new LineString[1];
				lineString[0] = (LineString)geometry;
				newData[i] = geometry.getFactory().createMultiLineString(lineString);
			}else
				if(geometry.getGeometryType().equals("Polygon")){
				Polygon[] polygons = new Polygon[1];
				polygons[0] = (Polygon)geometry;
				newData[i] = geometry.getFactory().createMultiPolygon(polygons);
				}else{
					newData[i] = geometry;
				}
			
		
		feature = featureBuilder.buildFeature(id, newData);
	
		return feature;
}
	
		public static QName createGML3SchemaForFeatureType(SimpleFeatureType featureType){
		
		String uuid = featureType.getName().getNamespaceURI().replace("http://www.52north.org/", "");
		String namespace = "http://www.52north.org/"+uuid;
		String schema = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xs:schema targetNamespace=\""+namespace+"\" " +
				"xmlns:n52=\""+namespace+"\" "+
				"xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" "+
				"xmlns:gml=\"http://www.opengis.net/gml\" "+
				"elementFormDefault=\"qualified\" "+
				"version=\"1.0\"> "+
				"<xs:import namespace=\"http://www.opengis.net/gml\" "+
				"schemaLocation=\"http://schemas.opengis.net/gml/3.1.1/base/gml.xsd\"/> ";
				
		String typeName = featureType.getGeometryDescriptor().getType().getBinding().getName();
		String geometryTypeName = "";
		if(typeName.contains("Point")){
	    	geometryTypeName = "PointPropertyType";
	    }
	  	     
		 if(typeName.contains("MultiPoint")){
			 geometryTypeName = "MultiPointPropertyType";	
					    }
		 if(typeName.contains("LineString")){
			 geometryTypeName = "CurvePropertyType";	
		 }
		 if(typeName.contains("MultiLineString")){
			 geometryTypeName = "MultiCurvePropertyType";	
		 }
		 if(typeName.contains("Polygon")){
			 geometryTypeName = "SurfacePropertyType";	
		 }
		 if(typeName.contains("MultiPolygon")){
			 geometryTypeName = "MultiSurfacePropertyType";	
		 }		
				
				// add feature type definition and generic geometry
			schema = schema + "<xs:element name=\"Feature-"+uuid+"\" type=\"n52:FeatureType\" substitutionGroup=\"gml:_Feature\"/> " +
					"<xs:complexType name=\"FeatureType\"> " +
					"<xs:complexContent> " +
					"<xs:extension base=\"gml:AbstractFeatureType\"> "+
					"<xs:sequence> " +
					//"<xs:element name=\"GEOMETRY\" type=\"gml:GeometryPropertyType\"> "+					
					"<xs:element name=\"GEOMETRY\" type=\"gml:"+geometryTypeName+"\"> "+
					"</xs:element> ";
			
			//add attributes
			Collection<PropertyDescriptor> attributes = featureType.getDescriptors();
			for(PropertyDescriptor property : attributes){
				String attributeName = property.getName().getLocalPart();
				if(!(property instanceof GeometryDescriptor)){
					
					if(property.getType().getBinding().equals(String.class) ){
						schema = schema + "<xs:element name=\""+attributeName+"\" minOccurs=\"0\" maxOccurs=\"1\"> "+
						"<xs:simpleType> ";
						schema = schema + "<xs:restriction base=\"xs:string\"> "+
						"</xs:restriction> "+
						"</xs:simpleType> "+
						"</xs:element> ";
					}else if(property.getType().getBinding().equals(Integer.class)|| property.getType().getBinding().equals(BigInteger.class)){
						schema = schema + "<xs:element name=\""+attributeName+"\" minOccurs=\"0\" maxOccurs=\"1\"> "+
						"<xs:simpleType> ";
						schema = schema + "<xs:restriction base=\"xs:integer\"> "+
						"</xs:restriction> "+
						"</xs:simpleType> "+
						"</xs:element> ";
					}else if(property.getType().getBinding().equals(Double.class)){
						schema = schema + "<xs:element name=\""+attributeName+"\" minOccurs=\"0\" maxOccurs=\"1\"> "+
						"<xs:simpleType> ";
						schema = schema + "<xs:restriction base=\"xs:integer\"> "+
						"</xs:restriction> "+
						"</xs:simpleType> "+
						"</xs:element> ";
					}
				}
			}		
			
			//close
			schema = schema +  "</xs:sequence> "+
		      "</xs:extension> "+
		      "</xs:complexContent> "+
		    "</xs:complexType> "+
		  "</xs:schema>";
			String schemalocation = "";
			try {
				schemalocation = storeSchema(schema, uuid);
				
			} catch (IOException e) {
				LOGGER.error("Exception while storing schema.", e);
				throw new RuntimeException("Exception while storing schema.", e);
			}
			return new QName(namespace, schemalocation);
			
		}
		
		public static QName createGML2SchemaForFeatureType(SimpleFeatureType featureType){
			
			String uuid = featureType.getName().getNamespaceURI().replace("http://www.52north.org/", "");
			String namespace = "http://www.52north.org/"+uuid;
			String schema = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xs:schema targetNamespace=\""+namespace+"\" " +
					"xmlns:n52=\""+namespace+"\" "+
					"xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" "+
					"xmlns:gml=\"http://www.opengis.net/gml\" "+
					"elementFormDefault=\"qualified\" "+
					"version=\"1.0\"> "+
					"<xs:import namespace=\"http://www.opengis.net/gml\" "+
					"schemaLocation=\"http://schemas.opengis.net/gml/2.1.2/feature.xsd\"/> ";
				
					// add feature type definition and generic geometry
				schema = schema + "<xs:element name=\"Feature\" type=\"n52:FeatureType\" substitutionGroup=\"gml:_Feature\"/> " +
						"<xs:complexType name=\"FeatureType\"> " +
						"<xs:complexContent> " +
						"<xs:extension base=\"gml:AbstractFeatureType\"> "+
						"<xs:sequence> " +
						"<xs:element name=\"GEOMETRY\" type=\"gml:GeometryPropertyType\"> "+
						"</xs:element> ";
				
				//add attributes
				Collection<PropertyDescriptor> attributes = featureType.getDescriptors();
				for(PropertyDescriptor property : attributes){
					String attributeName = property.getName().getLocalPart();
					if(!(property instanceof GeometryDescriptor)){
						
						if(property.getType().getBinding().equals(String.class) ){
							schema = schema + "<xs:element name=\""+attributeName+"\" minOccurs=\"0\" maxOccurs=\"1\"> "+
							"<xs:simpleType> ";
							schema = schema + "<xs:restriction base=\"xs:string\"> "+
							"</xs:restriction> "+
							"</xs:simpleType> "+
							"</xs:element> ";
						}else if(property.getType().getBinding().equals(Integer.class)|| property.getType().getBinding().equals(BigInteger.class)){
							schema = schema + "<xs:element name=\""+attributeName+"\" minOccurs=\"0\" maxOccurs=\"1\"> "+
							"<xs:simpleType> ";
							schema = schema + "<xs:restriction base=\"xs:integer\"> "+
							"</xs:restriction> "+
							"</xs:simpleType> "+
							"</xs:element> ";
						}else if(property.getType().getBinding().equals(Double.class)){
							schema = schema + "<xs:element name=\""+attributeName+"\" minOccurs=\"0\" maxOccurs=\"1\"> "+
							"<xs:simpleType> ";
							schema = schema + "<xs:restriction base=\"xs:integer\"> "+
							"</xs:restriction> "+
							"</xs:simpleType> "+
							"</xs:element> ";
						}
					}
				}		
				
				//close
				schema = schema +  "</xs:sequence> "+
			      "</xs:extension> "+
			      "</xs:complexContent> "+
			    "</xs:complexType> "+
			  "</xs:schema>";
				String schemalocation = "";
				try {
					schemalocation = storeSchema(schema, uuid);
					
				} catch (IOException e) {
					LOGGER.error("Exception while storing schema.", e);
					throw new RuntimeException("Exception while storing schema.", e);
				}
				return new QName(namespace, schemalocation);
				
			}

		public static String storeSchema(String schema, String uuid) throws IOException {
			Server server = WPSConfig.getInstance().getWPSConfig().getServer();
			String hostname = server.getHostname();
			String port = server.getHostport();
			String webapp = server.getWebappPath();
			
			LOGGER.debug("GTHelper hostname " + hostname);
			LOGGER.debug("GTHelper port " + port);
			LOGGER.debug("GTHelper webapp " + webapp);
			
			String domain = WPSConfig.class.getProtectionDomain().getCodeSource().getLocation().getFile();
			int startIndex = domain.indexOf("WEB-INF");
			if(startIndex<0){
				//not running as webapp				
				File f = File.createTempFile(uuid, ".xsd");
				f.deleteOnExit();
				FileWriter writer = new FileWriter(f);
				writer.write(schema);
				writer.flush();
				writer.close();
				return "file:"+f.getAbsolutePath();
			}else{
				domain = domain.substring(0,startIndex);			
				String baseDirLocation = domain;
			
				String baseDir = baseDirLocation +  "schemas" + File.separator;
				File folder = new File(baseDir);
				if(!folder.exists()){
					folder.mkdirs();
				}
				File f = new File(baseDir+uuid+".xsd");
				FileWriter writer = new FileWriter(f);
				writer.write(schema);
				writer.flush();
				writer.close();
				
				String url = "http://"+hostname+":"+port+"/"+webapp+"/schemas/"+ uuid+".xsd";
				return url;
			}
		}
		
		private static CoordinateReferenceSystem getDefaultCRS(){

			try {
				return CRS.decode("EPSG:4326");
			} catch (Exception e) {
				LOGGER.error("Exception while decoding CRS EPSG:4326", e);
			}
			return null;
		}

}
