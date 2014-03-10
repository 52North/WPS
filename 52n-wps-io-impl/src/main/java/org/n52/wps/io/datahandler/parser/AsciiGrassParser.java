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

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.gce.arcgrid.ArcGridReader;
import org.n52.wps.io.data.binding.complex.AsciiGrassDataBinding;
import org.opengis.coverage.grid.GridCoverageReader;

/**
 * @author Theodor Foerster, ITC
 *
 */
public class AsciiGrassParser extends AbstractParser {

	private static Logger LOGGER = LoggerFactory.getLogger(AsciiGrassParser.class);
	
	public AsciiGrassParser(){
		super();
		supportedIDataTypes.add(AsciiGrassDataBinding.class);
	}
	
	@Override
	public AsciiGrassDataBinding parse(InputStream input, String mimeType, String schema) {
		

		GridCoverage2D grid = null;
		
		try {			
			/** Step 1: Reading the coverage */

			GridCoverageReader reader = new ArcGridReader(dumpToFile(input));

			grid = (GridCoverage2D) reader.read(null);
				
			LOGGER.info("getCoordinateReferenceSystem2D(): " + grid.getCoordinateReferenceSystem2D().toString());
			LOGGER.info("getEnvelope():" + grid.getEnvelope2D().toString());
			
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
		return new AsciiGrassDataBinding(grid);
	}

	
	private File dumpToFile(InputStream inputStream)
			throws FileNotFoundException, IOException {

		BufferedInputStream bis = new BufferedInputStream(inputStream);

		File outputFile = File.createTempFile("temp" + inputStream.hashCode(), "tmp");

		BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));

		int _byte;
		while ((_byte = bis.read()) != -1) {
			bw.write(_byte);
		}

		bw.close();

		return outputFile;
	}

}
