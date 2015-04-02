/**
 * ﻿Copyright (C) 2007 - 2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.n52.wps.io;

import java.util.List;

import org.n52.wps.webapp.api.FormatEntry;

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
	public List<FormatEntry> getSupportedFullFormats();
	public Class<?>[] getSupportedDataBindings();
	
}
