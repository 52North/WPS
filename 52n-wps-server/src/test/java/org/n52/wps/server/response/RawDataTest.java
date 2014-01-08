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
package org.n52.wps.server.response;

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
import org.n52.wps.io.data.binding.bbox.GTReferenceEnvelope;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.algorithm.test.DummyTestClass;

/**
 * This class is for testing RawData output.
 * 
 * @author Benjamin Pross (bpross-52n)
 *
 */
public class RawDataTest {

	IAlgorithm algorithm;
	ProcessDescriptionType processDescription;
	String identifier;
	
    @BeforeClass
    public static void setUpClass() {
        try {
            WPSConfig.forceInitialization("../52n-wps-webapp/src/main/webapp/config/wps_config.xml");
        } catch (XmlException ex) {
            System.out.println(ex.getMessage());
        } catch (IOException ex) {
        	 System.out.println(ex.getMessage());
        }
    }
	
    @Before
    public void setUp(){    	
    	algorithm = new DummyTestClass();
    	processDescription = algorithm.getDescription();
    	identifier = algorithm.getWellKnownName();
    }
    
    @Test
    public void testBBoxRawDataOutputCRS(){
    	
    	IData envelope = new GTReferenceEnvelope(46,102,47,103, "EPSG:4326");
    	
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
    	
    	IData envelope = new GTReferenceEnvelope(46,102,47,103, null);
    	
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
