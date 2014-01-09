/**
 * ﻿Copyright (C) 2007
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
