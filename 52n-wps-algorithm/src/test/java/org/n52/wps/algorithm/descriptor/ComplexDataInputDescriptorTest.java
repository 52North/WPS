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
package org.n52.wps.algorithm.descriptor;

import java.math.BigInteger;
import junit.framework.TestCase;
import org.n52.test.mock.MockBinding;

/**
 *
 * @author tkunicki
 */
public class ComplexDataInputDescriptorTest extends TestCase {
    
    public ComplexDataInputDescriptorTest(String testName) {
        super(testName);
    }

    public void testMaximumMegabytes() {
        ComplexDataInputDescriptor descriptor = null;
        
        descriptor = ComplexDataInputDescriptor.builder("mock_identifier", MockBinding.class).build();
        assertNull(descriptor.getMaximumMegaBytes());
        assertFalse(descriptor.hasMaximumMegaBytes());
        
        descriptor = ComplexDataInputDescriptor.builder("mock_identifier", MockBinding.class).
                maximumMegaBytes(0).build();
        assertNotNull(descriptor.getMaximumMegaBytes());
        assertEquals(BigInteger.valueOf(0), descriptor.getMaximumMegaBytes());
        assertFalse(descriptor.hasMaximumMegaBytes());
        
        descriptor = ComplexDataInputDescriptor.builder("mock_identifier", MockBinding.class).
                maximumMegaBytes(BigInteger.valueOf(0)).build();
        assertNotNull(descriptor.getMaximumMegaBytes());
        assertEquals(BigInteger.valueOf(0), descriptor.getMaximumMegaBytes());
        assertFalse(descriptor.hasMaximumMegaBytes());
        
        descriptor = ComplexDataInputDescriptor.builder("mock_identifier", MockBinding.class).
                maximumMegaBytes(1).build();
        assertNotNull(descriptor.getMaximumMegaBytes());
        assertEquals(BigInteger.valueOf(1), descriptor.getMaximumMegaBytes());
        
        descriptor = ComplexDataInputDescriptor.builder("mock_identifier", MockBinding.class).
                maximumMegaBytes(BigInteger.valueOf(1)).build();
        assertNotNull(descriptor.getMaximumMegaBytes());
        assertEquals(BigInteger.valueOf(1), descriptor.getMaximumMegaBytes());
        
        descriptor = ComplexDataInputDescriptor.builder("mock_identifier", MockBinding.class).
                maximumMegaBytes(Integer.MAX_VALUE).build();
        assertNotNull(descriptor.getMaximumMegaBytes());
        assertEquals(BigInteger.valueOf(Integer.MAX_VALUE), descriptor.getMaximumMegaBytes());
        
        descriptor = ComplexDataInputDescriptor.builder("mock_identifier", MockBinding.class).
                maximumMegaBytes(BigInteger.valueOf(Integer.MAX_VALUE)).build();
        assertNotNull(descriptor.getMaximumMegaBytes());
        assertEquals(BigInteger.valueOf(Integer.MAX_VALUE), descriptor.getMaximumMegaBytes());
        
        boolean thrown = false;
        try {
            ComplexDataInputDescriptor.builder("mock_identifier", MockBinding.class).
                maximumMegaBytes(-1);
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);
        
        thrown = false;
        try {
            ComplexDataInputDescriptor.builder("mock_identifier", MockBinding.class).
                maximumMegaBytes(BigInteger.valueOf(-1));
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);
        
        thrown = false;
        try {
            ComplexDataInputDescriptor.builder("mock_identifier", MockBinding.class).
                maximumMegaBytes(Integer.MIN_VALUE);
            
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);
        
        thrown = false;
        try {
            ComplexDataInputDescriptor.builder("mock_identifier", MockBinding.class).
                maximumMegaBytes(BigInteger.valueOf(Integer.MIN_VALUE));
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    public void testStaticBuilder() {
        ComplexDataInputDescriptor descriptor =
                ComplexDataInputDescriptor.builder("mock_identifier", MockBinding.class).build();
        assertEquals("mock_identifier", descriptor.getIdentifier());
        assertEquals(MockBinding.class, descriptor.getBinding());
        
        boolean thrown = false;
        try {
            ComplexDataInputDescriptor.builder(null, MockBinding.class);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);
        
        thrown = false;
        try {
            ComplexDataInputDescriptor.builder("", MockBinding.class);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);
        
        thrown = false;
        try {
            ComplexDataInputDescriptor.builder("mock_identifier", null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);
        
    }

}
