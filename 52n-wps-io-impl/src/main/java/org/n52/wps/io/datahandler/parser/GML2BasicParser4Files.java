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
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;

/**
 * This parser handles xml files compliant to GML2.
 * @author schaeffer
 *
 */
public class GML2BasicParser4Files extends AbstractParser {
	private static Logger LOGGER = LoggerFactory.getLogger(GML2BasicParser4Files.class);

	
	public GML2BasicParser4Files() {
		super();
		supportedIDataTypes.add(GenericFileDataBinding.class);
	}
	
	public GenericFileDataBinding parse(InputStream stream, String mimeType, String schema) {
		
		FileOutputStream fos = null;
		try{
			File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".gml2");
			finalizeFiles.add(tempFile); // mark for final delete
			fos = new FileOutputStream(tempFile);
			int i = stream.read();
			while(i != -1){
				fos.write(i);
				i = stream.read();
			}
			fos.flush();
			fos.close();
			GenericFileDataBinding data = parseXML(tempFile);
			
			return data;
		}
		catch(IOException e) {
			if (fos != null) try { fos.close(); } catch (Exception e1) { }
			throw new IllegalArgumentException("Error while creating tempFile", e);
		}
	}

	private GenericFileDataBinding parseXML(File file) {
		
		SimpleFeatureCollection fc = new GML2BasicParser().parseSimpleFeatureCollection(file);
		
		GenericFileDataBinding data = null;
		try {
			data = new GenericFileDataBinding(new GenericFileData(fc));
		} catch (IOException e) {
			LOGGER.error("Exception while trying to wrap GenericFileData around GML2 FeatureCollection.", e);
		}
				
		return data;
	}

}
