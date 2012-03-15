/***************************************************************
 This implementation provides a framework to publish processes to the
web through the  OGC Web Processing Service interface. The framework 
is extensible in terms of processes and data handlers. It is compliant 
to the WPS version 0.4.0 (OGC 05-007r4). 

 Copyright (C) 2006 by con terra GmbH

 Authors: 
	 Bastian Sch�ffer, IfGI; Matthias Mueller, TU Dresden

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
package org.n52.wps.io.datahandler.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.geotools.feature.DefaultFeatureCollections;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.GeometryAttributeImpl;
import org.geotools.feature.type.GeometryDescriptorImpl;
import org.geotools.feature.type.GeometryTypeImpl;
import org.geotools.filter.identity.GmlObjectIdImpl;
import org.geotools.gml3.ApplicationSchemaConfiguration;
import org.geotools.gml3.GMLConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;
import org.n52.wps.commons.context.ExecutionContext;
import org.n52.wps.commons.context.ExecutionContextFactory;
import org.n52.wps.io.SchemaRepository;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.filter.identity.Identifier;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.vividsolutions.jts.geom.Geometry;


/**
 * This parser handles xml files compliant to gmlpacket.xsd 
 * @author schaeffer
 *
 */
public class GML3BasicParser extends AbstractParser {
	private static Logger LOGGER = Logger.getLogger(GML3BasicParser.class);

	
	public GML3BasicParser() {
		super();
		supportedIDataTypes.add(GTVectorDataBinding.class);
	}
	
	@Override
	public GTVectorDataBinding parse(InputStream stream, String mimeType, String schema) {
		
		ExecutionContext context = ExecutionContextFactory.getContext();
		FileOutputStream fos = null;
		try{
			File tempFile = File.createTempFile("wps", "tmp");
			finalizeFiles.add(tempFile); // mark for final delete
			fos = new FileOutputStream(tempFile);
			int i = stream.read();
			while(i != -1){
				fos.write(i);
				i = stream.read();
			}
			fos.flush();
			fos.close();
			GTVectorDataBinding data = parseXML(tempFile);
			return data;
		}
		catch(IOException e) {
			if (fos != null) try { fos.close(); } catch (Exception e1) { }
			throw new IllegalArgumentException("Error while creating tempFile", e);
		}
	}
	
	private GTVectorDataBinding parseXML(File file) {
		QName schematypeTuple = determineFeatureTypeSchema(file);
		Configuration configuration = null;
		if(schematypeTuple != null) {
			//throw new NullPointerException("featureTypeSchema null for file: " + file.getPath());
		
			
			//create the parser with the gml 2.0 configuration
			//org.geotools.xml.Configuration configuration = new org.geotools.gml2.GMLConfiguration();
			
			String schemaLocation =  schematypeTuple.getLocalPart();
			//schemaLocation = URLEncoder.encode(schemaLocation);
			
			if(schemaLocation!= null && schematypeTuple.getNamespaceURI()!=null){
				SchemaRepository.registerSchemaLocation(schematypeTuple.getNamespaceURI(), schemaLocation);
				configuration =  new ApplicationSchemaConfiguration(schematypeTuple.getNamespaceURI(), schemaLocation);
			}else{
				configuration = new GMLConfiguration();
	        	configuration.getProperties().add(Parser.Properties.IGNORE_SCHEMA_LOCATION );
	        	configuration.getProperties().add(Parser.Properties.PARSE_UNKNOWN_ELEMENTS);
			}
		}else{
			configuration = new GMLConfiguration();
        	configuration.getProperties().add(Parser.Properties.IGNORE_SCHEMA_LOCATION );
        	configuration.getProperties().add(Parser.Properties.PARSE_UNKNOWN_ELEMENTS);

		}
		
		org.geotools.xml.Parser parser = new org.geotools.xml.Parser(configuration);
		
		//parse
		FeatureCollection fc = DefaultFeatureCollections.newCollection();
		try {
//			String filepath =URLDecoder.decode(uri.toASCIIString().replace("file:/", ""));
			Object parsedData =  parser.parse( new FileInputStream(file));
			if(parsedData instanceof FeatureCollection){
				fc = (FeatureCollection) parsedData;
				
					
				
			}else{
				List<SimpleFeature> featureList = ((ArrayList<SimpleFeature>)((HashMap) parsedData).get("featureMember"));
				if(featureList!=null){
					for(SimpleFeature feature : featureList){
						fc.add(feature);
					}
				}else{
					fc = (FeatureCollection) ((HashMap) parsedData).get("FeatureCollection");
				}
			}
		
		Iterator featureIterator = fc.iterator();
		while(featureIterator.hasNext()){
			SimpleFeature feature = (SimpleFeature) featureIterator.next();
			if(feature.getDefaultGeometry()==null){
				Collection<org.opengis.feature.Property>properties = feature.getProperties();
				for(org.opengis.feature.Property property : properties){
					try{
						
						Geometry g = (Geometry)property.getValue();
						if(g!=null){
							GeometryAttribute oldGeometryDescriptor = feature.getDefaultGeometryProperty();
							GeometryType type = new GeometryTypeImpl(property.getName(),(Class)oldGeometryDescriptor.getType().getBinding(),oldGeometryDescriptor.getType().getCoordinateReferenceSystem(),oldGeometryDescriptor.getType().isIdentified(),oldGeometryDescriptor.getType().isAbstract(),oldGeometryDescriptor.getType().getRestrictions(),oldGeometryDescriptor.getType().getSuper(),oldGeometryDescriptor.getType().getDescription());
																
							GeometryDescriptor newGeometryDescriptor = new GeometryDescriptorImpl(type,property.getName(),0,1,true,null);
							Identifier identifier = new GmlObjectIdImpl(feature.getID());
							GeometryAttributeImpl geo = new GeometryAttributeImpl((Object)g,newGeometryDescriptor, identifier);
							feature.setDefaultGeometryProperty(geo);
							feature.setDefaultGeometry(g);
							
						}
					}catch(ClassCastException e){
						//do nothing
					}
					
				}
			}
		}
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (SAXException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		GTVectorDataBinding data = new GTVectorDataBinding(fc);
		
		return data;
	}
	
	private QName determineFeatureTypeSchema(File file) {
		try {
			GML2Handler handler = new GML2Handler();
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setNamespaceAware(true);
			factory.newSAXParser().parse(new FileInputStream(file), (DefaultHandler)handler); 
			String schemaUrl = handler.getSchemaUrl(); 
			if(schemaUrl == null){
				return null;
			}
			String namespaceURI = handler.getNameSpaceURI();
			return new QName(namespaceURI,schemaUrl);
			
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
		catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
		catch (SAXException e) {
			throw new IllegalArgumentException(e);
		}
		catch(ParserConfigurationException e) {
			throw new IllegalArgumentException(e);
		}
	}

}
