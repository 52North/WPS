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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.r.data.R_Resource;
import org.n52.wps.server.r.syntax.ImportAnnotation;
import org.n52.wps.server.r.syntax.RAnnotation;
import org.n52.wps.server.r.syntax.RAnnotationException;
import org.n52.wps.server.r.syntax.RAttribute;
import org.n52.wps.server.r.syntax.ResourceAnnotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

/**
 * Management class to store and retrieve resources used by scripts.
 * 
 * @author Daniel Nüst
 *
 */
@Repository
public class ResourceFileRepository {

    private static Logger LOGGER = LoggerFactory.getLogger(ResourceFileRepository.class);

    @Autowired
    private R_Config config;

    /** Maps process to its resources **/
    private Multimap<String, R_Resource> processToResourcesMap = ArrayListMultimap.create();

    private Set<Path> resourceDirectories = new HashSet<Path>();

    /** Maps resource name to full file location (within one of the resource directories) and vice versa **/
    private Map<R_Resource, Path> resourcePaths = Maps.newHashMap();

    public ResourceFileRepository() {
        LOGGER.info("NEW {}", this);
    }

    private boolean addResource(R_Resource res, Path resourceDir, Path fullPath) {
        LOGGER.debug("Found resource {} in directory {}, full path: ", res, resourceDir, fullPath);
        if ( !resourcePaths.containsKey(res))
            this.resourcePaths.put(res, fullPath);
        else
            LOGGER.trace("Resource already stored '{}': {}", res, fullPath);
        this.processToResourcesMap.put(res.getProcessId(), res);

        return true;
    }

    public void addResourceDirectory(Path dir) {
        this.resourceDirectories.add(dir);
        LOGGER.info("Resource directory '{}' added, now have {}: {}",
                     dir,
                     resourceDirectories.size(),
                     Arrays.toString(resourceDirectories.toArray()));
    }

    public Collection<R_Resource> getR_Resourcce(Path path) throws ExceptionReport {
        if ( !Files.exists(path))
            throw new ExceptionReport("Resource file not found: " + path, ExceptionReport.NO_APPLICABLE_CODE);

        ListMultimap<Path, R_Resource> inverse = Multimaps.invertFrom(Multimaps.forMap(resourcePaths),
                                                                      ArrayListMultimap.<Path, R_Resource> create());
        return inverse.get(path);
    }

    public Path getResource(R_Resource resource) throws ExceptionReport {
        Path out = resourcePaths.get(resource);
        if (out != null && Files.exists(out) && out.isAbsolute() && out.toFile().canRead()) {
            return out;
        }
        String name = out == null ? "(unknown)" : out.toString();
        throw new ExceptionReport("Error for resource: " + resource + ", File " + name + " not found or broken.",
                                  ExceptionReport.NO_APPLICABLE_CODE);
    }

    /**
     * provide acces via script id and resource name, only for public resources.
     */
    public Path getResource(String scriptId, String resourceId) throws ExceptionReport {
        return getResource(new R_Resource(scriptId, resourceId, true));
    }

    public boolean isResourceAvailable(R_Resource resource) {
        try {
            Path p = getResource(resource);
            boolean out = Files.exists(p);
            return out;
        }
        catch (RuntimeException | ExceptionReport e) {
            LOGGER.warn("Resource is unavailable: {}", resource, e);
            return false;
        }
    }

    /**
     * register all resources from the annotation
     * 
     * @param rAnnotation
     * @return true if all resources could be found in the file system and are subsequently available in this
     *         repo, false otherwise.
     */
    @SuppressWarnings("unchecked")
    public boolean registerResources(RAnnotation rAnnotation) {
        if ( ! (rAnnotation instanceof ResourceAnnotation))
            return false;

        Collection<R_Resource> resources = null;
        try {
            resources = (Collection<R_Resource>) rAnnotation.getObjectValue(RAttribute.NAMED_LIST);
        }
        catch (RAnnotationException e) {
            LOGGER.error("Could not get resoure list from annotation {}", rAnnotation);
            return false;
        }

        boolean allRegistered = true;
        for (R_Resource res : resources) {
            if (resourcePaths.containsKey(res))
                LOGGER.debug("Resource already registered, (quietly) not doing it again: {}", res);
            else {
                LOGGER.debug("Registering resource {} based on directories {}",
                             res,
                             Arrays.toString(resourceDirectories.toArray()));

                // see if resource is contained in one of the directories
                Path resourcePath = Paths.get(res.getResourceValue());

                boolean foundResourceInDir = false;
                for (Path resourceDirs : resourceDirectories) {
                    Path fullPath = resourceDirs.resolve(resourcePath);

                    if (Files.exists(fullPath)) {
                        foundResourceInDir = addResource(res, resourceDirs, fullPath);
                        break;
                    }
                }

                if ( !foundResourceInDir) {
                    LOGGER.warn("Could not find resource {} in any of the configured directories: {}",
                                res,
                                resourceDirectories);
                    allRegistered = false;
                }
            }
        }

        return allRegistered;
    }

    @SuppressWarnings("unchecked")
    public boolean registerImport(RAnnotation rAnnotation, Path scriptDirectory) {
        if ( ! (rAnnotation instanceof ImportAnnotation))
            return false;

        Collection<R_Resource> imports = null;
        try {
            imports = (Collection<R_Resource>) rAnnotation.getObjectValue(RAttribute.NAMED_LIST);
        }
        catch (RAnnotationException e) {
            LOGGER.error("Could not get resoure list from annotation {}", rAnnotation);
            return false;
        }

        boolean allRegistered = true;
        for (R_Resource imprts : imports) {
            if (resourcePaths.containsKey(imprts))
                LOGGER.debug("Import already registered, (quietly) not doing it again: {}", imprts);
            else {
                // look in both the resource directories and the directory of the current file
                Set<Path> currentLookInDirectories = Sets.newHashSet(resourceDirectories);
                currentLookInDirectories.add(scriptDirectory);
                
                LOGGER.debug("Registering import {} as resource based on directories {}",
                             imprts,
                             Arrays.toString(currentLookInDirectories.toArray()));

                // see if resource is contained in one of the directories
                Path resourcePath = Paths.get(imprts.getResourceValue());

                boolean foundResourceInDir = false;
                for (Path resourceDirs : currentLookInDirectories) {
                    Path fullPath = resourceDirs.resolve(resourcePath);

                    if (Files.exists(fullPath)) {
                        foundResourceInDir = addResource(imprts, resourceDirs, fullPath);
                        break;
                    }
                }

                if ( !foundResourceInDir) {
                    LOGGER.warn("Could not find resource {} in any of the configured directories: {}",
                                imprts,
                                resourceDirectories);
                    allRegistered = false;
                }
            }
        }

        return allRegistered;
    }

    public void reset() {
        LOGGER.info("Resetting {}", this);

        this.resourceDirectories.clear();
        this.resourcePaths.clear();
    }

}
