package org.n52.wps.server.profiles.IntalioBPMS;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringWriter;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.CancellationException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import net.opengis.wps.x100.AuditTraceType;
import net.opengis.wps.x100.DataInputsType;
import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.InputType;
import net.opengis.wps.x100.InvokedTasksDocument;
import net.opengis.wps.x100.InvokedTasksDocument.InvokedTasks.Task;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.util.Base64;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.util.XMLUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.ode.pmapi.types.x2006.x08.x02.EventInfoDocument;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.profiles.AbstractProcessManager;
import org.n52.wps.server.repository.ITransactionalAlgorithmRepository;
import org.n52.wps.server.request.DeployProcessRequest;
import org.n52.wps.server.request.ExecuteRequest;
import org.n52.wps.server.request.UndeployProcessRequest;
import org.n52.wps.server.request.deploy.DeploymentProfile;
import org.w3.x2005.x08.addressing.MessageIDDocument;
import org.w3.x2005.x08.addressing.ReplyToDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import xint.esa.ssegrid.wps.javaSAGAProfile.URLListDocument;

/**
 * TODO this class was based on transactional branch implementation. However the
 * invoke method was reimplemented Therefore there is a doublon implementation
 * for sending request. *
 * 
 * @author cnl
 * 
 */
public class ApacheOdeProcessManager extends AbstractProcessManager {

	private static Logger LOGGER = Logger
			.getLogger(ApacheOdeProcessManager.class);
	private ODEServiceClient _client;
	private OMFactory _factory;
	private String deploymentEndpoint;
	private String processManagerEndpoint;
	private String instanceManagerEndpoint;
	private OMElement deployRequestOde;
	private String processesPrefix;
	private String WPSPublicationPrefix;
	private ExecuteResponseDocument executeResponse;
	// Asychronous execute client must be shared between threads
	public static ServiceClient executeClient;
	private String processIdentifier;
	private String IID;
	private String SSEGridHome;
	private String processInstanceID;

