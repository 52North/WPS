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

import java.util.List;
import junit.framework.TestCase;
import org.n52.wps.algorithm.util.ClassUtil;
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
public class LiteralDataInputDescriptorTest extends TestCase {
    
    public final static String MOCK_UNALLOWED = "MOCK_UNALLOWED";
    
    public enum  MOCK_ALLOWED_VALUES {
        MOCK_ALLOWED1,
        MOCK_ALLOWED2,
        MOCK_ALLOWED3,
    }
    
    public LiteralDataInputDescriptorTest(String testName) {
        super(testName);
    }
    
    public void testDefaultValue() {
        LiteralDataInputDescriptor descriptor = null;
        
        // test default for defaultValue
        descriptor = LiteralDataInputDescriptor.builder("mock_identifier", LiteralStringBinding.class).build();
        assertNull(descriptor.getDefaultValue());
        assertFalse(descriptor.hasDefaultValue());
        
        // test "" for defaultValue (unset annotation case)
        descriptor = LiteralDataInputDescriptor.builder("mock_identifier", LiteralStringBinding.class).
                defaultValue("").
                build();
        assertEquals("", descriptor.getDefaultValue());
        assertFalse(descriptor.hasDefaultValue());
        
        // test with a valid defaultValue (unset annotation case)
        descriptor = LiteralDataInputDescriptor.builder("mock_identifier", LiteralStringBinding.class).
                defaultValue("mock_default").
                build();
        assertEquals("mock_default", descriptor.getDefaultValue());
        assertTrue(descriptor.hasDefaultValue());
    }

