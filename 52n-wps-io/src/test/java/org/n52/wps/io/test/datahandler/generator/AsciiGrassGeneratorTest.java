package org.n52.wps.io.test.datahandler.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.n52.wps.io.data.binding.complex.AsciiGrassDataBinding;
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;
import org.n52.wps.io.datahandler.generator.AsciiGrassGenerator;
import org.n52.wps.io.datahandler.parser.AsciiGrassParser;
import org.n52.wps.io.datahandler.parser.GeotiffParser;
import org.n52.wps.io.test.datahandler.AbstractTestCase;

public class AsciiGrassGeneratorTest extends AbstractTestCase {

	public void testGenerator() {
		
		String testFilePath = projectRoot
				+ "/52n-wps-io/src/test/resources/6_UTM2GTIF.TIF";

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

		AsciiGrassGenerator generator = new AsciiGrassGenerator();
		String[] mimetypes2 = generator.getSupportedFormats();

		AsciiGrassParser asciiGrassParser = new AsciiGrassParser();
		
		for (String string : mimetypes2) {
			try {
				InputStream resultStream = generator.generateStream(theBinding, string, null);
				
				AsciiGrassDataBinding rasterBinding = asciiGrassParser.parse(resultStream, mimetypes[0], null);
				
				assertTrue(rasterBinding.getPayload() != null);
				assertTrue(rasterBinding.getPayload().getDimension() != 0);
				assertTrue(rasterBinding.getPayload().getEnvelope() != null);
				
				InputStream resultStreamBase64 = generator.generateBase64Stream(theBinding, string, null);
				
				AsciiGrassDataBinding rasterBindingBase64 = (AsciiGrassDataBinding) asciiGrassParser.parseBase64(resultStreamBase64, mimetypes[0], null);
				
				assertTrue(rasterBindingBase64.getPayload() != null);
				assertTrue(rasterBindingBase64.getPayload().getDimension() != 0);
				assertTrue(rasterBindingBase64.getPayload().getEnvelope() != null);
				
			} catch (IOException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}

		}

	}

	
}
