/**
 * ﻿Copyright (C) 2007
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */
package org.n52.wps.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import java.io.IOException;

import org.apache.xmlbeans.XmlException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n52.wps.commons.WPSConfig;

public class ExecuteKvpIT {

    private static String url;

    @BeforeClass
    public static void beforeClass() throws XmlException, IOException {
        url = AllTestsIT.getURL();
        WPSConfig.forceInitialization("src/main/webapp/config/wps_config.xml");
    }
    
    @Test
    public void testExecuteKVPSynchronousLiteralDataReponseDoc() throws IOException {
        System.out.println("\nRunning testExecuteKVPSynchronousLiteralDataReponseDoc");
        
        String getURL = ExecuteKvpIT.url + "?Service=WPS&Request=Execute&Version=1.0.0&Identifier=org.n52.wps.server.algorithm.test.DummyTestClass&DataInputs=LiteralInputData=seventy@datatype=xs%3Astring@uom=meter&ResponseDocument=LiteralOutputData";
        
        String response = GetClient.sendRequest(getURL);
    	
        assertThat(response, response, not(containsString("ExceptionReport")));
    	assertThat(response, response, containsString("seventy"));
    	assertThat(response, response, containsString("uom=\"meter\""));
    }
    
    @Test
    public void testExecuteKVPSynchronousLiteralDataRawData() throws IOException {
    	System.out.println("\nRunning testExecuteKVPSynchronousLiteralDataRawData");
    	
    	String getURL = ExecuteKvpIT.url + "?Service=WPS&Request=Execute&Version=1.0.0&Identifier=org.n52.wps.server.algorithm.test.DummyTestClass&DataInputs=LiteralInputData=seventy@datatype=xs%3Astring@uom=meter&RawDataOutput=LiteralOutputData";
    	
    	String response = GetClient.sendRequest(getURL);
    	
    	assertThat(response, response, not(containsString("ExceptionReport")));
    	assertThat(response, response, containsString("seventy"));       
    }
    
    @Test
    public void testExecuteKVPSynchronousComplexDataReponseDoc() throws IOException {
    	System.out.println("\nRunning testExecuteKVPSynchronousComplexDataReponseDoc");
    	
    	String getURL = ExecuteKvpIT.url + "?Service=WPS&Request=Execute&Version=1.0.0&Identifier=org.n52.wps.server.algorithm.JTSConvexHullAlgorithm&DataInputs=data=LINESTRING%28848383.15654512%206793127.8657339,848440.48431633%206793777.5804742,849137.97219934%206793739.3619601,849004.20739986%206793414.5045899%29@mimeType=application/wkt&ResponseDocument=result";
    	
    	String response = GetClient.sendRequest(getURL);
    	
    	assertThat(response, response, not(containsString("ExceptionReport")));
    	assertThat(response, response, containsString("POLYGON"));       
    }
    
    @Test
    public void testExecuteKVPSynchronousComplexDataRawData() throws IOException {
    	System.out.println("\nRunning testExecuteKVPSynchronousComplexDataRawData");
    	
    	String getURL = ExecuteKvpIT.url + "?Service=WPS&Request=Execute&Version=1.0.0&Identifier=org.n52.wps.server.algorithm.JTSConvexHullAlgorithm&DataInputs=data=LINESTRING%28848383.15654512%206793127.8657339,848440.48431633%206793777.5804742,849137.97219934%206793739.3619601,849004.20739986%206793414.5045899%29@mimeType=application/wkt&RawDataOutput=result";
    	
    	String response = GetClient.sendRequest(getURL);
    	
    	assertThat(response, response, not(containsString("ExceptionReport")));
    	assertThat(response, response, containsString("POLYGON"));       
    }
    
