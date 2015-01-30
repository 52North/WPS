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

import java.io.InputStream;

import net.opengis.wps.x200.StatusInfoDocument;
import net.opengis.wps.x200.StatusInfoDocument.StatusInfo;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.io.IOUtils;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.database.DatabaseFactory;
import org.n52.wps.server.observerpattern.ISubject;
import org.n52.wps.server.response.ExecuteResponse;
import org.n52.wps.server.response.Response;
import org.w3c.dom.Document;

public class StatusRequestV200 extends Request {

	public StatusRequestV200(CaseInsensitiveMap map) throws ExceptionReport {
		super(map);
		// TODO Auto-generated constructor stub
	}

	public StatusRequestV200(Document doc) throws ExceptionReport {
		super(doc);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Object getAttachedResult() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response call() throws ExceptionReport {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean validate() throws ExceptionReport {
		// TODO Auto-generated method stub
		return false;
	}

	public void updateStatusAccepted() {
		StatusInfoDocument statusInfo = StatusInfoDocument.Factory
				.newInstance();

		StatusInfo status = statusInfo.addNewStatusInfo();
		status.setStatus("Process Accepted");
		updateStatus(statusInfo);
	}

	public void updateStatusStarted() {
		StatusInfoDocument statusInfo = StatusInfoDocument.Factory
				.newInstance();

		StatusInfo status = statusInfo.addNewStatusInfo();
		status.setPercentCompleted(0);
		updateStatus(statusInfo);
	}

	public void updateStatusSuccess() {
		StatusInfoDocument statusInfo = StatusInfoDocument.Factory
				.newInstance();

		StatusInfo status = statusInfo.addNewStatusInfo();
		status.setStatus("Process successful");
		updateStatus(statusInfo);
	}

	public void update(ISubject subject) {
		Object state = subject.getState();
		LOGGER.info("Update received from Subject, state changed to : " + state);

		StatusInfoDocument statusInfo = StatusInfoDocument.Factory
				.newInstance();

		StatusInfo status = statusInfo.addNewStatusInfo();

		int percentage = 0;
		if (state instanceof Integer) {
			percentage = (Integer) state;
			status.setPercentCompleted(percentage);
		} else if (state instanceof String) {
			status.setStatus((String) state);
		}
		updateStatus(statusInfo);
	}

	private void updateStatus(StatusInfoDocument statusInfo) {
		try {
			ExecuteResponse executeResponse = new ExecuteResponse(this);
			InputStream is = null;
			try {
				is = executeResponse.getAsStream();
				DatabaseFactory.getDatabase().storeResponse(
						getUniqueId().toString(), is);
			} finally {
				IOUtils.closeQuietly(is);
			}
		} catch (ExceptionReport e) {
			LOGGER.error("Update of process status failed.", e);
			throw new RuntimeException(e);
		}
	}

}
