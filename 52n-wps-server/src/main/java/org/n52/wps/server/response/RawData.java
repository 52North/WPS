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
package org.n52.wps.server.response;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.n52.wps.io.IOHandler;
import org.n52.wps.io.data.IBBOXData;
import org.n52.wps.io.data.IComplexData;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.ILiteralData;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.ProcessDescription;
import org.n52.wps.util.XMLBeansHelper;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.primitives.Doubles;

/*
 * @author foerster
 * TODO adjust for WPS 2.0
 */
public class RawData extends ResponseData {
    public static final Joiner SPACE_JOINER = Joiner.on(" ");

	/**
	 * @param obj
	 * @param id
	 * @param schema
	 * @param encoding
	 * @param mimeType
	 */
    public RawData(IData obj, String id, String schema, String encoding,
                   String mimeType, String algorithmIdentifier,
                   ProcessDescription description)
            throws ExceptionReport {
        super(obj, id, schema, encoding, mimeType, algorithmIdentifier, description);
        if (obj instanceof IComplexData) {
            prepareGenerator();
        }
    }

    public InputStream getAsStream() throws ExceptionReport {
        try {
            if(obj instanceof ILiteralData){
                return new ByteArrayInputStream(String.valueOf(obj.getPayload()).getBytes(Charsets.UTF_8));
            }
            if(obj instanceof IBBOXData){
                IBBOXData bbox  = (IBBOXData) obj;
                StringBuilder builder = new StringBuilder();

                builder.append("<wps:BoundingBoxData");
                appendAttr(builder, "xmlns:ows", XMLBeansHelper.NS_OWS_1_1);
                appendAttr(builder, "xmlns:wps", XMLBeansHelper.NS_WPS_1_0_0);
                if (bbox.getCRS() != null) {
                    appendAttr(builder, "crs", escape(bbox.getCRS()));
                }
                appendAttr(builder, "dimensions", bbox.getDimension());
                builder.append(">");
                builder.append("\n\t");
                builder.append("<ows:LowerCorner>");
                SPACE_JOINER.appendTo(builder, Doubles.asList(bbox.getLowerCorner()));
                builder.append("</ows:LowerCorner>");
                builder.append("\n\t");
                builder.append("<ows:UpperCorner>");
                SPACE_JOINER.appendTo(builder, Doubles.asList(bbox.getUpperCorner()));
                builder.append("</ows:UpperCorner>");
                builder.append("\n");
                builder.append("</wps:BoundingBoxData>");
                return new ByteArrayInputStream(builder.toString().getBytes(Charsets.UTF_8));
            }
            //complexdata
            if(encoding == null || "".equals(encoding) || encoding.equalsIgnoreCase(IOHandler.DEFAULT_ENCODING)){
                return generator.generateStream(obj, mimeType, schema);
            }
            else if(encoding.equalsIgnoreCase(IOHandler.ENCODING_BASE64)){
                return generator.generateBase64Stream(obj, mimeType, schema);

            }
        } catch (IOException e) {
            throw new ExceptionReport("Error while generating Complex Data out of the process result", ExceptionReport.NO_APPLICABLE_CODE, e);
        }
        throw new ExceptionReport("Could not determine encoding. Use default (=not set) or base64", ExceptionReport.NO_APPLICABLE_CODE);
    }

    private StringBuilder appendAttr(StringBuilder builder, String key, Object value) {
        return builder.append(' ').append(key).append('=')
                .append('"').append(value).append('"');
    }

    private static String escape(String s) {
        return s.replaceAll("&", "&amp;")
                .replaceAll("\"", "&quot;")
                .replaceAll("'", "&apos;")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;");
    }
}
