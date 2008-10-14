package org.n52.wps.transactional.handler;

import org.n52.wps.server.ITransactionalAlgorithmRepository;
import org.n52.wps.transactional.request.DeployProcessRequest;
import org.n52.wps.transactional.request.ITransactionalRequest;
import org.n52.wps.transactional.request.UndeployProcessRequest;
import org.n52.wps.transactional.response.TransactionalResponse;

public class TransactionalRequestHandler {

	

	public static  TransactionalResponse handle(ITransactionalRequest request) {
		
		try {
			//TODO get repository class from config by comparing the supported schemas
			ITransactionalAlgorithmRepository repository = null; 
			//request.execute();
			 if (request instanceof DeployProcessRequest) {
				 boolean success = repository.addAlgorithm(request);
				 if(! success){
					 return new TransactionalResponse("Error. Could not deploy process"); 
				 }
				 return new TransactionalResponse("Process successfully deployed");
			 }
			 if (request instanceof UndeployProcessRequest) {
				 boolean success = repository.removeAlgorithm(request);
				 if(! success){
					 return new TransactionalResponse("Error. Could not undeploy process"); 
				 }
				 return new TransactionalResponse("Process successfully undeployed");	
			 }
			
		} catch (Exception e) {
			e.printStackTrace();
			return new TransactionalResponse("Error = " +e.getMessage());
		}
		return new TransactionalResponse("");
		
		
	}

}
