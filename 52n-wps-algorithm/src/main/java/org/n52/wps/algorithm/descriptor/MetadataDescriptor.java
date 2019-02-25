/*
 * Copyright (C) 2007-2018 52°North Initiative for Geospatial Open Source
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

/**
 *
 * @author Benjamin Pross
 */
public class MetadataDescriptor extends Descriptor {

    private final String role;

    private final String href;

    MetadataDescriptor(Builder<? extends Builder<?>> builder) {
        super(builder);
        this.role = builder.role;
        this.href = builder.href;
    }

    public String getRole() {
        return role;
    }

    public String getHref() {
        return href;
    }

    public static Builder<?> builder() {
        return new BuilderTyped();
    }

    private static class BuilderTyped extends Builder<BuilderTyped> {
        public BuilderTyped() {
            super();
        }

        @Override
        protected BuilderTyped self() {
            return this;
        }
    }

    public static abstract class Builder<B extends Builder<B>> extends Descriptor.Builder<B> {

        private String role = "";

        private String href = "";

        protected Builder() {
            super("metadata");
        }

        public B role(String role) {
            this.role = role;
            return self();
        }

        public B href(String href) {
            this.href = href;
            return self();
        }

        public MetadataDescriptor build() {
            return new MetadataDescriptor(this);
        }

    }

}
