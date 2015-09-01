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
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.r.util.RConnector;
import org.n52.wps.server.r.util.RFileExtensionFilter;
import org.n52.wps.server.r.util.RStarter;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

@Component
@Scope(value = "singleton")
public class R_Config implements ServletContextAware {

    private static Logger LOGGER = LoggerFactory.getLogger(R_Config.class);

    public static final String SCRIPT_FILE_EXTENSION = "R";

    public static final String SCRIPT_FILE_SUFFIX = "." + SCRIPT_FILE_EXTENSION;

    private String wknPrefix = "org.n52.wps.server.r.";

    private static final String DEFAULT_RSERVE_HOST = "localhost";

    private static final int DEFAULT_RSERVE_PORT = 6311;

    private static final boolean DEFAULT_ENABLEBATCHSTART = false;

    private static final String DIR_DELIMITER = ";";

    private ArrayList<File> utilsFiles = null;

    private HashMap<RWPSConfigVariables, String> configVariables = new HashMap<RWPSConfigVariables, String>();

    private RConnector connector;

    private RStarter starter;

    private Path baseDir = null;

    private static FileFilter rFileFilter = new RFileExtensionFilter();

    private Map<Path, File> utilFileCache = new HashMap<Path, File>();

    protected R_Config() {
        this.starter = new RStarter();
        this.connector = new RConnector(starter);

        LOGGER.info("NEW {}", this);
    }

    public void setConfigVariable(RWPSConfigVariables key, String value) {
        this.configVariables.put(key, value);
    }

    public String getConfigVariable(RWPSConfigVariables key) {
        return this.configVariables.get(key);
    }

    public String getConfigVariableFullPath(RWPSConfigVariables key) throws ExceptionReport {
        String path = getConfigVariable(key);
        if (path == null)
            throw new ExceptionReport("Config variable '" + key.toString() + "' is not set!",
                                      "Inconsistent property",
                                      key.name());

        File file = new File(path);
        if ( !file.isAbsolute()) {
            file = getBaseDir().resolve(Paths.get(path)).toFile();
        }
        if ( !file.exists())
            throw new ExceptionReport("Invalid config property of name \"" + key + "\" and value \"" + path
                    + "\". It denotes a non-existent path.", "Inconsistent property");

        return file.getAbsolutePath();
    }

    public Collection<Path> getResourceDirectory() {
        String resourceDirConfigParam = getConfigVariable(RWPSConfigVariables.RESOURCE_DIR);
        Collection<Path> resourceDirectories = new ArrayList<Path>();

        String[] dirs = resourceDirConfigParam.split(DIR_DELIMITER);
        for (String s : dirs) {
            Path dir = Paths.get(s);
            if ( !dir.isAbsolute())
                dir = getBaseDir().resolve(s);

            resourceDirectories.add(dir);
            LOGGER.debug("Found resource directory configured in config variable: {}", dir);
        }

        return resourceDirectories;
    }

    public Collection<File> getScriptFiles() {
        String scriptDirConfigParam = getConfigVariable(RWPSConfigVariables.SCRIPT_DIR);
        Collection<File> scriptDirectories = new ArrayList<File>();

        String[] scriptDirs = scriptDirConfigParam.split(DIR_DELIMITER);
        for (String s : scriptDirs) {
            File dir = new File(s);
            if ( !dir.isAbsolute())
                dir = new File(getBaseDir().toFile(), s);

            scriptDirectories.add(dir);
            LOGGER.debug("Found script directory: {}", dir);
        }

        return scriptDirectories;
    }

    public Collection<File> getScriptDir() {
        String scriptDirConfigParam = getConfigVariable(RWPSConfigVariables.SCRIPT_DIR);
        Collection<File> scriptDirectories = new ArrayList<File>();

        String[] scriptDirs = scriptDirConfigParam.split(DIR_DELIMITER);
        for (String s : scriptDirs) {
            File dir = new File(s);
            scriptDirectories.add(dir);
        }

        return scriptDirectories;
    }

    // TODO the config should not open connections
    public FilteredRConnection openRConnection() throws RserveException {
        return this.connector.getNewConnection(this.getEnableBatchStart(),
                                               this.getRServeHost(),
                                               this.getRServePort(),
                                               this.getRServeUser(),
                                               this.getRServePassword());
    }

    public String getRServePassword() {
        return getConfigVariable(RWPSConfigVariables.RSERVE_PASSWORD);
    }

    public String getRServeUser() {
        return getConfigVariable(RWPSConfigVariables.RSERVE_USER);
    }

    public int getRServePort() {
        int port_number = DEFAULT_RSERVE_PORT;

        String port = getConfigVariable(RWPSConfigVariables.RSERVE_PORT);
        // try to retrieve config variable
        if (port != null && !port.equals("")) {
            try {
                port_number = Integer.parseInt(port);
            }
            catch (NumberFormatException e) {
                LOGGER.warn("Config variable " + RWPSConfigVariables.RSERVE_PORT
                        + " does not contain a parseble integer. Using default port " + port_number);
            }
        }
        return port_number;
    }

    public String getRServeHost() {
        String host = getConfigVariable(RWPSConfigVariables.RSERVE_HOST);
        if (host == null || host.equals("")) {
            host = DEFAULT_RSERVE_HOST;
        }
        return host;
    }

