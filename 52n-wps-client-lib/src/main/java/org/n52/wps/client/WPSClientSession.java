package org.n52.wps.client;


import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.opengis.ows.x11.ExceptionReportDocument;
import net.opengis.ows.x11.OperationDocument.Operation;
import net.opengis.wps.x100.CapabilitiesDocument;
import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.ProcessBriefType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionsDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Contains some convenient methods to access and manage WebProcessingSerivces in a very
 * generic way.
 * 
 * This is implemented as a singleton.
 * @author foerster
 */


public class WPSClientSession {
	
	private static Logger LOGGER = LoggerFactory.getLogger(WPSClientSession.class);
	private static final String OGC_OWS_URI = "http://www.opengeospatial.net/ows";
	private static String SUPPORTED_VERSION = "1.0.0"; 
	
	private static WPSClientSession session;
	private Map<String, CapabilitiesDocument> loggedServices;
	private XmlOptions options = null;
	
	// a Map of <url, all available process descriptions>
	private Map<String, ProcessDescriptionsDocument> processDescriptions;

	/**
	 * Initializes a WPS client session.
	 *
	 */
	private WPSClientSession() {
		options = new XmlOptions();
		options.setLoadStripWhitespace();
		options.setLoadTrimTextBuffer();
		loggedServices = new HashMap<String, CapabilitiesDocument>();
		processDescriptions = new HashMap<String, ProcessDescriptionsDocument>();
	}
	
	/*
	 * @result An instance of a WPS Client session.
	 */
	public static WPSClientSession getInstance() {
		if(session == null) {
			session = new WPSClientSession();
		}
		return session;
	}
	/**
	 * This resets the WPSClientSession. This might be necessary, to get rid of old service entries/descriptions. However, the session has to be repopulated afterwards.
	 */
	public static void reset() {
		session = new WPSClientSession();
	}
	
	/**
	 * Connects to a WPS and retrieves Capabilities plus puts all available Descriptions into cache.
	 * @param url the entry point for the service. This is used as id for further identification of the service.
	 * @return true, if connect succeeded, false else.
	 * @throws WPSClientException
	 */
	public boolean connect(String url) throws WPSClientException {
		LOGGER.info("CONNECT");
		if(loggedServices.containsKey(url)) {
			LOGGER.info("Service already registered: " + url);
			return false;
		}
		CapabilitiesDocument capsDoc = retrieveCapsViaGET(url);
		if(capsDoc != null) {
			loggedServices.put(url, retrieveCapsViaGET(url));
		}
		ProcessDescriptionsDocument processDescs = describeAllProcesses(url);
		if(processDescs != null && capsDoc != null) {
			processDescriptions.put(url, processDescs);
			return true;
		}
		LOGGER.warn("retrieving caps failed, caps are null");
		return false;
	}
	
	/**
	 * removes a service from the session
	 * @param url
	 */
	public void disconnect(String url) {
		if(loggedServices.containsKey(url)) {
			loggedServices.remove(url);
			processDescriptions.remove(url);
			LOGGER.info("service removed successfully: " + url);
		}
	}
	
	/**
	 * returns the serverIDs of all loggedServices
	 * @return
	 */
	public List<String> getLoggedServices() {
		return new ArrayList<String>(loggedServices.keySet());
	}
	
	/** 
	 * informs you if the descriptions for the specified service is already in the session. 
	 * in normal case it should return true :)
	 * @param serverID
	 * @return success
	 */
	public boolean descriptionsAvailableInCache(String serverID) {
		return processDescriptions.containsKey(serverID);
	}
	
	/**
	 * returns the cached processdescriptions of a service.
	 * @param serverID
	 * @return success
	 * @throws IOException 
	 */
	private ProcessDescriptionsDocument getProcessDescriptionsFromCache(String wpsUrl) throws IOException {
		if(! descriptionsAvailableInCache(wpsUrl)) {
			try{
				connect(wpsUrl);
			}
			catch(WPSClientException e) {
				throw new IOException("Could not initialize WPS " + wpsUrl);
			}
		}
		return processDescriptions.get(wpsUrl);
	}
	
	
	
