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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @note Uses lazy initialization without synchronization  
 */
public class DerbyDatabase extends AbstractDatabase {

	private static Logger LOGGER = LoggerFactory.getLogger(DerbyDatabase.class); // Get access to the global logger.
	private static String connectionURL = null;
	private static Connection conn = null;
	private static DerbyDatabase db = new DerbyDatabase(); // Static loading.
	
	/**
	 * Static initialization is guaranteed to be thread-safe and no synchronization must be incurred.
	 */
	private DerbyDatabase() {
		try {
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
			String dbDriverURI = "jdbc:derby";
			DerbyDatabase.connectionURL = dbDriverURI + ":" + getDatabasePath() + File.separator + getDatabaseName();
			LOGGER.debug("Database connection URL is: " + DerbyDatabase.connectionURL);
		} catch(ClassNotFoundException cnf_ex) {
			LOGGER.error("Database cannot be loaded: " + connectionURL);
			throw new UnsupportedDatabaseException("The database class could not be loaded.");
		}
		if(!DerbyDatabase.createConnection()) {
				throw new RuntimeException("Creating database connection failed.");
        }
		if(!DerbyDatabase.createResultTable()) {
				throw new RuntimeException("Creating result table failed.");
        }
		if(!DerbyDatabase.createPreparedStatements()) {
				throw new RuntimeException("Creating prepared statements failed.");
        }
	}
	
	public static synchronized DerbyDatabase getInstance() { 
		if (DerbyDatabase.conn == null) {
			if(!DerbyDatabase.createConnection()) {
					throw new RuntimeException("Creating database connection failed.");
            }
			if(!DerbyDatabase.createResultTable()) {
					throw new RuntimeException("Creating result table failed.");
            }
			if(!DerbyDatabase.createPreparedStatements()) {
					throw new RuntimeException("Creating prepared statements failed.");
            }
		}
		return DerbyDatabase.db;
	}
	
	private static boolean createConnection() {
		Properties props = new Properties();
		DerbyDatabase.conn = null;
		// Try to connect to an existing database. Note that create is set to true.
		try {
			DerbyDatabase.conn = DriverManager.getConnection(
					DerbyDatabase.connectionURL + ";create=true", props);
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
			DatabaseMetaData meta = DerbyDatabase.conn.getMetaData();
			rs = meta.getTables(null, null, "RESULTS",
					new String[] { "TABLE" });
			if (!rs.next()) {
				LOGGER.info("Table RESULTS does not yet exist.");
				Statement st = DerbyDatabase.conn.createStatement();
				st.executeUpdate(DerbyDatabase.creationString);
				DerbyDatabase.conn.commit();
				
				rs = null;
				meta = DerbyDatabase.conn.getMetaData();
				rs = meta.getTables(null, null, "RESULTS",
						new String[] { "TABLE" });
				if (rs.next()) {
					LOGGER.info("Succesfully created table RESULTS.");
				} else {
					LOGGER.error("Could not create table RESULTS.");
					return false;
				}
				// Set upcoming statements to autocommit.
				DerbyDatabase.conn.setAutoCommit(false);
			}
		} catch (SQLException e) {
			LOGGER.error("Connection to the HSQL database failed: "
					+ e.getMessage());
			return false;
		}
		return true;	
	}
	
	private static boolean createPreparedStatements() {
		// Create prepared staments (more efficient).
		try {
			DerbyDatabase.closePreparedStatements();
			DerbyDatabase.insertSQL = DerbyDatabase.conn.prepareStatement(insertionString);
			DerbyDatabase.selectSQL = DerbyDatabase.conn.prepareStatement(selectionString);
			DerbyDatabase.updateSQL = DerbyDatabase.conn.prepareStatement(updateString);
		} catch (SQLException e) {
			LOGGER.error("Could not create the prepared statements.");
			return false;
		}
		return true;
	}
	
    @Override
	public Connection getConnection() {
		return DerbyDatabase.conn;
	}
    
    @Override
	public String getConnectionURL() {
		return DerbyDatabase.connectionURL;
	}
	
	/**
	 * Shutdown the database in a clean, safe way.
	 */
	/*
	 
	 public void shutdown() {
		boolean gotSQLExc = false;
		try {
			HSQLDatabase.conn.close();
			DriverManager.getConnection(HSQLDatabase.connectionURL);
			HSQLDatabase.db = null;
			System.gc();
		} catch (SQLException se) {
			if (se.getSQLState().equals("XJ015")) {
				gotSQLExc = true;
			}
		}
		if (!gotSQLExc) {
			LOGGER.error("Database did not shut down normally");
		} else {
			LOGGER.info("Database shut down normally");
		}
	}
	*/
	
	/**
	 * Shutdown the database in a clean, safe way.
	 */
    @Override
	public void shutdown() {
   		boolean flag0 = false, flag1 = false;
   		try {
           if (DerbyDatabase.conn != null) {
        	   // Close PreparedStatements if needed.
        	   flag0 = closePreparedStatements();
        	   DerbyDatabase.conn = DriverManager.getConnection(DerbyDatabase.connectionURL + ";shutdown=true");
        	   // Return to connection pool.
        	   DerbyDatabase.conn.close();
        	   DerbyDatabase.conn = null;
               flag1 = true;
               // Force garbage collection. 
               System.gc();
               // Setting the database to null;
               DerbyDatabase.db = null;
           }
       } catch (SQLException sql_ex) {
       		LOGGER.error("Error occured while closing connection: " + 
       				sql_ex.getMessage() + "::" 
       				+ "closed prepared statements?" + flag0
       				+ ";closed connection?" + flag1);
       		return;
       } catch(Exception exception) {
       		LOGGER.error("Error occured while closing connection: " + 
       				exception.getMessage() + "::" 
       				+ "closed prepared statements?" + flag0
       				+ ";closed connection?" + flag1);
       		return;
       } finally {
   	    	// Make sure the connection is returned to the pool.
   	    	if (DerbyDatabase.conn!= null) {
   	    		try { DerbyDatabase.conn.close(); } catch (SQLException e) { ; }
   	    		DerbyDatabase.conn = null;
   	    	}
   	  }
       LOGGER.info("Derby database connection is closed succesfully");
	}
	
	/**
	 * Always make sure the statements are closed, when you exit.
	 */
	private static boolean closePreparedStatements() {
		try {
		if(DerbyDatabase.insertSQL != null) {
			DerbyDatabase.insertSQL.close();
			DerbyDatabase.insertSQL = null;
		} 
		if(DerbyDatabase.selectSQL != null) {
			DerbyDatabase.selectSQL.close();
			DerbyDatabase.selectSQL = null;
		}
		if(DerbyDatabase.updateSQL != null) {
			DerbyDatabase.updateSQL.close();
			DerbyDatabase.updateSQL = null;
		}
		} catch(SQLException sql_ex) {
			LOGGER.error("Prepared statements could not be closed.");
			return false;
		}
		return true;
	}

	
    @Override
	public boolean deleteStoredResponse(String id) {
		// TODO Auto-generated method stub
		return false;
	}

    @Override
	public File lookupResponseAsFile(String id) {
		// TODO Auto-generated method stub
		return null;
	}
    
}
