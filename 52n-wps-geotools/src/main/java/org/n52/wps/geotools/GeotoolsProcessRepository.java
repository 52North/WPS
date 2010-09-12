package org.n52.wps.geotools;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.log4j.Logger;
import org.geotools.process.ProcessFactory;
import org.geotools.process.Processors;
import org.geotools.process.feature.BufferFeatureCollectionFactory;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.IAlgorithmRepository;


/*
 * A container, which allows the 52n WPS to recognize the sextante library.
 * Basic initialization is performed here.
 * 
 * Whenever a getcapabilities request comes in, the process names are extraced based on the available process description documents for sextante processes.
 * This should be changed in the future, when process descriptions should be generated automatically. When a execute process request comes in, a generic GenericSextanteProcessDelegator is created. 
 */


public class GeotoolsProcessRepository implements IAlgorithmRepository{
	private static Logger LOGGER = Logger.getLogger(GeotoolsProcessRepository.class);
	private Map<String, ProcessDescriptionType> registeredProcesses;
	 
	
	public GeotoolsProcessRepository(){
		Set<ProcessFactory> processFactories = Processors.getProcessFactories();
		Iterator<ProcessFactory> iterator = processFactories.iterator();
		while (iterator.hasNext()) {
		    
		    ProcessFactory processFactory = (ProcessFactory) iterator.next();

		   
		}

		
		LOGGER.info("Initialization of Geotools Repository successfull");
	}


	@Override
	public boolean containsAlgorithm(String processID) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public IAlgorithm getAlgorithm(String processID) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Collection<String> getAlgorithmNames() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Collection<IAlgorithm> getAlgorithms() {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
