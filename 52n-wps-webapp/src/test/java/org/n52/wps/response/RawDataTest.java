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
package org.n52.wps.response;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.bbox.BoundingBoxData;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.algorithm.test.DummyTestClass;
import org.n52.wps.server.response.RawData;
import org.n52.wps.webapp.common.AbstractITClass;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * This class is for testing RawData output.
 *
 * @author Benjamin Pross (bpross-52n)
 *
 */
public class RawDataTest  extends AbstractITClass{

	IAlgorithm algorithm;
	ProcessDescriptionType processDescription;
	String identifier;

    @BeforeClass
    public static void setUpClass() {
//        try {
//            WPSConfig.forceInitialization("../52n-wps-webapp/src/main/webapp/WEB-INF/config/wps_config.xml");
//        } catch (XmlException ex) {
//            System.out.println(ex.getMessage());
//        } catch (IOException ex) {
//        	 System.out.println(ex.getMessage());
//        }
    }

    @Before
    public void setUp(){
    	algorithm = new DummyTestClass();
    	processDescription = algorithm.getDescription();
    	identifier = algorithm.getWellKnownName();
		MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    public void testBBoxRawDataOutputCRS(){

        IData envelope = new BoundingBoxData(
                   new double[] { 46, 102 },
                   new double[] { 47, 103 }, "EPSG:4326");

    	InputStream is;

    	try {
			RawData bboxRawData = new RawData(envelope, "BBOXOutputData", null, null, null, identifier, processDescription);

			is = bboxRawData.getAsStream();

			XmlObject bboxXMLObject = XmlObject.Factory.parse(is);

			assertTrue(bboxXMLObject != null);

			assertTrue(bboxXMLObject.getDomNode().getFirstChild().getNodeName().equals("wps:BoundingBoxData"));
		} catch (Exception e) {
			fail(e.getMessage());
		}
    }

    @Test
    public void testBBoxRawDataOutput(){

    	   IData envelope = new BoundingBoxData(
                   new double[] { 46, 102 },
                   new double[] { 47, 103 }, null);

    	InputStream is;

    	try {
			RawData bboxRawData = new RawData(envelope, "BBOXOutputData", null, null, null, identifier, processDescription);

			is = bboxRawData.getAsStream();

			XmlObject bboxXMLObject = XmlObject.Factory.parse(is);

			assertTrue(bboxXMLObject != null);

			assertTrue(bboxXMLObject.getDomNode().getFirstChild().getNodeName().equals("wps:BoundingBoxData"));
		} catch (Exception e) {
			fail(e.getMessage());
		}
    }

}
