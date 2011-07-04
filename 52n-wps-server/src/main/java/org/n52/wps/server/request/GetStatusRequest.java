/**
 * WPS 2.0 draft (WPS-G change request) implementation
 * Authors : Christophe Noel, Spacebel, Belgium
 * Date : June 2011
 * Email: Christophe.Noel AT Spacebel.be
 */
package org.n52.wps.server.request;

import java.util.Map;

import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.GetStatusDocument;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.io.data.IData;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.response.ExecuteResponseBuilder;
import org.n52.wps.server.response.GetStatusResponse;
import org.n52.wps.server.response.Response;
import org.n52.wps.server.response.builder.GetStatusResponseBuilder;
import org.w3c.dom.Document;

public class GetStatusRequest extends Request {

	private static Logger LOGGER = Logger.getLogger(GetStatusRequest.class);
	private GetStatusDocument statusDom;
	private GetStatusResponseBuilder getStatusRespBuilder;

	// not implemented yet (HTTP Get)
	public GetStatusRequest(CaseInsensitiveMap map) throws ExceptionReport {
		super(map);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Create a GetStatus Request based on Document (HTTP Post)
	 * @param doc The request submitted
	 * @throws ExceptionReport
	 */
	public GetStatusRequest(Document doc) throws ExceptionReport {
		super(doc);
		try {
			/** 
			 * XMLBeans option : the underlying xml text buffer is trimmed immediately
			 * after parsing a document resulting in a smaller memory footprint.
			 */
			XmlOptions option = new XmlOptions();
			option.setLoadTrimTextBuffer();
			this.statusDom = GetStatusDocument.Factory.parse(doc, option);
			if (this.statusDom == null) {
				LOGGER.fatal("GetStatusDocument is null");
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
		setGetStatusRespBuilder(new GetStatusResponseBuilder(this));
	}

	public GetStatusDocument getStatusDom() {
		return statusDom;
	}

	public void setStatusDom(GetStatusDocument statusDom) {
		this.statusDom = statusDom;
	}

	@Override
	public Object getAttachedResult() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response call() throws ExceptionReport {
		return new GetStatusResponse(this);
	}

	
	@Override
	/**
	 * Not implemented yet
	 */
	public boolean validate() throws ExceptionReport {
		// TODO Auto-generated method stub, return always true.
		return true;
	}

	public void setGetStatusRespBuilder(GetStatusResponseBuilder getStatusRespBuilder) {
		this.getStatusRespBuilder = getStatusRespBuilder;
	}

	public GetStatusResponseBuilder getGetStatusRespBuilder() {
		return getStatusRespBuilder;
	}

}
