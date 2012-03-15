package org.n52.wps.io.test.datahandler.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.datahandler.parser.GML3BasicParser;
import org.n52.wps.io.test.datahandler.AbstractTestCase;

public class GML3BasicParserTest extends AbstractTestCase {

	public void testParser() {

		String testFilePath = projectRoot
				+ "/52n-wps-io/src/test/resources/spearfish_restricted_sites_gml3.xml";

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

		assertNotNull(theBinding.getPayload());
		assertTrue(theBinding.getPayloadAsShpFile().exists());
		assertTrue(!theBinding.getPayload().isEmpty());

		// }

	}

}
