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

public enum RWPSConfigVariables {

    SCRIPT_DIR, RESOURCE_DIR, ALGORITHM_PROPERTY_NAME, ENABLE_BATCH_START, RSERVE_HOST, RSERVE_PORT, RSERVE_USER, RSERVE_PASSWORD, R_DATATYPE_CONFIG, R_WORK_DIR_STRATEGY, R_WORK_DIR_NAME, R_CACHE_PROCESSES, R_SESSION_MEMORY_LIMIT, R_CLEAN_UP_WORK_DIR, R_UTILS_DIR, R_ENABLE_RESOURCE_DOWNLOAD, R_ENABLE_IMPORT_DOWNLOAD, R_ENABLE_SCRIPT_DOWNLOAD, R_ENABLE_SESSION_INFO_DOWNLOAD;

    @Override
    public String toString() {
        switch (this) {
        case R_WORK_DIR_STRATEGY:
            return "R_wdStrategy";
        case R_WORK_DIR_NAME:
            return "R_wdName";
        case SCRIPT_DIR:
            return "R_scriptDirectory";
        case RESOURCE_DIR:
            return "R_resourceDirectory";
        case ALGORITHM_PROPERTY_NAME:
            return "Algorithm";
        case ENABLE_BATCH_START:
            return "R_enableBatchStart";
        case RSERVE_HOST:
            return "R_RserveHost";
        case RSERVE_PORT:
            return "R_RservePort";
        case RSERVE_USER:
            return "R_RserveUser";
        case RSERVE_PASSWORD:
            return "R_RservePassword";
        case R_DATATYPE_CONFIG:
            return "R_datatypeConfig";
        case R_CACHE_PROCESSES:
            return "R_cacheDescriptions";
        case R_SESSION_MEMORY_LIMIT:
            return "R_session_memoryLimit";
        case R_UTILS_DIR:
            return "R_utilsScriptDirectory";
        case R_ENABLE_RESOURCE_DOWNLOAD:
            return "R_enableResourceDownload";
        case R_ENABLE_IMPORT_DOWNLOAD:
            return "R_enableImportDownload";
        case R_ENABLE_SCRIPT_DOWNLOAD:
            return "R_enableScriptDownload";
        case R_ENABLE_SESSION_INFO_DOWNLOAD:
            return "R_enableSessionInfoDownload";
        default:
            return "NO STRING REPRESENTATION DEFINED FOR ENUM CONSTANT!";
        }

    }
}
