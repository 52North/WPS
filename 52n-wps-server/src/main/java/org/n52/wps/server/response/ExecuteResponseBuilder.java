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
package org.n52.wps.server.response;

import java.io.InputStream;

import org.apache.xmlbeans.XmlObject;
import org.n52.wps.server.ExceptionReport;

/**
 * WPS Execute operation response. By default, this XML document is delivered to
 * the client in response to an Execute request. If "status" is "false" in the
 * Execute operation request, this document is normally returned when process
 * execution has been completed. If "status" in the Execute request is "true",
 * this response shall be returned as soon as the Execute request has been
 * accepted for processing. In this case, the same XML document is also made
 * available as a web-accessible resource from the URL identified in the
 * statusLocation, and the WPS server shall repopulate it once the process has
 * completed. It may repopulate it on an ongoing basis while the process is
 * executing. However, the response to an Execute request will not include this
 * element in the special case where the output is a single complex value result
 * and the Execute request indicates that "store" is "false". Instead, the
 * server shall return the complex result (e.g., GIF image or GML) directly,
 * without encoding it in the ExecuteResponse. If processing fails in this
 * special case, the normal ExecuteResponse shall be sent, with the error
 * condition indicated. This option is provided to simplify the programming
 * required for simple clients and for service chaining.
 *
 * @author Timon ter Braak
 *
 */
public interface ExecuteResponseBuilder {

    public void update() throws ExceptionReport;

    public String getMimeType();

    public String getMimeType(XmlObject definitionObject);

    public InputStream getAsStream() throws ExceptionReport;

    public void setStatus(XmlObject statusObject);

}
