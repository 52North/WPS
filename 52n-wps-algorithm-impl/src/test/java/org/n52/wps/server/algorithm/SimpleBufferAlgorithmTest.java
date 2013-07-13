/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.wps.server.algorithm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlValidationError;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.IAlgorithm;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tkunicki
 */
public class SimpleBufferAlgorithmTest {
    
    public SimpleBufferAlgorithmTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        try {
            WPSConfig.forceInitialization("../52n-wps-webapp/src/main/webapp/config/wps_config.xml");
        } catch (XmlException ex) {
            LoggerFactory.getLogger(SimpleBufferAlgorithmTest.class.getName()).error(ex.getMessage());
        } catch (IOException ex) {
            LoggerFactory.getLogger(SimpleBufferAlgorithmTest.class.getName()).error(ex.getMessage());
        }
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testProcessDescription() {
        IAlgorithm a = new SimpleBufferAlgorithm();
        printAlgorithmProcessDescription(a);
        assertTrue(validateAlgorithmProcessDescription(a));
    }
    
    private void printAlgorithmProcessDescription(IAlgorithm algorithm) {
        System.out.println();
        System.out.println(" ### DescribeProcess for " + algorithm.getClass().getName() + " ###");
        System.out.println(getXMLAsStringFromDescription(algorithm.getDescription()));
        System.out.println();
    }
    
    private boolean validateAlgorithmProcessDescription(IAlgorithm algorithm) {
        XmlOptions xmlOptions = new XmlOptions();
        List<XmlValidationError> xmlValidationErrorList = new ArrayList<XmlValidationError>();
            xmlOptions.setErrorListener(xmlValidationErrorList);
        boolean valid = algorithm.getDescription().validate(xmlOptions);
        if (!valid) {
            System.err.println("Error validating process description for " + getClass().getCanonicalName());
            for (XmlValidationError xmlValidationError : xmlValidationErrorList) {
                System.err.println("\tMessage: " +  xmlValidationError.getMessage());
                System.err.println("\tLocation of invalid XML: " +
                     xmlValidationError.getCursorLocation().xmlText());
            }
        }
        return valid;
    }

    private String getXMLAsStringFromDescription(ProcessDescriptionType decription) {
        XmlOptions options = new XmlOptions();
        options.setSavePrettyPrint();
        options.setSaveOuter();
        HashMap ns = new HashMap();
        ns.put("http://www.opengis.net/wps/1.0.0", "wps");
        ns.put("http://www.opengis.net/ows/1.1", "ows");
        options.setSaveNamespacesFirst().
                setSaveSuggestedPrefixes(ns).
                setSaveAggressiveNamespaces();
        return decription.xmlText(options);
    }
}