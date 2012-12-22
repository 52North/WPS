package org.n52.test.mock;

import org.n52.wps.io.data.IComplexData;

/**
 *
 * @author tkunicki
 */
public class MockBinding implements IComplexData {

    private final MockComplexObject payload;

    public MockBinding(MockComplexObject payload) {
        this.payload = payload;
    }

    @Override
    public MockComplexObject getPayload() {
        return payload;
    }

    @Override
    public Class getSupportedClass() {
        return MockComplexObject.class;
    }

}
