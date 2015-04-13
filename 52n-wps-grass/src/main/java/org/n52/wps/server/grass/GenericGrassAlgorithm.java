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
package org.n52.wps.server.grass;

import org.n52.wps.server.AbstractAlgorithm;
import org.n52.wps.server.ProcessDescription;

/**
 * @author Benjamin Pross (bpross-52n)
 *
 */
public abstract class GenericGrassAlgorithm extends AbstractAlgorithm{

	private ProcessDescription description;
	private final String wkName;
	
	/** 
	 * default constructor, calls the initializeDescription() Method
	 */
	public GenericGrassAlgorithm() {
		this.description = initializeDescription();
		this.wkName = "";
	}
	
	/** 
	 * default constructor, calls the initializeDescription() Method
	 */
	public GenericGrassAlgorithm(String wellKnownName) {
		this.wkName = wellKnownName; // Has to be initialized before the description. 
		this.description = initializeDescription();
	}
	
	/** 
	 * This method should be overwritten, in case you want to have a way of initializing.
	 * 
	 * In detail it looks for a xml descfile, which is located in the same directory as the implementing class and has the same
	 * name as the class, but with the extension XML.
	 * @return
	 */
	protected ProcessDescription initializeDescription() {
		return null;
	}
	
	public ProcessDescription getDescription()  {
		return description;
	}

	public boolean processDescriptionIsValid(String version) {
		return description.getProcessDescriptionType(version).validate();
	}
	
	public String getWellKnownName() {
		return this.wkName;
	}


	
	
	
	
}
