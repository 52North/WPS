/**
 * ﻿Copyright (C) 2013 - 2014 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.matlab;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.IAlgorithmRepository;

import com.github.autermann.wps.commons.description.ows.OwsCodeType;

import org.n52.wps.matlab.description.MatlabProcessDescription;
import org.n52.wps.matlab.util.FileExtensionPredicate;

import com.github.autermann.yaml.Yaml;
import com.github.autermann.yaml.YamlNode;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public class MatlabAlgorithmRepository implements IAlgorithmRepository {
    private static final Logger LOG = LoggerFactory
            .getLogger(MatlabAlgorithmRepository.class);

    public static final String CONFIG_PROPERTY = "config";
    private final Yaml yaml = new Yaml();
    private final Map<OwsCodeType, MatlabAlgorithm> descriptions;

    public MatlabAlgorithmRepository() {
        this(getProperties());
    }

    public MatlabAlgorithmRepository(Map<String, List<String>> properties) {
        this.descriptions = getAlgorithms(properties);
    }

    private Map<OwsCodeType, MatlabAlgorithm> getAlgorithms(Map<String, List<String>> properties) {
        return getAlgorithms(Optional.ofNullable(properties.get(CONFIG_PROPERTY))
                    .orElseGet(Collections::emptyList));
    }

    private Map<OwsCodeType, MatlabAlgorithm> getAlgorithms(List<String> properties) {
        return getConfigPaths(properties)
                .map(MatlabProcessDescription::load)
                .peek(description ->  LOG.info("Loaded Matlab process: {}", description.getId()))
                .collect(toMap(MatlabProcessDescription::getId, MatlabAlgorithm::new));
    }

    private Stream<YamlNode> getConfigPaths(List<String> properties) {
        return properties.stream()
                .map(this::asURI)
                .filter(Objects::nonNull)
                .flatMap(this::open)
                .flatMap(this.yaml::loadStream);
    }

    private Stream<InputStream> open(URI uri) {
        switch (uri.getScheme()) {
            case "file":
                return getFileStreams(Paths.get(uri));
            case "classpath":
                String path = uri.getPath();
                if (path == null) {
                    path = uri.getSchemeSpecificPart();
                }
               return getClassPathStreams(path);
            default:
                try {
                    return Stream.of(uri.toURL().openStream());
                } catch (IOException ex) {
                    return Stream.empty();
                }
        }
    }

    private URI asURI(String property) {
        try {
            URI uri = new URI(property);
            if (uri.getScheme() == null) {
                return new URI("file://" + property);
            } else {
                return uri;
            }
        } catch (URISyntaxException ex) {
            LOG.warn("Unknown URI <" + property + ">", ex);
            return null;
        }
    }

    private Stream<InputStream> getFileStreams(Path path) {
        return getPaths(path)
                .filter(FileExtensionPredicate.of("yaml", "yml"))
                .distinct()
                .filter(Objects::nonNull)
                .filter(Files::exists)
                .map(p -> {
            try {
                return Files.newInputStream(p);
            } catch (IOException ex) {
                LOG.info("Can't open <" + path + ">", ex);
                return null;
            }
        }).filter(Objects::nonNull);
    }

    private Stream<Path> getPaths(Path path) {
        if (!Files.exists(path)) {
            return Stream.empty();
        } else if (Files.isRegularFile(path)) {
            return Stream.of(path);
        } else if (Files.isDirectory(path)) {
            try {
                return Files.walk(path).filter(Files::isRegularFile);
            } catch (IOException ex) {
                LOG.error("Could not access path at <" + path + ">", ex);
                return Stream.empty();
            }
        } else {
            return Stream.empty();
        }
    }

    private Stream<InputStream> getClassPathStreams(String resource) {
        return Stream.of(getClassPathStream(resource)).filter(Objects::nonNull);
    }

    private InputStream getClassPathStream(String resource) {
        InputStream stream;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader != null) {
            stream = classLoader.getResourceAsStream(resource);
            if (stream != null) {
                return stream;
            }
        }
        classLoader = MatlabAlgorithmRepository.class.getClassLoader();
        if (classLoader != null) {
            stream = classLoader.getResourceAsStream(resource);
            if (stream != null) {
                return stream;
            }
        }
        classLoader = ClassLoader.getSystemClassLoader();
        if (classLoader != null) {
            stream = classLoader.getResourceAsStream(resource);
            if (stream != null) {
                return stream;
            }
        }
        LOG.warn("Can not load resource <{}>", resource);
        return null;
    }

    @Override
    public Collection<String> getAlgorithmNames() {
        return descriptions.keySet().stream()
                .map(OwsCodeType::getValue)
                .collect(toCollection(LinkedList::new));
    }

    @Override
    public IAlgorithm getAlgorithm(String name) {
        return getOptionalAlgorithm(name).orElse(null);
    }

    private Optional<MatlabAlgorithm> getOptionalAlgorithm(String name) {
        return Optional.ofNullable(descriptions.get(asOwsCodeType(name)));
    }

    @Override
    public ProcessDescriptionType getProcessDescription(String name) {
        return getOptionalAlgorithm(name)
                .map(IAlgorithm::getDescription).orElse(null);
    }

    @Override
    public boolean containsAlgorithm(String name) {
        return this.descriptions.containsKey(asOwsCodeType(name));
    }

    private OwsCodeType asOwsCodeType(String name) {
        return (name == null || name.isEmpty()) ? null : new OwsCodeType(name);
    }

    @Override
    public void shutdown() {
        /* noop */
    }

    private static Map<String, List<String>> getProperties() {
        String name = MatlabAlgorithmRepository.class.getCanonicalName();
        return Optional.of(getConfig().getPropertiesForRepositoryClass(name))
                .map(Arrays::stream)
                .orElseGet(Stream::empty)
                .filter(Property::getActive)
                .collect(groupingBy(Property::getName,
                                    mapping(Property::getStringValue, toList())));
    }

    private static WPSConfig getConfig() {
        return WPSConfig.getInstance();
    }
}
