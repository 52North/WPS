package org.n52.wps.server.response.builder;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import net.opengis.wps.x100.CancelResponseDocument;
import net.opengis.wps.x100.UndeployDataResponseDocument;
import net.opengis.wps.x100.StatusDocumentType;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.WebProcessingService;
import org.n52.wps.server.request.UndeployDataRequest;
import org.n52.wps.server.request.Request;

public class UndeployDataResponseBuilder {

	private UndeployDataRequest request;	
	private UndeployDataResponseDocument responseDom;

	private static Logger LOGGER = Logger
			.getLogger(UndeployDataResponseBuilder.class);

	public UndeployDataResponseBuilder(UndeployDataRequest undeployDataRequest) {
		setRequest(undeployDataRequest);
		responseDom = UndeployDataResponseDocument.Factory.newInstance();
		responseDom.addNewUndeployDataResponse();
		if(!WPSConfig.getInstance().isRemoveschemalocation()) {
		XmlCursor c = responseDom.newCursor();
		c.toFirstChild();
		c.toLastAttribute();
		// TODO modify with future schema location
		c.setAttributeText(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation"), "http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsExecute_response.xsd");
		}
		responseDom.getUndeployDataResponse().setLang(WebProcessingService.DEFAULT_LANGUAGE);
		responseDom.getUndeployDataResponse().setService("WPS");
		responseDom.getUndeployDataResponse().setVersion(Request.SUPPORTED_VERSION);
		responseDom.getUndeployDataResponse().addNewResult();
		responseDom.getUndeployDataResponse().getResult().setSuccess(true);
		responseDom.getUndeployDataResponse().addNewIdentifier().setStringValue(getRequest().getDataID());
	}

	public void setRequest(UndeployDataRequest request) {
		this.request = request;
	}

	public UndeployDataRequest getRequest() {
		return request;
	}

	public void setResponseDom(UndeployDataResponseDocument responseDom) {
		this.responseDom = responseDom;
	}

	public UndeployDataResponseDocument getResponseDom() {
		return responseDom;
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


}
