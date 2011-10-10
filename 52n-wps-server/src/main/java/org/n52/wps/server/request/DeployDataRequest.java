package org.n52.wps.server.request;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


import net.opengis.wps.x100.DataDescriptionType;
import net.opengis.wps.x100.DeployDataDocument;
import net.opengis.wps.x100.DeployDataDocument;
import net.opengis.wps.x100.DataDescriptionType;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.server.AbstractTransactionalAlgorithm;
import org.n52.wps.server.AbstractTransactionalData;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.repository.DefaultTransactionalDataRepository;
import org.n52.wps.server.repository.ITransactionalAlgorithmRepository;
import org.n52.wps.server.repository.TransactionalRepositoryManager;
import org.n52.wps.server.request.deploy.DeploymentProfile;
import org.n52.wps.server.response.DeployDataResponse;
import org.n52.wps.server.response.Response;
import org.n52.wps.server.response.builder.DeployDataResponseBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class DeployDataRequest extends Request {

	private DeployDataDocument deployDataDom;
	private static Logger LOGGER = Logger.getLogger(DeployDataRequest.class);
	private DeployDataResponseBuilder deployDataRespBuilder;
	private String dataID;
	private String schema;
	private DataDescriptionType dataDescription;
	private DeploymentProfile deploymentProfile;
	private DefaultTransactionalDataRepository repositoryManager;

	public DeployDataRequest(CaseInsensitiveMap map) throws ExceptionReport {
		super(map);
		// TODO Auto-generated constructor stub
	}

	public DeployDataRequest(Document doc) throws ExceptionReport {
		super(doc);
		/**
		 * XMLBeans option : the underlying xml text buffer is trimmed
		 * immediately after parsing a document resulting in a smaller memory
		 * footprint.
		 */
		LOGGER.info("Deploy Data Request ----------");
		try {
			XmlOptions option = new XmlOptions();
			option.setLoadTrimTextBuffer();
			this.deployDataDom = DeployDataDocument.Factory.parse(doc,
					option);
			if (this.deployDataDom == null) {
				LOGGER.fatal("DeployDataDocument is null");
				throw new ExceptionReport("Error while parsing post data",
						ExceptionReport.MISSING_PARAMETER_VALUE);
			}
		} catch (XmlException e) {
			throw new ExceptionReport("Error while parsing post data",
					ExceptionReport.MISSING_PARAMETER_VALUE, e);
		}
		// Validate the request
		validate();
		// Get useful infos
		dataID = getDeployDataDom().getDeployData().getDataDescription().getIdentifier().getStringValue();
		 dataDescription = getDeployDataDom().getDeployData().getDataDescription();
		schema = getDeployDataDom().getDeployData().getDeploymentProfile().getSchema().getHref();
		if (schema == null) {
			throw new ExceptionReport(
					"Error. Could not find schema in the deployment profile",
					ExceptionReport.MISSING_PARAMETER_VALUE);
		}
		Logger.getLogger(DeployDataRequest.class).info(
				"data ID: " + dataID);
		// Parse the specialized part (profile)
		try {
			
			// Get the DeployementProfile specialized for this profile
			String deployementProfileClass = TransactionalRepositoryManager
					.getDataDeploymentProfileForSchema(schema);
			LOGGER.info("deployementprofile class:"+deployementProfileClass);		
			// Load the DeployementProfile Constructor
			Constructor<?> constructor;
			constructor = Class.forName(deployementProfileClass).getConstructor(
					DeployDataDocument.class, String.class);
			LOGGER.info("constructor loaded");
			setDeploymentProfile((DeploymentProfile) constructor
					.newInstance(getDeployDataDom(),
							dataID));
			LOGGER.info("DeployementPRofile Set");
		} catch (NoSuchMethodException e) {
			throw new ExceptionReport("An error has occurred while obtaining "
					+ "the deployment profile",
					ExceptionReport.NO_APPLICABLE_CODE, e);
		} catch (ClassNotFoundException e) {
			throw new ExceptionReport("An error has occurred while obtaining "
					+ "the deployment profile",
					ExceptionReport.NO_APPLICABLE_CODE, e);
		} catch (InstantiationException e) {
			throw new ExceptionReport("An error has occurred while obtaining "
					+ "the deployment profile",
					ExceptionReport.NO_APPLICABLE_CODE, e);
		} catch (IllegalAccessException e) {
			throw new ExceptionReport("An error has occurred while obtaining "
					+ "the deployment profile",
					ExceptionReport.NO_APPLICABLE_CODE, e);
		} catch (InvocationTargetException e) {
			throw new ExceptionReport("An error has occurred while obtaining "
					+ "the deployment profile",
					ExceptionReport.NO_APPLICABLE_CODE, e);
		}

	}

	public String getDataID() {
		return dataID;
	}

	public void setDataID(String dataID) {
		this.dataID = dataID;
	}

	public DeployDataDocument getDeployDataDom() {
		return deployDataDom;
	}

	public void setDeployDataDom(DeployDataDocument deployDataDom) {
		this.deployDataDom = deployDataDom;
	}

	@Override
	public Object getAttachedResult() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response call() throws ExceptionReport {
		// stores the Data Description (in WEB-INF)
		// TODO check to enhance with FlatFileDatabase (?)
		LOGGER.info("call()");
		try {
			// Get the repository manager
			 repositoryManager = new DefaultTransactionalDataRepository(schema);
			 LOGGER.info("---- ADD DATA ---");
			 LOGGER.info("Set Description...");
				AbstractTransactionalData.setDescription(this.getDataID(), this.getDataDescription());
			if (!repositoryManager.addData(this)) {
				AbstractTransactionalData.removeDescription(this.getDataID());
				throw new ExceptionReport("Could not deploy data",
						ExceptionReport.NO_APPLICABLE_CODE);
			} 
			
		} catch (RuntimeException e) {
			e.printStackTrace();
			throw new ExceptionReport("Could not deploy Data",
					ExceptionReport.NO_APPLICABLE_CODE);
		}
		
		setDeployDataRespBuilder(new DeployDataResponseBuilder(this));
		return new DeployDataResponse(this);
	}

	@Override
	public boolean validate() throws ExceptionReport {
		// TODO Auto-generated method stub
		return false;
	}


	public void setDeployDataRespBuilder(DeployDataResponseBuilder deployDataRespBuilder) {
		this.deployDataRespBuilder = deployDataRespBuilder;
	}

	public DeployDataResponseBuilder getDeployDataRespBuilder() {
		return deployDataRespBuilder;
	}

	public void setDeploymentProfile(DeploymentProfile deploymentProfile) {
		this.deploymentProfile = deploymentProfile;
	}

	public DeploymentProfile getDeploymentProfile() {
		return deploymentProfile;
	}

	public DataDescriptionType getDataDescription() {
		return dataDescription;
	}

	public void setDataDescription(DataDescriptionType dataDescription) {
		this.dataDescription = dataDescription;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	
}
