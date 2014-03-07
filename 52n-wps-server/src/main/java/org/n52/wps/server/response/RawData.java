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

package org.n52.wps.server.response;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.n52.wps.io.IOHandler;
import org.n52.wps.io.data.IBBOXData;
import org.n52.wps.io.data.IComplexData;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.ILiteralData;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.util.XMLBeansHelper;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.primitives.Doubles;

/*
 * @author foerster
 *
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
                   ProcessDescriptionType description)
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
                appendAttr(builder, "dimension", bbox.getDimension());
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
