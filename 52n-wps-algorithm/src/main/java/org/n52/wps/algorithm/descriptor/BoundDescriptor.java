package org.n52.wps.algorithm.descriptor;

import com.google.common.base.Preconditions;

/**
 *
 * @author tkunicki
 */
public abstract class BoundDescriptor<T extends Class<?>> extends Descriptor {

    private final T binding;

	BoundDescriptor(Builder<? extends Builder<?,T>, T> builder) {
        super(builder);
		this.binding = builder.binding;
    }

    public T getBinding() {
        return binding;
    }

    public static abstract class Builder<B extends Builder<B,T>, T extends Class<?>> extends Descriptor.Builder<B> {

        private final T binding;

        protected Builder(String identifier, T binding) {
            super(identifier);
            Preconditions.checkArgument(binding != null, "binding may not be null");
            this.binding = binding;
        }
    }
}
