package org.n52.wps.server.request;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import net.opengis.wps.x100.DeployProcessDocument;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.AbstractTransactionalAlgorithm;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.repository.ITransactionalAlgorithmRepository;
import org.n52.wps.server.repository.TransactionalRepositoryManager;
import org.n52.wps.server.request.deploy.DeploymentProfile;
import org.n52.wps.server.response.DeployProcessResponse;
import org.n52.wps.server.response.Response;
import org.n52.wps.server.response.builder.DeployProcessResponseBuilder;
import org.w3c.dom.Document;

public class DeployProcessRequest extends Request {

	private DeployProcessDocument deployProcessDom;
	private static Logger LOGGER = Logger.getLogger(DeployProcessRequest.class);
	private DeployProcessResponseBuilder deployProcessRespBuilder;
	private String processID;
	private String schema;
	private ProcessDescriptionType processDescription;
	private DeploymentProfile deploymentProfile;
	private ITransactionalAlgorithmRepository repositoryManager;

	public DeployProcessRequest(CaseInsensitiveMap map) throws ExceptionReport {
		super(map);
		// TODO use optionally for HTTP Get (not supported)
	}

	public DeployProcessRequest(Document doc) throws ExceptionReport {
		super(doc);
		/**
		 * XMLBeans option : the underlying xml text buffer is trimmed
		 * immediately after parsing a document resulting in a smaller memory
		 * footprint.
		 */
		try {
			XmlOptions option = new XmlOptions();
			option.setLoadTrimTextBuffer();
			// Parse (with XMLBeans generated library) the request
			this.deployProcessDom = DeployProcessDocument.Factory.parse(doc,
					option);
			if (this.deployProcessDom == null) {
				LOGGER.fatal("DeployProcessDocument is null");
				throw new ExceptionReport("Error while parsing post data",
						ExceptionReport.MISSING_PARAMETER_VALUE);
			}
		} catch (XmlException e) {
			throw new ExceptionReport("Error while parsing post data",
					ExceptionReport.MISSING_PARAMETER_VALUE, e);
		}
		// TODO Validate the request (semantic compliance)
		// validate();
		// Get useful infos
		processID = getDeployProcessDom().getDeployProcess()
				.getProcessDescription().getIdentifier().getStringValue();
		if (processID == null) {
			throw new ExceptionReport(
					"Error. Could not find process identifier in the process description",
					ExceptionReport.MISSING_PARAMETER_VALUE);
		}
		processDescription = getDeployProcessDom().getDeployProcess()
				.getProcessDescription();
		schema = getDeployProcessDom().getDeployProcess()
				.getDeploymentProfile().getSchema().getHref();
		if (schema == null) {
			throw new ExceptionReport(
					"Error. Could not find schema in the deployment profile",
					ExceptionReport.MISSING_PARAMETER_VALUE);
		}
		LOGGER.info("Deploying process ID: " + processID);
		// Parse the specialized part (profile)
		try {
			// Get the DeployementProfile specialized for this profile
			String deployementProfileClass = TransactionalRepositoryManager
					.getDeploymentProfileForSchema(schema);
			LOGGER.info("deployementprofile class:" + deployementProfileClass);
			// Load the DeployementProfile Constructor for parsing the specific
			// profile part
			Constructor<?> constructor;
			constructor = Class.forName(deployementProfileClass)
					.getConstructor(DeployProcessDocument.class, String.class);
			LOGGER.info("Constructor loaded");
			setDeploymentProfile((DeploymentProfile) constructor.newInstance(
					getDeployProcessDom(), processID));
			LOGGER.info("Deployement Profile Set");
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

	public String getProcessID() {
		return processID;
	}

	public void setProcessID(String processID) {
		this.processID = processID;
	}

	public DeployProcessDocument getDeployProcessDom() {
		return deployProcessDom;
	}

	public void setDeployProcessDom(DeployProcessDocument deployProcessDom) {
		this.deployProcessDom = deployProcessDom;
	}

	@Override
	public Object getAttachedResult() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response call() throws ExceptionReport {
		// stores the Process Description (in WEB-INF)
		// TODO check to enhance with FlatFileDatabase (?)
		LOGGER.info("Starting the deployement...");
		// Get the repository manager (typically the
		// DefaultTransactionalProcessRepository)
		repositoryManager = TransactionalRepositoryManager
				.getMatchingTransactionalRepository(schema);
		if (repositoryManager == null) {
			throw new ExceptionReport("Could not find matching repository",
					ExceptionReport.NO_APPLICABLE_CODE);
		}
		// Add the algorithm the repository of the specific profile
		repositoryManager.addAlgorithm(this);
		// if Deployement Successful then store the process description
		/**
		 * TODO I suggest not to use AbstractTransactionalAlgorithm but a
		 * ProcessDescriptionManager class instead
		 * */
		AbstractTransactionalAlgorithm.setDescription(this.getProcessID(),
				this.getProcessDescription());
		Property algoProp = WPSConfig.getInstance().getRepositoryForFormat(schema).addNewProperty();
		algoProp.setName("Algorithm");
		algoProp.setActive(true);
		algoProp.setStringValue(this.getProcessID());
		WPSConfig.getInstance().save();
		setDeployProcessRespBuilder(new DeployProcessResponseBuilder(this));
		return new DeployProcessResponse(this);
	}

	@Override
	public boolean validate() throws ExceptionReport {
		// TODO Auto-generated method stub
		return false;
	}

	public void setDeployProcessRespBuilder(
			DeployProcessResponseBuilder deployProcessRespBuilder) {
		this.deployProcessRespBuilder = deployProcessRespBuilder;
	}

	public DeployProcessResponseBuilder getDeployProcessRespBuilder() {
		return deployProcessRespBuilder;
	}

	public void setDeploymentProfile(DeploymentProfile deploymentProfile) {
		this.deploymentProfile = deploymentProfile;
	}

	public DeploymentProfile getDeploymentProfile() {
		return deploymentProfile;
	}

	public ProcessDescriptionType getProcessDescription() {
		return processDescription;
	}

	public void setProcessDescription(ProcessDescriptionType processDescription) {
		this.processDescription = processDescription;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

}
