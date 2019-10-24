/**
 * ﻿Copyright (C) 2007 - 2019 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
