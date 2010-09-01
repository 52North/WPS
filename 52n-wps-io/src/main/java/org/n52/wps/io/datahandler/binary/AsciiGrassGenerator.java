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
package org.n52.wps.io.datahandler.binary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.DataSourceException;
import org.geotools.gce.arcgrid.ArcGridWriter;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.io.IStreamableGenerator;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;

public class AsciiGrassGenerator extends AbstractBinaryGenerator implements IStreamableGenerator{

	private static Logger LOGGER = Logger.getLogger(AsciiGrassGenerator.class);
	
	public OutputStream generate(IData o) {

		OutputStream outputStream = null;

				
		if (o instanceof GTRasterDataBinding) {

			try {
				final GridCoverage2D grid = ((GTRasterDataBinding) o).getPayload();
				final File outputFile = File.createTempFile("temp"+grid.hashCode(), "tmp");
				final GridCoverageWriter writer = new ArcGridWriter(outputFile);

				// setting write parameters
				ParameterValueGroup params = writer.getFormat().getWriteParameters();
				params.parameter("GRASS").setValue(true);
				params.parameter("compressed").setValue(false);
				GeneralParameterValue[] gpv = { params.parameter("GRASS"), params.parameter("compressed") };
				writer.write(grid, gpv);
			
				
				outputStream = new FileOutputStream(outputFile);
				
				LOGGER.debug(outputFile.getAbsolutePath());

			} catch (Exception e) {
				LOGGER.error(e);
			}

		}

		return outputStream;
	}

	public void write(IData o, Writer w) {
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
				LOGGER.error(e);
				throw new RuntimeException(e);
			}
		}
	}

	public void init(Property[] propertyArray) {
		// TODO Auto-generated method stub
		
	}

	public void writeToStream(IData data, OutputStream os) {
		if(!(data instanceof GTRasterDataBinding)){
			throw new RuntimeException("ArcGridWriter  does not support incoming datatype");
		}
		GridCoverage2D grid = ((GTRasterDataBinding) data).getPayload();
		GridCoverageWriter writer;
		try {
			writer = new ArcGridWriter(os);
		} catch (DataSourceException e) {
			LOGGER.error(e);
			throw new RuntimeException(e);
		}
		// setting write parameters
		ParameterValueGroup params = writer.getFormat().getWriteParameters();
		params.parameter("GRASS").setValue(true);
		GeneralParameterValue[] gpv = { params.parameter("GRASS") };
		try {
			writer.write(grid, gpv);
		} catch (IllegalArgumentException e) {
			LOGGER.error(e);
			throw new RuntimeException(e);
		} catch (IOException e) {
			LOGGER.error(e);
			throw new RuntimeException(e);
		}	
	}

	public Class[] getSupportedInternalInputDataType() {
		Class[] supportedClasses = {GTRasterDataBinding.class};
		return supportedClasses;
	}



	@Override
	public File generateFile(IData data, String mimeType) {
		if(!(data instanceof GTRasterDataBinding)){
			throw new RuntimeException("ArcGridWriter  does not support incoming datatype");
		}
		GridCoverage2D grid = ((GTRasterDataBinding) data).getPayload();
		String fileName = "temp"+System.currentTimeMillis()+".tmp";
		File outputFile = new File(fileName);
		GridCoverageWriter writer;
		try {
			writer = new ArcGridWriter(outputFile);
		
		// setting write parameters
			ParameterValueGroup params = writer.getFormat().getWriteParameters();
			params.parameter("GRASS").setValue(true);
			GeneralParameterValue[] gpv = { params.parameter("GRASS") };
			writer.write(grid, gpv);
			return outputFile;
		} catch (DataSourceException e) {
			LOGGER.error(e);
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			LOGGER.error(e);
			throw new RuntimeException(e);
		} catch (IOException e) {
			LOGGER.error(e);
			throw new RuntimeException(e);
		}
		
	}

}
