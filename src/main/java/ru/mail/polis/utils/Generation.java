package ru.mail.polis.utils;

import java.io.File;
import java.nio.file.Path;

public final class Generation {

    private Generation(){}

    public static long fromPath(final Path path){
        return fromFileName(path.getFileName().toString());
    }

    public static long fromFile(final File file){
        return fromFileName(file.getName());
    }

    public static long fromFileName(final String fileName){
        return Long.parseLong(fileName.replaceAll("[^0-9]", ""));
    }
}
