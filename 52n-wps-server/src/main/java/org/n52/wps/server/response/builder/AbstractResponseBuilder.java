package org.n52.wps.server.response.builder;

import java.io.OutputStream;
import java.util.Calendar;

import net.opengis.wps.x100.DataInputsType;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.ResponseBaseType;
import net.opengis.wps.x100.StatusType;

import org.apache.xmlbeans.XmlObject;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.WebProcessingService;
import org.n52.wps.server.request.ExecuteRequest;
import org.n52.wps.server.request.Request;


public abstract class AbstractResponseBuilder {

	private String identifier;
	private DataInputsType dataInputs;
	// private DocumentOutputDefinitionType[] outputDefs;
	protected Request request;
	protected XmlObject doc;
	private Calendar creationTime;

	public AbstractResponseBuilder(Request request) throws ExceptionReport {
		this.request = request;
		initizialiseDocument();
		creationTime = Calendar.getInstance();
	}

	private void initizialiseDocument() {
		if (request instanceof ExecuteRequest) {
			doc = ExecuteResponseDocument.Factory.newInstance();
			ExecuteResponseDocument doc =(ExecuteResponseDocument) this.doc;
			ResponseBaseType basetype  = doc.addNewExecuteResponse();
			setCommonValues(basetype);
		} 
		
	}

	public abstract void setStatus(StatusType status);

	public abstract void save(OutputStream os) throws ExceptionReport;

	public abstract void update() throws ExceptionReport;

	public abstract String getMimeType();

	private void setCommonValues(ResponseBaseType basetype) {
		basetype.setLang(WebProcessingService.DEFAULT_LANGUAGE);
		basetype.setVersion(Request.SUPPORTED_VERSION);
		basetype.setService("WPS");

	}
}
