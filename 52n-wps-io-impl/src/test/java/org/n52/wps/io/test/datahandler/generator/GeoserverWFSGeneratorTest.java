package org.n52.wps.io.test.datahandler.generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import junit.framework.TestCase;

import org.apache.xmlbeans.XmlException;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.IOUtils;
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.datahandler.generator.GeoserverWFSGenerator;
import org.n52.wps.io.datahandler.parser.GML3BasicParser;
import org.n52.wps.io.datahandler.parser.GeotiffParser;
import org.n52.wps.io.test.datahandler.AbstractTestCase;

public class GeoserverWFSGeneratorTest extends AbstractTestCase<GeoserverWFSGenerator> {

	public void testGenerator() {
		
		if(!isDataHandlerActive()){
			return;
		}		

		String testFilePath = projectRoot
				+ "/52n-wps-io-impl/src/test/resources/spearfish_restricted_sites_gml3.xml";

		try {
			testFilePath = URLDecoder.decode(testFilePath, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			fail(e1.getMessage());
		}
		
		GML3BasicParser theParser = new GML3BasicParser();

		String[] mimetypes = theParser.getSupportedFormats();

		InputStream input = null;

		try {
			input = new FileInputStream(new File(testFilePath));
		} catch (FileNotFoundException e) {
			fail(e.getMessage());
		}

		GTVectorDataBinding theBinding = theParser.parse(input,
				"text/xml; subtype=gml/3.2.1",
				"http://schemas.opengis.net/gml/3.2.1/base/feature.xsd");

		assertTrue(theBinding.getPayload() != null);

		String[] mimetypes2 = dataHandler.getSupportedFormats();

		for (String string : mimetypes2) {
			try {
				InputStream resultStream = dataHandler.generateStream(theBinding, string, null);
				
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resultStream));
				
				String line = "";
				
				while((line = bufferedReader.readLine()) != null){
					System.out.println(line);
				}
				
				String request = "http://localhost:8181/geoserver/wms?service=WMS&version=1.1.0&request=GetMap&layers=N52:primary738239570087452915.tif_72e5aa87-5e2e-4c70-b913-53f4bf910245&styles=&bbox=444650.0,4631220.0,451640.0,4640510.0&width=385&height=512&srs=EPSG:26716&format=image/tiff";
				
			} catch (IOException e) {
				e.printStackTrace();
				fail(e.getMessage());
			}

		}

	}

	@Override
	protected void initializeDataHandler() {
		dataHandler = new GeoserverWFSGenerator();		
	}

}
