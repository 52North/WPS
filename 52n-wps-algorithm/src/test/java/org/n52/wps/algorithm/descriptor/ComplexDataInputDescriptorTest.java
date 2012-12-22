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
