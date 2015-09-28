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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Describes each annotation type considering attributes, their order and behavior
 * 
 */
public enum RAnnotationType {

    INPUT(Arrays.asList(RAttribute.INPUT_START,
                        RAttribute.IDENTIFIER,
                        RAttribute.TYPE,
                        RAttribute.TITLE,
                        RAttribute.ABSTRACT,
                        RAttribute.DEFAULT_VALUE,
                        RAttribute.MIN_OCCURS,
                        RAttribute.MAX_OCCURS)),

    OUTPUT(Arrays.asList(RAttribute.OUTPUT_START,
                         RAttribute.IDENTIFIER,
                         RAttribute.TYPE,
                         RAttribute.TITLE,
                         RAttribute.ABSTRACT)),

    DESCRIPTION(Arrays.asList(RAttribute.DESCRIPTION_START,
                              RAttribute.IDENTIFIER,
                              RAttribute.TITLE,
                              RAttribute.VERSION,
                              RAttribute.ABSTRACT,
                              RAttribute.AUTHOR)),

    RESOURCE(Arrays.asList(RAttribute.RESOURCE_START, RAttribute.NAMED_LIST)),

    IMPORT(Arrays.asList(RAttribute.IMPORT_START, RAttribute.NAMED_LIST)), METADATA(
            Arrays.asList(RAttribute.METADATA_START, RAttribute.TITLE, RAttribute.HREF));

    private HashMap<String, RAttribute> attributeLut = new HashMap<String, RAttribute>();

    private HashSet<RAttribute> mandatory = new HashSet<RAttribute>();

    private RAttribute startKey;

    private List<RAttribute> attributeSequence;

    private RAnnotationType(List<RAttribute> attributeSequence) {
        this.startKey = attributeSequence.get(0);
        this.attributeSequence = attributeSequence;

        for (RAttribute attribute : this.attributeSequence) {
            this.attributeLut.put(attribute.getKey(), attribute);
            if (attribute.isMandatory()) {
                this.mandatory.add(attribute);
            }
        }
    }

    public RAttribute getStartKey() {
        return this.startKey;
    }

    public RAttribute getAttribute(String key) throws RAnnotationException {
        String k = key.toLowerCase();
        if (this.attributeLut.containsKey(k))
            return this.attributeLut.get(k);

        throw new RAnnotationException("Annotation " + this + " (" + this.startKey
                + " ...) cannot contain a parameter named '" + key + "'.");
    }

    public Iterable<RAttribute> getAttributeSequence() {
        return this.attributeSequence;

    }

    /**
     * Checks if Annotation content is valid for process description and adds attributes / standard values if
     * necessary
     * 
     * @param key
     *        / value pairs given in the annotation from RSkript
     * @return key / value pairs ready for process description
     * @throws IOException
     */
    public void validateDescription(RAnnotation rAnnotation) throws RAnnotationException {
        // check minOccurs Attribute and default value:
        try {
            if (rAnnotation.containsKey(RAttribute.MIN_OCCURS)) {
                Integer min = Integer.parseInt(rAnnotation.getStringValue(RAttribute.MIN_OCCURS));
                if (rAnnotation.containsKey(RAttribute.DEFAULT_VALUE) && !min.equals(0))
                    throw new RAnnotationException("Default value found but minimum required number of occurrences is not '0' in annotation "
                            + this);
            }
        }
        catch (NumberFormatException e) {
            throw new RAnnotationException("Syntax Error in Annotation " + this + " (" + this.startKey + " ...), "
                    + "unable to parse Integer value from attribute " + RAttribute.MIN_OCCURS.getKey() + e.getMessage());
        }

        if (rAnnotation.containsKey(RAttribute.DEFAULT_VALUE) && !rAnnotation.containsKey(RAttribute.MIN_OCCURS)) {
            rAnnotation.setAttribute(RAttribute.MIN_OCCURS, 0);
        }

        // check maxOccurs Attribute:
        try {
            if (rAnnotation.containsKey(RAttribute.MAX_OCCURS)) {
                Integer.parseInt(rAnnotation.getStringValue(RAttribute.MAX_OCCURS));
            }
        }
        catch (NumberFormatException e) {
            throw new RAnnotationException("Syntax Error in Annotation " + this + " (" + this.startKey + " ...), "
                    + "unable to parse Integer value from attribute " + RAttribute.MAX_OCCURS.getKey());
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RAnnotationType [startKey = ");
        sb.append(this.startKey);
        // sb.append(", attributes = ");
        // sb.append(Arrays.toString(this.attributeSequence.toArray()));
        sb.append("]");
        return sb.toString();
    }

}