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

    private static Logger LOGGER = LoggerFactory.getLogger(CustomDataTypeManager.class);

    public RDataTypeRegistry() {
        // register types from enum
        RDataType[] values = RDataType.values();
        for (RDataType type : values) {
            register(type);
        }

        LOGGER.info("NEW {}", this);
    }

    private HashMap<String, RTypeDefinition> customDataTypes = new HashMap<String, RTypeDefinition>();

    private HashMap<String, RTypeDefinition> rDataTypeKeys = new HashMap<String, RTypeDefinition>();

    private HashMap<String, RTypeDefinition> rDataTypeAlias = new HashMap<String, RTypeDefinition>();

    public void register(RDataType type) {
        this.rDataTypeKeys.put(type.getKey(), type);

        // put process key, i.e. mimetype or xml-notation for literal type, as
        // alternative key (alias) into Hashmap:
        if ( !containsKey(type.getMimeType()))
            this.rDataTypeAlias.put(type.getMimeType(), type);
        else
            LOGGER.warn("Doubled definition of data type-key for notation: "
                    + type.getMimeType()
                    + "\n"
                    + "only the first definition will be used for this key.+"
                    + "(That might be the usual case if more than one annotation type key refer to one WPS-mimetype with different data handlers)");
    }

    public boolean containsKey(String key) {
        return this.rDataTypeKeys.containsKey(key) || this.rDataTypeAlias.containsKey(key);
    }

    /**
     * This method is important for parsers to request the meaning of a specific key
     * 
     * @param key
     *        process keys and self defined short keys are recognized as dataType keys
     * @return
     * @throws RAnnotationException
     */
    public RTypeDefinition getType(String key) throws RAnnotationException {
        RTypeDefinition out = this.rDataTypeKeys.get(key);
        if (out == null)
            out = this.rDataTypeAlias.get(key);
        if (out == null)
            out = this.customDataTypes.get(key);
        if (out == null)
            throw new RAnnotationException("Invalid datatype key for R script annotations: " + key);

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

    private static String addTabbs(String s, int nmax) {
        int n = nmax - s.length();
        String out = "";
        for (int i = 0; i < n; i++) {
            out += " ";
        }
        return out;
    }

    @Override
    public String toString() {
        String out = "RDataTypeRegistry:\nKey\t\t    MimeType\t\t\t\t    Schema\tEncoding   isComplex\tDataBinding";
        out += "\n-------------------------------------------------------------------------------------------------";
        out += "---------------------------";

        Collection<RTypeDefinition> definitions = getDefinitions();
        String complex = "";
        String literal = "";

        for (RTypeDefinition type : definitions) {
            String temp = "";
            temp += "\n";
            String val = type.getKey();
            temp += val + addTabbs("" + val, 20);

            val = type.getMimeType();
            temp += val + addTabbs("" + val, 40);

            val = type.getSchema();
            temp += val + addTabbs("" + val, 12);

            val = type.getEncoding();
            temp += val + addTabbs("" + val, 12);

            val = "" + type.isComplex();
            temp += val + addTabbs("" + val, 12);

            val = type.getIDataClass().getSimpleName();
            temp += val + addTabbs("" + val, 12);

            if (type.isComplex())
                complex += temp;
            else
                literal += temp;
        }
        return out + literal + complex;
    }

    public void register(CustomDataType type) {
        this.customDataTypes.put(type.getKey(), type);
        LOGGER.debug("New custom data type registered: {}", type);
    }

    /**
     * Deletes all registered custom type definitions (Useful for instance, if the config file was changed)
     */
    public void clearCustomDataTypes() {
        this.customDataTypes.clear();
    }

}
