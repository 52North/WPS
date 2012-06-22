package org.n52.wps.server.profiles.JavaSaga;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.concurrent.CancellationException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

// XMLBeans schemas import
import net.opengis.ows.x11.ExceptionReportDocument;
import net.opengis.ows.x11.ExceptionType;
import net.opengis.wps.x100.AuditTraceType;
import net.opengis.wps.x100.DataInputsType;
import net.opengis.wps.x100.DocumentOutputDefinitionType;
import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.ExecuteResponseDocument.ExecuteResponse.ProcessOutputs;
import net.opengis.wps.x100.InputType;
import net.opengis.wps.x100.OutputDataType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.SagaDeploymentProfileType.JsdlTemplate;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import xint.esa.ssegrid.wps.javaSAGAProfile.URLListDocument;

// Apache import
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.util.XMLUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.ComplexExceptionReport;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.profiles.AbstractProcessManager;
import org.n52.wps.server.repository.ITransactionalAlgorithmRepository;
import org.n52.wps.server.request.DeployProcessRequest;
import org.n52.wps.server.request.ExecuteRequest;
import org.n52.wps.server.request.UndeployProcessRequest;
import org.n52.wps.server.request.deploy.DeploymentProfile;

// Saga import
import org.ogf.saga.context.Context;
import org.ogf.saga.context.ContextFactory;
import org.ogf.saga.error.BadParameterException;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.job.Job;
import org.ogf.saga.session.Session;
import org.ogf.saga.session.SessionFactory;
import org.ogf.saga.task.State;
import org.ogf.saga.url.URLFactory;
import com.terradue.ogf.saga.impl.job.JobDescription;
import com.terradue.ogf.saga.impl.job.JobFactory;
import com.terradue.ogf.saga.impl.job.JobImpl;
import com.terradue.ogf.saga.impl.job.JobServiceImpl;
import com.terradue.ogf.schema.jsdl.JSDLException;
import com.terradue.ogf.schema.jsdl.JSDLFactory;
import com.terradue.ssegrid.sagaext.JSDLNotApplicableException;
import com.terradue.ssegrid.sagaext.JobServiceAssistant;
import com.terradue.ssegrid.sagaext.MyProxyClient;
import com.terradue.ssegrid.sagaext.ProcessingRegistry;

