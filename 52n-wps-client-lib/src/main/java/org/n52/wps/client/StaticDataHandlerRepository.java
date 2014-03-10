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
package org.n52.wps.client;

import org.n52.wps.GeneratorDocument.Generator;
import org.n52.wps.ParserDocument.Parser;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.GeneratorFactory;
import org.n52.wps.io.ParserFactory;

/*
 * Initializes the Factories for Generators and Parsers, based on static information.
 * @author foerster
 *
 */
public class StaticDataHandlerRepository {

	private static GeneratorFactory genFactory;
	private static ParserFactory parserFactory;
	
	public static GeneratorFactory getGeneratorFactory() {
		if(genFactory == null) {
			Generator[] generators = WPSConfig.getInstance().getActiveRegisteredGenerator();		
			
			GeneratorFactory.initialize(generators);
			genFactory = GeneratorFactory.getInstance();
			
			
			
		}
		return genFactory;
	}
	
	public static ParserFactory getParserFactory() {
		if(parserFactory == null) {
			Parser[] parsers = WPSConfig.getInstance().getActiveRegisteredParser();
			ParserFactory.initialize(parsers);
			
			parserFactory = ParserFactory.getInstance();
		}
		return parserFactory;
	}
}
