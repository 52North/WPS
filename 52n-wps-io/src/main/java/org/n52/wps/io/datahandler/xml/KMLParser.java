package org.n52.wps.io.datahandler.xml;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.geotools.data.DataUtilities;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import sapience.annotations.model.KeyValueProperty;
import sapience.features.Feature;
import sapience.features.streams.Streams;
import sapience.features.streams.kml.KMLStream;

import com.vividsolutions.jts.geom.Geometry;

public class KMLParser extends AbstractXMLParser {

	private static Logger LOGGER = Logger.getLogger(KMLParser.class);
	private int fcBufferTimeout;
	private static String SUPPORTED_SCHEMA = "http://localhost:8081/schemas/kmlschema.xsd";

	public KMLParser() {
		fcBufferTimeout = 1000000;
	}

	@Override
	public Class[] getSupportedInternalOutputDataType() {
		// TODO Auto-generated method stub
		Class[] supportedClasses = { GTVectorDataBinding.class };
		return supportedClasses;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IData parse(InputStream input) {
		GTVectorDataBinding kmlBinding = null;
		Streams kml = new KMLStream();

		try {
			Collection<Feature> list = kml.read(input)
					.listFeaturesRecursively();

			kmlBinding = createGTVectorBinding(list);

		}

		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (kmlBinding != null)
			return kmlBinding;
		else
			return null;
	}

	private GTVectorDataBinding createGTVectorBinding(Collection<Feature> list) {
		GTVectorDataBinding kmlBinding = null;
		String annotations = "";
		FeatureCollection features = FeatureCollections.newCollection();
		int counter = 0;
		for (Feature f : list) {
			
			Object[] attributes = f.listAnnotations().toArray();
			int size = attributes.length;
			ArrayList<String> stringKeys=new ArrayList<String>();
				for (int i = 0; i < size; i++) {
					if (attributes[i].getClass().equals(KeyValueProperty.class)) {
						KeyValueProperty keyValue = (KeyValueProperty) attributes[i];
						
						String key=keyValue.getKey();
						if(stringKeys.contains(key))
						key+="_";
						stringKeys.add(key);
						annotations += key
								+ ":"
								+ keyValue.getValue().getClass()
										.getSimpleName() + ",";
					} else {
						annotations += attributes[i].getClass().getSimpleName()
								+ ":"
								+ attributes[i].getClass().getSimpleName()
										.getClass().getSimpleName() + ",";
					}
				}
			

			Geometry geom = f.getGeometry();
			annotations += "Location:" + geom.getClass().getSimpleName() + ","
					+ "ID:" + int.class.getSimpleName();
			Object[] attributes2 = new Object[size + 2];
			for (int i = 0; i < size; i++) {
				attributes2[i] = attributes[i];
			}
			attributes2[size] = geom;
			attributes2[size + 1] = counter;
			FeatureType type = null;
			try {
				type = DataUtilities.createType("ID", annotations);
			} catch (SchemaException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				org.geotools.feature.Feature feature = type.create(attributes2,
						"Flag." + Integer.toString(counter));
				features.add(feature);
				System.out.println(feature.toString());

			} catch (IllegalAttributeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			counter += 1;
			annotations = "";

		}
		kmlBinding = new GTVectorDataBinding(features);

		return kmlBinding;
	}

	private String determineFeatureTypeSchema(URI uri) {
		try {
			GML2Handler handler = new GML2Handler();
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setNamespaceAware(true);
			factory.newSAXParser().parse(uri.toASCIIString(),
					(DefaultHandler) handler);
			String schemaUrl = handler.getSchemaUrl();
			return schemaUrl;
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		} catch (SAXException e) {
			throw new IllegalArgumentException(e);
		} catch (ParserConfigurationException e) {
			throw new IllegalArgumentException(e);
		}
		// return null;
	}

	@SuppressWarnings("unchecked")
	public GTVectorDataBinding parseXML(URI uri) {
		KMLStream kml = new KMLStream();
		GTVectorDataBinding kmlBinding = null;
		URL featureTypeSchemaURL = null;
		FeatureCollection features = null;
		try {
			featureTypeSchemaURL = new URL(determineFeatureTypeSchema(uri));
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
		if (featureTypeSchemaURL == null) {
			throw new NullPointerException("featureTypeSchema null for uri: "
					+ uri.getQuery());
		}
		LOGGER.debug("determinedFeatureTypeURL: " + featureTypeSchemaURL);
		try {
			Collection<Feature> coll = kml.read(
					featureTypeSchemaURL.openStream())
					.listFeaturesRecursively();
			Iterator<Feature> iter = coll.iterator();
			while (iter.hasNext()) {

				Feature feature = iter.next();
				features.add(feature);

			}
			kmlBinding = new GTVectorDataBinding(features);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return kmlBinding;

	}

	@Override
	public String[] getSupportedSchemas() {
		return new String[] { SUPPORTED_SCHEMA };
	}

	@Override
	public boolean isSupportedSchema(String schema) {
		return SUPPORTED_SCHEMA.equals(schema);
	}

	@Override
	public boolean supportsSchemas() {
		if (SUPPORTED_SCHEMA.isEmpty() == true)
			return false;
		else
			return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IData parseXML(String kml) {

		GTVectorDataBinding kmlBinding = null;
		FeatureCollection features = null;
		try {

			KMLStream kmlParser = new KMLStream();
			Collection<Feature> coll = kmlParser.read(
					new ByteArrayInputStream(kml.getBytes("UTF-8")))
					.listFeaturesRecursively();

			Iterator<Feature> iter = coll.iterator();
			while (iter.hasNext()) {

				Feature feature = iter.next();
				features.add(feature);

			}
			kmlBinding = new GTVectorDataBinding(features);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return kmlBinding;
	}

	@Override
	public IData parseXML(InputStream stream) {
		parse(stream);
		return null;
	}

	@Override
	public boolean isSupportedEncoding(String encoding) {
		// TODO Auto-generated method stub
		return true;
	}

	public static void main(String[] args) {
		KMLParser parser = new KMLParser();
		FileInputStream is = null;
		try {
			is = new FileInputStream(
					"C:/Program Files/Apache Software Foundation/apache-tomcat-5.5.27/webapps/data/adressen.kml");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		parser.parseXML(is);
	}

}