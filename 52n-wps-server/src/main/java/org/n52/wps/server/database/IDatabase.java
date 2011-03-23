/***************************************************************
 This implementation provides a framework to publish processes to the
 web through the  OGC Web Processing Service interface. The framework 
 is extensible in terms of processes and data handlers. It is compliant 
 to the WPS version 0.4.0 (OGC 05-007r4). 

 Copyright (C) 2006 by con terra GmbH

 Authors:
	Janne Kovanen, Finnish Geodetic Institute, Finland
	Theodor Foerster, ITC, Enschede, the Netherlands
	Carsten Priess, Institute for geoinformatics, University of Muenster, Germany
	Timon Ter Braak, University of Twente, the Netherlands

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

 ***************************************************************/
package org.n52.wps.server.database;

import java.io.File;
import java.io.InputStream;

import org.n52.wps.io.datahandler.binary.LargeBufferStream;
import org.n52.wps.server.response.Response;

/**
* An interface-layer to the databases.
* 
* @note All implementing classes have to be singletons!
* @author Janne Kovanen
* 
*/
public interface IDatabase {
	// Closes the database connections etc.
	public void shutdown();
	
	// Returns some name for the database, like DERBY, HSQL, ...
	public String getDatabaseName();
	
	// Insert a new Response into the Database.
	public String insertResponse(Response response);

	// Update the Response in the Database, based on the Identifier.
	public void updateResponse(Response response);
	
	// Store the Response of a deferred Request. It either gets inserted into
	// the databse, or it updates a previous Response, based on the identifier.
	public String storeResponse(Response response);
	
	// Retrieve the Response on a previous Request, based on an unique
	// identifier, which was already given to the client for reference.
	public InputStream lookupResponse(String request_id);
	
	public String storeComplexValue(String id, LargeBufferStream stream, String type, String mimeType);
	
	// The URL referencing the location from which the ExecuteResponse can be retrieved. 
	// If "status" is "true" in the Execute request, the ExecuteResponse should also be 
	// found here as soon as the process returns the initial response to the client. 
	// It should persist at this location as long as the outputs are accessible from the server. 
	// The outputs may be stored for as long as the implementer of the server decides. 
	// If the process takes a long time, this URL can be repopulated on an ongoing basis in 
	// order to keep the client updated on progress. Before the process has succeeded, 
	// the ExecuteResponse contains information about the status of the process, including 
	// whether or not processing has started, and the percentage completed. It may also 
	// optionally contain the inputs and any ProcessStartedType interim results. When the 
	// process has succeeded, the ExecuteResponse found at this URL shall contain the output 
	// values or references to them.
	public String generateRetrieveResultURL(String id);
	
	public String getMimeTypeForStoreResponse(String id);
	
	public boolean deleteStoredResponse(String id);

	public File lookupResponseAsFile(String id);
	
}
