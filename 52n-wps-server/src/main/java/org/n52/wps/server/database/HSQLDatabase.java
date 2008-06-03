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

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.hsqldb.DatabaseManager;

/**
 * @note Uses lazy initialization without synchronization  
 */
public class HSQLDatabase extends AbstractDatabase {
	public static final String creationString = "CREATE TABLE RESULTS (" +
			"REQUEST_ID VARCHAR(100) NOT NULL PRIMARY KEY, " +
			"REQUEST_DATE DATE, " +
			"RESPONSE_TYPE VARCHAR(100), " +
			"RESPONSE LONGVARCHAR)";
	private static String PROPERTY_NAME_HSQL_PROTOCOL = "hsqlProtocol";
	private static Logger LOGGER = Logger.getLogger(HSQLDatabase.class); // Get access to the global logger.
	private static String connectionURL = null;
	private static Connection conn = null;
	private static HSQLDatabase db = new HSQLDatabase();
	
	/**
	 * Static initialization is guaranteed to be thread-safe and no synchronization must be incurred.
	 */
	private HSQLDatabase() {
		try {
			Class.forName("org.hsqldb.jdbcDriver");
			String dbDriverURI = "jdbc:hsqldb:" + HSQLDatabase.getProtocol();
			HSQLDatabase.connectionURL = dbDriverURI + ":" + getDatabasePath() + File.separator + getDatabaseName() + ";";
			LOGGER.debug("Database connection URL is: " + HSQLDatabase.connectionURL);
		} catch(ClassNotFoundException cnf_ex) {
			LOGGER.error("The HSQL database class could not be loaded.");
			throw new UnsupportedDatabaseException("The database class could not be loaded.");
		}
		if(!HSQLDatabase.createConnection()) {
			LOGGER.error("Creating database connection failed.");
			throw new RuntimeException("Creating database connection failed.");
		}
		if(!HSQLDatabase.createResultTable()) {
			LOGGER.error("Creating result table failed.");
			throw new RuntimeException("Creating result table failed.");
		}
		if(!HSQLDatabase.createPreparedStatements()) {
			LOGGER.error("Creating prepared statements failed.");
			throw new RuntimeException("Creating prepared statements failed.");
		}
	}
	
	public static HSQLDatabase getInstance() { 
		if (HSQLDatabase.conn == null) {
			// These should not be needed, because of the static loading.
			if(!HSQLDatabase.createConnection()) {
				LOGGER.error("Creating database connection failed.");
				throw new RuntimeException("Creating database connection failed.");
			}
			if(!HSQLDatabase.createResultTable()) {
				LOGGER.error("Creating result table failed.");
				throw new RuntimeException("Creating result table failed.");
			}
			if(!HSQLDatabase.createPreparedStatements()) {
				LOGGER.error("Creating prepared statements failed.");
				throw new RuntimeException("Creating prepared statements failed.");
			}
		}
		return HSQLDatabase.db;
	}
	
	private static boolean createConnection() {
		Properties props = new Properties();
		HSQLDatabase.conn = null;
		// Try to connect to an existing database.
		try {
			HSQLDatabase.conn = DriverManager.getConnection(
					HSQLDatabase.connectionURL, props);
			LOGGER.info("Connected to WPS database.");
		} catch (SQLException e) {
			LOGGER.error("Could not connect to or create the database.");
			return false;
		}
		return true;
	}
	
	private static boolean createResultTable() {
		try {
			ResultSet rs = null;
			DatabaseMetaData meta = HSQLDatabase.conn.getMetaData();
			rs = meta.getTables(null, null, "RESULTS",
					new String[] { "TABLE" });
			if (!rs.next()) {
				LOGGER.info("Table RESULTS does not yet exist.");
				Statement st = HSQLDatabase.conn.createStatement();
				st.executeUpdate(HSQLDatabase.creationString);
				HSQLDatabase.conn.commit();
				
				rs = null;
				meta = HSQLDatabase.conn.getMetaData();
				rs = meta.getTables(null, null, "RESULTS",
						new String[] { "TABLE" });
				if (rs.next()) {
					LOGGER.info("Succesfully created table RESULTS.");
				} else {
					LOGGER.error("Could not create table RESULTS.");
					return false;
				}
				// Set upcoming statements to autocommit.
				HSQLDatabase.conn.setAutoCommit(false);
			}
		} catch (SQLException e) {
			LOGGER.error("Connection to the HSQL database failed: "
					+ e.getMessage());
			return false;
		}
		return true;	
	}
	
