
package org.n52.wps.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.xml.sax.SAXException;

public class Wps4rIT {

    private static String wpsUrl;

    @BeforeClass
    public static void beforeClass() {
        wpsUrl = AllTestsIT.getURL();

        // Seems not to work but it would be nice if it does...
        // URL resource = WPS4RTester.class
        // .getResource("/R/wps_config.xml");
        // WPSConfig.forceInitialization(new File(resource.getFile()).getAbsolutePath());

        String host = System.getProperty("test.rserve.host", "127.0.0.1");
        int port = Integer.parseInt(System.getProperty("test.rserve.port", "6311"));
        String user = System.getProperty("test.rserve.user", null);
        String password = System.getProperty("test.rserve.pwd", null);
        try {
            RConnection c = getNewConnection(host, port, user, password);
            c.close();
        }
        catch (RserveException e1) {
            Assume.assumeNoException(e1);
        }
    }

    @AfterClass
    public static void afterClass() {
        // WPSConfig.forceInitialization("src/main/webapp/config/wps_config.xml");
    }

    private static RConnection getNewConnection(String host, int port, String user, String password) throws RserveException {
        RConnection con = new RConnection(host, port);
        if (con != null && con.needLogin())
            con.login(user, password);

        return con;
    }

    @Test
    public void sessionInfoRetrievedFromWPSWebsite() throws MalformedURLException {
        String temp = wpsUrl.substring(0, wpsUrl.lastIndexOf("/"));
        URL urlSessionInfo = new URL(temp + "/R/sessioninfo.jsp");
        try {
            String response = GetClient.sendRequest(urlSessionInfo.toExternalForm());
            assertThat(response, containsString("R ")); // "R version" fails if using unstable R!
            assertThat(response, containsString("Platform:"));
            assertThat(response, containsString("attached base packages:"));
        }
        catch (IOException e) {
            String message = "Cannot retrieve the R session info from WPS.";
            e.printStackTrace();
            throw new AssertionError(message);
        }
    }

