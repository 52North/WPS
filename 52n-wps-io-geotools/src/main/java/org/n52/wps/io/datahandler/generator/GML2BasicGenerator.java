/**
 * ﻿Copyright (C) 2007 - 2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *       • Apache License, version 2.0
 *       • Apache Software License, version 1.0
 *       • GNU Lesser General Public License, version 3
 *       • Mozilla Public License, versions 1.0, 1.1 and 2.0
 *       • Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * As an exception to the terms of the GPL, you may copy, modify,
 * propagate, and distribute a work formed by combining 52°North WPS
 * GeoTools Modules with the Eclipse Libraries, or a work derivative of
 * such a combination, even if such copying, modification, propagation, or
 * distribution would otherwise violate the terms of the GPL. Nothing in
 * this exception exempts you from complying with the GPL in all respects
 * for all of the code used other than the Eclipse Libraries. You may
 * include this exception and its grant of permissions when you distribute
 * 52°North WPS GeoTools Modules. Inclusion of this notice with such a
 * distribution constitutes a grant of such permissions. If you do not wish
 * to grant these permissions, remove this paragraph from your
 * distribution. "52°North WPS GeoTools Modules" means the 52°North WPS
 * modules using GeoTools functionality - software licensed under version 2
 * or any later version of the GPL, or a work based on such software and
 * licensed under the GPL. "Eclipse Libraries" means Eclipse Modeling
 * Framework Project and XML Schema Definition software distributed by the
 * Eclipse Foundation and licensed under the Eclipse Public License Version
 * 1.0 ("EPL"), or a work based on such software and licensed under the EPL.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
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

import org.geotools.feature.FeatureCollection;
import org.geotools.gml.producer.FeatureTransformer;
import org.geotools.gml.producer.FeatureTransformer.FeatureTypeNamespaces;
import org.n52.wps.io.SchemaRepository;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.webapp.api.types.ConfigurationEntry;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Theodor Foerster, ITC; Matthias Mueller, TU Dresden
 *
 */
public class GML2BasicGenerator extends AbstractGenerator {
	
	private boolean featureTransformerIncludeBounding;
	private int featureTransformerDecimalPlaces;
	
	private static Logger LOGGER = LoggerFactory.getLogger(GML2BasicGenerator.class);		
	
	public GML2BasicGenerator(){
		super();
		supportedIDataTypes.add(GTVectorDataBinding.class);
		
		featureTransformerIncludeBounding = false;
		featureTransformerDecimalPlaces = 4;
		for(ConfigurationEntry<?> property : properties){
			if(property.getKey().equalsIgnoreCase("featureTransformerIncludeBounding")){
				featureTransformerIncludeBounding = new Boolean(property.getValue().toString());
				
			}
			if(property.getKey().equalsIgnoreCase("featureTransformerDecimalPlaces")){
				featureTransformerDecimalPlaces = new Integer(property.getValue().toString());
				
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
