package org.n52.wps.server.request;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


import net.opengis.wps.x100.DeployProcessDocument;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.server.AbstractTransactionalAlgorithm;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.repository.ITransactionalAlgorithmRepository;
import org.n52.wps.server.repository.TransactionalRepositoryManager;
import org.n52.wps.server.request.deploy.DeploymentProfile;
import org.n52.wps.server.response.DeployProcessResponse;
import org.n52.wps.server.response.Response;
import org.n52.wps.server.response.builder.DeployProcessResponseBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

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
		// TODO Auto-generated constructor stub
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
		// Validate the request
		validate();
		// Get useful infos
		processID = getDeployProcessDom().getDeployProcess()
				.getProcessDescription().getIdentifier().getStringValue();
		 processDescription = getDeployProcessDom()
				.getDeployProcess().getProcessDescription();
		schema = getDeployProcessDom().getDeployProcess()
				.getDeploymentProfile().getSchema().getHref();
		if (schema == null) {
			throw new ExceptionReport(
					"Error. Could not find schema in the deployment profile",
					ExceptionReport.MISSING_PARAMETER_VALUE);
		}
		Logger.getLogger(DeployProcessRequest.class).info(
				"process ID: " + processID);
		// Parse the specialized part (profile)
		try {
			
			// Get the DeployementProfile specialized for this profile
			String deployementProfileClass = TransactionalRepositoryManager
					.getDeploymentProfileForSchema(schema);
			LOGGER.info("deployementprofile class:"+deployementProfileClass);		
			// Load the DeployementProfile Constructor
			Constructor<?> constructor;
			constructor = Class.forName(deployementProfileClass).getConstructor(
					DeployProcessDocument.class, String.class);
			LOGGER.info("constructor loaded");
			setDeploymentProfile((DeploymentProfile) constructor
					.newInstance(getDeployProcessDom(),
							processID));
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

		try {
			// Get the repository manager
			 repositoryManager = TransactionalRepositoryManager
					.getMatchingTransactionalRepository(schema);
			
			if (repositoryManager == null) {
				throw new ExceptionReport("Could not find matching repository",
						ExceptionReport.NO_APPLICABLE_CODE);
			}

			if (!repositoryManager.addAlgorithm(this)) {
				throw new ExceptionReport("Could not deploy process",
						ExceptionReport.NO_APPLICABLE_CODE);
			} 
			else
			{
				// if Deployement Successful then store the process description
				// TODO don't use AbstractTransactionalAlgorithm but a Process Description manager !!!
				AbstractTransactionalAlgorithm.setDescription(this.getProcessID(), this.getProcessDescription());
			}
		} catch (RuntimeException e) {
			throw new ExceptionReport("Could not deploy process",
					ExceptionReport.NO_APPLICABLE_CODE);
		}
		
		setDeployProcessRespBuilder(new DeployProcessResponseBuilder(this));
		return new DeployProcessResponse(this);
	}

	@Override
	public boolean validate() throws ExceptionReport {
		// TODO Auto-generated method stub
		return false;
	}


	public void setDeployProcessRespBuilder(DeployProcessResponseBuilder deployProcessRespBuilder) {
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
