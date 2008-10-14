package org.n52.wps.transactional.deploy;

import java.util.Collection;
import java.util.Map;

import net.opengis.wps.x100.ExecuteDocument;

import org.n52.wps.transactional.request.DeployProcessRequest;
import org.n52.wps.transactional.request.UndeployProcessRequest;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public interface IDeployManager {

	
	boolean unDeployProcess(UndeployProcessRequest request) throws Exception;
	boolean containsProcess(String processID);
	Collection<String> getAllProcesses();
	Document invoke(ExecuteDocument payload, String algorithmID);
	void deployProcess(DeployProcessRequest request) throws Exception;
	
}
