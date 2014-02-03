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

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.database.IDatabase;
import org.n52.wps.server.database.DatabaseFactory;
import org.n52.wps.server.response.Response;
import org.n52.wps.server.response.RetrieveResultResponse;

/**
 * If the server is too busy or the computation is too long,
 * the client can retrieve the result at a later time.
 * This class represents a Request to obtain the result.
 * The respons might be the result, be also could contain a
 * message that the result is not available yet.
 */
public class RetrieveResultRequest extends Request {

	private InputStream storedResponse = null;
	
	/**
	 * Create a Request based on a CaseInsensitiveMap as input (HTTP GET)
	 * @param ciMap The Map which holds the client input.
	 */
	public RetrieveResultRequest(CaseInsensitiveMap ciMap) throws ExceptionReport{
		super(ciMap);
	}
	
	/**
	 * Actually serves the Request.
	 * @throws ExceptionReport
	 */
	public Response call() throws ExceptionReport {
		if(validate()){
			return new RetrieveResultResponse(this);
		}
		return null;
	}

	/**
	 * Validates the client input
	 * @return True if the input is valid, False otherwise
	 */
	public boolean validate() throws ExceptionReport {
		String req_id = getMapValue("request_id", true);
		if(req_id.length() == 0){
			throw new ExceptionReport("The value of parameter <request_id> is not valid.", ExceptionReport.INVALID_PARAMETER_VALUE);
		}
		try{
		}catch(NumberFormatException e){
			throw new ExceptionReport("The value of parameter <request_id> is not an integer identifier", ExceptionReport.INVALID_PARAMETER_VALUE);
		}
		IDatabase db = DatabaseFactory.getDatabase();
		this.storedResponse = db.lookupResponse(req_id);
		return (this.storedResponse != null);
	}
	
	public Object getAttachedResult() throws NullPointerException {
		if(this.storedResponse == null)
			throw new NullPointerException("No stored responses were found!");
		return this.storedResponse;
	}

}
