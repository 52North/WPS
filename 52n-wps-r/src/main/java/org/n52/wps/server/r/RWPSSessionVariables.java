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
package org.n52.wps.server.r;

/**
 * 
 * names of the variables that are put into the R session prior to execution of a script.
 * 
 * TODO: create a complex object called "wps" with the slots processDescriptionLink, serverRuntime (52N WPS
 * build version), serverName, resourceBaseUrl, scriptBaseUrl, ...
 * 
 * @author Daniel Nüst
 * 
 */
public class RWPSSessionVariables {

    public static final String PROCESS_DESCRIPTION = "wpsProcessDescription";

    public static final String WPS_SERVER = "wpsServer";
    
    public static final String WPS_SERVER_NAME = "wpsServerName";

    public static final String ERROR_MESSAGE = "wpsErrorMessage";

    public static final String RESOURCES_ENDPOINT = "wpsResourceEndpoint";

    public static final String SCRIPT_URL = "wpsScriptUrl";

    public static final String SCRIPT_RESOURCES = "wpsScriptResources";

    public static final String WARNING_OUTPUT_STORAGE = "wpsWarningStorage";

}
