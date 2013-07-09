/**
 * ﻿Copyright (C) 2006
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

package org.n52.wps.commons.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutionContextFactory {

    private static Logger log = LoggerFactory.getLogger(ExecutionContextFactory.class);
    
    private final static ThreadLocal<ExecutionContext> threadContexts = new ThreadLocal<ExecutionContext>();

    private static ExecutionContext defaultContext;

    public static ExecutionContext getContext() {
        return getContext(true);
    }

    public static ExecutionContext getContext(boolean fallBackToDefault) {
        ExecutionContext executionContext = null;
        synchronized (threadContexts) {
            executionContext = threadContexts.get();
            if (executionContext == null && fallBackToDefault) {
                executionContext = getDefault();
            }
        }
        return executionContext;
    }

    public synchronized static ExecutionContext getDefault() {
        if (defaultContext == null) {
            defaultContext = new ExecutionContext();
        }
        return defaultContext;
    }

    public static void registerContext(ExecutionContext context) {
        synchronized (threadContexts) {
            threadContexts.set(context);
        }
        
        log.info("Context registered");
    }

    public static void unregisterContext() {
        synchronized (threadContexts) {
            threadContexts.remove();
        }

        log.info("Context unregistered");
    }
}
