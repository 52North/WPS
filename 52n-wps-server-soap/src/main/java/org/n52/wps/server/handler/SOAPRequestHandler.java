/**
 * ﻿Copyright (C) 2007 - 2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *       • Apache License, version 2.0
 *       • Apache Software License, version 1.0
 *       • GNU Lesser General Public License, version 3
 *       • Mozilla Public License, versions 1.0, 1.1 and 2.0
 *       • Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.wps.server.handler;

import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.WebProcessingService;
import org.n52.wps.server.request.CapabilitiesRequest;
import org.n52.wps.server.request.DescribeProcessRequest;
import org.n52.wps.server.request.ExecuteRequest;
import org.n52.wps.server.request.Request;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class SOAPRequestHandler extends RequestHandler {

	private static Logger LOGGER = LoggerFactory.getLogger(SOAPRequestHandler.class);

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
		} else if (!localName.equals("Execute")) {
			throw new ExceptionReport("specified operation is not supported: "
					+ nodeName, ExceptionReport.OPERATION_NOT_SUPPORTED);
		} else if (nodeURI.equals(WebProcessingService.WPS_NAMESPACE)) {
			throw new ExceptionReport("specified namespace is not supported: "
					+ nodeURI, ExceptionReport.INVALID_PARAMETER_VALUE);
		}

	}
}