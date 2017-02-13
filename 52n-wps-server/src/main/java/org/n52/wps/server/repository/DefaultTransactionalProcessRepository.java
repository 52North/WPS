package org.n52.wps.server.repository;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.opengis.wps.x100.DataDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.log4j.Logger;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.RepositoryDocument.Repository;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.AbstractTransactionalAlgorithm;
import org.n52.wps.server.AbstractTransactionalData;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.profiles.AbstractProcessManager;
import org.n52.wps.server.profiles.DefaultTransactionalAlgorithm;
import org.n52.wps.server.profiles.IProcessManager;
import org.n52.wps.server.request.DeployProcessRequest;
import org.n52.wps.server.request.ExecuteRequest;
import org.n52.wps.server.request.UndeployProcessRequest;

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
	protected Map<String, DataDescriptionType> dataDescriptionMap;
	protected String format;

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	protected IProcessManager processManager;
	private Repository repository;

	public DefaultTransactionalProcessRepository(String format) {
		setFormat(format);
		LOGGER.info("DefaultTransactionalProcessRepository - format:" + format);
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
		dataDescriptionMap = new HashMap<String, DataDescriptionType>();
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
	/** Should not be used anymore (see new signature)	
	 * if (!(process instanceof DeployProcessRequest)) {
			return false;
		}
		DeployProcessRequest request = (DeployProcessRequest) process;
		try {
			if(containsAlgorithm(request.getProcessID())) {
			throw new ExceptionReport("Process already exists. Please undeploy before redeploying a Process.",
					ExceptionReport.NO_APPLICABLE_CODE);
			}
			LOGGER.info("Adding process for profile: " + this.getFormat());
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
		}*/
		return false;
	}

	public void addAlgorithm(DeployProcessRequest request) throws ExceptionReport {
				if(containsAlgorithm(request.getProcessID())) {
				throw new ExceptionReport("Process already exists. Please undeploy before redeploying a Process.",
						ExceptionReport.NO_APPLICABLE_CODE);
			}
			LOGGER.info("Adding process for profile: " + this.getFormat());
			try {
				processManager.deployProcess(request);
				/**
				 * Moved (TODO delete)
				Property algoProp = getRepository().addNewProperty();
				algoProp.setName("Algorithm");
				algoProp.setActive(true);
				algoProp.setStringValue(request.getProcessID());
				WPSConfig.getInstance().save();
				*/
			} catch (Exception e) {
				e.printStackTrace();
				throw new ExceptionReport("Deployment failed.",
						ExceptionReport.NO_APPLICABLE_CODE);
			}
		return;
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
	public Collection<String> getDataNames() {
		LOGGER.info("Get data names...");
		Collection<String> dataNames= new ArrayList<String>();
		try {
			for (Property property : getRepository().getPropertyArray()) {
				if (property.getName().equalsIgnoreCase("Data")
						&& property.getActive()) {
					if (!property.getStringValue().isEmpty()) {
						dataNames.add(property.getStringValue());
						LOGGER.info(property.getStringValue());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	return dataNames;
	}
	public Collection<String> getAlgorithmNames() {
		Collection<String> algoNames= new ArrayList<String>();
		try {
			for (Property property : getRepository().getPropertyArray()) {
				if (property.getName().equalsIgnoreCase("Algorithm")
						&& property.getActive()) {
					if (!property.getStringValue().isEmpty()) {
						algoNames.add(property.getStringValue());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	return algoNames;
		/**
		 * Previous implementation which request backend : 
		 * in SSEGRid this is not the case 
		 * 
		 try {
		 
			return processManager.getAllProcesses();
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<String>();
		}
		*/
	}

	public Collection<IAlgorithm> getAlgorithms() {
		Collection<IAlgorithm> result = new ArrayList<IAlgorithm>();
		Collection<String> allAlgorithms;
		try {
			LOGGER.info("class of processManager:"
					+ processManager.getClass().getName());
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
		LOGGER.info("removeAlgorithm");
		if (!(process instanceof UndeployProcessRequest)) {
			LOGGER.info("not instance");
			return false;
		}
		UndeployProcessRequest request = (UndeployProcessRequest) process;
		try {
			LOGGER.info("try undeploy");
			Property[] propArray = getRepository().getPropertyArray();
			for (int i = 0; i < propArray.length; i++) {
				LOGGER.info(i);
				Property algoProp = propArray[i];
				if (algoProp.getName().equalsIgnoreCase("Algorithm")
						&& algoProp.getActive()) {
					if (algoProp.getStringValue()
							.equals(request.getProcessID())) {
						getRepository().removeProperty(i);
						WPSConfig.getInstance().save();
						processDescriptionMap.remove(request.getProcessID());
						return true;
					}
				}
			}
			processManager.unDeployProcess(request);
			
			
		} catch (Exception e) {
			LOGGER.warn("Could not remove algorithm: " + request);
			e.printStackTrace();
			return false;
		}
		return false;

	}

	@Override
	public ProcessDescriptionType getProcessDescription(String processID) {
		if (!processDescriptionMap.containsKey(processID)) {
			LOGGER.info("Adding new process description to the map.");
			processDescriptionMap.put(processID,
					AbstractTransactionalAlgorithm.getDescription(processID));
		}
		return processDescriptionMap.get(processID);
	}

	public boolean containsData(String dataName) {
		try {
			for (Property property : getRepository().getPropertyArray()) {
				if (property.getName().equalsIgnoreCase("Data")
						&& property.getActive()) {
					if (property.getStringValue().equals(dataName)) {
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

	public DataDescriptionType getDataDescription(String dataName) {
		LOGGER.info("start");
		if (!dataDescriptionMap.containsKey(dataName)) {
			LOGGER.info("Adding new process description to the map.");
			dataDescriptionMap.put(dataName,
					AbstractTransactionalData.getDescription(dataName));
		}
		return dataDescriptionMap.get(dataName);

	}

}
