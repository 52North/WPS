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

package org.n52.wps.server.r.syntax;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.n52.wps.io.data.IData;
import org.n52.wps.server.r.data.RDataTypeRegistry;
import org.n52.wps.server.r.data.RTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines Syntax and Semantics for Annotations in R Skripts
 * 
 * Syntax in (raw) BNF: <RAnnotation> ::= <StartKey> <AttributeSequence> <EndKey> <StartKey> <Attributequence>
 * ::= <RAnnotationTypeInstance>.getStartKey() <RAnnotationTypeInstance>.getAttributeSequence() <EndKey> ::=
 * RSeparator.ANNOTATION_END.getKey() <AttributeSequence> ::= {<RAttributeInstance>.getKey()
 * ATRIBUTE_VALUE_SEPARATOR} <Attributevalue> {ATTRIBUTE_SEPARATOR <RAttributeSequence>}
 * 
 * @author Matthias Hinz
 */
public class RAnnotation {

    private RAnnotationType type;

    private HashMap<RAttribute, Object> attributeHash = new HashMap<RAttribute, Object>();

    private RDataTypeRegistry dataTypeRegistry;

    private static Logger LOGGER = LoggerFactory.getLogger(RAnnotation.class);

    /**
     * 
     * @param type
     * @param attributeHash
     * @throws IOException
     *         if AttributHash is not valid for any cause
     * @throws RAnnotationException
     */
    public RAnnotation(RAnnotationType type, HashMap<RAttribute, Object> attributeHash, RDataTypeRegistry registry) throws
            RAnnotationException {
        super();
        this.type = type;
        this.attributeHash.putAll(attributeHash);
        this.type.validateDescription(this);
        this.dataTypeRegistry = registry;

        LOGGER.debug("NEW {}", toString());
    }

    public RAnnotationType getType() {
        return this.type;
    }

    /**
     * 
     * @param attr
     * @return Returns Attribute value as Java Object in case it is more complex
     * @throws RAnnotationException
     */
    public Object getObjectValue(RAttribute attr) throws RAnnotationException {
        Object out = this.attributeHash.get(attr);

        if (out == null && attr.getDefValue() != null)
            out = attr.getDefValue();
        else if (attr == RAttribute.ENCODING)
            return getRDataType().getEncoding();
        if (attr == RAttribute.SCHEMA)
            return getRDataType().getSchema();
        return out;
    }

    /**
     * 
     * @param attr
     * @return Returns an attribute value as string. Suits for most literal data types
     * @throws RAnnotationException
     */
    public String getStringValue(RAttribute attr) throws RAnnotationException {
        Object value = getObjectValue(attr);
        if (value == null)
            return null;

        return value.toString();
    }

    public static List<RAnnotation> filterAnnotations(Collection<RAnnotation> annotations,
                                                      RAnnotationType type,
                                                      RAttribute attribute,
                                                      String value) throws RAnnotationException {
        LinkedList<RAnnotation> out = new LinkedList<RAnnotation>();
        for (RAnnotation annotation : annotations) {
            // type filter:
            if (type == null || annotation.getType() == type) {
                // attribute - value filter:
                if (attribute == null || value == null || annotation.getStringValue(attribute).equalsIgnoreCase(value)) {
                    out.add(annotation);
                }
            }
        }
        return out;
    }

    public static List<RAnnotation> filterAnnotations(List<RAnnotation> annotations, RAttribute attribute, String value) throws RAnnotationException {
        return filterAnnotations(annotations, null, attribute, value);
    }

    public static List<RAnnotation> filterAnnotations(List<RAnnotation> annotations, RAnnotationType type) throws RAnnotationException {
        return filterAnnotations(annotations, type, null, null);
    }

    public static RAnnotation filterFirstMatchingAnnotation(List<RAnnotation> annotations,
                                                            RAttribute attribute,
                                                            String value) throws RAnnotationException {
        Iterator<RAnnotation> iterator = filterAnnotations(annotations, null, attribute, value).iterator();
        if (iterator.hasNext())
            return iterator.next();

        return null;
    }

    public static RAnnotation filterFirstMatchingAnnotation(List<RAnnotation> annotations, RAnnotationType type) throws RAnnotationException {
        Iterator<RAnnotation> iterator = filterAnnotations(annotations, type, null, null).iterator();
        if (iterator.hasNext())
            return iterator.next();

        return null;
    }

    /**
     * 
     * @param rClass
     *        - value referring to RAttribute.TYPE
     * @return null or supported IData class for rClass - string
     * @throws RAnnotationException
     */
    public Class< ? extends IData> getDataClass(String rClass) throws RAnnotationException {
        RTypeDefinition rType = dataTypeRegistry.getType(rClass);
        return rType.getIDataClass();
    }

    public Class< ? extends IData> getDataClass() throws RAnnotationException {
        String rClass = getStringValue(RAttribute.TYPE);
        return getDataClass(rClass);
    }

    /**
     * Checks if the type - argument of an annotation refers to complex data
     */
    public boolean isComplex(String rClass) throws RAnnotationException {
        return dataTypeRegistry.getType(rClass).isComplex();

    }

    public RTypeDefinition getRDataType() throws RAnnotationException {
        return dataTypeRegistry.getType(getStringValue(RAttribute.TYPE));
    }

    /**
     * @return true, if the type attribute of an Annotation refers to a complex data type
     * @throws RAnnotationException
     */
    public boolean isComplex() throws RAnnotationException {
        return isComplex(this.getStringValue(RAttribute.TYPE));
    }

    /**
     * 
     * @return null or supported ProcessdescriptionType
     * @throws RAnnotationException
     */
    public String getProcessDescriptionType() throws RAnnotationException {
        String type = getStringValue(RAttribute.TYPE);
        RTypeDefinition rdt = dataTypeRegistry.getType(type);
        if (rdt != null)
            return rdt.getMimeType();

        return null;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RAnnotation [type=");
        builder.append(this.type);
        builder.append(", attributeHash=");
        builder.append(this.attributeHash);
        builder.append("]");
        return builder.toString();
    }

    public boolean containsKey(RAttribute key) {
        return this.attributeHash.containsKey(key);
    }

    public void setAttribute(RAttribute key, Object value) {
        this.attributeHash.put(key, value);
    }
}
