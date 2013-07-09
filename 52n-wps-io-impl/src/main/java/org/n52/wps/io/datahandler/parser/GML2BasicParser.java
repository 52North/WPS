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

package org.n52.wps.io.datahandler.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.xml.namespace.QName;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.DefaultFeatureCollections;
import org.geotools.feature.GeometryAttributeImpl;
import org.geotools.feature.type.GeometryDescriptorImpl;
import org.geotools.feature.type.GeometryTypeImpl;
import org.geotools.filter.identity.GmlObjectIdImpl;
import org.geotools.gml2.GMLConfiguration;
import org.geotools.gml3.ApplicationSchemaConfiguration;
import org.geotools.xml.Configuration;
import org.n52.wps.io.SchemaRepository;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.filter.identity.Identifier;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.vividsolutions.jts.geom.Geometry;

/**
 * This parser handles xml files compliant to GML2.
 * 
 * @author foerster
 * 
 */
public class GML2BasicParser extends AbstractParser {
	private static Logger LOGGER = LoggerFactory.getLogger(GML2BasicParser.class);

	public GML2BasicParser() {
		super();
		supportedIDataTypes.add(GTVectorDataBinding.class);
	}

	public GTVectorDataBinding parse(InputStream stream, String mimeType,
			String schema) {

		FileOutputStream fos = null;
		try {
			File tempFile = File.createTempFile(UUID.randomUUID().toString(),
					".gml2");
			finalizeFiles.add(tempFile); // mark for final delete
			fos = new FileOutputStream(tempFile);
			int i = stream.read();
			while (i != -1) {
				fos.write(i);
				i = stream.read();
			}
			fos.flush();
			fos.close();
			GTVectorDataBinding data = parseXML(tempFile);

			return data;
		} catch (IOException e) {
			if (fos != null)
				try {
					fos.close();
				} catch (Exception e1) {
				}
			throw new IllegalArgumentException("Error while creating tempFile",
					e);
		}
	}

	public GTVectorDataBinding parseXML(File file) {
		
		SimpleFeatureCollection fc = parseSimpleFeatureCollection(file);
		
		GTVectorDataBinding data = new GTVectorDataBinding(fc);

		return data;
	}
	
	public SimpleFeatureCollection parseSimpleFeatureCollection(File file) {
		QName schematypeTuple = determineFeatureTypeSchema(file);

		Configuration configuration = null;

		boolean shouldSetParserStrict = true;

		String schemaLocation = schematypeTuple.getLocalPart();

		if (schemaLocation != null && schematypeTuple.getNamespaceURI() != null) {
			SchemaRepository.registerSchemaLocation(
					schematypeTuple.getNamespaceURI(), schemaLocation);
			configuration = new ApplicationSchemaConfiguration(
					schematypeTuple.getNamespaceURI(), schemaLocation);
		} else {
			configuration = new GMLConfiguration();
			shouldSetParserStrict = false;
		}

		org.geotools.xml.Parser parser = new org.geotools.xml.Parser(
				configuration);

		// parse
		SimpleFeatureCollection fc = DefaultFeatureCollections.newCollection();
		try {
			Object parsedData = null;
			try {
				parser.setStrict(shouldSetParserStrict);
				parsedData = parser.parse(new FileInputStream(file));
			} catch (SAXException e5) {
				// assume the xsd containing the schema was not found
				configuration = new GMLConfiguration();
				parser = new org.geotools.xml.Parser(configuration);
				parser.setStrict(false);
				parsedData = parser.parse(new FileInputStream(file));
			}
			if (parsedData instanceof SimpleFeatureCollection) {
				fc = (SimpleFeatureCollection) parsedData;
			} else {
				List<?> possibleSimpleFeatureList = ((ArrayList<?>) ((HashMap<?, ?>) parsedData)
						.get("featureMember"));

				if (possibleSimpleFeatureList != null) {
					List<SimpleFeature> simpleFeatureList = new ArrayList<SimpleFeature>();

					SimpleFeatureType sft = null;

					for (Object possibleSimpleFeature : possibleSimpleFeatureList) {

						if (possibleSimpleFeature instanceof SimpleFeature) {
							SimpleFeature sf = ((SimpleFeature) possibleSimpleFeature);
							if (sft == null) {
								sft = sf.getType();
							}
							simpleFeatureList.add(sf);
						}
					}

					fc = new ListFeatureCollection(sft, simpleFeatureList);
				} else {
					fc = (SimpleFeatureCollection) ((HashMap<?, ?>) parsedData)
							.get("FeatureCollection");
				}
			}

			SimpleFeatureIterator featureIterator = fc.features();
			while (featureIterator.hasNext()) {
				SimpleFeature feature = (SimpleFeature) featureIterator.next();
				if (feature.getDefaultGeometry() == null) {
					Collection<org.opengis.feature.Property> properties = feature
							.getProperties();
					for (org.opengis.feature.Property property : properties) {
						try {

							Geometry g = (Geometry) property.getValue();
							if (g != null) {
								GeometryAttribute oldGeometryDescriptor = feature
										.getDefaultGeometryProperty();
								GeometryType type = new GeometryTypeImpl(
										property.getName(),
										(Class<?>) oldGeometryDescriptor
												.getType().getBinding(),
										oldGeometryDescriptor.getType()
												.getCoordinateReferenceSystem(),
										oldGeometryDescriptor.getType()
												.isIdentified(),
										oldGeometryDescriptor.getType()
												.isAbstract(),
										oldGeometryDescriptor.getType()
												.getRestrictions(),
										oldGeometryDescriptor.getType()
												.getSuper(),
										oldGeometryDescriptor.getType()
												.getDescription());

								GeometryDescriptor newGeometryDescriptor = new GeometryDescriptorImpl(
										type, property.getName(), 0, 1, true,
										null);
								Identifier identifier = new GmlObjectIdImpl(
										feature.getID());
								GeometryAttributeImpl geo = new GeometryAttributeImpl(
										(Object) g, newGeometryDescriptor,
										identifier);
								feature.setDefaultGeometryProperty(geo);
								feature.setDefaultGeometry(g);

							}
						} catch (ClassCastException e) {
							// do nothing
						}

					}
				}
			}
			return fc;
		} catch (Exception e) {
			LOGGER.error(
					"Exception while trying to parse GML2 FeatureCollection.",
					e);
			throw new RuntimeException(e);
		}
	}

	private QName determineFeatureTypeSchema(File file) {
		try {
			GML2Handler handler = new GML2Handler();
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setNamespaceAware(true);
			factory.newSAXParser().parse(new FileInputStream(file),
					(DefaultHandler) handler);
			String schemaUrl = handler.getSchemaUrl();
			String namespaceURI = handler.getNameSpaceURI();
			return new QName(namespaceURI, schemaUrl);

		} catch (Exception e) {
			LOGGER.error(
					"Exception while trying to determining GML2 FeatureType schema.",
					e);
			throw new IllegalArgumentException(e);
		}
	}

}
