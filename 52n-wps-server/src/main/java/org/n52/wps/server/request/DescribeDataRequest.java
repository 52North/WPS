/***************************************************************
 This implementation provides a framework to publish processes to the
web through the  OGC Web Dataing Service interface. The framework 
is extensible in terms of processes and data handlers. It is compliant 
to the WPS version 0.4.0 (OGC 05-007r4). 

 Copyright (C) 2006 by con terra GmbH

 Authors: 
	Theodor Foerster, ITC, Enschede, the Netherlands
	Carsten Priess, Institute for geoinformatics, University of
Muenster, Germany


 Contact: Albert Remke, con terra GmbH, Martin-Luther-King-Weg 24,
 48155 Muenster, Germany, 52n@conterra.de

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 version 2 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program (see gnu-gpl v2.txt); if not, write to
 the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA  02111-1307, USA or visit the web page of the Free
 Software Foundation, http://www.fsf.org.

 Created on: 13.06.2006
 ***************************************************************/
package org.n52.wps.server.request;



import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import net.opengis.wps.x100.DataDescriptionType;
import net.opengis.wps.x100.DataDescriptionsDocument;
import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.xmlbeans.XmlCursor;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.WebProcessingService;
import org.n52.wps.server.repository.RepositoryManager;
import org.n52.wps.server.response.DescribeDataResponse;
import org.n52.wps.server.response.Response;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Handles a DescribeDataRequest
 * @see Request  
 */
public class DescribeDataRequest extends Request {

	private DataDescriptionsDocument document;
	
	/**
	 * Creates a DescribeDataRequest based on a Map (HTTP_GET)
	 * @param ciMap The client input
	 * @throws ExceptionReport
	 */
	public DescribeDataRequest(CaseInsensitiveMap ciMap) throws ExceptionReport{
		super(ciMap);
	}
	
	/**
	 * Creates a DescribeDataRequest based on a Document (SOAP?)
	 * @param doc The client input
	 * @throws ExceptionReport
	 */
	public DescribeDataRequest(Document doc) throws ExceptionReport{
		super(doc);
		
		//put the respective elements of the document in the map
		NamedNodeMap nnm = doc.getFirstChild().getAttributes();
		
		map = new CaseInsensitiveMap();
		
		for (int i = 0; i < nnm.getLength(); i++) {
			
			Node n = nnm.item(i);
			if(n.getLocalName().equalsIgnoreCase("service")){
			map.put(n.getLocalName(), new String[]{n.getNodeValue()});
			}
		}
		//get identifier
		String identifierList = "";		
		
		NodeList nList = doc.getFirstChild().getChildNodes();
		
		for (int i = 0; i < nList.getLength(); i++) {
			Node n = nList.item(i);
			if(n.getLocalName() != null && n.getLocalName().equalsIgnoreCase("identifier")){
				String s = n.getTextContent();
				identifierList = identifierList.concat(s + ",");
			}
		}		
		map.put("identifier", new String[]{identifierList});
		
	}
	

	/**
	 * Validates the client input
	 * @throws ExceptionReport
	 * @return True if the input is valid, False otherwise
	 */
	public boolean validate() throws ExceptionReport{
		getMapValue("version", false); // not required?
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
		
		document = DataDescriptionsDocument.Factory.newInstance();
		document.addNewDataDescriptions();
		XmlCursor c = document.newCursor();
		c.toFirstChild();
		c.toLastAttribute();
		c.setAttributeText(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation"), "http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsDescribeData_response.xsd");
				
		String[] identifiers = getMapValue("identifier", true).split(",");
		document.getDataDescriptions().setLang(WebProcessingService.DEFAULT_LANGUAGE);
		document.getDataDescriptions().setService("WPS");
		document.getDataDescriptions().setVersion(Request.SUPPORTED_VERSION);
		for(String dataName : identifiers) {
			if(!RepositoryManager.getInstance().containsData(dataName)) {
				throw new ExceptionReport("Data does not exist: " + dataName, 
											ExceptionReport.INVALID_PARAMETER_VALUE, 
											"parameter: identifier | value: " + dataName);
			}
			DataDescriptionType description = RepositoryManager.getInstance().getDataDescription(dataName);
			document.getDataDescriptions().addNewDataDescription().set(description);
		}
		
		LOGGER.info("Handled Request successfully for: " + getMapValue("identifier", true));
		return new DescribeDataResponse(this);
	}

}
