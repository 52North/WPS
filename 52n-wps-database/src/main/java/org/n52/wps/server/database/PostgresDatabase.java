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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.n52.wps.commons.PropertyUtil;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.webapp.entities.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

/**
 *
 * @author isuftin (Ivan Suftin, USGS)
 */
public class PostgresDatabase extends AbstractDatabase {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresDatabase.class);
    private static PostgresDatabase db;
    private static String connectionURL = null;
    private static Connection conn = null;
    private final static String KEY_DATABASE_ROOT = "org.n52.wps.server.database";
    private final static String KEY_DATABASE_PATH = "path";
    private final static String KEY_DATABASE_WIPE_ENABLED = "wipe.enabled";
    private final static String KEY_DATABASE_WIPE_PERIOD = "wipe.period";
    private final static String KEY_DATABASE_WIPE_THRESHOLD = "wipe.threshold";
    private final static boolean DEFAULT_DATABASE_WIPE_ENABLED = true;
    private final static long DEFAULT_DATABASE_WIPE_PERIOD = 1000 * 60 * 60;
    private final static long DEFAULT_DATABASE_WIPE_THRESHOLD = 1000 * 60 * 60 * 24 * 7;
    private final static String SUFFIX_GZIP = "gz";
    private final static String DEFAULT_DATABASE_PATH
            = Joiner.on(File.separator).join(
                    System.getProperty("java.io.tmpdir", "."),
                    "Database",
                    "Results");
    private static File BASE_DIRECTORY;
    protected final String baseResultURL;
    public static final String pgCreationString = "CREATE TABLE RESULTS ("
            + "REQUEST_ID VARCHAR(100) NOT NULL PRIMARY KEY, "
            + "REQUEST_DATE TIMESTAMP, "
            + "RESPONSE_TYPE VARCHAR(100), "
            + "RESPONSE TEXT, "
            + "RESPONSE_MIMETYPE VARCHAR(100))";
    protected final Object storeResponseSerialNumberLock;
    protected final Timer wipeTimer;

    private PostgresDatabase() {
        try {
            Class.forName("org.postgresql.Driver");
            PostgresDatabase.connectionURL = "jdbc:postgresql:" + getDatabasePath() + "/" + getDatabaseName();
            LOGGER.debug("Database connection URL is: " + PostgresDatabase.connectionURL);

            // Create lock object
            storeResponseSerialNumberLock = new Object();
            
            PostgresDatabaseConfigurationModule flatFileDatabaseConfigurationModule = (PostgresDatabaseConfigurationModule) WPSConfig.getInstance().getConfigurationManager().getConfigurationServices().getConfigurationModule(PostgresDatabaseConfigurationModule.class.getName());
        	
        	Server server = WPSConfig.getInstance().getServerConfigurationModule();

            baseResultURL = String.format(server.getProtocol() + "://%s:%s/%s/RetrieveResultServlet?id=",
                    server.getHostname(), server.getHostport(), server.getWebappPath());
        	
            PropertyUtil propertyUtil = new PropertyUtil(flatFileDatabaseConfigurationModule, KEY_DATABASE_ROOT);
            
            // Create database wiper task 
            if (propertyUtil.extractBoolean(KEY_DATABASE_WIPE_ENABLED, DEFAULT_DATABASE_WIPE_ENABLED)) {
                long periodMillis = propertyUtil.extractPeriodAsMillis(KEY_DATABASE_WIPE_PERIOD, DEFAULT_DATABASE_WIPE_PERIOD);
                long thresholdMillis = propertyUtil.extractPeriodAsMillis(KEY_DATABASE_WIPE_THRESHOLD, DEFAULT_DATABASE_WIPE_THRESHOLD);

                wipeTimer = new Timer(getClass().getSimpleName() + " Postgres Wiper", true);
                wipeTimer.scheduleAtFixedRate(new PostgresDatabase.WipeTimerTask(thresholdMillis), 15000, periodMillis);
                LOGGER.info("Started {} Postgres wiper timer; period {} ms, threshold {} ms",
                        new Object[]{getDatabaseName(), periodMillis, thresholdMillis});
            } else {
                wipeTimer = null;
            }
        } catch (ClassNotFoundException cnf_ex) {
            LOGGER.error("Database class could not be loaded", cnf_ex);
            throw new UnsupportedDatabaseException("The database class could not be loaded.");
        }
    }

    public static synchronized PostgresDatabase getInstance() {
        if (PostgresDatabase.db == null) {
            PostgresDatabase.db = new PostgresDatabase();
        }

        if (db.getConnection() == null) {
            if (!PostgresDatabase.createConnection()) {
                throw new RuntimeException("Creating database connection failed.");
            }
            if (!PostgresDatabase.createResultTable()) {
                throw new RuntimeException("Creating result table failed.");
            }
            if (!PostgresDatabase.createPreparedStatements()) {
                throw new RuntimeException("Creating prepared statements failed.");
            }
        }

        PostgresDatabaseConfigurationModule flatFileDatabaseConfigurationModule = (PostgresDatabaseConfigurationModule) WPSConfig.getInstance().getConfigurationManager().getConfigurationServices().getConfigurationModule(PostgresDatabaseConfigurationModule.class.getName());

        PropertyUtil propertyUtil = new PropertyUtil(flatFileDatabaseConfigurationModule, KEY_DATABASE_ROOT);
        String baseDirectoryPath = propertyUtil.extractString(KEY_DATABASE_PATH, DEFAULT_DATABASE_PATH);
        BASE_DIRECTORY = new File(baseDirectoryPath);
        LOGGER.info("Using \"{}\" as base directory for results database", baseDirectoryPath);
        if (!BASE_DIRECTORY.exists()) {
            LOGGER.info("Results database does not exist, creating.", baseDirectoryPath);
            BASE_DIRECTORY.mkdirs();
        }

        return PostgresDatabase.db;
    }

    private static boolean createConnection() {
        Properties props = new Properties();
        DataSource dataSource;
        String jndiName = getDatabaseProperties("jndiName");
        String username = getDatabaseProperties("username");
        String password = getDatabaseProperties("password");

        if (jndiName != null) {
            InitialContext context;
            try {
                context = new InitialContext();
                dataSource = (DataSource) context.lookup("java:comp/env/jdbc/" + jndiName);
                PostgresDatabase.conn = dataSource.getConnection();
                PostgresDatabase.conn.setAutoCommit(false);
                LOGGER.info("Connected to WPS database.");
            } catch (NamingException e) {
                LOGGER.error("Could not connect to or create the database.", e);
                return false;
            } catch (SQLException e) {
                LOGGER.error("Could not connect to or create the database.", e);
                return false;
            }
        } else {
            props.setProperty("create", "true");
            props.setProperty("user", username);
            props.setProperty("password", password);
            PostgresDatabase.conn = null;
            try {
                PostgresDatabase.conn = DriverManager.getConnection(
                        PostgresDatabase.connectionURL, props);
                PostgresDatabase.conn.setAutoCommit(false);
                LOGGER.info("Connected to WPS database.");
            } catch (SQLException e) {
                LOGGER.error("Could not connect to or create the database.", e);
                return false;
            }
        }
        return true;
    }

    @Override
    public synchronized void insertRequest(String id, InputStream inputStream, boolean xml) {
        insertResultEntity(inputStream, "REQ_" + id, "ExecuteRequest", xml ? "text/xml" : "text/plain");
    }

    @Override
    public synchronized String insertResponse(String id, InputStream inputStream) {
        return insertResultEntity(inputStream, id, "ExecuteResponse", "text/xml");
    }

    @Override
    protected synchronized String insertResultEntity(InputStream stream, String id, String type, String mimeType) {
        Timestamp timestamp = new Timestamp(Calendar.getInstance().getTimeInMillis());
        FileInputStream fis = null;
        Boolean storingOutput = null != id && id.toLowerCase().contains("output");
        Boolean saveResultsToDB = Boolean.parseBoolean(getDatabaseProperties("saveResultsToDB"));
        String filename = storingOutput ? id : UUID.randomUUID().toString();
        Path filePath = new File(BASE_DIRECTORY, filename).toPath();

        try {
            filePath = Files.createFile(filePath);
            Files.copy(stream, filePath, StandardCopyOption.REPLACE_EXISTING);
            fis = new FileInputStream(filePath.toFile());

            AbstractDatabase.insertSQL.setString(INSERT_COLUMN_REQUEST_ID, id);
            AbstractDatabase.insertSQL.setTimestamp(INSERT_COLUMN_REQUEST_DATE, timestamp);
            AbstractDatabase.insertSQL.setString(INSERT_COLUMN_RESPONSE_TYPE, type);
            AbstractDatabase.insertSQL.setString(INSERT_COLUMN_MIME_TYPE, mimeType);

            if (storingOutput) {
                if (!saveResultsToDB) {
                    byte[] filePathByteArray = filePath.toUri().toString().getBytes();
                    AbstractDatabase.insertSQL.setAsciiStream(INSERT_COLUMN_RESPONSE, new ByteArrayInputStream(filePathByteArray), filePathByteArray.length);
                } else {
                    AbstractDatabase.insertSQL.setAsciiStream(INSERT_COLUMN_RESPONSE, fis, (int) filePath.toFile().length());
                }
            } else {
                AbstractDatabase.insertSQL.setAsciiStream(INSERT_COLUMN_RESPONSE, fis, (int) filePath.toFile().length());
            }

            AbstractDatabase.insertSQL.executeUpdate();
            getConnection().commit();
        } catch (SQLException e) {
            LOGGER.error("Could not insert Response into database.", e);
        } catch (IOException e) {
            LOGGER.error("Could not insert Response into database.", e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    LOGGER.error("Could not close file input stream", e);
                }
            }

            // If we are storing output, we want to only delete the file if we're
            // storing the results to the database. Otherwise, don't delete the
            // file since that will be served on request
            if (filePath != null) {
                try {
                    if (storingOutput) {
                        if (saveResultsToDB) {
                            Files.deleteIfExists(filePath);
                        }
                    } else {
                        Files.deleteIfExists(filePath);
                    }
                } catch (IOException e) {
                    LOGGER.error("Could not delete file: " + filePath.toString(), e);
                }
            }
        }
        return generateRetrieveResultURL(id);
    }

    @Override
    public synchronized void updateResponse(String id, InputStream stream) {
        Path tempFilePath = null;
        FileInputStream fis = null;
        try {
            tempFilePath = Files.createTempFile(UUID.randomUUID().toString(), null);
            Files.copy(stream, tempFilePath, StandardCopyOption.REPLACE_EXISTING);
            fis = new FileInputStream(tempFilePath.toFile());

            AbstractDatabase.updateSQL.setString(UPDATE_COLUMN_REQUEST_ID, id);
            AbstractDatabase.updateSQL.setAsciiStream(UPDATE_COLUMN_RESPONSE, fis, (int) tempFilePath.toFile().length());
            AbstractDatabase.updateSQL.executeUpdate();
            getConnection().commit();
        } catch (SQLException e) {
            LOGGER.error("Could not insert Response into database", e);
        } catch (IOException e) {
            LOGGER.error("Could not insert Response into database", e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    LOGGER.error("Could not close file input stream", e);
                }
            }
            if (tempFilePath != null) {
                try {
                    Files.deleteIfExists(tempFilePath);
                } catch (IOException e) {
                    LOGGER.error("Could not delete file: " + tempFilePath.toString(), e);
                }
            }
        }
    }

    private static boolean createResultTable() {
        try {
            ResultSet rs;
            DatabaseMetaData meta = PostgresDatabase.conn.getMetaData();
            rs = meta.getTables(null, null, "results", new String[]{"TABLE"});
            if (!rs.next()) {
                LOGGER.info("Table RESULTS does not yet exist.");
                Statement st = PostgresDatabase.conn.createStatement();
                st.executeUpdate(PostgresDatabase.pgCreationString);

                PostgresDatabase.conn.commit();

                meta = PostgresDatabase.conn.getMetaData();

                rs = meta.getTables(null, null, "results", new String[]{"TABLE"});
                if (rs.next()) {
                    LOGGER.info("Succesfully created table RESULTS.");
                } else {
                    LOGGER.error("Could not create table RESULTS.");
                    return false;
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Connection to the Postgres database failed: " + e.getMessage());
            return false;
        }
        return true;
    }

    private static boolean createPreparedStatements() {
        try {
            PostgresDatabase.closePreparedStatements();
            PostgresDatabase.insertSQL = PostgresDatabase.conn.prepareStatement(insertionString);
            PostgresDatabase.selectSQL = PostgresDatabase.conn.prepareStatement(selectionString);
            PostgresDatabase.updateSQL = PostgresDatabase.conn.prepareStatement(updateString);
        } catch (SQLException e) {
            LOGGER.error("Could not create the prepared statements.", e);
            return false;
        }
        return true;
    }

    private static boolean closePreparedStatements() {
        try {
            if (PostgresDatabase.insertSQL != null) {
                PostgresDatabase.insertSQL.close();
                PostgresDatabase.insertSQL = null;
            }
            if (PostgresDatabase.selectSQL != null) {
                PostgresDatabase.selectSQL.close();
                PostgresDatabase.selectSQL = null;
            }
            if (PostgresDatabase.updateSQL != null) {
                PostgresDatabase.updateSQL.close();
                PostgresDatabase.updateSQL = null;
            }
        } catch (SQLException e) {
            LOGGER.error("Prepared statements could not be closed.", e);
            return false;
        }
        return true;
    }

    @Override
    public void shutdown() {
        boolean isClosedPreparedStatements = false;
        boolean isClosedConnection = false;

        try {
            if (PostgresDatabase.conn != null) {
                isClosedPreparedStatements = closePreparedStatements();
                Properties props = new Properties();
                props.setProperty("shutdown", "true");
                PostgresDatabase.conn = DriverManager.getConnection(PostgresDatabase.connectionURL, props);
                PostgresDatabase.conn.close();
                PostgresDatabase.conn = null;
                isClosedConnection = true;
                PostgresDatabase.db = null;
            }
        } catch (SQLException e) {
            LOGGER.error("Error occured while closing Postgres database connection: "
                    + "closed prepared statements?" + isClosedPreparedStatements
                    + ";closed connection?" + isClosedConnection, e);
            return;
        } finally {
            try {
                if (PostgresDatabase.conn != null) {
                    try {
                        PostgresDatabase.conn.close();
                    } catch (SQLException e) {
                        LOGGER.warn("Postgres database connection was not closed successfully during shutdown", e);
                    }
                    PostgresDatabase.conn = null;
                }
            } finally {
                System.gc();
            }
        }
        LOGGER.info("Postgres database connection is closed succesfully");
    }

    @Override
    public InputStream lookupResponse(String id) {
        InputStream result = null;
        if (null != id) {
            if (!id.toLowerCase().contains("output")) {
                result = super.lookupResponse(id);
            } else {
                if (Boolean.parseBoolean(getDatabaseProperties("saveResultsToDB"))) {
                    result = super.lookupResponse(id);
                } else {
                    File responseFile = lookupResponseAsFile(id);
                    if (responseFile != null && responseFile.exists()) {
                        LOGGER.debug("Response file for {} is {}", id, responseFile.getPath());
                        try {
                            result = responseFile.getName().endsWith(SUFFIX_GZIP) ? new GZIPInputStream(new FileInputStream(responseFile))
                                    : new FileInputStream(responseFile);
                        } catch (FileNotFoundException e) {
                            LOGGER.warn("Response not found for id " + id, e);
                        } catch (IOException e) {
                            LOGGER.warn("Error processing response for id " + id, e);
                        }
                    }
                    LOGGER.warn("Response not found for id {}", id);
                }
            }
        }
        return result;
    }

    @Override
    public File lookupResponseAsFile(String id) {
        File result = null;
        InputStream responseStream = super.lookupResponse(id);
        try {
            String fileLocation = IOUtils.toString(responseStream);
            result = new File(new URI(fileLocation));
        } catch (IOException e) {
            LOGGER.warn("Could not get file location for response file for id " + id, e);
        } catch (URISyntaxException e) {
            LOGGER.warn("Could not get file location for response file for id " + id, e);
        } finally {
            if (null != responseStream) {
                try {
                    responseStream.close();
                } catch (IOException e) {
                    LOGGER.debug("Could not close input stream", e);
                }
            }
        }

        return result;
    }
    
    private class WipeTimerTask extends TimerTask {

        public final long thresholdMillis;

        WipeTimerTask(long thresholdMillis) {
            this.thresholdMillis = thresholdMillis;
        }

        @Override
        public void run() {
            Boolean savingResultsToDB = Boolean.parseBoolean(getDatabaseProperties("saveResultsToDB"));
            wipe(thresholdMillis, savingResultsToDB);
        }

        private void wipe(long thresholdMillis, Boolean saveResultsToDB) {
            // SimpleDataFormat is not thread-safe.
            long currentTimeMillis = System.currentTimeMillis();
            LOGGER.info(getDatabaseName() + " Postgres wiper, checking for records older than {} ms",
                    thresholdMillis);

            List<String> oldRecords = findOldRecords(currentTimeMillis, thresholdMillis);
            if (oldRecords.size() > 0) {
                // Clean up files on disk if needed
                if (!saveResultsToDB) {
                    for (String recordId : oldRecords) {
                        if (recordId.toLowerCase().contains("output")) {
                            deleteFileOnDisk(recordId);
                        }
                    }
                }

                // Clean up records in database 
                Integer recordsDeleted = deleteRecords(oldRecords);
                LOGGER.info("Cleaned {} records from database", recordsDeleted);
                
            }
        }

        private Boolean deleteFileOnDisk(String id) {
            Boolean deleted = false; 

            File fileToDelete = new File(BASE_DIRECTORY, id);

            if (fileToDelete.exists()) {
                try {
                    Files.delete(fileToDelete.toPath());
                    deleted = true;
                } catch (IOException ex) {
                    LOGGER.warn("{} could not be deleted. Reason: {}", fileToDelete.toURI().toString(), ex.getMessage());
                }
            }

            return deleted;
        }

        private Integer deleteRecords(List<String> recordIds) {
            Integer deletedRecordsCount = 0;
            PreparedStatement deleteStatement = null;

            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < recordIds.size(); i++) {
                builder.append("?,");
            }

            try {

                String deleteStatementString = "DELETE FROM RESULTS "
                        + "WHERE RESULTS.REQUEST_ID IN (" + builder.deleteCharAt(builder.length() - 1).toString() + ")";
                deleteStatement = PostgresDatabase.conn.prepareStatement(deleteStatementString);

                int idIdx = 1;
                for (String id : recordIds) {
                    deleteStatement.setString(idIdx, id);
                    idIdx++;
                }
                deletedRecordsCount = deleteStatement.executeUpdate();
                PostgresDatabase.conn.commit();
            } catch (SQLException ex) {
                LOGGER.warn("Could not delete rows from Postgres database", ex);
            } finally {
                if (null != deleteStatement) {
                    try {
                        deleteStatement.close();
                    } catch (SQLException e) {
                        LOGGER.warn("Postgres Wiper: Could not close prepared statement", e);
                    }
                }
            }

            return deletedRecordsCount;
        }

        private List<String> findOldRecords(long currentTimeMillis, long threshold) {
            PreparedStatement lookupStatement = null;
            ResultSet rs = null;
            List<String> matchingRecords = new ArrayList<String>();
            try {
                long ageMillis = currentTimeMillis - thresholdMillis;
                String lookupStatementString = "SELECT * FROM "
                        + "(SELECT REQUEST_ID, EXTRACT(EPOCH FROM REQUEST_DATE) * 1000 AS TIMESTAMP "
                        + "FROM RESULTS) items "
                        + "WHERE TIMESTAMP < ?";
                lookupStatement = PostgresDatabase.conn.prepareStatement(lookupStatementString);
                lookupStatement.setLong(1, ageMillis);
                rs = lookupStatement.executeQuery();

                while (rs.next()) {
                    matchingRecords.add(rs.getString(1));
                }

            } catch (SQLException ex) {
                LOGGER.warn("");
            } finally {
                if (null != rs) {
                    try {
                        rs.close();
                    } catch (SQLException e) {
                        LOGGER.warn("Postgres Wiper: Could not close result set", e);
                    }
                }

                if (null != lookupStatement) {
                    try {
                        lookupStatement.close();
                    } catch (SQLException e) {
                        LOGGER.warn("Postgres Wiper: Could not close prepared statement", e);
                    }
                }
            }
            return matchingRecords;
        }
    }


    @Override
    public String generateRetrieveResultURL(String id) {
        return baseResultURL + id;
    }

    @Override
    public Connection getConnection() {
        return PostgresDatabase.conn;
    }

    @Override
    public String getConnectionURL() {
        return PostgresDatabase.connectionURL;
    }

	@Override
	public InputStream lookupStatus(String request_id) {
		return null;
	}

}
