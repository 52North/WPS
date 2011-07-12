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
import org.n52.wps.RepositoryDocument.Repository;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.AbstractTransactionalAlgorithm;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.profiles.AbstractProcessManager;
import org.n52.wps.server.profiles.DefaultTransactionalAlgorithm;
import org.n52.wps.server.profiles.IProcessManager;
import org.n52.wps.server.request.DeployProcessRequest;
import org.n52.wps.server.request.ExecuteRequest;
import org.n52.wps.transactional.request.UndeployProcessRequest;

/**
 * DefaultTransactionalProcessRepository is a default repository which include :
 * -profile deployement class - process manager class - schema related to the
 * profile - algorithm class
 * 
 */
public class DefaultTransactionalProcessRepository implements
		ITransactionalAlgorithmRepository {
	private static Logger LOGGER = Logger
			.getLogger(DefaultTransactionalProcessRepository.class);
	protected Map<String, ProcessDescriptionType> processDescriptionMap;

	protected IProcessManager processManager;
	private Repository repository;

	public DefaultTransactionalProcessRepository(String format) {
		LOGGER.info("DefaultTransactionalProcessRepository - format:"+format);
		setRepository(WPSConfig.getInstance().getRepositoryForFormat(format));

		// DONE think of multiple instance of this class registered (yet not
		// possible since singleton)
		Property processManagerXML = getPropertyForKey("ProcessManager");
		if (processManagerXML == null) {
			throw new RuntimeException(
					"Error. Could not find matching ProcessManager");
		}
		LOGGER.info("found process maanger");
		processDescriptionMap = new HashMap<String, ProcessDescriptionType>();
		// TODO check repository is active
		/**
		 * algorithmMap = new HashMap<String, String>(); for (Property property
		 * : getRepository().getPropertyArray()) { if
		 * (property.getName().equalsIgnoreCase("Algorithm") &&
		 * property.getActive()) { algorithmMap.put(property.getStringValue(),
		 * property.getStringValue()); } }
		 */
		String className = processManagerXML.getStringValue();
		try {
			LOGGER.info("Process Manager class: "
					+ processManagerXML.getStringValue());
			Class<?> processManagerClass = Class.forName(className);
			if (processManagerClass.asSubclass(AbstractProcessManager.class)
					.equals(processManagerClass)) {
				Constructor constructor = processManagerClass
						.getConstructor(ITransactionalAlgorithmRepository.class);
				processManager = (IProcessManager) constructor
						.newInstance(this);
				LOGGER.info("asSubclass");
			} else {
				processManager = (IProcessManager) processManagerClass
						.newInstance();
			}

		} catch (InstantiationException e) {
			e.printStackTrace();
			throw new RuntimeException(
					"Error. Could not find matching DeployManager");
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new RuntimeException(
					"Error. Could not find matching DeployManager");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(
					"Error. Could not find matching DeployManager");
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
		LOGGER.info("end constructor DefaultTrans");
	}

	public Repository getRepository() {
		return repository;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public Property getPropertyForKey(String key) {
		for (Property property : getRepository().getPropertyArray()) {
			if (property.getName().equals(key)) {
				return property;
			}
		}
		return null;
	}

	public boolean addAlgorithm(Object process) {
		if (!(process instanceof DeployProcessRequest)) {
			return false;
		}
		DeployProcessRequest request = (DeployProcessRequest) process;
		try {
			processManager.deployProcess(request);
			Property algoProp = getRepository().addNewProperty();
			algoProp.setName("Algorithm");
			algoProp.setActive(true);
			algoProp.setStringValue(request.getProcessID());
			WPSConfig.getInstance().save();
		} catch (Exception e) {
			LOGGER.warn("Could not instantiate algorithm: " + request);
			e.printStackTrace();
			return false;
		}
		return true;

	}

	/**
	 * TODO : check if the remote contains also the process --- return
	 * processManager.containsProcess(processID); Note: for some profile the
	 * backend doesn't support any deploy / undeploy / contains operation ->
	 * done through WPS
	 */
	public boolean containsAlgorithm(String processID) {
		try {
			for (Property property : getRepository().getPropertyArray()) {
				if (property.getName().equalsIgnoreCase("Algorithm")
						&& property.getActive()) {
					if (property.getStringValue().equals(processID)) {
						return true;
					}
				}
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public IAlgorithm getAlgorithm(String processID,
			ExecuteRequest executeRequest) {
		return new DefaultTransactionalAlgorithm(processID);

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
		for (String processID : allAlgorithms) {
			result.add(new DefaultTransactionalAlgorithm(processID));
		}
		return result;
	}

	public boolean removeAlgorithm(Object process) {
		if (!(process instanceof UndeployProcessRequest)) {
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
		if (!processDescriptionMap.containsKey(processID)) {
			LOGGER.info("Adding new process description to the map.");
			processDescriptionMap.put(processID, AbstractTransactionalAlgorithm.getDescription(processID));
		}
		return processDescriptionMap.get(processID);
	}

}
