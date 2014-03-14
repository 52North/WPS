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
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.wps.io.datahandler.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.kml.KML;
import org.geotools.kml.KMLConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.Encoder;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;

/**
 * @author Bastian Schaeffer, IfGI; Matthias Mueller, TU Dresden
 *
 */
public class KMLGenerator extends AbstractGenerator {
	
	private static Logger LOGGER = LoggerFactory.getLogger(KMLGenerator.class);
	
	public KMLGenerator(){
		super();
		supportedIDataTypes.add(GTVectorDataBinding.class);
	}
	
	@Override
	public InputStream generateStream(IData data, String mimeType, String schema) throws IOException {
		
		File tempFile = null;
		InputStream stream = null;
		try {
			tempFile = File.createTempFile("kml", "xml");
			this.finalizeFiles.add(tempFile);
			FileOutputStream outputStream = new FileOutputStream(tempFile);
			this.writeToStream(data, outputStream);
			outputStream.flush();
			outputStream.close();
			
			stream = new FileInputStream(tempFile);
		} catch (IOException e){
			LOGGER.error(e.getMessage());
			throw new IOException("Unable to generate KML");
		}
		
		return stream;
	}

	private void writeToStream(IData coll, OutputStream os) {
		FeatureCollection<?, ?> fc = ((GTVectorDataBinding)coll).getPayload();
		
        Configuration configuration = new KMLConfiguration();
        Encoder encoder = new org.geotools.xml.Encoder(configuration);
       
        try{
            encoder.encode(fc, KML.kml, os);
           
        }catch(IOException e){
        	throw new RuntimeException(e);
        }
	}

}
