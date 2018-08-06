/*
 * Copyright (C) 2007-2018 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.io.datahandler.parser;

import java.io.InputStream;

import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Matthias Mueller, TU Dresden
 *
 */
public class GenericFileParser extends AbstractParser{

    private static Logger LOGGER = LoggerFactory.getLogger(GenericFileParser.class);

    public GenericFileParser() {
        super();
        supportedIDataTypes.add(GenericFileDataBinding.class);
    }

    @Override
    public GenericFileDataBinding parse(InputStream input, String mimeType, String schema) {

        GenericFileData theData = new GenericFileData(input, mimeType);
        LOGGER.info("Found File Input " + mimeType);

        return new GenericFileDataBinding(theData);
    }

}
