package org.n52.wps.server;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.n52.wps.io.data.IData;

public interface ITransactionalAlgorithm {
	
	public void cancel();
	Map<String, IData> run(Map<String, List<IData>> inputData) throws InterruptedException;
	public String getAudit();
	
}
