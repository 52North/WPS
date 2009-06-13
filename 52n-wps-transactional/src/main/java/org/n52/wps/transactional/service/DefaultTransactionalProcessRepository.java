package org.n52.wps.transactional.service;




import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.ITransactionalAlgorithmRepository;
import org.n52.wps.transactional.algorithm.DefaultTransactionalAlgorithm;
import org.n52.wps.transactional.deploy.IDeployManager;
import org.n52.wps.transactional.request.DeployProcessRequest;
import org.n52.wps.transactional.request.UndeployProcessRequest;




public class DefaultTransactionalProcessRepository implements ITransactionalAlgorithmRepository{
	private static Logger LOGGER = Logger.getLogger(DefaultTransactionalProcessRepository.class);
	
	
	private IDeployManager deployManager;
	
	public DefaultTransactionalProcessRepository(){
		Property[] properties = WPSConfig.getInstance().getPropertiesForRepositoryClass(this.getClass().getName());
		//TODO think of multiple instance of this class registered (yet not possible since singleton)
		Property deployManagerXML = WPSConfig.getInstance().getPropertyForKey(properties, "DeployManager");
		if(deployManagerXML==null){
			throw new RuntimeException("Error. Could not find matching DeployManager");
		}
		String className = deployManagerXML.getStringValue();
		try {
			deployManager = (IDeployManager) Class.forName(className).newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
			throw new RuntimeException("Error. Could not find matching DeployManager");
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new RuntimeException("Error. Could not find matching DeployManager");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("Error. Could not find matching DeployManager");
		}
		
	}
		
	public boolean addAlgorithm(Object process) {
		if(!(process instanceof DeployProcessRequest)){
			return false;
		}
		DeployProcessRequest request = (DeployProcessRequest) process;
		try {
			deployManager.deployProcess(request);
		} catch (Exception e) {
			LOGGER.warn("Could not instantiate algorithm: " + request);
			e.printStackTrace();
			return false;
		}
		return true;

	}

	
	public boolean containsAlgorithm(String processID) {
		try {
			return deployManager.containsProcess(processID);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} 
	}

	public IAlgorithm getAlgorithm(String processID) {
		return new DefaultTransactionalAlgorithm(processID, this.getClass());
		
	}

	public Collection<String> getAlgorithmNames() {
		try {
			return deployManager.getAllProcesses();
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<String>();
			
		}
	}

	public Collection<IAlgorithm> getAlgorithms() {
		Collection<IAlgorithm> result = new ArrayList<IAlgorithm>();
		Collection<String> allAlgorithms;
		try {
			allAlgorithms = deployManager.getAllProcesses();
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<IAlgorithm>();
		} 
		for(String processID : allAlgorithms){
			result.add(new DefaultTransactionalAlgorithm(processID, this.getClass()));
		}
		return result;
	}

	public boolean removeAlgorithm(Object process) {
		if(!(process instanceof UndeployProcessRequest)){
			return false;
		}
		UndeployProcessRequest request = (UndeployProcessRequest) process;
		try {
			deployManager.unDeployProcess(request);
		} catch (Exception e) {
			LOGGER.warn("Could not remove algorithm: " + request);
			e.printStackTrace();
			return false;
		}
		return true;
		
	}

}
