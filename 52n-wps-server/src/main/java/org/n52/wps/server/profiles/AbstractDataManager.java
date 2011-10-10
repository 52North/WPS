package org.n52.wps.server.profiles;

import org.n52.wps.server.repository.DefaultTransactionalDataRepository;

public abstract class AbstractDataManager implements IDataManager{
	protected DefaultTransactionalDataRepository parentRepository;
	
	public AbstractDataManager(DefaultTransactionalDataRepository parentRepository){
		this.parentRepository = parentRepository;
	}
		
	
}
