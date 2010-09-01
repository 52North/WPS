/***************************************************************
 This implementation provides a framework to publish processes to the
 web through the  OGC Web Processing Service interface. The framework 
 is extensible in terms of processes and data handlers. It is compliant 
 to the WPS version 0.4.0 (OGC 05-007r4). 

 Copyright (C) 2007 by con terra GmbH

 Authors:
 	Theodor Foerster, ITC, Enschede
 	
	

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.n52.wps.DatabaseDocument.Database;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.datahandler.binary.LargeBufferStream;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.RetrieveResultServlet;
import org.n52.wps.server.WebProcessingService;
import org.n52.wps.server.response.ExecuteResponse;
import org.n52.wps.server.response.Response;
import org.n52.wps.util.StreamUtils;

/**
* An anstract-layer to the databases. 
* 
* @author Janne Kovanen
* 
*/
public abstract class AbstractDatabase implements IDatabase{
	/** Property of the path to the location of the database */
	public static final String PROPERTY_NAME_DATABASE_PATH = "databasePath";
	
	/** Property of the path to the location of the database - Actual name of the database. */
	public static final String PROPERTY_NAME_DATABASE_NAME = "databaseName";
	
	/** Property of the path to the location of the database - name of database type: DERBY, HSQL, ...*/
	public static final String PROPERTY_NAME_DATABASE = "database";
	
	/** SQL to create a response in the DB **/
	public static final String 	creationString = "CREATE TABLE RESULTS (" +
	"REQUEST_ID VARCHAR(100) NOT NULL PRIMARY KEY, " +
	"REQUEST_DATE DATE, " +
	"RESPONSE_TYPE VARCHAR(100), " +
	"RESPONSE CLOB, " +
	"RESPONSE_MIMETYPE VARCHAR(100))";
	
	/** SQL to insert a response into the database */
	public static final String insertionString = "INSERT INTO RESULTS VALUES (?, ?, ?, ?, ?)";

	/** SQL to update a response, that was already stored in the database */
	public static final String updateString = "UPDATE RESULTS SET RESPONSE = (?) WHERE REQUEST_ID = (?)";

	/** SQL to retrieve a response from the database */
	public static final String selectionString = "SELECT RESPONSE, RESPONSE_MIMETYPE FROM RESULTS WHERE REQUEST_ID = (?)";

	/** The column of "response" in the select statement. */
	protected static final int SELECT_COLUMN_RESPONSE = 1;

	/** The column of "request_id" in the insert statement. */
	protected static final int INSERT_COLUMN_REQUEST_ID = 1;

	/** The column of "request_data" in the insert statement. */
	protected static final int INSERT_COLUMN_REQUEST_DATE = 2;

	/** The column of "response_type" in the insert statement. */
	protected static final int INSERT_COLUMN_RESPONSE_TYPE = 3;

	/** The column of "response" in the insert statement. */
	protected static final int INSERT_COLUMN_RESPONSE = 4;

	/** The column of "response" in the update statement. */
	protected static final int UPDATE_COLUMN_RESPONSE = 1;

	/** The column of "request_id" in the update statement. */
	protected static final int UPDATE_COLUMN_REQUEST_ID = 2;

	private static final int INSERT_COLUMN_MIME_TYPE = 5;
	
	/** get access to the global logger. */
	private static Logger LOGGER = Logger.getLogger(AbstractDatabase.class);
	
	protected static PreparedStatement insertSQL = null;
	protected static PreparedStatement updateSQL = null;
	protected static PreparedStatement selectSQL = null;
	
	/**
	 * Get an instance of the Database object. Only one instance is required. If
	 * it not already exists, it will be created. The first call of this method
	 * can take some time. Preferable call this method once on application
	 * startup, to serve additional calls in less time. When the
	 * Database-application is started, it first tries to connect to an existing
	 * database. If it is not found, this method tries to create a new one.
	 * Implementations can have additional properties, such as username/password.
	 * 
	 * @return A static instance of Database.
	 */
	public static IDatabase getInstance() {
		throw new SubclassNotImplementingException(
		    "Subclasses of AbstractDatabase must implement the method \"static String getInstance()\"");
	}
	
