package org.n52.wps.transactional.deploymentprofiles;

import org.w3c.dom.Node;

public abstract class DeploymentProfile {
	
	private Object payload;
	private String processID;
	
	

	public DeploymentProfile(Node payload, String processID){
		this.processID = processID;
		this.payload = payload;
	}

	public String getProcessID() {
		return processID;
	}

	public Object getPayload() {
		return payload;
	}

}
