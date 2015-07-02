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

import org.n52.wps.io.data.IData;

public interface RTypeDefinition {

    /**
     * @return Unique type-expression used in the WPS4R annotation
     *         (type-Attribute)
     */
    public abstract String getKey();

    /**
     * 
     * @return Type-expression used in the processDescription
     */
    public abstract String getMimeType();

    public abstract boolean isComplex();

    // TODO to be added:

    // public abstract boolean isLitearal();
    // public abstract boolean isLitearal();

    /**
     * 
     * @return (default) encoding or null if not applicable
     */
    public abstract String getEncoding();

    /**
     * 
     * @return (default) Schema
     */
    public abstract String getSchema();

    /**
     * Refers to the Databinding in use
     * 
     * @return IData class
     */
    public abstract Class<? extends IData> getIDataClass();

}