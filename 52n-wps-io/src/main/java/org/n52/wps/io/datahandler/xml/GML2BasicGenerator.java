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
package org.n52.wps.io.datahandler.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.eclipse.xsd.util.XSDSchemaBuildingTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.gml.producer.FeatureTransformer;
import org.geotools.gml.producer.FeatureTransformer.FeatureTypeNamespaces;
import org.geotools.gml2.GMLConfiguration;
import org.geotools.gml3.ApplicationSchemaConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.Encoder;
import org.geotools.xml.SchemaFactory;
import org.geotools.xml.schema.Schema;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.io.IStreamableGenerator;
import org.n52.wps.io.SchemaRepository;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.datahandler.binary.LargeBufferStream;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.FeatureType;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class GML2BasicGenerator extends AbstractXMLGenerator implements IStreamableGenerator {
	private boolean featureTransformerIncludeBounding;
	private int featureTransformerDecimalPlaces;
	
	private static Logger LOGGER = Logger.getLogger(GML2BasicGenerator.class);
		
	
	public GML2BasicGenerator(){
		super();
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
	
	public GML2BasicGenerator(boolean pReadWPSConfig){
		super(pReadWPSConfig);
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
	
	public void write(IData coll, Writer writer) {
		FeatureCollection fc = ((GTVectorDataBinding)coll).getPayload();
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
		FeatureType ft = f.getType();
		//String srsName = (String)f.getDefaultGeometry().getUserData();
	
		Map<Object, Object> userData = f.getUserData();
		Object srs = userData.get("srs");
		String srsName = null;
		if (srs instanceof String) {
		 srsName = (String) srs;
		}
		else if(srs instanceof CoordinateReferenceSystem) {
	     Iterator<ReferenceIdentifier> iter = ((CoordinateReferenceSystem)srs).getIdentifiers().iterator();
	            if(iter.hasNext()){
	                srsName= iter.next().toString();
	            }
		}
		
		
		FeatureTransformer tx = new FeatureTransformer();
		tx.setFeatureBounding(featureTransformerIncludeBounding);
		tx.setNumDecimals(featureTransformerDecimalPlaces);
	    FeatureTypeNamespaces ftNames = tx.getFeatureTypeNamespaces();
        // StringBuffer typeNames = new StringBuffer();
        
		Map<String, String> ftNamespaces = new HashMap<String, String>();

		String uri = ft.getName().getNamespaceURI();
		ftNames.declareNamespace(fc.getSchema(), fc.getSchema().getName()
				.getLocalPart(), uri);

        if (ftNamespaces.containsKey(uri)) {
            String location = (String) ftNamespaces.get(uri);
			ftNamespaces.put(uri, location + ","
					+ fc.getSchema().getName().getLocalPart());
        } else {
            ftNamespaces.put(uri, uri);
        }

        if(srsName != null) {
        	tx.setSrsName(srsName);
        }
       
        
        String namespace = f.getType().getName().getNamespaceURI();
        String schemaLocation = SchemaRepository.getSchemaLocation(namespace);
        
        tx.addSchemaLocation(uri,schemaLocation);
		tx.addSchemaLocation("http://www.opengis.net/wfs", "http://schemas.opengis.net/wfs/1.0.0/WFS-basic.xsd");
		if(writer == null) {
			LOGGER.debug("writer is null");
		}
		if(fc == null) {
			LOGGER.debug("FeatureCollection is null");
		}
		try{
			tx.transform( fc, writer);
			writer.close();
		}
		catch(TransformerException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	

	

	public Node generateXML(IData coll, String schema) {
		File f = null;
		FileWriter writer = null;
		try {
			f = File.createTempFile("gml2", "xml");
			FileOutputStream outputStream = new FileOutputStream(f);
			this.writeToStream(coll, outputStream);
			outputStream.flush();
			outputStream.close();
			if(f.length() <= 0) {
				return null;
			}
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Node node = builder.parse(f);
			if (f != null) f.delete();
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

	public OutputStream generate(IData coll) {
		LargeBufferStream baos = new LargeBufferStream();
		this.writeToStream(coll, baos);		
		return baos;
	}

	

	public void writeToStream(IData coll, OutputStream os) {
	
		OutputStreamWriter w = new OutputStreamWriter(os);
		write (coll, w);		
		
		
	}

	public Class[] getSupportedInternalInputDataType() {
		Class[] supportedClasses = {GTVectorDataBinding.class};
		return supportedClasses;
	
	}


}
