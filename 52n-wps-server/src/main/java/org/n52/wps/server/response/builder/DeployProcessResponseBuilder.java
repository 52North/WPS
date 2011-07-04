package org.n52.wps.server.response.builder;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import net.opengis.wps.x100.CancelResponseDocument;
import net.opengis.wps.x100.DeployProcessResponseDocument;
import net.opengis.wps.x100.StatusDocumentType;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.WebProcessingService;
import org.n52.wps.server.request.DeployProcessRequest;
import org.n52.wps.server.request.Request;

public class DeployProcessResponseBuilder {

	private DeployProcessRequest request;	
	private DeployProcessResponseDocument responseDom;

	private static Logger LOGGER = Logger
			.getLogger(GetStatusResponseBuilder.class);

	public DeployProcessResponseBuilder(DeployProcessRequest deployProcessRequest) {
		setRequest(deployProcessRequest);
		responseDom = DeployProcessResponseDocument.Factory.newInstance();
		responseDom.addNewDeployProcessResponse();
		XmlCursor c = responseDom.newCursor();
		c.toFirstChild();
		c.toLastAttribute();
		// TODO modify with future schema location
		c.setAttributeText(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation"), "http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsExecute_response.xsd");
		responseDom.getDeployProcessResponse().setLang(WebProcessingService.DEFAULT_LANGUAGE);
		responseDom.getDeployProcessResponse().setService("WPS");
		responseDom.getDeployProcessResponse().setVersion(Request.SUPPORTED_VERSION);
		responseDom.getDeployProcessResponse().addNewResult();
		responseDom.getDeployProcessResponse().getResult().setSuccess(true);
		responseDom.getDeployProcessResponse().addNewIdentifier().setStringValue(getRequest().getProcessID());
	}

	public void setRequest(DeployProcessRequest request) {
		this.request = request;
	}

	public DeployProcessRequest getRequest() {
		return request;
	}

	public void setResponseDom(DeployProcessResponseDocument responseDom) {
		this.responseDom = responseDom;
	}

	public DeployProcessResponseDocument getResponseDom() {
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
