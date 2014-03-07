/**
 * ﻿Copyright (C) 2007
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */

package org.n52.wps.io.datahandler.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.DataSourceException;
import org.geotools.gce.arcgrid.ArcGridWriter;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;

public class AsciiGrassGenerator extends AbstractGenerator {

	private static Logger LOGGER = LoggerFactory.getLogger(AsciiGrassGenerator.class);
	
	public AsciiGrassGenerator() {
		super();
		this.supportedIDataTypes.add(GTRasterDataBinding.class);
	}
	
	public InputStream generateStream(IData data, String mimeType, String schema) throws IOException {
		
//		// check for correct request before returning the stream
//		if (!(this.isSupportedGenerate(data.getSupportedClass(), mimeType, schema))){
//			throw new IOException("I don't support the incoming datatype");
//		}
		
		InputStream stream = null;
		
		GridCoverage2D grid = ((GTRasterDataBinding) data).getPayload();
		String fileName = "temp" + UUID.randomUUID();
		File outputFile = File.createTempFile(fileName, ".tmp");
		outputFile.deleteOnExit();
		this.finalizeFiles.add(outputFile); // mark file for final delete
		GridCoverageWriter writer;
		try {
			writer = new ArcGridWriter(outputFile);
		
			// setting write parameters
			ParameterValueGroup params = writer.getFormat().getWriteParameters();
			params.parameter("GRASS").setValue(true);
			GeneralParameterValue[] gpv = { params.parameter("GRASS") };
			
			writer.write(grid, gpv);
			writer.dispose();
			
			stream = new FileInputStream(outputFile);
			
		} catch (DataSourceException e) {
			LOGGER.error(e.getMessage());
			throw new IOException("AsciiGRID cannot be read from source");
		} catch (IllegalArgumentException e) {
			LOGGER.error(e.getMessage());
			throw new IOException("Illegal configuration of AsciiGRID writer");
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			throw new IOException("AsciiGrassGenerator could not create output due to an IO error");
		}
		
		return stream;
	}

}
