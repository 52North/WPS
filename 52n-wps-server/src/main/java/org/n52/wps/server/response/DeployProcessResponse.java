package org.n52.wps.server.response;

import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.request.CancelRequest;
import org.n52.wps.server.request.DeployProcessRequest;
import org.n52.wps.server.request.Request;
import org.n52.wps.server.response.builder.CancelResponseBuilder;
import org.n52.wps.server.response.builder.DeployProcessResponseBuilder;

public class DeployProcessResponse extends Response {

	private static Logger LOGGER = Logger.getLogger(ExecuteResponse.class);
	private DeployProcessResponseBuilder builder;

	
	public DeployProcessResponse(Request request) {
		super(request);
		setBuilder(((DeployProcessRequest)this.request).getDeployProcessRespBuilder());
	}

	@Override
	public void save(OutputStream os) throws ExceptionReport {
		getBuilder().save(os);
	}

	public void setBuilder(DeployProcessResponseBuilder deployProcessResponseBuilder) {
		this.builder = deployProcessResponseBuilder;
	}

	public DeployProcessResponseBuilder getBuilder() {
		return builder;
	}

}
