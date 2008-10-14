package org.n52.wps.transactional.response;

public class TransactionalResponse {
	private String message;

	public TransactionalResponse(String message) {
		super();
		this.message = message;
	}

	public String getMessage() {
		return "<Reponse>" + message + "</Reponse>";
		
		
	}
}
