/**
 * Copyright (C) 2007-2015 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.server;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.algorithm.util.CustomClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.opengis.wps.x100.ProcessDescriptionsDocument;

/**
 * This class has to be extended in order to be served through the WPS. 
 * The class file should also include a description file of the algorithm. This file has to be
 * valid against the describeProcess.xsd. The file has to be placed in the folder of the class file and has
 * to be named the same as the Algorithm. 
 * 
 * <p>If you want to apply a different initialization method, just override the initializeDescription() method.
 * 
 * NOTE: This class is an adapter and it is recommended to extend this. 
 * @author foerster
 *
 */
public abstract class AbstractAlgorithm implements IAlgorithm 
{
	private ProcessDescription description; // private, force access through getter method for lazy loading.
	private final String wkName;
	private static Logger LOGGER = LoggerFactory.getLogger(AbstractAlgorithm.class);
	
	/** 
	 * default constructor, calls the initializeDescription() Method
	 */
	public AbstractAlgorithm() {
		this.wkName = this.getClass().getName();
	}
	
	/** 
	 * default constructor, calls the initializeDescription() Method
	 */
	public AbstractAlgorithm(String wellKnownName) {
		this.wkName = wellKnownName;
	}
	
	/** 
	 * This method should be overwritten, in case you want to have a way of initializing.
	 * 
	 * In detail it looks for a xml descfile, which is located in the same directory as the implementing class and has the same
	 * name as the class, but with the extension XML.
	 * @return
	 */
	protected ProcessDescription initializeDescription() {
		String className = this.getClass().getName().replace(".", "/");
		
		InputStream xmlDesc; 
		
		if(this.getClass().getClassLoader() instanceof CustomClassLoader){
		    String baseDir = ((CustomClassLoader)this.getClass().getClassLoader()).getBaseDir();
		    xmlDesc = UploadedAlgorithmRepository.class.getClassLoader().getResourceAsStream(baseDir + File.separator + className + ".xml");
		}else{
	            xmlDesc = this.getClass().getResourceAsStream("/" + className + ".xml");		    
		}
		
		try {
			XmlOptions option = new XmlOptions();
			option.setLoadTrimTextBuffer();
			ProcessDescriptionsDocument doc = ProcessDescriptionsDocument.Factory.parse(xmlDesc, option);
			if(doc.getProcessDescriptions().getProcessDescriptionArray().length == 0) {
				LOGGER.warn("ProcessDescription does not contain correct any description");
				return null;
			}
			
			// Checking that the process name (full class name or well-known name) matches the identifier.
			if(!doc.getProcessDescriptions().getProcessDescriptionArray(0).getIdentifier().getStringValue().equals(this.getClass().getName()) &&
					!doc.getProcessDescriptions().getProcessDescriptionArray(0).getIdentifier().getStringValue().equals(this.getWellKnownName())) {
				doc.getProcessDescriptions().getProcessDescriptionArray(0).getIdentifier().setStringValue(this.getClass().getName());
				LOGGER.warn("Identifier was not correct, was changed now temporary for server use to " + this.getClass().getName() + ". Please change it later in the description!");
			}
			
			ProcessDescription processDescription = new ProcessDescription();
			
			processDescription.addProcessDescriptionForVersion(doc.getProcessDescriptions().getProcessDescriptionArray(0), "1.0.0");
			
			return processDescription;
		}
		catch(IOException e) {
			LOGGER.warn("Could not initialize algorithm, parsing error: " + this.getClass().getName(), e);
		}
		catch(XmlException e) {
			LOGGER.warn("Could not initialize algorithm, parsing error: " + this.getClass().getName(), e);
		}
		return null;
	}
	
    @Override
	public synchronized ProcessDescription getDescription()  {
        if (description == null) {
            description = initializeDescription();
        }
        return description;
	}

    @Override
	public boolean processDescriptionIsValid(String version) {
    	if(getDescription().getProcessDescriptionType(version) == null){
    		return false;
    	}
		return getDescription().getProcessDescriptionType(version).validate();
	}
	
    @Override
	public String getWellKnownName() {
		return this.wkName;
	}
}
