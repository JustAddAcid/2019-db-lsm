package ru.mail.polis.utils;

import java.io.File;
import java.nio.file.Path;

public class Generation {

    private Generation(){}

    public static long fromPath(Path path){
        return fromFileName(path.getFileName().toString());
    }

    public static long fromFile(File file){
        return fromFileName(file.getName());
    }

    public static long fromFileName(String fileName){
        return Long.parseLong(fileName.replaceAll("[^0-9]", ""));
    }
}
