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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.binary.Base64;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n52.wps.io.data.GenericFileDataConstants;
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.datahandler.parser.GTBinZippedSHPParser;
import org.n52.wps.io.datahandler.parser.GeotiffParser;
import org.xml.sax.SAXException;

/**
 * 
 * To run this integration tests the GRASS 7 repository must be enabled and properly configured in the WPS config.
 *  
 */
public class GrassIT {

    private static String wpsUrl;

    @BeforeClass
    public static void beforeClass() {
        wpsUrl = AllTestsIT.getURL();
    }

    @Test
    public void decribeProcess() throws IOException, ParserConfigurationException, SAXException {
        String identifier = "v.buffer";
        String response = GetClient.sendRequest(wpsUrl, "Service=WPS&Request=DescribeProcess&Version=1.0.0&Identifier="
                + identifier);

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, not(containsString("ExceptionReport")));
        assertThat(response, containsString(identifier));
    }

    @Test
    public void capabilitiesContainProcess() throws IOException, ParserConfigurationException, SAXException {
        String response = GetClient.sendRequest(wpsUrl, "Service=WPS&Request=GetCapabilities");

        assertThat(AllTestsIT.parseXML(response), is(not(nullValue())));
        assertThat(response, not(containsString("ExceptionReport")));
        assertThat(response, containsString("v.buffer"));
    }
    
    @Test
    public void resultRawSHPIsBase64Encoded() throws IOException, ParserConfigurationException, SAXException, XmlException {
        
        URL resource = GrassIT.class.getResource("/Grass/v.buffer_request_out_shp_raw_base64.xml");
        XmlObject xmlPayload = XmlObject.Factory.parse(resource);

        String payload = xmlPayload.toString();
        String response = PostClient.sendRequest(wpsUrl, payload);
        assertThat(response, not(containsString("ExceptionReport")));

        assertTrue(Base64.isBase64(response));
    }
    
    @Test
    public void resultRawSHPIsNotBase64Encoded() throws XmlException, IOException {
        
        URL resource = GrassIT.class.getResource("/Grass/v.buffer_request_out_shp_raw.xml");
        XmlObject xmlPayload = XmlObject.Factory.parse(resource);

        String payload = xmlPayload.toString();
        InputStream response = PostClient.sendRequestForInputStream(wpsUrl, payload);
        
        GTBinZippedSHPParser gtBinZippedSHPParser = new GTBinZippedSHPParser();
        
        GTVectorDataBinding gtVectorDataBinding = gtBinZippedSHPParser.parse(response, GenericFileDataConstants.MIME_TYPE_ZIPPED_SHP, null);
        
        assertTrue(gtVectorDataBinding.getPayload() != null);
        assertTrue(gtVectorDataBinding.getPayload().size() != 0);
    }
    
    @Test
    public void resultEmbeddedSHPIsBase64Encoded() throws IOException, ParserConfigurationException, SAXException, XmlException {
    	
    	URL resource = GrassIT.class.getResource("/Grass/v.buffer_request_out_shp_doc_base64.xml");
    	XmlObject xmlPayload = XmlObject.Factory.parse(resource);
    	
    	String payload = xmlPayload.toString();
    	String response = PostClient.sendRequest(wpsUrl, payload);
    	assertThat(response, not(containsString("ExceptionReport")));
    	
    	AllTestsIT.checkInlineResultBase64(response);
    }
	
	@Test
	public void resultRawGeoTiffIsBase64Encoded() throws IOException,
	ParserConfigurationException, SAXException, XmlException {
		
		URL resource = GrassIT.class
				.getResource("/Grass/r.resample_request_out_tiff_raw_base64.xml");
		XmlObject xmlPayload = XmlObject.Factory.parse(resource);
		
		String payload = xmlPayload.toString();
		String response = PostClient.sendRequest(wpsUrl, payload);
		assertThat(response, not(containsString("ExceptionReport")));
		
		assertTrue(Base64.isBase64(response));
	}
    
    @Test
    public void resultRawGeoTiffIsNotBase64Encoded() throws XmlException, IOException {
    	
    	URL resource = GrassIT.class.getResource("/Grass/r.resample_request_out_tiff_raw.xml");
    	XmlObject xmlPayload = XmlObject.Factory.parse(resource);
    	
    	String payload = xmlPayload.toString();
    	InputStream response = PostClient.sendRequestForInputStream(wpsUrl, payload);
    	
    	GeotiffParser geotiffParser = new GeotiffParser();

    	GTRasterDataBinding gtRasterDataBinding = geotiffParser.parse(response, "image/tiff", null);
    	
    	assertTrue(gtRasterDataBinding.getPayload() != null);
    	assertTrue(gtRasterDataBinding.getPayload().getEnvelope() != null);
    	assertTrue(gtRasterDataBinding.getPayload().getEnvelope().getLowerCorner().getCoordinate()[0] == 630000.0);
    }

	@Test
	public void resultEmbeddedGeoTiffIsBase64Encoded() throws IOException,
			ParserConfigurationException, SAXException, XmlException {

		URL resource = GrassIT.class
				.getResource("/Grass/r.resample_request_out_tiff_doc_base64.xml");
		XmlObject xmlPayload = XmlObject.Factory.parse(resource);

		String payload = xmlPayload.toString();
		String response = PostClient.sendRequest(wpsUrl, payload);
		assertThat(response, not(containsString("ExceptionReport")));

		AllTestsIT.checkInlineResultBase64(response);
	}
}
