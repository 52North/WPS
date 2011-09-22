package org.n52.wps.io.datahandler.binary;

import java.io.InputStream;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.log4j.Logger;
import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;

public class GenericFileParserBase64 extends AbstractBinaryParser{
	private static Logger LOGGER = Logger.getLogger(GenericFileParserBase64.class);
	
	
	public Class[] getSupportedInternalOutputDataType() {
		Class[] supportedClasses = {GenericFileDataBinding.class};
		return supportedClasses;
	
	}
	
	
	public GenericFileDataBinding parse(InputStream input, String mimeType) {
		
		Base64InputStream baseIS = new Base64InputStream(input);
		
		GenericFileData theData = new GenericFileData(baseIS, mimeType);
		LOGGER.info("Found File Input " + mimeType);
		
		return new GenericFileDataBinding(theData);
	}
}
