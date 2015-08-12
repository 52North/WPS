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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.IOUtils;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.r.data.CustomDataTypeManager;
import org.n52.wps.server.r.data.R_Resource;
import org.n52.wps.server.r.workspace.RSessionManager;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.io.Files;

/**
 * 
 * A class providing service endpoints to retrieve publicly available resources realted to R script-based
 * algorithms.
 * 
 * This class also provides URL building as static methods.
 * 
 * TODO: support multiple output formats (XML, JSON), see e.g.
 * http://springinpractice.com/2012/02/22/supporting
 * -xml-and-json-web-service-endpoints-in-spring-3-1-using-responsebody
 * 
 * @author Daniel Nüst
 *
 */
@Component
@RequestMapping(RResource.R_ENDPOINT)
public class RResource {

    /**
     * 
     * @param resource
     * @return a publicly available URL to retrieve the resource
     */
    public static URL getResourceURL(R_Resource resource) throws ExceptionReport {
        StringBuilder sb = new StringBuilder();
        sb.append(WPSConfig.getInstance().getServiceBaseUrl()).append(R_ENDPOINT);
        sb.append(RResource.RESOURCE_PATH).append("/");
        sb.append(resource.getProcessId());
        String resourceForUrl = internalEncode(resource.getResourceValue());
        try {
            sb.append("/").append(resourceForUrl);
            return new URL(sb.toString());
        }
        catch (MalformedURLException e) {
            throw new ExceptionReport("Could not create resource url", ExceptionReport.NO_APPLICABLE_CODE, e);
        }
    }

    /**
     * 
     * @param wkn
     *        well-known name for a process
     * @return a publicly available URL to retrieve the process script
     */
    public static URL getScriptURL(String wkn) throws MalformedURLException, ExceptionReport {
        StringBuilder sb = new StringBuilder();
        sb.append(WPSConfig.getInstance().getServiceBaseUrl()).append(R_ENDPOINT);
        sb.append(RResource.SCRIPT_PATH).append("/").append(wkn);
        return new URL(sb.toString());
    }

    /**
     * 
     * @param wkn
     *        well-known name for a process
     * @return a publicly available URL to retrieve the imported script
     */
    public static URL getImportURL(R_Resource resource) throws ExceptionReport {
        StringBuilder sb = new StringBuilder();
        sb.append(WPSConfig.getInstance().getServiceBaseUrl()).append(R_ENDPOINT);
        sb.append(RResource.IMPORT_PATH).append("/");
        sb.append(resource.getProcessId());
        String resourceForUrl = internalEncode(resource.getResourceValue());
        try {
            sb.append("/").append(resourceForUrl);
            return new URL(sb.toString());
        }
        catch (MalformedURLException e) {
            throw new ExceptionReport("Could not create import url", ExceptionReport.NO_APPLICABLE_CODE, e);
        }
    }

    /**
     * @return the service endpoint to retrieve a textual representation of the sessionInfo() function in R.
     */
    public static URL getSessionInfoURL() {
        StringBuilder sb = new StringBuilder();
        WPSConfig conf = WPSConfig.getInstance();
        sb.append(conf.getServiceBaseUrl()).append(R_ENDPOINT).append(RResource.SESSION_INFO_PATH);
        try {
            return new URL(sb.toString());
        }
        catch (MalformedURLException e) {
            log.error("Could not create URL for session info, returning fallback URL", e);
            return ERROR_SESSION_INFO_URL;
        }
    }

    private static String internalDecode(String s) {
        return s.replace(SLASH_REPLACEMENT, "/");
    }

    private static String internalEncode(String s) {
        return s.replace("/", SLASH_REPLACEMENT);
    }

    public static final String R_ENDPOINT = "/r";

    private static final String REQUEST_PARAM_SCRIPTID = "scriptId";

    private static final String REQUEST_PARAM_RESOURCEID = "resourcId";

    public static final String SESSION_INFO_PATH = "/sessionInfo";

    public static final String RESOURCE_PATH = "/resource";

    public static final String IMPORT_PATH = "/import";

