package org.n52.wps.server;


public interface ITransactionalAlgorithmRepository  {
	boolean addAlgorithm(Object className);
	boolean removeAlgorithm(Object className);
	
}
