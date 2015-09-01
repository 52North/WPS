/**
 * ﻿Copyright (C) 2010 - 2014 52°North Initiative for Geospatial Open Source
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

package org.n52.wps.server.r.data;

import org.n52.wps.io.data.GenericFileDataConstants;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.complex.GenericFileDataWithGTBinding;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;

/**
 * Data types which are supported by scripts Note that every IData class must be parsed from an to are to be
 * handled successful --> GenericRProcess TODO: restructure dependent classes & methods for new attributes
 * 
 * FIXME use either this class or the file R_Datatype.conf, potentially refactor the format of the file.
 */
public enum RDataType implements RTypeDefinition {

    // literal data:
    STRING("string", "xs:string", LiteralStringBinding.class), CHARACTER("character", "xs:string",
            LiteralStringBinding.class), INTEGER("integer", "xs:integer", LiteralIntBinding.class), DOUBLE("double",
            "xs:double", LiteralDoubleBinding.class), BOOLEAN("boolean", "xs:boolean", LiteralBooleanBinding.class),

    // geodata:
    DBASE("dbf", GenericFileDataConstants.MIME_TYPE_DBASE, GenericFileDataWithGTBinding.class, true, null, "base64"), DGN(
            "dgn", GenericFileDataConstants.MIME_TYPE_DGN, GenericFileDataWithGTBinding.class, true, null, "base64"), GEOTIFF(
            "geotiff", GenericFileDataConstants.MIME_TYPE_GEOTIFF, GenericFileDataWithGTBinding.class, true, null,
            "base64"), GEOTIFF2("geotiff_image", GenericFileDataConstants.MIME_TYPE_IMAGE_GEOTIFF,
            GenericFileDataWithGTBinding.class, true, null, "base64"), GEOTIFF_X("geotiff_x",
            GenericFileDataConstants.MIME_TYPE_X_GEOTIFF, GenericFileDataWithGTBinding.class, true, null, "base64"), IMG(
            "img", GenericFileDataConstants.MIME_TYPE_HDF, GenericFileDataWithGTBinding.class, true, null, "base64"), IMG2(
            "img_x", GenericFileDataConstants.MIME_TYPE_X_ERDAS_HFA, GenericFileDataWithGTBinding.class, true, null,
            "base64"), NETCDF("netcdf", GenericFileDataConstants.MIME_TYPE_NETCDF, GenericFileDataWithGTBinding.class,
            true, null, "base64"), NETCDF_X("netcdf_x", GenericFileDataConstants.MIME_TYPE_X_NETCDF,
            GenericFileDataWithGTBinding.class, true, null, "base64"), REMAP("remap",
            GenericFileDataConstants.MIME_TYPE_REMAPFILE, GenericFileDataWithGTBinding.class, true, null, "base64"), SHAPE(
            "shp", GenericFileDataConstants.MIME_TYPE_SHP, GenericFileDataWithGTBinding.class, true, null, "base64"),
    // SHAPE_ZIP("shp_zip",GenericFileDataConstants.MIME_TYPE_ZIPPED_SHP,
    // GenericFileDataBinding.class,
    // true),
    SHAPE_ZIP2("shp_x", GenericFileDataConstants.MIME_TYPE_ZIPPED_SHP, GTVectorDataBinding.class, true, null, "base64"), KML(
            "kml", GenericFileDataConstants.MIME_TYPE_KML, GenericFileDataWithGTBinding.class, true, null, "UTF-8"),

    // graphical data
    GIF("gif", GenericFileDataConstants.MIME_TYPE_IMAGE_GIF, GenericFileDataBinding.class, true, null, "base64"),

    JPEG("jpeg", GenericFileDataConstants.MIME_TYPE_IMAGE_JPEG, GenericFileDataBinding.class, true, null, "base64"),

    JPEG2("jpg", GenericFileDataConstants.MIME_TYPE_IMAGE_JPEG, GenericFileDataBinding.class, true, null, "base64"),

    PNG("png", GenericFileDataConstants.MIME_TYPE_IMAGE_PNG, GenericFileDataBinding.class, true, null, "base64"),

    TIFF("tiff", GenericFileDataConstants.MIME_TYPE_TIFF, GenericFileDataBinding.class, true, null, "base64"),

    // file data and xml:
    TEXT_PLAIN("text", GenericFileDataConstants.MIME_TYPE_PLAIN_TEXT, GenericFileDataBinding.class, true), TEXT_XML(
            "xml", GenericFileDataConstants.MIME_TYPE_TEXT_XML, GenericFileDataBinding.class, true),

    FILE("file", "application/unknown", GenericFileDataBinding.class), PDF("pdf", "application/pdf",
            GenericFileDataBinding.class, true, null, null), // "base64"),
    STY("sty", "application/sty", GenericFileDataBinding.class, true, null, "base64"), RNW("rnw", "application/rnw",
            GenericFileDataBinding.class, true, null, "base64");
    // TEXT_XML2("text_xml", GenericFileDataConstants.MIME_TYPE_TEXT_XML,
    // GenericFileDataBinding.class,true);

    private String key;

    private String processKey;

    private Class< ? extends IData> iDataClass;

    private boolean isComplex;

    // private static final Logger log = LoggerFactory.getLogger(RDataType.class);

    String schema;

    String encoding = "UTF-8";

    private RDataType(String key,
                      String processKey,
                      Class< ? extends IData> iDataClass,
                      boolean isComplex,
                      String schema,
                      String encoding) {
        this.key = key;
        this.processKey = processKey;
        this.iDataClass = iDataClass;
        this.isComplex = isComplex;
        this.schema = schema;
        this.encoding = encoding;
    }

    private RDataType(String key, String mimeType, Class< ? extends IData> iDataClass, boolean isComplex) {
        this.key = key;
        this.processKey = mimeType;
        this.iDataClass = iDataClass;
        this.isComplex = isComplex;
    }

    private RDataType(String key, String mimeType, Class< ? extends IData> iDataClass) {
        this.key = key;
        this.processKey = mimeType;
        this.iDataClass = iDataClass;
        this.isComplex = false;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getMimeType() {
        return this.processKey;
    }

    @Override
    public boolean isComplex() {
        return this.isComplex;
    }

    @Override
    public String getEncoding() {
        if (this.isComplex)
            return this.encoding;
        return null;
    }

    @Override
    public String getSchema() {
        return this.schema;
    }

    @Override
    public Class< ? extends IData> getIDataClass() {
        return this.iDataClass;
    }
}