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

import java.util.ArrayList;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.response.CapabilitiesResponse;
import org.n52.wps.server.response.Response;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Handles a CapabilitesRequest
 */
public class CapabilitiesRequest extends Request {
	
	/**
	 * Creates a CapabilitesRequest based on a Map (HTTP_GET)
	 * @param ciMap The client input
	 * @throws ExceptionReport
	 */
	public CapabilitiesRequest(CaseInsensitiveMap ciMap) throws ExceptionReport{
		super(ciMap);
		//Fix for https://bugzilla.52north.org/show_bug.cgi?id=907
		String providedAcceptVersionsString = Request.getMapValue("acceptversions", ciMap, false);
		
		if(providedAcceptVersionsString != null){
		
			String[] providedAcceptVersions = providedAcceptVersionsString.split(",");
			
			if(providedAcceptVersions != null){
				map.put("version", providedAcceptVersions);
			}
		}
	}

	public CapabilitiesRequest(Document doc) throws ExceptionReport{
		super(doc);
		
		map = new CaseInsensitiveMap();
		
		//TODO why is this done? (BenjaminPross)
//		String[] serviceArray = {"WPS"};
//			
//		map.put("service", serviceArray);						
		
		NodeList nList = doc.getFirstChild().getChildNodes();
		
		ArrayList<String> versionList = new ArrayList<String>();
		
		for (int i = 0; i < nList.getLength(); i++) {
			Node n = nList.item(i);
			if(n.getLocalName() != null){
				
				if(n.getLocalName().equalsIgnoreCase("AcceptVersions")){
					
					NodeList nList2 = n.getChildNodes();
					
					for (int j = 0; j < nList2.getLength(); j++) {
						Node n2 = nList2.item(j);
						
						if(n2.getLocalName() != null && n2.getLocalName().equalsIgnoreCase("Version")){
							versionList.add(n2.getTextContent());
						}
					}
					break;
				}
			}
		}
		
		if(!versionList.isEmpty()){			
			map.put("version", versionList.toArray(new String [versionList.size()]));	
		}	
		
	}
	
	/**
	 * Validates the client input
	 * @throws ExceptionReport
	 * @return True if the input is valid, False otherwise
	 */
	public boolean validate() throws ExceptionReport{
		
		String services = getMapValue("service", true);
		//Fix for Bug 904 https://bugzilla.52north.org/show_bug.cgi?id=904
		//TODO check, whether this can be removed as this is checked in RequestHandler
		if(! services.equalsIgnoreCase("wps")) {
			throw new ExceptionReport("Parameter <service> is not correct, expected: WPS, got: " + services, 
										ExceptionReport.INVALID_PARAMETER_VALUE, "service");
		}

		String[] versions = getMapArray("version", false);
		if(! requireVersion(SUPPORTED_VERSION, false)) {
				throw new ExceptionReport("Requested versions are not supported, you requested: " + Request.accumulateString(versions),
											ExceptionReport.VERSION_NEGOTIATION_FAILED, "version");
		}
		
		//String[] sections = getMapArray("sections");
		return true;
	}

	/**
	 * Actually serves the Request.
	 * @throws ExceptionReport
	 * @return Response The result of the computation
	 */
	public Response call() throws ExceptionReport {
		validate();
		LOGGER.info("Handled GetCapabilitiesRequest successfully!");
		return new CapabilitiesResponse(this);
	}

	/**
	 * Not used in this class. Returns null;
	 */
	public Object getAttachedResult(){
		return null;
	}
}
