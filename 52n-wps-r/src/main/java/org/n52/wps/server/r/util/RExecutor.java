/*
 * Copyright (C) 2010-2017 52°North Initiative for Geospatial Open Source
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.r.RWPSSessionVariables;
import org.n52.wps.server.r.syntax.RAnnotationException;
import org.n52.wps.server.r.syntax.RegExp;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RExecutor {

    private static final String COMMENT_CHARACTER = "#";

    private static Logger log = LoggerFactory.getLogger(RExecutor.class);

    private boolean debugScript = true; // TODO make configurable property

    private boolean appendSwitchedOffCommandsAsComments = false; // TODO make configurable property

    private boolean appendComments = false; // TODO make configurable property

    private InputStream openScriptStream(File rScriptFile) throws ExceptionReport, RserveException {
        InputStream rScriptStream = null;

        try {
            rScriptStream = new FileInputStream(rScriptFile);
        }
        catch (IOException e) {
            log.error("Error reading script file.", e);
            throw new ExceptionReport("Could not read script file " + rScriptFile,
                                      ExceptionReport.NO_APPLICABLE_CODE,
                                      e);
        }

        return rScriptStream;
    }


    /**
     * @param script
     *        R input script
     * @param rCon
     *        Connection - should be open usually / otherwise it will be opened and closed separately
     * @return true if read was successful
     * @throws RserveException if an exception occurred while executing the script
     * @throws IOException if an exception occurred while executing the script
     * @throws RAnnotationException if the script was invalid
     * @throws ExceptionReport if an exception occurred while executing the script
     */
    public boolean executeScript(File script, RConnection rCon) throws RserveException,
            IOException,
            RAnnotationException,
            ExceptionReport {
        log.debug("Executing script...");

        InputStream rScriptStream = openScriptStream(script);

        boolean success = true;

        BufferedReader fr = new BufferedReader(new InputStreamReader(rScriptStream));
        if ( !fr.ready()) {
            return false;
        }

        // reading script:
        StringBuilder scriptExecutionString = new StringBuilder();

        // surrounds R script with try / catch block in R
        scriptExecutionString.append("error = try({ \n");
        // wrapper to retrieve warnings (workaround, because warnings() reliable
        // for Rserve and often returns NULL)
        scriptExecutionString.append("withCallingHandlers({\n\n");

        // is set true when wps.off-annotations occur
        // this indicates that parts of the script shall not pass to Rserve
        boolean wpsoff_state = false;

        while (fr.ready()) {
            String line = fr.readLine();
            if (line.isEmpty()) {
                continue;
            }

            if (line.contains(RegExp.WPS_OFF) && line.contains(RegExp.WPS_ON)) {
                throw new RAnnotationException("Invalid R-script: Only one wps.on; / wps.off; expression per line!");
            }

            if (line.contains(RegExp.WPS_OFF)) {
                wpsoff_state = true;
            }
            else if (line.contains(RegExp.WPS_ON)) {
                wpsoff_state = false;
            }
            else if (wpsoff_state) {
                if (appendSwitchedOffCommandsAsComments) {
                    line = "# (ignored by " + RegExp.WPS_OFF + ") " + line;
                }
            }
            else {
                // not switched off:
                if (line.trim().startsWith(COMMENT_CHARACTER) && line.contains("updateStatus")) {
                    //remove comment in front of updateStatus call for execution
                    line = line.replaceFirst("#", "").trim();
                    scriptExecutionString.append(line);
                    scriptExecutionString.append("\n");
                }else if (line.trim().startsWith(COMMENT_CHARACTER)) {
                    if (appendComments) {
                        scriptExecutionString.append(line);
                    }
                }
                else {
                    // actually append the line
                    if (line.contains("setwd(")) {
                        log.warn("The running R script contains a call to \"setwd(...)\". "
                                + "This may cause runtime-errors and unexpected behaviour of WPS4R. "
                                + "It is strongly advised to not use this function in process scripts.");
                    }

                    scriptExecutionString.append(line);
                    scriptExecutionString.append("\n");
                }
            }
        }

        // apply handler to retrieve warnings:
        scriptExecutionString.append("\n}, warning = function(w) {\n  ");
        scriptExecutionString.append(RWPSSessionVariables.WARNING_OUTPUT_STORAGE);
        scriptExecutionString.append(" = get(\"");
        scriptExecutionString.append(RWPSSessionVariables.WARNING_OUTPUT_STORAGE);
        scriptExecutionString.append("\", envir = .GlobalEnv);");
        scriptExecutionString.append("\n  ");
        scriptExecutionString.append(RWPSSessionVariables.WARNING_OUTPUT_STORAGE);
        scriptExecutionString.append(" = append(");
        scriptExecutionString.append(RWPSSessionVariables.WARNING_OUTPUT_STORAGE);
        scriptExecutionString.append(", w$message);");
        scriptExecutionString.append("\n");
        scriptExecutionString.append("  assign(\"");
        scriptExecutionString.append(RWPSSessionVariables.WARNING_OUTPUT_STORAGE);
        scriptExecutionString.append("\", ");
        scriptExecutionString.append(RWPSSessionVariables.WARNING_OUTPUT_STORAGE);
        scriptExecutionString.append(", envir = .GlobalEnv);");
        scriptExecutionString.append("\n");
        scriptExecutionString.append("})\n"); // for "withCallingHandlers"
        scriptExecutionString.append("});\n\n"); // for "try{..."
        scriptExecutionString.append("hasError <- class(error) == \"try-error\" ");
        scriptExecutionString.append("\n");
        scriptExecutionString.append("if(hasError) ");
        scriptExecutionString.append(RWPSSessionVariables.ERROR_MESSAGE);
        scriptExecutionString.append(" <- as.character(error)");
        scriptExecutionString.append("\n");

        if (this.debugScript && log.isDebugEnabled()) {
            log.debug(scriptExecutionString.toString());
        }

        // call the actual script here
        rCon.eval(scriptExecutionString.toString());

        try {
            // handling internal R errors:
            if (rCon.eval("hasError").asInteger() == 1) {
                String message = "An R error occured while executing R script: \n"
                        + rCon.eval(RWPSSessionVariables.ERROR_MESSAGE).asString();
                log.error(message);
                success = false;
                throw new ExceptionReport(message, ExceptionReport.REMOTE_COMPUTATION_ERROR);
            }
        }
        catch (REXPMismatchException e) {
            log.error("Error handling during R script execution failed.", e);
            success = false;
        }
        finally {
            if (rScriptStream != null) {
                try {
                    rScriptStream.close();
                }
                catch (IOException e) {
                    log.error("Connection to R script cannot be closed for process file {}", script);
                }
            }
        }

        return success;
    }

}
