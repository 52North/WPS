package org.n52.wps.server.profiles.java;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.n52.wps.io.data.IData;
import org.n52.wps.server.ITransactionalAlgorithm;

public abstract class JavaTransactionalAlgorithm implements ITransactionalAlgorithm {

	private String instanceId;
	
	
	

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData)
			throws InterruptedException  {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAudit() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	
	public abstract Class getInputDataType(String id);
	public abstract Class getOutputDataType(String id);

}
