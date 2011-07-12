package org.n52.wps.server.profiles.IntalioBPMS;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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

import net.opengis.wps.x100.DataInputsType;
import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.InputType;
import net.opengis.wps.x100.OutputDataType;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.util.Base64;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.saaj.util.SAAJUtil;
import org.apache.axis2.transport.http.SimpleHTTPServer;
import org.apache.axis2.util.XMLUtils;
import org.apache.log4j.Logger;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.data.IData;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.profiles.AbstractProcessManager;
import org.n52.wps.server.profiles.OutputParser;
import org.n52.wps.server.repository.ITransactionalAlgorithmRepository;
import org.n52.wps.server.request.DeployProcessRequest;
import org.n52.wps.server.request.deploy.DeploymentProfile;
import org.n52.wps.server.response.OutputDataItem;
import org.n52.wps.transactional.request.UndeployProcessRequest;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.ibm.wsdl.extensions.http.HTTPConstants;


/**
 * TODO this class was based on transactional branch implementation. However the invoke method was reimplemented
 * Therefore there is a doublon implementation for sending request. * 
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
	private OMElement deployRequestOde;
	private String processesPrefix;
	private ExecuteResponseDocument executeResponse;
	// Asychronous execute client must be shared between threads
	public static ServiceClient executeClient;
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
		Property processesPrefixProperty = WPSConfig.getInstance()
				.getPropertyForKey(properties, "Ode_ProcessesPrefix");
		if (processesPrefixProperty == null) {
			throw new RuntimeException(
					"Error. Could not find OdE_ProcessManagerEndpoint");
		}
		setProcessesPrefix(processesPrefixProperty.getStringValue());
		try {
			_factory = OMAbstractFactory.getOMFactory();
			_client = new ODEServiceClient();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static ServiceClient getExecuteClient() {
		if(executeClient == null)	{
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
		OMNamespace pmapi = _factory.createOMNamespace(
				"http://www.apache.org/ode/pmapi", "pmapi");
		OMElement root = _factory.createOMElement("undeploy", pmapi); // qualified
																		// operation
																		// name
		OMElement part = _factory.createOMElement("processName", pmapi);
		part.setText(processID);
		root.addChild(part);

		// Undeploy
		sendToDeployment(root);

		return true;
	}

	public Document invoke(ExecuteDocument doc, String algorithmID)
			throws Exception {

		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		SOAPEnvelope response = null;
		try {
			Options options = new Options();
			// set the workflow endpoint
			options.setTo(new EndpointReference(processesPrefix+algorithmID));
			options.setUseSeparateListener(true);
			options.setAction("urn:executeResponseCallback");
			options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
			String hostname = null;
			String address = getExecuteClient().getMyEPR(Constants.TRANSPORT_HTTP).getAddress();
			System.out.println(address);
			Matcher matcher = Pattern.compile("(http://\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}:\\d{1,4})/.*").matcher(address);
			if(matcher.find()) {
				hostname = matcher.group(1);
			}
			System.out.println(hostname);
			//String hostname = (String) context.getProperty(MessageContext.TRANSPORT_ADDR); 
			//String port = context.getProperty(MessageContext.P)
			options.setReplyTo(new EndpointReference(hostname+"/wps/services/executeResponseCallback"));
			// use WS-Adressing (to perform asynchronous request)
			getExecuteClient().engageModule("addressing");
			getExecuteClient().setOptions(options);
			// get the callback manager
			CallbackManager callback = new CallbackManager(this);
			// send the request
			// Following doesnt work
			getExecuteClient().sendReceiveNonBlocking(XMLUtils.toOM( ((Document)doc.getDomNode()).getDocumentElement()), callback);
			waitCallback();
			LOGGER.info("Received callback response.");
		} catch (AxisFault af) {
			af.printStackTrace();

		} 
		if(getExecuteResponse()==null) {
			throw new ExceptionReport("Callback problem", ExceptionReport.REMOTE_COMPUTATION_ERROR);
		}

		return (Document)getExecuteResponse().getDomNode();
	}

	public Collection<String> getAllProcesses() throws Exception {
		try {
			List<String> allProcesses = new ArrayList<String>();

			// ServiceClient sc = new ServiceClient(null, null);

			OMElement listRoot = _client.buildMessage("listAllProcesses",
					new String[] {}, new String[] {});

			OMElement result = sendToPM(listRoot);
			Iterator<OMElement> pi = result
					.getFirstElement()
					.getChildrenWithName(
							new QName(
									"http://www.apache.org/ode/pmapi/types/2006/08/02/",
									"process-info"));

			while (pi.hasNext()) {
				OMElement omPID = pi.next();

				String fullName = omPID
						.getFirstChildWithName(
								new QName(
										"http://www.apache.org/ode/pmapi/types/2006/08/02/",
										"pid")).getText();

				/*
				 * just take the name as defined by the user... whats returned
				 * originally was something like {http://xy.z}ProcessName-XXX
				 * (-XXX is attached due to the ODE-versioning) this lead to
				 * problems with the processdescription, which has the name
				 * "ProcessName"
				 */
				allProcesses.add(fullName.substring(fullName.indexOf("}") + 1,
						fullName.indexOf("-")));
			}
			return allProcesses;
		} catch (Exception e) {
			return new ArrayList<String>();
		}

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
			OMElement result = sendToPM(listRoot);

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
		// unDeployProcess(String processID) is implemented though...
		return unDeployProcess((String) request.getProcessID());
		// return false;
		// throw new UnsupportedOperationException("Not supported yet.");
	}

	private OMElement sendToPM(OMElement msg) throws AxisFault {
		return _client.send(msg, this.processManagerEndpoint);
		// return _PMclient.send(msg, this.processManagerEndpoint,10000);
	}

	private OMElement sendToDeployment(OMElement msg) throws AxisFault {
		return _client.send(msg, this.deploymentEndpoint);

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

	public void setExecuteResponse(ExecuteResponseDocument executeResponse) {
		this.executeResponse = executeResponse;
	}

	public ExecuteResponseDocument getExecuteResponse() {
		return executeResponse;
	}
	
}
