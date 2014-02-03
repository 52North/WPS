/**
 * ﻿Copyright (C) 2007 - 2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *       • Apache License, version 2.0
 *       • Apache Software License, version 1.0
 *       • GNU Lesser General Public License, version 3
 *       • Mozilla Public License, versions 1.0, 1.1 and 2.0
 *       • Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
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