import org.w3.x2005.x08.addressing.MessageIDDocument;
import org.w3.x2005.x08.addressing.ReplyToDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class JavaSagaProcessManager extends AbstractProcessManager {

	// WPS Logger
	private static Logger LOGGER = Logger
			.getLogger(JavaSagaProcessManager.class);

	private String processesPrefix;
	// Location for process deployement
	private String deployProcessDir;
	private String WPSPublicationPrefix;
	private HashMap<String, String> WPSmap;
	private String myProxyURL;
	private String myProxyUser;
	private String myProxyPassword;

	private String IID;
	private String processInstanceID;

	private String processID;
	private static String GridFilesDir;
	private static String SagaLibDir;
	private static org.ogf.saga.url.URL gridmapGLUE;
	private static ProcessingRegistry pr;
	private JobImpl runningJob;
	private boolean cancelHack;
	// Asychronous execute client must be shared between threads
	/**
	 * TODO delete
	 */
	public static ServiceClient executeClient;

	/**
	 * @param repository
	 * @throws NoSuccessException
	 * @throws BadParameterException
	 * @throws JSDLException
	 * @throws JSDLNotApplicableException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public JavaSagaProcessManager(ITransactionalAlgorithmRepository repository)
			throws BadParameterException, NoSuccessException,
			FileNotFoundException, IOException, JSDLNotApplicableException,
			JSDLException {
		super(repository);
		/**
		 * Get useful properties from the repository configuration file: - SAGA
		 * Home : home directory for saga files - GridmapGLUE : the gridmapGLUE
		 * file location
		 */
		Property[] properties = WPSConfig.getInstance()
				.getPropertiesForRepositoryName("JavaSagaRepository");
		Property WPSSaga = WPSConfig.getInstance().getPropertyForKey(
				properties, "GridFilesDir");
		if (WPSSaga == null) {
			throw new RuntimeException(
					"Error. Could not find the required GridFilesDir property in wps_config.xml");
		}
		setGridFilesDir(WPSSaga.getStringValue());
		Property sagaLibProp = WPSConfig.getInstance().getPropertyForKey(
				properties, "SagaLibDir");
		if (sagaLibProp == null) {
			throw new RuntimeException(
					"Error. Could not find the required SagaLibDir property in wps_config.xml");
		}
		setSagaLibDir(sagaLibProp.getStringValue());

		Property myProxyURLProp = WPSConfig.getInstance().getPropertyForKey(
				properties, "MyProxyURL");
		if (myProxyURLProp == null) {
			throw new RuntimeException(
					"Error. Could not find the required MyProxyUser property in wps_config.xml");
		}
		myProxyURL = myProxyURLProp.getStringValue();

		Property myProxyUserProp = WPSConfig.getInstance().getPropertyForKey(
				properties, "MyProxyUser");
		if (myProxyUserProp == null) {
			throw new RuntimeException(
					"Error. Could not find the required MyProxyUser property in wps_config.xml");
		}
		myProxyUser = myProxyUserProp.getStringValue();

		Property myProxyPasswordProp = WPSConfig.getInstance()
				.getPropertyForKey(properties, "MyProxyPassword");
		if (myProxyPasswordProp == null) {
			throw new RuntimeException(
					"Error. Could not find the required MyProxyUser property in wps_config.xml");
		}
		myProxyPassword = myProxyPasswordProp.getStringValue();

		System.setProperty("saga.location", getSagaLibDir());
		Property wpsPublicRoot = WPSConfig.getInstance().getPropertyForKey(
				properties, "WPSPublicationPrefix");
		if (wpsPublicRoot == null) {
			throw new RuntimeException(
					"Error. Could not find WPSPublicationPrefix");
		}
		setWPSPublicationPrefix(wpsPublicRoot.getStringValue());

		// Set the deployement process directory
		setDeployProcessDir(GridFilesDir + "deploy/process/");
		Property gridmap = WPSConfig.getInstance().getPropertyForKey(
				properties, "GridGlue");
		if (gridmap == null) {
			throw new RuntimeException(
					"Error. Could not find the required GridGlue property in wps_config.xml");
		}
		// Saga.location must be loaded before the following line
		JavaSagaProcessManager.gridmapGLUE = URLFactory.createURL(gridmap
				.getStringValue());
		/**
		 * Set the required system properties (instead of setting them from
		 * tomcat script)
		 */

		System.setProperty("gai.default.rm", gridmapGLUE.toString());
		System.setProperty("gai.debug.working.dir", "true");
		System.setProperty("org.globus.tcp.port.range", "20000,22000");
		System.setProperty("gai.deploy.process.path", getDeployProcessDir());
		// Add port range prop org.globus.tcp.port.range="20000,20200"
		// TODO remove (useless here : getPRInstance();)

	}

	/**
	 * Singleton for the ProcessingRegistry
	 * 
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws JSDLNotApplicableException
	 * @throws JSDLException
	 */
	public static ProcessingRegistry getPRInstance()
			throws FileNotFoundException, IOException,
			JSDLNotApplicableException, JSDLException {
		if (pr == null) {
			LOGGER.info("ProcessingRegistry was not already instantiated.");
			try {
				pr = new ProcessingRegistry(true);
			} catch (FileNotFoundException e) {
				pr = new ProcessingRegistry(false);
			}
			// If recover is not working this should work
			/**
			 * FindFilter fileFilter = new FindFilter("*.xml"); File[] files =
			 * new File(getWPSSagaHome()).listFiles(fileFilter); if (files !=
			 * null && files.length > 0) { for (File JSDLFile : files) {
			 * LOGGER.info("Registering JSDL :" + JSDLFile.getAbsolutePath());
			 * pr.registerProcessing(JSDLFactory .createJSDLDocument(JSDLFile));
			 * } }
			 */
		}
		return pr;
	}

	/**
	 * Get the WPS Saga home directory
	 * 
	 * @return
	 */
	public static String getGridFilesDir() {
		return GridFilesDir;
	}

	/**
	 * Set the WPS Saga home directory
	 * 
	 * @param wPSSagaHome
	 */
	public static void setGridFilesDir(String gFilesDir) {
		GridFilesDir = gFilesDir;
	}

	/**
	 * Deploy a process : register the JSDL, store the archive (parsed by the
	 * saga deployement profile) TODO interface signature should move to void
	 * (exception is thrown on failure)
	 * 
	 * @throws ExceptionReport
	 */
	public boolean deployProcess(DeployProcessRequest request)
			throws ExceptionReport {
		DeploymentProfile profile = request.getDeploymentProfile();
		if (!(profile instanceof JavaSagaDeploymentProfile)) {
			throw new ExceptionReport("JavaSaga Deployement Profile not valid",
					ExceptionReport.INVALID_PARAMETER_VALUE);
		}
		JavaSagaDeploymentProfile deploymentProfile = (JavaSagaDeploymentProfile) profile;
		String processID = deploymentProfile.getProcessID();
		LOGGER.info("Saga deployement process for: " + processID);
		try {
			LOGGER.info("storing archive");
			storeArchive(deploymentProfile.getArchive(), processID);
			LOGGER.info("register JSDL");
			registerJSDL(processID, deploymentProfile.getJsdlTemplate());

		} catch (Exception e) {
			e.printStackTrace();
			throw new ExceptionReport("IO Exception during deployement",
					ExceptionReport.NO_APPLICABLE_CODE, e);
		}
		return true;
	}

	/**
	 * Unzip a collection of files contained in a archive zip file and write
	 * onto disk
	 * 
	 * @param archive
	 * @throws IOException
	 */
	private void storeArchive(byte[] archive, String processId)
			throws IOException {
		LOGGER.info("Store Archive");
		String archiveDir = getDeployProcessDir() + processId + File.separator;
		// create dir
		File archiveFile = new File(archiveDir);
		if (!archiveFile.exists()) {
			archiveFile.mkdirs();
		}
		LOGGER.info("Storing archive in " + archiveDir);
		ByteArrayInputStream bais = new ByteArrayInputStream(archive);
		byte[] buf = new byte[1024];
		ZipInputStream zis = new ZipInputStream(bais);
		ZipEntry zipentry = zis.getNextEntry();
		while (zipentry != null) {
			String entryName = zipentry.getName();
			LOGGER.info("Writing file " + entryName);
			int n;
			FileOutputStream fileoutputstream;
			File newFile = new File(entryName);
			String directory = newFile.getParent();
			if (directory == null) {
				if (newFile.isDirectory())
					break;
			}
			fileoutputstream = new FileOutputStream(archiveDir + entryName);
			while ((n = zis.read(buf, 0, 1024)) > -1)
				fileoutputstream.write(buf, 0, n);
			fileoutputstream.close();
			zis.closeEntry();
			zipentry = zis.getNextEntry();
		}
		zis.close();
	}

	private void unstoreArchive(String processId) throws IOException {
		String archiveDir = getDeployProcessDir() + processId + File.separator;
		LOGGER.info("unstoring archive in " + archiveDir);
		File archFile = new File(archiveDir);
		deleteDir(archFile);
	}

	// Deletes all files and subdirectories under dir.
	// Returns true if all deletions were successful.
	// If a deletion fails, the method stops attempting to delete and returns
	// false.
	public static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}

		// The directory is now empty so delete it
		return dir.delete();
	}

	/**
	 * Store the given jsdl document in the DEPLOY_DIR and register with the
	 * Processing Registry.
	 * 
	 * @param processId
	 * @param jsdlTemplate
	 * @throws IOException
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerException
	 * @throws ParserConfigurationException
	 * @throws NoSuccessException
	 * @throws BadParameterException
	 * @throws ExceptionReport
	 * @throws JSDLException
	 * @throws JSDLNotApplicableException
	 */
	private void registerJSDL(String processId, JsdlTemplate jsdlTemplate)
			throws IOException, TransformerFactoryConfigurationError,
			TransformerException, ParserConfigurationException,
			BadParameterException, NoSuccessException, ExceptionReport,
			JSDLNotApplicableException, JSDLException {
		JobDefinitionDocument jsdl = JobDefinitionDocument.Factory
				.newInstance();
		jsdl.addNewJobDefinition().set(jsdlTemplate.getJobDefinition());
		LOGGER.info("JSDL to wrile:" + jsdl);
		String dirPath = GridFilesDir + "JSDLtemplates";
		LOGGER.info(dirPath);
		File directory = new File(dirPath);
		if (!directory.exists()) {
			directory.mkdirs();
		}
		String jsdlPath = dirPath + File.separator + processId + ".xml";
		File f = new File(jsdlPath);
		jsdl.save(f);
		LOGGER.info("Store JSDL in " + jsdlPath);
		// Register the JSDL
		File procJSDL = new File(jsdlPath);
		String regProcId = getPRInstance().registerProcessing(
				JSDLFactory.createJSDLDocument(procJSDL));
		if (!regProcId.equals(processId)) {
			LOGGER.info("matching failed between : " + regProcId + " - "
					+ processId);
			throw new ExceptionReport(
					"Registered process id does not match the right process id",
					ExceptionReport.NO_APPLICABLE_CODE);
		}
		LOGGER.info("registering end...");
	}

	private void unregisterJSDL(String processId)
			throws NoSuchElementException, FileNotFoundException, IOException,
			JSDLNotApplicableException, JSDLException {
		String dirPath = GridFilesDir + "JSDLtemplates";
		String jsdlPath = dirPath + File.separator + processId + ".xml";
		File f = new File(jsdlPath);
		f.delete();
		LOGGER.info("Deleted JSDL in " + jsdlPath);
		// Unregister the JSDL
		try {
			getPRInstance().unregisterProcessing(processId);
		} catch (Exception e) {
			LOGGER.warn("-- PR Unregister failed -");
		}

	}

	/**
	 * TODO
	 * 
	 * @param processID
	 * @return
	 * @throws Exception
	 */
	public boolean unDeployProcess(String processID) throws Exception {
		// Prepare undeploy message
		// TODO
		// Undeploy
		// sendToDeployment(root);
		unregisterJSDL(processID);
		unstoreArchive(processID);

		return true;
	}

	public String getStringFromDocument(Document doc) {
		try {
			DOMSource domSource = new DOMSource(doc);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.transform(domSource, result);
			return writer.toString();
		} catch (TransformerException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public Document invoke(ExecuteRequest req, String algorithmID)
			throws Exception {
		cancelHack = false;
		setProcessInstanceID(req.getId());
		ExecuteDocument doc = req.getExecDom();
		this.processID = algorithmID;
		// Initialize WPS Map
		WPSmap = new HashMap<String, String>();
		WPSmap.put("WPS_DEPLOY_PROCESS_DIR", GridFilesDir + "deploy/process/");
		WPSmap.put("WPS_DEPLOY_AUXDATA_DIR", GridFilesDir + "deploy/auxdata/");
		WPSmap.put("WPS_JOB_INPUTS_DIR", GridFilesDir + "execute/" + processID
				+ "/" + getProcessInstanceID() + "/${GAI_JOB_UID}/inputs");
		WPSmap.put("WPS_JOB_OUTPUTS_DIR", GridFilesDir + "execute/" + processID
				+ "/" + getProcessInstanceID() + "/${GAI_JOB_UID}/outputs");
		WPSmap.put("WPS_JOB_AUDITS_DIR", GridFilesDir + "execute/" + processID
				+ "/" + getProcessInstanceID() + "/${GAI_JOB_UID}/audits");
		WPSmap.put("WPS_JOB_RESULTS_DIR", GridFilesDir + "execute/" + processID
				+ "/" + getProcessInstanceID() + "/${GAI_JOB_UID}/results");
		WPSmap.put("WPS_JOB_RESULTS_URL", WPSPublicationPrefix + "execute/"
				+ processID + "/" + getProcessInstanceID()
				+ "/${GAI_JOB_UID}/results");

		ExecuteResponseDocument execRepDom = null;
		this.setProcessID(algorithmID);
		// First create a session containing at least a context
		// for user credentials information
		Session session = SessionFactory.createSession(false);
		Context context = ContextFactory.createContext("globus");
		context.setAttribute(Context.USERPROXY, GridFilesDir + "proxy");
		session.addContext(context);
		LOGGER.info(context.getAttribute(Context.USERPROXY));
		// Get delegation to that user proxy and set propoerly context
		String cnName = null;
		try {

			for (SOAPHeaderBlock samlHeader : req.getSamlHeader()) {
				LOGGER.info("LocalName = " + samlHeader.getLocalName());
				OMElement assertion = (OMElement) samlHeader
						.getChildrenWithLocalName("Assertion").next();
				LOGGER.info("Assertion found:"+assertion.toString());
				OMElement attributeStatement = (OMElement) assertion
						.getChildrenWithLocalName("AttributeStatement").next();
				LOGGER.info("AttributeStatement found");
				OMElement subject = (OMElement) attributeStatement
						.getChildrenWithLocalName("Subject").next();
				LOGGER.info("Subject found:"+subject.toString());
				OMElement nameIdentifier = (OMElement) subject
						.getChildrenWithLocalName("NameIdentifier").next();
				LOGGER.info("Name Id found"+nameIdentifier.toString());
				cnName = nameIdentifier.getText();
				LOGGER.info(cnName);
			}

		} catch (Exception e) {
		}

		if (cnName != null && !cnName.equals("spb") && !cnName.equals("vito")
				&& !cnName.equals("superuser") && !cnName.equals("esasp")) {
			throw new ExceptionReport(
					"No Grid Proxy account is associated with the user",
					ExceptionReport.REMOTE_COMPUTATION_ERROR);
		}
		MyProxyClient.delegateProxyFromMyProxyServer(myProxyURL, 7512,
				myProxyUser, myProxyPassword, 604800, context);
		// then create a JobService from the JobFactory
		// that is ready to handle job submission passing the session
		// information
		// and the Resource Manager
		// N.B. here is an "extended" JobService
		JobServiceImpl js = JobFactory
				.createJobService(session/* , gridmapGLUE */);
		// Pass the additional substitution
		// create the JobServiceAssistant in order to provide
		// useful methods for the backend WPS process manager
		JobServiceAssistant jsa = new JobServiceAssistant(js);
		jsa.addSubstitutionVariables(WPSmap);
		// initialize a job description from the registered processing
		JobDescription jd = (JobDescription) JobFactory
				.createJobDescription(getPRInstance().getJSDLFromProc(
						algorithmID));
		// Build literal and complex inputs maps
		Map<String, String> literalInputs = new HashMap<String, String>();
		Map<String, Document> complexInputs = new HashMap<String, Document>();
		InputType[] inputs = doc.getExecute().getDataInputs().getInputArray();
		int numberOfTasks = 0;
		for (int i = 0; i < inputs.length; i++) {
			if (inputs[i].getData().isSetLiteralData()) {
				LOGGER.info("Found literal data: "
						+ inputs[i].getIdentifier().getStringValue() + " - "
						+ inputs[i].getData().getLiteralData().getStringValue());
				if (inputs[i].getIdentifier().getStringValue()
						.equals("numberOfTasks")) {
					numberOfTasks = Integer.parseInt(inputs[i].getData()
							.getLiteralData().getStringValue());
				} else {
					literalInputs.put("WPS_INPUT_"
							+ inputs[i].getIdentifier().getStringValue(),
							inputs[i].getData().getLiteralData()
									.getStringValue());
				}
			}
			if (inputs[i].getData().isSetComplexData()) {
				LOGGER.info("Found complex data: "
						+ inputs[i].getIdentifier().getStringValue() + " - "
						+ inputs[i].getData().getComplexData().xmlText());
				// TODO the following conversion to String can be simplified
				StringWriter stringWriter = new StringWriter();
				Transformer transformer = TransformerFactory.newInstance()
						.newTransformer();
				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
						"no");
				NodeList nodeList = inputs[i].getData().getComplexData()
						.getDomNode().getChildNodes();
				Node urlListNode = null;
				for (int k = 0; k < nodeList.getLength(); k++) {
					if (nodeList.item(k).getLocalName() != null
							&& nodeList.item(k).getLocalName()
									.equals("URLList")) {
						urlListNode = nodeList.item(k);
						break;
					}
				}
				transformer.transform(new DOMSource(urlListNode),
						new StreamResult(stringWriter));

				// XMLBeans parsing for validation (exception is thrown on
				// failure)
				LOGGER.info("String writer contains : "
						+ stringWriter.toString());
				XmlOptions options = new XmlOptions();
				URLListDocument urlDom = URLListDocument.Factory.parse(
						stringWriter.toString(), options);
				// Namespace-non-aware document used as workaround for the
				// extension bug (xpath does not include namespace)
				// -> xpath.compile("/URLList/url/text()"); int m = ((Double)
				String sampleString = stringWriter.toString();
				InputSource is = new InputSource(new StringReader(sampleString));
				DocumentBuilderFactory dbf = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document inputXML = db.parse(is);
				LOGGER.info("Complex Document Added: "
						+ inputs[i].getIdentifier().getStringValue() + " - "
						+ getStringFromDocument(inputXML));
				complexInputs.put(inputs[i].getIdentifier().getStringValue(),
						inputXML);
			}
		}
		if (literalInputs.size() > 0) {
			try {
				jsa.substituteSimpleInputs(jd, literalInputs);
			} catch (Exception e) {
				throw new ExceptionReport(e.getMessage(),
						ExceptionReport.INVALID_PARAMETER_VALUE, e);
			}
		}
		// Once JobDescription ready to run, create the jobs, run them, and wait
		// for them.
		// N.B. In this case, the "extension" provides a new class to handle an
		// array of jobs; they are still accessible individually from the
		// attributes
		// System.out.println("Creating job in "
		// + jd.getAttribute("NUMBEROFPROCESSES") + " instances");
		if (((com.terradue.ogf.saga.impl.job.JobDescriptionImpl) jd)
				.getAttribute("NumberOfProcesses") != null) {
			LOGGER.info("Creating job in "
					+ ((com.terradue.ogf.saga.impl.job.JobDescriptionImpl) jd)
							.getAttribute("NumberOfProcesses") + " instances");
		} else {
			LOGGER.info("NumberOfProcesses attribute is null");
		}
		JobImpl jobs = null;
		if (numberOfTasks == 0) {
			jobs = ((JobServiceImpl) js).createJob(jd);
		} else {
			jobs = ((JobServiceImpl) js).createJob(jd, numberOfTasks);
		}
		// create now the job execute dirs
		String inputsDir = jobs.getSubstitutedVariable("WPS_JOB_INPUTS_DIR");
		String outputsDir = jobs.getSubstitutedVariable("WPS_JOB_OUTPUTS_DIR");
		String auditsDir = jobs.getSubstitutedVariable("WPS_JOB_AUDITS_DIR");
		String resultsDir = jobs.getSubstitutedVariable("WPS_JOB_RESULTS_DIR");
		LOGGER.info("Creating inputsDir:" + inputsDir);
		(new File(inputsDir)).mkdirs();
		(new File(outputsDir)).mkdirs();
		(new File(auditsDir)).mkdirs();
		(new File(resultsDir)).mkdirs();
		(new File(resultsDir)).setWritable(true, false);
		if (complexInputs.size() > 0) {
			try {
				jsa.writeComplexInputs(jobs, complexInputs);
			} catch (Exception e) {
				throw new ExceptionReport(e.getMessage(),
						ExceptionReport.INVALID_PARAMETER_VALUE, e);
			}
		}
		// Callbacks
		jobs.addCallback(Job.JOB_STATE, new SagaCallbackManager());
		jobs.addCallback(Job.JOB_STATEDETAIL, new SagaCallbackManager());
		LOGGER.info("Running job...");
		setRunningJob(jobs);
		jobs.run();
		LOGGER.info("saga job id:" + jobs.getId());
		setIID(jobs.getId());
		LOGGER.info("jobs.run() returned " + jobs.getState().getValue());
		// wait for all jobs in the job array
		LOGGER.info("Waiting for...");
		jobs.waitFor();
		if (jobs.isCancelled() || cancelHack) {
			LOGGER.info("Force cancel if interruption not successful");
			throw new CancellationException();
		}
		// Check if any exitMessage is non null
		boolean exitFault = false;
		String[][] exitMessages;
		try {
			exitMessages = jsa.readExitMessages(jobs);
			for (String[] exitMessage : exitMessages) {
				if (!exitMessage[0].trim().equals("0")) {
					exitFault = true;
					LOGGER.info("exitFault is true: " + exitMessage[0] + "-"
							+ exitMessage[1]);
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new ExceptionReport(
					"No exitMessage file is present: check the Grid infrastructure or the application script file.",
					ExceptionReport.REMOTE_COMPUTATION_ERROR);
		}
		LOGGER.info("Job state:" + jobs.getState());
		if (jobs.getState() == State.DONE & !exitFault) {
			LOGGER.info("Jobs completed");
			OutputDescriptionType[] outputsDesc = this.parentRepository
					.getProcessDescription(algorithmID).getProcessOutputs()
					.getOutputArray();
			Vector<String> literalOutputNames = new Vector<String>();
			Vector<String> complexOutputNames = new Vector<String>();
			// TODO other case ? and check valid ?
			LOGGER.info("Building outputs");
			if (doc.getExecute().getResponseForm() != null) {
				DocumentOutputDefinitionType[] outputs = doc.getExecute()
						.getResponseForm().getResponseDocument()
						.getOutputArray();
				for (int i = 0; i < outputs.length; i++) {
					String id = outputs[i].getIdentifier().getStringValue();
					for (int j = 0; j < outputsDesc.length; j++) {
						if (outputsDesc[j].getIdentifier().getStringValue()
								.equals(id)) {
							if (outputsDesc[j].isSetLiteralOutput()) {
								literalOutputNames.add(id);
							}
							if (outputsDesc[j].isSetComplexOutput()) {
								complexOutputNames.add(id);
							}
						}
					}
				}
			} else {
				for (int j = 0; j < outputsDesc.length; j++) {

					if (outputsDesc[j].isSetLiteralOutput()) {
						literalOutputNames.add(outputsDesc[j].getIdentifier()
								.getStringValue());
						LOGGER.info("Literal output:"
								+ outputsDesc[j].getIdentifier()
										.getStringValue());
					}
					if (outputsDesc[j].isSetComplexOutput()) {
						complexOutputNames.add(outputsDesc[j].getIdentifier()
								.getStringValue());
						LOGGER.info("Complex Output:"
								+ outputsDesc[j].getIdentifier()
										.getStringValue());
					}
				}
			}

			// Preparing an ExecuteResponse document
			// (This is the fastest way to implement but probably not the right
			// way to go)
			execRepDom = ExecuteResponseDocument.Factory.newInstance();
			execRepDom.addNewExecuteResponse();
			execRepDom.getExecuteResponse().setLang("en");
			execRepDom.getExecuteResponse().addNewProcessInstanceIdentifier()
					.setInstanceId(req.getId());
			execRepDom.getExecuteResponse().addNewStatus()
					.setProcessSucceeded("success");
			execRepDom.getExecuteResponse().addNewProcessOutputs();
			ProcessOutputs outputsDom = execRepDom.getExecuteResponse()
					.getProcessOutputs();
			if (complexOutputNames.size() > 0) {
				Map<String, Document> complexOutputMap = jsa
						.readComplexOutputs(jobs, (String[]) complexOutputNames
								.toArray(new String[complexOutputNames.size()]));
				Iterator<Entry<String, Document>> complexOutputsIterator = complexOutputMap
						.entrySet().iterator();
				while (complexOutputsIterator.hasNext()) {
					Entry<String, Document> entry = complexOutputsIterator
							.next();
					LOGGER.info("Found Complex Output:" + entry.getKey()
							+ " - " + nodeToString(entry.getValue()));
					OutputDataType newOutput = outputsDom.addNewOutput();
					newOutput.addNewIdentifier().setStringValue(entry.getKey());
					newOutput
							.addNewData()
							.addNewComplexData()
							.set(URLListDocument.Factory
									.parse(nodeToString(entry.getValue())));
					LOGGER.info("test:"
							+ URLListDocument.Factory.parse(
									nodeToString(entry.getValue())).toString());
				}
			}
			if (literalOutputNames.size() > 0) {
				Iterator<Entry<String, String>> simpleOutputsIterator = jsa
						.readSimpleOutputs(jobs).entrySet().iterator();
				// build map
				while (simpleOutputsIterator.hasNext()) {
					Entry<String, String> entry = simpleOutputsIterator.next();
					OutputDataType newOutput = outputsDom.addNewOutput();
					newOutput.addNewIdentifier().setStringValue(entry.getKey());
					newOutput.addNewData().addNewLiteralData()
							.setStringValue(entry.getValue());
					LOGGER.info("Found Literal Output:" + entry.getKey()
							+ " - " + entry.getValue());
				}
			}
			LOGGER.info("Execution Output Document: " + outputsDom.toString());

			System.out.println("END");

		} else {
			LOGGER.info("Jobs failed, throwing exception");
			// TODO handle ExitMessages
			throw new ComplexExceptionReport(
					"Grid Jobs failed throwing exceptions",
					buildExceptionReportDom(exitMessages),
					ExceptionReport.REMOTE_COMPUTATION_ERROR);

		}

		return (Document) execRepDom.getDomNode();

	}

	private void setProcessInstanceID(String instanceID) {
		this.processInstanceID = instanceID;

	}

	private void setIID(String id) {
		this.IID = id;

	}

	public ExceptionReportDocument buildExceptionReportDom(
			String[][] exitMessages) {
		// Printing serivce Exception
		ExceptionReportDocument report = ExceptionReportDocument.Factory
				.newInstance();
		net.opengis.ows.x11.ExceptionReportDocument.ExceptionReport exceptionReport = report
				.addNewExceptionReport();
		for (int i = 0; i < exitMessages.length; i++) {
			String[] exitMessage = exitMessages[i];
			LOGGER.info("exitMessage:" + exitMessages[i]);
			ExceptionType ex = exceptionReport.addNewException();
			ex.setExceptionCode(exitMessage[0]);
			ex.setLocator("Task" + (i + 1));
			ex.addExceptionText(exitMessage[1]);
		}
		// Adding additional Java exception
		return report;
	}

	public Collection<String> getAllProcesses() {
		LOGGER.info("TODO should not be reached yet");
		return null;
	}

	public boolean containsProcess(String processID) throws Exception {
		boolean containsProcess = false;
		// need to filter out the namespace if it is passed in.
		if (processID.contains("}"))
			processID = processID.split("}")[1];

		// return getAllProcesses().contains(processID);
		return containsProcess;
		// throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean unDeployProcess(UndeployProcessRequest request)
			throws Exception {
		// unDeployProcess(String processID) is implemented though...
		LOGGER.info("Saga undeploying...");
		return unDeployProcess((String) request.getProcessID());
		// return false;
		// throw new UnsupportedOperationException("Not supported yet.");
	}

	/**
	 * private OMElement sendToPM(OMElement msg) throws AxisFault { return
	 * _client.send(msg, this.processManagerEndpoint); // return
	 * _PMclient.send(msg, this.processManagerEndpoint,10000); }
	 * 
	 * private OMElement sendToDeployment(OMElement msg) throws AxisFault {
	 * return _client.send(msg, this.deploymentEndpoint);
	 * 
	 * // return _DEPclient.send(msg,this.deploymentEndpoint,10000); }
	 */
	private ByteArrayOutputStream writeXMLToStream(Source source)
			throws TransformerException {
		// Prepare the DOM document for writing

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		// Prepare the output file

		Result result = new StreamResult(out);
		// System.etProperty("javax.xml.transform.TransformerFactory","com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
		// Write the DOM document to the file
		TransformerFactory x = TransformerFactory.newInstance();
		Transformer xformer = x.newTransformer();
		xformer.transform(source, result);

		return out;
	}

	private String nodeToString(Node node)
			throws TransformerFactoryConfigurationError, TransformerException {
		StringWriter stringWriter = new StringWriter();
		Transformer transformer = TransformerFactory.newInstance()
				.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.transform(new DOMSource(node), new StreamResult(
				stringWriter));

		return stringWriter.toString();
	}

	public SOAPEnvelope createSOAPEnvelope(Node domNode) {
		SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
		SOAPEnvelope envelope = fac.getDefaultEnvelope();

		NamespaceContext ctx = new NamespaceContext() {

			public String getNamespaceURI(String prefix) {
				String uri;
				if (prefix.equals("wps"))
					uri = "http://www.opengis.net/wps/1.0.0";
				else if (prefix.equals("ows"))
					uri = "http://www.opengis.net/ows/1.1";
				else
					uri = null;
				return uri;
			}

			public String getPrefix(String namespaceURI) {
				return null;
			}

			public Iterator getPrefixes(String namespaceURI) {
				return null;
			}
		};

		XPathFactory xpathFact = XPathFactory.newInstance();
		XPath xpath = xpathFact.newXPath();
		xpath.setNamespaceContext(ctx);

		String identifier = null;
		String input = null;
		String xpathidentifier = "//ows:Identifier";
		String xpathinput = "//wps:DataInputs/wps:Input/wps:Data/wps:LiteralData";

		try {
			identifier = xpath.evaluate(xpathidentifier, domNode);
			input = xpath.evaluate(xpathinput, domNode);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// OMNamespace wpsNs =
		// fac.createOMNamespace("http://scenz.lcr.co.nz/wpsHelloWorld", "wps");
		OMNamespace wpsNs = fac.createOMNamespace("http://scenz.lcr.co.nz/"
				+ identifier, "wps");
		// creating the payload

		// TODO: parse the domNode to a request doc
		// OMElement method = fac.createOMElement("wpsHelloWorldRequest",
		// wpsNs);
		OMElement method = fac.createOMElement(identifier + "Request", wpsNs);
		OMElement value = fac.createOMElement("input", wpsNs);
		// value.setText("Niels");
		value.setText(input);
		method.addChild(value);
		envelope.getBody().addChild(method);
		return envelope;
	}

	@SuppressWarnings("unchecked")
	private SOAPEnvelope createSOAPEnvelope(ExecuteDocument execDoc) {

		SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
		SOAPEnvelope envelope = fac.getDefaultEnvelope();

		new NamespaceContext() {

			public String getNamespaceURI(String prefix) {
				String uri;
				if (prefix.equals("wps"))
					uri = "http://www.opengis.net/wps/1.0.0";
				else if (prefix.equals("ows"))
					uri = "http://www.opengis.net/ows/1.1";
				else
					uri = null;
				return uri;
			}

			public String getPrefix(String namespaceURI) {
				return null;
			}

			public Iterator getPrefixes(String namespaceURI) {
				return null;
			}
		};

		// _client = new ODEServiceClient();
		HashMap<String, String> allProcesses = new HashMap<String, String>();

		// OMElement listRoot = _client.buildMessage("listAllProcesses",
		// new String[] {}, new String[] {});

		OMElement result = null;
		/**
		 * try { //result = sendToPM(listRoot); } catch (AxisFault e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 */
		Iterator<OMElement> pi = result.getFirstElement().getChildrenWithName(
				new QName("http://www.apache.org/ode/pmapi/types/2006/08/02/",
						"process-info"));

		while (pi.hasNext()) {
			OMElement omPID = pi.next();

			String fullName = omPID
					.getFirstChildWithName(
							new QName(
									"http://www.apache.org/ode/pmapi/types/2006/08/02/",
									"pid")).getText();
			allProcesses.put(
					fullName.substring(fullName.indexOf("}") + 1,
							fullName.indexOf("-")),
					fullName.substring(1, fullName.indexOf("}")));

		}

		String identifier = execDoc.getExecute().getIdentifier()
				.getStringValue();

		OMNamespace wpsNs = null;

		for (String string : allProcesses.keySet()) {

			if (string.equals(identifier)) {
				wpsNs = fac.createOMNamespace(allProcesses.get(string), "nas");
				break;
			}

		}
		// creating the payload

		// TODO: parse the domNode to a request doc
		// OMElement method = fac.createOMElement("wpsHelloWorldRequest",
		// wpsNs);
		OMElement method = fac.createOMElement(identifier + "Request", wpsNs);
		envelope.getBody().addChild(method);

		DataInputsType datainputs = execDoc.getExecute().getDataInputs();

		for (InputType input1 : datainputs.getInputArray()) {

			String inputIdentifier = input1.getIdentifier().getStringValue();
			OMElement value = fac.createOMElement(inputIdentifier, "", "");
			if (input1.getData() != null
					&& input1.getData().getLiteralData() != null) {
				value.setText(input1.getData().getLiteralData()
						.getStringValue());
			} else {
				// Node no =
				// input1.getData().getComplexData().getDomNode().getChildNodes().item(1);
				// value.setText("<![CDATA[" + nodeToString(no) + "]>");
				// value.addChild(no);
				OMElement reference = fac.createOMElement("Reference",
						"http://www.opengis.net/wps/1.0.0", "wps");
				OMNamespace xlin = fac.createOMNamespace(
						"http://www.w3.org/1999/xlink", "xlin");

				OMAttribute attr = fac.createOMAttribute("href", xlin, input1
						.getReference().getHref());
				reference.addAttribute(attr);
				reference.addAttribute("schema", input1.getReference()
						.getSchema(), fac.createOMNamespace("", ""));
				value.addChild(reference);
			}
			method.addChild(value);
		}

		return envelope;

	}

	public void setProcessesPrefix(String processesPrefix) {
		this.processesPrefix = processesPrefix;
	}

	public String getProcessesPrefix() {
		return processesPrefix;
	}

	/**
	 * Wait the asynchronousCallback
	 */
	public synchronized void waitCallback() {
		try {
			LOGGER.info("Waiting callback");
			wait();
			LOGGER.info("Callback received");
		} catch (Exception e) {
			System.out.println(e);
		}
		return;
	}

	public synchronized void notifyRequestManager() {
		notify();
	}

	public void setDeployProcessDir(String deployProcessDir) {
		deployProcessDir.replace('/', File.separatorChar);
		deployProcessDir.replace('\\', File.separatorChar);
		if (deployProcessDir.endsWith("/") || deployProcessDir.endsWith("\\")) {
			this.deployProcessDir = deployProcessDir;
		} else {
			this.deployProcessDir = deployProcessDir + File.separator;
		}
	}

	public String getDeployProcessDir() {
		return deployProcessDir;
	}

	public AuditTraceType getAudit() {
		LOGGER.info("short form apache get audit");
		AuditTraceType audit = null;
		URLListDocument auditURLS = URLListDocument.Factory.newInstance();
		File auditDir = new File(GridFilesDir + "execute" + File.separator
				+ processID + File.separator + getProcessInstanceID()
				+ File.separator + getIID() + File.separator + "audits"
				+ File.separator);

		LOGGER.info("--------------------------------------------------------------------");
		LOGGER.info("Audit dir: " + GridFilesDir + "execute" + File.separator
				+ processID + File.separator + getProcessInstanceID()
				+ File.separator + getIID() + File.separator + "audits"
				+ File.separator);
		String[] filenames = auditDir.list();
		auditURLS.addNewURLList().setCount(filenames.length);
		for (String filename : filenames) {
			auditURLS
					.getURLList()
					.addNewUrl()
					.setStringValue(
							getWPSPublicationPrefix() + "execute"
									+ File.separator + processID
									+ File.separator + getProcessInstanceID()
									+ File.separator + getIID()
									+ File.separator + "audits"
									+ File.separator + filename);
		}
		try {
			audit = AuditTraceType.Factory.parse(auditURLS.getDomNode());
		} catch (XmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		// LOGGER.info(audit.toString());
		return audit;

	}

	public AuditTraceType getAuditLongForm() {
		LOGGER.info("long form saga get audit");

		return getAudit();

	}

	@Override
	public String getIID() {
		// TODO Auto-generated method stub
		return this.IID;
	}

	public void setProcessID(String processID) {
		this.processID = processID;
	}

	public String getProcessID() {
		return processID;
	}

	/**
	 * TODO : re design the IProcessManager : instanceID is required for some
	 * backend. All request information should be available at backend. To be
	 * discussed...
	 */

	@Override
	public Document invoke(ExecuteDocument payload, String algorithmID)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void callback(ExecuteResponseDocument execRespDom) {
		// TODO Auto-generated method stub
		return;
	}

	public void setWPSPublicationPrefix(String wPSPublicationPrefix) {
		WPSPublicationPrefix = wPSPublicationPrefix;
	}

	public String getWPSPublicationPrefix() {
		return WPSPublicationPrefix;
	}

	public String getProcessInstanceID() {
		return processInstanceID;
	}

	public static void setSagaLibDir(String sagaLibDir) {
		SagaLibDir = sagaLibDir;
	}

	public static String getSagaLibDir() {
		return SagaLibDir;
	}

	public void cancel() {
		LOGGER.info("getrunning job cancel");
		this.cancelHack = true;
		this.getRunningJob().cancel(true);
	}

	public void setRunningJob(JobImpl runningJob) {
		this.runningJob = runningJob;
	}

	public JobImpl getRunningJob() {
		return runningJob;
	}

	public String getMyProxyURL() {
		return myProxyURL;
	}

	public void setMyProxyURL(String myProxyURL) {
		this.myProxyURL = myProxyURL;
	}

	public String getMyProxyUser() {
		return myProxyUser;
	}

	public void setMyProxyUser(String myProxyUser) {
		this.myProxyUser = myProxyUser;
	}

	public String getMyProxyPassword() {
		return myProxyPassword;
	}

	public void setMyProxyPassword(String myProxyPassword) {
		this.myProxyPassword = myProxyPassword;
	}

}
