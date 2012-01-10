package org.n52.wps.server.request;

import net.opengis.wps.x100.UndeployProcessDocument;
import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.server.AbstractTransactionalAlgorithm;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.repository.ITransactionalAlgorithmRepository;
import org.n52.wps.server.repository.RepositoryManager;
import org.n52.wps.server.response.UndeployProcessResponse;
import org.n52.wps.server.response.Response;
import org.n52.wps.server.response.builder.UndeployProcessResponseBuilder;
import org.w3c.dom.Document;

public class UndeployProcessRequest extends Request {

	private UndeployProcessDocument undeployProcessDom;
	private static Logger LOGGER = Logger.getLogger(UndeployProcessRequest.class);
	private UndeployProcessResponseBuilder undeployProcessRespBuilder;
	private String processID;
	private ITransactionalAlgorithmRepository repositoryManager;

	public UndeployProcessRequest(CaseInsensitiveMap map) throws ExceptionReport {
		super(map);
		// TODO Auto-generated constructor stub
	}

	public UndeployProcessRequest(Document doc) throws ExceptionReport {
		super(doc);
		/**
		 * XMLBeans option : the underlying xml text buffer is trimmed
		 * immediately after parsing a document resulting in a smaller memory
		 * footprint.
		 */
		try {
			XmlOptions option = new XmlOptions();
			option.setLoadTrimTextBuffer();
			this.undeployProcessDom = UndeployProcessDocument.Factory.parse(doc,
					option);
			if (this.undeployProcessDom == null) {
				LOGGER.fatal("UndeployProcessDocument is null");
				throw new ExceptionReport("Error while parsing post data",
						ExceptionReport.MISSING_PARAMETER_VALUE);
			}
		} catch (XmlException e) {
			throw new ExceptionReport("Error while parsing post data",
					ExceptionReport.MISSING_PARAMETER_VALUE, e);
		}
		// Validate the request
		if(!this.undeployProcessDom.validate()) {
			throw new ExceptionReport("UndeployProcess request is not valid (according WPS schemas)",ExceptionReport.INVALID_PARAMETER_VALUE);
		}
		// Get useful infos
		processID = getUndeployProcessDom().getUndeployProcess().getIdentifier().getStringValue();
		Logger.getLogger(UndeployProcessRequest.class).info(
				"process ID: " + processID);

	}

	public String getProcessID() {
		return processID;
	}

	public void setProcessID(String processID) {
		this.processID = processID;
	}

	public UndeployProcessDocument getUndeployProcessDom() {
		return undeployProcessDom;
	}

	public void setDeployProcessDom(UndeployProcessDocument deployProcessDom) {
		this.undeployProcessDom = deployProcessDom;
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
		LOGGER.info("call()");
		try {
			// Get the repository manager
			 repositoryManager = (ITransactionalAlgorithmRepository) RepositoryManager.getInstance().getRepositoryForAlgorithm(processID); 
			if (repositoryManager == null) {
				throw new ExceptionReport("Could not find matching repository",
						ExceptionReport.NO_APPLICABLE_CODE);
			}
			LOGGER.info(this.getClass().getCanonicalName());
			if (!repositoryManager.removeAlgorithm(this)) {
				throw new ExceptionReport("Could not undeploy process",
						ExceptionReport.NO_APPLICABLE_CODE);
			} 
			else
			{
				// if Deployement Successful then store the process description
				// TODO don't use AbstractTransactionalAlgorithm but a Process Description manager !!!
				AbstractTransactionalAlgorithm.removeDescription(this.getProcessID());
				
			}
		} catch (RuntimeException e) {
			throw new ExceptionReport("Could not deploy process",
					ExceptionReport.NO_APPLICABLE_CODE);
		}
		
		setUndeployProcessRespBuilder(new UndeployProcessResponseBuilder(this));
		return new UndeployProcessResponse(this);
	}

	@Override
	public boolean validate() throws ExceptionReport {
		// TODO Auto-generated method stub
		return false;
	}


	public void setUndeployProcessRespBuilder(UndeployProcessResponseBuilder deployProcessRespBuilder) {
		this.undeployProcessRespBuilder = deployProcessRespBuilder;
	}

	public UndeployProcessResponseBuilder getUndeployProcessRespBuilder() {
		return undeployProcessRespBuilder;
	}




	
}
