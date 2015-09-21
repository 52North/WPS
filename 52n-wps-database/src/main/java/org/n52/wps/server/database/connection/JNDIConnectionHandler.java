/**
 * Copyright (C) 2007 - 2014 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.server.database.connection;

import java.sql.Connection;
import java.sql.SQLException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * @author abramhall (Arthur Bramhall, USGS)
 */
public class JNDIConnectionHandler implements ConnectionHandler {

	private final DataSource dataSource;

	/**
	 * Create a new JNDI Connection Handler.
	 *
	 * @param jndiName the name used by the container to tie to the database
	 * @throws NamingException
	 */
	public JNDIConnectionHandler(String jndiName) throws NamingException {
		InitialContext context = new InitialContext();
		dataSource = (DataSource) context.lookup("java:comp/env/jdbc/" + jndiName);
	}

	/**
	 * Gets a connection from the database. Attempts to retrieve a new
	 * connection from the connection pool
	 *
	 * @return
	 * @throws SQLException
	 */
	@Override
	public Connection getConnection() throws SQLException {
		Connection conn = dataSource.getConnection();
		return conn;
	}
}
