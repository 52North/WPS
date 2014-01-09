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