    private static final String RESOURCE_PATH_PARAMS = RESOURCE_PATH + "/{" + REQUEST_PARAM_SCRIPTID + ":.+}" + "/{"
            + REQUEST_PARAM_RESOURCEID + ":.+}";

    private static final String IMPORT_PATH_PARAMS = IMPORT_PATH + "/{" + REQUEST_PARAM_SCRIPTID + ":.+}" + "/{"
            + REQUEST_PARAM_RESOURCEID + ":.+}";

    public static final String SCRIPT_PATH = "/script";

    // http://stackoverflow.com/questions/3526523/spring-mvc-pathvariable-getting-truncated
    private static final String SCRIPT_PATH_PARAMS = SCRIPT_PATH + "/{" + REQUEST_PARAM_SCRIPTID + ":.+}";

    private static final CharSequence SLASH_REPLACEMENT = "$subdir$";

    private static URL ERROR_SESSION_INFO_URL;

    protected static Logger log = LoggerFactory.getLogger(RResource.class);

    public static final MediaType ZIP_TYPE = MediaType.valueOf("application/x-zip-compressed");

    private static final String CHARSET_STRING = "; charset=utf-8";

    static {
        try {
            ERROR_SESSION_INFO_URL = new URL("http://internal.error/sessionInfo.not.available");
        }
        catch (MalformedURLException e) {
            log.error("cannot create fallback URL", e);
        }
    }

    @Autowired
    private R_Config config;

    @Autowired
    private ScriptFileRepository scriptRepo;

    @Autowired
    private ResourceFileRepository resourceRepo;

    @Autowired
    private CustomDataTypeManager dataTypeManager;

