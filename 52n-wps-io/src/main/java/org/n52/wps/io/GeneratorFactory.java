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


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import net.opengis.wps.x100.ComplexDataDescriptionType;
import net.opengis.wps.x100.OutputDescriptionType;

import org.apache.log4j.Logger;
import org.n52.wps.GeneratorDocument.Generator;
import org.n52.wps.io.datahandler.xml.AbstractXMLGenerator;
import org.n52.wps.io.datahandler.xml.SimpleGMLGenerator;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.RepositoryManager;


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
		loadAllGenerators(generators);

        // FvK: added Property Change Listener support
        // creates listener and register it to the wpsConfig instance.
        org.n52.wps.commons.WPSConfig.getInstance().addPropertyChangeListener(org.n52.wps.commons.WPSConfig.WPSCONFIG_PROPERTY_EVENT_NAME, new PropertyChangeListener() {
            public void propertyChange(
                    final PropertyChangeEvent propertyChangeEvent) {
                LOGGER.info(this.getClass().getName() + ": Received Property Change Event: " + propertyChangeEvent.getPropertyName());
                loadAllGenerators(org.n52.wps.commons.WPSConfig.getInstance().getRegisteredGenerators());
            }
        });
	}

    private void loadAllGenerators(Generator[] generators){
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
			if(generator != null) {
				registeredGenerators.add(generator);
			}
		}
    }

	public static GeneratorFactory getInstance() {
		return factory;
	}
	
	private IGenerator getGenerator(String schema, String format, String encoding) {
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
	/*
	public IGenerator getGenerator(String schema, String format, String encoding, String algorithmIdentifier, String outputIdentifier) {
		if(format == null) {
			format = IOHandler.DEFAULT_MIMETYPE;
			LOGGER.debug("format is null, assume standard text/xml");
		}
		if(encoding == null) {
			encoding = IOHandler.DEFAULT_ENCODING;
			LOGGER.debug("encoding is null, assume standard UTF-8");
		}
		
		IAlgorithm algorithm = RepositoryManager.getInstance().getAlgorithm(algorithmIdentifier);
		Class requiredInputClass = algorithm.getOutputDataType(outputIdentifier);
		return getGenerator(schema, format, encoding, requiredInputClass);
	}
	*/
	
	public IGenerator getGenerator(String schema, String format, String encoding, Class outputInternalClass) {
		if(format == null) {
			format = IOHandler.DEFAULT_MIMETYPE;
			LOGGER.debug("format is null, assume standard text/xml");
		}
		if(encoding == null) {
			encoding = IOHandler.DEFAULT_ENCODING;
			LOGGER.debug("encoding is null, assume standard UTF-8");
		}
			
		for(IGenerator generator : registeredGenerators) {
			Class[] supportedClasses = generator.getSupportedInternalInputDataType();
			for(Class clazz : supportedClasses){
				if(clazz.equals(outputInternalClass)) {
					if(generator.isSupportedSchema(schema) && generator.isSupportedEncoding(encoding) &&generator.isSupportedFormat(format)){
						return generator;
					}
				}
			}
			
			
		}
		//TODO
		//try a chaining approach, by calculation all permutations and look for matches.
		return null;
	}
	
	/**
	 * returns the a simple Generator. In this case the @link SimpleGMLGenerator.
	 * @return
	 */
	public AbstractXMLGenerator getSimpleXMLGenerator() {
		return new SimpleGMLGenerator();
	}

	public List<IGenerator> getAllGenerators() {
		return registeredGenerators;
	}

	public IGenerator getDefaultGeneratorForProcess(String algorithmIdentifier, Class algorithmOutput) {
		IAlgorithm algorithm = RepositoryManager.getInstance().getAlgorithm(algorithmIdentifier);
		OutputDescriptionType[] outputs = algorithm.getDescription().getProcessOutputs().getOutputArray();
		if(outputs[0].isSetComplexOutput()){
			ComplexDataDescriptionType format = outputs[0].getComplexOutput().getDefault().getFormat();
			String encoding = format.getEncoding();
			String mimeType = format.getMimeType();
			String schema = format.getSchema();
			return getGenerator(schema, mimeType, encoding, algorithmOutput);
		}else{
			//TODO
			return null;
		}
	}
	
}
