package org.n52.wps.transactional.request;

import org.w3c.dom.Document;

public class UndeployProcessRequest implements ITransactionalRequest{
	
	private String processID;
	
	public UndeployProcessRequest(Document request){
		//TODO parsing
	}

	public String getProcessID() {
		return processID;
	}
}
