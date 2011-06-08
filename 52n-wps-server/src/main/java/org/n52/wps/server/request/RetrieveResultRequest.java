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
	Timon Ter Braak, University of Twente, the Netherlands


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

import java.io.InputStream;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.database.IDatabase;
import org.n52.wps.server.database.DatabaseFactory;
import org.n52.wps.server.response.Response;
import org.n52.wps.server.response.RetrieveResultResponse;

/**
 * If the server is too busy or the computation is too long,
 * the client can retrieve the result at a later time.
 * This class represents a Request to obtain the result.
 * The respons might be the result, be also could contain a
 * message that the result is not available yet.
 */
public class RetrieveResultRequest extends Request {

	private InputStream storedResponse = null;
	
	/**
	 * Create a Request based on a CaseInsensitiveMap as input (HTTP GET)
	 * @param ciMap The Map which holds the client input.
	 */
	public RetrieveResultRequest(CaseInsensitiveMap ciMap) throws ExceptionReport{
		super(ciMap);
	}
	
	/**
	 * Actually serves the Request.
	 * @throws ExceptionReport
	 */
	public Response call() throws ExceptionReport {
		if(validate()){
			return new RetrieveResultResponse(this);
		}
		return null;
	}

	/**
	 * Validates the client input
	 * @return True if the input is valid, False otherwise
	 */
	public boolean validate() throws ExceptionReport {
		String req_id = getMapValue("request_id", true);
		if(req_id.length() == 0){
			throw new ExceptionReport("The value of parameter <request_id> is not valid.", ExceptionReport.INVALID_PARAMETER_VALUE);
		}
		try{
		}catch(NumberFormatException e){
			throw new ExceptionReport("The value of parameter <request_id> is not an integer identifier", ExceptionReport.INVALID_PARAMETER_VALUE);
		}
		IDatabase db = DatabaseFactory.getDatabase();
		this.storedResponse = db.lookupResponse(req_id);
		return (this.storedResponse != null);
	}
	
	public Object getAttachedResult() throws NullPointerException {
		if(this.storedResponse == null)
			throw new NullPointerException("No stored responses were found!");
		return this.storedResponse;
	}

}
