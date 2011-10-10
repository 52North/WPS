package org.n52.wps.server.response;

import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.request.GetAuditRequest;
import org.n52.wps.server.request.Request;
import org.n52.wps.server.response.builder.GetAuditResponseBuilder;


public class GetAuditResponse extends Response {

	private static Logger LOGGER = Logger.getLogger(ExecuteResponse.class);
	private GetAuditResponseBuilder builder;

	public GetAuditResponse(Request request) {
		super(request);
		setBuilder(((GetAuditRequest)this.request).getGetAuditRespBuilder());
	}

	@Override
	public void save(OutputStream os) throws ExceptionReport {
		getBuilder().save(os);

	}

	public void setBuilder(GetAuditResponseBuilder builder) {
		this.builder = builder;
	}

	public GetAuditResponseBuilder getBuilder() {
		return builder;
	}

}
