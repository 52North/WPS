package org.n52.wps.server.algorithm.ows5;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;

import org.apache.axis.AxisFault;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.message.SOAPEnvelope;
import org.w3c.dom.Document;

public class SOAPClient {

	
	 public Call prepareCall(String endpoint, String op) {
	    	Call call = null;
	        try {
	            call = (Call) new Service().createCall();
	            call.setTargetEndpointAddress(endpoint);
	            call.setOperationName(new QName("", op));
	            
	        } catch (javax.xml.rpc.ServiceException e) {
	            e.printStackTrace(); 
	        }
	        return call;
	    }
	 
	 public SOAPEnvelope buildRequest(Document bodyDoc, Document headerDoc) {
			SOAPEnvelope reqEnvelope = new SOAPEnvelope();
	    	try {
	    		SOAPBody body = reqEnvelope.getBody();
	    		if (bodyDoc != null) body.addDocument(bodyDoc);
	    		SOAPHeader header = reqEnvelope.getHeader();
	    		if (headerDoc != null) header.appendChild(headerDoc.getFirstChild());
	    	} catch (Exception e) {
	    		e.printStackTrace(System.err);
	    	}
	    	return reqEnvelope;
	    	
	    }
	    
		public SOAPEnvelope genericCall(String url, String action, SOAPEnvelope reqEnvelope) throws AxisFault {

			Call call = prepareCall(url, action);
			return call.invoke(reqEnvelope);
		}	
			
		

}
