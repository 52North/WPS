/**
 * Copyright (C) 2013-2015 52°North Initiative for Geospatial Open Source
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

import java.util.Collections;
import java.util.Set;

import org.n52.matlab.connector.value.MatlabType;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.literal.LiteralAnyURIBinding;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.io.data.binding.literal.LiteralByteBinding;
import org.n52.wps.io.data.binding.literal.LiteralDateTimeBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralFloatBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralLongBinding;
import org.n52.wps.io.data.binding.literal.LiteralShortBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;

import org.n52.wps.matlab.YamlConstants;

import com.google.common.collect.Sets;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public enum LiteralType {
    BOOLEAN(new BooleanTransformation(),
            MatlabType.BOOLEAN,
            LiteralBooleanBinding.class,
            "xs:boolean",
            YamlConstants.BOOLEAN_TYPE,
            YamlConstants.BOOL_TYPE),
    DATE_TIME(new DateTimeTransformation(),
              MatlabType.DATE_TIME,
              LiteralDateTimeBinding.class,
              "xs:dateTime",
              YamlConstants.DATE_TYPE,
              YamlConstants.DATETIME_TYPE,
              YamlConstants.TIME_TYPE,
              YamlConstants.DATE_TIME_TYPE),
    DOUBLE(new DoubleTransformation(),
           MatlabType.SCALAR,
           LiteralDoubleBinding.class,
           "xs:double",
           YamlConstants.DOUBLE_TYPE),
    FLOAT(new FloatTransformation(),
          MatlabType.SCALAR,
          LiteralFloatBinding.class,
          "xs:float",
          YamlConstants.FLOAT_TYPE),
    BYTE(new ByteTransformation(),
         MatlabType.SCALAR,
         LiteralByteBinding.class,
         "xs:byte",
         YamlConstants.BYTE_TYPE),
    SHORT(new ShortTransformation(),
          MatlabType.SCALAR,
          LiteralShortBinding.class,
          "xs:short",
          YamlConstants.SHORT_TYPE),
    INT(new IntegerTransformation(),
        MatlabType.SCALAR,
        LiteralIntBinding.class,
        "xs:int",
        YamlConstants.INT_TYPE,
        YamlConstants.INTEGER_TYPE),
    LONG(new LongTransformation(),
         MatlabType.SCALAR,
         LiteralLongBinding.class,
         "xs:long",
         YamlConstants.LONG_TYPE),
    STRING(new StringTransformation(),
           MatlabType.SCALAR,
           LiteralStringBinding.class,
           "xs:string",
           YamlConstants.STRING_TYPE,
           YamlConstants.TEXT_TYPE),
    URI(new URITransformation(),
        MatlabType.STRING,
        LiteralAnyURIBinding.class,
        "xs:anyURI",
        YamlConstants.URL,
        YamlConstants.URI);
    private final Set<String> values;
    private final LiteralTransformation transformation;
    private final MatlabType matlabType;
    private final String xmlType;
    private final Class<? extends IData> bindingClass;

    private LiteralType(LiteralTransformation transformation,
                        MatlabType type,
                        Class<? extends IData> bindingClass,
                        String xmlType,
                        String... values) {
        this.values = Sets.newHashSet(values);
        this.transformation = transformation;
        this.matlabType = type;
        this.xmlType = xmlType;
        this.bindingClass = bindingClass;
    }

    private Set<String> getValues() {
        return Collections.unmodifiableSet(values);
    }

    boolean isNumber() {
        return matlabType == MatlabType.SCALAR;
    }

    LiteralTransformation getTransformation() {
        return transformation;
    }

    public MatlabType getMatlabType() {
        return matlabType;
    }

    public String getXmlType() {
        return xmlType;
    }

    public Class<? extends IData> getBindingClass() {
        return bindingClass;
    }

    public static LiteralType of(String name) {
        String lower = name.toLowerCase();
        for (LiteralType t : LiteralType.values()) {
            if (t.getValues().contains(lower)) {
                return t;
            }
        }
        return null;
    }

}
