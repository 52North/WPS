/**
 * Copyright (C) 2010-2015 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.server.r.util;

import java.io.IOException;
import java.util.Arrays;

import org.n52.wps.server.r.FilteredRConnection;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RConnector {

    private static final long START_ATTEMPT_SLEEP = 1000l;

    private static final int START_ATTEMP_COUNT = 5;

    private static Logger log = LoggerFactory.getLogger(RConnector.class);

    private RStarter starter;

    public RConnector(RStarter starter) {
        this.starter = starter;
    }

    public FilteredRConnection getNewConnection(boolean enableBatchStart,
                                                String host,
                                                int port,
                                                String user,
                                                String password) throws RserveException {
        FilteredRConnection con = null;
        log.debug("Creating new RConnection");
        con = getNewConnection(enableBatchStart, host, port);

        // Login MUSST be the next request after connection
        // otherwise the connection is broken even if login is requested later
        if (con != null && con.needLogin()) {
            log.debug("Connection requires login... logging in with user {}", user);
            con.login(user, password);
        }

        RLogger.log(con, "New connection from WPS4R");
        REXP info = con.eval("capture.output(sessionInfo())");
        try {
            log.debug("NEW CONNECTION >>> sessionInfo:\n{}", Arrays.deepToString(info.asStrings()));
        }
        catch (REXPMismatchException e) {
            log.warn("Error creating session info.", e);
        }

        return con;
    }

    private FilteredRConnection getNewConnection(boolean enableBatchStart, String host, int port) throws RserveException {
        log.debug("New connection using batch = {} at {}:{}", enableBatchStart, host, port);

        FilteredRConnection con = null;
        try {
            con = new FilteredRConnection(host, port);
        }
        catch (RserveException rse) {
            log.debug("Could not connect to RServe, maybe it is not started yet? Exception messag is '{}'",
                      rse.getMessage());

            if (rse.getMessage().startsWith("Cannot connect") && enableBatchStart) {
                try {
                    con = attemptStarts(host, port);
                }
                catch (RuntimeException | InterruptedException | IOException e) {
                    log.error("Attempted to start Rserve and establish a connection failed", e);
                }
            }
            else {
                log.trace("Batch start is disabled!");
                throw rse;
            }
        }

        if (con == null)
            throw new RserveException(null,
                                      "Cannot start or connect with Rserve. Is Rserve installed and configured for remote connections? It is not by default. See http://www.rforge.net/Rserve/doc.html");

        return con;
    }

    private FilteredRConnection attemptStarts(String host, int port) throws InterruptedException,
            IOException,
            RserveException {
        log.info("Attempting to start RServe.");

        int attempt = 1;
        FilteredRConnection con = null;
        while (attempt <= START_ATTEMP_COUNT) {
            this.starter.startR();

            try {
                Thread.sleep(START_ATTEMPT_SLEEP); // wait for R to startup,
                                                   // then establish connection
                con = new FilteredRConnection(host, port);
                log.info("Started R, connection is {}", con);
                break;
            }
            catch (RserveException rse) {
                if (attempt >= START_ATTEMP_COUNT) {
                    throw rse;
                }
            }
            attempt++;
        }
        log.info("Started R, connection is {}", con);        return con;
    }

}
