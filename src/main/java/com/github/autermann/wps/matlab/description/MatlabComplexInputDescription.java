/*
 * Copyright (C) 2013 Christian Autermann
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.github.autermann.wps.matlab.description;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.FileDataBinding;

import com.github.autermann.matlab.value.MatlabType;
import com.github.autermann.wps.commons.description.impl.AbstractComplexInputDescriptionBuilder;
import com.github.autermann.wps.commons.description.impl.ComplexInputDescriptionImpl;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class MatlabComplexInputDescription extends ComplexInputDescriptionImpl
        implements MatlabProcessInputDescription {

    public MatlabComplexInputDescription(AbstractComplexInputDescriptionBuilder<?, ?> builder) {
        super(builder);
    }

    @Override
    public MatlabType getMatlabType() {
        return MatlabType.FILE;
    }

    @Override
    public Class<? extends IData> getBindingClass() {
        return FileDataBinding.class;
    }

    public static AbstractComplexInputDescriptionBuilder<?, ?> builder() {
        return new BuilderImpl();
    }

    private static class BuilderImpl extends AbstractComplexInputDescriptionBuilder<MatlabComplexInputDescription, BuilderImpl> {
        @Override
        public MatlabComplexInputDescription build() {
            return new MatlabComplexInputDescription(this);
        }
    }
}
