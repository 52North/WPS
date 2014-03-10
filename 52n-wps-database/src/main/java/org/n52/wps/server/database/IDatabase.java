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
package org.n52.wps.server.database;

import java.io.File;
import java.io.InputStream;

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
    
    // Insert a new Request into the Database. 
    public void insertRequest(String id, InputStream request, boolean xml);
	
	// Insert a new Response into the Database.    
    public String insertResponse(String id, InputStream reponse);

	// Update the Response in the Database, based on the Identifier.
    public void updateResponse(String id, InputStream reponse);
	
	// Store the Response of a deferred Request. It either gets inserted into
	// the databse, or it updates a previous Response, based on the identifier.
	public String storeResponse(String id, InputStream reponse);
	
    // Retrieve the Request on a previous Request, based on an unique
	// identifier, which was already given to the client for reference.
	public InputStream lookupRequest(String request_id);
    
	// Retrieve the Response on a previous Request, based on an unique
	// identifier, which was already given to the client for reference.
	public InputStream lookupResponse(String request_id);
	
	public String storeComplexValue(String id, InputStream stream, String type, String mimeType);
	
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
	
	public long getContentLengthForStoreResponse(String id);
	
	public boolean deleteStoredResponse(String id);
    
    public File lookupRequestAsFile(String id);

	public File lookupResponseAsFile(String id);
	
}