    @Test
    public void testExecuteKVPSynchronousBBoxDataReponseDoc() throws IOException {
    	System.out.println("\nRunning testExecuteKVPSynchronousBBoxDataReponseDoc");
    	
    	String getURL = ExecuteKvpIT.url + "?Service=WPS&Request=Execute&Version=1.0.0&Identifier=org.n52.wps.server.algorithm.test.DummyTestClass&DataInputs=BBOXInputData=46,102,47,103,urn%3Aogc%3Adef%3Acrs%3AEPSG%3A6.6%3A4326,2&ResponseDocument=BBOXOutputData";
    	
    	String response = GetClient.sendRequest(getURL);
    	
    	assertThat(response, response, not(containsString("ExceptionReport")));
    	
    	String expectedResult = "BoundingBoxData";
    	
    	String expectedResult2 = "crs=\"EPSG:4326\"";
    	
    	String expectedResult3 = "dimensions=\"2\"";
    	
    	String expectedResult4 = "LowerCorner";
    	
    	String expectedResult5 = "UpperCorner";
    	
    	String expectedResult6 = "46.0 102.0";
    	
    	String expectedResult7 = "47.0 103.0";
    	
    	assertThat(response, response, containsString(expectedResult));       
    	assertThat(response, response, containsString(expectedResult2));       
    	assertThat(response, response, containsString(expectedResult3));       
    	assertThat(response, response, containsString(expectedResult4));       
    	assertThat(response, response, containsString(expectedResult5));       
    	assertThat(response, response, containsString(expectedResult6));       
    	assertThat(response, response, containsString(expectedResult7));
    }
    
    @Test
    public void testExecuteKVPSynchronousBBoxDataRawData() throws IOException {
    	System.out.println("\nRunning testExecuteKVPSynchronousBBoxDataRawData");
    	
    	String getURL = ExecuteKvpIT.url + "?Service=WPS&Request=Execute&Version=1.0.0&Identifier=org.n52.wps.server.algorithm.test.DummyTestClass&DataInputs=BBOXInputData=46,102,47,103,urn%3Aogc%3Adef%3Acrs%3AEPSG%3A6.6%3A4326,2&RawDataOutput=BBOXOutputData";
    	
    	String response = GetClient.sendRequest(getURL);
    	
    	String expectedResult = "<wps:BoundingBoxData xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" crs=\"EPSG:4326\" dimensions=\"2\">"
				+ "<ows:LowerCorner xmlns:ows=\"http://www.opengis.net/ows/1.1\">46.0 102.0</ows:LowerCorner>"
				+ "<ows:UpperCorner xmlns:ows=\"http://www.opengis.net/ows/1.1\">47.0 103.0</ows:UpperCorner>"
				+ "</wps:BoundingBoxData>";
    	
    	assertThat(response, response, not(containsString("ExceptionReport")));
    	assertThat(response, response, containsString(expectedResult));       
    }
    
    @Test
    public void testExecuteKVPSynchronousComplexDataReferenceResponseDoc() throws IOException {
    	System.out.println("\nRunning testExecuteKVPSynchronousComplexDataReferenceResponseDoc");
    	
    	String getURL = ExecuteKvpIT.url + "?Service=WPS&Request=Execute&Version=1.0.0&Identifier=org.n52.wps.server.algorithm.SimpleBufferAlgorithm&DataInputs=data=@href=http%3A%2F%2Fgeoprocessing.demo.52north.org%3A8080%2Fgeoserver%2Fows%3Fservice%3DWFS%26version%3D1.0.0%26request%3DGetFeature%26typeName%3Dtopp%3Atasmania_roads%26maxFeatures%3D50@mimeType=text/xml@schema=http%3A%2F%2Fschemas.opengis.net%2Fgml%2F2.1.2%2Ffeature.xsd;width=0.05&ResponseDocument=result";
    	
    	String response = GetClient.sendRequest(getURL);
    	
    	String expectedResult = "ProcessSucceeded";
    	String expectedResult2 = "result";
    	String expectedResult3 = "FeatureCollection";
    	
    	assertThat(response, response, not(containsString("ExceptionReport")));
    	assertThat(response, response, containsString(expectedResult));       
    	assertThat(response, response, containsString(expectedResult2));     
    	assertThat(response, response, containsString(expectedResult3));     
    }
    
