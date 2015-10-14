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

import org.slf4j.helpers.MessageFormatter;

/**
 * 
 * @author Daniel Nüst
 *
 */
public class RAnnotationException extends Exception {

    private static final long serialVersionUID = -537308272628145782L;

    public RAnnotationException() {
        super();
    }

    public RAnnotationException(String message) {
        super(message);
    }

    public RAnnotationException(Throwable cause) {
        super(cause);
    }

    public RAnnotationException(String message, Throwable cause) {
        super(message, cause);
    }

    public RAnnotationException(String message, Object arg) {
        super(MessageFormatter.format(message, arg).getMessage());
    }

    public RAnnotationException(String message, Throwable cause, Object arg) {
        super(MessageFormatter.format(message, arg).getMessage(), cause);
    }

    public RAnnotationException(String message, Object arg1, Object arg2) {
        super(MessageFormatter.format(message, arg1, arg2).getMessage());
    }

    public RAnnotationException(String message, Throwable cause, Object arg1, Object arg2) {
        super(MessageFormatter.format(message, arg1, arg2).getMessage(), cause);
    }

    public RAnnotationException(String messagePattern, Object[] argArray) {
        super(MessageFormatter.arrayFormat(messagePattern, argArray).getMessage());
    }

    public RAnnotationException(String messagePattern, Throwable cause, Object[] argArray) {
        super(MessageFormatter.arrayFormat(messagePattern, argArray).getMessage(), cause);
    }

}
