package org.n52.wps.server;

import java.util.HashMap;

import net.opengis.wps.x100.ExecuteDocument;

public abstract class AbstractTransactionalAlgorithm implements IAlgorithm{

	
	protected String algorithmID;
	
	
	public AbstractTransactionalAlgorithm(String algorithmID){
		this.algorithmID = algorithmID;
		
	}

	public String getAlgorithmID() {
		return algorithmID;
	}
	
	public abstract HashMap run(ExecuteDocument document);
	
	
	
}
