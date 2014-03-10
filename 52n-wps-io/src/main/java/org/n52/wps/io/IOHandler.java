/**
 * ﻿Copyright (C) 2007 - 2014 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.io;

import org.n52.wps.FormatDocument.Format;

public interface IOHandler {
	
	
	public static final String DEFAULT_ENCODING="UTF-8";
	public static final String ENCODING_BASE64 = "base64";

	//public static final String DEFAULT_MIMETYPE = "text/xml";
	
	public static final String MIME_TYPE_ZIPPED_SHP = "application/x-zipped-shp";
	
	public boolean isSupportedSchema(String schema);
	public boolean isSupportedFormat(String format);
	public boolean isSupportedEncoding(String encoding);
	public boolean isSupportedDataBinding(Class<?> clazz);
	
	public String[] getSupportedSchemas();
	public String[] getSupportedFormats();
	public String[] getSupportedEncodings();
	public Format[] getSupportedFullFormats();
	public Class<?>[] getSupportedDataBindings();
	
}
