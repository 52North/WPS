/**
 * ﻿Copyright (C) 2006 - 2019 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
