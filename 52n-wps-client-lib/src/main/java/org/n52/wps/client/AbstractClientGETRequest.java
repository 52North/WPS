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
package org.n52.wps.client;


import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public abstract class AbstractClientGETRequest {
	protected Map<String, String> requestParams;
	
	private static String SERVICE_REQ_PARAM_NAME = "Service";
	private static String REQUEST_REQ_PARAM_NAME = "Request";
	private static String SERVICE_REQ_PARAM_VALUE = "WPS";
	private static String VERSION_REQ_PARAM_NAME = "version";
	private static String VERSION_REQ_PARAM_VALUE = "1.0.0";
	
	public AbstractClientGETRequest() {
		requestParams = new HashMap<String, String>();
		requestParams.put(SERVICE_REQ_PARAM_NAME, SERVICE_REQ_PARAM_VALUE);
		requestParams.put(VERSION_REQ_PARAM_NAME, VERSION_REQ_PARAM_VALUE);
	}
	
	protected void setRequestParamValue(String s) {
		requestParams.put(REQUEST_REQ_PARAM_NAME, s);
	}
	
	/** 
	 * adds to the url the designated parameter names and values, as configured before.
	 * @param url
	 * @return
	 */
	public String getRequest(String url) {
		if(! url.contains("?")) {
			url = url + "?";
		}
		for(Entry<String, String> entry : requestParams.entrySet()) {
			url = url + entry.getKey() + "=" + entry.getValue() + "&";
		}
		return url;
	}
	
	public abstract boolean valid();
}
