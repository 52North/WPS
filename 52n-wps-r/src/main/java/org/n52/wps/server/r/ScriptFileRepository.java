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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.r.info.RProcessInfo;
import org.n52.wps.server.r.metadata.RAnnotationParser;
import org.n52.wps.server.r.syntax.RAnnotation;
import org.n52.wps.server.r.syntax.RAnnotationException;
import org.n52.wps.server.r.syntax.RAnnotationType;
import org.n52.wps.server.r.syntax.RAttribute;
import org.n52.wps.server.r.util.RFileExtensionFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Management class to store and retrieve script files and their corresponding well known names.
 * 
 * @author Daniel Nüst
 *
 */
@Repository
public class ScriptFileRepository {

    private static final String DEFAULT_VERSION = "1";

    private static Logger LOGGER = LoggerFactory.getLogger(ScriptFileRepository.class);

    /** Maps current R-script files to identifiers **/
    private Map<File, String> fileToWknMap = new HashMap<File, String>();

    /** Maps each identifier to (multiple versioned) R script file **/
    private Map<String, Map<Integer, File>> wknToFileMap = new HashMap<>();

    @Autowired
    private RAnnotationParser annotationParser;

    @Autowired
    R_Config config;

    public ScriptFileRepository() {
        LOGGER.info("NEW {}", this);
    }

    public File getScriptFileForWKN(String wkn) throws ExceptionReport {
        File out = wknToFileMap.get(wkn).values().iterator().next();

        if (out != null && out.exists() && out.isFile() && out.canRead()) {
            return out;
        }

        String fname = out == null ? "(unknown)" : out.getName();
        StringBuilder message = new StringBuilder();
        message.append("Error in Process: '").append(wkn).append("' with file '").append(fname).append("':");
        if (out == null) {
            message.append("File is null. ");
        }
        if ( !out.exists()) {
            message.append("File does not exist. ");
        }
        if ( !out.isFile()) {
            message.append("Is not a file. ");
        }
        if ( !out.canRead()) {
            message.append("Cannot read file.");
        }

        throw new ExceptionReport(message.toString(), ExceptionReport.NO_APPLICABLE_CODE);
    }

    public Map<Integer, File> getScriptFileVersionsForWKN(String id) {
        return wknToFileMap.get(id);
    }