	private static boolean createPreparedStatements() {
		// Create prepared staments (more efficient)
		try {
			HSQLDatabase.closePreparedStatements();
			HSQLDatabase.insertSQL = HSQLDatabase.conn.prepareStatement(
					AbstractDatabase.insertionString);
			HSQLDatabase.selectSQL = HSQLDatabase.conn.prepareStatement(
					AbstractDatabase.selectionString);
			HSQLDatabase.updateSQL = HSQLDatabase.conn.prepareStatement(
					AbstractDatabase.updateString);
		} catch (SQLException e) {
			LOGGER.error("Could not create the prepared statements.");
			return false;
		}
		return true;
	}
	
	public Connection getConnection() {
		return HSQLDatabase.conn;
	}
	public String getConnectionURL() {
		return HSQLDatabase.connectionURL;
	}

	/**
	 * Shutdown the database in a clean, safe way.
	 */
	public void shutdown() {
   		boolean flag0 = false, flag1 = false, flag2 = false;;
   		try {
           if (HSQLDatabase.conn != null) {
        	   // Close PreparedStatements if needed.
        	   flag0 = closePreparedStatements();
        	   HSQLDatabase.conn = DriverManager.getConnection(
        			   HSQLDatabase.connectionURL + ";shutdown=true");
        	   // Return to connection pool.
        	   HSQLDatabase.conn.close();
        	   HSQLDatabase.conn = null;
               flag1 = true;
               // Close QGL timer if possible.
               flag2 = closeTimerIfPossible();
               // Force garbage collection. 
               System.gc();
               // Setting the database to null;
               HSQLDatabase.db = null;
           }
       } catch (SQLException sql_ex) {
       		LOGGER.error("Error occured while closing connection: " + 
       				sql_ex.getMessage() + "::" 
       				+ "closed prepared statements?" + flag0
       				+ ";closed connection?" + flag1 
       				+ ";timer closed" + flag2);
       		return;
       } catch(Exception exception) {
       		LOGGER.error("Error occured while closing connection: " + 
       				exception.getMessage() + "::" 
       				+ "closed prepared statements?" + flag0
       				+ ";closed connection?" + flag1 
       				+ ";timer closed" + flag2);
       		return;
       } finally {
   	    	// Make sure the connection is returned to the pool.
   	    	if (HSQLDatabase.conn!= null) {
   	    		try { HSQLDatabase.conn.close(); } catch (SQLException e) { ; }
   	    		HSQLDatabase.conn = null;
   	    	}
   	  }
       LOGGER.info("HSQL database connection is closed succesfully");
	}
	
	/**
	 * Always make sure the statements are closed, when you exit.
	 */
	private static boolean closePreparedStatements() {
		try {
			if(HSQLDatabase.insertSQL != null) {
				HSQLDatabase.insertSQL.close();
				HSQLDatabase.insertSQL = null;
			}
			if(HSQLDatabase.selectSQL != null) {
				HSQLDatabase.selectSQL.close();
				HSQLDatabase.selectSQL = null;
			}
			if(HSQLDatabase.updateSQL != null) {
				HSQLDatabase.updateSQL.close();
				HSQLDatabase.updateSQL = null;
			}
		} catch(SQLException sql_ex) {
			LOGGER.error("Prepared statements could not be closed.");
			return false;
		}
		return true;
	}
	
	/** The method closes the HSQL timer if no databases are anymore served by the timer.
	 * 
	 * @note The timer can serve more than one database, so the code checks the count of remaining databases.
	 */
	private boolean closeTimerIfPossible() {
		java.util.Vector v = DatabaseManager.getDatabaseURIs();
		for(int i=0; i<v.size(); i++) {
			LOGGER.info("WPS Database " + i + ": " + v.get(i).toString() + "\n");
		}
		
		//if(DatabaseManager.getDatabaseURIs().size() == 1) {
     	   	DatabaseManager.getTimer().shutDown();
     	   	return true;
		//}
		//return false;
	}

	/** Reads the protocol from the properties file.
	 * 
	 * @return The protocol as a string. The protocol can be 
	 * file, mem, res, hsql, http, hsqls or https. The default
	 * protocol is file.
	 */
	public static String getProtocol() {
		String hsqlProtocol = AbstractDatabase.getDatabaseProperties(
				PROPERTY_NAME_HSQL_PROTOCOL);
		if(hsqlProtocol == null || hsqlProtocol.compareTo("")==0)
			return "file";
		return hsqlProtocol;
	}
}
