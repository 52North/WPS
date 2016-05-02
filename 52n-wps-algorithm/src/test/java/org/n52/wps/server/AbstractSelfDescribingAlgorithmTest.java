/**
 * ﻿Copyright (C) 2007 - 2016 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
