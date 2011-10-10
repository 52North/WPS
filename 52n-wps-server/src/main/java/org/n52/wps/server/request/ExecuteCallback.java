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
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.response.ExecuteResponseBuilder;
import org.n52.wps.server.response.GetStatusResponse;
import org.n52.wps.server.response.Response;
import org.n52.wps.server.response.builder.GetStatusResponseBuilder;
import org.w3.x2005.x08.addressing.MessageIDDocument;
import org.w3.x2005.x08.addressing.ReplyToDocument;
import org.w3c.dom.Document;

public class ExecuteCallback extends Request {

	private static Logger LOGGER = Logger.getLogger(ExecuteCallback.class);
	private ExecuteResponseDocument execRespDom;
	private SOAPHeader soapHeader;
	
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
			/** 
			 * XMLBeans option : the underlying xml text buffer is trimmed immediately
			 * after parsing a document resulting in a smaller memory footprint.
			 */
			XmlOptions option = new XmlOptions();
			option.setLoadTrimTextBuffer();
			this.execRespDom = ExecuteResponseDocument.Factory.parse(doc, option);
			this.soapHeader = mySOAPHeader;
			if(mySOAPHeader==null) {
				LOGGER.info("soap is null here");
			}
			
		} catch (XmlException e) {
			LOGGER.debug(e.getMessage());
		}
	}

	public  ExecuteResponseDocument getStatusDom() {
		return execRespDom;
	}




	@Override
	public Response call() throws ExceptionReport {
		ArrayList<SOAPHeaderBlock> headerBlocks = this.soapHeader.
		getHeaderBlocksWithNSURI("http://www.w3.org/2005/08/addressing");
		ReplyToDocument replyToBlock = null;
		MessageIDDocument messageIDBlock = null;
		for (SOAPHeaderBlock headerBlock : headerBlocks) {
			if (headerBlock.getLocalName().equals("ReplyTo")) {
				try {
					replyToBlock = ReplyToDocument.Factory.parse(XMLUtils
							.toDOM(headerBlock));
				} catch (XmlException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				LOGGER.info(replyToBlock.toString());
			}
			if (headerBlock.getLocalName().equals("MessageID")) {
				try {
					messageIDBlock = MessageIDDocument.Factory.parse(XMLUtils
							.toDOM(headerBlock));
				} catch (XmlException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				LOGGER.info(messageIDBlock.toString());
			}
		}
		return null;
		// TODO
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


}