	/**
	 * return the processDescription for a specific process from Cache.
	 * @param serverID
	 * @param processID
	 * @return a ProcessDescription for a specific process from Cache.
	 * @throws IOException 
	 */
	public ProcessDescriptionType getProcessDescription(String serverID, String processID) throws IOException {
		ProcessDescriptionType[] processes = getProcessDescriptionsFromCache(serverID).getProcessDescriptions().getProcessDescriptionArray();
		for(ProcessDescriptionType process : processes) {
			if(process.getIdentifier().getStringValue().equals(processID)) {
				return process;
			}
		}
		return null;
	}

	/**
	 * Delivers all ProcessDescriptions from a WPS
	 * 
	 * @param wpsUrl the URL of the WPS
	 * @return An Array of ProcessDescriptions
	 * @throws IOException
	 */
	public ProcessDescriptionType[] getAllProcessDescriptions(String wpsUrl) throws IOException{
		return getProcessDescriptionsFromCache(wpsUrl).getProcessDescriptions().getProcessDescriptionArray();
	}
	
	/**
	 * looks up, if the service exists already in session.
	 */
	public boolean serviceAlreadyRegistered(String serverID) {
		return loggedServices.containsKey(serverID);
	}

	/**
	 * provides you the cached capabilities for a specified service.
	 * @param url
	 * @return
	 */
	public CapabilitiesDocument getWPSCaps(String url) {
		return loggedServices.get(url);
	}
	
	/**
	 * retrieves all current available ProcessDescriptions of a WPS. Mention: to get the current list 
	 * of all processes, which will be requested, the cached capabilities will be used. Please keep that in mind.
	 * the retrieved descriptions will not be cached, so only transient information!
	 * @param url
	 * @return
	 * @throws WPSClientException
	 */
	public ProcessDescriptionsDocument describeAllProcesses(String url) throws WPSClientException {
		CapabilitiesDocument doc = loggedServices.get(url);
		if(doc == null) {
			LOGGER.warn("serviceCaps are null, perhaps server does not exist");
			return null;
		}
		ProcessBriefType[] processes = doc.getCapabilities().getProcessOfferings().getProcessArray();
		String[] processIDs = new String[processes.length];
		for(int i = 0; i < processIDs.length; i++) {
			processIDs[i] = processes[i].getIdentifier().getStringValue();
		}
		return describeProcess(processIDs, url);
		
	}
	
	/**
	 * retrieves the desired description for a service. the retrieved information will not be held in cache! 
	 * @param processIDs one or more processIDs
	 * @param serverID
	 * @throws WPSClientException
	 */
	public ProcessDescriptionsDocument describeProcess(String[] processIDs, String serverID) throws WPSClientException {
		CapabilitiesDocument caps = this.loggedServices.get(serverID);
		Operation[] operations = caps.getCapabilities().getOperationsMetadata().getOperationArray();
		String url = null;
		for(Operation operation : operations){
			if(operation.getName().equals("DescribeProcess")) {
				url = operation.getDCPArray()[0].getHTTP().getGetArray()[0].getHref();
			}
		}
		if(url == null) {
			throw new WPSClientException("Missing DescribeOperation in Capabilities");
		}
		return retrieveDescriptionViaGET(processIDs, url);
	}
	
