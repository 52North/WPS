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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.n52.wps.server.r.data.RDataTypeRegistry;
import org.n52.wps.server.r.data.R_Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Daniel Nüst
 *
 */
public class ImportAnnotation extends RAnnotation {

    private static Logger log = LoggerFactory.getLogger(ImportAnnotation.class);

    private List<R_Resource> imports = new ArrayList<R_Resource>();

    public ImportAnnotation(List<R_Resource> resources,
                              RDataTypeRegistry dataTypeRegistry) throws IOException,
            RAnnotationException {
        super(RAnnotationType.IMPORT, new HashMap<RAttribute, Object>(), dataTypeRegistry);
        this.imports.addAll(resources);
        log.trace("NEW {}", this);
    }

    @Override
    public Object getObjectValue(RAttribute attr) throws RAnnotationException {
        if (attr.equals(RAttribute.NAMED_LIST)) {
            return getResources();
        }
        throw new RAnnotationException("Attribute '{}' not defined for this annotation: {}", attr, this);
    }

    protected Collection<R_Resource> getResources() {
        if (this.imports == null)
            this.imports = new ArrayList<R_Resource>();

        return this.imports;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ImportAnnotation [imports=");
        if (this.imports != null)
            builder.append(Arrays.toString(this.imports.toArray()));
        else
            builder.append("<null>");
        builder.append("]");
        return builder.toString();
    }

}
