package org.n52.wps.algorithm.descriptor;

import org.n52.wps.io.data.IComplexData;

/**
 *
 * @author tkunicki
 */
public class ComplexDataOutputDescriptor<T extends Class<? extends IComplexData>> extends OutputDescriptor<T> {


	private ComplexDataOutputDescriptor(Builder builder) {
        super(builder);
    }
    
    public static <T extends Class<? extends IComplexData>> Builder<?,T> builder(String identifier, T binding) {
        return new BuilderTyped(identifier, binding);
    }

    private static class BuilderTyped<T extends Class<? extends IComplexData>> extends Builder<BuilderTyped<T>, T> {
        public BuilderTyped(String identifier, T binding) {
            super(identifier, binding);
        }
        @Override
        protected BuilderTyped self() {
            return this;
        }
    }

    public static abstract class Builder<B extends Builder<B,T>, T extends Class<? extends IComplexData>> extends OutputDescriptor.Builder<B,T> {
        
        private Builder(String identifier, T binding) {
            super(identifier, binding);
        }

        public ComplexDataOutputDescriptor<T> build() {
            return new ComplexDataOutputDescriptor<T>(this);
        }
    }
}
