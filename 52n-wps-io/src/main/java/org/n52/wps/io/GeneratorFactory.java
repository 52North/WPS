/***************************************************************
 This implementation provides a framework to publish processes to the
web through the  OGC Web Processing Service interface. The framework 
is extensible in terms of processes and data handlers. It is compliant 
to the WPS version 0.4.0 (OGC 05-007r4). 

 Copyright (C) 2006 by con terra GmbH

 Authors: 
	Theodor Foerster, ITC, Enschede, the Netherlands
	Carsten Priess, Institute for geoinformatics, University of
Muenster, Germany


 Contact: Albert Remke, con terra GmbH, Martin-Luther-King-Weg 24,
 48155 Muenster, Germany, 52n@conterra.de

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 version 2 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program (see gnu-gpl v2.txt); if not, write to
 the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA  02111-1307, USA or visit the web page of the Free
 Software Foundation, http://www.fsf.org.

 Created on: 13.06.2006
 ***************************************************************/
package org.n52.wps.io;

import java.util.ArrayList;
import java.util.List;

import noNamespace.GeneratorDocument.Generator;

import org.apache.log4j.Logger;
import org.n52.wps.io.xml.AbstractXMLGenerator;
import org.n52.wps.io.xml.SimpleGMLGenerator;

public class GeneratorFactory {
	
	public static String PROPERTY_NAME_REGISTERED_GENERATORS = "registeredGenerators";
	private static GeneratorFactory factory;
	private static Logger LOGGER = Logger.getLogger(GeneratorFactory.class);
	
	private List<IGenerator> registeredGenerators;

	/**
	 * This factory provides all available {@link AbstractXMLGenerator} to WPS.
	 * @param generators
	 */
	public static void initialize(Generator[] generators) {
		if (factory == null) {
			factory = new GeneratorFactory(generators);
		}
		else {
			LOGGER.warn("Factory already initialized");
		}
	}
	
	private GeneratorFactory(Generator[] generators) {
		registeredGenerators = new ArrayList<IGenerator>();
		for(Generator currentGenerator : generators) {
			IGenerator generator = null;
			String generatorClass = currentGenerator.getClassName();
						
			try {
				 generator = (IGenerator) this.getClass().getClassLoader().loadClass(generatorClass).newInstance();
				 
			}
			catch (ClassNotFoundException e) {
				LOGGER.error("One of the parsers could not be loaded: " + generatorClass, e);
			}
			catch(IllegalAccessException e) {
				LOGGER.error("One of the parsers could not be loaded: " + generatorClass, e);
			}
			catch(InstantiationException e) {
				LOGGER.error("One of the parsers could not be loaded: " + generatorClass, e);
			}
			registeredGenerators.add(generator);
		}
	}

	public static GeneratorFactory getInstance() {
		return factory;
	}
	
	public IGenerator getGenerator(String schema, String format, String encoding) {
		if(format == null) {
			format = IOHandler.DEFAULT_MIMETYPE;
			LOGGER.debug("format is null, assume standard text/xml");
		}
		if(encoding == null) {
			encoding = IOHandler.DEFAULT_ENCODING;
			LOGGER.debug("encoding is null, assume standard UTF-8");
		}
		for(IGenerator generator : registeredGenerators) {
			if(generator.isSupportedSchema(schema) && 
					generator.isSupportedEncoding(encoding) &&
					generator.isSupportedFormat(format))
				return generator;
		}
		return null;
	}
	
	public IGenerator getGenerator(String schema, String format, String encoding, String algorithmIdentifier) {
		if(format == null) {
			format = IOHandler.DEFAULT_MIMETYPE;
			LOGGER.debug("format is null, assume standard text/xml");
		}
		if(encoding == null) {
			encoding = IOHandler.DEFAULT_ENCODING;
			LOGGER.debug("encoding is null, assume standard UTF-8");
		}
		
		String generatorList = IOConfiguration.getInstance().getProperty(algorithmIdentifier + ".generators");
		if(generatorList==null)
			return getGenerator(schema, format, encoding);
		String[] algorithmSupportedGenerators = generatorList.split(",");
		// If there are no explicitly defined parsers, then lets return 
		// what would be returned without knowing the algorithm identifier.
		if(algorithmSupportedGenerators.length==0)
			return getGenerator(schema, format, encoding);
		
		for(IGenerator generator : registeredGenerators) {
			for(String supportedParser: algorithmSupportedGenerators) {
				// Comparing fully-qualified names.
				if(supportedParser.compareTo(generator.getClass().getName()) == 0) {
					// Only parsers supported are checked out.
					if(generator.isSupportedSchema(schema) && 
							generator.isSupportedEncoding(encoding) &&
							generator.isSupportedFormat(format))
						return generator;
				}
			}
		}
		return null;
	}
	
	/**
	 * returns the a simple Generator. In this case the @link SimpleGMLGenerator.
	 * @return
	 */
	public AbstractXMLGenerator getSimpleXMLGenerator() {
		return new SimpleGMLGenerator();
	}
	
}
