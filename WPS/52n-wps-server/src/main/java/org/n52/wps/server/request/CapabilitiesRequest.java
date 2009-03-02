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

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.response.CapabilitiesResponse;
import org.n52.wps.server.response.Response;

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
	}

	/**
	 * Validates the client input
	 * @throws ExceptionReport
	 * @return True if the input is valid, False otherwise
	 */
	public boolean validate() throws ExceptionReport{
		
		String services = getMapValue("service", true);	
		if(! services.equalsIgnoreCase("wps")) {
			throw new ExceptionReport("Parameter <service> is not correct, expected: WPS , got: " + services, 
										ExceptionReport.INVALID_PARAMETER_VALUE);
		}

		String[] versions = getMapArray("version", false);
		if(! requireVersion(SUPPORTED_VERSION, false)) {
				throw new ExceptionReport("Requested versions are not supported, you requested: " + Request.accumulateString(versions),
											ExceptionReport.INVALID_PARAMETER_VALUE);
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
