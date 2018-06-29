/**
 * ﻿Copyright (C) 2007 - 2016 52°North Initiative for Geospatial Open Source
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

import com.google.common.base.Preconditions;
import org.n52.wps.io.BasicXMLTypeFactory;
import org.n52.wps.io.data.ILiteralData;
import org.n52.wps.io.data.binding.literal.LiteralAnyURIBinding;
import org.n52.wps.io.data.binding.literal.LiteralBase64BinaryBinding;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.io.data.binding.literal.LiteralByteBinding;
import org.n52.wps.io.data.binding.literal.LiteralDateTimeBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralFloatBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralLongBinding;
import org.n52.wps.io.data.binding.literal.LiteralShortBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;

/**
 *
 * @author tkunicki
 */
public class LiteralDataOutputDescriptor<T extends Class<? extends ILiteralData>> extends OutputDescriptor<T> {

    private final String dataType;

	protected LiteralDataOutputDescriptor(Builder builder) {
		super(builder);
        this.dataType = builder.dataType;
	}

    public String getDataType() {
        return dataType;
    }

    public static <T extends Class<? extends ILiteralData>> Builder<?,T> builder(String identifier, T binding) {
        return new BuilderTyped(identifier, binding);
    }

    // utility functions, quite verbose...
    public static Builder<?,Class<LiteralAnyURIBinding>> anyURIBuilder(String identifier) {
        return builder(identifier, LiteralAnyURIBinding.class);
    }

    public static Builder<?,Class<LiteralBase64BinaryBinding>> base64BinaryBuilder(String identifier) {
        return builder(identifier, LiteralBase64BinaryBinding.class);
    }

    public static Builder<?,Class<LiteralBooleanBinding>> booleanBuilder(String identifier) {
        return builder(identifier, LiteralBooleanBinding.class);
    }

    public static Builder<?,Class<LiteralByteBinding>> byteBuilder(String identifier) {
        return builder(identifier, LiteralByteBinding.class);
    }

    public static Builder<?,Class<LiteralDateTimeBinding>> dateTimeBuilder(String identifier) {
        return builder(identifier, LiteralDateTimeBinding.class);
    }

    public static Builder<?,Class<LiteralDoubleBinding>> doubleBuilder(String identifier) {
        return builder(identifier, LiteralDoubleBinding.class);
    }

    public static Builder<?,Class<LiteralFloatBinding>> floatBuilder(String identifier) {
        return builder(identifier, LiteralFloatBinding.class);
    }

    public static Builder<?,Class<LiteralIntBinding>> intBuilder(String identifier) {
        return builder(identifier, LiteralIntBinding.class);
    }

    public static Builder<?,Class<LiteralLongBinding>> longBuilder(String identifier) {
        return builder(identifier, LiteralLongBinding.class);
    }

    public static Builder<?,Class<LiteralShortBinding>> shortBuilder(String identifier) {
        return builder(identifier, LiteralShortBinding.class);
    }

    public static Builder<?,Class<LiteralStringBinding>> stringBuilder(String identifier) {
        return builder(identifier, LiteralStringBinding.class);
    }

    private static class BuilderTyped<T extends Class<? extends ILiteralData>> extends Builder<BuilderTyped<T>, T> {
        public BuilderTyped(String identifier, T binding) {
            super(identifier, binding);
        }
        @Override
        protected BuilderTyped self() {
            return this;
        }
    }

    public static abstract class Builder<B extends Builder<B,T>, T extends Class<? extends ILiteralData>> extends OutputDescriptor.Builder<B,T> {

        private final String dataType;

        protected Builder(String identifier, T binding) {
            super(identifier, binding);
            this.dataType = Preconditions.checkNotNull(
                    BasicXMLTypeFactory.getXMLDataTypeforBinding(binding),
                    "Unable to resolve XML DataType for binding class %s",
                    binding);
        }

        @Override
        public LiteralDataOutputDescriptor<T> build() {
            return new LiteralDataOutputDescriptor<T>(this);
        }
    }
}
