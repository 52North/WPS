/***************************************************************
 This implementation provides a framework to publish processes to the
 web through the  OGC Web Processing Service interface. The framework 
 is extensible in terms of processes and data handlers. It is compliant 
 to the WPS version 0.4.0 (OGC 05-007r4). 

 Copyright (C) 2007 by con terra GmbH

 Authors:
	Janne Kovanen, Finnish Geodetic Institute, Finland
	

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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;

import org.apache.log4j.Logger;
import org.n52.wps.io.datahandler.binary.LargeBufferStream;
import org.n52.wps.server.response.Response;

/**
 * 
 */
public class DatabaseFactory implements IDatabase
{
	private static Logger LOGGER = Logger.getLogger(DatabaseFactory.class);
	// Property of the name of the database. Used to define the database implementation.
	public static final String PROPERTY_NAME_DATABASE_CLASS_NAME = "databaseClass";
	private static IDatabase database;
	
	public static IDatabase getDatabase() {
		if(DatabaseFactory.database == null) {
			try {
				String databaseClassName = 
					AbstractDatabase.getDatabaseProperties(PROPERTY_NAME_DATABASE_CLASS_NAME);
				// if databaseClassName is not defined take derby.
				if(databaseClassName == null || databaseClassName.equals("")) {
					LOGGER.info("Database class name was not found in properties. FlatFileDatabase will be used.");
					databaseClassName = "org.n52.wps.server.database.FlatFileDatabase";
				}
				Class cls = Class.forName(databaseClassName, true, DatabaseFactory.class.getClassLoader());
				Method method = cls.getMethod("getInstance", new Class[0]);
				IDatabase db = (IDatabase) method.invoke(cls, new Object[0]);
				DatabaseFactory.database = db;
			} catch(NoSuchMethodException nsm_ex) {
				LOGGER.error("Instance returning method was not found while creating database instance. " + 
						nsm_ex.getMessage());
				return null;
			} catch(InvocationTargetException it_ex) {
				LOGGER.error("Invocation target exception while creating database instance. " + 
						it_ex.getMessage());
				return null;
			} catch(IllegalAccessException ia_ex) {
				LOGGER.error("Illegal access exception while creating database instance. " + 
						ia_ex.getMessage());
				return null;
			} catch(ClassNotFoundException cnf_ex) {
				LOGGER.error("Database class could not be found. " + 
						cnf_ex.getMessage());
				return null;
			} catch(Exception ex) {
				LOGGER.error("Database class could not be found.");
				return null;
			}
		}
		return DatabaseFactory.database;
	}

	protected void finalize() throws Throwable {
		super.finalize();
		DatabaseFactory.database.shutdown();
	}
	
	/**
	 * Shutdown the database in a clean, safe way.
	 */
	public void shutdown() {
		DatabaseFactory.database.shutdown();
	}

	/**
	 * Insert a new Response into the Database.
	 * 
	 * @param response  The Response to insert.
	 * @see #storeResponse(Response)
	 */
	public synchronized String insertResponse(Response response) {
		return DatabaseFactory.database.insertResponse(response);
	}

	/**
	 * Update the Response in the Database, based on the Identifier.
	 * 
	 * @param response  The Response to update
	 * @see #storeResponse(Response)
	 */
	public synchronized void updateResponse(Response response) {
		DatabaseFactory.database.updateResponse(response);
	}

	/**
	 * Store the Response of a deferred Request. It either gets inserted into
	 * the databse, or it updates a previous Response, based on the identifier.
	 * 
	 * @param response  The Response to store.
	 */
	public synchronized String storeResponse(Response response) {
		return DatabaseFactory.database.storeResponse(response);
	}

	/**
	 * Retrieve the Response on a previous Request, based on an unique
	 * identifier, which was already given to the client for reference.
	 * 
	 * @param request_id  The identifier of the Request
	 * @return null, if an SQLException occurred, else an InputStream with the
	 *         Response
	 */
	public synchronized InputStream lookupResponse(String request_id) {
		return DatabaseFactory.database.lookupResponse(request_id);
	}
	
	public synchronized String storeComplexValue(String id, LargeBufferStream stream, String type, String mimeType) {
		return DatabaseFactory.database.storeComplexValue(id, stream, type, mimeType);
	}
	
	/**
	 * The URL referencing the location from which the ExecuteResponse can be retrieved. 
	 * If "status" is "true" in the Execute request, the ExecuteResponse should also be 
	 * found here as soon as the process returns the initial response to the client. 
	 * It should persist at this location as long as the outputs are accessible from the server. 
	 * The outputs may be stored for as long as the implementer of the server decides. If the 
	 * process takes a long time, this URL can be repopulated on an ongoing basis in order to 
	 * keep the client updated on progress. Before the process has succeeded, the ExecuteResponse 
	 * contains information about the status of the process, including whether or not processing 
	 * has started, and the percentage completed. It may also optionally contain the inputs and 
	 * any ProcessStartedType interim results. When the process has succeeded, the ExecuteResponse 
	 * found at this URL shall contain the output values or references to them.
	 */	
	public String generateRetrieveResultURL(String id) {
		return DatabaseFactory.database.generateRetrieveResultURL(id);
	}
	
	/** 
	 * Returns the connection.
	 */
	public Connection getConnection() {
		return DatabaseFactory.database.getConnection();
	}
	
	/** 
	 * Returns the connection url.
	 */
	public String getConnectionURL() {
		return DatabaseFactory.database.getConnectionURL();
	}
	
	/** 
	 * Returns some name for the database, like DERBY, HSQL, ...
	 */
	public String getDatabaseName() {
		return DatabaseFactory.database.getDatabaseName();
	}

	public String getMimeTypeForStoreResponse(String id) {
		return DatabaseFactory.database.getMimeTypeForStoreResponse(id);
	}

	public boolean deleteStoredResponse(String id) {
		return DatabaseFactory.database.deleteStoredResponse(id);
	}

	public File lookupResponseAsFile(String id) {
		return DatabaseFactory.database.lookupResponseAsFile(id);
	}
	

	
	
}
