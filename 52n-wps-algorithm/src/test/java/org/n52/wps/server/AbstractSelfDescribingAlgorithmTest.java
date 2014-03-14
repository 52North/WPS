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
package org.n52.wps.server;

import java.util.HashMap;
import org.n52.test.mock.MockUtil;
import junit.framework.TestCase;
import net.opengis.wps.x100.ProcessDescriptionType;
import org.apache.xmlbeans.XmlOptions;

/**
 *
 * @author tkunicki
 */
public class AbstractSelfDescribingAlgorithmTest extends TestCase {
    
    public AbstractSelfDescribingAlgorithmTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockUtil.getMockConfig();
    }

    public void testComplexSelfDescribingAlgorithmUsingDescriptor() {
        IAlgorithm algorithm = new ComplexSelfDescribingAlgorithmUsingDescriptor();
        printAlgorithmProcessDescription(algorithm);
    }

    public void testComplexAnnotatedAlgorithm() {
        IAlgorithm algorithm = new ComplexAnnotatedAlgorithm();
        printAlgorithmProcessDescription(algorithm);
    }

    public void testStringReverseSelfDescribingAlgorithm() {
        IAlgorithm algorithm = new StringReverseSelfDescribingAlgorithm();
        printAlgorithmProcessDescription(algorithm);
    }

    public void testStringReverseAnnotatedAlgorithm() {
        IAlgorithm algorithm = new StringReverseAnnotatedAlgorithm();
        printAlgorithmProcessDescription(algorithm);
    }

    public void testStringJoinSelfDescribingAlgorithm() {
        IAlgorithm algorithm = new StringJoinSelfDescribingAlgorithm();
        printAlgorithmProcessDescription(algorithm);
    }

    public void testStringJoinAnnotatedAlgorithm() {
        IAlgorithm algorithm = new StringJoinAnnotatedAlgorithm();
        printAlgorithmProcessDescription(algorithm);
    }

    private void printAlgorithmProcessDescription(IAlgorithm algorithm) {
        System.out.println();
        System.out.println(" ### DescribeProcess for " + algorithm.getClass().getName() + " ###");
        System.out.println(getXMLAsStringFromDescription(algorithm.getDescription()));
        System.out.println();
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