    public String getWKNForScriptFile(File file) throws RAnnotationException, IOException, ExceptionReport {
        if ( !file.exists())
            throw new FileNotFoundException("File not found: " + file.getName());

        return fileToWknMap.get(file);
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

    public boolean isScriptAvailable(RProcessInfo processInfo) {
        if (processInfo != null)
            return isScriptAvailable(processInfo.getWkn());
        return false;
    }

    public boolean isScriptValid(String wkn) {
        try (FileInputStream fis = new FileInputStream(getScriptFileForWKN(wkn));) {

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
    }

    public boolean registerScript(File file) throws RAnnotationException, ExceptionReport {
        boolean registered = false;

        try (FileInputStream fis = new FileInputStream(file);) {

            if (fileToWknMap.containsKey(file.getAbsoluteFile()))
                LOGGER.debug("File already registered, not doing it again: {}", file);
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
                        String versionString = descriptionAnnotation.getStringValue(RAttribute.VERSION);
                        if (versionString == null)
                            versionString = DEFAULT_VERSION;

                        String wkn = config.getPublicScriptId(process_id);

                        Integer version = null;
                        try {
                            version = Integer.parseInt(versionString);
                        }
                        catch (NumberFormatException e) {
                            String message = String.format("Version '%s' cannot be parsed to integer! Process: '%s', file: '%s'",
                                                           versionString,
                                                           wkn,
                                                           file.getAbsoluteFile());
                            LOGGER.error(message);
                            throw new ExceptionReport(message, ExceptionReport.NO_APPLICABLE_CODE, e);
                        }
                        LOGGER.debug("Adding script based on description annotation: id = {}, version = {}, wkn = {}",
                                     process_id,
                                     version,
                                     wkn);

                        boolean identifierConflict = false;
                        String identifierMessage = null;

                        if (fileToWknMap.containsValue(wkn)) {
                            File conflictFile = getScriptFileForWKN(wkn);
                            if ( !conflictFile.exists()) {
                                LOGGER.info("Mapping for process '{}' with file '{}' replaced by file '{}'",
                                            wkn,
                                            conflictFile.getName(),
                                            file.getName());
                            }
                            else if ( !file.equals(conflictFile)) {
                                identifierMessage = String.format("Conflicting identifier '%s' detected for R scripts '%s' and '%s'",
                                                                  wkn,
                                                                  file.getAbsoluteFile(),
                                                                  conflictFile.getAbsoluteFile());
                                LOGGER.warn(identifierMessage);
                                identifierConflict = true; // could still be different versions!
                            }
                        }

                        if (identifierConflict && wknToFileMap.containsKey(wkn)
                                && wknToFileMap.get(wkn).containsKey(version)) {
                            // same version with the same identifier, throw identifier message
                            throw new ExceptionReport(identifierMessage, ExceptionReport.NO_APPLICABLE_CODE);
                        }

                        if ( !wknToFileMap.containsKey(wkn)) {
                            wknToFileMap.put(wkn, new TreeMap<Integer, File>(Collections.reverseOrder()));
                        }

                        // check conflicting versions
                        Map<Integer, File> files = wknToFileMap.get(wkn);
                        if (files.containsKey(version)) {
                            String message = String.format("Conflicting version '%s' detected for algorithm '%s':\nFiles: %s \nTo be added: '%s'",
                                                           version,
                                                           wkn,
                                                           Arrays.deepToString(files.entrySet().toArray()),
                                                           file);
                            LOGGER.error(message);
                            throw new ExceptionReport(message, ExceptionReport.NO_APPLICABLE_CODE);
                        }

                        // actually "register"
                        fileToWknMap.put(file.getAbsoluteFile(), wkn);
                        files.put(version, file.getAbsoluteFile());
                        registered = true;
                    }
                }
            }
        }
        catch (IOException e) {
            LOGGER.error("Could not create input stream for file '{}'", file, e);
        }

        return registered;
    }

    /**
     * For testing purposes only! Register all scripts in the given directory, returns true only if all
     * scripts could be registered and does not provide information about which script failed.
     * 
     * @throws ExceptionReport
     */
    public boolean registerScripts(File directory) {
        if ( !directory.isDirectory()) {
            LOGGER.error("Provided file is not a directory, cannot load scripts: {}", directory);
            return false;
        }

        File[] scripts = directory.listFiles(new RFileExtensionFilter());
        LOGGER.debug("Loading {} script files from {}: {}", scripts.length, directory, Arrays.toString(scripts));

        boolean allRegistered = true;
        for (File file : scripts) {
            try {
                boolean registered = registerScript(file);

                if ( !registered) {
                    LOGGER.debug("Could not register script based on file {}", file);
                    allRegistered = false;
                }
                LOGGER.debug("Registered script in file {} into {}", file, this);

            }
            catch (RAnnotationException | ExceptionReport e) {
                LOGGER.error("Could not register script based on file {}", file, e);
                allRegistered = false;
            }
        }

        return allRegistered;
    }

    public void reset() {
        LOGGER.info("Resetting {}", this);

        this.wknToFileMap.clear();
        this.fileToWknMap.clear();
    }

    public File getImportedFileForWKN(String scriptId, String importId) throws ExceptionReport {
        File basefile = getScriptFileForWKN(scriptId);
        Path basepath = basefile.toPath();
        Path importedFile = basepath.resolveSibling(importId);

        if (importedFile.toFile().exists()) {
            LOGGER.debug("Resolved imported '{}' for script with id '{}': {}", importId, scriptId, importedFile);
            return importedFile.toFile();
        }

        LOGGER.warn("Could not find import {} for {} at {}", importId, scriptId, importedFile);
        throw new ExceptionReport("Imported script '" + importId + "' not found for script '" + scriptId + "'.",
                                  ExceptionReport.NO_APPLICABLE_CODE);
    }
}
