package org.n52.wps.server.response.builder;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import net.opengis.wps.x100.CancelDocument;
import net.opengis.wps.x100.CancelResponseDocument;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.GetStatusResponseDocument;
import net.opengis.wps.x100.StatusDocumentType;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.WebProcessingService;
import org.n52.wps.server.request.CancelRequest;
import org.n52.wps.server.request.GetStatusRequest;
import org.n52.wps.server.request.Request;

public class CancelResponseBuilder {

	private CancelRequest cancelRequest;	
	private CancelResponseDocument responseDom;
	private ExecuteResponseDocument execDom;

	private static Logger LOGGER = Logger
			.getLogger(GetStatusResponseBuilder.class);

	public CancelResponseBuilder(CancelRequest cancelRequest) {
		XmlOptions option = new XmlOptions();
		option.setLoadTrimTextBuffer();
		this.cancelRequest = cancelRequest;
		execDom = cancelRequest.getDoc();
		String pii = execDom.getExecuteResponse().getProcessInstanceIdentifier().getInstanceId();
		responseDom = CancelResponseDocument.Factory.newInstance();
		responseDom.addNewCancelResponse();
		if(!WPSConfig.getInstance().isRemoveschemalocation()) {
			
		XmlCursor c = responseDom.newCursor();
		c.toFirstChild();
		c.toLastAttribute();
		// TODO modify with future schema location
		c.setAttributeText(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation"), "http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsExecute_response.xsd");
		}
		responseDom.getCancelResponse().setLang(WebProcessingService.DEFAULT_LANGUAGE);
		responseDom.getCancelResponse().setService("WPS");
		responseDom.getCancelResponse().setVersion(Request.SUPPORTED_VERSION);
		responseDom.getCancelResponse().addNewResult();
		responseDom.getCancelResponse().getResult().setSuccess(true);
		responseDom.getCancelResponse().addNewStatusDocument();
		StatusDocumentType statusDom = responseDom.getCancelResponse().getStatusDocument();
		ResponseBuilderUtils.fillStatusDocument(statusDom, execDom);
	}
	public void save(OutputStream os) throws ExceptionReport {
		try {
			//Forces XMLBeans to write the namespaces in front of all other attributes. Otherwise the xml is not valid
			XmlOptions opts = new XmlOptions();
			opts.setSaveNamespacesFirst();
			getResponseDom().save(os,opts);
		}
		catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	public void setRequest(CancelRequest request) {
		this.cancelRequest = request;
	}
	public CancelRequest getRequest() {
		return this.cancelRequest;
	}
	public void setResponseDom(CancelResponseDocument responseDom) {
		this.responseDom = responseDom;
	}
	public CancelResponseDocument getResponseDom() {
		return responseDom;
	}

}
