package org.n52.wps.server;

import java.util.HashMap;

import org.w3c.dom.Document;

public abstract class AbstractTransactionalAlgorithm implements IAlgorithm{

	
	protected String algorithmID;
	
	
	public AbstractTransactionalAlgorithm(String algorithmID){
		this.algorithmID = algorithmID;
		
	}

	public String getAlgorithmID() {
		return algorithmID;
	}
	
	public abstract HashMap run(Document document);
	
	
	
}
