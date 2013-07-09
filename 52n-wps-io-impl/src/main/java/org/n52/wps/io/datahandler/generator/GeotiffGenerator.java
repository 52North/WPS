/***************************************************************
 This implementation provides a framework to publish processes to the
web through the  OGC Web Processing Service interface. The framework 
is extensible in terms of processes and data handlers. It is compliant 
to the WPS version 0.4.0 (OGC 05-007r4). 

 Copyright (C) 2008 by con terra GmbH

 Authors: 
	Bastian Schaeffer, Institute for geoinformatics, University of Muenster, Germany
	Matthias Mueller, TU Dresden


 Contact: Albert Remke, con terra GmbH, Martin-Luther-King-Weg 24,
 48155 Muenster, Germany, 52n@conterra.de

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 version 2 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program (see gnu-gpl v2.txt); if not, write to
 the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA  02111-1307, USA or visit the web page of the Free
 Software Foundation, http://www.fsf.org.

 ***************************************************************/

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
