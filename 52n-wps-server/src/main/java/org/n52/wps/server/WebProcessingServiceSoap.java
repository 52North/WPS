package org.n52.wps.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axiom.soap.SOAPFaultValue;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.util.XMLUtils;
import org.apache.xmlbeans.impl.soap.SOAPException;
import org.n52.wps.server.handler.SOAPRequestHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class is the outwards interface for SOAP-requests
 * 
 * @author Kristof Lange
 *
 */

public class WebProcessingServiceSoap {

	private MessageContext m_msgCtx;

	public OMElement execute(OMElement input) throws Exception {
		return internalSOAPHandler(input);

	}

	/**
	 * Christophe Noël (Spacebel) - June 2011 : updated to WPS 2.0 operations
	 */
	public OMElement cancel(OMElement input) throws Exception {
		return internalSOAPHandler(input);
	}

	public OMElement getStatus(OMElement input) throws Exception {
		return internalSOAPHandler(input);
	}
	public OMElement getAudit(OMElement input) throws Exception {
		return internalSOAPHandler(input);
	}

	public OMElement deployProcess(OMElement input) throws Exception {
		return internalSOAPHandler(input);
	}

	public OMElement undeployProcess(OMElement input) throws Exception {
		return internalSOAPHandler(input);
	}
	public OMElement deployData(OMElement input) throws Exception {
		return internalSOAPHandler(input);
	}
	public OMElement undeployData(OMElement input) throws Exception {
		return internalSOAPHandler(input);
	}
	public OMElement describeData(OMElement input) throws Exception {
		return internalSOAPHandler(input);
	}
	public OMElement getCapabilities(OMElement input) throws Exception{
		return internalSOAPHandler(input);
	}

	public OMElement describeProcess(OMElement input) throws Exception {
		return internalSOAPHandler(input);
	}
	
	public OMElement executeResponseCallback(OMElement input) throws Exception {
		return internalSOAPHandler(input);
	}
	
	private OMElement internalSOAPHandler(OMElement input) throws Exception{
		m_msgCtx = MessageContext.getCurrentMessageContext();
		
		Element payload = null;
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ByteArrayInputStream bais=null;
		SOAPRequestHandler handler = null;
		Element inputDoc=null;
		

		/*convert OMElement to Element*/
		
		try{
		inputDoc = XMLUtils.toDOM(input);
		
	}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
				
			handler = new SOAPRequestHandler(inputDoc.getOwnerDocument(),outputStream);
			handler.handle();
			
			
		} catch (ExceptionReport serviceException) {

			OMElement faultElement = XMLUtils.toOM(((Document) serviceException.getExceptionDocument().getDomNode()).getDocumentElement());
		SOAPFault fault = OMAbstractFactory.getSOAP11Factory().createSOAPFault();
			SOAPFaultCode code = OMAbstractFactory.getSOAP11Factory().createSOAPFaultCode();
			code.setText(fault.getNamespace().getPrefix()+":Server");
			SOAPFaultReason faultstring = OMAbstractFactory.getSOAP11Factory().createSOAPFaultReason();
			faultstring.setText("WPS Fault");
			SOAPFaultDetail detail = OMAbstractFactory.getSOAP11Factory().createSOAPFaultDetail();
			detail.addChild(faultElement);
			fault.setCode(code);
			fault.setReason(faultstring);
			fault.setDetail(detail);
			return fault;
		}
		
		InputStream instream = new ByteArrayInputStream(outputStream
				.toByteArray());
		OMElement result = (OMElement) XMLUtils.toOM(instream);
		return result;
		
	}
	

//	 public static void writeXmlFile(Document doc, String filename) {
//	        try {
//	            // Prepare the DOM document for writing
//	            Source source = new DOMSource(doc);
//	    
//	            // Prepare the output file
//	            File file = new File(filename);
//	            Result result = new StreamResult(file);
//	    
//	            // Write the DOM document to the file
//	            Transformer xformer = TransformerFactory.newInstance().newTransformer();
//	            xformer.transform(source, result);
//	        } catch (TransformerConfigurationException e) {e.printStackTrace();
//	        } catch (TransformerException e) {e.printStackTrace();
//	        }
//	    }
}
