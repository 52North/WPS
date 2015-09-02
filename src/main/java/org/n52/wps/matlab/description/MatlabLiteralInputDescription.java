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
package org.n52.wps.matlab.description;

import org.n52.matlab.connector.value.MatlabType;
import org.n52.wps.io.data.IData;

import com.github.autermann.wps.commons.description.impl.LiteralInputDescriptionImpl;

import org.n52.wps.matlab.transform.LiteralType;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class MatlabLiteralInputDescription extends LiteralInputDescriptionImpl
        implements MatlabProcessInputDescription, MatlabLiteralTyped {
    private final LiteralType type;

    public MatlabLiteralInputDescription(AbstractMatlabLiteralInputDescriptionBuilder<?, ?> builder) {
        super(builder);
        this.type = builder.getType();
    }

    @Override
    public LiteralType getLiteralType() {
        return type;
    }

    @Override
    public MatlabType getMatlabType() {
        return this.type.getMatlabType();
    }

    @Override
    public Class<? extends IData> getBindingClass() {
        return this.type.getBindingClass();
    }

    public static AbstractMatlabLiteralInputDescriptionBuilder<?, ?> builder() {
        return new BuilderImpl();
    }

    private static class BuilderImpl extends AbstractMatlabLiteralInputDescriptionBuilder<MatlabLiteralInputDescription, BuilderImpl> {
        @Override
        public MatlabLiteralInputDescription build() {
            return new MatlabLiteralInputDescription(this);
        }
    }

}
