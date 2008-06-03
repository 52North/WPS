/***************************************************************
 This implementation provides a framework to publish processes to the
web through the  OGC Web Processing Service interface. The framework 
is extensible in terms of processes and data handlers. It is compliant 
to the WPS version 0.4.0 (OGC 05-007r4). 

 Copyright (C) 2006 by con terra GmbH

 Authors: 
	Theodor Foerster, ITC, Enschede, the Netherlands
	Carsten Priess, Institute for geoinformatics, University of
Muenster, Germany
	Timon Ter Braak, University of Twente, the Netherlands


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

 Created on: 13.06.2006
 ***************************************************************/
package org.n52.wps.io.binary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.Writer;

import noNamespace.PropertyDocument.Property;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.gce.arcgrid.ArcGridWriter;
import org.n52.wps.io.IStreamableGenerator;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;

public class AsciiGrassGenerator extends AbstractBinaryGenerator implements IStreamableGenerator{

	private static Logger LOGGER = Logger.getLogger(AsciiGrassGenerator.class);
	private static String SUPPORTED_FORMAT = "application/image-ascii-grass";

	public synchronized OutputStream generate(Object o) {

		OutputStream outputStream = null;

		LOGGER.debug("111");
		
		if (o instanceof GridCoverage2D) {

			try {

				LOGGER.debug("112");
				
				final GridCoverage2D grid = (GridCoverage2D) o;
				
				LOGGER.debug("113");
				final File outputFile = File.createTempFile("temp"+grid.hashCode(), "tmp");

				LOGGER.debug("114");
				final GridCoverageWriter writer = new ArcGridWriter(outputFile);

				LOGGER.debug("115");
				// setting write parameters
				ParameterValueGroup params = writer.getFormat().getWriteParameters();
				LOGGER.debug("116");
				params.parameter("GRASS").setValue(true);
				LOGGER.debug("117");
				params.parameter("compressed").setValue(false);
				LOGGER.debug("118");
				GeneralParameterValue[] gpv = { params.parameter("GRASS"), params.parameter("compressed") };
				LOGGER.debug("119");
				writer.write(grid, gpv);
				LOGGER.debug("120");
				
				outputStream = new FileOutputStream(outputFile);
				
				LOGGER.debug(outputFile.getAbsolutePath());

			} catch (Exception e) {
				LOGGER.error(outputStream);
			}

		}

		return outputStream;
	}

	public String[] getSupportedRootClasses() {
		return new String[]{GridCoverage2D.class.getName()};
	}

	public String[] getSupportedSchemas() {
		return null;
	}

	public boolean isSupportedEncoding(String encoding) {
		return true;
	}

	public boolean isSupportedFormat(String format) {
		if(format.equals(SUPPORTED_FORMAT)) {
			return true;
		}
		return false;
	}

	public boolean isSupportedRootClass(String clazzName) {
		if(clazzName.equals(GridCoverage2D.class.getName())) {
			return true;
		}
		return false;
	}

	public String[] getSupportedFormats() {
		return new String[] { "application/image" };
	}

	public void write(Object o, Writer w) {
		OutputStream outputStream = null;
		if (o instanceof GridCoverage2D) {
			try {
				GridCoverage2D grid = (GridCoverage2D) o;
				String fileName = "temp"+grid.hashCode()+".tmp";
				File outputFile = new File(fileName);
				GridCoverageWriter writer = new ArcGridWriter(outputFile);
				// setting write parameters
				ParameterValueGroup params = writer.getFormat().getWriteParameters();
				params.parameter("GRASS").setValue(true);
				GeneralParameterValue[] gpv = { params.parameter("GRASS") };
				writer.write(grid, gpv);
				FileReader reader = new FileReader(fileName);
				LOGGER.debug(outputFile.getAbsolutePath());
				IOUtils.copy(reader, w);
				outputFile.delete();
			} catch (Exception e) {
				LOGGER.error(outputStream);
				throw new RuntimeException(e);
			}
		}
	}

	public void init(Property[] propertyArray) {
		// TODO Auto-generated method stub
		
	}

}
