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
