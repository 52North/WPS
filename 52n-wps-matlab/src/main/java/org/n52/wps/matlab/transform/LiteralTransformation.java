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

import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import org.n52.matlab.connector.value.MatlabArray;
import org.n52.matlab.connector.value.MatlabBoolean;
import org.n52.matlab.connector.value.MatlabCell;
import org.n52.matlab.connector.value.MatlabDateTime;
import org.n52.matlab.connector.value.MatlabFile;
import org.n52.matlab.connector.value.MatlabMatrix;
import org.n52.matlab.connector.value.MatlabScalar;
import org.n52.matlab.connector.value.MatlabString;
import org.n52.matlab.connector.value.MatlabStruct;
import org.n52.matlab.connector.value.MatlabValue;
import org.n52.matlab.connector.value.ReturningMatlabValueVisitor;
import org.n52.wps.io.data.IData;


/**
 * TODO JavaDoc
 * @author Christian Autermann
 */
public abstract class LiteralTransformation {

    public abstract MatlabValue transformInput(IData data);

    public final IData transformOutput(MatlabValue value) {
        return value.accept(new ReturningMatlabValueVisitor<IData>() {
            @Override
            public IData visit(MatlabArray array) {
                return fromArray(array.value());
            }

            @Override
            public IData visit(MatlabBoolean bool) {
                return fromBoolean(bool.value());
            }

            @Override
            public IData visit(MatlabCell cell) {
                return fromCell(cell.value());
            }

            @Override
            public IData visit(MatlabMatrix matrix) {
                return fromMatrix(matrix.value());
            }

            @Override
            public IData visit(MatlabScalar scalar) {
                return fromScalar(scalar.value());
            }

            @Override
            public IData visit(MatlabString string) {
                return fromString(string.value());
            }

            @Override
            public IData visit(MatlabStruct struct) {
                return fromStruct(struct.value());
            }

            @Override
            public IData visit(MatlabFile file) {
                throw new IllegalArgumentException();
            }

            @Override
            public IData visit(MatlabDateTime time) {
                return fromDateTime(time.value());
            }
        });
    }

    protected IData fromScalar(double value) {
        throw new IllegalArgumentException();
    }

    protected IData fromArray(double[] value) {
        throw new IllegalArgumentException();
    }

    protected IData fromBoolean(boolean value) {
        throw new IllegalArgumentException();
    }

    protected IData fromCell(List<MatlabValue> value) {
        throw new IllegalArgumentException();
    }

    protected IData fromMatrix(double[][] value) {
        throw new IllegalArgumentException();
    }

    protected IData fromString(String value) {
        throw new IllegalArgumentException();
    }

    protected IData fromStruct(Map<MatlabString, MatlabValue> value) {
        throw new IllegalArgumentException();
    }

    protected IData fromDateTime(DateTime value) {
        throw new IllegalArgumentException();
    }

}
