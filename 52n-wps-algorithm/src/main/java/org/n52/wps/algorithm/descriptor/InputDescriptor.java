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
