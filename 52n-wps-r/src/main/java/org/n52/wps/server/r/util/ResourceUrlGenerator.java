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

import java.net.MalformedURLException;
import java.net.URL;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.r.RResource;
import static org.n52.wps.server.r.RResource.R_ENDPOINT;
import org.n52.wps.server.r.data.R_Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:d.nuest@52north.org">Daniel Nüst</a>
 */
public class ResourceUrlGenerator {

    protected static final Logger log = LoggerFactory.getLogger(ResourceUrlGenerator.class);

    private static final CharSequence SLASH_REPLACEMENT = "$subdir$";

    private static URL ERROR_SESSION_INFO_URL;

    static {
        try {
            ERROR_SESSION_INFO_URL = new URL("http://internal.error/sessionInfo.not.available");
        } catch (MalformedURLException e) {
            log.error("cannot create fallback URL", e);
        }
    }

    private final String baseURL;

    public ResourceUrlGenerator(String baseURL) {
        this.baseURL = baseURL;
    }

    /**
     *
     * @param resource the <code>R_Resource</code>
     * @return a publicly available URL to retrieve the resource
     * @throws ExceptionReport if the resource URL could not be created
     */
    public URL getResourceURL(R_Resource resource) throws ExceptionReport {
        StringBuilder sb = new StringBuilder();
        sb.append(baseURL).append(R_ENDPOINT);
        sb.append(RResource.RESOURCE_PATH).append("/");
        sb.append(resource.getProcessId());
        String resourceForUrl = internalEncode(resource.getResourceValue());
        try {
            sb.append("/").append(resourceForUrl);
            log.trace("Created url {} for resource {}", resourceForUrl, resource);
            return new URL(sb.toString());
        } catch (MalformedURLException e) {
            throw new ExceptionReport("Could not create resource url", ExceptionReport.NO_APPLICABLE_CODE, e);
        }
    }

    /**
     *
     * @param wkn
     *        well-known name for a process
     * @return a publicly available URL to retrieve the process script
     * @throws MalformedURLException if the URL was malformed
     * @throws ExceptionReport if the script URL could not be created
     */
    public URL getScriptURL(String wkn) throws MalformedURLException, ExceptionReport {
        StringBuilder sb = new StringBuilder();
        sb.append(baseURL).append(R_ENDPOINT);
        sb.append(RResource.SCRIPT_PATH).append("/").append(wkn);
        return new URL(sb.toString());
    }

    /**
     *
     * @param resource
     *        the <code>R_Resource</code>
     * @return a publicly available URL to retrieve the imported script
     * @throws ExceptionReport if the import URL could not be created
     */
    public URL getImportURL(R_Resource resource) throws ExceptionReport {
        StringBuilder sb = new StringBuilder();
        sb.append(baseURL).append(R_ENDPOINT);
        sb.append(RResource.IMPORT_PATH).append("/");
        sb.append(resource.getProcessId());
        String resourceForUrl = internalEncode(resource.getResourceValue());
        try {
            sb.append("/").append(resourceForUrl);
            return new URL(sb.toString());
        } catch (MalformedURLException e) {
            throw new ExceptionReport("Could not create import url", ExceptionReport.NO_APPLICABLE_CODE, e);
        }
    }

    /**
     * @return the service endpoint to retrieve a textual representation of the sessionInfo() function in R.
     */
    public URL getSessionInfoURL() {
        StringBuilder sb = new StringBuilder();
        sb.append(baseURL).append(R_ENDPOINT).append(RResource.SESSION_INFO_PATH);
        try {
            return new URL(sb.toString());
        } catch (MalformedURLException e) {
            log.error("Could not create URL for session info, returning fallback URL", e);
            return ERROR_SESSION_INFO_URL;
        }
    }

    public String internalDecode(String s) {
        return s.replace(SLASH_REPLACEMENT, "/");
    }

    public String internalEncode(String s) {
        return s.replace("/", SLASH_REPLACEMENT);
    }

}
