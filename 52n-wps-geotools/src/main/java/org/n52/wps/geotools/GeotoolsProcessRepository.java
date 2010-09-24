package org.n52.wps.geotools;
/*
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.log4j.Logger;
import org.geotools.process.ProcessFactory;
import org.geotools.process.Processors;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.IAlgorithmRepository;





public class GeotoolsProcessRepository implements IAlgorithmRepository{
	private static Logger LOGGER = Logger.getLogger(GeotoolsProcessRepository.class);
	private Map<String, ProcessDescriptionType> registeredProcesses;
	 
	
	public GeotoolsProcessRepository(){
		Set<ProcessFactory> processFactories = Processors.getProcessFactories();
		Iterator<ProcessFactory> iterator = processFactories.iterator();
		
		GeotoolsProcessDescriptionCreator processDescriptionCreator = new GeotoolsProcessDescriptionCreator();
		registeredProcesses = new HashMap<String, ProcessDescriptionType>();
		while (iterator.hasNext()) {
		    
		    ProcessFactory processFactory = (ProcessFactory) iterator.next();
		    ProcessDescriptionType processDescription = processDescriptionCreator.createDescribeProcessType(processFactory);
		    if(processDescription!=null){
		    	registeredProcesses.put(processDescription.getIdentifier().stringValue(), processDescription);
				LOGGER.info("Geotools Process " + processDescription.getIdentifier().stringValue() + " added.");
		    }
		    
			 
		}
	
		LOGGER.info("Initialization of Geotools Repository successfull");
	}


	public boolean containsAlgorithm(String processID) {
		if(registeredProcesses.containsKey(processID)){
			return true;
		}
		LOGGER.warn("Could not find Geotools Process " + processID, null);
		return false;
	}

	@Override
	public IAlgorithm getAlgorithm(String processID) {
		if(!containsAlgorithm(processID)){
			throw new RuntimeException("Could not allocate Process");
		}
		return new GenericGeotoolsProcessDelegator(processID, registeredProcesses.get(processID));
		
	}


	@Override
	public Collection<String> getAlgorithmNames() {
		return registeredProcesses.keySet();
	}


	@Override
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
	
	

}*/
