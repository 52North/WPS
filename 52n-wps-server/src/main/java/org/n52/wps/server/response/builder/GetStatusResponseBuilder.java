package org.n52.wps.server.response.builder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.GetStatusResponseDocument;
import net.opengis.wps.x100.StatusDocumentType;
import net.opengis.wps.x100.StatusDocumentType.ProcessOutputs;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.WebProcessingService;
import org.n52.wps.server.database.DatabaseFactory;
import org.n52.wps.server.request.GetStatusRequest;
import org.n52.wps.server.request.Request;

public class GetStatusResponseBuilder {

	private GetStatusRequest request;	
	private GetStatusResponseDocument responseDom;
	private ExecuteResponseDocument execDom;

	private static Logger LOGGER = Logger
			.getLogger(GetStatusResponseBuilder.class);

	public GetStatusResponseBuilder(GetStatusRequest getStatusRequest)
			throws ExceptionReport {
		/**
		 * Main steps:
		 *  1. Get the process instance id
		 *  2. Get the stored ExecuteResponse document (and parse) 
		 *  3. Build the GetStatusResponseDocument
		 *  4. Copy ExecuteResponse children to StatusDocument
		 */
		// 1. Get the process instance identifier from the GetStatus request
		String pii = getStatusRequest.getStatusDom().getGetStatus()
				.getProcessInstanceIdentifier().getInstanceId();
		// 2. Get the stored file (ExecuteResponse) thanks to the database
		try {
			XmlOptions option = new XmlOptions();
			option.setLoadTrimTextBuffer();
			InputStream execStream = DatabaseFactory.getDatabase()
					.lookupResponse(pii);
			 execDom = ExecuteResponseDocument.Factory
					.parse(execStream, option);
			if (execDom == null) {
				LOGGER.fatal("GetStatus cannot read file");
				throw new ExceptionReport(
						"Error while retrieve status document (parsing failed)",
						ExceptionReport.NO_APPLICABLE_CODE);
			}
		} catch (IOException e) {
			throw new ExceptionReport(
					"Error while retrieve status document (parsing failed)",
					ExceptionReport.NO_APPLICABLE_CODE, e);
		} catch (XmlException e) {
			throw new ExceptionReport(
					"Error while retrieve status document (parsing failed)",
					ExceptionReport.NO_APPLICABLE_CODE, e);
		}
		// 3. create Response
		responseDom = GetStatusResponseDocument.Factory.newInstance();
		responseDom.addNewGetStatusResponse();
		if(!WPSConfig.getInstance().isRemoveschemalocation()) {
			
		XmlCursor c = responseDom.newCursor();
		c.toFirstChild();
		c.toLastAttribute();
		// TODO modify with future schema location
		c.setAttributeText(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation"), "http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsExecute_response.xsd");
		}
		// ??????? doc.getExecuteResponse().setServiceInstance(CapabilitiesConfiguration.ENDPOINT_URL+"?REQUEST=GetCapabilities&SERVICE=WPS");
		responseDom.getGetStatusResponse().setLang(WebProcessingService.DEFAULT_LANGUAGE);
		responseDom.getGetStatusResponse().setService("WPS");
		responseDom.getGetStatusResponse().setVersion(Request.SUPPORTED_VERSION);
		responseDom.getGetStatusResponse().addNewStatusDocument();
		StatusDocumentType statusDom = responseDom.getGetStatusResponse().getStatusDocument();
		ResponseBuilderUtils.fillStatusDocument(statusDom, execDom);
		}

	public void setResponseDom(GetStatusResponseDocument responseDom) {
		this.responseDom = responseDom;
	}

	public GetStatusResponseDocument getResponseDom() {
		return responseDom;
	}

	public void setRequest(GetStatusRequest request) {
		this.request = request;
	}

	public GetStatusRequest getRequest() {
		return request;
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
