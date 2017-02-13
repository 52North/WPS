package org.n52.wps.server.profiles.oozie;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.opengis.wps.x100.AuditTraceType;
import net.opengis.wps.x100.DocumentOutputDefinitionType;
import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.ExecuteResponseDocument.ExecuteResponse.ProcessOutputs;
import net.opengis.wps.x100.InputType;
import net.opengis.wps.x100.OutputDataType;
import net.opengis.wps.x100.OutputDescriptionType;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.oozie.client.OozieClient;
import org.apache.oozie.client.OozieClientException;
import org.apache.oozie.client.WorkflowAction;
import org.apache.oozie.client.WorkflowJob;
import org.apache.oozie.client.WorkflowJob.Status;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlString;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.profiles.AbstractProcessManager;
import org.n52.wps.server.repository.ITransactionalAlgorithmRepository;
import org.n52.wps.server.request.DeployProcessRequest;
import org.n52.wps.server.request.ExecuteRequest;
import org.n52.wps.server.request.UndeployProcessRequest;
import org.n52.wps.server.request.deploy.DeploymentProfile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xint.esa.ese.wps.format.urlList.URLListDocument;
import xint.esa.ese.wps.format.urlList.URLListDocument.URLList;

/**
 * TODO this class was based on transactional branch implementation. However the
 * invoke method was reimplemented Therefore there is a doublon implementation
 * for sending request. *
 * 
 * @author cnl
 * 
 */
public class OozieManager extends AbstractProcessManager {
	// TODO rename to Oozie instead ozzie
	// logger for debug purpose
	private final Logger log = Logger.getLogger(getClass());
	private String oozieUrl;
	private String deployDirectory;
	private String hdfsUrl;

	// the WPS instance ID (not expected to be the same)
	private String taskId;
	// the oozie instance ID
	private String jobId;
	// Client for Oozie (oozie jar)
	private OozieClient client;
	// HDFS manager (for file system operations)
	private HDFSClient hdfsManager;
	private String jobTrackerUrl;
	private String hadoopDefaultUser;

