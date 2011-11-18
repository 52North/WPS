/**
 * WPS 2.0 draft (WPS-G change request) implementation
 * Authors : Christophe Noel, Spacebel, Belgium
 * Date : June 2011
 * Email: Christophe.Noel AT Spacebel.be
 */
package org.n52.wps.server.request;

import java.util.ArrayList;
import java.util.Map;

import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.GetStatusDocument;

import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.util.XMLUtils;
import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.io.data.IData;
import org.n52.wps.server.AbstractTransactionalAlgorithm;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.handler.WPSTask;
import org.n52.wps.server.response.CancelResponse;
import org.n52.wps.server.response.ExecuteResponse;
import org.n52.wps.server.response.ExecuteResponseBuilder;
import org.n52.wps.server.response.GetStatusResponse;
import org.n52.wps.server.response.Response;
import org.n52.wps.server.response.builder.CancelResponseBuilder;
import org.n52.wps.server.response.builder.GetStatusResponseBuilder;
import org.w3.x2005.x08.addressing.MessageIDDocument;
import org.w3.x2005.x08.addressing.RelatesToDocument;
import org.w3.x2005.x08.addressing.ReplyToDocument;
import org.w3c.dom.Document;

public class ExecuteCallback extends Request {

	private static Logger LOGGER = Logger.getLogger(ExecuteCallback.class);
	private ExecuteResponseDocument execRespDom;
	private SOAPHeader soapHeader;
	private String relatesTo;
	private WPSTask<Response> task;
	
	// not implemented yet (HTTP Get)
	public ExecuteCallback(CaseInsensitiveMap map) throws ExceptionReport {
		super(map);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Create a GetStatus Request based on Document (HTTP Post)
	 * @param doc The request submitted
	 * @throws ExceptionReport
	 */
	public ExecuteCallback(Document doc,SOAPHeader mySOAPHeader) throws ExceptionReport {
		super(doc);
		try {
			LOGGER.info("Execute Callback received");
			
			/** 
			 * XMLBeans option : the underlying xml text buffer is trimmed immediately
			 * after parsing a document resulting in a smaller memory footprint.
			 */
			XmlOptions option = new XmlOptions();
			option.setLoadTrimTextBuffer();
			this.execRespDom = ExecuteResponseDocument.Factory.parse(doc, option);
			LOGGER.info(execRespDom.toString());
			this.soapHeader = mySOAPHeader;
			if(mySOAPHeader==null) {
				LOGGER.info("soap is null here");
			}
			setRelatesTo(extractRelatesTo());
			
		} catch (XmlException e) {
			LOGGER.debug(e.getMessage());
		}
	}

	private String extractRelatesTo() {
		ArrayList<SOAPHeaderBlock> headerBlocks = this.soapHeader.
		getHeaderBlocksWithNSURI("http://www.w3.org/2005/08/addressing");
		RelatesToDocument relatesToBlock = null;
		for (SOAPHeaderBlock headerBlock : headerBlocks) {
			if (headerBlock.getLocalName().equals("RelatesTo")) {
				try {
					relatesToBlock = RelatesToDocument.Factory.parse(XMLUtils
							.toDOM(headerBlock));
					return relatesToBlock.getRelatesTo().getStringValue();
				} catch (XmlException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				LOGGER.info(relatesToBlock.toString());
			}
			
		}
		return null;
	}
	
	public  ExecuteResponseDocument getStatusDom() {
		return execRespDom;
	}


	public Response call(WPSTask<Response> task) throws ExceptionReport {
		setTask(task);
		return call();
	}


	private void setTask(WPSTask<Response> task) {
		this.task = task;
		
	}

	@Override
	public Response call() throws ExceptionReport {
		LOGGER.info("ExecuteCallback call()");
		if (getTask() == null) {
			// TODO check if ExecuteResponse document available (failed,
			// cancelled) to be more precised
			LOGGER.info("task doesn't exist");
			throw new ExceptionReport(
					"The process instance identifier is not valid. The taks may have been already cancelled.",
					ExceptionReport.INVALID_TASKID);
		}
		try {
				if (getTask().getRequest().getAlgorithm() instanceof AbstractTransactionalAlgorithm) {
					((AbstractTransactionalAlgorithm) getTask().getRequest()
							.getAlgorithm()).callback(this.execRespDom);
				}
				
			}
		 catch (Exception e) {
			LOGGER.info("Callback failed");
			throw new ExceptionReport("Callback failed.",
					ExceptionReport.CANCELLATION_FAILED);
		}
		// Nothing to return
		return null;

	}

	private WPSTask<Response> getTask() {
		// TODO Auto-generated method stub
		return this.task;
	}

	@Override
	public Object getAttachedResult() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean validate() throws ExceptionReport {
		// TODO Auto-generated method stub
		return false;
	}

	public void setRelatesTo(String relatesTo) {
		this.relatesTo = relatesTo;
	}

	public String getRelatesTo() {
		return relatesTo;
	}


}
