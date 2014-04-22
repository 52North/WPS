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
package com.github.autermann.wps.matlab.util;

import java.io.File;
import java.util.Arrays;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

public class FileExtensionPredicate implements Predicate<File> {

    private final Set<String> extensions;

    public FileExtensionPredicate(Iterable<? extends String> extensions) {
        Preconditions.checkNotNull(extensions);
        this.extensions = Sets.newHashSet(extensions);
    }

    public boolean apply(File file) {
        return extensions.contains(Files.getFileExtension(file.getName()));
    }

    public static Predicate<File> of(String... extensions) {
        return of(Arrays.asList(extensions));
    }

    public static Predicate<File> of(Iterable<? extends String> extensions) {
        return new FileExtensionPredicate(extensions);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("FileExtensionPredicate.of(" );
        Joiner.on(", ").appendTo(sb, extensions);
        return sb.append(")").toString();
    }

}
