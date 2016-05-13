/**
 * Copyright (C) 2007-2015 52°North Initiative for Geospatial Open Source
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

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;

/**
 *
 * @author tkunicki
 */
public abstract class BoundDescriptor<T extends Class<?>> extends Descriptor {

    private final T binding;
    private final List<MetadataDescriptor> metadataDescriptors;

	BoundDescriptor(Builder<? extends Builder<?,T>, T> builder) {
        super(builder);
		this.binding = builder.binding;
		this.metadataDescriptors = builder.metadataDescriptors;
    }

    public T getBinding() {
        return binding;
    }

    public List<MetadataDescriptor> getMetadataDescriptors() {
        return metadataDescriptors;
    }

    public static abstract class Builder<B extends Builder<B,T>, T extends Class<?>> extends Descriptor.Builder<B> {

        private final T binding;
        private List<MetadataDescriptor> metadataDescriptors;

        protected Builder(String identifier, T binding) {
            super(identifier);
            Preconditions.checkArgument(binding != null, "binding may not be null");
            this.binding = binding;
            metadataDescriptors = new ArrayList<>();
        }
        
        public B addMetadataDescriptor(MetadataDescriptor.Builder metadataDescriptorBuilder) {
            return addMetadataDescriptor(metadataDescriptorBuilder.build());
        }

        public B addMetadataDescriptor(MetadataDescriptor metadataDescriptor) {
            this.metadataDescriptors.add(metadataDescriptor);
            return self();
        }

        public B addMetadataDescriptors(List<? extends MetadataDescriptor> metadataDescriptors) {
            this.metadataDescriptors.addAll(metadataDescriptors);
            return self();
        }
    }
}
