package org.n52.wps.io.test.datahandler.generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;

import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.datahandler.generator.GML3BasicGenerator;
import org.n52.wps.io.datahandler.parser.GML3BasicParser;
import org.n52.wps.io.test.datahandler.AbstractTestCase;

public class GML3BasicGeneratorTest extends AbstractTestCase {

	public void testParser() {

		String testFilePath = projectRoot
				+ "/52n-wps-io/src/test/resources/spearfish_restricted_sites_gml3.xml";

		testFilePath = URLDecoder.decode(testFilePath);
		
		GML3BasicParser theParser = new GML3BasicParser();

//		String[] mimetypes = theParser.getSupportedFormats();

		InputStream input = null;

		try {
			input = new FileInputStream(new File(testFilePath));
		} catch (FileNotFoundException e) {
			fail(e.getMessage());
		}

		// for (String mimetype : mimetypes) {

		GTVectorDataBinding theBinding = theParser.parse(input,
				"text/xml; subtype=gml/3.2.1",
				"http://schemas.opengis.net/gml/3.2.1/base/feature.xsd");

		GML3BasicGenerator gml3Generator = new GML3BasicGenerator();
		
		try {
			InputStream resultStream = gml3Generator.generateStream(theBinding, "text/xml; subtype=gml/3.2.1", "http://schemas.opengis.net/gml/3.2.1/base/feature.xsd");
			
			GTVectorDataBinding parsedGeneratedBinding = theParser.parse(resultStream, "text/xml; subtype=gml/3.2.1", "http://schemas.opengis.net/gml/3.2.1/base/feature.xsd");
			
			assertNotNull(parsedGeneratedBinding.getPayload());
			assertTrue(parsedGeneratedBinding.getPayloadAsShpFile().exists());
			assertTrue(!parsedGeneratedBinding.getPayload().isEmpty());

			InputStream resultStreamBase64 = gml3Generator.generateBase64Stream(theBinding, "text/xml; subtype=gml/3.2.1", "http://schemas.opengis.net/gml/3.2.1/base/feature.xsd");
			
			GTVectorDataBinding parsedGeneratedBindingBase64 = (GTVectorDataBinding) theParser.parseBase64(resultStreamBase64, "text/xml; subtype=gml/3.2.1", "http://schemas.opengis.net/gml/3.2.1/base/feature.xsd");
			
			assertNotNull(parsedGeneratedBindingBase64.getPayload());
			assertTrue(parsedGeneratedBindingBase64.getPayloadAsShpFile().exists());
			assertTrue(!parsedGeneratedBindingBase64.getPayload().isEmpty());
			
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// }

	}

}
