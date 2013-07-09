/***************************************************************
Copyright © 2012 52°North Initiative for Geospatial Open Source Software GmbH

 Author: < >

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
package org.n52.wps.io.datahandler.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.GeometryAttributeImpl;
import org.geotools.feature.type.GeometryDescriptorImpl;
import org.geotools.feature.type.GeometryTypeImpl;
import org.geotools.filter.identity.GmlObjectIdImpl;
import org.geotools.gml3.ApplicationSchemaConfiguration;
import org.geotools.gml3.v3_2.GMLConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;
import org.n52.wps.io.SchemaRepository;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.filter.identity.Identifier;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.Geometry;


/**
 * This parser handles xml files for GML 3.2.1
 *  
 * @author matthes rieke
 */
public class GML32BasicParser extends AbstractParser {
	
	private static Logger LOGGER = LoggerFactory.getLogger(GML32BasicParser.class);
	private Configuration configuration;


	public GML32BasicParser() {
		super();
		supportedIDataTypes.add(GTVectorDataBinding.class);
	}
	
	public void setConfiguration(Configuration config) {
		this.configuration = config;
	}

	@Override
	public GTVectorDataBinding parse(InputStream stream, String mimeType, String schema) {

		FileOutputStream fos = null;
		try {
			File tempFile = File.createTempFile("wps", "tmp");
			finalizeFiles.add(tempFile); // mark for final delete
			fos = new FileOutputStream(tempFile);
			int i = stream.read();
			while (i != -1) {
				fos.write(i);
				i = stream.read();
			}
			fos.flush();
			fos.close();

			QName schematypeTuple = determineFeatureTypeSchema(tempFile);
			return parse(new FileInputStream(tempFile), schematypeTuple);
		}
		catch (IOException e) {
			if (fos != null) try { fos.close(); } catch (Exception e1) { }
			throw new IllegalArgumentException("Error while creating tempFile", e);
		}
	}

	public GTVectorDataBinding parse(InputStream input, QName schematypeTuple) {
		if (configuration == null) {
			configuration = resolveConfiguration(schematypeTuple);
		}

		Parser parser = new Parser(configuration);
		parser.setStrict(true);

		//parse
		FeatureCollection fc = resolveFeatureCollection(parser, input);

		GTVectorDataBinding data = new GTVectorDataBinding(fc);

		return data;
	}
	

	private FeatureCollection resolveFeatureCollection(Parser parser, InputStream input) {
		FeatureCollection fc = null;
		try {
			Object parsedData = parser.parse(input);
			if (parsedData instanceof FeatureCollection){
				fc = (FeatureCollection) parsedData;
			} else {
				List<SimpleFeature> featureList = ((ArrayList<SimpleFeature>)((HashMap) parsedData).get("featureMember"));
				if (featureList != null){
					for( SimpleFeature feature : featureList){
						fc.add(feature);
					}
				} else {
					fc = (FeatureCollection) ((Map) parsedData).get("FeatureCollection");
				}
			}

			FeatureIterator featureIterator = fc.features();
			while (featureIterator.hasNext()) {
				SimpleFeature feature = (SimpleFeature) featureIterator.next();
				
				if (feature.getDefaultGeometry() == null) {
					Collection<Property> properties = feature.getProperties();
					for (Property property : properties){
						try {
							Geometry g = (Geometry) property.getValue();
							if (g != null) {
								GeometryAttribute oldGeometryDescriptor = feature.getDefaultGeometryProperty();
								GeometryType type = new GeometryTypeImpl(property.getName(), (Class<?>) oldGeometryDescriptor.getType().getBinding(),
										oldGeometryDescriptor.getType().getCoordinateReferenceSystem(),
										oldGeometryDescriptor.getType().isIdentified(),
										oldGeometryDescriptor.getType().isAbstract(),
										oldGeometryDescriptor.getType().getRestrictions(),
										oldGeometryDescriptor.getType().getSuper()
										,oldGeometryDescriptor.getType().getDescription());

								GeometryDescriptor newGeometryDescriptor = new GeometryDescriptorImpl(type, property.getName(), 0, 1, true, null);
								Identifier identifier = new GmlObjectIdImpl(feature.getID());
								GeometryAttributeImpl geo = new GeometryAttributeImpl((Object) g, newGeometryDescriptor, identifier);
								feature.setDefaultGeometryProperty(geo);
								feature.setDefaultGeometry(g);

							}
						} catch (ClassCastException e){
							//do nothing
						}

					}
				}
			}
		} catch (IOException e) {
			LOGGER.warn(e.getMessage(), e);
			throw new RuntimeException(e);
		} catch (SAXException e) {
			LOGGER.warn(e.getMessage(), e);
			throw new RuntimeException(e);
		} catch (ParserConfigurationException e) {
			LOGGER.warn(e.getMessage(), e);
			throw new RuntimeException(e);
		}
		
		return fc;
	}
	

	private Configuration resolveConfiguration(QName schematypeTuple) {
		/*
		 * TODO all if-statements are nonsense.. clean up
		 */
		Configuration configuration = null;
		if (schematypeTuple != null) {
			String schemaLocation =  schematypeTuple.getLocalPart();
			if (schemaLocation.startsWith("http://schemas.opengis.net/gml/3.2")){
				configuration = new GMLConfiguration();
			} else {
				if (schemaLocation != null && schematypeTuple.getNamespaceURI()!=null){
					SchemaRepository.registerSchemaLocation(schematypeTuple.getNamespaceURI(), schemaLocation);
					configuration =  new ApplicationSchemaConfiguration(schematypeTuple.getNamespaceURI(), schemaLocation);
				} else {
					configuration = new GMLConfiguration();
				}
			}
		} else{
			configuration = new GMLConfiguration();
		}
		
		return configuration;
	}

	private QName determineFeatureTypeSchema(File file) {
		try {
			GML2Handler handler = new GML2Handler();
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setNamespaceAware(true);

			factory.newSAXParser().parse(new FileInputStream(file), handler); 

			String schemaUrl = handler.getSchemaUrl(); 

			if(schemaUrl == null){
				return null;
			}

			String namespaceURI = handler.getNameSpaceURI();

			/*
			 * TODO dude, wtf? Massive abuse of QName.
			 */
			return new QName(namespaceURI, schemaUrl);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		} catch (SAXException e) {
			throw new IllegalArgumentException(e);
		} catch(ParserConfigurationException e) {
			throw new IllegalArgumentException(e);
		}
	}

	
	public static GML32BasicParser getInstanceForConfiguration(
			Configuration config) {
		GML32BasicParser parser = new GML32BasicParser();
		parser.setConfiguration(config);
		return parser;
	}



}

