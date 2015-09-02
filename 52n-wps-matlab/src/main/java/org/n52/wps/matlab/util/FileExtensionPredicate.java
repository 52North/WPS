/*
 * Copyright (C) 2013 Christian Autermann
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
