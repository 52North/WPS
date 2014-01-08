package org.n52.wps.io.test.datahandler.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.datahandler.generator.KMLGenerator;
import org.n52.wps.io.datahandler.parser.GTBinZippedSHPParser;
import org.n52.wps.io.datahandler.parser.KMLParser;
import org.n52.wps.io.test.datahandler.AbstractTestCase;

public class KMLGeneratorTest extends AbstractTestCase<KMLGenerator> {

	public void testGenerator(){
		
		if(!isDataHandlerActive()){
			return;
		}
		
		String testFilePath = projectRoot + "/52n-wps-io-impl/src/test/resources/states.zip";
		
		try {
			testFilePath = URLDecoder.decode(testFilePath, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			fail(e1.getMessage());
		}
		
		GTBinZippedSHPParser theParser = new GTBinZippedSHPParser();
		
		KMLParser kmlParser = new KMLParser();
		
		String[] mimetypes1 = theParser.getSupportedFormats();
		
		InputStream input = null;
		
		try {
			input = new FileInputStream(new File(testFilePath));
		} catch (FileNotFoundException e) {
			fail(e.getMessage());
		}
		
		String mimetype = mimetypes1[0];
		
		GTVectorDataBinding theBinding = theParser.parse(input, mimetype, "");
			
		assertNotNull(theBinding.getPayload());		
		assertTrue(!theBinding.getPayload().isEmpty());	
		
		String[] mimetypes2 = dataHandler.getSupportedFormats();
		String[] schemas2 = dataHandler.getSupportedSchemas();
		
		for (String string : mimetypes2) {
			
			for (String schema : schemas2) {
				try {
					InputStream in = dataHandler.generateStream(theBinding, string, schema);
					
					GTVectorDataBinding generatedParsedBinding = kmlParser.parse(in, kmlParser.getSupportedFormats()[0], kmlParser.getSupportedSchemas()[0]);
					
					assertNotNull(generatedParsedBinding.getPayload());	
					assertTrue(generatedParsedBinding.getPayloadAsShpFile().exists());		
					assertTrue(!generatedParsedBinding.getPayload().isEmpty());			
					
				} catch (IOException e) {
					e.printStackTrace();
					fail(e.getMessage());
				}
			}
			
		}
		
	}

	@Override
	protected void initializeDataHandler() {
		dataHandler = new KMLGenerator();
	}
	
}
