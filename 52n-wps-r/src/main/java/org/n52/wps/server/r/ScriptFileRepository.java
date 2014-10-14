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
import java.util.HashMap;
import java.util.List;

import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.r.info.RProcessInfo;
import org.n52.wps.server.r.metadata.RAnnotationParser;
import org.n52.wps.server.r.syntax.RAnnotation;
import org.n52.wps.server.r.syntax.RAnnotationException;
import org.n52.wps.server.r.syntax.RAnnotationType;
import org.n52.wps.server.r.syntax.RAttribute;
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

    private static Logger LOGGER = LoggerFactory.getLogger(ScriptFileRepository.class);

    /** Maps current R-script files to identifiers **/
    private HashMap<File, String> fileToWknMap = new HashMap<File, String>();

    /** Maps each identifier to an R script file **/
    private HashMap<String, File> wknToFileMap = new HashMap<String, File>();

    /** caches conflicts for the wkn-Rscript mapping until resetWknFileMapping is invoked **/
    private HashMap<String, ExceptionReport> wknConflicts = new HashMap<String, ExceptionReport>();

    @Autowired
    private RAnnotationParser annotationParser;

    @Autowired
    R_Config config;

    public ScriptFileRepository() {
        LOGGER.info("NEW {}", this);
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
                        String wkn = config.getPublicScriptId(process_id);

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

    public void reset() {
        LOGGER.info("Resetting {}", this);

        this.wknToFileMap.clear();
        this.fileToWknMap.clear();
        this.wknConflicts.clear();
    }

}
