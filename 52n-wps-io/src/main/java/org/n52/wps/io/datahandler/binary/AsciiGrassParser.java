/***************************************************************
Copyright © 2007 52°North Initiative for Geospatial Open Source Software GmbH

 Author: Theodor Foerster, ITC

 Contact: Andreas Wytzisk, 
 52°North Initiative for Geospatial Open Source SoftwareGmbH, 
 Martin-Luther-King-Weg 24,
 48155 Muenster, Germany, 
 info@52north.org

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 version 2 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; even without the implied WARRANTY OF
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program (see gnu-gpl v2.txt). If not, write to
 the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA 02111-1307, USA or visit the Free
 Software Foundation’s web page, http://www.fsf.org.

 ***************************************************************/
package org.n52.wps.io.datahandler.binary;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.gce.arcgrid.ArcGridReader;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.io.data.binding.complex.AsciiGrassDataBinding;
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;
import org.opengis.coverage.grid.GridCoverageReader;

public class AsciiGrassParser extends AbstractBinaryParser {

	private static Logger LOGGER = Logger.getLogger(AsciiGrassParser.class);
	
	public AsciiGrassDataBinding parse(InputStream input, String mimeType) {

		GridCoverage2D grid = null;
		
		try {			
			/** Step 1: Reading the coverage */

			GridCoverageReader reader = new ArcGridReader(dumpToFile(input));

			grid = (GridCoverage2D) reader.read(null);
				
			LOGGER.info("getCoordinateReferenceSystem2D(): " + grid.getCoordinateReferenceSystem2D().toString());
			LOGGER.info("getEnvelope():" + grid.getEnvelope2D().toString());
			
		} catch (Exception e) {
			LOGGER.error(e);
			throw new RuntimeException(e);
		}
		return new AsciiGrassDataBinding(grid);
	}

	
	private File dumpToFile(InputStream inputStream)
			throws FileNotFoundException, IOException {

		BufferedInputStream bis = new BufferedInputStream(inputStream);

		File outputFile = File.createTempFile("temp" + inputStream.hashCode(),
				"tmp");

		BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));

		int _byte;
		while ((_byte = bis.read()) != -1) {
			bw.write(_byte);
		}

		bw.close();

		return outputFile;
	}

	public Class[] getSupportedInternalOutputDataType() {
		Class[] supportedClasses = {GTRasterDataBinding.class};
		return supportedClasses;	
	}

}
