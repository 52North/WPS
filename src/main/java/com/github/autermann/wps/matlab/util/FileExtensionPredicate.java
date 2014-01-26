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
