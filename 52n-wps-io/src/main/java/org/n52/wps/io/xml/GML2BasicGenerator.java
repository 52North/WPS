/***************************************************************
Copyright � 2007 52�North Initiative for Geospatial Open Source Software GmbH

 Author: Theodor Foerster, ITC

 Contact: Andreas Wytzisk, 
 52�North Initiative for Geospatial Open Source SoftwareGmbH, 
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
 Software Foundation�s web page, http://www.fsf.org.

 ***************************************************************/
package org.n52.wps.io.xml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.n52.wps.PropertyDocument.Property;

import org.apache.log4j.Logger;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.gml.producer.FeatureTransformer;
import org.geotools.gml.producer.FeatureTransformer.FeatureTypeNamespaces;
import org.geotools.referencing.NamedIdentifier;
import org.geotools.xml.SchemaFactory;
import org.geotools.xml.schema.Schema;
import org.n52.wps.io.IStreamableGenerator;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class GML2BasicGenerator extends AbstractXMLGenerator implements IStreamableGenerator {
	private boolean featureTransformerIncludeBounding;
	private int featureTransformerDecimalPlaces;
	
	private static Logger LOGGER = Logger.getLogger(GML2BasicGenerator.class);
	private static String[] SUPPORTED_SCHEMAS = new String[]{
			"http://schemas.opengis.net/gml/2.1.2/feature.xsd"};
	
	public GML2BasicGenerator(){
		featureTransformerIncludeBounding = false;
		featureTransformerDecimalPlaces = 4;
		for(Property property : properties){
			if(property.getName().equalsIgnoreCase("featureTransformerIncludeBounding")){
				featureTransformerIncludeBounding = new Boolean(property.getStringValue());
				
			}
			if(property.getName().equalsIgnoreCase("featureTransformerDecimalPlaces")){
				featureTransformerDecimalPlaces = new Integer(property.getStringValue());
				
			}
			
		}
	}
	
	public void write(Object coll, Writer writer) {
		FeatureCollection fc = (FeatureCollection) coll;
		// this might be a workaround... 
		if(fc == null || fc.size() == 0) {
			try{
				writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
				writer.write("<wfs:FeatureCollection xmlns:wfs=\"http://www.opengis.net/wfs\" xmlns:gml=\"http://www.opengis.net/gml\"/>");
				writer.flush();
			}
			catch(IOException e) {
				new RuntimeException(e);
			}
			return;
		}
		Feature f = fc.features().next();
		FeatureType ft = f.getFeatureType();
		//String srsName = (String)f.getDefaultGeometry().getUserData();
	
		Object srs = f.getDefaultGeometry().getUserData();
		String srsName = null;
		if (srs instanceof String) {
		 srsName = (String) srs;
		}
		else if(srs instanceof CoordinateReferenceSystem) {
	     Iterator<NamedIdentifier> iter = ((CoordinateReferenceSystem)srs).getIdentifiers().iterator();
	            if(iter.hasNext()){
	                srsName= ((NamedIdentifier)iter.next()).toString();
	            }
		}
		
		
		FeatureTransformer tx = new FeatureTransformer();
		tx.setFeatureBounding(featureTransformerIncludeBounding);
		tx.setNumDecimals(featureTransformerDecimalPlaces);
	    FeatureTypeNamespaces ftNames = tx.getFeatureTypeNamespaces();
        // StringBuffer typeNames = new StringBuffer();
        
        Map ftNamespaces = new HashMap();

    	URI namespaceURI = ft.getNamespace();
        
        String uri = namespaceURI.toASCIIString();
        ftNames.declareNamespace(fc.getSchema(), fc.getSchema().getTypeName(), uri);
		
        if (ftNamespaces.containsKey(uri)) {
            String location = (String) ftNamespaces.get(uri);
            ftNamespaces.put(uri, location + "," + fc.getSchema().getTypeName());
        } else {
            ftNamespaces.put(uri,
                uri);
        }

        if(srsName != null) {
        	tx.setSrsName(srsName);
        }
        Schema s = SchemaFactory.getInstance(namespaceURI);
        tx.addSchemaLocation(ft.getNamespace().toASCIIString(),s.getURI().toASCIIString());
		tx.addSchemaLocation("http://www.opengis.net/wfs", "http://geoserver.itc.nl:8080/geoserver/schemas/wfs/1.0.0/WFS-basic.xsd");
		if(writer == null) {
			LOGGER.debug("writer is null");
		}
		if(fc == null) {
			LOGGER.debug("FeatureCollection is null");
		}
		try{
			tx.transform( fc, writer);
		}
		catch(TransformerException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns an array having the supported schemas.
	 */
	public String[] getSupportedSchemas() {
		return SUPPORTED_SCHEMAS;
	}

	/**
	 * Returns true if the given schema is supported, else false.
	 */
	public boolean isSupportedSchema(String schema) {
		for(String supportedSchema : SUPPORTED_SCHEMAS) {
			if(supportedSchema.equals(schema))
				return true;
		}
		return false;
	}

	public Node generateXML(Object coll, String schema) {
		File f = null;
		FileWriter writer = null;
		try {
			f = File.createTempFile("gml2", "xml");
			writer = new FileWriter(f);
			this.write(coll, writer);
			if(f.length() <= 0) {
				return null;
			}
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Node node = builder.parse(f);
			return node;
		}
		catch (IOException e){
			throw new RuntimeException(e);
		}
		catch(ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
		catch(SAXException e) {
			throw new RuntimeException(e);
		}
		finally {
			if (writer != null) try { writer.close(); } catch (Exception e) { }
			if (f != null) f.delete();
		}
		
	}

	public OutputStream generate(Object coll) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Writer writer = new OutputStreamWriter(baos);
		this.write(coll, writer);		
		return baos;
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
