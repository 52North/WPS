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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.n52.wps.io.IStreamableGenerator;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.GenericFileDataConstants;
import org.n52.wps.io.data.binding.complex.ArrayDataBinding;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;

public class WCPSGenerator extends AbstractBinaryGenerator implements IStreamableGenerator{
	
	private File tempfile;
	
	private static Logger LOGGER = Logger.getLogger(WCPSGenerator.class);
	
	public boolean isSupportedSchema(String schema) {
		return true;
	}

	

	public void writeToStream(IData outputData, OutputStream outputStream) {
		
		
		if(!(outputData instanceof ArrayDataBinding)){
			if(outputData==null){
				throw new RuntimeException("GenericFileGenerator writer does not support incoming datatype.");
			}else{
				throw new RuntimeException("GenericFileGenerator writer does not support incoming datatype. Datatyüe is " + outputData.getClass());
			}
		}
		List<byte[]> wcpsoutput = ((ArrayDataBinding)outputData).getPayload();
		for(byte[] element : wcpsoutput){
			ByteArrayInputStream inStream = new ByteArrayInputStream(element);
			try {
				IOUtils.copy(inStream, outputStream);
				inStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
	
	}
	

	public OutputStream generate(IData data) {
		LargeBufferStream outputStream = new LargeBufferStream();

		
		writeToStream(data, outputStream);
		return outputStream;
		
	}
	
	
	public File generateFile(IData data, String mimeType) {
		
			OutputStream outputStream = null;
			try {
				outputStream = new FileOutputStream(tempfile);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			writeToStream(data, outputStream);
			return tempfile;
	}
	
	
	public Class[] getSupportedInternalInputDataType() {
		Class[] supportedClasses = {ArrayDataBinding.class};
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
