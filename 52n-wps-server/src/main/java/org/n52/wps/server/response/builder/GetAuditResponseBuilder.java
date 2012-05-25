package org.n52.wps.server.response.builder;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import net.opengis.wps.x100.GetAuditResponseDocument;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.WebProcessingService;
import org.n52.wps.server.request.GetAuditRequest;
import org.n52.wps.server.request.Request;

public class GetAuditResponseBuilder {

	private GetAuditRequest cancelRequest;	
	private GetAuditResponseDocument responseDom;
	
	private static Logger LOGGER = Logger
			.getLogger(GetAuditResponseBuilder.class);

	public GetAuditResponseBuilder(GetAuditRequest getAuditRequest) {
		XmlOptions option = new XmlOptions();
		option.setLoadTrimTextBuffer();
		responseDom = GetAuditResponseDocument.Factory.newInstance();
		responseDom.addNewGetAuditResponse();
		if(!WPSConfig.getInstance().isRemoveschemalocation()) {
			
		XmlCursor c = responseDom.newCursor();
		c.toFirstChild();
		c.toLastAttribute();
		// TODO modify with future schema location
		c.setAttributeText(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation"), "http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsExecute_response.xsd");
		}
		responseDom.getGetAuditResponse().setLang(WebProcessingService.DEFAULT_LANGUAGE);
		responseDom.getGetAuditResponse().setService("WPS");
		responseDom.getGetAuditResponse().setVersion(Request.SUPPORTED_VERSION);
		responseDom.getGetAuditResponse().addNewResult();
		responseDom.getGetAuditResponse().getResult().setSuccess(true);
		responseDom.getGetAuditResponse().addNewProcessInstanceIdentifier().setInstanceId(getAuditRequest.getGetAuditDom().getGetAudit().getProcessInstanceIdentifier().getInstanceId());
		//LOGGER.info(getAuditRequest.getAuditTrace().toString());
		responseDom.getGetAuditResponse().setAuditTrace(getAuditRequest.getAuditTrace());
		
		// TODO set audit trace
	
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
	public void setRequest(GetAuditRequest request) {
		this.cancelRequest = request;
	}
	public GetAuditRequest getRequest() {
		return this.cancelRequest;
	}
	public void setResponseDom(GetAuditResponseDocument responseDom) {
		this.responseDom = responseDom;
	}
	public GetAuditResponseDocument getResponseDom() {
		return responseDom;
	}

}
