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
package org.n52.wps.server;

import java.io.IOException;
import java.io.InputStream;

import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionsDocument;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;

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
	private ProcessDescriptionType description;
	private final String wkName;
	private static Logger LOGGER = Logger.getLogger(AbstractAlgorithm.class);
	
	/** 
	 * default constructor, calls the initializeDescription() Method
	 */
	public AbstractAlgorithm() {
		this.description = initializeDescription();
		this.wkName = "";
	}
	
	/** 
	 * default constructor, calls the initializeDescription() Method
	 */
	public AbstractAlgorithm(String wellKnownName) {
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
	protected ProcessDescriptionType initializeDescription() {
		String className = this.getClass().getName().replace(".", "/");
		InputStream xmlDesc = this.getClass().getResourceAsStream("/" + className + ".xml");
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
			
			return doc.getProcessDescriptions().getProcessDescriptionArray(0);
		}
		catch(IOException e) {
			LOGGER.warn("Could not initialize algorithm, parsing error: " + this.getClass().getName(), e);
		}
		catch(XmlException e) {
			LOGGER.warn("Could not initialize algorithm, parsing error: " + this.getClass().getName(), e);
		}
		return null;
	}
	
	public ProcessDescriptionType getDescription()  {
		return description;
	}

	public boolean processDescriptionIsValid() {
		return description.validate();
	}
	
	public String getWellKnownName() {
		return this.wkName;
	}
}
