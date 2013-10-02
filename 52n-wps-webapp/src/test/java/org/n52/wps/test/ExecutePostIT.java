package org.n52.wps.test;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xmlbeans.XmlException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.datahandler.parser.GeotiffParser;
import org.xml.sax.SAXException;

public class ExecutePostIT {

    private final static String TIFF_MAGIC = "<![CDATA[MM";
    private static String url;

    @BeforeClass
    public static void beforeClass() throws XmlException, IOException {
        url = AllTestsIT.getURL();
        WPSConfig.forceInitialization("src/main/webapp/config/wps_config.xml");
    }

    /*Complex XML Input by value */
    @Test
    public void testExecutePOSTinlineComplexXMLSynchronousXMLOutput() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTinlineComplexXMLSynchronousXMLOutput");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 "
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.SimpleBufferAlgorithm</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>data</ows:Identifier>"
                + "<wps:Data>"
                + "<wps:ComplexData schema=\"http://schemas.opengis.net/gml/3.1.0/base/feature.xsd\" mimeType=\"text/xml; subtype=gml/3.1.0\">"
                + "<wfs:FeatureCollection numberOfFeatures=\"0\" timeStamp=\"2012-03-08T18:26:46.296+01:00\" xsi:schemaLocation=\"http://www.openplans.org/topp http://geoprocessing.demo.52north.org:8080/geoserver/wfs?service=WFS&amp;version=1.1.0&amp;request=DescribeFeatureType&amp;typeName=topp%3Atasmania_roads http://www.opengis.net/wfs http://geoprocessing.demo.52north.org:8080/geoserver/schemas/wfs/1.1.0/wfs.xsd\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:tiger=\"http://www.census.gov\" xmlns:cite=\"http://www.opengeospatial.net/cite\" xmlns:nurc=\"http://www.nurc.nato.int\" xmlns:sde=\"http://geoserver.sf.net\" xmlns:wfs=\"http://www.opengis.net/wfs\" xmlns:topp=\"http://www.openplans.org/topp\" xmlns:it.geosolutions=\"http://www.geo-solutions.it\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:diss=\"diss\" xmlns:sf=\"http://www.openplans.org/spearfish\" xmlns:ows=\"http://www.opengis.net/ows\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"><gml:featureMembers><topp:tasmania_roads gml:id=\"tasmania_roads.1\"><topp:the_geom><gml:MultiLineString><gml:lineStringMember><gml:LineString><gml:posList>146.46858200000003 -41.241478 146.574768 -41.251186 146.64041099999997 -41.255154 146.76612899999998 -41.332348 146.79418900000002 -41.34417 146.82217400000002 -41.362988 146.86343399999998 -41.380234 146.899521 -41.379452 146.929504 -41.378227 147.008041 -41.356079 147.098343 -41.362919</gml:posList></gml:LineString></gml:lineStringMember></gml:MultiLineString></topp:the_geom><topp:TYPE>street</topp:TYPE></topp:tasmania_roads><topp:tasmania_roads gml:id=\"tasmania_roads.2\"><topp:the_geom><gml:MultiLineString><gml:lineStringMember><gml:LineString><gml:posList>147.098343 -41.362919 147.17305 -41.452778 147.213867 -41.503773 147.234894 -41.546661 147.251129 -41.573826 147.26466399999998 -41.602474 147.28448500000002 -41.617554 147.30058300000002 -41.637878</gml:posList></gml:LineString></gml:lineStringMember></gml:MultiLineString></topp:the_geom><topp:TYPE>highway</topp:TYPE></topp:tasmania_roads><topp:tasmania_roads gml:id=\"tasmania_roads.3\"><topp:the_geom><gml:MultiLineString><gml:lineStringMember><gml:LineString><gml:posList>147.30058300000002 -41.637878 147.225815 -41.626938 147.18331899999998 -41.619236 147.08236699999998 -41.577755 147.03132599999998 -41.565205 146.96148699999998 -41.564186 146.92454500000002 -41.568565 146.876328 -41.569614 146.783722 -41.56073 146.684937 -41.536232 146.614258 -41.478153 146.61999500000002 -41.423958 146.582581 -41.365482 146.52478000000002 -41.29541 146.47749299999998 -41.277622 146.46858200000003 -41.241478</gml:posList></gml:LineString></gml:lineStringMember></gml:MultiLineString></topp:the_geom><topp:TYPE>lane</topp:TYPE></topp:tasmania_roads><topp:tasmania_roads gml:id=\"tasmania_roads.4\"><topp:the_geom><gml:MultiLineString><gml:lineStringMember><gml:LineString><gml:posList>147.522247 -41.859921 147.55186500000002 -41.927834 147.59732100000002 -42.017418 147.578644 -42.113216 147.541656 -42.217743 147.46867400000002 -42.22662</gml:posList></gml:LineString></gml:lineStringMember></gml:MultiLineString></topp:the_geom><topp:TYPE>highway</topp:TYPE></topp:tasmania_roads><topp:tasmania_roads gml:id=\"tasmania_roads.5\"><topp:the_geom><gml:MultiLineString><gml:lineStringMember><gml:LineString><gml:posList>146.103699 -41.171677 146.30361900000003 -41.237202 146.36222800000002 -41.236279 146.39418 -41.245384 146.44372600000003 -41.244308 146.46858200000003 -41.241478</gml:posList></gml:LineString></gml:lineStringMember></gml:MultiLineString></topp:the_geom><topp:TYPE>gravel</topp:TYPE></topp:tasmania_roads><topp:tasmania_roads gml:id=\"tasmania_roads.6\"><topp:the_geom><gml:MultiLineString><gml:lineStringMember><gml:LineString><gml:posList>145.856018 -41.08007 145.944839 -41.119896 146.03799400000003 -41.150059 146.103699 -41.171677</gml:posList></gml:LineString></gml:lineStringMember></gml:MultiLineString></topp:the_geom><topp:TYPE>road</topp:TYPE></topp:tasmania_roads><topp:tasmania_roads gml:id=\"tasmania_roads.7\"><topp:the_geom><gml:MultiLineString><gml:lineStringMember><gml:LineString><gml:posList>147.46867400000002 -42.22662 147.474945 -42.292259 147.467697 -42.301292 147.45182799999998 -42.341656 147.42454500000002 -42.378723 147.366013 -42.412552 147.345779 -42.432449 147.28932200000003 -42.476475 147.26451100000003 -42.503899 147.25991800000003 -42.547539 147.24940500000002 -42.614006 147.278351 -42.693249 147.284271 -42.757759 147.25674400000003 -42.778393</gml:posList></gml:LineString></gml:lineStringMember></gml:MultiLineString></topp:the_geom><topp:TYPE>highway</topp:TYPE></topp:tasmania_roads><topp:tasmania_roads gml:id=\"tasmania_roads.8\"><topp:the_geom><gml:MultiLineString><gml:lineStringMember><gml:LineString><gml:posList>148.249252 -41.860851 148.23443600000002 -41.901783 148.19212299999998 -41.93721 148.15576199999998 -41.953667 148.12773099999998 -41.994537 148.053131 -42.100563</gml:posList></gml:LineString></gml:lineStringMember></gml:MultiLineString></topp:the_geom><topp:TYPE>road</topp:TYPE></topp:tasmania_roads><topp:tasmania_roads gml:id=\"tasmania_roads.9\"><topp:the_geom><gml:MultiLineString><gml:lineStringMember><gml:LineString><gml:posList>145.19754 -40.878323 145.24667399999998 -40.86021 145.29328900000002 -40.852802 145.46522499999998 -40.897865 145.538498 -40.936264 145.554062 -40.939201 145.60211199999998 -40.962936 145.646362 -40.98243 145.68383799999998 -40.989883 145.71058699999998 -40.996201 145.74429300000003 -41.007545 145.80195600000002 -41.041782 145.856018 -41.08007</gml:posList></gml:LineString></gml:lineStringMember></gml:MultiLineString></topp:the_geom><topp:TYPE>logging</topp:TYPE></topp:tasmania_roads><topp:tasmania_roads gml:id=\"tasmania_roads.10\"><topp:the_geom><gml:MultiLineString><gml:lineStringMember><gml:LineString><gml:posList>147.360001 -42.91993 147.348816 -42.93726 147.28504900000001 -42.979027 147.220886 -42.995876 147.16442899999998 -43.027004 147.068237 -43.06319 146.96463 -43.116447 146.94955399999998 -43.17004 146.95369 -43.209591 146.96412700000002 -43.224545 146.97572300000002 -43.250484 146.98075899999998 -43.2701 146.98260499999998 -43.287716 146.970871 -43.31691 146.940521 -43.33812 146.94305400000002 -43.362263 146.95219400000002 -43.39278 146.95542899999998 -43.423512</gml:posList></gml:LineString></gml:lineStringMember></gml:MultiLineString></topp:the_geom><topp:TYPE>road</topp:TYPE></topp:tasmania_roads><topp:tasmania_roads gml:id=\"tasmania_roads.11\"><topp:the_geom><gml:MultiLineString><gml:lineStringMember><gml:LineString><gml:posList>147.30058300000002 -41.637878 147.372009 -41.695503 147.40258799999998 -41.725574 147.44406099999998 -41.749676 147.490433 -41.782482 147.506866 -41.795624 147.522919 -41.835609 147.522247 -41.859921</gml:posList></gml:LineString></gml:lineStringMember></gml:MultiLineString></topp:the_geom><topp:TYPE>highway</topp:TYPE></topp:tasmania_roads><topp:tasmania_roads gml:id=\"tasmania_roads.12\"><topp:the_geom><gml:MultiLineString><gml:lineStringMember><gml:LineString><gml:posList>148.053131 -42.100563 148.028229 -42.188286 148.00225799999998 -42.2295 147.96995500000003 -42.254417 147.96029700000003 -42.284897 147.942719 -42.398819 147.92640699999998 -42.486034 147.875092 -42.538582 147.832001 -42.587299 147.744217 -42.631607 147.69329800000003 -42.656067 147.61819500000001 -42.691135 147.57531699999998 -42.743092 147.57829299999997 -42.769539 147.54785199999998 -42.814312 147.50669900000003 -42.842907 147.488312 -42.877041 147.44969200000003 -42.901054 147.416809 -42.902828</gml:posList></gml:LineString></gml:lineStringMember></gml:MultiLineString></topp:the_geom><topp:TYPE>road</topp:TYPE></topp:tasmania_roads><topp:tasmania_roads gml:id=\"tasmania_roads.13\"><topp:the_geom><gml:MultiLineString><gml:lineStringMember><gml:LineString><gml:posList>147.098343 -41.362919 147.065445 -41.311977 147.02407799999997 -41.257534 146.981445 -41.211391 146.94822699999997 -41.181595 146.92677300000003 -41.172501 146.905029 -41.147144 146.940765 -41.085857 146.96266200000002 -41.075096 147.02108800000002 -41.080925 147.09922799999998 -41.123959 147.187607 -41.150597 147.28202800000003 -41.104244 147.29571499999997 -41.075798 147.30659500000002 -41.062832 147.32574499999998 -41.053524 147.36299100000002 -41.080441 147.41902199999998 -41.081764 147.46588100000002 -41.06089 147.51930199999998 -41.092793 147.528595 -41.137089 147.552521 -41.193565 147.594223 -41.233875 147.73440599999998 -41.239891 147.82937600000002 -41.196636 147.882614 -41.163197 147.91127 -41.163109 147.985168 -41.226128 148.022156 -41.292599 148.07511899999997 -41.313915 148.200104 -41.323097 148.23619100000002 -41.339245 148.27298000000002 -41.383488 148.25 -41.45713 148.254395 -41.53941 148.26243599999998 -41.585217 148.249252 -41.860851</gml:posList></gml:LineString></gml:lineStringMember></gml:MultiLineString></topp:the_geom><topp:TYPE>road</topp:TYPE></topp:tasmania_roads><topp:tasmania_roads gml:id=\"tasmania_roads.14\"><topp:the_geom><gml:MultiLineString><gml:lineStringMember><gml:LineString><gml:posList>147.25674400000003 -42.778393 147.22018400000002 -42.824776 147.179596 -42.82143 147.11132800000001 -42.795731 147.057098 -42.741581 147.00347900000003 -42.704803 146.91909800000002 -42.622734 146.91053799999997 -42.610928 146.88998400000003 -42.585396 146.83844 -42.572792 146.78569 -42.539352 146.724335 -42.485966 146.695023 -42.469582 146.64987200000002 -42.450371 146.604965 -42.432274 146.578781 -42.408531 146.539307 -42.364208 146.525055 -42.30883 146.558044 -42.275948 146.57624800000002 -42.23777 146.58146699999998 -42.203426 146.490005 -42.180222 146.3797 -42.146332 146.33406100000002 -42.138741 146.270966 -42.165703 146.197296 -42.224072 146.167908 -42.244835 146.16493200000002 -42.245171 146.111023 -42.265202 146.03747600000003 -42.239738 145.981628 -42.187851 145.85391199999998 -42.133492 145.819611 -42.129154 145.72052000000002 -42.104084 145.61857600000002 -42.056023 145.541718 -42.027241 145.48628200000002 -41.983326 145.452744 -41.926544 145.494034 -41.896477 145.59173600000003 -41.860214 145.64211999999998 -41.838398 145.669449 -41.830734 145.680923 -41.795753 145.68296800000002 -41.743221 145.67515600000002 -41.710377 145.680115 -41.688908 145.70106500000003 -41.648228 145.71479799999997 -41.609509 145.62919599999998 -41.462051 145.64889499999998 -41.470337 145.633423 -41.420902 145.631866 -41.36528 145.640854 -41.301533 145.700424 -41.242611 145.77242999999999 -41.193897 145.80233800000002 -41.161488 145.856018 -41.08007</gml:posList></gml:LineString></gml:lineStringMember></gml:MultiLineString></topp:the_geom><topp:TYPE>road</topp:TYPE></topp:tasmania_roads></gml:featureMembers></wfs:FeatureCollection>"
                + "</wps:ComplexData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "<wps:Input>"
                + "<ows:Identifier>width</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:LiteralData>20</wps:LiteralData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument>"
                + "<wps:Output>"
                + "<ows:Identifier>result</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";

