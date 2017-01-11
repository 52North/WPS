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
package org.n52.wps.server.r;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(R_Config.class);

    public static final String SCRIPT_FILE_EXTENSION = "R";

    public static final String SCRIPT_FILE_SUFFIX = "." + SCRIPT_FILE_EXTENSION;

    private String wknPrefix = "org.n52.wps.server.r.";

    public static final String LOCK_SUFFIX = "lock";

    private static final String DIR_DELIMITER = ";";

    private RConfigurationModule configModule;

    private ArrayList<File> utilsFiles = null;

    private final RConnector connector;

    private final RStarter starter;

    private Path baseDir = null;

    private static final FileFilter rFileFilter = new RFileExtensionFilter();

    private final Map<Path, File> utilFileCache = new HashMap<>();

    protected R_Config() {
        this.starter = new RStarter();
        this.connector = new RConnector(starter);
        this.configModule = new RConfigurationModule();

        LOGGER.info("NEW {}", this);
    }

    public void setConfigModule(RConfigurationModule configModule) {
        this.configModule = configModule;
    }

    public RConfigurationModule getConfigModule() {
        return configModule;
    }

   public String resolveFullPath(String pathToResolve) throws ExceptionReport {
        File file = new File(pathToResolve);
        if ( !file.isAbsolute()) {
            file = getBaseDir().resolve(Paths.get(pathToResolve)).toFile();
        }

        if ( !file.exists()) {
            LOGGER.error("'" + pathToResolve + "' denotes a non-existent path.");
            throw new ExceptionReport("Configuration Error!", "Inconsistent property");
        }

        return file.getAbsolutePath();
    }

    public Collection<Path> getResourceDirectories() {
        String resourceDirConfigParam = configModule.getResourceDirectory();
        Collection<Path> resourceDirectories = new ArrayList<>();

        String[] dirs = resourceDirConfigParam.split(DIR_DELIMITER);
        for (String s : dirs) {
            Path dir = Paths.get(s);
            if ( !dir.isAbsolute()) {
                dir = getBaseDir().resolve(s);
            }

            resourceDirectories.add(dir);
            LOGGER.debug("Found resource directory configured in config variable: {}", dir);
        }

        return resourceDirectories;
    }

    public Collection<File> getScriptFiles() {
        String scriptDirConfigParam = getConfigModule().getScriptDirectory();
        Collection<File> rScripts = new ArrayList<>();

        String[] scriptDirs = scriptDirConfigParam.split(DIR_DELIMITER);
        for (String s : scriptDirs) {
            File dir = new File(s);
            if ( !dir.isAbsolute()) {
                dir = new File(getBaseDir().toFile(), s);
            }
            File[] files = dir.listFiles(new RFileExtensionFilter());
            if (files == null) {
                LOGGER.info("Configured script dir does not exist: {}", dir);
                continue;
            }
            for (File rScript : files) {
                rScripts.add(rScript);
                LOGGER.debug("Registered script: {}", rScript.getAbsoluteFile());
            }
        }
        return rScripts;
    }

    public Collection<File> getScriptDirectories() {
        String scriptDirConfigParam = configModule.getScriptDirectory();
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

    private String getRServePassword() {
        return configModule.getrServePassword();
    }

    private String getRServeUser() {
        return configModule.getRServeUser();
    }

    private int getRServePort() {
        String port = configModule.getRServePort();
        return Integer.parseInt(port);
    }

    public String getRServeHost() {
        return configModule.getRServeHost();
    }

    public boolean getEnableBatchStart() {
        return configModule.isEnableBatchStart();
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

    public boolean isCacheProcesses() {
        return configModule.isCacheProcesses();
    }

    public Collection<File> getUtilsFiles() {
        if (this.utilsFiles == null) {
            this.utilsFiles = new ArrayList<File>();
            Path basedir = getBaseDir();
            String configVariable = configModule.getRServeUtilsScriptsDirectory();
            if (configVariable != null) {
                String[] configVariableDirs = configVariable.split(DIR_DELIMITER);
                for (String s : configVariableDirs) {
                    Collection<File> files = resolveFilesFromResourcesOrFromWebapp(s, basedir);
                    this.utilsFiles.addAll(files);
                    LOGGER.debug("Added {} files to the list of util files: {}",
                                 files.size(),
                                 Arrays.toString(files.toArray()));
                }
            }
            else {
                LOGGER.error("Could not load utils directory variable from config, not loading any utils files!");
            }
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
    private Collection<File> resolveFilesFromResourcesOrFromWebapp(String s, Path baseDir) {
        LOGGER.debug("Loading util files from {}", s);

        Path p = Paths.get(s);
        if ( !baseDir.isAbsolute()) {
            throw new RuntimeException(String.format("The given basedir (%s) is not absolute, cannot resolve path %s.",
                                                     baseDir,
                                                     p));
        }

        ArrayList<File> foundFiles = new ArrayList<>();
        File f = null;

        // try resource first
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(s);) {
        //URL url = Resources.getResource(p.toString());
        //try (InputStream input = Resources.asByteSource(url).openStream();) {
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
            else {
                LOGGER.warn("Configured utils directory does not exist: {}", p);
            }
        }

        LOGGER.debug("Found {} util files in directory configured as '{}' at {}", foundFiles.size(), p, f);

        return foundFiles;
    }

    public Path getBaseDir() {
//        return baseDir;
        try {
            return this.baseDir == null
                    ? Paths.get(getClass().getResource("/").toURI())
                    : baseDir;
        } catch (URISyntaxException e) {
            LOGGER.error("Could not determine fallback base dir!", e);
            return Paths.get(""); // empty path
        }
    }

    protected void setBaseDir(Path baseDir) {
        this.baseDir = baseDir;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        setBaseDir(Paths.get(servletContext.getRealPath("")));
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
        return configModule.isResourceDownloadEnabled();
    }

    public boolean isImportDownloadEnabled() {
        return configModule.isImportDownloadEnabled();
    }

    public boolean isScriptDownloadEnabled() {
        return configModule.isScriptDownloadEnabled();
    }

    public boolean isSessionInfoLinkEnabled() {
        return configModule.isSessionInfoDownloadEnabled();
    }
}
