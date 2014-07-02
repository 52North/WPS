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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import javax.media.jai.JAI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffWriteParams;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;
import org.n52.wps.io.data.binding.complex.GeotiffBinding;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;

public class GeotiffGenerator  extends AbstractGenerator {
	private static Logger LOGGER = LoggerFactory.getLogger(GeotiffGenerator.class);
	
	public GeotiffGenerator() {
		super();
		supportedIDataTypes.add(GTRasterDataBinding.class);
		supportedIDataTypes.add(GeotiffBinding.class);
	}
	
	public InputStream generateStream(IData data, String mimeType, String schema) throws IOException {
		
//		// check for correct request before returning the stream
//		if (!(this.isSupportedGenerate(data.getSupportedClass(), mimeType, schema))){
//			throw new IOException("I don't support the incoming datatype");
//		}
		
		InputStream stream = null;
		
		if((data instanceof GTRasterDataBinding)){
			
			GridCoverage coverage = ((GTRasterDataBinding)data).getPayload();
			GeoTiffWriter geoTiffWriter = null;
			String tmpDirPath = System.getProperty("java.io.tmpdir");			
			String fileName = tmpDirPath + File.separatorChar + "temp" + UUID.randomUUID() + ".tmp";
			File outputFile = new File(fileName);
			this.finalizeFiles.add(outputFile); // mark file for final delete
			
			try {
				geoTiffWriter = new GeoTiffWriter(outputFile);
				writeGeotiff(geoTiffWriter, coverage);
				geoTiffWriter.dispose();
				stream = new FileInputStream(outputFile);
				
			} catch (IOException e) {
				LOGGER.error(e.getMessage());
				throw new IOException("Could not create output due to an IO error");
			}
		}
		if(data instanceof GeotiffBinding){
			File geotiff = ((GeotiffBinding)data).getPayload();
			try {
				stream = new FileInputStream(geotiff);
			} catch (FileNotFoundException e) {
				throw new IOException("Error while generating geotiff. Source file not found.");
			}
		}
		
		return stream;
	}
	
	private void writeGeotiff(GeoTiffWriter geoTiffWriter, GridCoverage coverage){
		GeoTiffFormat format = new GeoTiffFormat();
		
		GeoTiffWriteParams wp = new GeoTiffWriteParams();

	
		wp.setCompressionMode(GeoTiffWriteParams.MODE_EXPLICIT);
		wp.setCompressionType("LZW"); 
		wp.setTilingMode(GeoToolsWriteParams.MODE_EXPLICIT);
		int width = ((GridCoverage2D) coverage).getRenderedImage().getWidth();
		int tileWidth = 1024;
		if(width<2048){
			tileWidth = new Double(Math.sqrt(width)).intValue();
		}
		wp.setTiling(tileWidth, tileWidth);
		ParameterValueGroup paramWrite = format.getWriteParameters();
		paramWrite.parameter(AbstractGridFormat.GEOTOOLS_WRITE_PARAMS.getName().toString()).setValue(wp);
		JAI.getDefaultInstance().getTileCache().setMemoryCapacity(256*1024*1024);
		
		
		try {
			geoTiffWriter.write(coverage, (GeneralParameterValue[])paramWrite.values().toArray(new
					GeneralParameterValue[1]));
		} catch (IllegalArgumentException e1) {
			LOGGER.error(e1.getMessage(), e1);
			throw new RuntimeException(e1);
		} catch (IndexOutOfBoundsException e1) {
			LOGGER.error(e1.getMessage(), e1);
			throw new RuntimeException(e1);
		} catch (IOException e1) {LOGGER.error(e1.getMessage(), e1);
			throw new RuntimeException(e1);
		}
	}
	
}
