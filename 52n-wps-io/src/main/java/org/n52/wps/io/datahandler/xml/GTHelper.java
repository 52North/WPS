package org.n52.wps.io.datahandler.xml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Collection;

import javax.xml.namespace.QName;

import org.geotools.feature.GeometryAttributeImpl;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.feature.type.GeometryDescriptorImpl;
import org.geotools.filter.identity.GmlObjectIdImpl;
import org.n52.wps.ServerDocument.Server;
import org.n52.wps.commons.WPSConfig;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.identity.Identifier;
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

public class GTHelper {
	
	public static SimpleFeatureType createFeatureType(Collection<Property> attributes, Geometry newGeometry, String uuid){
		String namespace = "http://www.52north.org/"+uuid;
		
		SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
		typeBuilder.setNamespaceURI(namespace);
		Name nameType = new NameImpl(namespace, "Feature");
		typeBuilder.setName(nameType);
		
		
		
		for(Property property : attributes){
			if(property.getValue()!=null){ 
				String name = property.getName().getLocalPart();
				Class binding = property.getType().getBinding();
				if( 
				   (binding.equals(GeometryCollection.class) ||
				   binding.equals(MultiCurve.class) || 
				   binding.equals(MultiLineString.class) ||
				   binding.equals(Curve.class) ||
				   binding.equals(MultiPoint.class) ||
				   binding.equals(MultiPolygon.class) ||
				   binding.equals(MultiSurface.class) ||
				   binding.equals(LineString.class) ||
				   binding.equals(LinearRing.class) ||
				   binding.equals(Point.class) ||
				   binding.equals(Polygon.class))&&(!name.equals("location"))){
				   			   
			
					typeBuilder.add("GEOMETRY", newGeometry.getClass());
				}else if(!binding.equals(Geometry.class) && !binding.equals(Object.class)){
					typeBuilder.add(name, property.getType().getBinding());
				}else if(!name.equals("location")){
					try{
						Geometry g = (Geometry)property.getValue();
						typeBuilder.add("GEOMETRY", newGeometry.getClass());
					}catch(ClassCastException e){
						//do nothing
					}
				}
			}
		 
		}
		
		SimpleFeatureType featureType;
		
		featureType = typeBuilder.buildFeatureType();
		return featureType;
	}
	
	public static Feature createFeature(String id, Geometry geometry, SimpleFeatureType featureType, Collection<Property> originalAttributes) {
		
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
					System.out.println(geometry);
					newData[i] = geometry;
				}
				i++;
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return new QName(namespace, schemalocation);
			
		}

		public static String storeSchema(String schema, String uuid) throws IOException {
			Server server = WPSConfig.getInstance().getWPSConfig().getServer();
			String hostname = "localhost";//server.getHostname();
			String port = server.getHostport();
			String webapp = server.getWebappPath();
			
			String baseDirLocation = Server.class.getProtectionDomain().getCodeSource().toString();
			int startIndex = baseDirLocation.indexOf("WEB-INF");
			baseDirLocation = baseDirLocation.substring(0,startIndex);
			baseDirLocation = baseDirLocation.replace("(file:/", "");
			
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
