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

/**
 * attributes used in Annotations
 */
public enum RAttribute {
    INPUT_START("wps.in", null, true), OUTPUT_START("wps.out", null, true), DESCRIPTION_START("wps.des", null, true), RESOURCE_START("wps.res", null, true), IDENTIFIER("id", null, true), TYPE("type",
            null, true), TITLE("title", IDENTIFIER, false), VERSION("version", null, false), ABSTRACT("abstract", null, false), MIN_OCCURS("minOccurs", 1, true), MAX_OCCURS("maxOccurs", 1, true), DEFAULT_VALUE(
            "value", null, false), METADATA("meta", null, false), MIMETYPE("mimetype", null, false), SCHEMA("schema", null, false), ENCODING("encoding", null, false), AUTHOR("author", null, false),
    // A sequence of values:
    NAMED_LIST("seq", null, true),
    // derives from a named list
    NAMED_LIST_R_SYNTAX("rseq", null, true);

    private String key;

    private Object defValue;

    private RAttribute(String key, Object defValue, boolean mandatory) {
        this.key = key.toLowerCase();
        this.defValue = defValue;
        this.mandatory = mandatory;
    }

    public String getKey()
    {
        return this.key;
    }

    public Object getDefValue()
    {
        return this.defValue;
    }

    /**
     * @return true if attribute has to occur in Process description, if so,
     *         there has to be a standard value or a value in R Annotion given
     */
    public boolean isMandatory()
    {
        return this.mandatory;
    }

    private boolean mandatory;

    @Override
    public String toString()
    {
        return getKey();
    }
}