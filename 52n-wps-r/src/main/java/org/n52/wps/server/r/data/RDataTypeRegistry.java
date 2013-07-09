/**
 * ﻿Copyright (C) 2010
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

package org.n52.wps.server.r.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.n52.wps.server.r.syntax.RAnnotationException;

public class RDataTypeRegistry {

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private static RDataTypeRegistry instance = new RDataTypeRegistry();

    private RDataTypeRegistry() {

    }

    public static RDataTypeRegistry getInstance() {
        if (instance == null) {
            instance = new RDataTypeRegistry();
        }
        return instance;
    }

    private HashMap<String, RTypeDefinition> customDataTypes = new HashMap<String, RTypeDefinition>();
    private HashMap<String, RTypeDefinition> rDataTypeKeys = new HashMap<String, RTypeDefinition>();
    private HashMap<String, RTypeDefinition> rDataTypeAlias = new HashMap<String, RTypeDefinition>();

    // TODO: Eventually throw Exceptions here?
    public void register(RDataType type) {
        this.rDataTypeKeys.put(type.getKey(), type);

        // put process key, i.e. mimetype or xml-notation for literal type, as alternative key (alias) into
        // Hashmap:
        if ( !containsKey(type.getProcessKey()))
            this.rDataTypeAlias.put(type.getProcessKey(), type);
        else
            this.LOGGER.warn("Doubled definition of data type-key for notation: "
                    + type.getProcessKey()
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

    public static RTypeDefinition test = RDataType.DOUBLE;

    private static String addTabbs(String s, int nmax) {
        int n = nmax - s.length();
        String out = "";
        for (int i = 0; i < n; i++) {
            out += " ";
        }
        return out;
    }

    public String toString() {
        String out = "Key\t\t    MimeType\t\t\t\t    Schema\tEncoding   isComplex\tDataBinding";
        out += "\n-------------------------------------------------------------------------------------------------";
        out += "---------------------------";

        Collection<RTypeDefinition> definitions = getInstance().getDefinitions();
        String complex = "";
        String literal = "";

        for (RTypeDefinition type : definitions) {
            String temp = "";
            temp += "\n";
            String val = type.getKey();
            temp += val + addTabbs("" + val, 20);

            val = type.getProcessKey();
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

    public static void main(String[] args) {

        System.out.println(RDataTypeRegistry.getInstance());
    }

    public void register(CustomDataType type) {
        this.customDataTypes.put(type.getKey(), type);

    }

    /**
     * Deletes all registered custom type definitions (Useful for instance, if the config file was changed)
     */
    public void clearCustomDataTypes() {
        this.customDataTypes.clear();
    }

}
