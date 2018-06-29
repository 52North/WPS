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
import java.math.BigInteger;
import org.n52.wps.io.data.IComplexData;

/**
 *
 * @author tkunicki
 */
public class ComplexDataInputDescriptor<T extends Class<? extends IComplexData>> extends InputDescriptor<T> {

    private final BigInteger maximumMegaBytes;

	private ComplexDataInputDescriptor(Builder builder) {
        super(builder);
		this.maximumMegaBytes = builder.maximumMegaBytes;
    }

    public boolean hasMaximumMegaBytes() {
        return maximumMegaBytes != null && maximumMegaBytes.longValue() > 0;
    }
    
    public BigInteger getMaximumMegaBytes() {
        return maximumMegaBytes;
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

    public static abstract class Builder<B extends Builder<B,T>, T extends Class<? extends IComplexData>> extends InputDescriptor.Builder<B,T> {

        private BigInteger maximumMegaBytes;
        
        private Builder(String identifier, T binding) {
            super(identifier, binding);
        }

        public B maximumMegaBytes(int maximumMegaBytes) {
            return maximumMegaBytes(BigInteger.valueOf(maximumMegaBytes));
        }
        
        public B maximumMegaBytes(BigInteger maximumMegaBytes) {
            Preconditions.checkArgument(maximumMegaBytes.longValue() >= 0, "maximumMegabytes must be >= 0");
            this.maximumMegaBytes = maximumMegaBytes;
            return self();
        }

        @Override
        public ComplexDataInputDescriptor<T> build() {
            return new ComplexDataInputDescriptor<T>(this);
        }
    }
}
