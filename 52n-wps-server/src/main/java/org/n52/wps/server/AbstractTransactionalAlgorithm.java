package org.n52.wps.server;


import java.util.Map;

import org.n52.wps.io.data.IData;

import net.opengis.wps.x100.ExecuteDocument;

public abstract class AbstractTransactionalAlgorithm implements IAlgorithm{

	
	protected String algorithmID;
	
	
	public AbstractTransactionalAlgorithm(String algorithmID){
		this.algorithmID = algorithmID;
		
	}

	public String getAlgorithmID() {
		return algorithmID;
	}
	
	public abstract Map<String, IData> run(ExecuteDocument document);
	
	
	
}
