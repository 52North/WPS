/**
 * Copyright (C) 2010-2015 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.server.r.syntax;

/**
 * attributes used in Annotations
 * 
 * @author Matthias Hinz
 * 
 */
public enum RAttribute {

    INPUT_START("wps.in", null, true), OUTPUT_START("wps.out", null, true), DESCRIPTION_START("wps.des", null, true), RESOURCE_START(
            "wps.res", null, true), IDENTIFIER("id", null, true), TYPE("type", null, true), TITLE("title", null,
            false), VERSION("version", null, false), ABSTRACT("abstract", null, false), MIN_OCCURS("minOccurs", 1, true), MAX_OCCURS(
            "maxOccurs", 1, true), DEFAULT_VALUE("value", null, false), METADATA("meta", null, false), MIMETYPE(
            "mimetype", null, false), SCHEMA("schema", null, false), ENCODING("encoding", null, false), AUTHOR(
            "author", null, false),
    // A sequence of values:
    NAMED_LIST("seq", null, true),
    // derives from a named list
    NAMED_LIST_R_SYNTAX("rseq", null, true),
    // imports:
    IMPORT_START("wps.import", null, false),
    // metadata links:
    HREF("href", null, true), METADATA_START("wps.metadata", null, false);

    private String key;

    private Object defValue;

    private RAttribute(String key, Object defValue, boolean mandatory) {
        this.key = key.toLowerCase();
        this.defValue = defValue;
        this.mandatory = mandatory;
    }

    public String getKey() {
        return this.key;
    }

    public Object getDefValue() {
        return this.defValue;
    }

    /**
     * @return true if attribute has to occur in Process description, if so, there has to be a standard value
     *         or a value in R Annotion given
     */
    public boolean isMandatory() {
        return this.mandatory;
    }

    private boolean mandatory;

    @Override
    public String toString() {
        return getKey();
    }
}