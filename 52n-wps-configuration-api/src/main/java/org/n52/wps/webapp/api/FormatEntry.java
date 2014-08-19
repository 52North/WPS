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
package org.n52.wps.webapp.api;

public class FormatEntry {
	
	private String mimeType;
	private String schema;
	private String encoding;
	private boolean active;

	public FormatEntry(String mimeType, String schema, String encoding, boolean active) {
		this.mimeType = mimeType;
		this.schema = schema;
		this.encoding = encoding;
		this.active = active;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if(obj instanceof FormatEntry){
			FormatEntry formatEntry = (FormatEntry)obj;
			
			boolean mimeTypesEqual = false;
			boolean schemasEqual = false;
			boolean encodingsEqual = false;
			
			if(mimeType != null){
				mimeTypesEqual = mimeType.equals(formatEntry.getMimeType());
				if(!mimeTypesEqual){
					return false;
				}
			}
			
			if(schema != null){
				schemasEqual = schema.equals(formatEntry.getSchema());
				if(!schemasEqual){
					return false;
				}
			}
			
			if(encoding != null){
				encodingsEqual = encoding.equals(formatEntry.getEncoding());
				if(!encodingsEqual){
					return false;
				}
			}
			
			return true;
		}
		
		return super.equals(obj);
	}
	
}
