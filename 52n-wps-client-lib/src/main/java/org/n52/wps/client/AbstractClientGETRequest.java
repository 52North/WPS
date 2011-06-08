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
