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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.n52.wps.ServerDocument.Server;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.WebProcessingService;
import org.n52.wps.server.r.metadata.RAnnotationParser;
import org.n52.wps.server.r.syntax.RAnnotation;
import org.n52.wps.server.r.syntax.RAnnotationException;
import org.n52.wps.server.r.syntax.RAnnotationType;
import org.n52.wps.server.r.syntax.RAttribute;
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

    public static final String WKN_PREFIX = "org.n52.wps.server.r.";

    private static final String DEFAULT_RSERVE_HOST = "localhost";

    private static final int DEFAULT_RSERVE_PORT = 6311;

    private static final boolean DEFAULT_ENABLEBATCHSTART = false;

    private static final String DIR_DELIMITER = ";";

    private ArrayList<File> utilsFiles = null;

    private HashMap<RWPSConfigVariables, String> configVariables = new HashMap<RWPSConfigVariables, String>();

    private static R_Config instance = null;

    private RConnector connector;

    private RAnnotationParser annotationParser;

    /** Maps current R-script files to identifiers **/
    private HashMap<File, String> fileToWknMap = new HashMap<File, String>();

    /** Maps each identifier to an R script file **/
    private HashMap<String, File> wknToFileMap = new HashMap<String, File>();

    /** caches conflicts for the wkn-Rscript mapping until resetWknFileMapping is invoked **/
    private HashMap<String, ExceptionReport> wknConflicts = new HashMap<String, ExceptionReport>();

    private RStarter starter;

    private ServletContext servletContext = null;

    private Path baseDir = null;

    private static FileFilter rFileFilter = new RFileExtensionFilter();

    private Map<Path, File> utilFileCache = new HashMap<Path, File>();

    private R_Config() {
        this.starter = new RStarter();
        this.connector = new RConnector(starter);
        this.annotationParser = new RAnnotationParser(this);

        // TODO use injection wherever R_Config is required
        instance = this;

        LOGGER.info("NEW {}", this);
    }

    public static R_Config getInstance() {
        return instance;
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
            throw new ExceptionReport("Config variable is not set!", "Inconsistent property");
        File testFile = new File(path);
        if ( !testFile.isAbsolute()) {
            testFile = new File(WebProcessingService.getApplicationBaseDir(), path);
        }
        if ( !testFile.exists())
            throw new ExceptionReport("Invalid config property of name \"" + key + "\" and value \"" + path
                    + "\". It denotes a non-existent path.", "Inconsistent property");

        return testFile.getAbsolutePath();
    }

    public URL getSessionInfoURL() throws MalformedURLException {
        // FIXME implement service endpoint to retrieve r session information
        return new URL(getUrlPathUpToWebapp() + "/not_supported");
    }

    // FIXME this should use generic WPS methods to get the URL
    public String getResourceDirURL() {
        String webapp = getUrlPathUpToWebapp();
        String resourceDirectory = getResourceDirectory();

        // important: this url should be appendable with a resource name, i.e. either end in "/" or "id="
        if (webapp != null & resourceDirectory != null)
            return webapp + "/" + resourceDirectory.replace("\\", "/") + "/";

        LOGGER.warn("Cannot create resource dir URL");
        return null;
    }

    public String getResourceDirectory() {
        return getConfigVariable(RWPSConfigVariables.RESOURCE_DIR);
    }

    public URL getScriptURL(String wkn) throws MalformedURLException, ExceptionReport {
        String fname = getScriptFileForWKN(wkn).getName();

        if (fname == null)
            return null;

        Collection<File> scriptDirFullPath = getScriptDir();
        // find in which script dir the file is

        for (File dir : scriptDirFullPath) {
            File f = new File(dir, fname);
            if (f.isAbsolute()) {
                // FIXME can only access scripts that are in the webapp folder
                LOGGER.debug("Cannot create URL for script file {} at location {} of process {}", fname, dir, wkn);
                return null;
            }
        }

        return null;
    }

    private String getUrlPathUpToWebapp() {
        Server server = WPSConfig.getInstance().getWPSConfig().getServer();
        String host = server.getHostname();
        String port = server.getHostport();
        String webapppath = server.getWebappPath();

        return "http://" + host + ":" + port + "/" + webapppath;
    }

    public URL getOutputFileURL(String currentWorkdir, String filename) throws IOException {
        // check if file exists
        String path = currentWorkdir + "/" + filename;
        File out = new File(path);
        if ( ! (out.isFile() && out.canRead()))
            throw new IOException("Error in creating URL: " + currentWorkdir + " / " + path + " not found or broken.");

        // create URL
        path = path.substring(WebProcessingService.getApplicationBaseDir().length() + 1, path.length());
        String urlString = getUrlPathUpToWebapp() + "/" + path;

        return new URL(urlString);
    }

    protected boolean registerScript(File file) throws RAnnotationException, ExceptionReport {
        boolean registered = false;

        try (FileInputStream fis = new FileInputStream(file);) {

            if (fileToWknMap.containsKey(file.getAbsoluteFile()))
                LOGGER.debug("File already registered, not doint it again: {}", file);
            else {

                LOGGER.info("Registering script file {} from input {}", file, fis);

                List<RAnnotation> annotations = annotationParser.parseAnnotationsfromScript(fis);
                if (annotations.size() < 1) {
                    LOGGER.warn("Could not parse any annotations from file '{}'. Did not load the script.", file);
                    registered = false;
                }
                else {
                    RAnnotation descriptionAnnotation = RAnnotation.filterFirstMatchingAnnotation(annotations,
                                                                                                  RAnnotationType.DESCRIPTION);
                    if (descriptionAnnotation == null) {
                        LOGGER.error("No description annotation for script '{}' - cannot be registered!", file);
                        registered = false;
                    }
                    else {
                        String process_id = descriptionAnnotation.getStringValue(RAttribute.IDENTIFIER);
                        String wkn = WKN_PREFIX + process_id;

                        if (fileToWknMap.containsValue(wkn)) {
                            File conflictFile = getScriptFileForWKN(wkn);
                            if ( !conflictFile.exists()) {
                                LOGGER.info("Cached mapping for process '{}' with file '{}' replaced by file '{}'",
                                            wkn,
                                            conflictFile.getName(),
                                            file.getName());
                            }
                            else if ( !file.equals(conflictFile)) {
                                String message = String.format("Conflicting identifier '{}' detected for R scripts '{}' and '{}'",
                                                               wkn,
                                                               file.getName(),
                                                               conflictFile.getName());
                                ExceptionReport e = new ExceptionReport(message, ExceptionReport.NO_APPLICABLE_CODE);
                                LOGGER.error(message);
                                wknConflicts.put(wkn, e);
                                throw e;
                            }
                        }

                        fileToWknMap.put(file.getAbsoluteFile(), wkn);
                        wknToFileMap.put(wkn, file.getAbsoluteFile());

                        registered = true;
                    }
                }
            }
        }
        catch (IOException e) {
            LOGGER.error("Could not create input stream for file {}", file);
        }

        return registered;
    }

    public String getWKNForScriptFile(File file) throws RAnnotationException, IOException, ExceptionReport {
        if ( !file.exists())
            throw new FileNotFoundException("File not found: " + file.getName());

        return fileToWknMap.get(file);
    }

    public File getScriptFileForWKN(String wkn) throws ExceptionReport {
        // check for existing identifier conflicts
        if (wknConflicts.containsKey(wkn))
            throw wknConflicts.get(wkn);

        File out = wknToFileMap.get(wkn);
        if (out != null && out.exists() && out.isFile() && out.canRead()) {
            return out;
        }
        String fname = out == null ? "(unknown)" : out.getName();
        throw new ExceptionReport("Error in Process: " + wkn + ", File " + fname + " not found or broken.",
                                  ExceptionReport.NO_APPLICABLE_CODE);
    }

    public void resetWknFileMapping() {
        LOGGER.info("Resetting wkn mappings.");

        this.wknToFileMap.clear();
        this.fileToWknMap.clear();
        this.wknConflicts.clear();
    }

    public Collection<File> getScriptDirFullPath() {
        String scriptDirConfigParam = getConfigVariable(RWPSConfigVariables.SCRIPT_DIR);
        Collection<File> scriptDirectories = new ArrayList<File>();

        String[] scriptDirs = scriptDirConfigParam.split(DIR_DELIMITER);
        for (String s : scriptDirs) {
            File dir = new File(s);
            if ( !dir.isAbsolute())
                dir = new File(WebProcessingService.getApplicationBaseDir(), s);

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

    public boolean isScriptAvailable(String identifier) {
        try {
            File f = getScriptFileForWKN(identifier);
            boolean out = f.exists();
            return out;
        }
        catch (RuntimeException | ExceptionReport e) {
            LOGGER.error("Script file unavailable for process id " + identifier, e);
            return false;
        }
    }

    public boolean isScriptValid(String wkn) {
        FileInputStream fis = null;

        try {
            File file = getScriptFileForWKN(wkn);
            // RAnnotationParser parser = new RAnnotationParser();
            fis = new FileInputStream(file);
            boolean valid = annotationParser.validateScript(fis, wkn);

            return valid;
        }
        catch (IOException e) {
            LOGGER.error("Script file unavailable for process " + wkn + ".", e);
            return false;
        }
        catch (RuntimeException | ExceptionReport | RAnnotationException e) {
            LOGGER.error("Validation of process " + wkn + " failed.", e);
            return false;
        }
        finally {
            if (fis != null)
                try {
                    fis.close();
                }
                catch (IOException e) {
                    LOGGER.error("Could not flose file input.", e);
                }
        }
    }

    public void killRserveOnWindows() {
        try {
            if (Runtime.getRuntime().exec("taskkill /IM RServe.exe /T /F").waitFor() == 0)
                return;
        }
        catch (InterruptedException | IOException e) {
            LOGGER.warn("Error trying to stop Rserve on windows.", e);
        }
    }

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
        String s = getUrlPathUpToWebapp() + "/WebProcessingService?Request=DescribeProcess&identifier=" + processWKN;
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
        }

        LOGGER.debug("Found {} util files in directory configured as '{}' at {}", foundFiles.size(), p, f);

        return foundFiles;
    }

    public Path getBaseDir() {
        if (this.baseDir == null && this.servletContext != null) {
            this.baseDir = Paths.get(this.servletContext.getRealPath(""));
        }

        return this.baseDir;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}
