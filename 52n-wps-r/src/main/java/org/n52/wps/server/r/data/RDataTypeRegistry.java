/*
 * Copyright (C) 2010-2017 52°North Initiative for Geospatial Open Source
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.n52.wps.server.r.syntax.RAnnotationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 *
 * @author Daniel Nüst
 *
 */
@Component
public class RDataTypeRegistry {

    private static Logger LOGGER = LoggerFactory.getLogger(RDataTypeRegistry.class);

    private HashMap<String, RTypeDefinition> customDataTypes = new HashMap<String, RTypeDefinition>();

    private HashMap<String, RTypeDefinition> rDataTypeKeys = new HashMap<String, RTypeDefinition>();

    private HashMap<String, RTypeDefinition> rDataTypeAlias = new HashMap<String, RTypeDefinition>();

    @Deprecated
    public RDataTypeRegistry() {
        // register types from enum
        LOGGER.info("NEW {}", this);
        RDataType[] values = RDataType.values();
        for (RDataType type : values) {
            register(type);
        }
        logDataTypeTable();
    }

    public void register(RDataType type) {
        this.rDataTypeKeys.put(type.getKey(), type);

        // put process key, i.e. mimetype or xml-notation for literal type, as
        // alternative key (alias) into Hashmap:
        if ( !containsKey(type.getMimeType())) {
            this.rDataTypeAlias.put(type.getMimeType(), type);
        } else {
            LOGGER.warn("Doubled definition of data type-key for notation: "
                    + type.getMimeType()
                    + "\n"
                    + "only the first definition will be used for this key.+"
                    + "(That might be the usual case if more than one annotation type key refer to one WPS-mimetype with different data handlers)");
        }
    }

    public boolean containsKey(String key) {
        return this.rDataTypeKeys.containsKey(key) || this.rDataTypeAlias.containsKey(key);
    }

    /**
     * This method is important for parsers to request the meaning of a specific key
     *
     * @param key
     *        process keys and self defined short keys are recognized as dataType keys
     * @return the <code>RTypeDefinition</code> belonging to the key
     * @throws RAnnotationException if an invalid key was passed
     */
    public RTypeDefinition getType(String key) throws RAnnotationException {
        RTypeDefinition out = this.rDataTypeKeys.get(key);
        if (out == null) {
            out = this.rDataTypeAlias.get(key);
        }
        if (out == null) {
            out = this.customDataTypes.get(key);
        }
        if (out == null) {
            throw new RAnnotationException("Invalid datatype key for R script annotations: " + key);
        }

        return out;
    }

    public Collection<RTypeDefinition> getDefinitions() {
        ArrayList<RTypeDefinition> definitions = new ArrayList<RTypeDefinition>();
        definitions.addAll(this.rDataTypeKeys.values());
        definitions.addAll(getCustomDataTypes());
        return definitions;
    }

    public Collection<RTypeDefinition> getCustomDataTypes() {
        return this.customDataTypes.values();
    }

    /**
     * Deletes all registered custom type definitions (Useful for instance, if the config file was changed)
     */
    public void clearCustomDataTypes() {
        this.customDataTypes.clear();
    }

    public void logDataTypeTable() {

        int maxLengthKeys = "Key".length();
        int maxLengthMimetype = "Mimetype".length();
        int maxLengthSchema = "Schema".length();
        int maxLengthEncoding = "Encoding".length();
        int maxLengthComplex = "isComplex".length();
        int maxLengthDataBinding = "DataBinding".length();

        Collection<RTypeDefinition> definitions = getDefinitions();
        for (RTypeDefinition definition : definitions) {
            maxLengthKeys = Math.max(maxLengthKeys, getLengthOf(definition.getKey()));
            maxLengthMimetype = Math.max(maxLengthMimetype, getLengthOf(definition.getMimeType()));
            maxLengthSchema = Math.max(maxLengthSchema, getLengthOf(definition.getSchema()));
            maxLengthEncoding = Math.max(maxLengthEncoding, getLengthOf(definition.getEncoding()));
            maxLengthComplex = Math.max(maxLengthComplex, getLengthOf(Boolean.toString(definition.isComplex())));
            maxLengthDataBinding = Math.max(maxLengthDataBinding, getLengthOf(definition.getClass().getSimpleName()));
        }

        StringBuilder sb = new StringBuilder("RDataTypeRegistry: \n");
        sb.append("Key").append(getSpacesToFillGapFor("Key", maxLengthKeys));
        sb.append("MimeType").append(getSpacesToFillGapFor("MimeType", maxLengthMimetype));
        sb.append("Schema").append(getSpacesToFillGapFor("Schema", maxLengthSchema));
        sb.append("Encoding").append(getSpacesToFillGapFor("Encoding", maxLengthEncoding));
        sb.append("isComplex").append(getSpacesToFillGapFor("isComplex", maxLengthComplex));
        sb.append("DataBinding").append(getSpacesToFillGapFor("DataBinding", maxLengthDataBinding));

        sb.append("\n");
        int tablewidth = sb.length();
        for (int i = 0 ; i < tablewidth ; i++) {
            sb.append("-"); // underlines header
        }
        sb.append("\n");

        for (RTypeDefinition type : definitions) {
            final String key = type.getKey();
            final String mimeType = type.getMimeType();
            final String schema = type.getSchema();
            final String encoding = type.getEncoding();
            final String complex = Boolean.toString(type.isComplex());
            final String dataBinding = type.getClass().getSimpleName();

            sb.append(key).append(getSpacesToFillGapFor(key, maxLengthKeys));
            sb.append(mimeType).append(getSpacesToFillGapFor(mimeType, maxLengthMimetype));
            sb.append(schema).append(getSpacesToFillGapFor(schema, maxLengthSchema));
            sb.append(encoding).append(getSpacesToFillGapFor(encoding, maxLengthEncoding));
            sb.append(complex).append(getSpacesToFillGapFor(complex, maxLengthComplex));
            sb.append(dataBinding).append(getSpacesToFillGapFor(dataBinding, maxLengthDataBinding));
            sb.append("\n");
        }

        LOGGER.info("R Data Type Mapping: {}", sb.toString());
    }

    private int getLengthOf(String text) {
        return text != null ? text.length() : 4; // null -> 4 characters
    }

    private String getSpacesToFillGapFor(String text, int columnWidth) {
        if (text == null) {
            text = "null";
        }
        if (text.length() > columnWidth) {
            throw new IllegalArgumentException("'" + text + "' does not fit into column of with " + columnWidth);
        }

        String spaces = "";
        int countSpaces = columnWidth - text.length();
        for (int i = 0 ; i < countSpaces ; i++) {
            spaces += " ";
        }
        spaces += "  "; // separate columns
        return spaces;
    }

    public void register(CustomDataType type) {
        this.customDataTypes.put(type.getKey(), type);
        LOGGER.debug("New custom data type registered: {}", type);
    }

}
