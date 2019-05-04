package ru.mail.polis.justAddAcid;


import com.google.common.collect.Iterators;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;


public class MemTable {

    private static AtomicLong counter = new AtomicLong(0);
    private NavigableMap<ByteBuffer, RowValue> storage;
    private long tableSize;
    private File directory;
    private long generation;
    private long flushLimit;

    public MemTable(File directory, long flushLimit) {
        this.directory = directory;
        this.flushLimit = flushLimit;
        storage = new TreeMap<>();
    }

    @NotNull
    public Iterator<Row> iterator(@NotNull ByteBuffer from) throws IOException {
        return Iterators.transform(
                storage.tailMap(from).entrySet().iterator(), e -> {
                    assert e != null;
                    return new Row(e.getKey(), e.getValue());
                }
        );
    }

    public void upsert(@NotNull ByteBuffer key, ByteBuffer value) throws IOException {
        final RowValue prev = storage.put(key, RowValue.of(value));
        if(prev == null) {
            tableSize = tableSize + key.remaining() + value.remaining();
        }
        else if(prev.isTombstone()) {
            tableSize = tableSize + value.remaining();
        }
        else {
            tableSize = tableSize + value.remaining() - prev.getData().remaining();
        }
        if (tableSize >= flushLimit) {
            this.generation = counter.incrementAndGet();
            WriteToFileHelper.writeToFile(this.iterator(ByteBuffer.allocate(0)), directory, generation);
            storage = new TreeMap<>();
            tableSize = 0;
        }
    }

    public long getGeneration() {
        return generation;
    }

    public void remove(@NotNull ByteBuffer key) throws IOException {
        final RowValue prev = storage.put(key, RowValue.deadCluster());
        if (prev == null) {
            tableSize = tableSize + key.remaining();
        } else if (prev.isTombstone()) {

        } else {
            tableSize -= prev.getData().remaining();
        }
    }
}