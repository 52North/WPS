/**
 * ﻿Copyright (C) 2010 - 2014 52°North Initiative for Geospatial Open Source
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

package org.n52.wps.server.r;

import java.util.HashSet;

import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.r.util.RLogger;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An RConnection that can be used to filter certain commands for security reasons using the
 * <code>filteredEval()</code> function. If the command passes the filter or the regular <code>eval()</code>
 * is used, then this class is a simple wrapper around {@link RConnection}.
 * 
 * @author Daniel Nüst
 * 
 */
public class FilteredRConnection extends RConnection {

    public static interface RInjectionFilter {

        public abstract String filter(String command) throws ExceptionReport;

    }

    /**
     * dangerous types of commands: system(), unlink(), setwd(), quit(), ...
     * 
     * "";quit("no"); does not start with quit, so check with contains.
     */
    public class StringContainsInjectionFilter implements RInjectionFilter {

        private HashSet<String> illegalCommands = new HashSet<String>();

        public StringContainsInjectionFilter() {
            illegalCommands.add("eval");
            illegalCommands.add("system");
            illegalCommands.add("unlink");
            illegalCommands.add("setwd");
            illegalCommands.add("quit");
            illegalCommands.add("q(");
        }

        @Override
        public String filter(String command) throws ExceptionReport {
            for (String illegal : this.illegalCommands) {
                if (command.contains(illegal))
                    throw new ExceptionReport("Input is not allowed: " + command,
                                              ExceptionReport.INVALID_PARAMETER_VALUE);
            }

            return command;
        }

    }

    private static final String EMPTY_RESULT = "NA";

    private static Logger log = LoggerFactory.getLogger(FilteredRConnection.class);

    private boolean failOnFilter = true;

    private RInjectionFilter filter;

    private boolean forceFilter;

    private boolean logAllEval = true;

    public FilteredRConnection(RInjectionFilter filter, String host, int port) throws RserveException {
        super(host, port);
        this.filter = filter;
    }

    public FilteredRConnection(String host, int port) throws RserveException {
        super(host, port);
        this.filter = new StringContainsInjectionFilter();
    }

    @Override
    public boolean close() {
        log.debug("[R] closing connection.");
        RLogger.log(this, "Closing connection.");

        return super.close();
    }

    @Override
    public REXP eval(REXP arg0, REXP arg1, boolean arg2) throws REngineException {
        log.warn("Unfiltered command (filtering for this function not implemented.");
        return super.eval(arg0, arg1, arg2);
    }

    @Override
    public REXP eval(String arg0) throws RserveException {
        if (forceFilter)
            return filteredEval(arg0);
        else
            return internalEval(arg0);
    }

    /**
     * logs filtered commands.
     * 
     * @param arg0
     * @return
     * @throws RserveException
     */
    public REXP filteredEval(String arg0) throws RserveException {
        try {
            String command = this.filter.filter(arg0);

            return internalEval(command);
        }
        catch (ExceptionReport e) {
            log.error("Illegal command {}", arg0, e);
            // would be nice to add a warning into the R session
            // super.eval("warning( \"" + e.getMessage() + "\" )");

            if (failOnFilter)
                throw new RserveException(this, "Illegal command: " + e.getMessage());

            log.debug("Filtered removed the command '{}', but not failing, returning {}", arg0, EMPTY_RESULT);
            return internalEval(EMPTY_RESULT);
        }
    }

    /**
     * internal eval method to put the logging code in one place
     */
    private REXP internalEval(String command) throws RserveException {
        if (logAllEval)
            log.debug("[R] {}", command);

        return super.eval(command);
    }

    public boolean isForceFilter() {
        return forceFilter;
    }

    public void setForceFilter(boolean forceFilter) {
        this.forceFilter = forceFilter;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("FilteredRConnection [");
        if (filter != null) {
            builder.append("filter=");
            builder.append(filter);
            builder.append(", ");
        }
        builder.append(", rsrvVersion=");
        builder.append(rsrvVersion);
        builder.append(", super=");
        builder.append(super.toString());
        builder.append("]");
        return builder.toString();
    }

}
