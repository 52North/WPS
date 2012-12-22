package org.n52.wps.algorithm.descriptor;

import junit.framework.TestCase;

/**
 *
 * @author tkunicki
 */
public class BoundDataDescriptorTest extends TestCase {
    
    public BoundDataDescriptorTest(String testName) {
        super(testName);
    }

    public void testBinding() {
        
        BoundDescriptor descriptor = null;
        
        // Test fail-early, exception should be thrown if binding is 'null';
        boolean thrown = false;
        try {
            descriptor = (new BoundDescriptorImpl.Builder(null)).build();
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertTrue(thrown);
        
        // make sure the class type we build with is the same as the one returned
        // by the constucted object
        descriptor = (new BoundDescriptorImpl.Builder(Double.class)).build();
        assertEquals(Double.class, descriptor.getBinding());
        
        descriptor = (new BoundDescriptorImpl.Builder(MockNumber.class)).build();
        assertEquals(MockNumber.class, descriptor.getBinding());
    }

    public static class BoundDescriptorImpl extends BoundDescriptor<Class<? extends Number>> {
        private BoundDescriptorImpl(Builder builder) { super(builder); }
        public static class Builder extends BoundDescriptor.Builder<Builder, Class<? extends Number>> {
            Builder(Class<? extends Number> binding) {
                super("mock_identifier", binding);
            }
            @Override protected Builder self() { return this;  }
            public BoundDescriptorImpl build() { return new BoundDescriptorImpl(this); }
        }
    }
    
    public static class MockNumber extends Number {
        @Override public int intValue() { return Integer.MAX_VALUE; }
        @Override public long longValue() { return Long.MAX_VALUE; }
        @Override public float floatValue() { return Float.MAX_VALUE; }
        @Override public double doubleValue() { return Double.MAX_VALUE; };
    }
}
