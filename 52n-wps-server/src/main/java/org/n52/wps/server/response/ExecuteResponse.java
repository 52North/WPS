/***************************************************************
 This implementation provides a framework to publish processes to the
web through the  OGC Web Processing Service interface. The framework 
is extensible in terms of processes and data handlers. It is compliant 
to the WPS version 0.4.0 (OGC 05-007r4). 

 Copyright (C) 2006 by con terra GmbH

 Authors: 
	Theodor Foerster, ITC, Enschede, the Netherlands
	Carsten Priess, Institute for geoinformatics, University of
Muenster, Germany


 Contact: Albert Remke, con terra GmbH, Martin-Luther-King-Weg 24,
 48155 Muenster, Germany, 52n@conterra.de

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 version 2 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program (see gnu-gpl v2.txt); if not, write to
 the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA  02111-1307, USA or visit the web page of the Free
 Software Foundation, http://www.fsf.org.

 Created on: 13.06.2006
 ***************************************************************/
package org.n52.wps.server.response;

import java.io.OutputStream;
import java.util.ArrayList;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.util.XMLUtils;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.database.DatabaseFactory;
import org.n52.wps.server.request.ExecuteRequest;
import org.w3.x2005.x08.addressing.MessageIDDocument;
import org.w3.x2005.x08.addressing.ReplyToDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ExecuteResponse extends Response {

	private static Logger LOGGER = Logger.getLogger(ExecuteResponse.class);
	private boolean alreadyStored;
	private ExecuteResponseBuilder builder;
	public static ServiceClient executeClient;

	public ExecuteResponse(ExecuteRequest request) throws ExceptionReport {
		super(request);
		alreadyStored = false;
		this.builder = ((ExecuteRequest) this.request)
				.getExecuteResponseBuilder();
		if (request.isStoreResponse()) {
			LOGGER.debug("Store Response in Database");

			DatabaseFactory.getDatabase().storeResponse(this);
		}
	}

	/**
	 * Note : Can be called either with a FileOutputStream (by ExecuteRequest),
	 * either for the response (http outputstream) (and then also the fileoutputstream must be called)
	 * In my opinion, the class should be simplified using 2 separate method:
	 * saveStatusFile
	 * and saveAll (for HTTP stream) which call saveStatusFile itself
	 */
	public void save(OutputStream os) throws ExceptionReport {
		// workaround, to avoid infinite processing.
		if (!alreadyStored) {
			this.builder.update();
			if (((ExecuteRequest) request).isStoreResponse()) {
				this.alreadyStored = true;
				DatabaseFactory.getDatabase().storeResponse(this);
			}
		}
		this.builder.save(os);
	}

	public ExecuteResponseBuilder getExecuteResponseBuilder() {
		return builder;
	}

	public String getMimeType() {
		return builder.getMimeType();
	}

	public void sendCallback(ArrayList<SOAPHeaderBlock> headerBlocks)
			throws Exception {
		LOGGER.info("sending callback");
		ReplyToDocument replyToBlock = null;
		MessageIDDocument messageIDBlock = null;
		for (SOAPHeaderBlock headerBlock : headerBlocks) {
			if (headerBlock.getLocalName().equals("ReplyTo")) {
				replyToBlock = ReplyToDocument.Factory.parse(XMLUtils
						.toDOM(headerBlock));
				LOGGER.info(replyToBlock.toString());
			}
			if (headerBlock.getLocalName().equals("MessageID")) {
				messageIDBlock = MessageIDDocument.Factory.parse(XMLUtils
						.toDOM(headerBlock));
				LOGGER.info(messageIDBlock.toString());
			}
		}
		LOGGER.info("sending callback service client...");
		ServiceClient sender = new ServiceClient();

		LOGGER.info("target EPR:"
				+ replyToBlock.getReplyTo().getAddress().getStringValue());
		EndpointReference targetEPR = new EndpointReference(replyToBlock
				.getReplyTo().getAddress().getStringValue());
		Options options = new Options();
		options.setTo(targetEPR);
		options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
		options.setAction("urn:executeResponseCallback");
		//options.addRelatesTo(new RelatesTo(messageIDBlock.getMessageID()
			//	.getStringValue()));
		OMFactory _factory;
		_factory = OMAbstractFactory.getOMFactory();
		OMNamespace ins = _factory.createOMNamespace(
				"http://www.w3.org/2005/08/addressing", "wsa");
		OMElement relatesToHeader = _factory.createOMElement("RelatesTo", ins); // qualified
		relatesToHeader
				.setText((messageIDBlock.getMessageID().getStringValue()));
		// sender.engageModule("addressing");
		sender.setOptions(options);
		sender.addHeader(relatesToHeader);
		sender.sendRobust(XMLUtils.toOM(((Document) this
				.getExecuteResponseBuilder().getDoc().getDomNode())
				.getDocumentElement()));
		LOGGER.debug("Fire and Forget Callback closed");
		sender.cleanupTransport();
		sender.cleanup();
		LOGGER.debug("All cleaned for axis service client");
		
	}

	// use this if you want a single ServiceClient (TODO to be deleted)
	public static ServiceClient getCallbackClient() {
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
}
