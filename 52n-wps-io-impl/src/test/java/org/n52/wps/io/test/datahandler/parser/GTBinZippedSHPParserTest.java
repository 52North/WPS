package org.n52.wps.io.test.datahandler.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.datahandler.parser.GTBinZippedSHPParser;
import org.n52.wps.io.test.datahandler.AbstractTestCase;

public class GTBinZippedSHPParserTest extends AbstractTestCase<GTBinZippedSHPParser>{

	public void testParser(){	
		
		if(!isDataHandlerActive()){
			return;
		}
		
		String testFilePath = projectRoot + "/52n-wps-io-impl/src/test/resources/tasmania_roads.zip";
		
		try {
			testFilePath = URLDecoder.decode(testFilePath, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			fail(e1.getMessage());
		}
		
		String[] mimetypes = dataHandler.getSupportedFormats();
		
		InputStream input = null;
		
		
		
		for (String mimetype : mimetypes) {
			
			try {
				input = new FileInputStream(new File(testFilePath));
			} catch (FileNotFoundException e) {
				fail(e.getMessage());
			}
			
			GTVectorDataBinding theBinding = dataHandler.parse(input, mimetype, "");
			
			assertNotNull(theBinding.getPayload());
			assertTrue(theBinding.getPayloadAsShpFile().exists());			
			assertTrue(!theBinding.getPayload().isEmpty());			
		}
		
	}

	@Override
	protected void initializeDataHandler() {
		dataHandler = new GTBinZippedSHPParser();
	}
	
}
