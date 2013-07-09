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

package org.n52.wps.io.datahandler.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

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
