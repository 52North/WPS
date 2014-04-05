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
package org.n52.wps.io.data.binding.complex;

import org.apache.commons.io.FileUtils;
import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.IComplexData;


/**
 * @author Matthias Mueller, TU Dresden
 *
 */
public class GenericFileDataBinding implements IComplexData {
	/**
	 * 
	 */
	private static final long serialVersionUID = 625383192227478620L;
	protected GenericFileData payload; 
	
	public GenericFileDataBinding(GenericFileData fileData){
		this.payload = fileData;
	}
	
	public GenericFileData getPayload() {
		return payload;
	}

	public Class getSupportedClass() {
		return GenericFileData.class;
	}
    
    @Override
	public void dispose(){
                //FIXME (MH) The command bellow is flawed because getBaseFile(...) *writes* files from an inputstream into the wps temp directory. 
                   // If the given input stream is closed, the method throws *RuntimeExceptions* that let the process crash.
		//FileUtils.deleteQuietly(payload.getBaseFile(false));
	}
}
