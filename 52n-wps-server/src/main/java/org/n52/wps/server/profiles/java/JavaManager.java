package org.n52.wps.server.profiles.java;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.opengis.wps.x100.AuditTraceType;
import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.InputType;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.log4j.Logger;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.data.IData;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.profiles.AbstractProcessManager;
import org.n52.wps.server.repository.ITransactionalAlgorithmRepository;
import org.n52.wps.server.request.DeployProcessRequest;
import org.n52.wps.server.request.ExecuteRequest;
import org.n52.wps.server.request.InputHandler;
import org.n52.wps.server.request.UndeployProcessRequest;
import org.n52.wps.server.request.deploy.DeploymentProfile;
import org.n52.wps.util.StreamUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;




/**
 * TODO this class was based on transactional branch implementation. However the
 * invoke method was reimplemented Therefore there is a doublon implementation
 * for sending request. *
 * 
 * @author cnl
 * 
 */
public class JavaManager extends AbstractProcessManager {
	// logger for debug purpose
	private final Logger log = Logger.getLogger(getClass());
	private String deployDirectory;
	// the WPS instance ID (not expected to be the same)
	private String taskId;
	// the oozie instance ID
	private String jobId;
	// Client for Oozie (oozie jar)
	// HDFS manager (for file system operations)
	private String jobTrackerUrl;
	private JavaTransactionalAlgorithm algorithm;
	

