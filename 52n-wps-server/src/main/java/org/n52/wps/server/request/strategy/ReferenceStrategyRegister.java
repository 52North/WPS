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
package org.n52.wps.server.request.strategy;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.n52.wps.server.ExceptionReport;

import net.opengis.wps.x100.InputType;

public class ReferenceStrategyRegister {

	protected List<IReferenceStrategy> registeredStrategies;
	private static ReferenceStrategyRegister instance;
	
	
	public synchronized static ReferenceStrategyRegister getInstance(){
		if(instance==null){
			instance = new ReferenceStrategyRegister();
		}
		return instance;
	}
	
	private ReferenceStrategyRegister(){
		registeredStrategies = new ArrayList<IReferenceStrategy>();
		registeredStrategies.add(new WCS111XMLEmbeddedBase64OutputReferenceStrategy());
	}
	
	protected void registerStrategy(IReferenceStrategy strategy){
		registeredStrategies.add(strategy);
	}
	
	public ReferenceInputStream resolveReference(InputType input) throws ExceptionReport{
		IReferenceStrategy foundStrategy = new DefaultReferenceStrategy();
		for(IReferenceStrategy strategy : registeredStrategies){
			if(strategy.isApplicable(input)){
				foundStrategy = strategy;
				break;
			}
		}
		return foundStrategy.fetchData(input);
	}
}
