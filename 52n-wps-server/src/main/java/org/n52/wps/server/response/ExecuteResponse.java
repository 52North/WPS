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
package org.n52.wps.server.response;

import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.database.DatabaseFactory;
import org.n52.wps.server.request.ExecuteRequest;

public class ExecuteResponse extends Response {

	private static Logger LOGGER = Logger.getLogger(ExecuteResponse.class);
	private boolean alreadyStored;
	private ExecuteResponseBuilder builder;
	
	public ExecuteResponse(ExecuteRequest request) throws ExceptionReport{
		super(request);
		alreadyStored = false;
		this.builder = ((ExecuteRequest)this.request).getExecuteResponseBuilder();
		if(request.isStoreResponse()){
			LOGGER.debug("Store Response in Database");
			
			DatabaseFactory.getDatabase().storeResponse(this);
		}
	}
	
	public void save(OutputStream os) throws ExceptionReport{
		//workaround, to avoid infinite processing. 
		if(!alreadyStored) {
			this.builder.update();
			if(((ExecuteRequest)request).isStoreResponse()) {
				this.alreadyStored = true;
				DatabaseFactory.getDatabase().storeResponse(this);
			}
		}
		this.builder.save(os);
	}
	
	public ExecuteResponseBuilder getExecuteResponseBuilder(){
		return builder;
	}
	
	public String getMimeType(){
		return builder.getMimeType();
	}
}
