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

import junit.framework.TestCase;

/**
 *
 * @author tkunicki
 */
public class DescriptorTest extends TestCase {
    
    public DescriptorTest(String testName) {
        super(testName);
    }

    public void testIdentifier() {
        
        DescriptorImpl descriptor = null;
        
        boolean thrown = false;
        try {
            new DescriptorImpl.Builder(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);
        
        thrown = false;
        try {
            new DescriptorImpl.Builder("");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);
        
        // set case: set to 'an_identifier'
        descriptor = (new DescriptorImpl.Builder("mock_identifier")).build();
        assertEquals("mock_identifier", descriptor.getIdentifier());
        
    }

    public void testTitle() {
        
        DescriptorImpl descriptor = null;

        // default case: title is not initialized and therefore is null;
        descriptor = (new DescriptorImpl.Builder("mock_identifier")).build();
        assertNull(descriptor.getTitle());
        assertFalse(descriptor.hasTitle());
        
        // unset annotation case: 'title' default/unset value for annotations
        // is empty string as 'null' is not a valid annotation value;
        descriptor = (new DescriptorImpl.Builder("mock_identifier")).title("").build();
        assertEquals("", descriptor.getTitle());
        assertFalse(descriptor.hasTitle());
        
        // set case: set to 'an_title'
        descriptor = (new DescriptorImpl.Builder("mock_identifier")).title("mock_title").build();
        assertEquals("mock_title", descriptor.getTitle());
        assertTrue(descriptor.hasTitle());
    }

    public void testAbstract() {
        
        DescriptorImpl descriptor = null;

        // default case: abstrakt is not initialized and therefore is null;
        descriptor = (new DescriptorImpl.Builder("mock_identifier")).build();
        assertNull(descriptor.getAbstract());
        assertFalse(descriptor.hasAbstract());
        
        // unset annotation case: 'abstrakt' default/unset value for annotations
        // is empty string as 'null' is not a valid annotation value;
        descriptor = (new DescriptorImpl.Builder("mock_identifier")).abstrakt("").build();
        assertEquals("", descriptor.getAbstract());
        assertFalse(descriptor.hasAbstract());
        
        // set case: set to 'an_abstrakt'
        descriptor = (new DescriptorImpl.Builder("mock_identifier")).abstrakt("an_abstract").build();
        assertEquals("an_abstract", descriptor.getAbstract());
        assertTrue(descriptor.hasAbstract());
        
    }

    // Dummy implementation, Descriptor and Builder classes are abstract
    // so we need to provide an concrete implementation to test.
    public static class DescriptorImpl extends Descriptor {
        private DescriptorImpl(Builder builder) {  super(builder);  }
        public static class Builder extends Descriptor.Builder<Builder> {
            Builder(String identifier) { super(identifier); }
            @Override protected Builder self() { return this;  }
             public DescriptorImpl build() { return new DescriptorImpl(this); }
        }
        
    }
}
