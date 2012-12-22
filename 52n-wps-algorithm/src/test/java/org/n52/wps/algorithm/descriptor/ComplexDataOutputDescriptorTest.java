package org.n52.wps.algorithm.descriptor;

import junit.framework.TestCase;
import org.n52.test.mock.MockBinding;

/**
 *
 * @author tkunicki
 */
public class ComplexDataOutputDescriptorTest extends TestCase {
    
    public ComplexDataOutputDescriptorTest(String testName) {
        super(testName);
    }

    public void testBuilder() {
        ComplexDataOutputDescriptor descriptor =
                ComplexDataOutputDescriptor.builder("mock_identifier", MockBinding.class).build();
        assertEquals("mock_identifier", descriptor.getIdentifier());
        assertEquals(MockBinding.class, descriptor.getBinding());
        
        boolean thrown = false;
        try {
            ComplexDataOutputDescriptor.builder(null, MockBinding.class);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);
        
        thrown = false;
        try {
            ComplexDataOutputDescriptor.builder("", MockBinding.class);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);
        
        thrown = false;
        try {
            ComplexDataOutputDescriptor.builder("mock_identifier", null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);
        
    }
    
}
