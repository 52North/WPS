package org.n52.wps.server.repository;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.opengis.wps.x100.DataDescriptionType;
import net.opengis.wps.x100.DataDescriptionType;

import org.apache.log4j.Logger;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.RepositoryDocument.Repository;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.AbstractTransactionalAlgorithm;
import org.n52.wps.server.AbstractTransactionalData;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.profiles.DefaultTransactionalAlgorithm;
import org.n52.wps.server.profiles.IDataManager;
import org.n52.wps.server.profiles.IProcessManager;
import org.n52.wps.server.request.DeployDataRequest;
import org.n52.wps.server.request.ExecuteRequest;
import org.n52.wps.server.request.UndeployDataRequest;

/**
 * DefaultTransactionalDataRepository is a default repository which include :
 * -profile deployement class - process manager class - schema related to the
 * profile - algorithm class
 * 
 */
public class DefaultTransactionalDataRepository  {
	private static Logger LOGGER = Logger
			.getLogger(DefaultTransactionalDataRepository.class);
	protected HashMap<String, DataDescriptionType> dataDescriptionMap;
	protected String format;

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	protected IDataManager dataManager;
	private Repository repository;

	public DefaultTransactionalDataRepository(String format) {
		setFormat(format);
		LOGGER.info("DefaultTransactionalDataRepository - format:" + format);
		setRepository(WPSConfig.getInstance().getRepositoryForFormat(format));

		// DONE think of multiple instance of this class registered (yet not
		// possible since singleton)
		Property dataManagerXML = getPropertyForKey("DataManager");
		if (dataManagerXML == null) {
			throw new RuntimeException(
					"Error. Could not find matching DataManager");
		}
		LOGGER.info("found process maanger");
		dataDescriptionMap = new HashMap<String, DataDescriptionType>();
		// TODO check repository is active
		/**
		 * algorithmMap = new HashMap<String, String>(); for (Property property
		 * : getRepository().getPropertyArray()) { if
		 * (property.getName().equalsIgnoreCase("Algorithm") &&
		 * property.getActive()) { algorithmMap.put(property.getStringValue(),
		 * property.getStringValue()); } }
		 */
		String className = dataManagerXML.getStringValue();
		try {
			LOGGER.info("Data Manager class: "
					+ dataManagerXML.getStringValue());
			Class<?> dataManagerClass = Class.forName(className);
			Constructor constructor = dataManagerClass
			.getConstructor(DefaultTransactionalDataRepository.class);
			dataManager = (IDataManager) constructor
			.newInstance(this);
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
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
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

	public boolean addData(Object process) throws ExceptionReport {
		if (!(process instanceof DeployDataRequest)) {
			return false;
		}
		DeployDataRequest request = (DeployDataRequest) process;
		if(containsData(request.getDataID())) {
			throw new ExceptionReport("Data already exists. Please undeploy before redeploying a Process.",
					ExceptionReport.NO_APPLICABLE_CODE);
		}
		try {
			LOGGER.info("Deploy data in manager: " + this.getFormat());
			dataManager.deployData(request);
			Property algoProp = getRepository().addNewProperty();
			algoProp.setName("Data");
			algoProp.setActive(true);
			algoProp.setStringValue(request.getDataID());
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
	 * processManager.containsData(processID); Note: for some profile the
	 * backend doesn't support any deploy / undeploy / contains operation ->
	 * done through WPS
	 */
	public boolean containsData(String processID) {
		try {
			LOGGER.info("contains data");
			for (Property property : getRepository().getPropertyArray()) {
				LOGGER.info(property.getName());
				if (property.getName().equalsIgnoreCase("Data")
						&& property.getActive()) {
					LOGGER.info(property.getStringValue());
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


	public Collection<String> getDataNames() {
		try {
			return dataManager.getAllDatas();
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<String>();
		}
	}

	
	public boolean removeData(Object process) {
		LOGGER.info("removeData");
		if (!(process instanceof UndeployDataRequest)) {
			LOGGER.info("not instance");
			return false;
		}
		UndeployDataRequest request = (UndeployDataRequest) process;
		try {
			LOGGER.info("try undeploy");
			dataManager.unDeployData(request);
			Property[] propArray = getRepository().getPropertyArray();
			for (int i = 0; i < propArray.length; i++) {
				LOGGER.info(i);
				Property algoProp = propArray[i];
				if (algoProp.getName().equalsIgnoreCase("Data")
						&& algoProp.getActive()) {
					if (algoProp.getStringValue()
							.equals(request.getDataID())) {
						getRepository().removeProperty(i);
						WPSConfig.getInstance().save();
						dataDescriptionMap.remove(request.getDataID());
						return true;
					}
				}
			}
			
		} catch (Exception e) {
			LOGGER.warn("Could not remove algorithm: " + request);
			e.printStackTrace();
			return false;
		}
		return false;

	}

	
	public DataDescriptionType getDataDescription(String processID) {
		if (!dataDescriptionMap.containsKey(processID)) {
			LOGGER.info("Adding new process description to the map.");
			dataDescriptionMap.put(processID,
					AbstractTransactionalData.getDescription(processID));
		}
		return dataDescriptionMap.get(processID);
	}

}
