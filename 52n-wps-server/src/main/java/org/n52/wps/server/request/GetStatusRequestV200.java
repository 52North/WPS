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
package org.n52.wps.server.request;

import java.io.IOException;

import net.opengis.wps.x20.StatusInfoDocument;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.xmlbeans.XmlException;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.database.DatabaseFactory;
import org.n52.wps.server.response.GetStatusResponseV200;
import org.n52.wps.server.response.Response;
import org.w3c.dom.Document;

public class GetStatusRequestV200 extends Request {

	private StatusInfoDocument document;
	
	private String jobID;
	
	public GetStatusRequestV200(CaseInsensitiveMap map) throws ExceptionReport {
		super(map);
		jobID = getMapValue("jobid", true);		
	}

	public GetStatusRequestV200(Document doc) throws ExceptionReport {
		super(doc);		
		jobID = getMapValue("jobid", true);	
	}

	@Override
	public Object getAttachedResult() {
		return document;
	}

	@Override
	public Response call() throws ExceptionReport {
		try {
			document = StatusInfoDocument.Factory.parse(DatabaseFactory.getDatabase().lookupStatus(jobID));
		} catch (XmlException | IOException e) {
			LOGGER.error("Could not parse StatusinfoDocument looked up in database.");
		}		
		
		return new GetStatusResponseV200(this);
	}

	@Override
	public boolean validate() throws ExceptionReport {		
		return true;
	}

}
