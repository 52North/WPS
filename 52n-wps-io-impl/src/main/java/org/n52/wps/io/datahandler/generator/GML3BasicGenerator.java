/***************************************************************
Copyright (C) 2007 52 North Initiative for Geospatial Open Source Software GmbH

 Author: Bastian Schaeffer, IfGI; Matthias Mueller, TU Dresden

 Contact: Andreas Wytzisk, 
 52 North Initiative for Geospatial Open Source SoftwareGmbH, 
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
 Software Foundations web page, http://www.fsf.org.

 ***************************************************************/

package org.n52.wps.io.datahandler.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.gml3.ApplicationSchemaConfiguration;
import org.geotools.gml3.GMLConfiguration;
import org.geotools.xml.Configuration;
import org.n52.wps.io.GTHelper;
import org.n52.wps.io.SchemaRepository;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;

import com.vividsolutions.jts.geom.Geometry;

public class GML3BasicGenerator extends AbstractGenerator {
	
	private static Logger LOGGER = LoggerFactory.getLogger(GML3BasicGenerator.class);
		
	public GML3BasicGenerator(){
		super();
		supportedIDataTypes.add(GTVectorDataBinding.class);
	}
	
	public void writeToStream(IData coll, OutputStream os) {
		FeatureCollection<?,?> fc = ((GTVectorDataBinding)coll).getPayload();
		
		FeatureCollection<?,?> correctFeatureCollection = createCorrectFeatureCollection(fc);
		//get the namespace from the features to pass into the encoder        
        FeatureType schema = correctFeatureCollection.getSchema();
        String namespace = null;
        String schemaLocation = null;
        if(schema !=null){
        	namespace = schema.getName().getNamespaceURI();
        	schemaLocation = SchemaRepository.getSchemaLocation(namespace);
        }
       
        Configuration configuration = null;
        org.geotools.xml.Encoder encoder = null;
        if(schemaLocation==null || namespace==null){
        	namespace = "http://www.opengis.net/gml";
        	schemaLocation = "http://schemas.opengis.net/gml/3.1.1/base/feature.xsd";
        	configuration = new GMLConfiguration();//new ApplicationSchemaConfiguration(namespace, schemaLocation);
            
            encoder = new org.geotools.xml.Encoder(configuration );
            encoder.setNamespaceAware(true);
            encoder.setSchemaLocation("http://www.opengis.net/gml", "http://schemas.opengis.net/gml/3.1.1/base/feature.xsd");
           
        }else{
        	
        	configuration = new ApplicationSchemaConfiguration(namespace, schemaLocation);
        	    
            encoder = new org.geotools.xml.Encoder(configuration );
            encoder.setNamespaceAware(true);
            encoder.setSchemaLocation("http://www.opengis.net/gml http://schemas.opengis.net/gml/3.1.1/base/feature.xsd", namespace + " " + schemaLocation);
                      
        }
        	
        fc.features().close();
        //use the gml namespace with the FeatureCollection element to start parsing the collection
        QName ns = new QName("http://www.opengis.net/gml","FeatureCollection","wfs");
        try{
            encoder.encode(correctFeatureCollection, ns, os);           
        }catch(IOException e){
        	LOGGER.error("Exception while trying to encode FeatureCollection.", e);
        	throw new RuntimeException(e);
        }
		
	}

	@Override
	public InputStream generateStream(final IData data, String mimeType, String schema) throws IOException {
		String uuid = UUID.randomUUID().toString();
		File file = File.createTempFile("gml3"+uuid, ".xml");
		FileOutputStream outputStream = new FileOutputStream(file);
		this.writeToStream(data, outputStream);
		outputStream.flush();
		outputStream.close();
		if(file.length() <= 0) {
			return null;
		}
		FileInputStream inputStream = new FileInputStream(file);
		
		return inputStream;
		
	}

	private SimpleFeatureCollection createCorrectFeatureCollection(FeatureCollection<?,?> fc) {
		
		List<SimpleFeature> simpleFeatureList = new ArrayList<SimpleFeature>();
		SimpleFeatureType featureType = null;
		FeatureIterator<?> iterator = fc.features();
		String uuid = UUID.randomUUID().toString();
		int i = 0;
		while(iterator.hasNext()){
			SimpleFeature feature = (SimpleFeature) iterator.next();
		
			if(i==0){
				featureType = GTHelper.createFeatureType(feature.getProperties(), (Geometry)feature.getDefaultGeometry(), uuid, feature.getFeatureType().getCoordinateReferenceSystem());
				QName qname = GTHelper.createGML3SchemaForFeatureType(featureType);
				SchemaRepository.registerSchemaLocation(qname.getNamespaceURI(), qname.getLocalPart());
			}
			SimpleFeature resultFeature = GTHelper.createFeature("ID"+i, (Geometry)feature.getDefaultGeometry(), featureType, feature.getProperties());
		
			simpleFeatureList.add(resultFeature);
			i++;
		}
		iterator.close();
		
		ListFeatureCollection resultFeatureCollection = new ListFeatureCollection(featureType, simpleFeatureList);
		return resultFeatureCollection;
		
	}

}
