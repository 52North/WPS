package org.n52.wps.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

public class DescribeProcessKvpIT {

    private static String url;

    @BeforeClass
    public static void beforeClass() {
        url = AllTestsIT.getURL();
    }

    @Test
    public void testDescribeProcessCompleteSingle() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testDescribeProcessCompleteSingle");

        String response = GetClient.sendRequest(url, "Service=WPS&Request=DescribeProcess&Version=1.0.0&Identifier=org.n52.wps.server.algorithm.SimpleBufferAlgorithm");
        
        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("org.n52.wps.server.algorithm.SimpleBufferAlgorithm"));
    }

    @Test
    public void testDescribeProcessCompleteMultiple() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testDescribeProcessCompleteMultiple");

        String response = GetClient.sendRequest(url, "Service=WPS&Request=DescribeProcess&Version=1.0.0&Identifier=org.n52.wps.server.algorithm.SimpleBufferAlgorithm,org.n52.wps.server.algorithm.simplify.DouglasPeuckerAlgorithm");

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("org.n52.wps.server.algorithm.SimpleBufferAlgorithm"));
        assertThat(response, response, containsString("org.n52.wps.server.algorithm.simplify.DouglasPeuckerAlgorithm"));

    }

    @Test
    public void testDescribeProcessCompleteAll() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testDescribeProcessCompleteAll");
        
        String response = GetClient.sendRequest(url, "Service=WPS&Request=DescribeProcess&Version=1.0.0&Identifier=all");
        
        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("org.n52.wps.server.algorithm.SimpleBufferAlgorithm"));
        assertThat(response, response, containsString("org.n52.wps.server.algorithm.simplify.DouglasPeuckerAlgorithm"));

    }

    @Test
    public void testDescribeProcessMissingVersionParameter() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testDescribeProcessMissingVersionParameter");
        
        String response = GetClient.sendRequest(url, "Service=WPS&Request=DescribeProcess&Identifier=org.n52.wps.server.algorithm.SimpleBufferAlgorithm");
     
        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, containsString("ExceptionReport"));
        assertThat(response, response, not(containsString("org.n52.wps.server.algorithm.SimpleBufferAlgorithm")));
    }

    @Test
    public void testDescribeProcessMissingServiceParameter() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testDescribeProcessMissingServiceParameter");
        
        String response = GetClient.sendRequest(url, "Request=DescribeProcess&Version=1.0.0&Identifier=org.n52.wps.server.algorithm.SimpleBufferAlgorithm");
        
        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, containsString("ExceptionReport"));
        assertThat(response, response, not(containsString("org.n52.wps.server.algorithm.SimpleBufferAlgorithm")));
    }

    @Test
    public void testDescribeProcessMissingIdentifierParameter() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testDescribeProcessMissingIdentifierParameter");
        
        String response = GetClient.sendRequest(url, "Request=DescribeProcess&Version=1.0.0");
        
        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, containsString("ExceptionReport"));
        assertThat(response, response, not(containsString("org.n52.wps.server.algorithm.SimpleBufferAlgorithm")));
    }

    @Test
    public void testDescribeProcessWrongIdentifierParameter() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testDescribeProcessWrongIdentifierParameter");
        
        String response = GetClient.sendRequest(url, "Request=DescribeProcess&Version=1.0.0&Identifier=XXX");
        
        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, containsString("ExceptionReport"));
        assertThat(response, response, not(containsString("org.n52.wps.server.algorithm.SimpleBufferAlgorithm")));
    }
}
