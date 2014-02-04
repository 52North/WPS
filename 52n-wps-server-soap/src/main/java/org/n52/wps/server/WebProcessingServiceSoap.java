/**
 * ﻿Copyright (C) 2007 - 2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *       • Apache License, version 2.0
 *       • Apache Software License, version 1.0
 *       • GNU Lesser General Public License, version 3
 *       • Mozilla Public License, versions 1.0, 1.1 and 2.0
 *       • Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.wps.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axiom.soap.SOAPFaultText;
import org.apache.axiom.soap.SOAPFaultValue;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.util.XMLUtils;
import org.n52.wps.server.handler.SOAPRequestHandler;
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

	public OMElement cancelJob(OMElement input) throws Exception {
		return internalSOAPHandler(input);
	}

	public OMElement getJobs(OMElement input) throws Exception {
		return internalSOAPHandler(input);
	}

	public OMElement pauseJob(OMElement input) throws Exception {
		return internalSOAPHandler(input);
	}

	public OMElement resumeJob(OMElement input) throws Exception {
		return internalSOAPHandler(input);
	}

	public OMElement getCapabilities(OMElement input) throws Exception{
		return internalSOAPHandler(input);
	}

	public OMElement describeProcess(OMElement input) throws Exception {
		return internalSOAPHandler(input);
	}
	
	
	
	private OMElement internalSOAPHandler(OMElement input) throws IOException, XMLStreamException{
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
			serviceException.getExceptionDocument().save(outputStream);
			InputStream instream = new ByteArrayInputStream(outputStream
					.toByteArray());
			OMElement omException = (OMElement) XMLUtils.toOM(instream);
			
			SOAPFactory soapFactory;
			
			if (m_msgCtx.isSOAP11()) {
			soapFactory = OMAbstractFactory.getSOAP11Factory();
		
			} else {
			soapFactory = OMAbstractFactory.getSOAP12Factory();
			
			}
			SOAPFault soapFault = soapFactory.createSOAPFault();
			
			SOAPFaultCode soapFaultCode = soapFactory.createSOAPFaultCode();
			SOAPFaultValue soapFaultValue = soapFactory.createSOAPFaultValue(soapFaultCode);
			soapFaultValue.setText(new QName("http://52north.org", "WPS Fault", "WPS fault"));

			SOAPFaultReason soapFaultReason = soapFactory.createSOAPFaultReason();
			SOAPFaultText soapFaultText = soapFactory.createSOAPFaultText(soapFaultReason);
			soapFaultText.setText(serviceException.getMessage());

			SOAPFaultDetail soapFaultDetail = soapFactory.createSOAPFaultDetail();
			QName qName = new QName("http://www.opengis.net/ows/1.1", "ExceptionReport");
			OMElement detail = soapFactory.createOMElement(qName, soapFaultDetail);
			detail.addChild(omException);
			
			soapFault.setDetail(soapFaultDetail);
			soapFault.setCode(soapFaultCode);
			soapFault.setReason(soapFaultReason);
			        
			m_msgCtx.setProperty(SOAP12Constants.SOAP_FAULT_CODE_LOCAL_NAME, soapFaultCode);
			m_msgCtx.setProperty(SOAP12Constants.SOAP_FAULT_REASON_LOCAL_NAME, soapFaultReason);
			m_msgCtx.setProperty(SOAP12Constants.SOAP_FAULT_DETAIL_LOCAL_NAME, soapFaultDetail);
			
			throw new AxisFault(soapFault);
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
