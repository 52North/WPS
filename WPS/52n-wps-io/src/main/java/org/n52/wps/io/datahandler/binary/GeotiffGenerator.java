/***************************************************************
 This implementation provides a framework to publish processes to the
web through the  OGC Web Processing Service interface. The framework 
is extensible in terms of processes and data handlers. It is compliant 
to the WPS version 0.4.0 (OGC 05-007r4). 

 Copyright (C) 2008 by con terra GmbH

 Authors: 
	Bastian Schaeffer, Institute for geoinformatics, University of Muenster, Germany


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

package org.n52.wps.io.datahandler.binary;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.media.jai.JAI;

import org.apache.log4j.Logger;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffWriteParams;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.n52.wps.io.IStreamableGenerator;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;

public class GeotiffGenerator  extends AbstractBinaryGenerator implements IStreamableGenerator{
	private static String[] SUPPORTED_FORMAT = {"application/tiff","image/tiff"};
	private static Logger LOGGER = Logger.getLogger(GeotiffGenerator.class);
	
	
	public String[] getSupportedFormats() {
		return SUPPORTED_FORMAT;
	}

	
	

	public boolean isSupportedEncoding(String encoding) {
		return true;
	}

	public boolean isSupportedFormat(String format) {
		for(String tempFormat : SUPPORTED_FORMAT){
			if(tempFormat.equals(format)){
				return true;
			}
		}
		return false;
	}

	public void writeToStream(IData raster, OutputStream outputStream) {
		if(!(raster instanceof GTRasterDataBinding)){
			throw new RuntimeException("Geotiff writer does not support incoming datatype");
		}
		GridCoverage coverage = ((GTRasterDataBinding)raster).getPayload();
		//BufferedOutputStream outPutStream = new BufferedOutputStream(new FileOutputStream(outputFile));
		GeoTiffWriter geoTiffWriter = null;
		try {
			geoTiffWriter = new GeoTiffWriter(outputStream);
			writeGeotiff(geoTiffWriter, coverage);
		} catch (IOException e) {
			LOGGER.error(e);
			throw new RuntimeException(e);
		}
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
			LOGGER.error(e1);
			throw new RuntimeException(e1);
		} catch (IndexOutOfBoundsException e2) {
			LOGGER.error(e2);
			throw new RuntimeException(e2);
		} catch (IOException e3) {
			LOGGER.error(e3);
			throw new RuntimeException(e3);
		}
	}

	public OutputStream generate(IData data) {
		LargeBufferStream outputStream = new LargeBufferStream();

		
		if(!(data instanceof GTRasterDataBinding)){
			throw new RuntimeException("ArcGridWriter  does not support incoming datatype");
		}
		
		writeToStream(data, outputStream);
		return outputStream;
		
	}
	
	
	
	@Override
	public File generateFile(IData data) {
		if(!(data instanceof GTRasterDataBinding)){
			throw new RuntimeException("ArcGridWriter  does not support incoming datatype");
		}
		File file = new File("tempFile"+System.currentTimeMillis()+".temp");
		try {
			OutputStream outputStream = new FileOutputStream(file);
			writeToStream(data, outputStream);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return file;
		
	}
	
	public Class[] getSupportedInternalInputDataType() {
		Class[] supportedClasses = {GTRasterDataBinding.class};
		return supportedClasses;
	}


}
