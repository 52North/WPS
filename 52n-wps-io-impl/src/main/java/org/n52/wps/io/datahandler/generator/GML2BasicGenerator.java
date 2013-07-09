/***************************************************************
Copyright © 2011 52°North Initiative for Geospatial Open Source Software GmbH

 Author: Theodor Foerster, ITC; Matthias Mueller, TU Dresden

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

package org.n52.wps.io.datahandler.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.gml.producer.FeatureTransformer;
import org.geotools.gml.producer.FeatureTransformer.FeatureTypeNamespaces;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.io.SchemaRepository;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class GML2BasicGenerator extends AbstractGenerator {
	
	private boolean featureTransformerIncludeBounding;
	private int featureTransformerDecimalPlaces;
	
	private static Logger LOGGER = LoggerFactory.getLogger(GML2BasicGenerator.class);
		
	
	public GML2BasicGenerator(){
		super();
		supportedIDataTypes.add(GTVectorDataBinding.class);
		
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
	
	private void write(IData data, Writer writer) throws IOException {
		FeatureCollection<?,?> fc = ((GTVectorDataBinding)data).getPayload();
		// this might be a workaround... 
		if(fc == null || fc.size() == 0) {
			writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
			writer.write("<wfs:FeatureCollection xmlns:wfs=\"http://www.opengis.net/wfs\" xmlns:gml=\"http://www.opengis.net/gml\"/>");
			writer.flush();
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
		ftNames.declareNamespace(fc.getSchema(), fc.getSchema().getName().getLocalPart(), uri);

        if (ftNamespaces.containsKey(uri)) {
            String location = (String) ftNamespaces.get(uri);
			ftNamespaces.put(uri, location + "," + fc.getSchema().getName().getLocalPart());
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
		
		try{
			tx.transform( fc, writer);
			writer.close();
		}
		catch(TransformerException e) {
			LOGGER.error(e.getMessage());
			throw new IOException("Unable to generate GML");
		}
	}
	
	@Override
	public InputStream generateStream(IData data, String mimeType, String schema) throws IOException {
		
		File tempFile = File.createTempFile("gml2", "xml");
		finalizeFiles.add(tempFile);
		FileWriter fw = new FileWriter(tempFile);
		write(data, fw);
		fw.close();
		InputStream is = new FileInputStream(tempFile);
		return is;
		
	}

}
