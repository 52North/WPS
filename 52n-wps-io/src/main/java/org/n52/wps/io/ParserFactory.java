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

import org.n52.wps.ParserDocument.Parser;

import org.apache.log4j.Logger;
import org.n52.wps.io.xml.SimpleGMLParser;
/**
 * XMLParserFactory. Will be initialized within each Framework. 
 * @author foerster
 *
 */

public class ParserFactory {
	
	public static String PROPERTY_NAME_REGISTERED_PARSERS = "registeredParsers";
	private static ParserFactory factory;
	private static Logger LOGGER = Logger.getLogger(ParserFactory.class);
	
	private List<IParser> registeredParsers;

	/**
	 * This factory provides all available {@link IParser} to WPS.
	 * @param parsers
	 */
	public static void initialize(Parser[] parsers) {
		if (factory == null) {
			factory = new ParserFactory(parsers);
		}
		else {
			LOGGER.warn("Factory already initialized");
		}
	}
	
	private ParserFactory(Parser[] parsers) {
		registeredParsers = new ArrayList<IParser>();
		for(Parser currentParser : parsers) {
			String parserClass = currentParser.getClassName();
			IParser parser = null;
			try {
				 parser = (IParser) this.getClass().getClassLoader().loadClass(parserClass).newInstance();
				
			}
			catch (ClassNotFoundException e) {
				LOGGER.error("One of the parsers could not be loaded: " + parserClass, e);
			}
			catch(IllegalAccessException e) {
				LOGGER.error("One of the parsers could not be loaded: " + parserClass, e);
			}
			catch(InstantiationException e) {
				LOGGER.error("One of the parsers could not be loaded: " + parserClass, e);
			}
			
			if(parser != null) {
				if (parser.supportsSchemas()) {
					LOGGER.info("Parser class registered: "+parserClass + " " + parser.getSupportedSchemas()[0]);
				}
				else {
					LOGGER.info("Parser class registered: "+parserClass + " " + parser.getSupportedFormats()[0]);
				}
				registeredParsers.add(parser);
			}
		}
	}

	public static ParserFactory getInstance() {
		return factory;
	}
	
	public IParser getParser(String schema, String format, String encoding) {
		if(format == null) {
			format = IOHandler.DEFAULT_MIMETYPE;
			LOGGER.debug("Format is null, assume standard text/xml");
		}
		if(encoding == null) {
			encoding = IOHandler.DEFAULT_ENCODING;
			LOGGER.debug("Encoding is null, assume standard UTF-8");
		}
		for(IParser parser : registeredParsers) {
			if(parser.isSupportedSchema(schema) &&
					parser.isSupportedEncoding(encoding) &&
					parser.isSupportedFormat(format))
				return parser;
		}
		return null;
	}
	
	public IParser getParser(String schema, String format, String encoding, String algorithmIdentifier) {
		if(format == null) {
			format = IOHandler.DEFAULT_MIMETYPE;
			LOGGER.debug("Format is null, assume standard text/xml");
		}
		if(encoding == null) {
			encoding = IOHandler.DEFAULT_ENCODING;
			LOGGER.debug("Encoding is null, assume standard UTF-8");
		}
		
		String parserList = IOConfiguration.getInstance().getProperty(algorithmIdentifier + ".parsers");
		if(parserList==null)
			return getParser(schema, format, encoding);
		String[] algorithmSupportedParsers = parserList.split(",");
		// If there are no explicitly defined parsers, then lets return 
		// what would be returned without knowing the algorithm identifier.
		if(algorithmSupportedParsers.length==0)
			return getParser(schema, format, encoding);
		
		for(IParser parser : registeredParsers) {
			for(String supportedParser: algorithmSupportedParsers) {
				// Comparing fully-qualified names.
				if(supportedParser.compareTo(parser.getClass().getName()) == 0) {
					// Only parsers supported are checked out.
					if(parser.isSupportedSchema(schema) &&
							parser.isSupportedEncoding(encoding) &&
							parser.isSupportedFormat(format)) {
						return parser;
					}
				}
			}
		}
		return null;
	}
	
	public IParser getSimpleParser() {
		return (IParser)new SimpleGMLParser();
	}
}
