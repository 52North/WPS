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
package com.github.autermann.wps.matlab.transform;

import java.util.Calendar;
import java.util.Date;

import org.joda.time.DateTime;

import org.n52.matlab.connector.value.MatlabDateTime;
import org.n52.matlab.connector.value.MatlabValue;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.literal.LiteralDateTimeBinding;


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
        return new LiteralDateTimeBinding(new Date(Double.valueOf(value).longValue()));
    }

    @Override
    protected IData fromDateTime(DateTime value) {
        return new LiteralDateTimeBinding(value.toDate());
    }

}
