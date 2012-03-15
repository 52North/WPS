package org.n52.wps.io.test.datahandler.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;

import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;
import org.n52.wps.io.datahandler.generator.GeotiffGenerator;
import org.n52.wps.io.datahandler.parser.GeotiffParser;
import org.n52.wps.io.test.datahandler.AbstractTestCase;

public class GeotiffGeneratorTest extends AbstractTestCase {

	public void testGenerator() {

		String testFilePath = projectRoot
				+ "/52n-wps-io/src/test/resources/6_UTM2GTIF.TIF";
		
		testFilePath = URLDecoder.decode(testFilePath);

		GeotiffParser theParser = new GeotiffParser();

		String[] mimetypes = theParser.getSupportedFormats();

		InputStream input = null;

		try {
			input = new FileInputStream(new File(testFilePath));
		} catch (FileNotFoundException e) {
			fail(e.getMessage());
		}

		GTRasterDataBinding theBinding = theParser.parse(input, mimetypes[0],
				null);

		assertTrue(theBinding.getPayload() != null);

		GeotiffGenerator generator = new GeotiffGenerator();
		String[] mimetypes2 = generator.getSupportedFormats();

		for (String string : mimetypes2) {
			try {
				InputStream resultStream = generator.generateStream(theBinding, string, null);
				
				GTRasterDataBinding rasterBinding = theParser.parse(resultStream, mimetypes[0], null);
				
				assertTrue(rasterBinding.getPayload() != null);
				assertTrue(rasterBinding.getPayload().getDimension() != 0);
				assertTrue(rasterBinding.getPayload().getEnvelope() != null);
				
			} catch (IOException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}

		}

	}

	
}
