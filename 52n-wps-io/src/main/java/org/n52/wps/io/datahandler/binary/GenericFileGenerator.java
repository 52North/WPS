/***************************************************************
Copyright © 2009 52°North Initiative for Geospatial Open Source Software GmbH

 Author: Matthias Mueller, TU Dresden

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.n52.wps.io.IStreamableGenerator;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.GenericFileDataConstants;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;

public class GenericFileGenerator extends AbstractBinaryGenerator implements IStreamableGenerator{
	
	private File tempfile;
	
	private static Logger LOGGER = Logger.getLogger(GenericFileGenerator.class);
	
	public boolean isSupportedEncoding(String encoding) {
		return true;
	}
	
	public boolean isSupportedSchema(String schema) {
		boolean ret = false;
		if(schema == null){
			ret = true;
		}
		else {
			if (schema.isEmpty()){
				ret = true;
			}
		}
		return ret;
	}

	

	public void writeToStream(IData outputData, OutputStream outputStream) {
		
		
		if(!(outputData instanceof GenericFileDataBinding)){
			throw new RuntimeException("GenericFileGenerator writer does not support incoming datatype");
		}
		LOGGER.info("Generating tempfile ...");
		InputStream theStream = ((GenericFileDataBinding)outputData).getPayload().dataStream;
		
		try {
			IOUtils.copy(theStream, outputStream);
			theStream.close();
			LOGGER.info("Tempfile generated!");
			System.gc();
		} catch (Exception e) {
			LOGGER.error(e);
			throw new RuntimeException(e);
		}
	}
	

	public OutputStream generate(IData data) {
		LargeBufferStream outputStream = new LargeBufferStream();

		
		if(!(data instanceof GenericFileDataBinding)){
			throw new RuntimeException("GenericFileGenerator does not support incoming datatype");
		}
		
		InputStream theStream = ((GenericFileDataBinding)data).getPayload().dataStream;
		
		try {
			IOUtils.copy(theStream, outputStream);
			theStream.close();
			System.gc();
		} catch (Exception e) {
			LOGGER.error(e);
			throw new RuntimeException(e);
		}
		return outputStream;
		
	}
	
	
	public File generateFile(IData data, String mimeType) {
		
		if(!(data instanceof GenericFileDataBinding)){
			throw new RuntimeException("GenericFileGenerator does not support incoming datatype");
		}
		
		GenericFileData currentFD = (GenericFileData)data.getPayload();
		
		//check for necessary conversions
		if (currentFD.mimeType != mimeType)
			data = new GenericFileDataBinding(convertFile(currentFD));
		
		//create a tempfile for the output
		tempfile = new File("tempFile"+System.currentTimeMillis()+".temp");
		try {
			OutputStream outputStream = new FileOutputStream(tempfile);
			writeToStream(data, outputStream);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tempfile;
	}
	
	
	public Class[] getSupportedInternalInputDataType() {
		Class[] supportedClasses = {GenericFileDataBinding.class};
		return supportedClasses;
	}
	
	protected void finalize(){
		if(tempfile.delete())
			LOGGER.info("Generator tempfile successfully deleted!");
		else
			LOGGER.info("Generator tempfile could not be deleted :-(");
		
		//just to be safe - call the destructor from the superior class
		try {
			super.finalize();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private GenericFileData convertFile (GenericFileData inputFile){
		//not implemented
		return null;
	}
	
}
