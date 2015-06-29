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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.xml.ws.Response;

import org.n52.wps.commons.WPSConfig;
import org.n52.wps.webapp.api.ConfigurationCategory;
import org.n52.wps.webapp.api.ConfigurationManager;
import org.n52.wps.webapp.api.ConfigurationModule;
import org.n52.wps.webapp.api.types.ConfigurationEntry;
import org.n52.wps.webapp.entities.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
	"REQUEST_DATE TIMESTAMP, " +
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

	protected static final int INSERT_COLUMN_MIME_TYPE = 5;
	
	/** get access to the global logger. */
	private static Logger LOGGER = LoggerFactory.getLogger(AbstractDatabase.class);
	
	protected static PreparedStatement insertSQL = null;
	protected static PreparedStatement updateSQL = null;
	protected static PreparedStatement selectSQL = null;

	@Inject
	private static ConfigurationManager configurationManager;
    private Server serverConfigurationModule;
    
	public Server getServerConfigurationModule() {

		if (serverConfigurationModule == null) {
			serverConfigurationModule = (Server) configurationManager
					.getConfigurationServices().getConfigurationModule(
							Server.class.getName());
		}
		return serverConfigurationModule;
	}
	
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
	
    
    @Override
	public synchronized void insertRequest(String id, InputStream inputStream, boolean xml) {			
        insertResultEntity(inputStream, "REQ_" + id, "ExecuteRequest", xml ? "text/xml" : "text/plain");
	}
    
	/**
	 * Insert a new Response into the Database.
	 * 
	 * @param response  The Response to insert.
	 * @see #storeResponse(Response)
	 * 
	 */
    @Override
	public synchronized String insertResponse(String id, InputStream inputStream) {			
        return insertResultEntity(inputStream, id, "ExecuteResponse", "text/xml");
	}
	
	/**
	 * Inserts any result, which has to be stored in the DB.
	 * @param baos
	 * @param id
	 * @param type
	 */
	protected synchronized String insertResultEntity(InputStream stream, String id, String type, String mimeType) {
		// Use Calendar to get the current timestamp.
		// Uses java.sql.Date !
		Timestamp timestamp = new Timestamp(Calendar.getInstance().getTimeInMillis());

		// try to insert a row of data into the database.
		try {
			AbstractDatabase.insertSQL.setString(INSERT_COLUMN_REQUEST_ID, id);
			AbstractDatabase.insertSQL.setTimestamp(INSERT_COLUMN_REQUEST_DATE, timestamp);
			AbstractDatabase.insertSQL.setString(INSERT_COLUMN_RESPONSE_TYPE, type);
			AbstractDatabase.insertSQL.setAsciiStream(INSERT_COLUMN_RESPONSE, stream);
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
    @Override
	public synchronized void updateResponse(String id, InputStream inputStream) {

		// Try to update the row of data into the database.
		try {
			AbstractDatabase.updateSQL.setString(
					UPDATE_COLUMN_REQUEST_ID, id);
			AbstractDatabase.updateSQL.setAsciiStream(
					UPDATE_COLUMN_RESPONSE, inputStream);
			AbstractDatabase.updateSQL.executeUpdate();
			getConnection().commit();
		} catch (SQLException e) {
			LOGGER.error("Could not insert Response into database: "
					+ e.getMessage());
		}
	}

	/**
	 * Store the Response of a deferred Request. It either gets inserted into
	 * the database, or it updates a previous Response, based on the identifier.
	 * 
	 * @param response
	 *            The Response to store.
	 */ 
    @Override
	public synchronized String storeResponse(String id, InputStream inputStream) {
		if (lookupResponse(id) == null) {
			return insertResponse(id, inputStream);
		} else {
			updateResponse(id, inputStream);
			return null;
		}
	}

    @Override
	public synchronized InputStream lookupRequest(String request_id) {
        request_id = "REQ_" + request_id;
		try {
			AbstractDatabase.selectSQL.setString(SELECT_COLUMN_RESPONSE, request_id);
			ResultSet res = AbstractDatabase.selectSQL.executeQuery();
			if (res == null || !res.next()) {
				LOGGER.warn("Query did not return a valid result.");
				return null;
			} else {
				LOGGER.info("Successfully retrieved the Request: "
						+ request_id);
				return res.getAsciiStream(1);
			}
		} catch (SQLException e) {
			LOGGER.error("SQLException with request_id: " + request_id
					+ "and message: " + e.getMessage());
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
    @Override
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
	
    @Override
	public synchronized String storeComplexValue(String id, InputStream stream, String type, String mimeType) {
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
    @Override
	public String generateRetrieveResultURL(String id) {
		return getServerConfigurationModule().getProtocol() + "://"
                + getServerConfigurationModule().getHostname() + ":"
                + getServerConfigurationModule().getHostport() + "/"
                + getServerConfigurationModule().getWebappPath() + "/"
                + "RetrieveResultServlet?id=";   // TODO:  Parameterize this... Execution Context..?
	}
	
	public abstract Connection getConnection();
	public abstract String getConnectionURL();
	
	/**
	 * Returns the name of the database.
	 */
    @Override
	public String getDatabaseName() {
		
		String dbName = getDatabaseProperties(PROPERTY_NAME_DATABASE_NAME);
		return (dbName == null || dbName.equals("")) ? "wps" : dbName;
	}
	
	static String getDatabaseProperties(String propertyName) {
		
		Map<String, ConfigurationModule> activeDatabaseConfigModules = configurationManager.getConfigurationServices().getActiveConfigurationModulesByCategory(ConfigurationCategory.DATABASE);
		
		ConfigurationModule databaseConfigModule = null;
		
		try{
			//there should be only one
			databaseConfigModule = activeDatabaseConfigModules.get(activeDatabaseConfigModules.keySet().iterator().next());
		}catch(Exception e){
			throw new RuntimeException("Could not load any active database configuration module.");
		}
		
		List<? extends ConfigurationEntry<?>> configurationEntries = databaseConfigModule.getConfigurationEntries();

		for(ConfigurationEntry<?> property : configurationEntries){
			if(property.getKey().equalsIgnoreCase(propertyName)){
				return property.getValue().toString();
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
		
		if (dbPath == null || dbPath.compareTo("") == 0) {
            // TODO:  parameterize base path
			dbPath = System.getProperty("java.io.tmpdir", ".") + File.separator + "Databases";
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
    @Override
	public void shutdown() {
		try {
			getConnection().close();
		} catch (SQLException e) {
			
			LOGGER.error("Problem encountered when closing the SQL connection", e);
		}
	}
	
    @Override
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
	
	@Override
	public long getContentLengthForStoreResponse(String id) {
		return -1;
	}
	
    @Override
	public boolean deleteStoredResponse(String id) {
		// TODO Auto-generated method stub
		return false;
	}

    @Override
	public File lookupRequestAsFile(String id) {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
	public File lookupResponseAsFile(String id) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
