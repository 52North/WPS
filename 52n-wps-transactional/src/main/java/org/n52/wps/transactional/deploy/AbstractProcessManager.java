package org.n52.wps.transactional.deploy;

import org.n52.wps.server.ITransactionalAlgorithmRepository;

public abstract class AbstractProcessManager implements IProcessManager{
	private ITransactionalAlgorithmRepository parentRepository;
	
	public AbstractProcessManager(ITransactionalAlgorithmRepository parentRepository){
		this.parentRepository = parentRepository;
	}
		
	
}