        String response = PostClient.sendRequest(url, payload);

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("LinearRing"));
    }

    /*Complex XML Input by reference */
    @Test
    public void testExecutePOSTreferenceComplexXMLSynchronousXMLOutput() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTreferenceComplexXMLSynchronousXMLOutput");
        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.SimpleBufferAlgorithm</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>data</ows:Identifier>"
                + "<wps:Reference schema=\"http://schemas.opengis.net/gml/2.1.2/feature.xsd\" xlink:href=\"http://geoprocessing.demo.52north.org:8080/geoserver/ows?service=WFS&amp;version=1.0.0&amp;request=GetFeature&amp;typeName=topp:tasmania_roads\"/>"
                + "</wps:Input>"
                + "<wps:Input>"
                + "<ows:Identifier>width</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:LiteralData>20</wps:LiteralData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument>"
                + "<wps:Output>"
                + "<ows:Identifier>result</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("LinearRing"));
    }
    
    /*Multiple complex XML Input by reference */
    @Test
    public void testExecutePOSTMultipleReferenceComplexXMLSynchronousXMLOutput() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTMultipleReferenceComplexXMLSynchronousXMLOutput");
        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.test.MultiReferenceInputAlgorithm</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>data</ows:Identifier>"
                + "<wps:Reference schema=\"http://schemas.opengis.net/gml/2.1.2/feature.xsd\" xlink:href=\"http://geoprocessing.demo.52north.org:8080/geoserver/ows?service=WFS&amp;version=1.0.0&amp;request=GetFeature&amp;typeName=topp:tasmania_roads\"/>"
                + "</wps:Input>"
                + "<wps:Input>"
                + "<ows:Identifier>data</ows:Identifier>"
                + "<wps:Reference schema=\"http://schemas.opengis.net/gml/2.1.2/feature.xsd\" xlink:href=\"http://geoprocessing.demo.52north.org:8080/geoserver/ows?service=WFS&amp;version=1.0.0&amp;request=GetFeature&amp;typeName=topp:tasmania_roads\"/>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument>"
                + "<wps:Output>"
                + "<ows:Identifier>result</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("FeatureCollection"));
    }
    
    /*Multiple complex XML Input by reference */
    @Test
    public void testExecutePOSTMultipleReferenceComplexBinarySynchronousBinaryOutput() throws IOException, ParserConfigurationException, SAXException {
    	System.out.println("\nRunning testExecutePOSTMultipleReferenceComplexBinarySynchronousBinaryOutput");
    	String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
    			+ "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
    			+ "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
    			+ "<ows:Identifier>org.n52.wps.server.algorithm.test.MultiReferenceBinaryInputAlgorithm</ows:Identifier>"
    			+ "<wps:DataInputs>"
    			+ "<wps:Input>"
    			+ "<ows:Identifier xmlns:ns1=\"http://www.opengis.net/ows/1.1\">data</ows:Identifier>"
    			+ "<wps:Reference xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m21.tif\" mimeType=\"image/tiff\"/>"
    			+ "</wps:Input>"
    			+ "<wps:Input>"
    			+ "<ows:Identifier xmlns:ns1=\"http://www.opengis.net/ows/1.1\">data</ows:Identifier>"
    			+ "<wps:Reference xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m21.tif\" mimeType=\"image/tiff\"/>"
    			+ "</wps:Input>"
    			+ "</wps:DataInputs>"
    			+ "<wps:ResponseForm>"
    			+ "<wps:ResponseDocument>"
    			+ "<wps:Output mimeType=\"image/tiff\" encoding=\"base64\">"
    			+ "<ows:Identifier>result</ows:Identifier>"
    			+ "</wps:Output>"
    			+ "</wps:ResponseDocument>"
    			+ "</wps:ResponseForm>"
    			+ "</wps:Execute>";
    	String response = PostClient.sendRequest(url, payload);
    	
    	assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
    	assertThat(response, response, not(containsString("ExceptionReport")));
    	assertThat(response, response, containsString("SUkqAAgAAAASAAABAwA"));
    }
    
    /*Complex XML Input by reference, POST*/
    @Test
    public void testExecutePOSTreferenceComplexXMLSynchronousXMLOutput_WFS_POST_MissingMimeType() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTreferenceComplexXMLSynchronousXMLOutput");
        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.SimpleBufferAlgorithm</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>data</ows:Identifier>"
                + "<wps:Reference xlink:href=\"http://geoprocessing.demo.52north.org:8080/geoserver/ows\">"
                + "<wps:Body>"
                + "<wfs:GetFeature xmlns:wfs=\"http://www.opengis.net/wfs\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:gml=\"http://www.opengis.net/gml\" outputFormat=\"GML2\" version=\"1.0.0\" service=\"WFS\">"
                + "<wfs:Query typeName=\"topp:tasmania_roads\" />"
                + "</wfs:GetFeature>"
                + "</wps:Body>"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "<wps:Input>"
                + "<ows:Identifier>width</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:LiteralData>20</wps:LiteralData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument>"
                + "<wps:Output>"
                + "<ows:Identifier>result</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("LinearRing"));
    }

    /*Complex binary Input by value */
    // Disabled test due to heap size issues. 
    @Test
    public void testExecutePOSTValueComplexBinaryASynchronousBinaryOutput() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTValueComplexBinaryASynchronousBinaryOutput");
        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.raster.AddRasterValues</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>dataset1</ows:Identifier>"
                + "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m21.tif\">"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "<wps:Input>"
                + "<ows:Identifier>dataset2</ows:Identifier>"
                + "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m21.tif\">"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"true\" storeExecuteResponse=\"true\">"
                + "<wps:Output encoding=\"base64\" >"
                + "<ows:Identifier>result</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        AllTestsIT.validateBinaryBase64Async(response);
    }

    /*Complex binary Input by reference */
    // Disabled test due to heap size issues. 
    @Test
    public void testExecutePOSTReferenceComplexBinaryASynchronousBinaryOutput() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTReferenceComplexBinaryASynchronousBinaryOutput");
        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.raster.AddRasterValues</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>dataset1</ows:Identifier>"
                + "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m21.tif\">"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "<wps:Input>"
                + "<ows:Identifier>dataset2</ows:Identifier>"
                + "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m21.tif\">"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"true\" storeExecuteResponse=\"true\">"
                + "<wps:Output encoding=\"base64\" >"
                + "<ows:Identifier>result</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        AllTestsIT.validateBinaryBase64Async(response);
    }

    /*Literal Input by value integer */
    @Test
    public void testExecutePOSTreferenceLiteralIntSynchronousXMLOutput() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTreferenceLiteralIntSynchronousXMLOutput");
        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.SimpleBufferAlgorithm</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>data</ows:Identifier>"
                + "<wps:Reference schema=\"http://schemas.opengis.net/gml/2.1.2/feature.xsd\" xlink:href=\"http://geoprocessing.demo.52north.org:8080/geoserver/ows?service=WFS&amp;version=1.0.0&amp;request=GetFeature&amp;typeName=topp:tasmania_roads\"/>"
                + "</wps:Input>"
                + "<wps:Input>"
                + "<ows:Identifier>width</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:LiteralData>20</wps:LiteralData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument>"
                + "<wps:Output>"
                + "<ows:Identifier>result</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("LinearRing"));
    }

    /*BBOX Input by value */
    @Test
    public void testExecutePOSTinlineBBOXDataInputTestSynchronousBBOXOutput() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTinlineBBOXDataInputTestSynchronousBBOXOutput");
        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 "
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.test.DummyTestClass</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>BBOXInputData</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:BoundingBoxData crs=\"EPSG:4326\">"
                + "<ows:LowerCorner>46.75 13.05</ows:LowerCorner>"
                + "<ows:UpperCorner>46.85 13.25</ows:UpperCorner>"
                + "</wps:BoundingBoxData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"false\" storeExecuteResponse=\"false\">"
                + "<wps:Output asReference=\"false\">"
                + "<ows:Identifier>BBOXOutputData</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("46.75 13.05"));
        assertThat(response, response, containsString("EPSG"));
    }

    /*BBOX Input by value NO EPSG*/
    @Test
    public void testExecutePOSTinlineBBOXDataInputTestSynchronousBBOXOutputNoEPSG() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTinlineBBOXDataInputTestSynchronousBBOXOutputNoEPSG");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 "
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.test.DummyTestClass</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>BBOXInputData</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:BoundingBoxData>"
                + "<ows:LowerCorner>46.75 13.05</ows:LowerCorner>"
                + "<ows:UpperCorner>46.85 13.25</ows:UpperCorner>"
                + "</wps:BoundingBoxData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"false\" storeExecuteResponse=\"false\">"
                + "<wps:Output asReference=\"false\">"
                + "<ows:Identifier>BBOXOutputData</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, not(containsString("EPSG")));
        assertThat(response, response, containsString("46.75 13.05"));
    }

    /*Complex XML Output by value */
    @Test
    public void testExecutePOSTComplexXMLSynchronousXMLOutput() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTComplexXMLSynchronousXMLOutput");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.SimpleBufferAlgorithm</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>data</ows:Identifier>"
                + "<wps:Reference schema=\"http://schemas.opengis.net/gml/2.1.2/feature.xsd\" xlink:href=\"http://geoprocessing.demo.52north.org:8080/geoserver/ows?service=WFS&amp;version=1.0.0&amp;request=GetFeature&amp;typeName=topp:tasmania_roads\"/>"
                + "</wps:Input>"
                + "<wps:Input>"
                + "<ows:Identifier>width</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:LiteralData>20</wps:LiteralData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument>"
                + "<wps:Output>"
                + "<ows:Identifier>result</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("LinearRing"));
    }

    @Test
    public void testExecutePOSTComplexXMLSynchronousXMLOutputByReference() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTComplexXMLSynchronousXMLOutputByReference");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.SimpleBufferAlgorithm</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>data</ows:Identifier>"
                + "<wps:Reference schema=\"http://schemas.opengis.net/gml/2.1.2/feature.xsd\" xlink:href=\"http://geoprocessing.demo.52north.org:8080/geoserver/ows?service=WFS&amp;version=1.0.0&amp;request=GetFeature&amp;typeName=topp:tasmania_roads\"/>"
                + "</wps:Input>"
                + "<wps:Input>"
                + "<ows:Identifier>width</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:LiteralData>20</wps:LiteralData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument>"
                + "<wps:Output asReference=\"true\">"
                + "<ows:Identifier>result</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        AllTestsIT.checkReferenceXMLResult(response);
    }

    @Test
    public void testExecutePOSTComplexXMLSynchronousXMLOutputStatusTrue() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTComplexXMLSynchronousXMLOutputStatusTrue");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.SimpleBufferAlgorithm</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>data</ows:Identifier>"
                + "<wps:Reference schema=\"http://schemas.opengis.net/gml/2.1.2/feature.xsd\" xlink:href=\"http://geoprocessing.demo.52north.org:8080/geoserver/ows?service=WFS&amp;version=1.0.0&amp;request=GetFeature&amp;typeName=topp:tasmania_roads\"/>"
                + "</wps:Input>"
                + "<wps:Input>"
                + "<ows:Identifier>width</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:LiteralData>20</wps:LiteralData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"true\" storeExecuteResponse=\"false\">"
                + "<wps:Output>"
                + "<ows:Identifier>result</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("LinearRing"));
        assertThat(response, response, containsString("ProcessSucceeded"));
    }

    @Test
    public void testExecutePOSTComplexXMLSynchronousXMLOutputByReferenceStatusTrue() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTComplexXMLSynchronousXMLOutputByReferenceStatusTrue");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.SimpleBufferAlgorithm</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>data</ows:Identifier>"
                + "<wps:Reference schema=\"http://schemas.opengis.net/gml/2.1.2/feature.xsd\" xlink:href=\"http://geoprocessing.demo.52north.org:8080/geoserver/ows?service=WFS&amp;version=1.0.0&amp;request=GetFeature&amp;typeName=topp:tasmania_roads\"/>"
                + "</wps:Input>"
                + "<wps:Input>"
                + "<ows:Identifier>width</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:LiteralData>20</wps:LiteralData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"false\" storeExecuteResponse=\"false\">"
                + "<wps:Output asReference=\"true\">"
                + "<ows:Identifier>result</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        AllTestsIT.checkReferenceXMLResult(response);
    }

    @Test
    public void testExecutePOSTComplexXMLASynchronousXMLOutputBStoreStatusTrue() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTComplexXMLASynchronousXMLOutputBStoreStatusTrue");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.SimpleBufferAlgorithm</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>data</ows:Identifier>"
                + "<wps:Reference schema=\"http://schemas.opengis.net/gml/2.1.2/feature.xsd\" xlink:href=\"http://geoprocessing.demo.52north.org:8080/geoserver/ows?service=WFS&amp;version=1.0.0&amp;request=GetFeature&amp;typeName=topp:tasmania_roads\"/>"
                + "</wps:Input>"
                + "<wps:Input>"
                + "<ows:Identifier>width</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:LiteralData>20</wps:LiteralData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"true\" storeExecuteResponse=\"true\">"
                + "<wps:Output>"
                + "<ows:Identifier>result</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("statusLocation"));

        String refResult = AllTestsIT.getAsyncDoc(response);
        assertThat(refResult, refResult, not(containsString("ExceptionReport")));
        assertThat(refResult, refResult, containsString("LinearRing"));
    }

    @Test
    public void testExecutePOSTComplexXMLASynchronousXMLOutputStoreTrue() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTComplexXMLASynchronousXMLOutputStoreTrue");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.SimpleBufferAlgorithm</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>data</ows:Identifier>"
                + "<wps:Reference schema=\"http://schemas.opengis.net/gml/2.1.2/feature.xsd\" xlink:href=\"http://geoprocessing.demo.52north.org:8080/geoserver/ows?service=WFS&amp;version=1.0.0&amp;request=GetFeature&amp;typeName=topp:tasmania_roads\"/>"
                + "</wps:Input>"
                + "<wps:Input>"
                + "<ows:Identifier>width</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:LiteralData>20</wps:LiteralData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"false\" storeExecuteResponse=\"true\">"
                + "<wps:Output>"
                + "<ows:Identifier>result</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("statusLocation"));

        String refResult = AllTestsIT.getAsyncDoc(response);
        assertThat(refResult, refResult, not(containsString("ExceptionReport")));
        assertThat(refResult, refResult, containsString("LinearRing"));
    }

    @Test
    public void testExecutePOSTComplexXMLASynchronousXMLOutputBReferenceStoreTrue() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTComplexXMLASynchronousXMLOutputBReferenceStoreTrue");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.SimpleBufferAlgorithm</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>data</ows:Identifier>"
                + "<wps:Reference schema=\"http://schemas.opengis.net/gml/2.1.2/feature.xsd\" xlink:href=\"http://geoprocessing.demo.52north.org:8080/geoserver/ows?service=WFS&amp;version=1.0.0&amp;request=GetFeature&amp;typeName=topp:tasmania_roads\"/>"
                + "</wps:Input>"
                + "<wps:Input>"
                + "<ows:Identifier>width</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:LiteralData>20</wps:LiteralData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"false\" storeExecuteResponse=\"true\">"
                + "<wps:Output asReference=\"true\">"
                + "<ows:Identifier>result</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("statusLocation"));

        String doc = AllTestsIT.getAsyncDoc(response);
        String refResult = AllTestsIT.getRefAsString(doc);
        assertThat(refResult, refResult, not(containsString("ExceptionReport")));
        assertThat(refResult, refResult, containsString("LinearRing"));
    }

    @Test
    public void testExecutePOSTComplexXMLASynchronousXMLOutputByValueStoreStoreTrue() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTComplexXMLASynchronousXMLOutputByValueStoreStoreTrue");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.SimpleBufferAlgorithm</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>data</ows:Identifier>"
                + "<wps:Reference schema=\"http://schemas.opengis.net/gml/2.1.2/feature.xsd\" xlink:href=\"http://geoprocessing.demo.52north.org:8080/geoserver/ows?service=WFS&amp;version=1.0.0&amp;request=GetFeature&amp;typeName=topp:tasmania_roads\"/>"
                + "</wps:Input>"
                + "<wps:Input>"
                + "<ows:Identifier>width</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:LiteralData>20</wps:LiteralData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"true\" storeExecuteResponse=\"true\">"
                + "<wps:Output asReference=\"false\">"
                + "<ows:Identifier>result</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("statusLocation"));

        String refResult = AllTestsIT.getAsyncDoc(response);
        assertThat(refResult, refResult, not(containsString("ExceptionReport")));
        assertThat(refResult, refResult, containsString("LinearRing"));
    }

    @Test
    public void testExecutePOSTComplexXMLASynchronousXMLOutputByReferenceStatusStoreTrue() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTComplexXMLASynchronousXMLOutputByReferenceStatusStoreTrue");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.SimpleBufferAlgorithm</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>data</ows:Identifier>"
                + "<wps:Reference schema=\"http://schemas.opengis.net/gml/2.1.2/feature.xsd\" xlink:href=\"http://geoprocessing.demo.52north.org:8080/geoserver/ows?service=WFS&amp;version=1.0.0&amp;request=GetFeature&amp;typeName=topp:tasmania_roads\"/>"
                + "</wps:Input>"
                + "<wps:Input>"
                + "<ows:Identifier>width</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:LiteralData>20</wps:LiteralData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"true\" storeExecuteResponse=\"true\">"
                + "<wps:Output asReference=\"true\">"
                + "<ows:Identifier>result</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("statusLocation"));

        String doc = AllTestsIT.getAsyncDoc(response);
        String refResult = AllTestsIT.getRefAsString(doc);
        assertThat(refResult, refResult, not(containsString("ExceptionReport")));
        assertThat(refResult, refResult, containsString("gml:FeatureCollection"));
    }

    @Test
    public void testExecutePOSTComplexXMLASynchronousRawXMLOutput() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTComplexXMLASynchronousRawXMLOutput");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.SimpleBufferAlgorithm</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>data</ows:Identifier>"
                + "<wps:Reference schema=\"http://schemas.opengis.net/gml/2.1.2/feature.xsd\" xlink:href=\"http://geoprocessing.demo.52north.org:8080/geoserver/ows?service=WFS&amp;version=1.0.0&amp;request=GetFeature&amp;typeName=topp:tasmania_roads\"/>"
                + "</wps:Input>"
                + "<wps:Input>"
                + "<ows:Identifier>width</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:LiteralData>20</wps:LiteralData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:RawDataOutput>"
                + "<ows:Identifier>result</ows:Identifier>"
                + "</wps:RawDataOutput>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";

        String response = PostClient.sendRequest(url, payload);

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("LinearRing"));
        assertThat(response, response, not(containsString("Execute")));
    }

    @Test
    public void testExecutePOSTValueComplexBinarySynchronousBinaryOutputBase64() throws ParserConfigurationException, IOException, SAXException {
        System.out.println("\nRunning testExecutePOSTValueComplexBinarySynchronousBinaryOutputBase64");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.raster.AddRasterValues</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>dataset1</ows:Identifier>"
                + "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m21.tif\">"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "<wps:Input>"
                + "<ows:Identifier>dataset2</ows:Identifier>"
                + "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m21.tif\">"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"false\" storeExecuteResponse=\"false\">"
                + "<wps:Output  asReference=\"false\" mimeType=\"image/tiff\" encoding=\"base64\" >"
                + "<ows:Identifier>result</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("ExecuteResponse"));
        assertThat(response, response, containsString("AAEGAAMAAAABAAEAAAEVAAMAAAABA"));
    }

    @Test
    public void testExecutePOSTValueComplexBinarySynchronousBinaryOutputNoEncoding() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTValueComplexBinarySynchronousBinaryOutputNoEncoding");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.raster.AddRasterValues</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>dataset1</ows:Identifier>"
                + "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m21.tif\">"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "<wps:Input>"
                + "<ows:Identifier>dataset2</ows:Identifier>"
                + "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m21.tif\">"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"false\" storeExecuteResponse=\"false\">"
                + "<wps:Output  asReference=\"false\"  mimeType=\"image/tiff\" >"
                + "<ows:Identifier>result</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("ExecuteResponse"));
        assertThat(response, response, containsString("<![CDATA[MM"));
    }

    @Test
    public void testExecutePOSTValueComplexBinarySynchronousBinaryOutputAsReferenceBase64() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTValueComplexBinarySynchronousBinaryOutputAsReferenceBase64");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.raster.AddRasterValues</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>dataset1</ows:Identifier>"
                + "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m21.tif\">"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "<wps:Input>"
                + "<ows:Identifier>dataset2</ows:Identifier>"
                + "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m21.tif\">"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"false\" storeExecuteResponse=\"false\">"
                + "<wps:Output asReference=\"true\" mimeType=\"image/tiff\" encoding=\"base64\" >"
                + "<ows:Identifier>result</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("ExecuteResponse"));
        AllTestsIT.checkReferenceBinaryResultBase64(response);
    }

    @Test
    public void testExecutePOSTValueComplexBinarySynchronousBinaryOutputAsReferenceNoEncoding() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTValueComplexBinarySynchronousBinaryOutputAsReferenceNoEncoding");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.raster.AddRasterValues</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>dataset1</ows:Identifier>"
                + "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m21.tif\">"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "<wps:Input>"
                + "<ows:Identifier>dataset2</ows:Identifier>"
                + "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m21.tif\">"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"false\" storeExecuteResponse=\"false\">"
                + "<wps:Output asReference=\"true\"  mimeType=\"image/tiff\" >"
                + "<ows:Identifier>result</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        assertThat(response, response, not(containsString("ExceptionReport")));
        AllTestsIT.checkReferenceBinaryResultDefault(response);
    }

    @Test
    public void testExecutePOSTValueComplexBinarySynchronousBinaryOutputStatusBase64() throws ParserConfigurationException, SAXException, IOException {
        System.out.println("\nRunning testExecutePOSTValueComplexBinarySynchronousBinaryOutputStatusBase64");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.raster.AddRasterValues</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>dataset1</ows:Identifier>"
                + "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m21.tif\">"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "<wps:Input>"
                + "<ows:Identifier>dataset2</ows:Identifier>"
                + "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m21.tif\">"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"true\" storeExecuteResponse=\"false\">"
                + "<wps:Output asReference=\"false\" encoding=\"base64\"  mimeType=\"image/tiff\" >"
                + "<ows:Identifier>result</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("ExecuteResponse"));
        assertThat(response, response, containsString("AAEGAAMAAAABAAEAAAEVAAMAAAABA"));
        assertThat(response, response, containsString("Status"));
    }

    @Test
    public void testExecutePOSTValueComplexBinarySynchronousBinaryOutputStatusNoEncoding() throws ParserConfigurationException, SAXException, IOException {
        System.out.println("\nRunning testExecutePOSTValueComplexBinarySynchronousBinaryOutputStatusNoEncoding");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.raster.AddRasterValues</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>dataset1</ows:Identifier>"
                + "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m21.tif\">"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "<wps:Input>"
                + "<ows:Identifier>dataset2</ows:Identifier>"
                + "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m21.tif\">"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"true\" storeExecuteResponse=\"false\">"
                + "<wps:Output asReference=\"false\" mimeType=\"image/tiff\" >"
                + "<ows:Identifier>result</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, containsString("ProcessSucceeded"));
        assertThat(response, response, containsString(TIFF_MAGIC));
    }

    @Test
    public void testExecutePOSTValueComplexBinarySynchronousBinaryOutputAsReferenceStatusBase64() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTValueComplexBinarySynchronousBinaryOutputAsReferenceStatusBase64");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.raster.AddRasterValues</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>dataset1</ows:Identifier>"
                + "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m21.tif\">"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "<wps:Input>"
                + "<ows:Identifier>dataset2</ows:Identifier>"
                + "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m21.tif\">"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"true\" storeExecuteResponse=\"false\">"
                + "<wps:Output asReference=\"true\" encoding=\"base64\"  mimeType=\"image/tiff\" >"
                + "<ows:Identifier>result</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("Status"));
        AllTestsIT.checkReferenceBinaryResultBase64(response);
    }

    @Test
    public void testExecutePOSTValueComplexBinarySynchronousBinaryOutputAsReferenceStatusNoEncoding() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTValueComplexBinarySynchronousBinaryOutputAsReferenceStatusNoEncoding");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.raster.AddRasterValues</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>dataset1</ows:Identifier>"
                + "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m21.tif\">"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "<wps:Input>"
                + "<ows:Identifier>dataset2</ows:Identifier>"
                + "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m21.tif\">"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"true\" storeExecuteResponse=\"false\">"
                + "<wps:Output asReference=\"true\" mimeType=\"image/tiff\" >"
                + "<ows:Identifier>result</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("Status"));
        AllTestsIT.checkReferenceBinaryResultDefault(response);
    }

    @Test
    public void testExecutePOSTValueComplexBinaryASynchronousBinaryOutputStoreBase64() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTValueComplexBinaryASynchronousBinaryOutputStoreBase64");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.raster.AddRasterValues</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>dataset1</ows:Identifier>"
                + "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m21.tif\">"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "<wps:Input>"
                + "<ows:Identifier>dataset2</ows:Identifier>"
                + "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m21.tif\">"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"false\" storeExecuteResponse=\"true\">"
                + "<wps:Output asReference=\"false\" encoding=\"base64\"  mimeType=\"image/tiff\" >"
                + "<ows:Identifier>result</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, not(containsString("ExceptionReport")));
        AllTestsIT.validateBinaryBase64Async(response);
    }

    @Test
    public void testExecutePOSTValueComplexBinaryASynchronousBinaryOutputStoreNoEncoding() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTValueComplexBinaryASynchronousBinaryOutputStoreNoEncoding");
        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.raster.AddRasterValues</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>dataset1</ows:Identifier>"
                + "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m21.tif\">"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "<wps:Input>"
                + "<ows:Identifier>dataset2</ows:Identifier>"
                + "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m21.tif\">"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"false\" storeExecuteResponse=\"true\">"
                + "<wps:Output asReference=\"false\" mimeType=\"image/tiff\" >"
                + "<ows:Identifier>result</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        String async = AllTestsIT.getAsyncDoc(response);
        assertThat(AllTestsIT.parseXML(async), is(not(nullValue())));
        assertThat(async, async, not(containsString("ExceptionReport")));
        assertThat(async, async, containsString("ProcessSucceeded"));
        assertThat(async, async, containsString(TIFF_MAGIC));
    }

    @Test
    public void testExecutePOSTValueComplexBinaryASynchronousBinaryOutputStoreReferenceBase64() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTValueComplexBinaryASynchronousBinaryOutputStoreReferenceBase64");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.raster.AddRasterValues</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>dataset1</ows:Identifier>"
                + "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m21.tif\">"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "<wps:Input>"
                + "<ows:Identifier>dataset2</ows:Identifier>"
                + "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m21.tif\">"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"false\" storeExecuteResponse=\"true\">"
                + "<wps:Output asReference=\"true\" encoding=\"base64\"  mimeType=\"image/tiff\" >"
                + "<ows:Identifier>result</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        String asynDoc = AllTestsIT.getAsyncDoc(response);
        assertThat(AllTestsIT.parseXML(asynDoc), is(not(nullValue())));
        assertThat(asynDoc, asynDoc, not(containsString("ExceptionReport")));
        AllTestsIT.checkReferenceBinaryResultBase64(asynDoc);
    }

    @Test
    public void testExecutePOSTValueComplexBinaryASynchronousBinaryOutputStoreReferenceNoEncoding() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTValueComplexBinaryASynchronousBinaryOutputStoreReferenceNoEncoding");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.raster.AddRasterValues</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>dataset1</ows:Identifier>"
                + "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m21.tif\">"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "<wps:Input>"
                + "<ows:Identifier>dataset2</ows:Identifier>"
                + "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m21.tif\">"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"false\" storeExecuteResponse=\"true\">"
                + "<wps:Output asReference=\"true\"  mimeType=\"image/tiff\" >"
                + "<ows:Identifier>result</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        String asynDoc = AllTestsIT.getAsyncDoc(response);
        AllTestsIT.checkReferenceBinaryResultDefault(asynDoc);
    }

    @Test
    public void testExecutePOSTValueComplexBinaryASynchronousBinaryOutputStoreStatusBase64() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTValueComplexBinaryASynchronousBinaryOutputStoreStatusBase64");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.raster.AddRasterValues</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>dataset1</ows:Identifier>"
                + "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m21.tif\">"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "<wps:Input>"
                + "<ows:Identifier>dataset2</ows:Identifier>"
                + "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m21.tif\">"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"true\" storeExecuteResponse=\"true\">"
                + "<wps:Output asReference=\"false\" encoding=\"base64\"  mimeType=\"image/tiff\" >"
                + "<ows:Identifier>result</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("Status"));
        AllTestsIT.validateBinaryBase64Async(response);
    }

    @Test
    public void testExecutePOSTValueComplexBinaryASynchronousBinaryOutputStoreStatusNoEncoding() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTValueComplexBinaryASynchronousBinaryOutputStoreStatusNoEncoding");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.raster.AddRasterValues</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>dataset1</ows:Identifier>"
                + "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m21.tif\">"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "<wps:Input>"
                + "<ows:Identifier>dataset2</ows:Identifier>"
                + "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m21.tif\">"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"true\" storeExecuteResponse=\"true\">"
                + "<wps:Output asReference=\"false\"  mimeType=\"image/tiff\" >"
                + "<ows:Identifier>result</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        String asyncDoc = AllTestsIT.getAsyncDoc(response);
        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(asyncDoc, asyncDoc, containsString("ProcessSucceeded"));
        assertThat(asyncDoc, asyncDoc, containsString(TIFF_MAGIC));
    }

    @Test
    public void testExecutePOSTValueComplexBinaryASynchronousBinaryOutputRawBase64() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTValueComplexBinaryASynchronousBinaryOutputRawBase64");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.raster.AddRasterValues</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>dataset1</ows:Identifier>"
                + "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m21.tif\">"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "<wps:Input>"
                + "<ows:Identifier>dataset2</ows:Identifier>"
                + "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m21.tif\">"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"true\" storeExecuteResponse=\"true\">"
                + "<wps:Output asReference=\"true\" encoding=\"base64\"  mimeType=\"image/tiff\" >"
                + "<ows:Identifier>result</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("Status"));
        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        String refDoc = AllTestsIT.getAsyncDoc(response);
        AllTestsIT.checkReferenceBinaryResultBase64(refDoc);
    }

    @Test
    public void testExecutePOSTValueComplexBinaryASynchronousBinaryOutputReferenceStoreStatusNoEncoding() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTValueComplexBinaryASynchronousBinaryOutputReferenceStoreStatusNoEncoding");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.raster.AddRasterValues</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>dataset1</ows:Identifier>"
                + "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m21.tif\">"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "<wps:Input>"
                + "<ows:Identifier>dataset2</ows:Identifier>"
                + "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m21.tif\">"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"true\" storeExecuteResponse=\"true\">"
                + "<wps:Output asReference=\"true\"  mimeType=\"image/tiff\" >"
                + "<ows:Identifier>result</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("Status"));
        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        String refDoc = AllTestsIT.getAsyncDoc(response);
        AllTestsIT.checkReferenceBinaryResultDefault(refDoc);
    }

    @Test
    public void testExecutePOSTValueComplexBinarySynchronousBinaryOutputRawbase64() throws IOException {
        System.out.println("\nRunning testExecutePOSTValueComplexBinarySynchronousBinaryOutputRawbase64");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.raster.AddRasterValues</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>dataset1</ows:Identifier>"
                + "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m21.tif\">"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "<wps:Input>"
                + "<ows:Identifier>dataset2</ows:Identifier>"
                + "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m21.tif\">"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:RawDataOutput encoding=\"base64\"  mimeType=\"image/tiff\" >"
                + "<ows:Identifier>result</ows:Identifier>"
                + "</wps:RawDataOutput>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";

        InputStream stream = PostClient.sendRequestForInputStream(url, payload);

        GeotiffParser parser = new GeotiffParser();
        IData iData = parser.parseBase64(stream, "image/tiff", null);
        assertThat(iData, is(not(nullValue())));
    }

    @Test
    public void testExecutePOSTValueComplexBinarySynchronousBinaryOutputRawNoEncoding() throws IOException {
        System.out.println("\nRunning testExecutePOSTValueComplexBinarySynchronousBinaryOutputRawNoEncoding");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0"
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.raster.AddRasterValues</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>dataset1</ows:Identifier>"
                + "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m21.tif\">"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "<wps:Input>"
                + "<ows:Identifier>dataset2</ows:Identifier>"
                + "<wps:Reference mimeType=\"image/tiff\" xlink:href=\"http://52north.org/files/geoprocessing/Testdata/elev_srtm_30m21.tif\">"
                + "</wps:Reference>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:RawDataOutput  mimeType=\"image/tiff\" >"
                + "<ows:Identifier>result</ows:Identifier>"
                + "</wps:RawDataOutput>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";

        InputStream stream = PostClient.sendRequestForInputStream(url, payload);
        GeotiffParser parser = new GeotiffParser();
        IData iData = parser.parse(stream, "image/tiff", null);
        assertThat(iData, is(not(nullValue())));
    }

    @Test
    public void testExecutePOSTinlineLiteralDataSynchronousLiteralOutput() throws IOException {
        System.out.println("\nRunning testExecutePOSTinlineLiteralDataSynchronousLiteralOutput");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 "
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.test.DummyTestClass</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>LiteralInputData</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:LiteralData>007</wps:LiteralData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"false\" storeExecuteResponse=\"false\">"
                + "<wps:Output asReference=\"false\">"
                + "<ows:Identifier>LiteralOutputData</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("007"));
    }

    @Test
    public void testExecutePOSTinlineLiteralDataSynchronousLiteralOutputStatus() throws IOException {
        System.out.println("\nRunning testExecutePOSTinlineLiteralDataSynchronousLiteralOutputStatus");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 "
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.test.DummyTestClass</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>LiteralInputData</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:LiteralData>007</wps:LiteralData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"true\" storeExecuteResponse=\"false\">"
                + "<wps:Output asReference=\"false\">"
                + "<ows:Identifier>LiteralOutputData</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("Status"));
        assertThat(response, response, containsString("007"));
    }
    
    @Test
    public void testExecutePOSTinlineLiteralDataASynchronousLiteralOutputStoreStatus() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTinlineLiteralDataASynchronousLiteralOutputStoreStatus");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 "
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.test.LongRunningDummyTestClass</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>LiteralInputData</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:LiteralData>007</wps:LiteralData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"true\" storeExecuteResponse=\"true\">"
                + "<wps:Output asReference=\"true\">"
                + "<ows:Identifier>LiteralOutputData</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        
        String response = PostClient.sendRequest(url, payload);
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("Status"));
        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        String refDoc = AllTestsIT.getAsyncDoc(response);
        assertThat(refDoc, refDoc, containsString("Status"));
        assertThat(refDoc, refDoc, containsString("007"));
    }

    @Test
    public void testExecutePOSTinlineLiteralDataASynchronousLiteralOutputStore() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTinlineLiteralDataASynchronousLiteralOutputStore");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 "
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.test.DummyTestClass</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>LiteralInputData</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:LiteralData>007</wps:LiteralData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"false\" storeExecuteResponse=\"true\">"
                + "<wps:Output asReference=\"false\">"
                + "<ows:Identifier>LiteralOutputData</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, response, not(containsString("ExceptionReport")));
        
        String asyncDoc = AllTestsIT.getAsyncDoc(response);
        assertThat(asyncDoc, asyncDoc, containsString("007"));
    }

    @Test
    public void testExecutePOSTinlineLiteralDataSynchronousLiteralOutputRaw() throws IOException {
        System.out.println("\nRunning testExecutePOSTinlineLiteralDataSynchronousLiteralOutputRaw");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 "
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.test.DummyTestClass</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>LiteralInputData</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:LiteralData>007</wps:LiteralData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:RawDataOutput>"
                + "<ows:Identifier>LiteralOutputData</ows:Identifier>"
                + "</wps:RawDataOutput>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, not(containsString("Response")));
        assertThat(response, response, containsString("007"));
    }

    @Test
    public void testExecutePOSTinlineBBOXDataSynchronousBBOXOutput() throws IOException {
        System.out.println("\nRunning testExecutePOSTinlineBBOXDataSynchronousBBOXOutput");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 "
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.test.DummyTestClass</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>BBOXInputData</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:BoundingBoxData crs=\"EPSG:4326\">"
                + "<ows:LowerCorner>46.75 13.05</ows:LowerCorner>"
                + "<ows:UpperCorner>46.85 13.25</ows:UpperCorner>"
                + "</wps:BoundingBoxData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"false\" storeExecuteResponse=\"false\">"
                + "<wps:Output asReference=\"false\">"
                + "<ows:Identifier>BBOXOutputData</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("46.75 13.05"));
        assertThat(response, response, containsString("EPSG:4326"));
    }

    @Test
    public void testExecutePOSTinlineBBOXDataSynchronousBBOXOutputStatus() throws IOException {
        System.out.println("\nRunning testExecutePOSTinlineBBOXDataSynchronousBBOXOutputStatus");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 "
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.test.DummyTestClass</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>BBOXInputData</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:BoundingBoxData crs=\"EPSG:4326\">"
                + "<ows:LowerCorner>46.75 13.05</ows:LowerCorner>"
                + "<ows:UpperCorner>46.85 13.25</ows:UpperCorner>"
                + "</wps:BoundingBoxData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"true\" storeExecuteResponse=\"false\">"
                + "<wps:Output asReference=\"false\">"
                + "<ows:Identifier>BBOXOutputData</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("Status"));
        assertThat(response, response, containsString("46.75 13.05"));
        assertThat(response, response, containsString("EPSG:4326"));
    }

    @Test
    public void testExecutePOSTinlineBBOXDataASynchronousBBOXOutputStore() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTinlineBBOXDataASynchronousBBOXOutputStore");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 "
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.test.DummyTestClass</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>BBOXInputData</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:BoundingBoxData crs=\"EPSG:4326\">"
                + "<ows:LowerCorner>46.75 13.05</ows:LowerCorner>"
                + "<ows:UpperCorner>46.85 13.25</ows:UpperCorner>"
                + "</wps:BoundingBoxData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"false\" storeExecuteResponse=\"true\">"
                + "<wps:Output asReference=\"false\">"
                + "<ows:Identifier>BBOXOutputData</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        assertThat(response, response, not(containsString("ExceptionReport")));
        String asyncDoc = AllTestsIT.getAsyncDoc(response);
        assertThat(asyncDoc, asyncDoc, containsString("46.75 13.05"));
        assertThat(asyncDoc, asyncDoc, containsString("EPSG:4326"));
    }

    @Test
    public void testExecutePOSTinlineBBOXDataASynchronousBBOXOutputStoreStatus() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTinlineBBOXDataASynchronousBBOXOutputStoreStatus");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 "
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.test.DummyTestClass</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>BBOXInputData</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:BoundingBoxData crs=\"EPSG:4326\">"
                + "<ows:LowerCorner>46.75 13.05</ows:LowerCorner>"
                + "<ows:UpperCorner>46.85 13.25</ows:UpperCorner>"
                + "</wps:BoundingBoxData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"false\" storeExecuteResponse=\"true\">"
                + "<wps:Output asReference=\"false\">"
                + "<ows:Identifier>BBOXOutputData</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("Status"));
        
        String asyncDoc = AllTestsIT.getAsyncDoc(response);
        assertThat(asyncDoc, asyncDoc, containsString("46.75 13.05"));
        assertThat(asyncDoc, asyncDoc, containsString("EPSG:4326"));
    }

    @Test
    public void testExecutePOSTinlineBBOXDataSynchronousBBOXOutputRaw() throws IOException {
        System.out.println("\nRunning testExecutePOSTinlineBBOXDataSynchronousBBOXOutputRaw");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 "
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.test.DummyTestClass</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>BBOXInputData</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:BoundingBoxData crs=\"EPSG:4326\">"
                + "<ows:LowerCorner>46.75 13.05</ows:LowerCorner>"
                + "<ows:UpperCorner>46.85 13.25</ows:UpperCorner>"
                + "</wps:BoundingBoxData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:RawDataOutput>"
                + "<ows:Identifier>BBOXOutputData</ows:Identifier>"
                + "</wps:RawDataOutput>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, not(containsString("Response")));
        
        assertThat(response, response, containsString("46.75 13.05"));
        assertThat(response, response, containsString("EPSG:4326"));
    }

    @Test
    public void testExecutePOSTinlineBBOXDataSynchronousBBOXOutputNoEPSG() throws IOException {
        System.out.println("\nRunning testExecutePOSTinlineBBOXDataSynchronousBBOXOutputNoEPSG");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 "
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.test.DummyTestClass</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>BBOXInputData</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:BoundingBoxData>"
                + "<ows:LowerCorner>46.75 13.05</ows:LowerCorner>"
                + "<ows:UpperCorner>46.85 13.25</ows:UpperCorner>"
                + "</wps:BoundingBoxData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"false\" storeExecuteResponse=\"false\">"
                + "<wps:Output asReference=\"false\">"
                + "<ows:Identifier>BBOXOutputData</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, not(containsString("EPSG")));
        assertThat(response, response, containsString("46.75 13.05"));
    }

    @Test
    public void testExecutePOSTinlineBBOXDataSynchronousBBOXOutputStatusNoEPSG() throws IOException {
        System.out.println("\nRunning testExecutePOSTinlineBBOXDataSynchronousBBOXOutputStatusNoEPSG");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 "
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.test.DummyTestClass</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>BBOXInputData</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:BoundingBoxData crs=\"EPSG:4326\">"
                + "<ows:LowerCorner>46.75 13.05</ows:LowerCorner>"
                + "<ows:UpperCorner>46.85 13.25</ows:UpperCorner>"
                + "</wps:BoundingBoxData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"true\" storeExecuteResponse=\"false\">"
                + "<wps:Output asReference=\"false\">"
                + "<ows:Identifier>BBOXOutputData</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("46.75 13.05"));
        assertThat(response, response, containsString("Status"));
        assertThat(response, response, containsString("EPSG"));
    }

    @Test
    public void testExecutePOSTinlineBBOXDataASynchronousBBOXOutputStoreNoEPSG() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTinlineBBOXDataASynchronousBBOXOutputStoreNoEPSG");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 "
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.test.DummyTestClass</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>BBOXInputData</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:BoundingBoxData>"
                + "<ows:LowerCorner>46.75 13.05</ows:LowerCorner>"
                + "<ows:UpperCorner>46.85 13.25</ows:UpperCorner>"
                + "</wps:BoundingBoxData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"false\" storeExecuteResponse=\"true\">"
                + "<wps:Output asReference=\"false\">"
                + "<ows:Identifier>BBOXOutputData</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        assertThat(response, response, not(containsString("ExceptionReport")));

        String asyncDoc = AllTestsIT.getAsyncDoc(response);
        assertThat(asyncDoc, asyncDoc, containsString("Status"));
        assertThat(asyncDoc, asyncDoc, not(containsString("EPSG")));
    }

    @Test
    public void testExecutePOSTinlineBBOXDataASynchronousBBOXOutputStoreStatusNoEPSG() throws IOException, ParserConfigurationException, SAXException {
        System.out.println("\nRunning testExecutePOSTinlineBBOXDataASynchronousBBOXOutputStoreStatusNoEPSG");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 "
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.test.DummyTestClass</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>BBOXInputData</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:BoundingBoxData>"
                + "<ows:LowerCorner>46.75 13.05</ows:LowerCorner>"
                + "<ows:UpperCorner>46.85 13.25</ows:UpperCorner>"
                + "</wps:BoundingBoxData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:ResponseDocument status=\"false\" storeExecuteResponse=\"true\">"
                + "<wps:Output asReference=\"false\">"
                + "<ows:Identifier>BBOXOutputData</ows:Identifier>"
                + "</wps:Output>"
                + "</wps:ResponseDocument>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, containsString("Status"));
        assertThat(response, response, not(containsString("EPSG")));

        String asyncDoc = AllTestsIT.getAsyncDoc(response);
        assertThat(asyncDoc, asyncDoc, containsString("46.75 13.05"));
    }

    @Test
    public void testExecutePOSTinlineBBOXDataSynchronousBBOXOutputRawNoEPSG() throws IOException {
        System.out.println("\nRunning testExecutePOSTinlineBBOXDataSynchronousBBOXOutputRawNoEPSG");

        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 "
                + "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">"
                + "<ows:Identifier>org.n52.wps.server.algorithm.test.DummyTestClass</ows:Identifier>"
                + "<wps:DataInputs>"
                + "<wps:Input>"
                + "<ows:Identifier>BBOXInputData</ows:Identifier>"
                + "<ows:Title>Distance which people will walk to get to a playground.</ows:Title>"
                + "<wps:Data>"
                + "<wps:BoundingBoxData>"
                + "<ows:LowerCorner>46.75 13.05</ows:LowerCorner>"
                + "<ows:UpperCorner>46.85 13.25</ows:UpperCorner>"
                + "</wps:BoundingBoxData>"
                + "</wps:Data>"
                + "</wps:Input>"
                + "</wps:DataInputs>"
                + "<wps:ResponseForm>"
                + "<wps:RawDataOutput>"
                + "<ows:Identifier>BBOXOutputData</ows:Identifier>"
                + "</wps:RawDataOutput>"
                + "</wps:ResponseForm>"
                + "</wps:Execute>";
        String response = PostClient.sendRequest(url, payload);
        
        assertThat(response, response, not(containsString("ExceptionReport")));
        assertThat(response, response, not(containsString("Response")));
        assertThat(response, response, not(containsString("EPSG")));
        assertThat(response, response, containsString("46.75 13.05"));
    }
}
