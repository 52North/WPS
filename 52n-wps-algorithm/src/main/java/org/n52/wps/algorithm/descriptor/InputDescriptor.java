/**
 * ﻿Copyright (C) 2007
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
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
