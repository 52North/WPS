package org.n52.wps.io.test.datahandler.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;
import org.n52.wps.io.datahandler.parser.GeotiffParser;
import org.n52.wps.io.test.datahandler.AbstractTestCase;

public class GeotiffParserTest extends AbstractTestCase {


	public void testParser(){	
		
		String testFilePath = projectRoot + "/52n-wps-io/src/test/resources/6_UTM2GTIF.TIF";
		
		GeotiffParser theParser = new GeotiffParser();
		
		String[] mimetypes = theParser.getSupportedFormats();
		
		InputStream input = null;
		
		for (String mimetype : mimetypes) {
			
			try {
				input = new FileInputStream(new File(testFilePath));
			} catch (FileNotFoundException e) {
				fail(e.getMessage());
			}
			
			GTRasterDataBinding theBinding = theParser.parse(input, mimetype, null);
			
			assertTrue(theBinding.getPayload() != null);			
			
		}
		
	}
	
}
