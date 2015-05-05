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
package org.n52.wps.server.request;

import net.opengis.wps.x100.InputType;
import net.opengis.wps.x20.DataInputType;

import org.apache.xmlbeans.XmlObject;

/**
 * Wrapper for inputs references of different WPS versions
 * 
 * @author Benjamin Pross
 *
 */
public class InputReference {

	private InputType inputV100;

	private DataInputType inputV200;
	
	public InputReference(InputType inputs){
		inputV100 = inputs;
	}
	
	public InputReference(DataInputType inputs){
		inputV200 = inputs;
	}

	public InputType getInputV100() {
		return inputV100;
	}

	public void setInputV100(InputType inputV100) {
		this.inputV100 = inputV100;
	}

	public DataInputType getInputV200() {
		return inputV200;
	}

	public void setInputV200(DataInputType inputV200) {
		this.inputV200 = inputV200;
	}

	public String getHref() {		
		return inputV100 != null ? inputV100.getReference().getHref() : inputV200.getReference().getHref();
	}
	
	public String getMimeType() {		
		return inputV100 != null ? inputV100.getReference().getMimeType() : inputV200.getReference().getMimeType();
	}
	
	public String getSchema() {		
		return inputV100 != null ? inputV100.getReference().getSchema() : inputV200.getReference().getSchema();
	}
	
	public String getEncoding() {		
		return inputV100 != null ? inputV100.getReference().getEncoding() : inputV200.getReference().getEncoding();
	}

	public String getIdentifier() {
		return inputV100 != null ? inputV100.getIdentifier().getStringValue() : inputV200.getId();
	}

	public boolean isSetBodyReference() {
		return inputV100 != null ? inputV100.getReference().isSetBodyReference() : inputV200.getReference().isSetBodyReference();
	}

	public String getBodyReferenceHref() {
		return inputV100 != null ? inputV100.getReference().getBodyReference().getHref() : inputV200.getReference().getBodyReference().getHref();
	}

	public boolean isSetBody() {
		return inputV100 != null ? inputV100.getReference().isSetBody() : inputV200.getReference().isSetBody();
	}

	public XmlObject getBody() {
		return inputV100 != null ? inputV100.getReference().getBody() : inputV200.getReference().getBody();
	}
	
}