	/**
	 * Contructor
	 * 
	 * @param parentRepository
	 */
	public JavaManager(ITransactionalAlgorithmRepository parentRepository) {
		super(parentRepository);
		// Retrieve repository properties from wps_config.xml (WEB-INF/config)
		Property[] properties = WPSConfig.getInstance()
				.getPropertiesForRepositoryName("JavaRepository");
		String fullPath = this.getClass()
				.getProtectionDomain().getCodeSource().getLocation().toString();
		int searchIndex = fullPath.indexOf("WEB-INF");
		String subPath = fullPath.substring(0, searchIndex);
		subPath = subPath.replaceFirst("file:", "");
		String path = subPath + "WEB-INF/JavaProcesses/";
		this.setDeployDirectory(path);
		// Todo include the configuration files
		
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

	
	public  Map<String, IData> invokeJava(ExecuteRequest request, String algorithmID)
			throws Exception {
		
		Properties outputProps = null;
		//String processPath = getProcessJar(algorithmID);
		// Get the user from the SOAP header
		// String user = getUser(request);
		// TODO replace user by saml token value
		String user = "christophe";
		// Get the WPS process instance id (task id)
		String taskId = request.getId();
		// set the username
		// set the namenode and jobtracker URLs
		// do not change the IP of the jobTracker and the nameNode
		String inputDir = "/user/" + user + File.separator + algorithmID + File.separator+ "/results/" + taskId + "/inputs";
		String outputDir = "/user/" + user + File.separator + algorithmID + File.separator + "/results/" + taskId + "/ouputs";
		List<URL> jarList = new ArrayList<URL>();
		// Jar file 
		String jarDir = getProcessDeploymentDirectory(algorithmID);
		// List all file in parent directory with jar suffix extension
		Collection<File> list = FileUtils.listFiles(new File(jarDir), FileFilterUtils.suffixFileFilter("jar"), null);
		for(File f : list) {
			URL jarURL = f.toURI().toURL();
			jarList.add(jarURL);
		}
		ClassLoader classLoader = new URLClassLoader(jarList.toArray(new URL[jarList.size()]), this.getClass().getClassLoader());
		
		//if(		be.spacebel.ese.data.wps.DownloadEOData)
		
		JavaTransactionalAlgorithm algorithm = (JavaTransactionalAlgorithm)classLoader.loadClass(algorithmID).newInstance();
		InputType[] inputs = new InputType[0];
		if (request.getExecute().getDataInputs() != null) {
			inputs = request.getExecute().getDataInputs().getInputArray();
		}
		InputHandler parser = new InputHandler(inputs,algorithmID);
		algorithm.setInstanceId(taskId);
		this.setAlgorithm(algorithm);
		Map<String, IData> outputs = algorithm.run(parser.getParsedInputData());
		return outputs;
		
		
		
	}

	@Override
	public boolean deployProcess(DeployProcessRequest request) throws Exception {
		// Get the the deployment profile from the request
		DeploymentProfile profile = request.getDeploymentProfile();
		if (!(profile instanceof JavaDeploymentProfile)) {
			throw new Exception("Requested Deployment Profile not supported");
		}
		JavaDeploymentProfile deploymentProfile = (JavaDeploymentProfile) profile;
		String processID = deploymentProfile.getProcessID();
		// Store the archive from the request
		writePackage(deploymentProfile.getArchive(), processID);
		log.debug("Stored Archive:" + deploymentProfile.getArchive());
		return true;
	}

	/**
	 * Write a jar file
	 * 
	 * @param archive
	 * @param processID
	 * @throws IOException
	 */
	private void writePackage(byte[] archive, String processID)
			throws IOException {
		// Deployment directory (definition)
		String archiveDir = getProcessDeploymentDirectory(processID);
		File directory = new File(archiveDir);
		if (!directory.exists()) {
			log.info("Creating directory " + archiveDir);
			directory.mkdirs();
		}
		ByteArrayInputStream bais = new ByteArrayInputStream(archive);
		byte[] buf = new byte[1024];
		ZipInputStream zis = new ZipInputStream(bais);
		ZipEntry zipentry = null;
		// For each zip entry (file), create the directory, then write the file
		while ((zipentry = zis.getNextEntry()) != null) {
			String entryName = zipentry.getName();
			if (zipentry.isDirectory()) {
				log.info("Create dir " + entryName);
				File entryFile = new File(archiveDir+entryName);
				if(!directory.exists())
					directory.mkdirs();
				//this.getHdfsManager().mkdir(archiveDir + entryName);
			} else {
				log.info("Writing file " + entryName);
				File entryFile = new File(archiveDir+entryName);
				byte[] byteArray = IOUtils.toByteArray(zis);
				// note: cannot use write Stream to file because it closes the input stream.
				FileUtils.writeByteArrayToFile(new File(archiveDir+entryName),byteArray);
			}
			zis.closeEntry();

		}

		zis.close();
		log.info("Stored archive in " + archiveDir);
	
	}

	/**
	 * Unstore Archive
	 * 
	 * @param processId
	 * @return
	 * @throws IOException
	 */
	private boolean unstoreArchive(String processId) throws IOException {
		String archiveDir = getProcessDeploymentDirectory(processId);
		FileUtils.deleteDirectory(new File(archiveDir));
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
	public String getProcessDeploymentDirectory(String processID) {
		return this.getDeployDirectory() + processID + File.separator;
	}

	
	
	@Override
	public AuditTraceType getAuditLongForm() throws Exception {
	log.debug("long form");
		
		// Get the job log
		System.out.print("JobLog:");
		// Create a new empty XML document
		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();
		// Create a "Log" root element
		Element rootElement = doc.createElement("Log");
		// Set the log as text content
		rootElement.setTextContent(this.getAlgorithm().getAudit());
		doc.appendChild(rootElement);
		// Parse the Audit Trace document (any el / any att type)
		AuditTraceType audit = AuditTraceType.Factory.parse(doc);
		return audit;
	}

	@Override
	public AuditTraceType getAudit() throws Exception {
		// TODO Auto-generated method stub
		
		return AuditTraceType.Factory.newInstance();
	}

	@Override
	public String getIID() {
		return this.getTaskId();
	}

	@Override
	public void cancel() {
		try {
			//this.getClient().kill(this.jobId);
			this.getAlgorithm().cancel();
		} catch (Exception e) {
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

	

	public String getDeployDirectory() {
		return this.deployDirectory;
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

	

	public String getJobTrackerUrl() {
		return jobTrackerUrl;
	}

	public void setJobTrackerUrl(String jobTrackerUrl) {
		this.jobTrackerUrl = jobTrackerUrl;
	}

	public static String getJavaProcessPath(String process) {
		String fullPath = JavaManager.class
				.getProtectionDomain().getCodeSource().getLocation().toString();
		int searchIndex = fullPath.indexOf("WEB-INF");
		String subPath = fullPath.substring(0, searchIndex);
		subPath = subPath.replaceFirst("file:", "");
		String javaProcessPath = subPath + "WEB-INF/JavaRepository/"+process+"/";
		return javaProcessPath;
	}

	@Override
	public Document invoke(ExecuteRequest request, String algorithmID)
			throws Exception {
		throw new ExceptionReport("This method should not have been called. Implementation error.", "1", "none");
	}

	public JavaTransactionalAlgorithm getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(JavaTransactionalAlgorithm algorithm) {
		this.algorithm = algorithm;
	}
	
}
