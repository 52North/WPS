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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.xmlbeans.XmlException;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.IAlgorithmRepository;
import org.n52.wps.server.sextante.SextanteProcessDescriptionCreator.UnsupportedGeoAlgorithmException;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;


/*
 * A container, which allows the 52n WPS to recognize the sextante library.
 * Basic initialization is performed here.
 * 
 * Whenever a getcapabilities request comes in, the process names are extraced based on the available process description documents for sextante processes.
 * This should be changed in the future, when process descriptions should be generated automatically. When a execute process request comes in, a generic GenericSextanteProcessDelegator is created. 
 */


public class SextanteProcessRepository implements IAlgorithmRepository{
	private static Logger LOGGER = LoggerFactory.getLogger(SextanteProcessRepository.class);
	private Map<String, ProcessDescriptionType> registeredProcesses;
	 
	
	public SextanteProcessRepository(){
		LOGGER.info("Initializing Sextante Repository");
		registeredProcesses = new HashMap<String, ProcessDescriptionType>();
		
		/*
		 * get properties of Repository
		 *
		 * check whether process is amongst them and active
		 * 
		 * if properties are empty (not initialized yet)
		 * 		add all valid processes to WPSConfig
		 */
		
		Property[] propertyProcesses = WPSConfig.getInstance().getPropertiesForRepositoryClass(this.getClass().getName());
		
		ArrayList<String> processList = new ArrayList<String>(propertyProcesses.length);
		
		for (Property prop : propertyProcesses) {
			
			if(prop.getActive()){
				processList.add(prop.getStringValue());
			}else{
				LOGGER.info("Sextante Process : " + prop.getStringValue() + " not active.");				
			}
		}
		
		Sextante.initialize();
		HashMap<String, HashMap<String, GeoAlgorithm>> sextanteMap = Sextante.getAlgorithms();
		HashMap<String, GeoAlgorithm> algorithmMap = sextanteMap.get("SEXTANTE");
		Set<String> keys = algorithmMap.keySet();
		SextanteProcessDescriptionCreator descriptionCreator = new SextanteProcessDescriptionCreator();
		for(Object keyObject : keys){
			String key = (String) keyObject;
			if(!processList.contains(key)){
				LOGGER.info("Did not add Sextante Process : " + key +". Not in Repository properties or not active.");
				continue;
			}
			GeoAlgorithm sextanteProcess = Sextante.getAlgorithmFromCommandLineName(key);
			ProcessDescriptionType processDescription;
			try {
				processDescription = descriptionCreator.createDescribeProcessType(sextanteProcess);
			} catch (NullParameterAdditionalInfoException e) {
				LOGGER.warn("Could not add Sextante Process : " + key +". Errors while creating describe Process");
				continue;
			} catch (UnsupportedGeoAlgorithmException e) {
				LOGGER.warn("Could not add Sextante Process : " + key + ". Errors while creating describe Process");
				continue;
			}
		
			registeredProcesses.put(key, processDescription);
			LOGGER.info("Sextante Process " + key + " added.");
		}
		
		
		LOGGER.info("Initialization of Sextante Repository successfull");
	}
	
	
	public boolean addAlgorithm(Object describeProcess) {
		String processName = "";
		ProcessDescriptionType document = null;
		try {
			if(describeProcess instanceof File){
		
			document = ProcessDescriptionType.Factory.parse((File)describeProcess);
			}
			if(describeProcess instanceof ProcessDescriptionType){
				document = (ProcessDescriptionType) describeProcess;
			}
		
		
		} catch (IOException e) {
			LOGGER.warn("Could not add Sextante Extension Process. Identifier: Unknown", e);
			e.printStackTrace();
		} catch (XmlException e) {
			e.printStackTrace();
		}
		if(describeProcess == null){
			throw new RuntimeException("Could not add process");
		}
		
		
			registeredProcesses.put(document.getIdentifier().getStringValue(), document);
		
		LOGGER.info("Sextante Extension Process "+ processName + " added successfully");
		return true;
		
	}

	public boolean containsAlgorithm(String processID) {
		if(registeredProcesses.containsKey(processID)){
			return true;
		}
		LOGGER.warn("Could not find Sextante Process " + processID);
		return false;
	}

	public IAlgorithm getAlgorithm(String processID) {
		if(!containsAlgorithm(processID)){
			throw new RuntimeException("Could not allocate Process");
		}
		return new GenericSextanteProcessDelegator(processID, registeredProcesses.get(processID));
				
		
	}

	public Collection<String> getAlgorithmNames() {
		return registeredProcesses.keySet();
	}

	public boolean removeAlgorithm(Object className) {
		//not implemented
		return false;
	}
	
	@Override
	public ProcessDescriptionType getProcessDescription(String processID) {
		if(!registeredProcesses.containsKey(processID)){
			registeredProcesses.put(processID, getAlgorithm(processID).getDescription());
		}
		return registeredProcesses.get(processID);
	}


	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
	}

}
