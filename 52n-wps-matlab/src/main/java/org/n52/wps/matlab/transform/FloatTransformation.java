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
package org.n52.wps.matlab.transform;

import org.n52.matlab.connector.value.MatlabScalar;
import org.n52.matlab.connector.value.MatlabValue;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.literal.LiteralFloatBinding;


/**
 * TODO JavaDoc
 * @author Christian Autermann
 */
public class FloatTransformation extends LiteralTransformation {

    @Override
    public MatlabValue transformInput(IData value) {
        if (value.getPayload() instanceof Float) {
            return new MatlabScalar((Float) value.getPayload());
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    protected IData fromScalar(double value) {
        return new LiteralFloatBinding(Double.valueOf(value).floatValue());
    }

}