	/**
	 * Executes a process at a WPS
	 * 
	 * @param url url of server not the entry additionally defined in the caps.
	 * @param execute Execute document
	 * @return either an ExecuteResponseDocument or an InputStream if asked for RawData or an Exception Report 
	 */
	private Object execute(String serverID, ExecuteDocument execute, boolean rawData) throws WPSClientException{
		CapabilitiesDocument caps = loggedServices.get(serverID);
		Operation[] operations = caps.getCapabilities().getOperationsMetadata().getOperationArray();
		String url = null;
		for(Operation operation : operations) {
			if(operation.getName().equals("Execute")) {
				url = operation.getDCPArray()[0].getHTTP().getPostArray()[0].getHref();
			}
		}
		if(url == null) {
			throw new WPSClientException("Caps does not contain any information about the entry point for process execution");
		}
		execute.getExecute().setVersion(SUPPORTED_VERSION);
		return retrieveExecuteResponseViaPOST(url, execute,rawData);
	}
	
	/**
	 * Executes a process at a WPS
	 * 
	 * @param url url of server not the entry additionally defined in the caps.
	 * @param execute Execute document
	 * @return either an ExecuteResponseDocument or an InputStream if asked for RawData or an Exception Report 
	 */
	public Object execute(String serverID, ExecuteDocument execute) throws WPSClientException{
		if(execute.getExecute().isSetResponseForm()==true && execute.getExecute().isSetResponseForm()==true && execute.getExecute().getResponseForm().isSetRawDataOutput()==true){
			return execute(serverID, execute,true);
		}else{
			return execute(serverID, execute,false);
		}
			
	}
	
	private CapabilitiesDocument retrieveCapsViaGET(String url) throws WPSClientException {
		ClientCapabiltiesRequest req = new ClientCapabiltiesRequest();
		url = req.getRequest(url);
		try {
			URL urlObj = new URL(url);
			urlObj.getContent();
			InputStream is = urlObj.openStream();
			Document doc = checkInputStream(is);
			return CapabilitiesDocument.Factory.parse(doc, options);
		} catch (MalformedURLException e) {
			throw new WPSClientException("Capabilities URL seems to be unvalid: " + url, e);
		} catch (IOException e) {
			throw new WPSClientException("Error occured while retrieving capabilities from url: " + url, e);
		} catch (XmlException e) {
			throw new WPSClientException("Error occured while parsing XML", e);
		}
	}
	
	private ProcessDescriptionsDocument retrieveDescriptionViaGET(String[] processIDs, String url) throws WPSClientException{
		ClientDescribeProcessRequest req = new ClientDescribeProcessRequest();
		req.setIdentifier(processIDs);
		String requestURL = req.getRequest(url);
		try {
			URL urlObj = new URL(requestURL);
			InputStream is = urlObj.openStream();
			Document doc = checkInputStream(is);
			return ProcessDescriptionsDocument.Factory.parse(doc, options);
		} catch (MalformedURLException e) {
			throw new WPSClientException("URL seems not to be valid", e);
		}
		catch (IOException e) {
			throw new WPSClientException("Error occured while receiving data", e);
		}
		catch(XmlException e) {
			throw new WPSClientException("Error occured while parsing ProcessDescription document", e);
		}
	}
	
	private InputStream retrieveDataViaPOST(XmlObject obj, String urlString) throws WPSClientException{
		try {
			URL url = new URL(urlString);
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("Accept-Encoding", "gzip");
			conn.setRequestProperty("Content-Type", "text/xml");
			conn.setDoOutput(true);
			obj.save(conn.getOutputStream());
			InputStream input = null;
			String encoding = conn.getContentEncoding();
			if(encoding != null && encoding.equalsIgnoreCase("gzip")) {
				input = new GZIPInputStream(conn.getInputStream());
			}
			else {
				input = conn.getInputStream();
			}
			return input;
		} catch (MalformedURLException e) {
			throw new WPSClientException("URL seems to be unvalid", e);
		} catch (IOException e) {
			throw new WPSClientException("Error while transmission", e);
		}
	}
	
