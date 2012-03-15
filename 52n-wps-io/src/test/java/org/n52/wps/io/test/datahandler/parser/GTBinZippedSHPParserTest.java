package org.n52.wps.io.test.datahandler.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URLDecoder;

import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.datahandler.parser.GTBinZippedSHPParser;
import org.n52.wps.io.test.datahandler.AbstractTestCase;

public class GTBinZippedSHPParserTest extends AbstractTestCase {

	public void testParser(){	
		
		String testFilePath = projectRoot + "/52n-wps-io/src/test/resources/tasmania_roads.zip";
		
		testFilePath = URLDecoder.decode(testFilePath);
		
		GTBinZippedSHPParser theParser = new GTBinZippedSHPParser();
		
		String[] mimetypes = theParser.getSupportedFormats();
		
		InputStream input = null;
		
		try {
			input = new FileInputStream(new File(testFilePath));
		} catch (FileNotFoundException e) {
			fail(e.getMessage());
		}
		
		for (String mimetype : mimetypes) {
			
			GTVectorDataBinding theBinding = theParser.parse(input, mimetype, "");
			
			assertNotNull(theBinding.getPayload());
			assertTrue(theBinding.getPayloadAsShpFile().exists());			
			assertTrue(!theBinding.getPayload().isEmpty());			
		}
		
	}
	
}
