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
