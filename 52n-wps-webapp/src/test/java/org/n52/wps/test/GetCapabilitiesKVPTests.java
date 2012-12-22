package org.n52.wps.test;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xmlbeans.XmlException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

public class GetCapabilitiesKVPTests {

    private static String url;

    @BeforeClass
    public static void beforeClass() throws XmlException, IOException {
        url = AllTestsIT.getURL();
    }

    @Test
    public void testGetCapabilitiesMissingRequestParameter() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testGetCapabilitiesMissingRequestParameter");

        String response = GetClient.sendRequest(url, "Service=WPS&Request=GetCapabilities");

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("org.n52.wps.server.algorithm.SimpleBufferAlgorithm"));
    }

    @Test
    public void testGetCapabilitiesComplete() throws ParserConfigurationException, SAXException, IOException {
        System.out.println("\nRunning testGetCapabilitiesComplete");
        
        String response = GetClient.sendRequest(url, "Service=WPS&Request=GetCapabilities&Version=1.0.0");

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("org.n52.wps.server.algorithm.SimpleBufferAlgorithm"));
    }

    @Test
    public void testGetCapabilitiesMissingVersionParameter() throws ParserConfigurationException, SAXException, IOException {
        System.out.println("\nRunning testGetCapabilitiesMissingVersionParameter");
        
        String response = GetClient.sendRequest(url, "Service=WPS&Request=GetCapabilities");

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("org.n52.wps.server.algorithm.SimpleBufferAlgorithm"));
    }

    @Test
    public void testGetCapabilitiesMissingServiceParameter() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testGetCapabilitiesMissingServiceParameter");
        
        String response = GetClient.sendRequest(url, "Request=GetCapabilities&Version=1.0.0");

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, containsString("ExceptionReport"));
        assertThat(response, response, not(containsString("org.n52.wps.server.algorithm.SimpleBufferAlgorithm")));
    }
}
