package org.n52.wps.transactional.deploy;

import org.n52.wps.server.ITransactionalAlgorithmRepository;

public abstract class AbstractDeployManager implements IDeployManager{
	private ITransactionalAlgorithmRepository parentRepository;
	
	public AbstractDeployManager(ITransactionalAlgorithmRepository parentRepository){
		this.parentRepository = parentRepository;
	}
		
	
}
