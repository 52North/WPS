/***************************************************************
 This implementation provides a framework to publish processes to the
web through the  OGC Web Processing Service interface. The framework 
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

import net.opengis.wps.x100.ProcessDescriptionsDocument;
import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.xmlbeans.XmlCursor;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.RepositoryManager;
import org.n52.wps.server.WebProcessingService;
import org.n52.wps.server.response.DescribeProcessResponse;
import org.n52.wps.server.response.Response;

/**
 * Handles a DescribeProcessRequest
 * @see Request  
 */
public class DescribeProcessRequest extends Request {

	private ProcessDescriptionsDocument document;
	
	/**
	 * Creates a DescribeProcessRequest based on a Map (HTTP_GET)
	 * @param ciMap The client input
	 * @throws ExceptionReport
	 */
	public DescribeProcessRequest(CaseInsensitiveMap ciMap) throws ExceptionReport{
		super(ciMap);
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
		
		document = ProcessDescriptionsDocument.Factory.newInstance();
		document.addNewProcessDescriptions();
		XmlCursor c = document.newCursor();
		c.toFirstChild();
		c.toLastAttribute();
		c.setAttributeText(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation"), "http://www.opengis.net/wps/1.0.0 http://geoserver:8080/wps/schemas/wps/1.0.0/wpsDescribeProcess_response.xsd");
				
		String[] identifiers = getMapValue("identifier", true).split(",");
		document.getProcessDescriptions().setLang(WebProcessingService.DEFAULT_LANGUAGE);
		document.getProcessDescriptions().setService("WPS");
		document.getProcessDescriptions().setVersion(Request.SUPPORTED_VERSION);
		for(String algorithmName : identifiers) {
			if(!RepositoryManager.getInstance().containsAlgorithm(algorithmName)) {
				throw new ExceptionReport("Algorithm does not exist: " + algorithmName, 
											ExceptionReport.INVALID_PARAMETER_VALUE, 
											"parameter: identifier | value: " + algorithmName);
			}
			IAlgorithm algorithm = RepositoryManager.getInstance().getAlgorithm(algorithmName);
			document.getProcessDescriptions().addNewProcessDescription().set(algorithm.getDescription());
		}
		
		LOGGER.info("Handled Request successfully for: " + getMapValue("identifier", true));
		return new DescribeProcessResponse(this);
	}

}
