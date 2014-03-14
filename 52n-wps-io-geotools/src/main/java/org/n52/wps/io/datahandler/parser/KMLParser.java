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
package org.n52.wps.io.datahandler.parser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.kml.KMLConfiguration;
import org.geotools.xml.Configuration;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;


/**
 * This parser handles xml files compliant to gmlpacket.xsd 
 * @author schaeffer
 *
 */
public class KMLParser extends AbstractParser {
	
	public KMLParser() {
		super();
		supportedIDataTypes.add(GTVectorDataBinding.class);
	}
	
	public GTVectorDataBinding parse(InputStream stream, String mimeType, String schema) {
		
		FileOutputStream fos = null;
		try{
			File tempFile = File.createTempFile("kml", "tmp");
			finalizeFiles.add(tempFile); // mark for final delete
			fos = new FileOutputStream(tempFile);
			int i = stream.read();
			while(i != -1){
				fos.write(i);
				i = stream.read();
			}
			fos.flush();
			fos.close();
			GTVectorDataBinding data = parseXML(tempFile);
			return data;
		}
		catch(IOException e) {
			if (fos != null) try { fos.close(); } catch (Exception e1) { }
			throw new IllegalArgumentException("Error while creating tempFile", e);
		}
	}

	private GTVectorDataBinding parseXML(File file) {
		Configuration configuration = new KMLConfiguration();
		
		SimpleFeatureCollection fc = new GML3BasicParser().parseFeatureCollection(file, configuration, true);
		
		GTVectorDataBinding data = new GTVectorDataBinding(fc);
		
		return data;
	}

}
