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
