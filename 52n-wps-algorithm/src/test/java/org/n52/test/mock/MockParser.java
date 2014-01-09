/**
 * ﻿Copyright (C) 2007
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */
package org.n52.test.mock;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.n52.wps.FormatDocument.Format;
import org.n52.wps.io.IParser;
import org.n52.wps.io.data.IData;

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
