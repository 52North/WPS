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
package org.n52.wps.server.request;

import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import net.opengis.wps.x20.ProcessOfferingDocument.ProcessOffering;
import net.opengis.wps.x20.ProcessOfferingsDocument;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.xmlbeans.XmlCursor;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.RepositoryManager;
import org.n52.wps.server.response.DescribeProcessResponse;
import org.n52.wps.server.response.Response;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Handles a DescribeProcessRequest
 * @see Request  
 */
public class DescribeProcessRequestV200 extends Request {

	private ProcessOfferingsDocument document;
	
	/**
	 * Creates a DescribeProcessRequest based on a Map (HTTP_GET)
	 * @param ciMap The client input
	 * @throws ExceptionReport
	 */
	public DescribeProcessRequestV200(CaseInsensitiveMap ciMap) throws ExceptionReport{
		super(ciMap);
	}
	
	/**
	 * Creates a DescribeProcessRequest based on a Document
	 * @param doc The client input
	 * @throws ExceptionReport
	 */
	public DescribeProcessRequestV200(Document doc) throws ExceptionReport{
		super(doc);
		
		//put the respective elements of the document in the map
		NamedNodeMap nnm = doc.getFirstChild().getAttributes();
		
		map = new CaseInsensitiveMap();
		
		for (int i = 0; i < nnm.getLength(); i++) {
			
			Node n = nnm.item(i);
			if(n.getLocalName().equalsIgnoreCase("service")){
			map.put(n.getLocalName(), new String[]{n.getNodeValue()});
			}else if(n.getLocalName().equalsIgnoreCase("version")){
				map.put(n.getLocalName(), new String[]{n.getNodeValue()});				
			}
		}
		//get identifier
		String identifierList = "";		
		
		NodeList nList = doc.getFirstChild().getChildNodes();
		
		boolean identifierParameterExists = false;
		
		for (int i = 0; i < nList.getLength(); i++) {
			Node n = nList.item(i);
			if(n.getLocalName() != null && n.getLocalName().equalsIgnoreCase("identifier")){
				identifierParameterExists = true;
				String s = n.getTextContent();
				if(s != null && !s.isEmpty()){
					identifierList = identifierList.concat(s + ",");
				}
			}
		}
		if(identifierParameterExists){
			map.put("identifier", new String[]{identifierList});
		}
	}
	

	/**
	 * Validates the client input
	 * @throws ExceptionReport
	 * @return True if the input is valid, False otherwise
	 */
	public boolean validate() throws ExceptionReport{
		getMapValue("version", true, new String[]{WPSConfig.VERSION_200}); // required		
		getMapValue("identifier", true);  // required!
		return true;
	}
	
	public Object getAttachedResult(){
		return document;
	}
	
	/**
	 * Actually serves the Request.
	 * @throws ExceptionReport
	 * @return Response The result of the computation
	 */
	public Response call() throws ExceptionReport {
		validate();
		
		document = ProcessOfferingsDocument.Factory.newInstance();
		document.addNewProcessOfferings();
		XmlCursor c = document.newCursor();
		c.toFirstChild();
		c.toLastAttribute();
		c.setAttributeText(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation"), "http://www.opengis.net/wps/2.0.0 http://schemas.opengis.net/wps/2.0.0/wpsDescribeProcess.xsd");
				
		String[] identifiers = getMapValue("identifier", true).split(",");
		
		if(identifiers.length==1 && identifiers[0].equalsIgnoreCase("all")){
			List<String> identifierList = RepositoryManager.getInstance().getAlgorithms();
			identifiers = new String[identifierList.size()];
			for(int i = 0;i<identifierList.size();i++){
				identifiers[i] = identifierList.get(i);
			}
		}
		if(identifiers.length == 1){
			if(identifiers[0] == null || identifiers[0].isEmpty()){
				throw new ExceptionReport("Process description request with empty identifier.", 
						ExceptionReport.INVALID_PARAMETER_VALUE, 
						"identifier");
			}
		}
		
		List<ProcessOffering> processOfferings = new ArrayList<>(identifiers.length);
		
		for(String algorithmName : identifiers) {
			if(!RepositoryManager.getInstance().containsAlgorithm(algorithmName)) {
				throw new ExceptionReport("Algorithm does not exist: " + algorithmName, 
											ExceptionReport.INVALID_PARAMETER_VALUE, 
											"identifier");
			}
			ProcessOffering offering = (ProcessOffering) RepositoryManager.getInstance().getProcessDescription(algorithmName).getProcessDescriptionType(WPSConfig.VERSION_200);
			processOfferings.add(offering);
		}
		document.getProcessOfferings().setProcessOfferingArray(processOfferings.toArray(new ProcessOffering[identifiers.length]));
		
		LOGGER.info("Handled Request successfully for: " + getMapValue("identifier", true));
		return new DescribeProcessResponse(this);
	}

}
