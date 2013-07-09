/***************************************************************
Copyright © 2011 52°North Initiative for Geospatial Open Source Software GmbH

 Author: Bastian Schaeffer; Matthias Mueller, TU Dresden

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

package org.n52.wps.io.datahandler.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.ArrayDataBinding;

public class WCPSGenerator extends AbstractGenerator {
	
	private static Logger LOGGER = LoggerFactory.getLogger(WCPSGenerator.class);

	public WCPSGenerator(){
		super();
		supportedIDataTypes.add(ArrayDataBinding.class);
	}
	
	@Override
	public InputStream generateStream(IData data, String mimeType, String schema) throws IOException {

//		// check for correct request before returning the stream
//		if (!(this.isSupportedGenerate(data.getSupportedClass(), mimeType, schema))){
//			throw new IOException("I don't support the incoming datatype");
//		}
		
		List<byte[]> wcpsoutput = ((ArrayDataBinding)data).getPayload();
		
		File tempFile = File.createTempFile("wcps", ".bin");
		this.finalizeFiles.add(tempFile);
		FileOutputStream fos = new FileOutputStream(tempFile);
		
		for (byte[] currentArray : wcpsoutput){
			fos.write(currentArray);
		}
		
		fos.flush();
		fos.close();
		
		InputStream stream = new FileInputStream(tempFile);
		
		return stream;
	}

	public void writeToStream(IData outputData, OutputStream outputStream) {
		
		
	}
	
}
