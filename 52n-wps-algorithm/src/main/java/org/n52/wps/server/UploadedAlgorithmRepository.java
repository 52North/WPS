
	/***************************************************************
	This implementation provides a framework to publish processes to the
	web through the  OGC Web Processing Service interface. The framework 
	is extensible in terms of processes and data handlers. It is compliant 
	to the WPS version 0.4.0 (OGC 05-007r4). 

	Copyright (C) 2009 by con terra GmbH

	Authors: 
		Bastian Schäffer, University of Muenster



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

	package org.n52.wps.server;


	import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.opengis.wps.x100.ProcessDescriptionType;

	import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;



	/**
	 * A static repository to retrieve the available algorithms.
	 * @author foerster
	 *
	 */
	public class UploadedAlgorithmRepository implements ITransactionalAlgorithmRepository{
		
		private static Logger LOGGER = LoggerFactory.getLogger(LocalAlgorithmRepository.class);
		private Map<String, String> algorithmMap;
		private Map<String, ProcessDescriptionType> processDescriptionMap;
		
		public UploadedAlgorithmRepository() {
			algorithmMap = new HashMap<String, String>();
			processDescriptionMap = new HashMap<String, ProcessDescriptionType>();
			
			if(WPSConfig.getInstance().isRepositoryActive(this.getClass().getCanonicalName())){
				Property[] propertyArray = WPSConfig.getInstance().getPropertiesForRepositoryClass(this.getClass().getCanonicalName());
				for(Property property : propertyArray){
					if(property.getName().equalsIgnoreCase("Algorithm") && property.getActive()){
						addAlgorithm(property.getStringValue());
					}
				}
			} else {
				LOGGER.debug("Local Algorithm Repository is inactive.");
			}			
			
		}
		
		public boolean addAlgorithms(String[] algorithms)  {
			for(String algorithmClassName : algorithms) {
				addAlgorithm(algorithmClassName);
			}
			LOGGER.info("Algorithms registered!");
			return true;
			
		}
		
		public IAlgorithm getAlgorithm(String className) {
			try {
				return loadAlgorithm(algorithmMap.get(className));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}
		
		public Collection<IAlgorithm> getAlgorithms() {
			Collection<IAlgorithm> resultList = new ArrayList<IAlgorithm>();
			try {
				for(String algorithmClasses : algorithmMap.values()){
					resultList.add(loadAlgorithm(algorithmMap.get(algorithmClasses)));
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			}
			return resultList;
		}
		
		public Collection<String> getAlgorithmNames() {
			return new ArrayList<String>(algorithmMap.keySet());
		}
		
		public boolean containsAlgorithm(String className) {
			return algorithmMap.containsKey(className);
		}
		
		private IAlgorithm loadAlgorithm(String algorithmClassName) throws Exception{
			IAlgorithm algorithm = (IAlgorithm)LocalAlgorithmRepository.class.getClassLoader().loadClass(algorithmClassName).newInstance();
			if(!algorithm.processDescriptionIsValid()) {
				LOGGER.warn("Algorithm description is not valid: " + algorithmClassName);
				throw new Exception("Could not load algorithm " +algorithmClassName +". ProcessDescription Not Valid.");
			}
			return algorithm;
		}

		public boolean addAlgorithm(Object processID) {
			if(!(processID instanceof String)){
				return false;
			}
			String algorithmClassName = (String) processID;
					
			algorithmMap.put(algorithmClassName, algorithmClassName);
			LOGGER.info("Algorithm class registered: " + algorithmClassName);
						
				
			
			return true;

		}

		public boolean removeAlgorithm(Object processID) {
			if(!(processID instanceof String)){
				return false;
			}
			String className = (String) processID;
			if(algorithmMap.containsKey(className)){
				algorithmMap.remove(className);
				return true;
			}
			return false;
		}

		
		
		@Override
		public ProcessDescriptionType getProcessDescription(String processID) {
			if(!processDescriptionMap.containsKey(processID)){
				processDescriptionMap.put(processID, getAlgorithm(processID).getDescription());
			}
			return processDescriptionMap.get(processID);
		}

		@Override
		public void shutdown() {
			// TODO Auto-generated method stub
			
		}
		

		


		

	}

	
	

	

	


	



