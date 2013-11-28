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

package org.n52.wps.server.r.syntax;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Describes each annotation type considering attributes, their order and
 * behavior
 * 
 */
public enum RAnnotationType {

    INPUT(Arrays.asList(RAttribute.INPUT_START, RAttribute.IDENTIFIER, RAttribute.TYPE, RAttribute.TITLE, RAttribute.ABSTRACT, RAttribute.DEFAULT_VALUE, RAttribute.MIN_OCCURS, RAttribute.MAX_OCCURS)),

    OUTPUT(Arrays.asList(RAttribute.OUTPUT_START, RAttribute.IDENTIFIER, RAttribute.TYPE, RAttribute.TITLE, RAttribute.ABSTRACT)),

    DESCRIPTION(Arrays.asList(RAttribute.DESCRIPTION_START, RAttribute.IDENTIFIER, RAttribute.TITLE, RAttribute.VERSION, RAttribute.ABSTRACT, RAttribute.AUTHOR)),

    RESOURCE(Arrays.asList(RAttribute.RESOURCE_START, RAttribute.NAMED_LIST));

    private HashMap<String, RAttribute> attributeLut = new HashMap<String, RAttribute>();

    private HashSet<RAttribute> mandatory = new HashSet<RAttribute>();

    private RAttribute startKey;

    private List<RAttribute> attributeSequence;

    private RAnnotationType(List<RAttribute> attributeSequence) {
        this.startKey = attributeSequence.get(0);
        this.attributeSequence = attributeSequence;

        for (RAttribute attribute : attributeSequence) {
            this.attributeLut.put(attribute.getKey(), attribute);
            if (attribute.isMandatory()) {
                this.mandatory.add(attribute);
            }
        }
    }

    public RAttribute getStartKey()
    {
        return this.startKey;
    }

    public RAttribute getAttribute(String key) throws RAnnotationException
    {
        String k = key.toLowerCase();
        if (this.attributeLut.containsKey(k))
            return this.attributeLut.get(k);

        throw new RAnnotationException("Annotation for " + this + " (" + this.startKey + " ...) contains no key named: " + key + ".");
    }

    public Iterable<RAttribute> getAttributeSequence()
    {
        return this.attributeSequence;

    }

    /**
     * Checks if Annotation content is valid for process description and adds
     * attributes / standard values if necessary
     * 
     * @param key
     *            / value pairs given in the annotation from RSkript
     * @return key / value pairs ready for process description
     * @throws IOException
     */
    public void validateDescription(RAnnotation rAnnotation) throws RAnnotationException
    {
        // check minOccurs Attribute and default value:
        try {
            if (rAnnotation.containsKey(RAttribute.MIN_OCCURS)) {
                Integer min = Integer.parseInt(rAnnotation.getStringValue(RAttribute.MIN_OCCURS));
                if (rAnnotation.containsKey(RAttribute.DEFAULT_VALUE) && !min.equals(0))
                    throw new RAnnotationException("");
            }
        } catch (NumberFormatException e) {
            throw new RAnnotationException("Syntax Error in Annotation " + this + " (" + this.startKey + " ...), " + "unable to parse Integer value from attribute " + RAttribute.MIN_OCCURS.getKey()
                    + e.getMessage());
        }

        if (rAnnotation.containsKey(RAttribute.DEFAULT_VALUE) && !rAnnotation.containsKey(RAttribute.MIN_OCCURS)) {
            rAnnotation.setAttribute(RAttribute.MIN_OCCURS, 0);
        }

        // check maxOccurs Attribute:
        try {
            if (rAnnotation.containsKey(RAttribute.MAX_OCCURS)) {
                Integer.parseInt(rAnnotation.getStringValue(RAttribute.MAX_OCCURS));
            }
        } catch (NumberFormatException e) {
            throw new RAnnotationException("Syntax Error in Annotation " + this + " (" + this.startKey + " ...), " + "unable to parse Integer value from attribute " + RAttribute.MAX_OCCURS.getKey());
        }
    }

}