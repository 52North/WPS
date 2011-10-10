package org.n52.wps.server.response;

import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.request.UndeployDataRequest;
import org.n52.wps.server.request.Request;
import org.n52.wps.server.response.builder.UndeployDataResponseBuilder;

public class UndeployDataResponse extends Response {

	private static Logger LOGGER = Logger.getLogger(UndeployDataResponse.class);
	private UndeployDataResponseBuilder builder;

	
	public UndeployDataResponse(Request request) {
		super(request);
		setBuilder(((UndeployDataRequest)this.request).getUndeployDataRespBuilder());
	}

	@Override
	public void save(OutputStream os) throws ExceptionReport {
		getBuilder().save(os);
	}

	public void setBuilder(UndeployDataResponseBuilder undeployDataResponseBuilder) {
		this.builder = undeployDataResponseBuilder;
	}

	public UndeployDataResponseBuilder getBuilder() {
		return builder;
	}

}
