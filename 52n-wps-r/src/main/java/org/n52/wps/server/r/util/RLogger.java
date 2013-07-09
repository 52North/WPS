/**
 * ﻿Copyright (C) 2010
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */

package org.n52.wps.server.r.util;

import java.text.DateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

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
        evalString.append("cat(\"[WPS4R @ ");
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

    private static String prepareMessage(String message) {
        // return message.replace("\"", "\\\"");
        return new String(message);
    }

}