	/**
	 * @param repository
	 */
	public ApacheOdeProcessManager(ITransactionalAlgorithmRepository repository) {
		super(repository);
		/**
		 * Get the properties of the repository (in regard of the repository
		 * name instead of repository class) (This may also be done with
		 * this.className)
		 */
		Property[] properties = WPSConfig.getInstance()
				.getPropertiesForRepositoryName("ApacheOdeRepository");
		Property deployEndpointProperty = WPSConfig.getInstance()
				.getPropertyForKey(properties, "Ode_DeploymentEndpoint");
		if (deployEndpointProperty == null) {
			throw new RuntimeException(
					"Error. Could not find Ode_DeploymentEndpoint");
		}
		deploymentEndpoint = deployEndpointProperty.getStringValue();
		Property processManagerEndpointProperty = WPSConfig.getInstance()
				.getPropertyForKey(properties, "Ode_ProcessManagerEndpoint");
		if (processManagerEndpointProperty == null) {
			throw new RuntimeException(
					"Error. Could not find OdE_ProcessManagerEndpoint");
		}
		processManagerEndpoint = processManagerEndpointProperty
				.getStringValue();
		instanceManagerEndpoint = processManagerEndpoint.substring(0,
				processManagerEndpoint.indexOf("/ProcessManagement"))
				+ "/InstanceManagement";
		Property processesPrefixProperty = WPSConfig.getInstance()
				.getPropertyForKey(properties, "Ode_ProcessesPrefix");
		if (processesPrefixProperty == null) {
			throw new RuntimeException(
					"Error. Could not find OdE_ProcessManagerEndpoint");
		}
		setProcessesPrefix(processesPrefixProperty.getStringValue());
		
		Property wpsPublicRoot = WPSConfig.getInstance()
		.getPropertyForKey(properties, "WPSPublicationPrefix");
if (wpsPublicRoot == null) {
	throw new RuntimeException(
			"Error. Could not find WPSPublicationPrefix");
}
setWPSPublicationPrefix(wpsPublicRoot.getStringValue());

		
		
		Property SSEGridHomeValue = WPSConfig.getInstance().getPropertyForKey(
				properties, "SSEGrid_Home");
		if (SSEGridHomeValue == null) {
			throw new RuntimeException(
					"Error. Could not find the required SSEGrid_Home property in wps_config.xml");
		}
		setSSEGridHome(SSEGridHomeValue.getStringValue());
		try {
			_factory = OMAbstractFactory.getOMFactory();
			_client = new ODEServiceClient();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static ServiceClient getExecuteClient() {
		if (executeClient == null) {
			try {
				executeClient = new ServiceClient();
			} catch (AxisFault e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return executeClient;
	}

	public static void setExecuteClient(ServiceClient executeClient) {
		ApacheOdeProcessManager.executeClient = executeClient;
	}

	/**
	 * Signature should move to void (exception if failure)
	 */
	public boolean deployProcess(DeployProcessRequest request) throws Exception {

		DeploymentProfile profile = request.getDeploymentProfile();
		if (!(profile instanceof BPELDeploymentProfile)) {
			throw new Exception("Requested Deployment Profile not supported");
		}
		BPELDeploymentProfile deploymentProfile = (BPELDeploymentProfile) profile;
		String processID = deploymentProfile.getProcessID();
		byte[] archive = deploymentProfile.getArchive();
		LOGGER.info("Archive:" + archive);
		OMElement result = null;
		try {
			OMNamespace ins = _factory.createOMNamespace(
					"http://tempo.intalio.org/deploy/deploymentService",
					"deploy");
			deployRequestOde = _factory.createOMElement("deployAssembly", ins); // qualified
			OMElement namePart = _factory.createOMElement("assemblyName", ins);
			namePart.setText(processID);
			OMElement zipElmt = _factory.createOMElement("zip", ins);
			// Need to re-encode the archive
			String base64Enc = Base64.encode(archive);
			OMText zipContent = _factory.createOMText(base64Enc,
					"application/zip", true);
			OMElement activeElem = _factory.createOMElement("activate", ins);
			activeElem.setText("true");
			deployRequestOde.addChild(namePart);
			deployRequestOde.addChild(zipElmt);
			// zipPart.addChild(zipElmt);
			zipElmt.addChild(zipContent);
			deployRequestOde.addChild(activeElem);
			result = sendToDeployment(deployRequestOde);
			// TODO throw Exception if result is not correct
			LOGGER.info("--");
			LOGGER.info(result.getText());
			LOGGER.info("--");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ExceptionReport("Backend error during deployement",
					ExceptionReport.REMOTE_COMPUTATION_ERROR, e);
		}
		return true;
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
		// TODO which version should be deleted ?
		LOGGER.info("hack return true");
		OMElement result = null;
		try {
			OMNamespace ins = _factory.createOMNamespace(
					"http://tempo.intalio.org/deploy/deploymentService",
					"deploy");
			deployRequestOde = _factory
					.createOMElement("undeployAssembly", ins); // qualified
			OMElement namePart = _factory.createOMElement("assemblyName", ins);
			namePart.setText(processID);
			deployRequestOde.addChild(namePart);
			OMElement version = _factory
					.createOMElement("assemblyVersion", ins);
			version.setText("0");
			deployRequestOde.addChild(version);
			result = sendToUndeployment(deployRequestOde);
			// TODO throw Exception if result is not correct
			LOGGER.info("--");
			LOGGER.info(result.getText());
			LOGGER.info("--");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ExceptionReport("Backend error during deployement",
					ExceptionReport.REMOTE_COMPUTATION_ERROR, e);
		}
		return true;
	}

	public synchronized void executeAndGetId(Document doc) {
		// Get the workflow instance id
		try {
			LOGGER.info("execute");
			OMElement response = getExecuteClient().sendReceive(
					XMLUtils.toOM((doc).getDocumentElement()));
			try {
				Document execRespDom = XMLUtils.toDOM(response).getOwnerDocument();
				ExecuteResponseDocument execResp = ExecuteResponseDocument.Factory.parse(execRespDom);
				String workflowId = execResp.getExecuteResponse().getProcessInstanceIdentifier().getInstanceId();
				LOGGER.info("Workflow instance id = "+ workflowId);
				setIID(workflowId);
				if(!StringUtils.isEmpty(workflowId)) {
				return;
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			// If instanceId not present then check the last instance appeared
			LOGGER.info("sleep");
			// Workfaround Wait for the instance to appear TODO enhance
			Thread.sleep(300);
			OMElement listRoot = _client.buildMessage("listInstances",
					new String[] { "filter", "order", "limit" }, new String[] {
							"", "-started", "1" });
			LOGGER.info("Get last instance Request:" + listRoot.toString());
			OMElement result = getInstanceId(listRoot);
			String iid = result.getFirstElement().getFirstElement()
					.getFirstElement().getText();
			LOGGER.info("iid:" + iid);
			setIID(iid);

		} catch (AxisFault af) {

			af.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	public Document invoke(ExecuteRequest req, String algorithmID)
			throws InterruptedException, ExceptionReport {
		ExecuteDocument doc = req.getExecDom();
		 setProcessInstanceID(req.getId());
		String address = req.getMyEPR();
		LOGGER.info("invoke");
		setProcessIdentifier(algorithmID);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ExceptionReport("Unexpected problem",
					ExceptionReport.REMOTE_COMPUTATION_ERROR,e);
		}
		SOAPEnvelope response = null;
		Options options = new Options();
		// set the workflow endpoint
		options.setTo(new EndpointReference(processesPrefix + algorithmID));
		LOGGER.info("Target Worflow endpoint:" + processesPrefix + algorithmID);
		// options.setUseSeparateListener(true);
		options.setAction("urn:executeRequest");
		options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
		String hostname = null;

		// getExecuteClient().getMyEPR(Constants.TRANSPORT_HTTP).getAddress();
		System.out.println("Address:" + address);
		/**
		 * Matcher matcher = Pattern .compile(
		 * "(http://\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}:\\d{1,4})/.*")
		 * .matcher(address); if (matcher.find()) { hostname = matcher.group(1);
		 * }
		 */
		// System.out.println(hostname);
		// String hostname = (String)
		// context.getProperty(MessageContext.TRANSPORT_ADDR);
		// String port = context.getProperty(MessageContext.P)
		// options.setMessageId(instanceID);
		// options.setReplyTo(new EndpointReference(address));

		// Set WSA header without activating the addressing module !
		OMFactory _factory;
		_factory = OMAbstractFactory.getOMFactory();
		OMNamespace ins = _factory.createOMNamespace(
				"http://www.w3.org/2005/08/addressing", "wsa");
		OMElement messageIDHeader = _factory.createOMElement("MessageID", ins); // qualified
		messageIDHeader.setText(getProcessInstanceID());
		OMElement replyToHeader = _factory.createOMElement("ReplyTo", ins); // qualified
		OMElement addressHeader = _factory.createOMElement("Address", ins); // qualified
		addressHeader.setText(address);
		replyToHeader.addChild(addressHeader);

		OMElement toHeader = _factory.createOMElement("To", ins); // qualified
		toHeader.setText(processesPrefix + algorithmID);
		// sender.engageModule("addressing");

		// use WS-Adressing (to perform asynchronous request)
		// getExecuteClient().engageModule("addressing");
		LOGGER.info("set options");
		getExecuteClient().setOptions(options);
		getExecuteClient().removeHeaders();
		getExecuteClient().addHeader(messageIDHeader);
		getExecuteClient().addHeader(replyToHeader);
		getExecuteClient().addHeader(toHeader);
		for (SOAPHeaderBlock headerBlock : req.getSamlHeader()) {
			if (headerBlock.getLocalName().equals("Security")) {
				getExecuteClient().addHeader(headerBlock);
			}
		}
		// get the callback manager
		try {
		executeAndGetId((Document) doc.getDomNode());
		waitCallback();
		}
		catch(InterruptedException ie) {
			LOGGER.info("interrupted");
			ie.printStackTrace();
			throw new CancellationException();
		}
		
		LOGGER.info("Received callback response.");
		if (getExecuteResponse() == null) {
			throw new ExceptionReport("Callback problem",
					ExceptionReport.REMOTE_COMPUTATION_ERROR);
		}
		if(getExecuteResponse().getExecuteResponse().getStatus().isSetProcessFailed()) {
			// if response status is failed, throw an exception
			String exceptionCode = getExecuteResponse().getExecuteResponse().getStatus().getProcessFailed().getExceptionReport().getExceptionArray(0).getExceptionCode();
			String exceptionLocator = getExecuteResponse().getExecuteResponse().getStatus().getProcessFailed().getExceptionReport().getExceptionArray(0).getLocator();
			String exceptionText = getExecuteResponse().getExecuteResponse().getStatus().getProcessFailed().getExceptionReport().getExceptionArray(0).getExceptionTextArray(0);
			throw new ExceptionReport(exceptionText, exceptionCode, exceptionLocator);
		}
		

		return (Document) getExecuteResponse().getDomNode();
	}

	private synchronized OMElement getInstanceId(OMElement listRoot)
			throws AxisFault {
		OMElement result = sendToIM(listRoot);
		return result;
	}

	private void setIID(String iid) {
		this.IID = iid;
	}

	public String getIID() {
		return this.IID;
	}

	public Collection<String> getAllProcesses() throws Exception {
		LOGGER.info("should not be reached todo");
		return null;
		/**
		 * List<String> allProcesses = new ArrayList<String>();
		 * 
		 * // ServiceClient sc = new ServiceClient(null, null);
		 * 
		 * OMElement listRoot = _client.buildMessage("listAllProcesses", new
		 * String[] {}, new String[] {});
		 * 
		 * OMElement result = sendToPM(listRoot); Iterator<OMElement> pi =
		 * result .getFirstElement() .getChildrenWithName( new QName(
		 * "http://www.apache.org/ode/pmapi/types/2006/08/02/",
		 * "process-info"));
		 * 
		 * while (pi.hasNext()) { OMElement omPID = pi.next();
		 * 
		 * String fullName = omPID .getFirstChildWithName( new QName(
		 * "http://www.apache.org/ode/pmapi/types/2006/08/02/",
		 * "pid")).getText();
		 * 
		 * 
		 * allProcesses.add(fullName.substring(fullName.indexOf("}") + 1,
		 * fullName.indexOf("-"))); } return allProcesses; } catch (Exception e)
		 * { return new ArrayList<String>(); }
		 */
	}

	public boolean containsProcess(String processID) throws Exception {
		boolean containsProcess = false;
		// need to filter out the namespace if it is passed in.
		if (processID.contains("}"))
			processID = processID.split("}")[1];
		try {
			OMElement listRoot = _client.buildMessage("listProcesses",
					new String[] { "filter", "orderKeys" }, new String[] {
							"name=" + processID + "", "" });
			LOGGER.info("Conains Process Request:" + listRoot.toString());
			OMElement result = sendToPM(listRoot);
			LOGGER.info("ContainsProcess Response:" + result.toString());
			if (result.toString().contains(processID))
				containsProcess = true;
		} catch (AxisFault af) {
			containsProcess = false;
			af.printStackTrace();
		} catch (Exception e) {
			containsProcess = false;
			e.printStackTrace();
		}
		// return getAllProcesses().contains(processID);
		return containsProcess;
		// throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean unDeployProcess(UndeployProcessRequest request)
			throws Exception {
		LOGGER.info("undeploy starting");
		// unDeployProcess(String processID) is implemented though...
		return unDeployProcess((String) request.getProcessID());
		// return false;
		// throw new UnsupportedOperationException("Not supported yet.");
	}

	private OMElement sendToPM(OMElement msg) throws AxisFault {
		return _client.send(msg, this.processManagerEndpoint, "listProcesses");
		// return _PMclient.send(msg, this.processManagerEndpoint,10000);
	}

	private OMElement sendToIM(OMElement msg) throws AxisFault {
		return _client.send(msg, this.instanceManagerEndpoint, "listInstances");
		// return _PMclient.send(msg, this.processManagerEndpoint,10000);
	}

	private OMElement sendCancel(OMElement msg) throws AxisFault {
		return _client.send(msg, this.instanceManagerEndpoint, "terminate");
		// return _PMclient.send(msg, this.processManagerEndpoint,10000);
	}
	
	private OMElement sendListEvents(OMElement msg) throws AxisFault {
		return _client.send(msg, this.instanceManagerEndpoint, "listEvents");
		// return _PMclient.send(msg, this.processManagerEndpoint,10000);
	}

	private OMElement sendToDeployment(OMElement msg) throws AxisFault {
		return _client.send(msg, this.deploymentEndpoint, "deployAssembly");

		// return _DEPclient.send(msg,this.deploymentEndpoint,10000);
	}

	private OMElement sendToUndeployment(OMElement msg) throws AxisFault {
		return _client.send(msg, this.deploymentEndpoint, "undeployAssembly");

		// return _DEPclient.send(msg,this.deploymentEndpoint,10000);
	}

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

		_client = new ODEServiceClient();
		HashMap<String, String> allProcesses = new HashMap<String, String>();

		OMElement listRoot = _client.buildMessage("listAllProcesses",
				new String[] {}, new String[] {});

		OMElement result = null;
		try {
			result = sendToPM(listRoot);
		} catch (AxisFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	 * @throws InterruptedException 
	 */
	public synchronized void waitCallback() throws InterruptedException {
		try {
			LOGGER.info("Waiting callback");
			wait();
			LOGGER.info("Callback received");
		}
		catch(InterruptedException e)
		{
			throw e;
		}
		
		catch (Exception e) {
			System.out.println(e);
		}
		return;
	}

	public synchronized void notifyRequestManager() {
		notify();
	}

	public void setExecuteResponse(ExecuteResponseDocument executeResponse) {
		this.executeResponse = executeResponse;
	}

	public ExecuteResponseDocument getExecuteResponse() {
		return executeResponse;
	}

	// TODO should enhance (getAudit short and long should call getInstanceEvents once)
	public AuditTraceType getAudit() throws Exception {
		LOGGER.info("************* short form apache get audit **********----*");
		AuditTraceType audit = null;
		InvokedTasksDocument invokedDom = InvokedTasksDocument.Factory
		.newInstance();

		try {

			Element eventDom = XMLUtils.toDOM(getInstanceEvents());
			System.out.println("test");
			audit = AuditTraceType.Factory.parse(eventDom);
			// LOGGER.info("Audit Long document after XMLBeans parsing : "+audit.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("test2");
		LOGGER.info("AUDIT:" + audit.getDomNode().getNodeName());
		LOGGER.info("AUDIT:" + audit.getDomNode().getFirstChild().getNodeName());
		LOGGER.info("AUDIT:"
				+ audit.getDomNode().getFirstChild().getFirstChild()
						.getNodeName());

		// TEventInfoList infoList = TEventInfoList.Factory.parse(.get);
		NodeList infoList = audit.getDomNode().getFirstChild().getFirstChild()
				.getChildNodes();
		LOGGER.info("infoList is parsed:" + infoList.getLength());
		// LOGGER.info("infoList is parsed:"+infoList.getEventInfoArray()[0].toString());
		invokedDom.addNewInvokedTasks();
		for (int i = 0; i < infoList.getLength(); i++) {
			EventInfoDocument eventInfo = EventInfoDocument.Factory
					.parse(infoList.item(i));
			if (eventInfo.getEventInfo().getName()
					.equalsIgnoreCase("ActivityExecStartEvent")
					&& eventInfo.getEventInfo().getActivityType()
							.equalsIgnoreCase("Oinvoke")) {
				long parentId = eventInfo.getEventInfo().getParentScopeId();
				long scopeId = eventInfo.getEventInfo().getScopeId();
				LOGGER.info(eventInfo.getEventInfo().getName());
				LOGGER.info(eventInfo.getEventInfo().getActivityType());

				LOGGER.info("parentId: " + parentId);
				LOGGER.info("scopeId: " + scopeId);
				for (int j = i + 1; j < infoList.getLength(); j++) {
					EventInfoDocument matchingInfo = EventInfoDocument.Factory
							.parse(infoList.item(j));
					if (matchingInfo.getEventInfo().getName()
							.equalsIgnoreCase("VariableModificationEvent")
							&& matchingInfo.getEventInfo().getParentScopeId() == parentId
							&& matchingInfo.getEventInfo().getScopeId() == scopeId) {

						// TODO exctract and build
						// Note : regexp fails here because of XML ! Used
						// substring instead...
						// TODO parse XML to ExecuteResponse instead of string
						// manipulation
						String timestamp = matchingInfo.getEventInfo()
								.getTimestamp().toString();
						LOGGER.info("timestamp:" + timestamp);
						String newValue = matchingInfo.getEventInfo()
								.getNewValue();
						String processiid = newValue.substring(newValue
								.indexOf("instanceId=\"") + 12);
						processiid = processiid.substring(0,
								processiid.indexOf("\""));
						String processid = newValue.substring(newValue
								.indexOf("Process") + 1);
						processid = processid.substring(processid
								.indexOf("Identifier") + 1);
						processid = processid
								.substring(processid.indexOf(">") + 1);
						processid = processid.substring(0,
								processid.indexOf("<"));
						LOGGER.info("found process id : " + processid);
						// LOGGER.info("info:"+newValue);
						LOGGER.info("found iid:" + processiid);
						String statusLocation = newValue.substring(newValue
								.indexOf("statusLocation=\"") + 16);
						statusLocation = statusLocation.substring(0,
								statusLocation.indexOf("\""));
						// extract domain
						System.out.println(statusLocation);
						URL tempURL = new URL(statusLocation);
						System.out.println(tempURL.getPath());
						System.out.println(tempURL.getPath().substring(0,tempURL.getPath().indexOf("/")));
						String serviceDomain= statusLocation.substring(0,statusLocation.indexOf(tempURL.getPath()));
						serviceDomain = serviceDomain.concat(tempURL.getPath().substring(0,tempURL.getPath().indexOf("/",1)));
						serviceDomain = serviceDomain.concat("/services/WPS.WPSHttpSoap11Endpoint");
						
						Task newtask = invokedDom.getInvokedTasks()
								.addNewTask();
						newtask.addNewProcessIdentifier().setStringValue(
								processid);
						newtask.addNewProcessInstanceIdentifier()
								.setStringValue(processiid);
						newtask.addNewTimestamp().setStringValue(timestamp);
						newtask.addNewServer().setStringValue(serviceDomain);
						System.out.println(invokedDom.toString()+"========");
						break;
					}
				}
			}
		}
		System.out.println(invokedDom.toString());
		AuditTraceType shortAudit = AuditTraceType.Factory.parse(invokedDom.getDomNode());
		// Remove because it fails
		//LOGGER.info(shortAudit.toString());
		System.out.println(shortAudit.toString());
		return shortAudit;

	}

	private synchronized OMElement getInstanceEvents() throws AxisFault {

		OMElement listRoot = _client.buildMessage("listEvents", new String[] {
				"instanceFilter", "eventFilter", "maxCount" }, new String[] {
				"iid=" + getIID(), "", "0" });
		LOGGER.info("Get events Request:" + listRoot.toString());
		OMElement result = null;
		// Synchronized to wait the pii
		result = sendListEvents(listRoot);
		LOGGER.info("End of request");
		return result;

	}

	public AuditTraceType getAuditLongForm() throws AxisFault {
		LOGGER.info("long form apache get audit");
		AuditTraceType audit = null;
		OMElement eventResult = getInstanceEvents();
		try {
			// eventResult.get
			String auditDir = getSSEGridHome() + "execute/"
					+ getProcessIdentifier() + "/" + getProcessInstanceID() + "/audits";
			File auditDirFile = new File(auditDir);
			auditDirFile.mkdirs();
			String auditPath = auditDir + "/eventlist.xml";
			File auditFile = new File(auditPath);
			if(auditFile.exists()) {
				auditFile.delete();
				auditFile.createNewFile();
			}
			Element eventDom = XMLUtils.toDOM(eventResult);
			Source source = new DOMSource(eventDom);
			// Prepare the output file
			Result result = new StreamResult(auditFile);
			// Write the DOM document to the file
			Transformer xformer = TransformerFactory.newInstance()
					.newTransformer();
			xformer.transform(source, result);
			URLListDocument auditURLS = URLListDocument.Factory.newInstance();
			auditURLS.addNewURLList().setCount(1);
			String eventListURL = getWPSPublicationPrefix() + "execute/"
			+ getProcessIdentifier() + "/" + getProcessInstanceID() + "/audits"+ "/eventlist.xml";
			auditURLS.getURLList().addNewUrl().setStringValue(eventListURL);
			audit = AuditTraceType.Factory.parse(auditURLS.getDomNode());
			// LOGGER.info("Audit Long document after XMLBeans parsing : "+audit.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		LOGGER.info("setting audit trace");

		return audit;

	}

	public void setProcessIdentifier(String processIdentifier) {
		this.processIdentifier = processIdentifier;
	}

	public String getProcessIdentifier() {
		return processIdentifier;
	}

	@Override
	public Document invoke(ExecuteDocument payload, String algorithmID)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void callback(ExecuteResponseDocument execRespDom) {
		this.setExecuteResponse(execRespDom);
		this.notifyRequestManager();
		return;
	}

	public void setSSEGridHome(String sSEGridHome) {
		SSEGridHome = sSEGridHome;
	}

	public String getSSEGridHome() {
		return SSEGridHome;
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

	public void setProcessInstanceID(String processInstanceID) {
		this.processInstanceID = processInstanceID;
	}
	public void cancel() {
		LOGGER.info("BPMS Cancel...");
		OMElement terminateRequest = _client.buildMessage("terminate",
				new String[] { "iid" }, new String[] {
						getIID() });
		LOGGER.info("Terminate instance request:" + terminateRequest.toString());
		try {
			OMElement result = terminateInstance(terminateRequest);
		} catch (AxisFault e) {
			LOGGER.error("Could not cancel the intalio workflow instance");
			e.printStackTrace();
		}
		return;
	}

	private OMElement terminateInstance(OMElement terminateRequest) throws AxisFault {
		OMElement result = sendCancel(terminateRequest);
		return result;
	}
}
