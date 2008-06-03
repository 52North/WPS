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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import noNamespace.PropertyDocument.Property;

import org.apache.log4j.Logger;
import org.geotools.data.FeatureReader;
import org.geotools.feature.DefaultFeatureCollections;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.xml.SchemaFactory;
import org.geotools.xml.gml.FCBuffer;
import org.geotools.xml.gml.GMLComplexTypes;
import org.geotools.xml.schema.Element;
import org.geotools.xml.schema.Schema;
import org.n52.wps.io.IStreamableParser;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This parser handles xml files compliant to gmlpacket.xsd 
 * @author foerster
 *
 */
public class GML2BasicParser extends AbstractXMLParser implements IStreamableParser {
	private static Logger LOGGER = Logger.getLogger(GML2BasicParser.class);
	private static String SUPPORTED_SCHEMA = "http://schemas.opengis.net/gml/2.1.2/feature.xsd";	
	private int fcBufferTimeout;
	
	public GML2BasicParser() {
		//default
		fcBufferTimeout = 10000;
		
		for(Property property : properties){
			if(property.getName().equalsIgnoreCase("fcBufferTimeout")){
				fcBufferTimeout = new Integer(property.getStringValue());
				
			}
		}
		
	}

	public String[] getSupportedSchemas() {
		return new String[]{SUPPORTED_SCHEMA};
	}

	public FeatureCollection parseXML(String gml) {
		File f = null;
		FileOutputStream fos = null;
		try{
			f = File.createTempFile("wps", "tmp");
			fos = new FileOutputStream(f);
			if(gml.startsWith("<xml-fragment")) {
				gml = gml.replaceFirst("<xml-fragment .*?>", "");
				gml = gml.replaceFirst("</xml-fragment>", "");	
			}
			// TODO find a better solution. XML-beans hands in inappropriate XML, so the namespaces have to be set manually.
			gml = gml.replaceFirst("<wfs:FeatureCollection", "<wfs:FeatureCollection xmlns:xsi=\"" + XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI + "\"");
			StringReader sr = new StringReader(gml);
			int i = sr.read();
			while(i != -1){
				fos.write(i);
				i = sr.read();
			}
			//f.deleteOnExit();
			return parseXML(f.toURI());
		}
		catch(IOException e) {
			throw new IllegalArgumentException("Error while creating tempFile", e);
		}
		finally {
			if (fos != null) try { fos.close(); } catch (Exception e) { }
			if (f != null) f.delete();
		}
	}
	
	public FeatureCollection parseXML(InputStream stream) {
		File f = null;
		FileOutputStream fos = null;
		try{
			f = File.createTempFile("wps", "tmp");
			fos = new FileOutputStream(f);
			int i = stream.read();
			while(i != -1){
				fos.write(i);
				i = stream.read();
			}
			//f.deleteOnExit();
			return parseXML(f.toURI());
		}
		catch(IOException e) {
			throw new IllegalArgumentException("Error while creating tempFile", e);
		}
		finally {
			if (fos != null) try { fos.close(); } catch (Exception e) { }
			if (f != null) f.delete();
		}
	}	

	public boolean isSupportedSchema(String schema) {
		return SUPPORTED_SCHEMA.equals(schema);
	}
	
	public FeatureCollection parseXML(URI uri) {
		FeatureReader reader;
		FeatureCollection coll = null;
		URL featureTypeSchemaURL = null;
		try {
			featureTypeSchemaURL = new URL (determineFeatureTypeSchema(uri));
		}
		catch(MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
		if(featureTypeSchemaURL == null) {
			throw new NullPointerException("featureTypeSchema null for uri: " + uri.getQuery());
		}
		LOGGER.debug("determinedFeatureTypeURL: " + featureTypeSchemaURL);
		Schema schema = null;
		InputStream stream = null;
		try{
			stream = featureTypeSchemaURL.openStream();
		}
		catch(IOException e) {
			LOGGER.debug(e);
			LOGGER.debug(e.getStackTrace());
			throw new IllegalArgumentException(e);
		}
		try{
			schema = SchemaFactory.getInstance(null , stream);
		}
		catch(SAXException e) {
			throw new IllegalArgumentException(e);
		}
			
		Element[] elems = schema.getElements();
		try{			
			FeatureType tempType = GMLComplexTypes.createFeatureType(elems[0]);

			
			reader = FCBuffer.getFeatureReader(uri, 10, fcBufferTimeout, tempType);	


		} catch (SAXException e) {
e.printStackTrace();
			throw new IllegalArgumentException(e);
		} 
		
		if(reader != null) {
			coll = DefaultFeatureCollections.newCollection();
			try {
				while(reader.hasNext()) {
					coll.add(reader.next());
				}  
				// coll = (FeatureCollection)collType.create(features.toArray());
			}
			
			catch (IOException e) {
				LOGGER.debug(e);e.printStackTrace();
				throw new IllegalArgumentException(e);
			} catch (IllegalAttributeException e) {
				LOGGER.debug(e);
			}
		}
		return coll;
	}
	
	private String determineFeatureTypeSchema(URI uri) {
		try {
			GML2Handler handler = new GML2Handler();
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setNamespaceAware(true);
			factory.newSAXParser().parse(uri.toASCIIString(), (DefaultHandler)handler); 
			String schemaUrl = handler.getSchemaUrl(); 
			return schemaUrl;
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
		//return null;
	}

	public Object parse(InputStream input) {
		return parseXML(input);
	}

	public String[] getSupportedRootClasses() {
		return new String[]{FeatureCollection.class.getName()};
	}

	public boolean isSupportedEncoding(String encoding) {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean isSupportedRootClass(String clazzName) {
		// TODO Auto-generated method stub
		if(clazzName.equals(FeatureCollection.class.getName())) { 
			return true;
		}
		return false;
	}
	
	public static void main(String[] args){
		GML2BasicParser parser = new GML2BasicParser();
		try {
			long start = System.currentTimeMillis();
			parser.parse(new URL("http://geoserver.itc.nl:8080/geoserver/wfs?Request=GetFeature&typeName=topp:tasmania_roads").openStream());
			long finish = System.currentTimeMillis();
			System.out.println((finish-start)/1000.0);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	


}
