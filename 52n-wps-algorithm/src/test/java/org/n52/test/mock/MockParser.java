/**
 * ﻿Copyright (C) 2007 - 2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.n52.test.mock;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.n52.wps.io.IParser;
import org.n52.wps.io.data.IData;
import org.n52.wps.webapp.api.FormatEntry;

/**
 *
 * @author tkunicki
 */
public class MockParser implements IParser {

    public final static List<String> schemaSet;
    public final static List<String> formatSet;
    public final static List<String> encodingSet;

    static {
        schemaSet = Collections.unmodifiableList(new ArrayList(MockUtil.getParserSupportedSchemas(MockParser.class)));
        formatSet = Collections.unmodifiableList(new ArrayList(MockUtil.getParserSupportedFormats(MockParser.class)));
        encodingSet = Collections.unmodifiableList(new ArrayList(MockUtil.getParserSupportedEncodings(MockParser.class)));
    }

//    @Override
//    public IData parse(InputStream input, String mimeType) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    @Override
//    public Class[] getSupportedInternalOutputDataType() {
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
    public IData parse(InputStream input, String mimeType, String schema) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public IData parseBase64(InputStream input, String mimeType, String schema) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isSupportedDataBinding(Class<?> clazz) {
        return Arrays.binarySearch(getSupportedDataBindings(), clazz) > -1;
    }

    @Override
    public List<FormatEntry> getSupportedFullFormats() {
    	FormatEntry f = new FormatEntry(formatSet.get(0), schemaSet.get(0), encodingSet.get(0), true);
    	List<FormatEntry> result = new ArrayList<>();
    	result.add(f);
        return result;
    }

    @Override
    public Class<?>[] getSupportedDataBindings() {
        return new Class[] { MockBinding.class };
    }

}
