package org.n52.wps.server;


import java.util.Collection;

public interface IAlgorithmRepository {


	
	
	
	Collection<String> getAlgorithmNames();
	
	IAlgorithm getAlgorithm(String processID);
	Collection<IAlgorithm> getAlgorithms();
	
	boolean containsAlgorithm(String processID);
	
	


	

}
