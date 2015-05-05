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

/**
 * Wrapper for inputs of different WPS versions
 * 
 * @author Benjamin Pross
 *
 */
public class Input {

	private InputType[] inputsV100;

	private DataInputType[] inputsV200;
	
	public Input(InputType[] inputs){
		inputsV100 = inputs;
	}
	
	public Input(DataInputType[] inputs){
		inputsV200 = inputs;
	}

	public InputType[] getInputsV100() {
		return inputsV100;
	}

	public void setInputsV100(InputType[] inputsV100) {
		this.inputsV100 = inputsV100;
	}

	public DataInputType[] getInputsV200() {
		return inputsV200;
	}

	public void setInputsV200(DataInputType[] inputsV200) {
		this.inputsV200 = inputsV200;
	}
}
