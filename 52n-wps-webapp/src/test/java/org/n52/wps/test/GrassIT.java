/**
 * ﻿Copyright (C) 2007 - 2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *       • Apache License, version 2.0
 *       • Apache Software License, version 1.0
 *       • GNU Lesser General Public License, version 3
 *       • Mozilla Public License, versions 1.0, 1.1 and 2.0
 *       • Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
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
