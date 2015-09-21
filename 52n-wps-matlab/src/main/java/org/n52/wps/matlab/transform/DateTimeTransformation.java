/**
 * Copyright (C) 2013 - 2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *       • Apache License, version 2.0
 *       • Apache Software License, version 1.0
 *       • GNU Lesser General Public License, version 3
 *       • Mozilla Public License, versions 1.0, 1.1 and 2.0
 *       • Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.wps.matlab.transform;

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
