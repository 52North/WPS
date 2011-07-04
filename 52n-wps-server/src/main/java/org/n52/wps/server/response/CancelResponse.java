package org.n52.wps.server.response;

import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.request.CancelRequest;
import org.n52.wps.server.request.GetStatusRequest;
import org.n52.wps.server.request.Request;
import org.n52.wps.server.response.builder.CancelResponseBuilder;
import org.n52.wps.server.response.builder.GetStatusResponseBuilder;

public class CancelResponse extends Response {

	private static Logger LOGGER = Logger.getLogger(ExecuteResponse.class);
	private CancelResponseBuilder builder;

	public CancelResponse(Request request) {
		super(request);
		setBuilder(((CancelRequest)this.request).getCancelRespBuilder());
	}

	@Override
	public void save(OutputStream os) throws ExceptionReport {
		getBuilder().save(os);

	}

	public void setBuilder(CancelResponseBuilder builder) {
		this.builder = builder;
	}

	public CancelResponseBuilder getBuilder() {
		return builder;
	}

}
