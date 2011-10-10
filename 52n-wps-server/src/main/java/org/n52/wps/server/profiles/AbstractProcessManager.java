package org.n52.wps.server.profiles;

import org.n52.wps.server.repository.ITransactionalAlgorithmRepository;

public abstract class AbstractProcessManager implements IProcessManager{
	protected ITransactionalAlgorithmRepository parentRepository;
	
	public AbstractProcessManager(ITransactionalAlgorithmRepository parentRepository){
		this.parentRepository = parentRepository;
	}
		
	
}
