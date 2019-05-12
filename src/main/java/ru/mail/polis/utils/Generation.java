package ru.mail.polis.utils;

import ru.mail.polis.justaddacid.JustDAO;

import java.io.File;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Generation {
    private Generation(){}

    public static long fromPath(final Path path){
        return fromFileName(path.getFileName().toString());
    }

    public static long fromFile(final File file){
        return fromFileName(file.getName());
    }

    public static long fromFileName(final String fileName){
        Pattern regex = Pattern.compile(JustDAO.PREFIX_FILE + "(\\d+)" + JustDAO.SUFFIX_DAT);
        Matcher matcher = regex.matcher(fileName);
        if (matcher.find()){matcher.group(1);
            return Long.parseLong(matcher.group(1));
        }
        return -1L;
    }
}
