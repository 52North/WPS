package org.n52.wps.client;


public class ClientCapabiltiesRequest extends AbstractClientGETRequest {

	private String REQUEST_REQ_PARAM_VALUE = "GetCapabilities";
	
	public ClientCapabiltiesRequest() {
		super();
		setRequestParamValue(REQUEST_REQ_PARAM_VALUE);
	}
	
	public boolean valid() {
		return true;
	}

}
