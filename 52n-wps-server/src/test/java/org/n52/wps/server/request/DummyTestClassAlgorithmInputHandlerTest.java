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
import org.geotools.geometry.jts.ReferencedEnvelope;
import static org.hamcrest.Matchers.*;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n52.wps.server.ExceptionReport;

/**
 *
 * @author isuftin
 */
public class DummyTestClassAlgorithmInputHandlerTest {
        
    private static String sampleFileName = null;
    private static File sampleFile = null;
    private static ExecuteDocument execDoc = null;
    private static InputType[] inputArray = null;
    private static File projectRoot = null;

    @BeforeClass 
    public static void setupClass() {
        sampleFileName = "src/test/resources/DummyTestClass.xml";
        sampleFile = new File(sampleFileName);
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws XmlException, IOException {
        WPSConfigTestUtil.generateMockConfig(getClass(), "/org/n52/wps/io/test/inputhandler/generator/wps_config.xml");

        execDoc = ExecuteDocument.Factory.parse(sampleFile);
        inputArray = execDoc.getExecute().getDataInputs().getInputArray();

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
        InputHandler instance = new InputHandler.Builder(null, "org.n52.wps.server.algorithm.test.DummyTestClass").build();
    }

    @Test
    public void testInputHandlerInitializationWithEmptyInputsArray() throws ExceptionReport {
        System.out.println("Testing testInputHandlerInitialization...");
        InputHandler instance = new InputHandler.Builder(new InputType[]{}, "org.n52.wps.server.algorithm.test.DummyTestClass").build();

        assertThat(instance, not(nullValue()));
        assertThat(instance.getParsedInputData().isEmpty(), is(true));
    }

    @Test
    public void testInputHandlerInitialization() throws ExceptionReport, XmlException, IOException {
        System.out.println("Testing testInputHandlerInitialization...");
        InputHandler instance = new InputHandler.Builder(inputArray, "org.n52.wps.server.algorithm.test.DummyTestClass").build();

        assertThat(instance, not(nullValue()));
    }

    @Test
    public void testGetParsedInputDataWithCorrectInput() throws ExceptionReport, XmlException, IOException {
        System.out.println("Testing testInputHandlerInitialization...");
        InputHandler instance = new InputHandler.Builder(inputArray, "org.n52.wps.server.algorithm.test.DummyTestClass").build();

        assertThat(instance.getParsedInputData().isEmpty(), is(false));
        assertThat(instance.getParsedInputData().size(), equalTo(1));
        assertThat(instance.getParsedInputData().size(), equalTo(1));
        assertThat(instance.getParsedInputData().get("BBOXInputData"), is(notNullValue()));
        assertThat(instance.getParsedInputData().get("BBOXInputData").size(), equalTo(1));
        assertThat(instance.getParsedInputData().get("BBOXInputData").get(0), is(notNullValue()));
        assertThat((ReferencedEnvelope)instance.getParsedInputData().get("BBOXInputData").get(0).getPayload(), is(notNullValue()));
        
        ReferencedEnvelope test = (ReferencedEnvelope)instance.getParsedInputData().get("BBOXInputData").get(0).getPayload();
        assertThat(test.getArea(), equalTo(0.020000000000000212d));
        assertThat(test.getLowerCorner().getDirectPosition().toString(), equalTo("DirectPosition2D[46.75, 13.05]"));
        assertThat(test.getLowerCorner().getDimension(), equalTo(2));
    }
}
