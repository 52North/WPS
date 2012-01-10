package org.n52.wps.server.request;

import net.opengis.wps.x100.AuditTraceType;
import net.opengis.wps.x100.GetAuditDocument;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.StatusType;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.server.AbstractTransactionalAlgorithm;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.handler.WPSTask;
import org.n52.wps.server.response.GetAuditResponse;
import org.n52.wps.server.response.Response;
import org.n52.wps.server.response.builder.GetAuditResponseBuilder;
import org.w3c.dom.Document;

public class GetAuditRequest extends Request {

	private static Logger LOGGER = Logger.getLogger(GetAuditRequest.class);
	private GetAuditDocument getAuditDom;
	private GetAuditResponseBuilder getAuditRespBuilder;
	private WPSTask<Response> task;
	private AuditTraceType auditTrace;

	// not implemented yet (HTTP GET)
	public GetAuditRequest(CaseInsensitiveMap map) throws ExceptionReport {
		super(map);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Create a GetAudit Request based on Document (HTTP Post)
	 * 
	 * @param doc
	 * @throws ExceptionReport
	 */
	public GetAuditRequest(Document doc) throws ExceptionReport {
		super(doc);
		try {
			/**
			 * XMLBeans option : the underlying xml text buffer is trimmed
			 * immediately after parsing a document resulting in a smaller
			 * memory footprint.
			 */
			XmlOptions option = new XmlOptions();
			option.setLoadTrimTextBuffer();
			this.getAuditDom = GetAuditDocument.Factory.parse(doc, option);
			if (this.getAuditDom == null) {
				LOGGER.fatal("GetAuditDocument is null");
				throw new ExceptionReport("Error while parsing post data",
						ExceptionReport.MISSING_PARAMETER_VALUE);
			}
		} catch (XmlException e) {
			throw new ExceptionReport("Error while parsing post data",
					ExceptionReport.MISSING_PARAMETER_VALUE, e);
		}
		// Validate the request
		if(!this.getAuditDom.validate()) {
			throw new ExceptionReport("GetAudit request is not valid (according WPS schemas)",ExceptionReport.INVALID_PARAMETER_VALUE);
		}		// create an initial response
	}

	public GetAuditDocument getGetAuditDom() {
		return getAuditDom;
	}

	public void setGetAuditDom(GetAuditDocument getAuditDom) {
		this.getAuditDom = getAuditDom;
	}

	@Override
	public Object getAttachedResult() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response call() throws ExceptionReport {
		LOGGER.info("GetAudit call()");

		try {
			// If a task is already started, the backend should also
			// getAudit the task
			if (getTask() != null) {

				LOGGER.info("Process is started --> getAudit");
				if (getTask().getRequest().getAlgorithm() instanceof AbstractTransactionalAlgorithm) {
					if (getGetAuditDom().getGetAudit().getShortForm()) {
						LOGGER.info("GetShortForm");
						setAuditTrace(((AbstractTransactionalAlgorithm) getTask()
								.getRequest().getAlgorithm()).getAudit());
					} else {
						LOGGER.info("GetLonForm");
						setAuditTrace(((AbstractTransactionalAlgorithm) getTask()
								.getRequest().getAlgorithm())
								.getAuditLongForm());
					}
				}

			} else {
				LOGGER.info("Task is finished, cancelled, failed. Retrieving AuditDocument");
				if (getGetAuditDom().getGetAudit().getShortForm()) {
					LOGGER.info("GetShortForm");
					setAuditTrace(AbstractTransactionalAlgorithm
							.getAuditDocument(getGetAuditDom().getGetAudit()
									.getProcessInstanceIdentifier()
									.getInstanceId()));
				} else {
					setAuditTrace(AbstractTransactionalAlgorithm
							.getAuditLongDocument(getGetAuditDom().getGetAudit()
									.getProcessInstanceIdentifier()
									.getInstanceId()));
				}
			}
		} catch (Exception e) {
			LOGGER.info("Task cannot be getAuditled");
			throw new ExceptionReport("The task cannot be getAuditled.",
					ExceptionReport.CANCELLATION_FAILED);
		}
		// The GetAuditResponse is only returned if no exception occurs
		setGetAuditRespBuilder(new GetAuditResponseBuilder(this));
		return new GetAuditResponse(this);
	}

	private StatusType getTaskStatus() {
		return getTask().getRequest().getExecuteResponseBuilder().getDoc()
				.getExecuteResponse().getStatus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.n52.wps.server.request.Request#validate() TODO not implemented
	 * yet
	 */
	@Override
	public boolean validate() throws ExceptionReport {
		return true;
	}

	public void setGetAuditRespBuilder(
			GetAuditResponseBuilder getAuditRespBuilder) {
		this.getAuditRespBuilder = getAuditRespBuilder;
	}

	public GetAuditResponseBuilder getGetAuditRespBuilder() {
		return getAuditRespBuilder;
	}

	public Response call(WPSTask<Response> task) throws ExceptionReport {
		setTask(task);
		return call();
	}

	public void setTask(WPSTask<Response> task) {
		this.task = task;
	}

	public ExecuteResponseDocument getDoc() {
		return getTask().getRequest().getExecuteResponseBuilder().getDoc();
	}

	public WPSTask<Response> getTask() {
		return task;
	}

	public void setAuditTrace(AuditTraceType auditTrace) {
		this.auditTrace = auditTrace;
	}

	public AuditTraceType getAuditTrace() {
		return auditTrace;
	}

}
