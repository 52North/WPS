package com.github.autermann.wps.matlab.transform;

import java.util.Calendar;
import java.util.Date;

import org.joda.time.DateTime;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.literal.LiteralDateTimeBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.autermann.matlab.value.MatlabDateTime;
import com.github.autermann.matlab.value.MatlabValue;

/**
 * TODO JavaDoc
 * @author Christian Autermann
 */
public class DateTimeTransformation extends LiteralTransformation {

    @Override
    public MatlabValue transformInput(IData value) {
        if (value.getPayload() instanceof Date) {
            Date date = (Date) value.getPayload();
            return new MatlabDateTime(new DateTime(date));
        } else if (value.getPayload() instanceof Calendar) {
            Calendar calendar = (Calendar) value.getPayload();
            return new MatlabDateTime(new DateTime(calendar));
        } else if (value.getPayload() instanceof DateTime) {
            DateTime dateTime = (DateTime) value.getPayload();
            return new MatlabDateTime(dateTime);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    protected IData fromScalar(double value) {
        return new LiteralDateTimeBinding(new Date(Double.valueOf(value)
                .longValue()));
    }

    @Override
    protected IData fromDateTime(DateTime value) {
        return new LiteralDateTimeBinding(value.toDate());
    }

}
