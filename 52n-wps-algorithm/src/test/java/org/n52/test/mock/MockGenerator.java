/**
 * ﻿Copyright (C) 2007 - 2014 52°North Initiative for Geospatial Open Source
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
package org.n52.test.mock;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.n52.wps.FormatDocument.Format;
import org.n52.wps.io.IGenerator;
import org.n52.wps.io.data.IData;

/**
 *
 * @author tkunicki
 */
public class MockGenerator implements IGenerator {

    public final static List<String> schemaSet;
    public final static List<String> formatSet;
    public final static List<String> encodingSet;

    static {
        schemaSet = Collections.unmodifiableList(new ArrayList(MockUtil.getParserSupportedSchemas(MockParser.class)));
        formatSet = Collections.unmodifiableList(new ArrayList(MockUtil.getParserSupportedFormats(MockParser.class)));
        encodingSet = Collections.unmodifiableList(new ArrayList(MockUtil.getParserSupportedEncodings(MockParser.class)));
    }

//    @Override
//    public OutputStream generate(IData coll) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    @Override
//    public Class[] getSupportedInternalInputDataType() {
//        return new Class[] { MockBinding.class };
//    }
    
    @Override
    public boolean isSupportedSchema(String schema) {
        return schemaSet.contains(schema);
    }

    @Override
    public boolean isSupportedFormat(String format) {
        return formatSet.contains(format);
    }

    @Override
    public boolean isSupportedEncoding(String encoding) {
        return encodingSet.contains(encoding);
    }

    @Override
    public String[] getSupportedSchemas() {
        return schemaSet.toArray(new String[0]);
    }

    @Override
    public String[] getSupportedFormats() {
        return formatSet.toArray(new String[0]);
    }

    @Override
    public String[] getSupportedEncodings() {
        return encodingSet.toArray(new String[0]);
    }

    @Override
    public InputStream generateStream(IData data, String mimeType, String schema) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public InputStream generateBase64Stream(IData data, String mimeType, String schema) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isSupportedDataBinding(Class<?> clazz) {
        return Arrays.binarySearch(getSupportedDataBindings(), clazz) > -1;
    }

    @Override
    public Format[] getSupportedFullFormats() {
        Format f = Format.Factory.newInstance();
        f.setSchema(schemaSet.get(0));
        f.setEncoding(encodingSet.get(0));
        f.setMimetype(formatSet.get(0));
        return new Format[] {f};
    }

    @Override
    public Class<?>[] getSupportedDataBindings() {
        return new Class[] { MockBinding.class };
    }

}
