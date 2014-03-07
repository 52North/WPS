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


package org.n52.wps.io.datahandler.parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.factory.Hints;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.n52.wps.io.IOUtils;
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;

public class GeotiffZippedParser extends AbstractParser {
	
	private static Logger LOGGER = LoggerFactory.getLogger(GeotiffZippedParser.class);
	
	public GeotiffZippedParser() {
		super();
		supportedIDataTypes.add(GTRasterDataBinding.class);
	}
	
	@Override
	public GTRasterDataBinding parse(InputStream input, String mimeType, String schema) {
		//unzip
		File zippedFile;
		try {
			zippedFile = IOUtils.writeStreamToFile(input, "zip");
			finalizeFiles.add(zippedFile); // mark for final delete
		
			List<File> files = IOUtils.unzipAll(zippedFile);
			finalizeFiles.addAll(files); // mark for final delete
			
			for(File file : files){
				if(file.getName().toLowerCase().endsWith(".tif") || file.getName().toLowerCase().endsWith(".tiff")){
					return parseTiff(file);
				}
			}
			
		} catch (IOException e) {
			LOGGER.error("Exception while trying to unzip tiff.", e);
		}
		throw new RuntimeException("Could not parse zipped geotiff.");
	}
	
	private GTRasterDataBinding parseTiff(File file){
		Hints hints = new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER,
				Boolean.TRUE);
		GeoTiffReader reader;
		try {
			reader = new GeoTiffReader(file, hints);
			GridCoverage2D coverage = (GridCoverage2D) reader.read(null);
			return new GTRasterDataBinding(coverage);
		} catch (Exception e) {
			LOGGER.error("Exception while trying to create GTRasterDataBinding out of tiff.", e);
			throw new RuntimeException(e);
		} 
	}

}
