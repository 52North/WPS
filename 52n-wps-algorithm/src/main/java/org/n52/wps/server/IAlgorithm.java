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
package org.n52.wps.server;

import java.util.List;
import java.util.Map;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.n52.wps.io.data.IData;
import org.n52.wps.server.ExceptionReport;

/**
 * @author Bastian Schaeffer, University of Muenster,	Theodor Foerster, ITC
 *
 */
public interface IAlgorithm  {
		
	Map<String, IData> run(Map<String, List<IData>> inputData) throws ExceptionReport;
	
	List<String> getErrors();
	
	ProcessDescriptionType getDescription();
	
	/** Returns some well-known name for the process.
	 *  
	 *  @return Returns some well-known name for the process or algorithm
	 *  if that exists, else returns an empty String, never null.
	 *  @note The fully-qualified class name is gotten via getName();
	 */ 
	String getWellKnownName();
	
	/**
	 * Checks if the processDescription complies to the process itself and fits any schema or other dependencies.
	 */
	boolean processDescriptionIsValid();
	
	Class< ? > getInputDataType(String id);
	
	Class< ? > getOutputDataType(String id);
	
}
