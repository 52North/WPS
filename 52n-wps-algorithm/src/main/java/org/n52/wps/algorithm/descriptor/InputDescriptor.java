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
import org.n52.wps.io.data.IData;

/**
 *
 * @author tkunicki
 */
public abstract class InputDescriptor<T extends Class<? extends IData>> extends BoundDescriptor<T> {

	private final BigInteger minOccurs;
	private final BigInteger maxOccurs;

	protected InputDescriptor(Builder<? extends Builder<?,T>, T> builder) {
        super(builder);
		this.minOccurs = builder.minOccurs;
		this.maxOccurs = builder.maxOccurs;
        Preconditions.checkState(maxOccurs.longValue() >= minOccurs.longValue(), "maxOccurs must be >= minOccurs");
    }

    public BigInteger getMinOccurs() {
        return minOccurs;
    }

    public BigInteger getMaxOccurs() {
        return maxOccurs;
    }

    public static abstract class Builder<B extends Builder<B,T>, T extends Class<? extends IData>> extends BoundDescriptor.Builder<B,T>{

        private BigInteger minOccurs = BigInteger.ONE;
        private BigInteger maxOccurs = BigInteger.ONE;

        protected Builder(String identifier, T binding) {
            super(identifier, binding);
        }

        public B minOccurs(int minOccurs) {
            return minOccurs(BigInteger.valueOf(minOccurs));
        }

        public B minOccurs(BigInteger minOccurs) {
            Preconditions.checkArgument(minOccurs.longValue() >= 0, "minOccurs must be >= 0");
            this.minOccurs = minOccurs;
            return self();
        }

        public B maxOccurs(int maxOccurs) {
            return maxOccurs(BigInteger.valueOf(maxOccurs));
        }

        public B maxOccurs(BigInteger maxOccurs) {
            Preconditions.checkArgument(maxOccurs.longValue() > 0, "maxOccurs must be > 0");
            this.maxOccurs = maxOccurs;
            return self();
        }

        public <E extends Enum<E>> B maxOccurs(Class<E> enumType) {
            return maxOccurs(enumType.getEnumConstants().length);
        }

        public abstract InputDescriptor<T> build();
    }
}
