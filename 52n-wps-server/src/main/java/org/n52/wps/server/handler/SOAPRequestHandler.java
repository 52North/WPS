package org.n52.wps.server.handler;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.WebProcessingService;
import org.n52.wps.server.request.CancelRequest;
import org.n52.wps.server.request.CapabilitiesRequest;
import org.n52.wps.server.request.DeployProcessRequest;
import org.n52.wps.server.request.DescribeProcessRequest;
import org.n52.wps.server.request.ExecuteRequest;
import org.n52.wps.server.request.GetStatusRequest;
import org.n52.wps.server.request.Request;
import org.n52.wps.server.request.UndeployRequest;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class SOAPRequestHandler extends RequestHandler {

	private static Logger LOGGER = Logger.getLogger(SOAPRequestHandler.class);

	/**
	 * Handles requests of type SOAPMessage (currently capabilities and
	 * describeProcess). A OMElement is used to represent the client input.
	 * 
	 * @param params
	 *            The client input
	 * @param outOM
	 *            The OMElement to write the response to.
	 * @throws ExceptionReport
	 *             If the requested operation is not supported
	 */

	public SOAPRequestHandler(Document inputDoc, OutputStream os)
			throws ExceptionReport {
		this.os = os;
		// sleepingTime is 0, by default.
		/*
		 * if(WPSConfiguration.getInstance().exists(PROPERTY_NAME_COMPUTATION_TIMEOUT)) {
		 * this.sleepingTime =
		 * Integer.parseInt(WPSConfiguration.getInstance().getProperty(PROPERTY_NAME_COMPUTATION_TIMEOUT)); }
		 */
		String nodeName, localName, nodeURI, version;
		String sleepTime = WPSConfig.getInstance().getWPSConfig().getServer()
				.getComputationTimeoutMilliSeconds();
		if (sleepTime == null || sleepTime.equals("")) {
			sleepTime = "5";
		}
		//this.sleepingTime = new Integer(sleepTime);

	
			Node child = inputDoc.getFirstChild();
			while (child.getNodeName().compareTo("#comment") == 0) {
				child = child.getNextSibling();
			}
			nodeName = child.getNodeName();
			localName = child.getLocalName();
			nodeURI = child.getNamespaceURI();

		// get the request type
		if (nodeURI.equals(WebProcessingService.WPS_NAMESPACE)
				&& localName.equals("Execute")) {
			
			Node versionNode = child.getAttributes().getNamedItem("version");
			if (versionNode == null) {
				throw new ExceptionReport("No version parameter supplied.",
						ExceptionReport.MISSING_PARAMETER_VALUE);
			}
			version = child.getAttributes().getNamedItem("version")
					.getNodeValue();

			if (version == null) {
				throw new ExceptionReport("version is null: ",
						ExceptionReport.MISSING_PARAMETER_VALUE);
			}
			if (!version.equals(Request.SUPPORTED_VERSION)) {
				throw new ExceptionReport("version is null: ",
						ExceptionReport.INVALID_PARAMETER_VALUE);
			}

			req = new ExecuteRequest(inputDoc);
			if (req instanceof ExecuteRequest) {
				setResponseMimeType((ExecuteRequest) req);
			} else {
				this.responseMimeType = "text/xml";
			}
		} else if (localName.equals("GetCapabilities")) {
			req = new CapabilitiesRequest(inputDoc);
		} else if (localName.equals("DescribeProcess")) {
			req = new DescribeProcessRequest(inputDoc);
		} 
		/**
		 * Christophe Noël - Spacebel June 2011 : added new WPS 2.0 operations
		 */
		
		else if (localName.equals("GetStatus")) {
			req = new GetStatusRequest(inputDoc);
		} else if (localName.equals("Cancel")) {
			req = new CancelRequest(inputDoc);
		}
		else if (localName.equals("DeployProcess")) {
			req = new DeployProcessRequest(inputDoc);
		}
		/**else if (localName.equals("UndeployProcess")) {
			req = new UndeployProcessRequest(inputDoc);
			}
		 */
		
		// fix : gardians were not correct...
		else if (nodeURI.equals(WebProcessingService.WPS_NAMESPACE)) {
			throw new ExceptionReport("specified operation is not supported: "
					+ nodeName, ExceptionReport.OPERATION_NOT_SUPPORTED);
		}
		else if (!nodeURI.equals(WebProcessingService.WPS_NAMESPACE)) {
			throw new ExceptionReport("specified namespace is not supported: "
					+ nodeURI, ExceptionReport.INVALID_PARAMETER_VALUE);
		}

	}
}