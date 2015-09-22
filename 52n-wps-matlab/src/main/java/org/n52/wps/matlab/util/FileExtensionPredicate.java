/**
 * Copyright (C) 2013-2015 52°North Initiative for Geospatial Open Source
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
package org.n52.wps.matlab.util;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class FileExtensionPredicate implements Predicate<Path> {
    private final Set<String> extensions;

    public FileExtensionPredicate(Iterable<String> extensions) {
        Objects.requireNonNull(extensions);
        this.extensions = StreamSupport.stream(extensions.spliterator(), false)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean test(Path file) {
        String fileName = file.getFileName().toString();
        int idx = fileName.lastIndexOf('.');
        String ext = (idx < 0) ? "" : fileName.substring(idx + 1);
        return extensions.contains(ext);
    }

    @Override
    public String toString() {
        return this.extensions.stream().collect(Collectors
                .joining(", ", "FileExtensionPredicate.of(", ")"));
    }

    public static Predicate<Path> of(String... extensions) {
        return of(Arrays.asList(extensions));
    }

    public static Predicate<Path> of(Iterable<String> extensions) {
        return new FileExtensionPredicate(extensions);
    }

}
