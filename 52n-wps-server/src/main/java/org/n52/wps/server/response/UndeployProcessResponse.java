package org.n52.wps.server.response;

import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.request.CancelRequest;
import org.n52.wps.server.request.UndeployProcessRequest;
import org.n52.wps.server.request.Request;
import org.n52.wps.server.response.builder.CancelResponseBuilder;
import org.n52.wps.server.response.builder.UndeployProcessResponseBuilder;

public class UndeployProcessResponse extends Response {

	private static Logger LOGGER = Logger.getLogger(UndeployProcessResponse.class);
	private UndeployProcessResponseBuilder builder;

	
	public UndeployProcessResponse(Request request) {
		super(request);
		setBuilder(((UndeployProcessRequest)this.request).getUndeployProcessRespBuilder());
	}

	@Override
	public void save(OutputStream os) throws ExceptionReport {
		getBuilder().save(os);
	}

	public void setBuilder(UndeployProcessResponseBuilder undeployProcessResponseBuilder) {
		this.builder = undeployProcessResponseBuilder;
	}

	public UndeployProcessResponseBuilder getBuilder() {
		return builder;
	}

}
