/***************************************************************
 This implementation provides a framework to publish processes to the
web through the  OGC Web Processing Service interface. The framework 
is extensible in terms of processes and data handlers. 

 Copyright (C) 2006 by con terra GmbH

 Authors: 
	Bastian Schaeffer, Institute for Geoinformatics, Muenster, Germany

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

 ***************************************************************/

package org.n52.wps.server.sextante;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.geotools.io.DefaultFileFilter;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.IAlgorithmRepository;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import es.unex.sextante.core.Sextante;


/*
 * A container, which allows the 52n WPS to recognize the sextante library.
 * Basic initialization is performed here.
 * 
 * Whenever a getcapabilities request comes in, the process names are extraced based on the available process description documents for sextante processes.
 * This should be changed in the future, when process descriptions should be generated automatically. When a execute process request comes in, a generic GenericSextanteProcessDelegator is created. 
 */


public class SextanteProcessRepository implements IAlgorithmRepository{
	private static Logger LOGGER = Logger.getLogger(SextanteProcessRepository.class);
	private Map<String, File> registeredProcesses;
	 
	
	public SextanteProcessRepository(){
		LOGGER.info("Initializing Sextante Repository");
		registeredProcesses = new HashMap<String, File>();
		Property[] propertyArray = WPSConfig.getInstance().getPropertiesForRepositoryClass(this.getClass().getCanonicalName());
		for(Property property : propertyArray){
			if(property.getName().equalsIgnoreCase("Sextante-Describe-Process-Location")){
				File directory = new File(property.getStringValue());
				if(!directory.isDirectory()){
					LOGGER.warn("Could not find process descriptions for Sextante Extension", null);
				}
				FileFilter filter = new DefaultFileFilter("*.xml");
				File[] files = directory.listFiles(filter);
				for(File file : files){
					addAlgorithm(file);
				}
			}
				
		}
		Sextante.initialize();
		LOGGER.info("Initialization of Sextante Repository successfull");
	}
	
	
	public boolean addAlgorithm(Object describeProcessFile) {
		String processName = "";
		Document document = null;
		if(!(describeProcessFile instanceof File)) {
			 return false;
		}
	
		DocumentBuilder parser;
		try {
			parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			document = parser.parse((File)describeProcessFile);
		} catch (ParserConfigurationException e) {
			LOGGER.warn("Could not add Sextante Extension Process. Identifier: Unknown", null);
			e.printStackTrace();
		} catch (SAXException e) {
			LOGGER.warn("Could not add Sextante Extension Process. Identifier: Unknown", null);
			e.printStackTrace();
		} catch (IOException e) {
			LOGGER.warn("Could not add Sextante Extension Process. Identifier: Unknown", null);
			e.printStackTrace();
		}
		
		
		NodeList identifiers = document.getElementsByTagName("ows:Identifier");
		if(identifiers.getLength()>0){
			Node identifier = identifiers.item(0);
			processName = identifier.getFirstChild().getNodeValue();
			registeredProcesses.put(processName, (File) describeProcessFile);
		}else{
			return false;
		}
		LOGGER.info("Sextante Extension Process "+ processName + " added successfully");
		return true;
		
	}

	public boolean containsAlgorithm(String processID) {
		if(registeredProcesses.containsKey(processID)){
			return true;
		}
		LOGGER.warn("Could not find Sextante Process " + processID, null);
		return false;
	}

	public IAlgorithm getAlgorithm(String processID) {
		return new GenericSextanteProcessDelegator(processID, registeredProcesses.get(processID));
				
		
	}

	public Collection<String> getAlgorithmNames() {
		return registeredProcesses.keySet();
	}

	public Collection<IAlgorithm> getAlgorithms() {
		Collection<IAlgorithm> algorithms = new ArrayList<IAlgorithm>(registeredProcesses.size());
		for(String processID : registeredProcesses.keySet()){
			IAlgorithm algorithm = getAlgorithm(processID);
			if(algorithm!=null){
				algorithms.add(algorithm);
			}
		}
		return algorithms;
	}

	public boolean removeAlgorithm(Object className) {
		//not implemented
		return false;
	}

}
