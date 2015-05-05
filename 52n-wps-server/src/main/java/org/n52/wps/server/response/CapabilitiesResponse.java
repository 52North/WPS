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
package org.n52.wps.server.response;

import java.io.IOException;
import java.io.InputStream;

import org.apache.xmlbeans.XmlException;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.CapabilitiesConfiguration;
import org.n52.wps.server.CapabilitiesConfigurationV200;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.request.CapabilitiesRequest;
import org.n52.wps.util.XMLBeansHelper;

/**
 * A Response to the Request "GetCapabilities"
 *
 */
public class CapabilitiesResponse extends Response {

	/**
	 * Serves the Request with a Response
	 * @param request The GetCapabilities request
	 */
	public CapabilitiesResponse(CapabilitiesRequest request){
		super(request);
	}
	
	/**
	 * Save this Response to an OutputStream
	 * @param os The OutputStream to save this Response to
	 * @throws ExceptionReport
	 */
	public InputStream getAsStream() throws ExceptionReport{
		try {

			/* [OGC 06-121r9 OWS Common 2.0]:
			 * if acceptVersions parameter was send, the first supported version should be used
			 */
			String[] requestedVersions = (String[]) getRequest().getMap().get("version");
			
			if(requestedVersions != null && requestedVersions.length != 0){
				
				for (int i = 0; i < requestedVersions.length; i++) {
					String requestedVersion = requestedVersions[i].trim();
					if(WPSConfig.SUPPORTED_VERSIONS.contains(requestedVersion)){

						if(requestedVersion.equals(WPSConfig.VERSION_100)){
							return CapabilitiesConfiguration.getInstance().newInputStream(XMLBeansHelper.getXmlOptions());							
						}else if(requestedVersion.equals(WPSConfig.VERSION_200)){
							return CapabilitiesConfigurationV200.getInstance().newInputStream(XMLBeansHelper.getXmlOptions());	
						}
					}
				}
				
			}
			/* [OGC 06-121r9 OWS Common 2.0]:
			 * if no acceptVersions parameter was send, the highest supported version should be used
			 * WPS 2.0 in this case
			 */
			return CapabilitiesConfigurationV200.getInstance().newInputStream(XMLBeansHelper.getXmlOptions());
		} catch (IOException e) {
			throw new ExceptionReport("Exception occured while generating response", ExceptionReport.NO_APPLICABLE_CODE, e);
		} catch (XmlException e) {
			throw new ExceptionReport("Exception occured while generating response", ExceptionReport.NO_APPLICABLE_CODE, e);
		}
	}
}