    public void testAllowedValues() {
        LiteralDataInputDescriptor descriptor = null;
        
        // test default for allowedValues
        descriptor = LiteralDataInputDescriptor.builder("mock_identifier", LiteralStringBinding.class).build();
        assertNotNull(descriptor.getAllowedValues());
        assertEquals(0, descriptor.getAllowedValues().size());
        assertFalse(descriptor.hasAllowedValues());
        
        // test allowedValues(String[])
        descriptor = LiteralDataInputDescriptor.builder("mock_identifier", LiteralStringBinding.class).
                allowedValues(ClassUtil.convertEnumToStringArray(MOCK_ALLOWED_VALUES.class)).build();
        validateAllowValues(descriptor);
        
        // test allowedValues(List<String>)
        descriptor = LiteralDataInputDescriptor.builder("mock_identifier", LiteralStringBinding.class).
                allowedValues(ClassUtil.convertEnumToStringList(MOCK_ALLOWED_VALUES.class)).build();
        validateAllowValues(descriptor);
        
        // test allowedValues(Class<? extends Enum>)
        descriptor = LiteralDataInputDescriptor.builder("mock_identifier", LiteralStringBinding.class).
                allowedValues(MOCK_ALLOWED_VALUES.class).build();
        validateAllowValues(descriptor);
        
        // test allowedValues()
        descriptor = LiteralDataInputDescriptor.builder("mock_identifier", LiteralStringBinding.class).
                defaultValue(MOCK_ALLOWED_VALUES.MOCK_ALLOWED1.name()).
                allowedValues(MOCK_ALLOWED_VALUES.class).build();
        validateAllowValues(descriptor);
        
        boolean thrown = false;
        try {
            LiteralDataInputDescriptor.builder("mock_identifier", LiteralStringBinding.class).
                defaultValue(MOCK_UNALLOWED).
                allowedValues(MOCK_ALLOWED_VALUES.class).
                build();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    public void testBuilder() {
        LiteralDataInputDescriptor descriptor =
                LiteralDataInputDescriptor.builder("mock_identifier", LiteralStringBinding.class).build();
        assertEquals("mock_identifier", descriptor.getIdentifier());
        assertEquals(LiteralStringBinding.class, descriptor.getBinding());
        
        boolean thrown = false;
        try {
            LiteralDataInputDescriptor.builder(null, LiteralStringBinding.class);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);
        
        thrown = false;
        try {
            LiteralDataInputDescriptor.builder("", LiteralStringBinding.class);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);
        
        thrown = false;
        try {
            LiteralDataInputDescriptor.builder("mock_identifier", null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);
        
    }

    public void testAnyURIBuilder() {
        LiteralDataInputDescriptor descriptor = LiteralDataInputDescriptor.anyURIBuilder("mock_identifier").build();
        assertEquals("mock_identifier", descriptor.getIdentifier());
        assertEquals(LiteralAnyURIBinding.class, descriptor.getBinding());
        assertEquals(BasicXMLTypeFactory.ANYURI_URI, descriptor.getDataType());
    }

    public void testBase64BinaryBuilder() {
        LiteralDataInputDescriptor descriptor = LiteralDataInputDescriptor.anyURIBuilder("mock_identifier").build();
        assertEquals("mock_identifier", descriptor.getIdentifier());
        assertEquals(LiteralAnyURIBinding.class, descriptor.getBinding());
        assertEquals(BasicXMLTypeFactory.ANYURI_URI, descriptor.getDataType());
    }

    public void testBooleanBuilder() {
        LiteralDataInputDescriptor descriptor = LiteralDataInputDescriptor.booleanBuilder("mock_identifier").build();
        assertEquals("mock_identifier", descriptor.getIdentifier());
        assertEquals(LiteralBooleanBinding.class, descriptor.getBinding());
        assertEquals(BasicXMLTypeFactory.BOOLEAN_URI, descriptor.getDataType());
    }

    public void testByteBuilder() {
        LiteralDataInputDescriptor descriptor = LiteralDataInputDescriptor.byteBuilder("mock_identifier").build();
        assertEquals("mock_identifier", descriptor.getIdentifier());
        assertEquals(LiteralByteBinding.class, descriptor.getBinding());
        assertEquals(BasicXMLTypeFactory.BYTE_URI, descriptor.getDataType());
    }

    public void testDateTimeBuilder() {
        LiteralDataInputDescriptor descriptor = LiteralDataInputDescriptor.dateTimeBuilder("mock_identifier").build();
        assertEquals("mock_identifier", descriptor.getIdentifier());
        assertEquals(LiteralDateTimeBinding.class, descriptor.getBinding());
        assertEquals(BasicXMLTypeFactory.DATETIME_URI, descriptor.getDataType());
    }

    public void testDoubleBuilder() {
        LiteralDataInputDescriptor descriptor = LiteralDataInputDescriptor.doubleBuilder("mock_identifier").build();
        assertEquals("mock_identifier", descriptor.getIdentifier());
        assertEquals(LiteralDoubleBinding.class, descriptor.getBinding());
        assertEquals(BasicXMLTypeFactory.DOUBLE_URI, descriptor.getDataType());
    }

    public void testFloatBuilder() {
        LiteralDataInputDescriptor descriptor = LiteralDataInputDescriptor.floatBuilder("mock_identifier").build();
        assertEquals("mock_identifier", descriptor.getIdentifier());
        assertEquals(LiteralFloatBinding.class, descriptor.getBinding());
        assertEquals(BasicXMLTypeFactory.FLOAT_URI, descriptor.getDataType());
    }

    public void testIntBuilder() {
        LiteralDataInputDescriptor descriptor = LiteralDataInputDescriptor.intBuilder("mock_identifier").build();
        assertEquals("mock_identifier", descriptor.getIdentifier());
        assertEquals(LiteralIntBinding.class, descriptor.getBinding());
        assertEquals(BasicXMLTypeFactory.INT_URI, descriptor.getDataType());
    }

    public void testLongBuilder() {
        LiteralDataInputDescriptor descriptor = LiteralDataInputDescriptor.longBuilder("mock_identifier").build();
        assertEquals("mock_identifier", descriptor.getIdentifier());
        assertEquals(LiteralLongBinding.class, descriptor.getBinding());
        assertEquals(BasicXMLTypeFactory.LONG_URI, descriptor.getDataType());
    }

    public void testShortBuilder() {
        LiteralDataInputDescriptor descriptor = LiteralDataInputDescriptor.shortBuilder("mock_identifier").build();
        assertEquals("mock_identifier", descriptor.getIdentifier());
        assertEquals(LiteralShortBinding.class, descriptor.getBinding());
        assertEquals(BasicXMLTypeFactory.SHORT_URI, descriptor.getDataType());
    }

    public void testStringBuilder() {
        LiteralDataInputDescriptor descriptor = LiteralDataInputDescriptor.stringBuilder("mock_identifier").build();
        assertEquals("mock_identifier", descriptor.getIdentifier());
        assertEquals(LiteralStringBinding.class, descriptor.getBinding());
        assertEquals(BasicXMLTypeFactory.STRING_URI, descriptor.getDataType());
    }
    
    private void validateAllowValues(LiteralDataInputDescriptor descriptor) {
        assertTrue(descriptor.hasAllowedValues());
       
        List<String> allowedValueList = descriptor.getAllowedValues();
        
        assertNotNull(allowedValueList);
        assertEquals(MOCK_ALLOWED_VALUES.values().length, allowedValueList.size());
        for (int index = 0; index < allowedValueList.size(); ++index) {
            assertNotNull(allowedValueList.get(index));
            assertEquals(MOCK_ALLOWED_VALUES.values()[index].name(), allowedValueList.get(index));
        }
    }
}
