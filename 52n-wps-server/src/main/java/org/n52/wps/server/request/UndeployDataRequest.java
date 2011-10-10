package org.n52.wps.server.request;

import net.opengis.wps.x100.UndeployDataDocument;
import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.server.AbstractTransactionalAlgorithm;
import org.n52.wps.server.AbstractTransactionalData;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.repository.DefaultTransactionalDataRepository;
import org.n52.wps.server.repository.DefaultTransactionalProcessRepository;
import org.n52.wps.server.repository.ITransactionalAlgorithmRepository;
import org.n52.wps.server.repository.RepositoryManager;
import org.n52.wps.server.response.UndeployDataResponse;
import org.n52.wps.server.response.Response;
import org.n52.wps.server.response.builder.UndeployDataResponseBuilder;
import org.w3c.dom.Document;

public class UndeployDataRequest extends Request {

	private UndeployDataDocument undeployDataDom;
	private static Logger LOGGER = Logger.getLogger(UndeployDataRequest.class);
	private UndeployDataResponseBuilder undeployDataRespBuilder;
	private String dataID;
	private DefaultTransactionalDataRepository repositoryManager;

	public UndeployDataRequest(CaseInsensitiveMap map) throws ExceptionReport {
		super(map);
		// TODO Auto-generated constructor stub
	}

	public UndeployDataRequest(Document doc) throws ExceptionReport {
		super(doc);
		/**
		 * XMLBeans option : the underlying xml text buffer is trimmed
		 * immediately after parsing a document resulting in a smaller memory
		 * footprint.
		 */
		try {
			XmlOptions option = new XmlOptions();
			option.setLoadTrimTextBuffer();
			this.undeployDataDom = UndeployDataDocument.Factory.parse(doc,
					option);
			if (this.undeployDataDom == null) {
				LOGGER.fatal("UndeployDataDocument is null");
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
		dataID = getUndeployDataDom().getUndeployData().getIdentifier().getStringValue();
		Logger.getLogger(UndeployDataRequest.class).info(
				"process ID: " + dataID);

	}

	public String getDataID() {
		return dataID;
	}

	public void setDataID(String processID) {
		this.dataID = processID;
	}

	public UndeployDataDocument getUndeployDataDom() {
		return undeployDataDom;
	}

	public void setDeployDataDom(UndeployDataDocument deployDataDom) {
		this.undeployDataDom = deployDataDom;
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
			 repositoryManager =  RepositoryManager.getInstance().getRepositoryForData(dataID); 
			if (repositoryManager == null) {
				throw new ExceptionReport("Could not find matching repository",
						ExceptionReport.NO_APPLICABLE_CODE);
			}
			LOGGER.info(this.getClass().getCanonicalName());
			if (!repositoryManager.removeData(this)) {
				throw new ExceptionReport("Could not undeploy process",
						ExceptionReport.NO_APPLICABLE_CODE);
			} 
			else
			{
				// if Deployement Successful then store the process description
				// TODO don't use AbstractTransactionalAlgorithm but a Data Description manager !!!
				AbstractTransactionalData.removeDescription(this.getDataID());
			}
		} catch (RuntimeException e) {
			throw new ExceptionReport("Could not deploy process",
					ExceptionReport.NO_APPLICABLE_CODE);
		}
		
		setUndeployDataRespBuilder(new UndeployDataResponseBuilder(this));
		return new UndeployDataResponse(this);
	}

	@Override
	public boolean validate() throws ExceptionReport {
		// TODO Auto-generated method stub
		return false;
	}


	public void setUndeployDataRespBuilder(UndeployDataResponseBuilder deployDataRespBuilder) {
		this.undeployDataRespBuilder = deployDataRespBuilder;
	}

	public UndeployDataResponseBuilder getUndeployDataRespBuilder() {
		return undeployDataRespBuilder;
	}




	
}
