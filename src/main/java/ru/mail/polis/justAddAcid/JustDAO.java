package ru.mail.polis.justAddAcid;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.jetbrains.annotations.NotNull;

import ru.mail.polis.DAO;
import ru.mail.polis.Record;

public class JustDAO implements DAO {

    private final NavigableMap<ByteBuffer, Record> content = new TreeMap<>();

    @NotNull
    @Override
    public Iterator<Record> iterator(@NotNull ByteBuffer from) throws IOException {
        return content.tailMap(from).values().iterator();
    }

    @Override
    public void upsert(@NotNull ByteBuffer key, @NotNull ByteBuffer value) throws IOException {
        content.put(key, Record.of(key,value));
    }

    @Override
    public void remove(@NotNull ByteBuffer key) throws IOException {
        content.remove(key);
    }

    @Override
    public void close() throws IOException {
        // do nothing
    }
}
