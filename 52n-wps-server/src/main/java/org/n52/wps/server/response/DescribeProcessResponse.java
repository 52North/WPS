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

import java.io.InputStream;

import net.opengis.wps.x100.ProcessDescriptionsDocument;
import net.opengis.wps.x200.ProcessOfferingsDocument;

import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.request.Request;
import org.n52.wps.util.XMLBeansHelper;


public class DescribeProcessResponse extends Response{

	public DescribeProcessResponse(Request request){
		super(request);
	}
	
    @Override
	public InputStream getAsStream() throws ExceptionReport{
		try {
			//TODO change to Request.getMapValue
			String[] requestedVersions = (String[]) getRequest().getMap().get("version");
			
			if(requestedVersions != null && requestedVersions.length > 0){
				
				String requestedVersion = requestedVersions[0];
				
				if(requestedVersion.equals(WPSConfig.VERSION_100)){
					
					return ((ProcessDescriptionsDocument)request.getAttachedResult()).newInputStream(XMLBeansHelper.getXmlOptions());
					
				}else if(requestedVersion.equals(WPSConfig.VERSION_200)){
					
					return ((ProcessOfferingsDocument)request.getAttachedResult()).newInputStream(XMLBeansHelper.getXmlOptions());
					
				}
				
			}
		}
		catch(Exception e) {
			throw new ExceptionReport("Exception occured while writing response document", ExceptionReport.NO_APPLICABLE_CODE, e);
		}
		return null;
	}
}