	/**
	 * Insert a new Response into the Database.
	 * 
	 * @param response  The Response to insert.
	 * @see #storeResponse(Response)
	 * 
	 */
	public synchronized String insertResponse(Response response) {
		// Save the response to this outputstream.
		if(response instanceof ExecuteResponse){
			ExecuteResponse executeResponse = (ExecuteResponse) response;
		
			LargeBufferStream baos = new LargeBufferStream();
			try {
				response.save(baos);
			} catch (ExceptionReport e) {
				LOGGER.error("Saving the Response threw an ErrorReport: "
						+ e.getMessage());
			}
			
			
			return insertResultEntity(baos, Long.toString(response.getUniqueId()), response.getType(), executeResponse.getMimeType());
			
		}else{
			throw new RuntimeException("Could not insert a non execute response");
		}
	}
	
	/**
	 * Inserts any result, which has to be stored in the DB.
	 * @param baos
	 * @param id
	 * @param type
	 */
	private synchronized String insertResultEntity(
			LargeBufferStream baos, String id, String type, String mimeType) {
		// store the contents of the (finite) outputstream into a bytes array
		InputStream bais = StreamUtils.convertOutputStreamToInputStream(baos);
		// Use Calendar to get the current date.
		// Uses java.sql.Date !
		Date date = new Date(Calendar.getInstance().getTimeInMillis());

		// try to insert a row of data into the database.
		try {
			AbstractDatabase.insertSQL.setString(INSERT_COLUMN_REQUEST_ID, id);
			AbstractDatabase.insertSQL.setDate(INSERT_COLUMN_REQUEST_DATE, date);
			AbstractDatabase.insertSQL.setString(INSERT_COLUMN_RESPONSE_TYPE, type);
			AbstractDatabase.insertSQL.setAsciiStream(INSERT_COLUMN_RESPONSE, bais);
			AbstractDatabase.insertSQL.setString(INSERT_COLUMN_MIME_TYPE, mimeType);
			// AbstractDatabase.insertSQL.setAsciiStream(INSERT_COLUMN_RESPONSE, bais, b.length);
		
			AbstractDatabase.insertSQL.executeUpdate();
			getConnection().commit();
		} catch (SQLException e) {
			LOGGER.error("Could not insert Response into database: "
					+ e.getMessage());
		} 
		return generateRetrieveResultURL(id);
	}

	/**
	 * Update the Response in the Database, based on the Identifier.
	 * 
	 * @param response
	 *            The Response to update
	 * @see #storeResponse(Response)
	 */
	public synchronized void updateResponse(Response response) {
		// Save the response to this outputstream
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			response.save(baos);
		} catch (ExceptionReport e) {
			LOGGER.error("Updating the Response threw an ErrorReport: "
					+ e.getMessage());
		}

		// Store the contents of the (finite) outputstream into a bytes array
		byte[] b = baos.toByteArray();
		// Create a new inputstream from the byte array
		ByteArrayInputStream bais = new ByteArrayInputStream(b);

