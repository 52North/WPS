package org.n52.wps.server.response;

import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.request.DeployDataRequest;
import org.n52.wps.server.request.Request;
import org.n52.wps.server.response.builder.DeployDataResponseBuilder;

public class DeployDataResponse extends Response {

	private static Logger LOGGER = Logger.getLogger(ExecuteResponse.class);
	private DeployDataResponseBuilder builder;

	
	public DeployDataResponse(Request request) {
		super(request);
		setBuilder(((DeployDataRequest)this.request).getDeployDataRespBuilder());
	}

	@Override
	public void save(OutputStream os) throws ExceptionReport {
		getBuilder().save(os);
	}

	public void setBuilder(DeployDataResponseBuilder deployDataResponseBuilder) {
		this.builder = deployDataResponseBuilder;
	}

	public DeployDataResponseBuilder getBuilder() {
		return builder;
	}

}
