package com.github.autermann.wps.matlab.transform;

import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.n52.wps.io.data.IData;

import com.github.autermann.matlab.value.MatlabArray;
import com.github.autermann.matlab.value.MatlabBoolean;
import com.github.autermann.matlab.value.MatlabCell;
import com.github.autermann.matlab.value.MatlabDateTime;
import com.github.autermann.matlab.value.MatlabFile;
import com.github.autermann.matlab.value.MatlabMatrix;
import com.github.autermann.matlab.value.MatlabScalar;
import com.github.autermann.matlab.value.MatlabString;
import com.github.autermann.matlab.value.MatlabStruct;
import com.github.autermann.matlab.value.MatlabValue;
import com.github.autermann.matlab.value.ReturningMatlabValueVisitor;

/**
 * TODO JavaDoc
 * @author Christian Autermann
 */
public abstract class LiteralTransformation {

    public abstract MatlabValue transformInput(IData data);

    public final IData transformOutput(MatlabValue value) {
        return value.accept(new ReturningMatlabValueVisitor<IData>() {
            public IData visit(MatlabArray array) {
                return fromArray(array.value());
            }

            public IData visit(MatlabBoolean bool) {
                return fromBoolean(bool.value());
            }

            public IData visit(MatlabCell cell) {
                return fromCell(cell.value());
            }

            public IData visit(MatlabMatrix matrix) {
                return fromMatrix(matrix.value());
            }

            public IData visit(MatlabScalar scalar) {
                return fromScalar(scalar.value());
            }

            public IData visit(MatlabString string) {
                return fromString(string.value());
            }

            public IData visit(MatlabStruct struct) {
                return fromStruct(struct.value());
            }

            public IData visit(MatlabFile file) {
                throw new IllegalArgumentException();
            }

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
