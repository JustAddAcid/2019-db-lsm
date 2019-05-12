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

    /**
     * Get generation SSTable from filename.
     *
     * @param fileName name of SSTable file
     * @return generation number
     */
    public static long fromFileName(final String fileName){
        final Pattern regex = Pattern.compile(JustDAO.PREFIX_FILE + "(\\d+)" + JustDAO.SUFFIX_DAT);
        final Matcher matcher = regex.matcher(fileName);
        if (matcher.find()){
            return Long.parseLong(matcher.group(1));
        }
        return -1L;
    }
}