    @Test
    public void testExecuteKVPSynchronousComplexDataReferenceResponseDocSchemaMimeType() throws IOException {
    	System.out.println("\nRunning testExecuteKVPSynchronousComplexDataReferenceResponseDoc");
    	
    	String getURL = ExecuteKvpIT.url + "?Service=WPS&Request=Execute&Version=1.0.0&Identifier=org.n52.wps.server.algorithm.SimpleBufferAlgorithm&DataInputs=data=@href=http%3A%2F%2Fgeoprocessing.demo.52north.org%3A8080%2Fgeoserver%2Fows%3Fservice%3DWFS%26version%3D1.0.0%26request%3DGetFeature%26typeName%3Dtopp%3Atasmania_roads%26maxFeatures%3D50@mimeType=text/xml@schema=http%3A%2F%2Fschemas.opengis.net%2Fgml%2F2.1.2%2Ffeature.xsd;width=0.05&ResponseDocument=result@mimeType=text/xml@schema=http%3A%2F%2Fschemas.opengis.net%2Fgml%2F2.1.2%2Ffeature.xsd";
    	
    	String response = GetClient.sendRequest(getURL);
    	
    	String expectedResult = "ProcessSucceeded";
    	String expectedResult2 = "result";
    	String expectedResult3 = "FeatureCollection";
    	String expectedResult4 = "schema=\"http://schemas.opengis.net/gml/2.1.2/feature.xsd\"";
    	String expectedResult5 = "mimeType=\"text/xml\"";
    	
    	assertThat(response, response, not(containsString("ExceptionReport")));
    	assertThat(response, response, containsString(expectedResult));       
    	assertThat(response, response, containsString(expectedResult2));     
    	assertThat(response, response, containsString(expectedResult3));     
    	assertThat(response, response, containsString(expectedResult4));     
    	assertThat(response, response, containsString(expectedResult5));
    }
    
    @Test
    public void testExecuteKVPSynchronousComplexDataReferenceResponseDocMimeTypeEncodingBase64() throws IOException {
    	System.out.println("\nRunning testExecuteKVPSynchronousComplexDataReferenceResponseDocMimeTypeEncodingBase64");
    	
    	String getURL = ExecuteKvpIT.url + "?Service=WPS&Request=Execute&Version=1.0.0&Identifier=org.n52.wps.server.algorithm.test.MultiReferenceBinaryInputAlgorithm&DataInputs=data=@href=http%3A%2F%2F52north.org%2Ffiles%2Fgeoprocessing%2FTestdata%2Felev_srtm_30m21.tif@mimeType=image/tiff;data=@href=http%3A%2F%2F52north.org%2Ffiles%2Fgeoprocessing%2FTestdata%2Felev_srtm_30m21.tif@mimeType=image/tiff&ResponseDocument=result@mimeType=image/tiff@encoding=base64";
    	
    	String response = GetClient.sendRequest(getURL);
    	
    	String expectedResult = "ProcessSucceeded";
    	String expectedResult2 = "result";
    	String expectedResult3 = "encoding=\"base64\"";
    	String expectedResult4 = " mimeType=\"image/tiff\"";
    	
    	assertThat(response, response, not(containsString("ExceptionReport")));
    	assertThat(response, response, containsString(expectedResult));       
    	assertThat(response, response, containsString(expectedResult2));     
    	assertThat(response, response, containsString(expectedResult3));     
    	assertThat(response, response, containsString(expectedResult4)); 
    	AllTestsIT.checkInlineResultBase64(response);
    }
    
    @Test
    public void testExecuteKVPSynchronousComplexDataReferenceRawData() throws IOException {
    	System.out.println("\nRunning testExecuteKVPSynchronousComplexDataReferenceRawData");
    	
    	String getURL = ExecuteKvpIT.url + "?Service=WPS&Request=Execute&Version=1.0.0&Identifier=org.n52.wps.server.algorithm.SimpleBufferAlgorithm&DataInputs=data=@href=http%3A%2F%2Fgeoprocessing.demo.52north.org%3A8080%2Fgeoserver%2Fows%3Fservice%3DWFS%26version%3D1.0.0%26request%3DGetFeature%26typeName%3Dtopp%3Atasmania_roads%26maxFeatures%3D50@mimeType=text/xml@schema=http%3A%2F%2Fschemas.opengis.net%2Fgml%2F2.1.2%2Ffeature.xsd;width=0.05&RawDataOutput=result";
    	
    	String response = GetClient.sendRequest(getURL);
    	
    	String expectedResult = "FeatureCollection";
    	
    	assertThat(response, response, not(containsString("ExceptionReport")));
    	assertThat(response, response, containsString(expectedResult));         
    }
}
