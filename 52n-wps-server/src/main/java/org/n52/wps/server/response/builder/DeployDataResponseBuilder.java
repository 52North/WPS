package org.n52.wps.server.response.builder;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import net.opengis.wps.x100.CancelResponseDocument;
import net.opengis.wps.x100.DeployDataResponseDocument;
import net.opengis.wps.x100.StatusDocumentType;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.WebProcessingService;
import org.n52.wps.server.request.DeployDataRequest;
import org.n52.wps.server.request.Request;

public class DeployDataResponseBuilder {

	private DeployDataRequest request;	
	private DeployDataResponseDocument responseDom;

	private static Logger LOGGER = Logger
			.getLogger(GetStatusResponseBuilder.class);

	public DeployDataResponseBuilder(DeployDataRequest deployDataRequest) {
		setRequest(deployDataRequest);
		responseDom = DeployDataResponseDocument.Factory.newInstance();
		responseDom.addNewDeployDataResponse();
		XmlCursor c = responseDom.newCursor();
		c.toFirstChild();
		c.toLastAttribute();
		// TODO modify with future schema location
		c.setAttributeText(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation"), "http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsExecute_response.xsd");
		responseDom.getDeployDataResponse().setLang(WebProcessingService.DEFAULT_LANGUAGE);
		responseDom.getDeployDataResponse().setService("WPS");
		responseDom.getDeployDataResponse().setVersion(Request.SUPPORTED_VERSION);
		responseDom.getDeployDataResponse().addNewResult();
		responseDom.getDeployDataResponse().getResult().setSuccess(true);
		responseDom.getDeployDataResponse().addNewIdentifier().setStringValue(getRequest().getDataID());
	}

	public void setRequest(DeployDataRequest deployDataRequest) {
		this.request = deployDataRequest;
	}

	public DeployDataRequest getRequest() {
		return request;
	}

	public void setResponseDom(DeployDataResponseDocument responseDom) {
		this.responseDom = responseDom;
	}

	public DeployDataResponseDocument getResponseDom() {
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
