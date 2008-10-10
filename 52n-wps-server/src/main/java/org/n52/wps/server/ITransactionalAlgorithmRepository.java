package org.n52.wps.server;

public interface ITransactionalAlgorithmRepository extends IAlgorithmRepository{
	boolean addAlgorithm(Object className);
	boolean removeAlgorithm(Object className);
}