		// Try to update the row of data into the database.
		try {
			AbstractDatabase.updateSQL.setString(
					UPDATE_COLUMN_REQUEST_ID, Long.toString(response.getUniqueId()));
			AbstractDatabase.updateSQL.setAsciiStream(
					UPDATE_COLUMN_RESPONSE, bais);
			AbstractDatabase.updateSQL.executeUpdate();
			getConnection().commit();
		} catch (SQLException e) {
			LOGGER.error("Could not insert Response into database: "
					+ e.getMessage());
		}
	}

	/**
	 * Store the Response of a deferred Request. It either gets inserted into
	 * the databse, or it updates a previous Response, based on the identifier.
	 * 
	 * @param response
	 *            The Response to store.
	 */
	public synchronized String storeResponse(Response response) {
		if (lookupResponse(Long.toString(response.getUniqueId())) == null) {
			return insertResponse(response);
		} else {
			updateResponse(response);
			return null;
		}
	}

	/**
	 * Retrieve the Response on a previous Request, based on an unique
	 * identifier, which was already given to the client for reference.
	 * 
	 * @param request_id
	 *            The identifier of the Request
	 * @return null, if an SQLException occurred, else an InputStream with the
	 *         Response
	 */
	public synchronized InputStream lookupResponse(String request_id) {
		try {
			AbstractDatabase.selectSQL.setString(SELECT_COLUMN_RESPONSE, request_id);
			ResultSet res = AbstractDatabase.selectSQL.executeQuery();
			if (res == null || !res.next()) {
				LOGGER.warn("Query did not return a valid result.");
				return null;
			} else {
				LOGGER.info("Successfully retrieved the Response of Request: "
						+ request_id);
				return res.getAsciiStream(1);
			}
		} catch (SQLException e) {
			LOGGER.error("SQLException with request_id: " + request_id
					+ "and message: " + e.getMessage());
			return null;
		}
	}
	
	public synchronized String storeComplexValue(String id, 
			LargeBufferStream stream, String type, String mimeType) {
		return insertResultEntity(stream, id, type, mimeType);
	}
	
	/**
	 * The URL referencing the location from which the ExecuteResponse can be retrieved. 
	 * If "status" is "true" in the Execute request, the ExecuteResponse should also be 
	 * found here as soon as the process returns the initial response to the client. 
	 * It should persist at this location as long as the outputs are accessible from the server.
	 * The outputs may be stored for as long as the implementer of the server decides. 
	 * If the process takes a long time, this URL can be repopulated on an ongoing basis 
	 * in order to keep the client updated on progress. Before the process has succeeded, 
	 * the ExecuteResponse contains information about the status of the process, including 
	 * whether or not processing has started, and the percentage completed. It may also 
	 * optionally contain the inputs and any ProcessStartedType interim results. When the 
	 * process has succeeded, the ExecuteResponse found at this URL shall contain the output 
	 * values or references to them.
	 * @return
	 */	
	public String generateRetrieveResultURL(String id) {
		return "http://" + 
				WPSConfig.getInstance().getWPSConfig().getServer().getHostname() + ":" + 
				WPSConfig.getInstance().getWPSConfig().getServer().getHostport() + "/" + 
				WebProcessingService.WEBAPP_PATH + "/" + 
				RetrieveResultServlet.SERVLET_PATH + "?id=" + id;
	}
	
	public abstract Connection getConnection();
	public abstract String getConnectionURL();
	
	/**
	 * Returns the name of the database.
	 */
	public String getDatabaseName() {
		
		String dbName = getDatabaseProperties(PROPERTY_NAME_DATABASE_NAME);
		if(dbName == null || dbName.equals(""))
			return "wps";
		return dbName;
	}
	
	static String getDatabaseProperties(String propertyName) {
		Database database = WPSConfig.getInstance().getWPSConfig().getServer().getDatabase();
		Property[] dbProperties = database.getPropertyArray();
		for(Property property : dbProperties){
			if(property.getName().equalsIgnoreCase(propertyName)){
				return property.getStringValue();
			}
		}
		return null;
	}

	/** Returns the path to the database.
	 * 
	 * @note The path has no file separator in the end of the file.
	 */
	protected static String getDatabasePath() {
		String dbPath = getDatabaseProperties(PROPERTY_NAME_DATABASE_PATH);
		String dbName = getDatabaseProperties(PROPERTY_NAME_DATABASE_NAME);
		String dbTypeName = getDatabaseProperties(PROPERTY_NAME_DATABASE);
		
		if (dbPath == null || dbPath.compareTo("")==0) {
			dbPath = WebProcessingService.BASE_DIR + File.separator + "Databases";
			if(dbTypeName!=null && !dbTypeName.equals("")) {
				dbPath += File.separator + dbTypeName.toUpperCase();
			} else {
				dbPath += File.separator + "DERBY";
			}
			if(dbName!=null && !dbName.equals("")) {
				dbPath += File.separator + dbName.toUpperCase();
			} else {
				dbPath += File.separator + "wps";
			}
		}
		return dbPath;
	}
	
	/**
	 * @throws Exception 
	 * 
	 */
	public void shutdown() {
		try {
			getConnection().close();
		} catch (SQLException e) {
			
			LOGGER.error("Problem encountered when closing the SQL connection", e);
		}
	}
	
	public String getMimeTypeForStoreResponse(String id) {
		try {
			AbstractDatabase.selectSQL.setString(SELECT_COLUMN_RESPONSE, id);
		
			ResultSet res = AbstractDatabase.selectSQL.executeQuery();
			if (res == null || !res.next()) {
				LOGGER.warn("Query did not return a valid result.");
				return null;
			} else {
				LOGGER.info("Successfully retrieved the Mimetyoe of the response: "
						+ id);
				return res.getString(2);
			}
		} catch (SQLException e) {
			LOGGER.error("SQLException with request_id: " + id
					+ "and message: " + e.getMessage());
			return null;
		}
	}

	
	public boolean deleteStoredResponse(String id) {
		// TODO Auto-generated method stub
		return false;
	}

	

	
	public File lookupResponseAsFile(String id) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