	private Document checkInputStream(InputStream is) throws WPSClientException {
		DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
		fac.setNamespaceAware(true);
		try {
			Document doc = fac.newDocumentBuilder().parse(is);
			if(getFirstElementNode(doc.getFirstChild()).getLocalName().equals("ExceptionReport") && getFirstElementNode(doc.getFirstChild()).getNamespaceURI().equals(OGC_OWS_URI)) {
				try {
					ExceptionReportDocument exceptionDoc = ExceptionReportDocument.Factory.parse(doc);
					LOGGER.debug(exceptionDoc.xmlText(options));
					throw new WPSClientException("Error occured while executing query", exceptionDoc);
				}
				catch(XmlException e) {
					throw new WPSClientException("Error while parsing ExceptionReport retrieved from server", e);
				}
			}
			return doc;
		} catch (SAXException e) {
			throw new WPSClientException("Error while parsing input.", e);
		} catch (IOException e) {
			throw new WPSClientException("Error occured while transfer", e);
		} catch (ParserConfigurationException e) {
			throw new WPSClientException("Error occured, parser is not correctly configured", e);
		}
	}

	private Node getFirstElementNode(Node node) {
		if(node == null) {
			return null;
		}
		if(node.getNodeType() == Node.ELEMENT_NODE) {
			return node;
		}
		else {
			return getFirstElementNode(node.getNextSibling());
		}
		
	}
	/**
	 * either an ExecuteResponseDocument or an InputStream if asked for RawData or an Exception Report
	 * @param url
	 * @param doc
	 * @param rawData
	 * @return
	 * @throws WPSClientException
	 */
	private Object retrieveExecuteResponseViaPOST(String url, ExecuteDocument doc, boolean rawData) throws WPSClientException{
		InputStream is = retrieveDataViaPOST(doc, url);
		if(rawData) {
			return is;
		}
		Document documentObj = checkInputStream(is);
		ExceptionReportDocument erDoc = null;
		try {
			return ExecuteResponseDocument.Factory.parse(documentObj);
		}
		catch(XmlException e) {
			try {
				erDoc = ExceptionReportDocument.Factory.parse(documentObj);
			} catch (XmlException e1) {
				throw new WPSClientException("Error occured while parsing executeResponse", e);
			}
			return erDoc;
		}
	}
	
	public String[] getProcessNames(String url) throws IOException {
		ProcessDescriptionType[] processes = getProcessDescriptionsFromCache(url).getProcessDescriptions().getProcessDescriptionArray();
		String[] processNames = new String[processes.length];
		for(int i = 0; i<processNames.length; i++){
			processNames[i] = processes[i].getIdentifier().getStringValue();
		}
		return processNames;
	}

	/**
	 * Executes a process at a WPS
	 * 
	 * @param url url of server not the entry additionally defined in the caps.
	 * @param executeAsGETString KVP Execute request
	 * @return either an ExecuteResponseDocument or an InputStream if asked for RawData or an Exception Report 
	 */
	public Object executeViaGET(String url, String executeAsGETString) throws WPSClientException {
		url = url + executeAsGETString;
		try {
			URL urlObj = new URL(url);
			InputStream is = urlObj.openStream();
		
			if(executeAsGETString.toUpperCase().contains("RAWDATA")){
				return is;
			}
			Document doc = checkInputStream(is);
			ExceptionReportDocument erDoc = null;
			try {
				return ExecuteResponseDocument.Factory.parse(doc);
			}
			catch(XmlException e) {
				try {
					erDoc = ExceptionReportDocument.Factory.parse(doc);
				} catch (XmlException e1) {
					throw new WPSClientException("Error occured while parsing executeResponse", e);
				}
				throw new WPSClientException("Error occured while parsing executeResponse", erDoc);
			}
		} catch (MalformedURLException e) {
			throw new WPSClientException("Capabilities URL seems to be unvalid: " + url, e);
		} catch (IOException e) {
			throw new WPSClientException("Error occured while retrieving capabilities from url: " + url, e);
		}
		
	}
	
	
	

	
	

}
