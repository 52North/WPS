package org.n52.wps.test;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class GetCapabilitiesPOSTTester extends TestCase {
	private String url;
	
    @Override
	protected void setUp(){
		url = AllTestsIT.getURL();
	}
	
	/*
	 * *GetCapabilities*
		- POST Request with missing "request" parameter -->not possible
		- GetCapabilities POST request
		- GetCapabilities POST request with missing "version" paramater
		- GetCapabilities POST request with missing "service" paramater
	 */
	
	public void testGetCapabilitiesComplete(){
			
		String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"+ 
		"<wps:GetCapabilities service=\"WPS\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"+
			"http://schemas.opengis.net/wps/1.0.0/wpsGetCapabilities_request.xsd\">"+
			"<wps:AcceptVersions>"+
		        "<ows:Version>1.0.0</ows:Version>"+
		    "</wps:AcceptVersions>"+
				"</wps:GetCapabilities>";
		
				String response ="";
				try {
					response = PostClient.sendRequest(url, payload);
				} catch (IOException e) {
					fail(e.getMessage());
				}
				assertTrue(!response.contains("ExceptionReport"));
				assertTrue(response.contains("org.n52.wps.server.algorithm.SimpleBufferAlgorithm"));
	}
	
	
	

	public void testGetCapabilitiesMissingVersionParameter(){
		String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"+ 
		"<wps:GetCapabilities service=\"WPS\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"+
			"http://schemas.opengis.net/wps/1.0.0/wpsGetCapabilities_request.xsd\">"+
			"<wps:AcceptVersions>"+
			"<ows:Version>1.0.0</ows:Version>"+
		    "</wps:AcceptVersions>"+
				"</wps:GetCapabilities>";
		
				String response ="";
				try {
					response = PostClient.sendRequest(url, payload);
				} catch (IOException e) {
					fail(e.getMessage());
				}
				assertTrue(!response.contains("ExceptionReport"));
				assertTrue(response.contains("org.n52.wps.server.algorithm.SimpleBufferAlgorithm"));
	}
	
	public void testGetCapabilitiesMissingServiceParameter(){
		String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"+ 
		"<wps:GetCapabilities  version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"+
			"http://schemas.opengis.net/wps/1.0.0/wpsGetCapabilities_request.xsd\">"+
			"<wps:AcceptVersions>"+
		        "<ows:Version>1.0.0</ows:Version>"+
		    "</wps:AcceptVersions>"+
				"</wps:GetCapabilities>";
		
				String response ="";
				try {
					response = PostClient.sendRequest(url, payload);
				} catch (IOException e) {
					fail(e.getMessage());
				}
				assertTrue(!response.contains("ExceptionReport"));
				assertTrue(response.contains("org.n52.wps.server.algorithm.SimpleBufferAlgorithm"));
	
	}
	
		
	
	private Document parseXML(String xmlString) throws ParserConfigurationException, SAXException, IOException{
		DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		StringReader inStream = new StringReader(xmlString);
		InputSource inSource = new InputSource(inStream);
		return documentBuilder.parse(inSource);
	}

}