    @Test
    public void resourcesAreLoadedAndRead() throws IOException,
            ParserConfigurationException,
            SAXException,
            XmlException {
        URL resource = Wps4rIT.class.getResource("/R/ExecuteTestResources.xml");
        XmlObject xmlPayload = XmlObject.Factory.parse(resource);

        String payload = xmlPayload.toString();
        String response = PostClient.sendRequest(wpsUrl, payload);

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, not(containsString("ExceptionReport")));
        assertThat(response, containsString("This is a dummy txt-file"));
        assertThat(response, containsString("480"));
    }

    @Test
    public void responseContainsVersionSection() throws IOException,
            ParserConfigurationException,
            SAXException,
            XmlException {
        URL resource = Wps4rIT.class.getResource("/R/ExecuteTestResources.xml");
        XmlObject xmlPayload = XmlObject.Factory.parse(resource);

        String payload = xmlPayload.toString();
        String response = PostClient.sendRequest(wpsUrl, payload);

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, not(containsString("ExceptionReport")));
        assertThat(response, containsString("R version "));
    }

    @Test
    public void responseContainsWarningsSection() throws IOException,
            ParserConfigurationException,
            SAXException,
            XmlException {
        URL resource = Wps4rIT.class.getResource("/R/ExecuteTestWarnings.xml");
        XmlObject xmlPayload = XmlObject.Factory.parse(resource);

        String payload = xmlPayload.toString();
        String response = PostClient.sendRequest(wpsUrl, payload);

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, containsString("warnings"));
    }

    @Test
    public void responseContainsWarningsContent() throws IOException,
            ParserConfigurationException,
            SAXException,
            XmlException {

        URL resource = Wps4rIT.class.getResource("/R/ExecuteTestWarnings.xml");
        XmlObject xmlPayload = XmlObject.Factory.parse(resource);

        String payload = xmlPayload.toString();
        String response = PostClient.sendRequest(wpsUrl, payload);

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, containsString("Test warning 4: This is a warning with some text."));
    }

    @Test
    public void decribeProcess() throws IOException, ParserConfigurationException, SAXException {
        String identifier = "org.n52.wps.server.r.test_resources";
        String response = GetClient.sendRequest(wpsUrl, "Service=WPS&Request=DescribeProcess&Version=1.0.0&Identifier="
                + identifier);

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, not(containsString("ExceptionReport")));
        assertThat(response, containsString(identifier));

        // TODO fix test: assertThat(response, containsString("<ows:Identifier>" + identifier +
        // "</ows:Identifier>"));
    }

    @Test
    public void capabilitiesContainProcess() throws IOException, ParserConfigurationException, SAXException {
        String response = GetClient.sendRequest(wpsUrl, "Service=WPS&Request=GetCapabilities");

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, not(containsString("ExceptionReport")));
        assertThat(response, containsString("org.n52.wps.server.r.test_resources"));
    }

    @Test
    public void responseTypeIsImage() throws IOException, XmlException {
        URL resource = Wps4rIT.class.getResource("/R/ExecuteTestImage.xml");
        XmlObject xmlPayload = XmlObject.Factory.parse(resource);
        String payload = xmlPayload.toString();

        String response = PostClient.sendRequest(wpsUrl, payload);
        assertThat(response.split("\n", 1)[0], containsString("PNG"));
        assertThat(response, response, not(containsString("ExceptionReport")));
    }

    @Test
    public void uniformIsExecuted() throws IOException, XmlException {
        URL resource = Wps4rIT.class.getResource("/R/ExecuteTestUniform.xml");
        XmlObject xmlPayload = XmlObject.Factory.parse(resource);
        String payload = xmlPayload.toString();

        String response = PostClient.sendRequest(wpsUrl, payload);
        assertThat(response, response, containsString("Process successful"));
        // output-specific:
        assertThat(response, containsString("\"x\""));
        assertThat(response, containsString("\"1\""));
        assertThat(response, containsString("\"2\""));
        assertThat(response, containsString("\"3\""));
        assertThat(response, not(containsString("ExceptionReport")));
    }

    @Test
    public void calculatorWorksCorrectly() throws IOException, ParserConfigurationException, SAXException, XmlException {
        URL resource = Wps4rIT.class.getResource("/R/ExecuteTestCalculator.xml");
        XmlObject xmlPayload = XmlObject.Factory.parse(resource);
        String payload = xmlPayload.toString();

        Random rand = new Random();
        int a = rand.nextInt(100);
        payload = payload.replace("@@@a@@@", Integer.toString(a));
        int b = rand.nextInt(100);
        payload = payload.replace("@@@b@@@", Integer.toString(b));
        int op = rand.nextInt(3);
        String[] ops = new String[] {"+", "-", "*"};
        String opString = ops[op];
        payload = payload.replace("@@@op@@@", opString);
        int result = Integer.MIN_VALUE;
        if (opString.equals("+"))
            result = a + b;
        else if (opString.equals("-"))
            result = a - b;
        else if (opString.equals("*"))
            result = a * b;

        String response = PostClient.sendRequest(wpsUrl, payload);

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, containsString(Integer.toString(result)));
    }

    // TODO add unit test for wps.off and wps.on annotations using a test script that contains various on/off
    // statements.

    // /*Complex XML Input by reference */
    // @Test
    // public void testExecutePOSTreferenceComplexXMLSynchronousXMLOutput()
    // throws IOException, ParserConfigurationException, SAXException {
    // System.out.println("\nRunning testExecutePOSTreferenceComplexXMLSynchronousXMLOutput");
    // String payload =
    // "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
    // +
    // "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
    // + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
    // +
    // "<ows:Identifier>org.n52.wps.server.algorithm.SimpleBufferAlgorithm</ows:Identifier>"
    // + "<wps:DataInputs>"
    // + "<wps:Input>"
    // + "<ows:Identifier>data</ows:Identifier>"
    // +
    // "<wps:Reference schema=\"http://schemas.opengis.net/gml/2.1.2/feature.xsd\" xlink:href=\"http://geoprocessing.demo.52north.org:8080/geoserver/ows?service=WFS&amp;version=1.0.0&amp;request=GetFeature&amp;typeName=topp:tasmania_roads\"/>"
    // + "</wps:Input>"
    // + "<wps:Input>"
    // + "<ows:Identifier>width</ows:Identifier>"
    // +
    // "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
    // + "<wps:Data>"
    // + "<wps:LiteralData>20</wps:LiteralData>"
    // + "</wps:Data>"
    // + "</wps:Input>"
    // + "</wps:DataInputs>"
    // + "<wps:ResponseForm>"
    // + "<wps:ResponseDocument>"
    // + "<wps:Output>"
    // + "<ows:Identifier>result</ows:Identifier>"
    // + "</wps:Output>"
    // + "</wps:ResponseDocument>"
    // + "</wps:ResponseForm>"
    // + "</wps:Execute>";
    // String response = PostClient.sendRequest(url, payload);
    //
    // assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
    // assertThat(response, response, not(containsString("ExceptionReport")));
    // assertThat(response, response, containsString("LinearRing"));
    // }
    //
    // /*Complex binary Input by value */
    // // Disabled test due to heap size issues.
    // @Test
    // public void testExecutePOSTValueComplexBinarySynchronousBinaryOutput()
    // throws IOException, ParserConfigurationException, SAXException {
    // System.out.println("\nRunning testExecutePOSTValueComplexBinarySynchronousBinaryOutput");
    // String payload =
    // "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
    // +
    // "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
    // + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
    // +
    // "<ows:Identifier>org.n52.wps.server.algorithm.raster.AddRasterValues</ows:Identifier>"
    // + "<wps:DataInputs>"
    // + "<wps:Input>"
    // + "<ows:Identifier>dataset1</ows:Identifier>"
    // +
    // "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m.tif\">"
    // + "</wps:Reference>"
    // + "</wps:Input>"
    // + "<wps:Input>"
    // + "<ows:Identifier>dataset2</ows:Identifier>"
    // +
    // "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m.tif\">"
    // + "</wps:Reference>"
    // + "</wps:Input>"
    // + "</wps:DataInputs>"
    // + "<wps:ResponseForm>"
    // + "<wps:ResponseDocument status=\"true\" storeExecuteResponse=\"true\">"
    // + "<wps:Output encoding=\"base64\" >"
    // + "<ows:Identifier>result</ows:Identifier>"
    // + "</wps:Output>"
    // + "</wps:ResponseDocument>"
    // + "</wps:ResponseForm>"
    // + "</wps:Execute>";
    // String response = PostClient.sendRequest(url, payload);
    // AllTestsIT.validateBinaryBase64Async(response);
    // }
}
