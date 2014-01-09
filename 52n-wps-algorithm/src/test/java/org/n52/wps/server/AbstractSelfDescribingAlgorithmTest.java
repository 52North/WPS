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
