package org.n52.wps.server.repository;




import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.log4j.Logger;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.profiles.AbstractProcessManager;
import org.n52.wps.server.profiles.DefaultTransactionalAlgorithm;
import org.n52.wps.server.profiles.IProcessManager;
import org.n52.wps.server.request.DeployProcessRequest;
import org.n52.wps.server.request.ExecuteRequest;
import org.n52.wps.transactional.request.UndeployProcessRequest;



/**
 * DefaultTransactionalProcessRepository is a default repository 
 * which include :
 * -profile deployement class
 * - process manager class
 * - schema related to the profile
 * - algorithm class
 * 
 */
public class DefaultTransactionalProcessRepository implements ITransactionalAlgorithmRepository{
	private static Logger LOGGER = Logger.getLogger(DefaultTransactionalProcessRepository.class);
	protected Map<String, ProcessDescriptionType> processDescriptionMap;
	
	protected IProcessManager processManager;
	
	
	public DefaultTransactionalProcessRepository(String format){
		Property[] properties = WPSConfig.getInstance().getPropertiesForRepositoryClass(this.getClass().getName());
		//TODO think of multiple instance of this class registered (yet not possible since singleton)
		Property processManagerXML = WPSConfig.getInstance().getPropertyForKey(properties, "ProcessManager");
		if(processManagerXML==null){
			throw new RuntimeException("Error. Could not find matching ProcessManager");
		}
		processDescriptionMap = new HashMap<String, ProcessDescriptionType>();
		String className = processManagerXML.getStringValue();
		try {
			LOGGER.info("Process Manager class: "+processManagerXML.getStringValue());
			Class<?> processManagerClass = Class.forName(className);
			if(processManagerClass.asSubclass(AbstractProcessManager.class).equals(processManagerClass)){
				Constructor constructor = processManagerClass.getConstructor(ITransactionalAlgorithmRepository.class);
				processManager = (IProcessManager) constructor.newInstance(this);
				LOGGER.info("asSubclass");
			}else{
				processManager = (IProcessManager) processManagerClass.newInstance();
			}
			
		} catch (InstantiationException e) {
			e.printStackTrace();
			throw new RuntimeException("Error. Could not find matching DeployManager");
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new RuntimeException("Error. Could not find matching DeployManager");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("Error. Could not find matching DeployManager");
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
		
	public boolean addAlgorithm(Object process) {
		if(!(process instanceof DeployProcessRequest)){
			return false;
		}
		DeployProcessRequest request = (DeployProcessRequest) process;
		try {
			processManager.deployProcess(request);
		} catch (Exception e) {
			LOGGER.warn("Could not instantiate algorithm: " + request);
			e.printStackTrace();
			return false;
		}
		return true;

	}

	
	public boolean containsAlgorithm(String processID) {
		try {
			return processManager.containsProcess(processID);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} 
	}

	public IAlgorithm getAlgorithm(String processID, ExecuteRequest executeRequest) {
		return new DefaultTransactionalAlgorithm(processID, this.getClass());
		
	}

	public Collection<String> getAlgorithmNames() {
		try {
			return processManager.getAllProcesses();
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<String>();
			
		}
	}

	public Collection<IAlgorithm> getAlgorithms() {
		Collection<IAlgorithm> result = new ArrayList<IAlgorithm>();
		Collection<String> allAlgorithms;
		try {
			allAlgorithms = processManager.getAllProcesses();
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
			processManager.unDeployProcess(request);
		} catch (Exception e) {
			LOGGER.warn("Could not remove algorithm: " + request);
			e.printStackTrace();
			return false;
		}
		processDescriptionMap.remove(request.getProcessID());
		return true;
		
	}
	
	@Override
	public ProcessDescriptionType getProcessDescription(String processID) {
		if(!processDescriptionMap.containsKey(processID)){
			processDescriptionMap.put(processID, getAlgorithm(processID, null).getDescription());
		}
		return processDescriptionMap.get(processID);
	}

}
