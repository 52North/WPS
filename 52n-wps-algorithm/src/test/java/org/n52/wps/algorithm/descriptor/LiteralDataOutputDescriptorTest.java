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
package org.n52.wps.algorithm.descriptor;

import junit.framework.TestCase;
import org.n52.wps.io.BasicXMLTypeFactory;
import org.n52.wps.io.data.binding.literal.LiteralAnyURIBinding;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.io.data.binding.literal.LiteralByteBinding;
import org.n52.wps.io.data.binding.literal.LiteralDateTimeBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralFloatBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralLongBinding;
import org.n52.wps.io.data.binding.literal.LiteralShortBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;

/**
 *
 * @author tkunicki
 */
public class LiteralDataOutputDescriptorTest extends TestCase {
    
    public LiteralDataOutputDescriptorTest(String testName) {
        super(testName);
    }
    
    public void testStaticBuilder() {
        LiteralDataOutputDescriptor descriptor =
                LiteralDataOutputDescriptor.builder("mock_identifier", LiteralStringBinding.class).build();
        assertEquals("mock_identifier", descriptor.getIdentifier());
        assertEquals(LiteralStringBinding.class, descriptor.getBinding());
        
        boolean thrown = false;
        try {
            LiteralDataOutputDescriptor.builder(null, LiteralStringBinding.class);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);
        
        thrown = false;
        try {
            LiteralDataOutputDescriptor.builder("", LiteralStringBinding.class);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);
        
        thrown = false;
        try {
            LiteralDataOutputDescriptor.builder("mock_identifier", null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);
        
    }

    public void testAnyURIBuilder() {
        LiteralDataOutputDescriptor descriptor = LiteralDataOutputDescriptor.anyURIBuilder("mock_identifier").build();
        assertEquals("mock_identifier", descriptor.getIdentifier());
        assertEquals(LiteralAnyURIBinding.class, descriptor.getBinding());
        assertEquals(BasicXMLTypeFactory.ANYURI_URI, descriptor.getDataType());
    }

    public void testBase64BinaryBuilder() {
        LiteralDataOutputDescriptor descriptor = LiteralDataOutputDescriptor.anyURIBuilder("mock_identifier").build();
        assertEquals("mock_identifier", descriptor.getIdentifier());
        assertEquals(LiteralAnyURIBinding.class, descriptor.getBinding());
        assertEquals(BasicXMLTypeFactory.ANYURI_URI, descriptor.getDataType());
    }

    public void testBooleanBuilder() {
        LiteralDataOutputDescriptor descriptor = LiteralDataOutputDescriptor.booleanBuilder("mock_identifier").build();
        assertEquals("mock_identifier", descriptor.getIdentifier());
        assertEquals(LiteralBooleanBinding.class, descriptor.getBinding());
        assertEquals(BasicXMLTypeFactory.BOOLEAN_URI, descriptor.getDataType());
    }

    public void testByteBuilder() {
        LiteralDataOutputDescriptor descriptor = LiteralDataOutputDescriptor.byteBuilder("mock_identifier").build();
        assertEquals("mock_identifier", descriptor.getIdentifier());
        assertEquals(LiteralByteBinding.class, descriptor.getBinding());
        assertEquals(BasicXMLTypeFactory.BYTE_URI, descriptor.getDataType());
    }

    public void testDateTimeBuilder() {
        LiteralDataOutputDescriptor descriptor = LiteralDataOutputDescriptor.dateTimeBuilder("mock_identifier").build();
        assertEquals("mock_identifier", descriptor.getIdentifier());
        assertEquals(LiteralDateTimeBinding.class, descriptor.getBinding());
        assertEquals(BasicXMLTypeFactory.DATETIME_URI, descriptor.getDataType());
    }

    public void testDoubleBuilder() {
        LiteralDataOutputDescriptor descriptor = LiteralDataOutputDescriptor.doubleBuilder("mock_identifier").build();
        assertEquals("mock_identifier", descriptor.getIdentifier());
        assertEquals(LiteralDoubleBinding.class, descriptor.getBinding());
        assertEquals(BasicXMLTypeFactory.DOUBLE_URI, descriptor.getDataType());
    }

    public void testFloatBuilder() {
        LiteralDataOutputDescriptor descriptor = LiteralDataOutputDescriptor.floatBuilder("mock_identifier").build();
        assertEquals("mock_identifier", descriptor.getIdentifier());
        assertEquals(LiteralFloatBinding.class, descriptor.getBinding());
        assertEquals(BasicXMLTypeFactory.FLOAT_URI, descriptor.getDataType());
    }

    public void testIntBuilder() {
        LiteralDataOutputDescriptor descriptor = LiteralDataOutputDescriptor.intBuilder("mock_identifier").build();
        assertEquals("mock_identifier", descriptor.getIdentifier());
        assertEquals(LiteralIntBinding.class, descriptor.getBinding());
        assertEquals(BasicXMLTypeFactory.INT_URI, descriptor.getDataType());
    }

    public void testLongBuilder() {
        LiteralDataOutputDescriptor descriptor = LiteralDataOutputDescriptor.longBuilder("mock_identifier").build();
        assertEquals("mock_identifier", descriptor.getIdentifier());
        assertEquals(LiteralLongBinding.class, descriptor.getBinding());
        assertEquals(BasicXMLTypeFactory.LONG_URI, descriptor.getDataType());
    }

    public void testShortBuilder() {
        LiteralDataOutputDescriptor descriptor = LiteralDataOutputDescriptor.shortBuilder("mock_identifier").build();
        assertEquals("mock_identifier", descriptor.getIdentifier());
        assertEquals(LiteralShortBinding.class, descriptor.getBinding());
        assertEquals(BasicXMLTypeFactory.SHORT_URI, descriptor.getDataType());
    }

    public void testStringBuilder() {
        LiteralDataOutputDescriptor descriptor = LiteralDataOutputDescriptor.stringBuilder("mock_identifier").build();
        assertEquals("mock_identifier", descriptor.getIdentifier());
        assertEquals(LiteralStringBinding.class, descriptor.getBinding());
        assertEquals(BasicXMLTypeFactory.STRING_URI, descriptor.getDataType());
    }
    
}
