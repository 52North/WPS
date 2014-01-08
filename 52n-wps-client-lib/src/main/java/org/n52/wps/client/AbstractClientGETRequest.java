/**
 * ﻿Copyright (C) 2007
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
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
