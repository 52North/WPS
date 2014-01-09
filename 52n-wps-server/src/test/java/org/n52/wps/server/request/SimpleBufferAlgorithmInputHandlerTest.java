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
package org.n52.wps.server.request;

import java.io.File;
import java.io.IOException;
import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.InputType;
import org.apache.xmlbeans.XmlException;
import org.geotools.feature.DefaultFeatureCollection;
import static org.hamcrest.Matchers.*;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before; 
import org.junit.BeforeClass;
import org.junit.Test;
import org.n52.wps.io.data.IData;
import org.n52.wps.server.ExceptionReport;

/**
 *
 * @author isuftin
 */
public class SimpleBufferAlgorithmInputHandlerTest {
        
    private static String sampleFileName = null;
    private static File sampleFile = null;
    private static ExecuteDocument execDoc = null;
    private static InputType[] inputArray = null;
    private static File projectRoot = null;
    
    @BeforeClass
    public static void setupClass() throws XmlException, IOException {
        sampleFileName = "src/test/resources/SimpleBufferAlgorithm.xml";
        sampleFile = new File(sampleFileName);
        WPSConfigTestUtil.generateMockConfig(SimpleBufferAlgorithmInputHandlerTest.class, "/org/n52/wps/io/test/inputhandler/generator/wps_config.xml");

        execDoc = ExecuteDocument.Factory.parse(sampleFile);
        inputArray = execDoc.getExecute().getDataInputs().getInputArray();
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws XmlException, IOException {
        File f = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getFile());
        projectRoot = new File(f.getParentFile().getParentFile().getParent());
    }

    @After
    public void tearDown() {
    }

    @Test(expected = ExceptionReport.class)
    public void testInputHandlerInitializationWithIncorrectAlgorithmName() throws ExceptionReport {
        System.out.println("Testing testInputHandlerInitialization...");
        InputHandler instance = new InputHandler.Builder(inputArray, "this.algorithm.name.does.not.exist").build();
    }

    @Test(expected = ExceptionReport.class)
    public void testInputHandlerInitializationWithNullAlgorithmName() throws ExceptionReport {
        System.out.println("Testing testInputHandlerInitialization...");
        InputHandler instance = new InputHandler.Builder(inputArray, null).build();
    }

    @Test(expected = NullPointerException.class)
    public void testInputHandlerInitializationWithNullInputsArray() throws ExceptionReport {
        System.out.println("Testing testInputHandlerInitialization...");
        InputHandler instance = new InputHandler.Builder(null, "org.n52.wps.server.algorithm.SimpleBufferAlgorithm").build();
    }

    @Test
    public void testInputHandlerInitializationWithEmptyInputsArray() throws ExceptionReport {
        System.out.println("Testing testInputHandlerInitialization...");
        InputHandler instance = new InputHandler.Builder(new InputType[]{}, "org.n52.wps.server.algorithm.SimpleBufferAlgorithm").build();

        assertThat(instance, not(nullValue()));
        assertThat(instance.getParsedInputData().isEmpty(), is(true));
    }

    @Test
    public void testInputHandlerInitialization() throws ExceptionReport, XmlException, IOException {
        System.out.println("Testing testInputHandlerInitialization...");
        InputHandler instance = new InputHandler.Builder(inputArray, "org.n52.wps.server.algorithm.SimpleBufferAlgorithm").build();

        assertThat(instance, not(nullValue()));
    }

    @Test
    public void testGetParsedInputDataWithCorrectInput() throws ExceptionReport, XmlException, IOException {
        System.out.println("Testing testInputHandlerInitialization...");
        InputHandler instance = new InputHandler.Builder(inputArray, "org.n52.wps.server.algorithm.SimpleBufferAlgorithm").build();

        assertThat(instance.getParsedInputData().isEmpty(), is(false));
        assertThat(instance.getParsedInputData().size(), equalTo(2));
        assertThat(instance.getParsedInputData().keySet().size(), equalTo(2));
        assertThat(instance.getParsedInputData().keySet().toArray()[0].toString(), is(equalToIgnoringCase("width")));
        assertThat(instance.getParsedInputData().keySet().toArray()[1].toString(), is(equalToIgnoringCase("data")));
        assertThat(instance.getParsedInputData().get("data").size(), equalTo(1));
        assertThat(instance.getParsedInputData().get("width").size(), equalTo(1));
        
        IData width = instance.getParsedInputData().get("width").get(0);
        IData data = instance.getParsedInputData().get("data").get(0);
        
        assertThat(data, is(notNullValue()));
        assertThat(data.getSupportedClass().getName(), is(equalToIgnoringCase("org.geotools.feature.FeatureCollection")));
        assertThat(data.getPayload(), is(notNullValue()));
        assertThat(((DefaultFeatureCollection) data.getPayload()).getID(), is(equalToIgnoringCase("featureCollection")));
        assertThat(((DefaultFeatureCollection) data.getPayload()).getSchema().getTypeName(), is(equalToIgnoringCase("tasmania_roads")));
        assertThat(((DefaultFeatureCollection) data.getPayload()).getSchema().getAttributeCount(), equalTo(7));
        assertThat(((DefaultFeatureCollection) data.getPayload()).fids().size(), equalTo(14));
        assertThat(((DefaultFeatureCollection) data.getPayload()).fids().toArray()[0].toString(), is(equalToIgnoringCase("tasmania_roads.1")));
        assertThat(((DefaultFeatureCollection) data.getPayload()).fids().toArray()[13].toString(), is(equalToIgnoringCase("tasmania_roads.9")));
        assertThat(((DefaultFeatureCollection) data.getPayload()).getBounds().toString(), is(equalToIgnoringCase("ReferencedEnvelope[145.19754 : 148.27298000000002, -43.423512 : -40.852802]")));
        assertThat(((DefaultFeatureCollection) data.getPayload()).getBounds().getArea(), equalTo(7.906064362400054d));
        assertThat(((DefaultFeatureCollection) data.getPayload()).getBounds().getDimension(), equalTo(2));
        
        assertThat(width, is(notNullValue()));
        assertThat(((Double)width.getPayload()), equalTo(20.0d));
        
    }
}
