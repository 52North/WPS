package org.n52.wps.server.request;

import net.opengis.wps.x100.CancelDocument;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.GetStatusDocument;
import net.opengis.wps.x100.StatusType;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.server.AbstractTransactionalAlgorithm;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.handler.WPSTask;
import org.n52.wps.server.response.CancelResponse;
import org.n52.wps.server.response.ExecuteResponse;
import org.n52.wps.server.response.Response;
import org.n52.wps.server.response.builder.CancelResponseBuilder;
import org.n52.wps.server.response.builder.GetStatusResponseBuilder;
import org.w3c.dom.Document;

public class CancelRequest extends Request {

	private static Logger LOGGER = Logger.getLogger(CancelRequest.class);
	private CancelDocument cancelDom;
	private CancelResponseBuilder cancelRespBuilder;
	private WPSTask<Response> task;

	// not implemented yet (HTTP GET)
	public CancelRequest(CaseInsensitiveMap map) throws ExceptionReport {
		super(map);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Create a Cancel Request based on Document (HTTP Post)
	 * 
	 * @param doc
	 * @throws ExceptionReport
	 */
	public CancelRequest(Document doc) throws ExceptionReport {
		super(doc);
		try {
			/**
			 * XMLBeans option : the underlying xml text buffer is trimmed
			 * immediately after parsing a document resulting in a smaller
			 * memory footprint.
			 */
			XmlOptions option = new XmlOptions();
			option.setLoadTrimTextBuffer();
			this.cancelDom = CancelDocument.Factory.parse(doc, option);
			if (this.cancelDom == null) {
				LOGGER.fatal("CancelDocument is null");
				throw new ExceptionReport("Error while parsing post data",
						ExceptionReport.MISSING_PARAMETER_VALUE);
			}
		} catch (XmlException e) {
			throw new ExceptionReport("Error while parsing post data",
					ExceptionReport.MISSING_PARAMETER_VALUE, e);
		}
		// Validate the request
		validate();
		// create an initial response
	}

	public CancelDocument getCancelDom() {
		return cancelDom;
	}

	public void setCancelDom(CancelDocument cancelDom) {
		this.cancelDom = cancelDom;
	}

	@Override
	public Object getAttachedResult() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response call() throws ExceptionReport {
		LOGGER.info("Cancel call()");
		if (getTask() == null) {
			// TODO check if ExecuteResponse document available (failed,
			// cancelled) to be more precised
			LOGGER.info("task doesn't exist");
			throw new ExceptionReport(
					"The process instance identifier is not valid. The taks may have been already cancelled.",
					ExceptionReport.INVALID_TASKID);
		}
		try {
			// If a task is already started, the backend should also
			// cancel the task
			if (getTaskStatus().isSetProcessStarted()) {
				LOGGER.info("Process is started --> cancel");
				if (getTask().getRequest().getAlgorithm() instanceof AbstractTransactionalAlgorithm) {
					((AbstractTransactionalAlgorithm) getTask().getRequest()
							.getAlgorithm()).cancel();
				}
				// then cancel the task (the previous step generated an
				// Exception if a problem occured)
				LOGGER.info("Doing Process Cancellation");
				getTask().cancel(true);
				if (getTask().isCancelled()) {
					LOGGER.info("ProcessCancelled Yes");
				}
				// update database (status file) with new status cancelled
				// TODO replace with ProcessCancelled
				getTaskStatus().unsetProcessStarted();
				getTaskStatus().setProcessCancelled("");
				ExecuteResponse resp = new ExecuteResponse(getTask()
						.getRequest());
			}
		} catch (Exception e) {
			LOGGER.info("Task cannot be cancelled");
			throw new ExceptionReport("The task cannot be cancelled.",
					ExceptionReport.CANCELLATION_FAILED);
		}
		// The CancelResponse is only returned if no exception occurs
		setCancelRespBuilder(new CancelResponseBuilder(this));
		return new CancelResponse(this);
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

	public void setCancelRespBuilder(CancelResponseBuilder cancelRespBuilder) {
		this.cancelRespBuilder = cancelRespBuilder;
	}

	public CancelResponseBuilder getCancelRespBuilder() {
		return cancelRespBuilder;
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

}
