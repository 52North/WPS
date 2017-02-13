package org.n52.wps.server.request;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.xml.namespace.QName;

import net.opengis.ows.x11.MetadataType;
import net.opengis.wps.x100.DeployProcessDocument;
import net.opengis.wps.x100.ProcessDescriptionDocument;
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

import xint.esa.ese.wps.metadata.MetadataDocument;

public class DeployProcessRequest extends Request {

	private DeployProcessDocument deployProcessDom;
	private static Logger LOGGER = Logger.getLogger(DeployProcessRequest.class);
	private DeployProcessResponseBuilder deployProcessRespBuilder;
	private String processID;
	private String schema;
	private ProcessDescriptionType processDescription;
	private DeploymentProfile deploymentProfile;
	private ITransactionalAlgorithmRepository repositoryManager;
	
	public static void main(String[] args) {
		try {
			File f = new File("D:\\users\\cnl\\project\\ESE\\meetings\\demo\\OozieURLList\\OozieURL-Estimates.xml");
			ProcessDescriptionDocument docx = ProcessDescriptionDocument.Factory.parse(f);
			System.out.println(docx.toString());
			System.out.println(docx.validate());

			 f = new File("D:\\users\\cnl\\project\\ESE\\meetings\\demo\\OozieURLList\\DeployProcessRequest.txt");
			
			DeployProcessDocument doc = DeployProcessDocument.Factory.parse(f);
			MetadataType test = doc.getDeployProcess().getProcessDescription().getMetadataArray()[0];
			
			System.out.println("validation is "+doc.validate());
			System.out.println("validation is "+doc.getDeployProcess().getProcessDescription().getMetadataArray()[0].validate());
			System.out.println(doc.getDeployProcess().getProcessDescription().getMetadataArray()[0].toString());
			
			//MetadataDcument doc2 = (MetadataDocument) doc.getDeployProcess().getProcessDescription().getMetadataArray()[0].getAbstractMetaData();
//			  QName qname = new javax.xml.namespace.QName("http://ese.esa.int/wps/metadata", "Metadata");
			// doc2 = (MetadataDocument) doc.getDeployProcess().getProcessDescription().getMetadataArray()[0].getAbstractMetaData().substitute(qname, MetadataDocument.type);
	//		 System.out.println(doc2.toString());
			//DeployProcessDocument doc = DeployProcessDocument.Factory.parse(f);
			MetadataDocument doc2 = MetadataDocument.Factory.parse(doc.getDeployProcess().getProcessDescription().getMetadataArray()[0].toString());
			doc.getDeployProcess().getProcessDescription().removeMetadata(0);
			doc.getDeployProcess().getProcessDescription().addNewMetadata().set(doc2);
		System.out.println(doc.toString());
			
			System.out.println("validation is "+doc.validate( ));
			System.out.println("validation is "+doc2.getMetadata().validate());
			System.out.println("validation is "+doc2.getMetadata().getResourceEstimates().validate());
			
		} catch (XmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

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
			LOGGER.debug("Starting DeployProcessREquest constructor");
			XmlOptions option = new XmlOptions();
			option.setLoadTrimTextBuffer();
			// Parse (with XMLBeans generated library) the request
			LOGGER.debug("Parsing");
			this.deployProcessDom = DeployProcessDocument.Factory.parse(doc,
					option);
			/**
			 * XMLBeans workaround: xmlbeans does not parse correctly the document when including an abstract element
			 * The workaround rebuild the object as expected
			 */
			if(this.deployProcessDom.getDeployProcess().getProcessDescription().getMetadataArray() !=null && this.deployProcessDom.getDeployProcess().getProcessDescription().getMetadataArray().length > 0) {
				MetadataDocument substElement = MetadataDocument.Factory.parse(this.deployProcessDom.getDeployProcess().getProcessDescription().getMetadataArray()[0].toString());
				this.deployProcessDom.getDeployProcess().getProcessDescription().removeMetadata(0);
				this.deployProcessDom.getDeployProcess().getProcessDescription().addNewMetadata().set(substElement);	
			}
			

		} catch (XmlException e) {
			throw new ExceptionReport("Error while parsing post data",
					ExceptionReport.MISSING_PARAMETER_VALUE, e);
		}
			if (this.deployProcessDom == null ) {
				LOGGER.fatal("DeployProcessDocument is null");
				throw new ExceptionReport("Error while parsing post data",
						ExceptionReport.MISSING_PARAMETER_VALUE);
			}
		if(!this.deployProcessDom.getDeployProcess().getDeploymentProfile().validate()) {
			throw new ExceptionReport("DeployProcess request (deployement profile section) is not valid against WPS-G schemas)",ExceptionReport.INVALID_PARAMETER_VALUE);
		} 
		if(!this.deployProcessDom.getDeployProcess().getProcessDescription().getDataInputs().validate()) {
			throw new ExceptionReport("DeployProcess request (data inputs section) is not valid against WPS-G schemas)",ExceptionReport.INVALID_PARAMETER_VALUE);
		}
		if(!this.deployProcessDom.getDeployProcess().getProcessDescription().getProcessOutputs().validate()) {
			throw new ExceptionReport("DeployProcess request is not valid against WPS-G schemas)",ExceptionReport.INVALID_PARAMETER_VALUE);
		}
		if(this.deployProcessDom.getDeployProcess().getProcessDescription().getMetadataArray()!=null && this.deployProcessDom.getDeployProcess().getProcessDescription().getMetadataArray().length > 0) {
			try {
				MetadataDocument doc2 = MetadataDocument.Factory.parse(this.deployProcessDom.getDeployProcess().getProcessDescription().getMetadataArray()[0].toString());
				if(!doc2.validate()) {
					throw new ExceptionReport("DeployProcess request (in Metadata section) is not valid against WPS-G schemas)",ExceptionReport.INVALID_PARAMETER_VALUE);
				}
			} catch (XmlException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
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
		LOGGER.info("*************************=========... match");
		
		if (repositoryManager == null) {
			LOGGER.info("*************************=========... not found");
			throw new ExceptionReport("Could not find matching repository",
					ExceptionReport.NO_APPLICABLE_CODE);
			
			
		}
		// Add the algorithm the repository of the specific profile
		repositoryManager.addAlgorithm(this);
		LOGGER.info("*************************=========... addedAlgo");
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