    public RResource() {
        log.debug("NEW {}", this);
    }

    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE + CHARSET_STRING)
    public ResponseEntity<String> index() {

        StringBuilder sb = new StringBuilder();
        sb.append("R endpoints: \n");
        sb.append(SESSION_INFO_PATH).append("\n");
        sb.append(RESOURCE_PATH_PARAMS).append("\n");
        sb.append(IMPORT_PATH_PARAMS).append("\n");
        sb.append(SCRIPT_PATH_PARAMS).append("\n");
        ResponseEntity<String> entity = new ResponseEntity<String>(sb.toString(), HttpStatus.ACCEPTED);
        return entity;
    }

    @RequestMapping(value = RESOURCE_PATH_PARAMS, method = RequestMethod.GET)
    public ResponseEntity<Resource> getResource(@PathVariable(REQUEST_PARAM_SCRIPTID) String scriptId,
                                                @PathVariable(REQUEST_PARAM_RESOURCEID) String resourceId) throws ExceptionReport {
        if ( !config.isResourceDownloadEnabled())
            return new ResponseEntity<Resource>(new ByteArrayResource(new String("Access forbidden.").getBytes()),
                                                HttpStatus.FORBIDDEN);

        HttpHeaders headers = new HttpHeaders();

        Path path = null;
        File f = null;
        String rid = internalDecode(resourceId);
        try {
            path = resourceRepo.getResource(scriptId, rid);
            log.trace("Serving resource '{}' for process {}: {}", resourceId, scriptId, path);

            f = path.toFile();

            if ( !f.exists()) {
                log.debug("Resource file '{}' does not exist for process '{}'", resourceId, scriptId);
                throw new ExceptionReport("Requested resourcce file does not exist: " + resourceId + " for script "
                        + resourceId, ExceptionReport.NO_APPLICABLE_CODE);
            }

            if (f.isDirectory()) {
                f = IOUtils.zipDirectory(rid, f);
                log.debug("Zipped directory {} to file {}", path, f);
                headers.setContentType(ZIP_TYPE);
            }
            else {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }
        }
        catch (ExceptionReport e) {
            log.debug("Could not get resource '{}' for process '{}'", resourceId, scriptId);
            throw e;
        }
        catch (IOException e) {
            log.debug("Could not create zip file resource '{}' for process '{}'", resourceId, scriptId);
            throw new ExceptionReport("Error creating zip file resource " + resourceId,
                                      ExceptionReport.NO_APPLICABLE_CODE,
                                      e);
        }

        FileSystemResource fsr = new FileSystemResource(f);
        String ext = Files.getFileExtension(fsr.getFilename().toString());
        if (resourceId.endsWith(ext))
            ext = "";

        headers.setContentDispositionFormData("attachment", resourceId + "." + ext);

        ResponseEntity<Resource> entity = new ResponseEntity<Resource>(fsr, headers, HttpStatus.OK);
        return entity;
    }

    @RequestMapping(value = SCRIPT_PATH_PARAMS, method = RequestMethod.GET, produces = {MediaType.TEXT_PLAIN_VALUE,
                                                                                        RConstants.R_SCRIPT_TYPE_VALUE})
    public ResponseEntity<Resource> getScript(@PathVariable(REQUEST_PARAM_SCRIPTID) String id) throws ExceptionReport,
            IOException {
        if ( !config.isScriptDownloadEnabled())
            return new ResponseEntity<Resource>(new ByteArrayResource(new String("Access to resources forbidden for all processes.").getBytes()),
                                                HttpStatus.FORBIDDEN);

        HttpHeaders headers = new HttpHeaders();

        File f = null;
        try {
            f = scriptRepo.getScriptFileForWKN(id);
            log.trace("Serving script file for id {}: {}", id, f);
        }
        catch (ExceptionReport e) {
            log.debug("Could not get script file for id '{}'", id);
            throw e;
        }

        FileSystemResource fsr = new FileSystemResource(f);

        // headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentType(RConstants.R_SCRIPT_TYPE);
        headers.setContentDispositionFormData("attachment", id + "." + RConstants.R_FILE_EXTENSION);
        // headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        ResponseEntity<Resource> entity = new ResponseEntity<Resource>(fsr, headers, HttpStatus.OK);
        return entity;
    }

    @RequestMapping(value = IMPORT_PATH_PARAMS, method = RequestMethod.GET, produces = {RConstants.R_SCRIPT_TYPE_VALUE})
    public ResponseEntity<Resource> getImport(@PathVariable(REQUEST_PARAM_SCRIPTID) String scriptId,
                                              @PathVariable(REQUEST_PARAM_RESOURCEID) String importId) throws ExceptionReport {
        if ( !config.isImportDownloadEnabled())
            return new ResponseEntity<Resource>(new ByteArrayResource(new String("Access forbidden.").getBytes()),
                                                HttpStatus.FORBIDDEN);

        HttpHeaders headers = new HttpHeaders();

        File f = null;
        try {
            f = scriptRepo.getImportedFileForWKN(scriptId, importId);
            log.trace("Serving imported script file '{}' for id '{}': {}", importId, scriptId, f);
        }
        catch (ExceptionReport e) {
            log.debug("Could not get  imported script file '{}' for id '{}'", importId, scriptId);
            throw e;
        }

        FileSystemResource fsr = new FileSystemResource(f);

        headers.setContentType(RConstants.R_SCRIPT_TYPE);
        headers.setContentDispositionFormData("attachment", importId);

        ResponseEntity<Resource> entity = new ResponseEntity<Resource>(fsr, headers, HttpStatus.OK);
        return entity;
    }

    @RequestMapping(value = SESSION_INFO_PATH, produces = MediaType.TEXT_PLAIN_VALUE + CHARSET_STRING)
    public ResponseEntity<String> sessionInfo() {
        if ( !config.isSessionInfoLinkEnabled())
            return new ResponseEntity<String>("Access to sessionInfo() output forbidden.", HttpStatus.FORBIDDEN);

        FilteredRConnection rCon = null;
        try {
            rCon = config.openRConnection();

            RSessionManager session = new RSessionManager(rCon, config);
            String sessionInfo = session.getSessionInfo();

            ResponseEntity<String> entity = new ResponseEntity<String>(sessionInfo, HttpStatus.OK);
            return entity;
        }
        catch (RserveException | REXPMismatchException e) {
            log.error("Could not open connection to retrieve sesion information.", e);
            ResponseEntity<String> entity = new ResponseEntity<String>("R exception: " + e.getMessage(),
                                                                       HttpStatus.INTERNAL_SERVER_ERROR);
            return entity;
        }
        finally {
            if (rCon != null)
                rCon.close();
        }
    }

}