    public boolean getEnableBatchStart() {
        boolean isBatch = DEFAULT_ENABLEBATCHSTART;
        // try to retrieve config variable
        String batch_c = getConfigVariable(RWPSConfigVariables.ENABLE_BATCH_START);
        if (batch_c != null && !batch_c.equals("")) {
            try {
                isBatch = Boolean.parseBoolean(batch_c);
            }
            catch (NumberFormatException e) {
                LOGGER.warn("Config variable " + RWPSConfigVariables.RSERVE_PORT
                        + " does not contain a parseble boolean. Using default port " + isBatch);
            }
        }

        return isBatch;
    }

    public URL getProcessDescriptionURL(String processWKN) {
        String s = WPSConfig.getInstance().getServiceBaseUrl()
                + "/WebProcessingService?Request=DescribeProcess&identifier=" + processWKN;
        try {
            return new URL(s);
        }
        catch (MalformedURLException e) {
            LOGGER.error("Could not create URL for process {}", processWKN, e);
            return null;
        }
    }

    public boolean getCacheProcesses() {
        String s = getConfigVariable(RWPSConfigVariables.R_CACHE_PROCESSES);
        return Boolean.valueOf(s);
    }

    public Collection<File> getUtilsFiles() {
        if (this.utilsFiles == null) {
            this.utilsFiles = new ArrayList<File>();
            Path basedir = getBaseDir();
            String configVariable = getConfigVariable(RWPSConfigVariables.R_UTILS_DIR);
            if (configVariable != null) {
                String[] configVariableDirs = configVariable.split(DIR_DELIMITER);
                for (String s : configVariableDirs) {
                    Path dir = Paths.get(s);
                    Collection<File> files = resolveFilesFromResourcesOrFromWebapp(dir, basedir);
                    this.utilsFiles.addAll(files);
                    LOGGER.debug("Added {} files to the list of util files: {}",
                                 files.size(),
                                 Arrays.toString(files.toArray()));
                }
            }
            else
                LOGGER.error("Could not load utils directory variable from config, not loading any utils files!");
        }

        return utilsFiles;
    }

    /**
     * given a relative path, this method tries to locate the directory first within the webapp folder, then
     * within the resources directory.
     * 
     * @param p
     * @param baseDir
     *        the full path to the webapp directory
     * @return
     */
    private Collection<File> resolveFilesFromResourcesOrFromWebapp(Path p, Path baseDir) {
        LOGGER.debug("Loading util files from {}", p);

        if ( !baseDir.isAbsolute())
            throw new RuntimeException(String.format("The given basedir (%s) is not absolute, cannot resolve path %s.",
                                                     baseDir,
                                                     p));

        ArrayList<File> foundFiles = new ArrayList<File>();
        File f = null;

        // try resource first
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(p.toString());) {
            if (input != null) {
                if (this.utilFileCache.containsKey(p) && this.utilFileCache.get(p).exists()) {
                    f = this.utilFileCache.get(p);
                }
                else {
                    try {
                        f = File.createTempFile("wps4rutil_", "_" + p.getFileName().toString());
                        Files.copy(input, Paths.get(f.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
                        if (f.exists() && rFileFilter.accept(f)) {
                            foundFiles.add(f);
                            this.utilFileCache.put(p, f);
                        }
                    }
                    catch (IOException e) {
                        LOGGER.warn("Could not add util file from classpath.", e);
                    }
                }
            }
        }
        catch (IOException e) {
            LOGGER.warn("Could not add util file from classpath.", e);
        }

        if (f == null) {
            // try resolving with basedir
            Path resolved = baseDir.resolve(p);
            if (Files.exists(resolved)) {
                f = resolved.toFile();
                File[] files = f.listFiles(rFileFilter);
                foundFiles.addAll(Arrays.asList(files));
            }
            else
                LOGGER.warn("Configured utils directory does not exist: {}", p);
        }

        LOGGER.debug("Found {} util files in directory configured as '{}' at {}", foundFiles.size(), p, f);

        return foundFiles;
    }

    public Path getBaseDir() {
        return this.baseDir;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.baseDir = Paths.get(servletContext.getRealPath(""));
    }

    public String getPublicScriptId(String s) {
        return getWknPrefix() + s;
    }

    public String getWknPrefix() {
        return wknPrefix;
    }

    public void setWknPrefix(String wknPrefix) {
        this.wknPrefix = wknPrefix;
    }

    public boolean isResourceDownloadEnabled() {
        return Boolean.parseBoolean(getConfigVariable(RWPSConfigVariables.R_ENABLE_RESOURCE_DOWNLOAD));
    }

    public boolean isImportDownloadEnabled() {
        return Boolean.parseBoolean(getConfigVariable(RWPSConfigVariables.R_ENABLE_IMPORT_DOWNLOAD));
    }

    public boolean isScriptDownloadEnabled() {
        return Boolean.parseBoolean(getConfigVariable(RWPSConfigVariables.R_ENABLE_SCRIPT_DOWNLOAD));
    }

    public boolean isSessionInfoLinkEnabled() {
        return Boolean.parseBoolean(getConfigVariable(RWPSConfigVariables.R_ENABLE_SESSION_INFO_DOWNLOAD));
    }
}
