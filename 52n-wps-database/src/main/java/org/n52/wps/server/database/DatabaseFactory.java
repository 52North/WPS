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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.inject.Inject;

import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.ExceptionReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class DatabaseFactory implements IDatabase
{
	private static Logger LOGGER = LoggerFactory.getLogger(DatabaseFactory.class);
	// Property of the name of the database. Used to define the database implementation.
	public static final String PROPERTY_NAME_DATABASE_CLASS_NAME = "databaseClass";
	private static IDatabase database;

    private static PropertyChangeListener propertyChangeListener;
    @Inject
	private static WPSConfig wpsConfig;
	
	public static IDatabase getDatabase() {

        // FvK: create and register listener to the WPSConfig if not yet happend.
        if (propertyChangeListener == null){
            propertyChangeListener =  new PropertyChangeListener(){
                public void propertyChange(
                    final PropertyChangeEvent propertyChangeEvent) {
                        //shutdown Database connection and instance
                        DatabaseFactory.database.shutdown();
                        DatabaseFactory.database = null;
                        DatabaseFactory.getDatabase();
                        LOGGER.info(this.getClass().getName() + ": Received Property Change Event: " + propertyChangeEvent.getPropertyName());
                    }
            }; 
            wpsConfig.addPropertyChangeListener(org.n52.wps.commons.WPSConfig.WPSCONFIG_PROPERTY_EVENT_NAME, propertyChangeListener);
        }

        if(DatabaseFactory.database == null) {
			try {
				String databaseClassName = 
					AbstractDatabase.getDatabaseProperties(PROPERTY_NAME_DATABASE_CLASS_NAME);
				// if databaseClassName is not defined take derby.
				if(databaseClassName == null || databaseClassName.equals("")) {
					LOGGER.info("Database class name was not found in properties. FlatFileDatabase will be used.");
					databaseClassName = "org.n52.wps.server.database.FlatFileDatabase";
				}
				Class<?> cls = Class.forName(databaseClassName, true, DatabaseFactory.class.getClassLoader());
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

	@Override
    protected void finalize() throws Throwable {
		super.finalize();
		DatabaseFactory.database.shutdown();
	}
	
	/**
	 * Shutdown the database in a clean, safe way.
	 */
	@Override
    public void shutdown() {
		DatabaseFactory.database.shutdown();
	}
    
    /**
	 * Insert a new Request into the Database.
	 * 
	 * @param request  The Response to insert.
	 * @see #storeResponse(Response)
	 */
    @Override
    public synchronized void insertRequest(String id, InputStream inputStream, boolean xml) {
		DatabaseFactory.database.insertRequest(id, inputStream, xml);
	}

	/**
	 * Insert a new Response into the Database.
	 * 
	 * @param response  The Response to insert.
	 * @see #storeResponse(Response)
	 */
    @Override
    public synchronized String insertResponse(String id, InputStream outputStream) {
		return DatabaseFactory.database.insertResponse(id, outputStream);
	}

	/**
	 * Update the Response in the Database, based on the Identifier.
	 * 
	 * @param response  The Response to update
	 * @see #storeResponse(Response)
	 */
    @Override
    public synchronized void updateResponse(String id, InputStream outputStream) {
		DatabaseFactory.database.updateResponse(id, outputStream);
	}

	/**
	 * Store the Response of a deferred Request. It either gets inserted into
	 * the database, or it updates a previous Response, based on the identifier.
	 * 
	 * @param response  The Response to store.
	 */
    @Override
    public synchronized String storeResponse(String id, InputStream outputStream) {
		return DatabaseFactory.database.storeResponse(id, outputStream);
	}

    @Override
    public synchronized InputStream lookupRequest(String request_id) {
		return DatabaseFactory.database.lookupRequest(request_id);
	}
    
	/**
	 * Retrieve the Response on a previous Request, based on an unique
	 * identifier, which was already given to the client for reference.
	 * 
	 * @param request_id  The identifier of the Request
	 * @return null, if an SQLException occurred, else an InputStream with the
	 *         Response
	 */
	@Override
    public synchronized InputStream lookupResponse(String request_id) {
		return DatabaseFactory.database.lookupResponse(request_id);
	}
	
	@Override
    public synchronized String storeComplexValue(String id, InputStream stream, String type, String mimeType) {
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
	@Override
    public String generateRetrieveResultURL(String id) {
		return DatabaseFactory.database.generateRetrieveResultURL(id);
	}
	
	/** 
	 * Returns some name for the database, like DERBY, HSQL, ...
	 */
	@Override
    public String getDatabaseName() {
		return DatabaseFactory.database.getDatabaseName();
	}

    @Override
	public String getMimeTypeForStoreResponse(String id) {
		return DatabaseFactory.database.getMimeTypeForStoreResponse(id);
	}
	
    @Override
	public long getContentLengthForStoreResponse(String id) {
		return DatabaseFactory.database.getContentLengthForStoreResponse(id);
	}

    @Override
	public boolean deleteStoredResponse(String id) {
		return DatabaseFactory.database.deleteStoredResponse(id);
	}

    @Override
	public File lookupRequestAsFile(String id) {
		return DatabaseFactory.database.lookupRequestAsFile(id);
	}

    @Override
	public File lookupResponseAsFile(String id) {
		return DatabaseFactory.database.lookupResponseAsFile(id);
	}

	@Override
	public InputStream lookupStatus(String request_id) throws ExceptionReport {
		return DatabaseFactory.database.lookupStatus(request_id);
	}
	
}
