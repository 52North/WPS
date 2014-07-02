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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import javax.media.jai.JAI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.DataSourceException;
import org.geotools.factory.Hints;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;

public class GeotiffParser extends AbstractParser {
	
	private static Logger LOGGER = LoggerFactory.getLogger(GeotiffParser.class);

	public GeotiffParser() {
		super();
		supportedIDataTypes.add(GTRasterDataBinding.class);
	}
	
	
	@Override
	public GTRasterDataBinding parse(InputStream input, String mimeType, String schema) {
		
		File tempFile;
		
		try {
            tempFile = File.createTempFile("tempfile" + UUID.randomUUID(),"tmp");
            finalizeFiles.add(tempFile); // mark for final delete
			FileOutputStream outputStream = new FileOutputStream(tempFile);
			byte buf[] = new byte[4096];
			int len;
			while ((len = input.read(buf)) > 0) {
				outputStream.write(buf, 0, len);
			}
			
			outputStream.flush();
			outputStream.close();
			input.close();
		} catch (FileNotFoundException e) {
			LOGGER.error(e.getMessage(), e);
			throw new RuntimeException(e);
		} catch (IOException e1) {
			LOGGER.error(e1.getMessage(), e1);
			throw new RuntimeException(e1);
		}

		return parseTiff(tempFile);

	}
	
	private GTRasterDataBinding parseTiff(File file){
		JAI.getDefaultInstance().getTileCache().setMemoryCapacity(256*1024*1024);
		
		Hints hints = new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER,
				Boolean.TRUE);
		GeoTiffReader reader;
		try {
			reader = new GeoTiffReader(file, hints);
			GridCoverage2D coverage = (GridCoverage2D) reader.read(null);
			return new GTRasterDataBinding(coverage);
		} catch (DataSourceException e) {
			LOGGER.error(e.getMessage(), e);
			throw new RuntimeException(e);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

}
