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

import java.text.DateFormat;
import java.util.Date;

import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RLogger {

    private static Logger LOGGER = LoggerFactory.getLogger(RLogger.class);

    private static DateFormat format = DateFormat.getDateTimeInstance();

    public static void logGenericRProcess(RConnection rCon, String message) {
        String msg = prepareMessage(message);

        StringBuilder evalString = new StringBuilder();
        evalString.append("cat(\"[GenericRProcess @ ");
        evalString.append(format.format(new Date(System.currentTimeMillis())));
        evalString.append("] ");
        evalString.append(msg);
        evalString.append("\\n\")");

        try {
            rCon.eval(evalString.toString());
        }
        catch (RserveException e) {
            LOGGER.warn("Could not log message '" + msg + "'", e);
        }
    }

    public static void log(RConnection rCon, String message) {
        String msg = prepareMessage(message);

        StringBuilder evalString = new StringBuilder();
        evalString.append("cat(");
        appendPre(evalString);
        evalString.append(" ");
        evalString.append(msg);
        evalString.append("\\n\")");

        logIt(rCon, evalString);
    }

    private static void logIt(RConnection rCon, String evalString) {
        try {
            rCon.eval(evalString);
        }
        catch (RserveException e) {
            LOGGER.warn("Could not log message '{}'", evalString.toString(), e);
        }
    }

    private static void logIt(RConnection rCon, StringBuilder evalString) {
        logIt(rCon, evalString.toString());
    }

    private static void appendPre(StringBuilder evalString) {
        evalString.append("\"[WPS4R @ ");
        evalString.append(format.format(new Date(System.currentTimeMillis())));
        evalString.append("]");
    }

    private static String prepareMessage(String message) {
        // return message.replace("\"", "\\\"");
        return new String(message);
    }

    public static void logVariable(RConnection rCon, String var) {
        StringBuilder evalString = new StringBuilder();
        evalString.append("cat(");
        appendPre(evalString);
        evalString.append("\"");
        evalString.append(", ");
        
        evalString.append("\"");
        evalString.append(var);
        evalString.append(" =\", ");
        
        evalString.append("toString(");
        evalString.append(var);
        evalString.append(")");
        
        evalString.append(", \"");
        evalString.append("\\n\")");
        logIt(rCon, evalString);
    }

    public static void logSessionContent(RConnection rCon) {
        logIt(rCon, "cat(paste(capture.output(ls()), collapse = \"\\n\"), \"\n\")");
    }

    public static void logWorkspaceContent(RConnection rCon) {
        logIt(rCon, "cat(paste(capture.output(list.files(getwd())), collapse = \"\\n\"), \"\n\")");
    }

}