	/**
	 * Singleton for the Oozie client
	 * 
	 * @return
	 */
	private OozieClient getClient() {
		if (this.client == null) {
			this.client = new OozieClient(this.getOozieUrl());
		}
		return this.client;
	}

	
	public String getPriority(ArrayList<SOAPHeaderBlock> priority) {
		String cnName = null;
		try {
			for (SOAPHeaderBlock samlHeader : priority) {
				OMElement priority1 = (OMElement) samlHeader
						.getChildrenWithLocalName("Priority").next();
				
				cnName = priority1.getText();
				log.debug("discovered CNAME is "+cnName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (StringUtils.isBlank(cnName)) {
			return null;
		}
		return cnName;
	}
	
	
	public String getSAMLUserName(ArrayList<SOAPHeaderBlock> saml) {
		String cnName = null;
		try {
			for (SOAPHeaderBlock samlHeader : saml) {
				OMElement assertion = (OMElement) samlHeader
						.getChildrenWithLocalName("Assertion").next();
				OMElement attributeStatement = (OMElement) assertion
						.getChildrenWithLocalName("AttributeStatement").next();
				OMElement subject = (OMElement) attributeStatement
						.getChildrenWithLocalName("Subject").next();
				OMElement nameIdentifier = (OMElement) subject
						.getChildrenWithLocalName("NameIdentifier").next();
				cnName = nameIdentifier.getText();
			}
		} catch (Exception e) {
		}
		if (StringUtils.isBlank(cnName)) {
			return this.getHadoopDefaultUser();
		}
		return cnName;
	}

	/**
	 * Contructor
	 * 
	 * @param parentRepository
	 */
	public OozieManager(ITransactionalAlgorithmRepository parentRepository) {
		super(parentRepository);
		// Retrieve repository properties from wps_config.xml (WEB-INF/config)
		Property[] properties = WPSConfig.getInstance()
				.getPropertiesForRepositoryName("OozieRepository");
		// Get the Oozie URL property
		this.setOozieUrl(getPropertyUtil("Oozie_URL", properties));
		this.setDeployDirectory(getPropertyUtil("HDFS_Deploy_Directory",
				properties));
		this.hdfsUrl = getPropertyUtil("HDFS_Local_URL", properties);
		this.jobTrackerUrl = getPropertyUtil("JobTracker_URL", properties);
		// Todo include the configuration files
		this.setHdfsManager(new HDFSClient(getPropertyUtil("HDFS_Remote_URL",
				properties), getPropertyUtil("HDFS_Admin_User", properties)));
		this.setHadoopDefaultUser(getPropertyUtil("Hadoop_Default_User",
				properties));
	}

	/**
	 * undeploy a process
	 */
	@Override
	public boolean unDeployProcess(UndeployProcessRequest request)
			throws Exception {
		return unstoreArchive(request.getProcessID());
	}

	@Override
	public boolean containsProcess(String processID) throws Exception {
		// Not used, inherited historically
		return false;
	}

	@Override
	public Collection<String> getAllProcesses() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Document invoke(ExecuteDocument payload, String algorithmID)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Document invoke(ExecuteRequest request, String algorithmID)
			throws Exception {
		Properties outputProps = null;
		Properties conf = this.getClient().createConfiguration();
		// Get the user from the SOAP header
		// String user = getUser(request);
		// TODO replace user by saml token value
		String user = this.getSAMLUserName(request.getSamlHeader());
		// Get the WPS process instance id (task id)
		String taskId = request.getId();
		// set the username
		conf.setProperty("user.name", user);
		log.debug("has set the user name: " + user);
		// create the user home directory if necessary
		try {
		this.getHdfsManager().createHomeDirectoryIfNeeded(user);
		}
		catch(Exception e) {
			// create Home directory error.
		}
		String priority = this.getPriority(request.getQOSHeaderBlocks());
		if(priority != null) {
		 conf.setProperty("priority",priority);
		 log.debug("priority property set  :"+priority);
		}
		// set the namenode and jobtracker URLs
		conf.setProperty("jobTracker", this.getJobTrackerUrl());
		// do not change the IP of the jobTracker and the nameNode
		conf.setProperty("nameNode", this.getHdfsUrl());
		///conf.setProperty("jobTracker", this.getJobTrackerUrl());
		// set the path to the workflow that is deployed on HDFS
		conf.setProperty(
				"oozie.wf.application.path",
				"${nameNode}" + this.getDeployDirectory()
						+ request.getAlgorithmIdentifier() + "/workflow.xml");
		// String inputDir = "/user/" + user + "/results/" + taskId + "/inputs";
		String outputDir = "/user/" + user + "/results/" + taskId + "/ouputs";
		// this.getHdfsManager().mkdir(inputDir);
		// this.getHdfsManager().mkdir(outputDir);
		// conf.setProperty("inputDir", "${nameNode}/user/" + user + "/eodata");
		// conf.setProperty("outputDir", "${nameNode}/user/" + user +
		// "/results");
		// conf.setProperty("inputDir", "${nameNode}" + inputDir);
		// conf.setProperty("outputDir", "${nameNode}" + outputDir);

		InputType[] inputs = request.getExecute().getDataInputs()
				.getInputArray();

		for (InputType input : inputs) {
			if (input.getData().isSetLiteralData()) {
				String identifier = input.getIdentifier().getStringValue();
				String value = input.getData().getLiteralData()
						.getStringValue();
				conf.setProperty(identifier, value);
			}
			if (input.getData().isSetComplexData()) {
				log.debug("INPUT:" + input.toString());
				URLListDocument doc = URLListDocument.Factory.parse(input
						.getData().getComplexData().newInputStream());
				StringBuffer sb = new StringBuffer();
				for (String url : doc.getURLList().getUrlArray()) {
					if (sb.length() > 0) {
						sb.append(",");
					}
					sb.append(url);
				}
				String identifier = input.getIdentifier().getStringValue();
				conf.setProperty(identifier, sb.toString());
			}
			if (input.getData().isSetBoundingBoxData()) {
				log.debug("INPUT:" + input.toString());
				List<Double> upCorner = (List<Double>)input.getData().getBoundingBoxData().getUpperCorner();
				List<Double> lowCorner = (List<Double>) input.getData().getBoundingBoxData().getLowerCorner();
				String identifier = input.getIdentifier().getStringValue();
				conf.setProperty(identifier+"_tlx", lowCorner.get(1).toString());
				conf.setProperty(identifier+"_tly", upCorner.get(0).toString());
				conf.setProperty(identifier+"_brx", upCorner.get(1).toString());
				conf.setProperty(identifier+"_bry", lowCorner.get(0).toString());
			}
		}
		try {
			this.jobId = this.getClient().run(conf);
			System.out.println("Job ID: " + this.jobId);
			WorkflowJob workflowJob = this.getClient().getJobInfo(this.jobId);
			Status status = workflowJob.getStatus();
			int sleepStrategy = 150;
			while (status != WorkflowJob.Status.SUCCEEDED
					&& status != WorkflowJob.Status.FAILED
					&& status != WorkflowJob.Status.KILLED) {
				Thread.sleep(sleepStrategy);
				log.debug("sleeping for " + sleepStrategy + " ms");
				if (sleepStrategy < 3000)
					sleepStrategy = sleepStrategy * 2;
				workflowJob = this.getClient().getJobInfo(this.jobId);
				status = workflowJob.getStatus();

			}
			if (status == WorkflowJob.Status.SUCCEEDED) {
				log.debug("publishing");
				for (WorkflowAction action : workflowJob.getActions()) {
					if (action.getName().equals("Publish")) {
						System.out.println("Data: " + action.getData());
						outputProps = new Properties();
						outputProps.load(new StringReader(action.getData()));
						log.debug(outputProps.toString());
					}
				}
			} else {
				if (status == WorkflowJob.Status.KILLED) {
					String errorMessage = "Killed Process - Failed Actions:";

					for (WorkflowAction action : workflowJob.getActions()) {
						if (action.getStatus() == WorkflowAction.Status.ERROR) {
							WorkflowAction realAction;
							if(action.getErrorMessage()==null){
								realAction = guessSubWorkflowErrorMessage(action.getExternalId());
							}
								else {
									realAction= action;
								}
							errorMessage = errorMessage.concat("\n"
									+ realAction.getName() + "(id:"
									+ realAction.getId() + ") - status:"
									+ realAction.getStatus().toString()
									+ " -start time:"
									+ realAction.getStartTime().toGMTString()
									+ " - error code:" + realAction.getErrorCode()
									+ " - message:" + realAction.getErrorMessage());
							}
							
						}
					
					ExceptionReport exReport = new ExceptionReport(
							errorMessage, ExceptionReport.NO_APPLICABLE_CODE);
					log.debug("killed");
					exReport.printStackTrace();
					throw exReport;

				}
				if (status == WorkflowJob.Status.FAILED) {
					String errorMessage = "Failed Process - Failed Actions:";

					for (WorkflowAction action : workflowJob.getActions()) {
						if (action.getStatus() == WorkflowAction.Status.ERROR) {
							WorkflowAction realAction;
							if(action.getErrorMessage()==null){
								realAction = guessSubWorkflowErrorMessage(action.getExternalId());
							}
								else {
									realAction= action;
								}
							errorMessage = errorMessage.concat("\n"
									+ realAction.getName() + "(id:"
									+ realAction.getId() + ") - status:"
									+ realAction.getStatus().toString()
									+ " -start time:"
									+ realAction.getStartTime().toGMTString()
									+ " - error code:" + realAction.getErrorCode()
									+ " - message:" + realAction.getErrorMessage());
							}
							
						}
					ExceptionReport exReport = new ExceptionReport(
							errorMessage, ExceptionReport.NO_APPLICABLE_CODE);
					log.debug("killed");
					exReport.printStackTrace();
					throw exReport;

				}
				log.debug("untrowned exception");
				throw new ExceptionReport("Oozie - Not successful job",
						ExceptionReport.NO_APPLICABLE_CODE);
			}
		}

		catch (OozieClientException ex) {
			ex.printStackTrace();
			throw new ExceptionReport("Oozie client exception", "1", "none");
		}
		ExecuteResponseDocument execRepDom = ExecuteResponseDocument.Factory
				.newInstance();
		execRepDom.addNewExecuteResponse();
		execRepDom.getExecuteResponse().setLang("en");
		execRepDom.getExecuteResponse().addNewProcessInstanceIdentifier()
				.setInstanceId(taskId);
		execRepDom.getExecuteResponse().addNewStatus()
				.setProcessSucceeded("success");
		execRepDom.getExecuteResponse().addNewProcessOutputs();
		execRepDom.getExecuteResponse().addNewProcessOutputs();
		ProcessOutputs outputsDom = execRepDom.getExecuteResponse()
				.getProcessOutputs();

		for (DocumentOutputDefinitionType outputEntry : request.getExecute()
				.getResponseForm().getResponseDocument().getOutputArray()) {
			String outputName = outputEntry.getIdentifier().getStringValue();
			String value = outputProps.getProperty(outputName);
			OutputDataType output = outputsDom.addNewOutput();
			output.addNewIdentifier().setStringValue(outputName);
			OutputDescriptionType desc = getOutputDescription(
					request.getAlgorithmIdentifier(), outputName);
			if (desc != null && desc.isSetComplexOutput()) {
				output.addNewData().addNewComplexData()
						.set(this.createURLList(value));
			} else {
				output.addNewData().addNewLiteralData().setStringValue(value);
			}
		}

		log.debug(execRepDom.toString());
		// ProcessOutputs outputsDom = execRepDom.getExecuteResponse()
		return (Document) execRepDom.getDomNode();
	}

	private URLListDocument createURLList(String urls) {
		URLListDocument urlListDocument = URLListDocument.Factory.newInstance();
		URLList list = urlListDocument.addNewURLList();
		StringTokenizer st = new StringTokenizer(urls, ",");
		while (st.hasMoreTokens()) {
			list.addNewUrl().setStringValue(st.nextToken());
		}
		return urlListDocument;
	}

	private WorkflowAction guessSubWorkflowErrorMessage(String oozieSubWorkflowId) throws OozieClientException {

		WorkflowJob workflowJob = this.getClient().getJobInfo(
				oozieSubWorkflowId);
		List<WorkflowAction> workflowActions = workflowJob.getActions();
		for (WorkflowAction workflowAction : workflowActions) {
			if (workflowAction.getErrorMessage() != null) {
				return workflowAction;
			} else if ("sub-workflow".equals(workflowAction.getType())) {
				return guessSubWorkflowErrorMessage(workflowAction
						.getExternalId());
			}
		}
		return null;
	}

	public OutputDescriptionType getOutputDescription(String algo,
			String identifier) {
		OutputDescriptionType[] outputsDesc = this.parentRepository
				.getProcessDescription(algo).getProcessOutputs()
				.getOutputArray();
		for (OutputDescriptionType desc : outputsDesc) {
			if (desc.getIdentifier().getStringValue()
					.equalsIgnoreCase(identifier)) {
				return desc;
			}
		}
		return null;
	}

	@Override
	public boolean deployProcess(DeployProcessRequest request) throws Exception {
		// Get the the deployment profile from the request
		DeploymentProfile profile = request.getDeploymentProfile();
		if (!(profile instanceof OozieDeploymentProfile)) {
			throw new Exception("Requested Deployment Profile not supported");
		}
		OozieDeploymentProfile deploymentProfile = (OozieDeploymentProfile) profile;
		String processID = deploymentProfile.getProcessID();
		// Store the archive from the request
		storeArchive(deploymentProfile.getArchive(), processID);
		log.debug("Stored Archive:" + deploymentProfile.getArchive());
		return true;
	}

	/**
	 * Unzip a collection of files contained in a archive zip file and write
	 * onto hdfs
	 * 
	 * @param archive
	 * @param processID
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	private void storeArchive(byte[] archive, String processID)
			throws IOException, InterruptedException {
		// Deployment directory (definition)
		String archiveDir = getProcessDeploymentDirectory(processID);
		// Creation of deployment directory
		log.info("Creating directory " + archiveDir);
		this.getHdfsManager().mkdir(archiveDir);
		log.info("Storing archive in " + archiveDir);
		// Read the zip archive
		ByteArrayInputStream bais = new ByteArrayInputStream(archive);
		byte[] buf = new byte[1024];
		ZipInputStream zis = new ZipInputStream(bais);
		ZipEntry zipentry = null;
		// For each zip entry (file), create the directory, then write the file
		while ((zipentry = zis.getNextEntry()) != null) {
			String entryName = zipentry.getName();
			if (zipentry.isDirectory()) {
				log.info("Create dir " + entryName);
				this.getHdfsManager().mkdir(archiveDir + entryName);
			} else {
				log.info("Writing file " + entryName);
				this.getHdfsManager().writeFile(zis, archiveDir + entryName);
			}
			zis.closeEntry();

		}

		zis.close();

	}

	/**
	 * Unstore Archive
	 * 
	 * @param processId
	 * @return
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	private boolean unstoreArchive(String processId) throws IOException, InterruptedException {
		String archiveDir = getProcessDeploymentDirectory(processId);
		this.getHdfsManager().deleteFile(archiveDir);
		;

		return true;
	}

	/**
	 * Deletes all files and subdirectories under dir. Returns true if all
	 * deletions were successful. If a deletion fails, the method stops
	 * attempting to delete and returns false.
	 * 
	 * @param dir
	 * @return
	 */
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
	 * Get the deployment directory of a process
	 * 
	 * @param processID
	 * @return
	 */
	private String getProcessDeploymentDirectory(String processID) {
		return this.getDeployDirectory() + processID + File.separator;
	}

	@Override
	public AuditTraceType getAuditLongForm() throws Exception {
		log.debug("long form");
		if (this.getJobId() == null) {
			throw new RuntimeException(
					"Error. WPS service has not created the Oozie job yet.");
		}
		// Get the job log
		String jobLog = this.getClient().getJobLog(jobId);
		System.out.print("JobLog:"+jobLog);
		
		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();
		// Create a "Log" root element
		Element rootElement = doc.createElement("Audit");
		// Set the log as text content
		rootElement.setTextContent(jobLog);
		
		//doc.appendChild(rootElement);
		// Parse the Audit Trace document (any el / any att type)
		//AuditTraceType audit = AuditTraceType.Factory.parse(jobLog);
		AuditTraceType audit = AuditTraceType.Factory.parse(rootElement);
		return audit;
	}
	
	public static void main(String[] args) throws XmlException, ParserConfigurationException {
		 XmlString xmlString =XmlString.Factory.newInstance();
		 xmlString.setStringValue("1");
		 AuditTraceType audit = AuditTraceType.Factory.parse(xmlString.getDomNode());
		audit.setShortForm(true);
		
		//audit.set(xmlString);
		 
		/**
		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();
		// Create a "Log" root element
		Element rootElement = doc.createElement("Audit");
		// Set the log as text content
		rootElement.setTextContent(test);
		
		//doc.appendChild(rootElement);
		//AuditTraceType audit = AuditTraceType.Factory.parse(rootElement);
		AuditTraceType audit = AuditTraceType.Factory.newInstance();
		*/
		XmlString x = XmlString.Factory.parse(audit.getDomNode());
		
		System.out.println(x.getStringValue());
		
	}

	@Override
	public AuditTraceType getAudit() throws Exception {
		// TODO Auto-generated method stub
		 XmlString xmlString =XmlString.Factory.newInstance();
		 xmlString.setStringValue(this.jobId);
		 AuditTraceType audit = AuditTraceType.Factory.parse(xmlString.getDomNode());
		audit.setShortForm(true);
return audit;
		 
	}

	@Override
	public String getIID() {
		return this.getTaskId();
	}

	@Override
	public void cancel() {
		try {
			if (StringUtils.isNotBlank(this.jobId)) {
				this.getClient().kill(this.jobId);
			}
		} catch (OozieClientException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Utility method that should better be implemented in WPSConfig class Get
	 * the property string value and throws exception if null
	 */

	private String getPropertyUtil(String property, Property[] properties) {
		Property prop = WPSConfig.getInstance().getPropertyForKey(properties,
				property);
		if (prop == null) {
			throw new RuntimeException("Error. Could not find " + property);
		}
		return prop.getStringValue();

	}

	public String getOozieUrl() {
		return oozieUrl;
	}

	public void setOozieUrl(String oozieUrl) {
		this.oozieUrl = oozieUrl;
	}

	public String getDeployDirectory() {
		if (!deployDirectory.endsWith(File.separator)) {
			deployDirectory = deployDirectory.concat(File.separator);
		}
		return deployDirectory;
	}

	public void setDeployDirectory(String deployDirectory) {
		this.deployDirectory = deployDirectory;
	}

	private String getUser(ExecuteRequest req) {
		String cnName = null;
		try {

			for (SOAPHeaderBlock samlHeader : req.getSamlHeader()) {
				log.info("LocalName = " + samlHeader.getLocalName());
				OMElement assertion = (OMElement) samlHeader
						.getChildrenWithLocalName("Assertion").next();
				log.info("Assertion found:" + assertion.toString());
				OMElement attributeStatement = (OMElement) assertion
						.getChildrenWithLocalName("AttributeStatement").next();
				log.info("AttributeStatement found");
				OMElement subject = (OMElement) attributeStatement
						.getChildrenWithLocalName("Subject").next();
				log.info("Subject found:" + subject.toString());
				OMElement nameIdentifier = (OMElement) subject
						.getChildrenWithLocalName("NameIdentifier").next();
				log.info("Name Id found" + nameIdentifier.toString());
				cnName = nameIdentifier.getText();
				log.info(cnName);
			}

		} catch (Exception e) {
		}

		return cnName;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public HDFSClient getHdfsManager() {
		return hdfsManager;
	}

	public void setHdfsManager(HDFSClient hdfsManager) {
		this.hdfsManager = hdfsManager;
	}

	public String getJobTrackerUrl() {
		return jobTrackerUrl;
	}

	public void setJobTrackerUrl(String jobTrackerUrl) {
		this.jobTrackerUrl = jobTrackerUrl;
	}

	public static void main2(String[] args) {
		OozieClient xclient = new OozieClient(
				"http://192.168.56.101:11000/oozie/");
		Properties conf = xclient.createConfiguration();

		// set the username
		// conf.setProperty("user.name", "oozie");
		conf.setProperty("user.name", "christophe");
		// set the namenode and jobtracker URLs
	
		// do not change the IP of the jobTracker and the nameNode
		conf.setProperty("nameNode", "hdfs://localhost.localdomain:8020");
		conf.setProperty("jobTracker", "localhost.localdomain:8021");

		// set the path to the workflow that is deployed on HDFS
		conf.setProperty("oozie.wf.application.path",
				"${nameNode}/user/christophe/workflows/parent/workflow.xml");
		// set additional parameters for the Oozie workflow
		conf.setProperty("roiX", "4000");
		conf.setProperty("roiY", "4000");
		conf.setProperty("roiW", "1000");
		conf.setProperty("roiH", "1000");
		conf.setProperty("byteDepth", "1");
		conf.setProperty("inputDir", "${nameNode}/user/christophe/eodata");
		conf.setProperty("outputDir", "${nameNode}/user/christophe/results");
		try {
			String jobId = xclient.run(conf);
			System.out.println("Workflow job submitted : " + jobId);
			WorkflowJob workflowJob = xclient.getJobInfo(jobId);
			Status status = workflowJob.getStatus();
			while (status != WorkflowJob.Status.SUCCEEDED) {
				String jobLog = xclient.getJobLog(jobId);
				System.out.print("JobLog:");
				System.out.println(jobLog);

				Thread.sleep(5000);
				workflowJob = xclient.getJobInfo(jobId);
				status = workflowJob.getStatus();

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public String getHdfsUrl() {
		return hdfsUrl;
	}

	public void setHdfsUrl(String hdfsUrl) {
		this.hdfsUrl = hdfsUrl;
	}

	public String getHadoopDefaultUser() {
		return hadoopDefaultUser;
	}

	public void setHadoopDefaultUser(String hadoopDefaultUser) {
		this.hadoopDefaultUser = hadoopDefaultUser;
	}
}
