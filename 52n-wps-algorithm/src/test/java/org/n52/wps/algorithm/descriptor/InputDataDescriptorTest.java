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

import java.math.BigInteger;
import junit.framework.TestCase;
import org.n52.test.mock.MockEnum;
import org.n52.wps.io.data.IData;

/**
 *
 * @author tkunicki
 */
public class InputDataDescriptorTest extends TestCase {
    
    public InputDataDescriptorTest(String testName) {
        super(testName);
    }

    public void testMinOccurs() {
        InputDescriptor inputDescriptor = null;
        
        // test default minOccurs is 1
        inputDescriptor = (new InputDescriptorImpl.Builder()).build();
        assertEquals(BigInteger.valueOf(1), inputDescriptor.getMinOccurs());
        
        // test default minOccurs is 1, that we set it again doesn't matter
        inputDescriptor = (new InputDescriptorImpl.Builder()).minOccurs(1).build();
        assertEquals(BigInteger.valueOf(1), inputDescriptor.getMinOccurs());
        // the other API
        inputDescriptor = (new InputDescriptorImpl.Builder()).minOccurs(BigInteger.valueOf(1)).build();
        assertEquals(BigInteger.valueOf(1), inputDescriptor.getMinOccurs());
        
        // test that 0 is OK
        inputDescriptor = (new InputDescriptorImpl.Builder()).minOccurs(0).build();
        assertEquals(BigInteger.valueOf(0), inputDescriptor.getMinOccurs());
        // the other API
        inputDescriptor = (new InputDescriptorImpl.Builder()).minOccurs(BigInteger.valueOf(0)).build();
        assertEquals(BigInteger.valueOf(0), inputDescriptor.getMinOccurs());
        
        // test fail early on < 0
        boolean thrown = false;
        try {
            (new InputDescriptorImpl.Builder()).minOccurs(-1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);
        // The other API
        thrown = false;
        try {
            (new InputDescriptorImpl.Builder()).minOccurs(BigInteger.valueOf(-1));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);
        
        // test that minOccurs can't be > maxOccurs
        thrown = false;
        try {
            (new InputDescriptorImpl.Builder()).minOccurs(2).build();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            thrown = true;
        }
        assertTrue(thrown);
        // The other API
        thrown = false;
        try {
            (new InputDescriptorImpl.Builder()).minOccurs(BigInteger.valueOf(2)).build();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    public void testMaxOccurs() {
        
        InputDescriptor inputDescriptor = null;
        
        // test default maxOccurs is 1
        inputDescriptor = (new InputDescriptorImpl.Builder()).build();
        assertEquals(BigInteger.valueOf(1), inputDescriptor.getMaxOccurs());
        
        // test default maxOccurs is 1, that we set it again doesn't matter
        inputDescriptor = (new InputDescriptorImpl.Builder()).maxOccurs(1).build();
        assertEquals(BigInteger.valueOf(1), inputDescriptor.getMaxOccurs());
        // the other API
        inputDescriptor = (new InputDescriptorImpl.Builder()).maxOccurs(BigInteger.valueOf(1)).build();
        assertEquals(BigInteger.valueOf(1), inputDescriptor.getMaxOccurs());
        
        // test that we can set maxOccurs value > 1
        inputDescriptor = (new InputDescriptorImpl.Builder()).maxOccurs(2).build();
        assertEquals(BigInteger.valueOf(2), inputDescriptor.getMaxOccurs());
        // the other API
        inputDescriptor = (new InputDescriptorImpl.Builder()).maxOccurs(BigInteger.valueOf(2)).build();
        assertEquals(BigInteger.valueOf(2), inputDescriptor.getMaxOccurs());
        
        // test that we set maxOccurs to number of enum constants
        inputDescriptor = (new InputDescriptorImpl.Builder()).maxOccurs(MockEnum.class).build();
        assertEquals(BigInteger.valueOf(MockEnum.values().length), inputDescriptor.getMaxOccurs());
        
        // test fail-early for maxOccurs < 1;
        boolean thrown = false;
        try {
            (new InputDescriptorImpl.Builder()).maxOccurs(0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);
        // the other API
        thrown = false;
        try {
            (new InputDescriptorImpl.Builder()).maxOccurs(BigInteger.valueOf(0));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);
        
        // test maxOccurs can be < minOccurs even if both are non-default
        thrown = false;
        try {
            (new InputDescriptorImpl.Builder()).
                    minOccurs(3).
                    maxOccurs(2).
                    build();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            thrown = true;
        }
        assertTrue(thrown);
        // the other API
        thrown = false;
        try {
            (new InputDescriptorImpl.Builder()).
                    minOccurs(BigInteger.valueOf(3)).
                    maxOccurs(BigInteger.valueOf(2)).
                    build();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    public static class InputDescriptorImpl extends InputDescriptor<Class<IData>> {
        private InputDescriptorImpl(Builder builder) { super(builder); }
        public static class Builder extends InputDescriptor.Builder<Builder, Class<IData>> {
            Builder() {
                super("mock_identifier", IData.class);
            }
            @Override protected Builder self() { return this;  }
            @Override public InputDescriptorImpl build() { return new InputDescriptorImpl(this); }
        }
    }
}
