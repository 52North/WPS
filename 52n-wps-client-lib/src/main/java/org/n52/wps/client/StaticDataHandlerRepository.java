/**
 * ﻿Copyright (C) 2007 - 2019 52°North Initiative for Geospatial Open Source
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
