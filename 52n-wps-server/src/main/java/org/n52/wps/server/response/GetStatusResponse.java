package org.n52.wps.server.response;

import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.database.DatabaseFactory;
import org.n52.wps.server.request.ExecuteRequest;
import org.n52.wps.server.request.GetStatusRequest;
import org.n52.wps.server.request.Request;
import org.n52.wps.server.response.builder.GetStatusResponseBuilder;

public class GetStatusResponse extends Response {

	private static Logger LOGGER = Logger.getLogger(ExecuteResponse.class);

	private GetStatusResponseBuilder builder;

	public GetStatusResponse(Request request) {
		super(request);
		setBuilder(((GetStatusRequest)this.request).getGetStatusRespBuilder());
	}

	@Override
	public void save(OutputStream os) throws ExceptionReport {
	getBuilder().save(os);
	}

	public void setBuilder(GetStatusResponseBuilder builder) {
		this.builder = builder;
	}

	public GetStatusResponseBuilder getBuilder() {
		return builder;
	}

}
