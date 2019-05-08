package ru.mail.polis.justaddacid;

import com.google.common.collect.Iterators;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.NavigableMap;
import java.util.TreeMap;

public class MemTable implements Table {

    private final NavigableMap<ByteBuffer, Value> map;
    private long tableSize;

    public MemTable() {
        map = new TreeMap<>();
    }

    /**
     * Get data iterator from memtable
     * @param from key to find data
     * @return data iterator
     */
    public final Iterator<Cell> iterator(@NotNull final ByteBuffer from) {
        return Iterators.transform(map.tailMap(from)
                        .entrySet().iterator(),
                e -> {
                    assert e != null;
                    return new Cell(e.getKey(), e.getValue());
                });
    }

    @Override
    public void upsert(@NotNull final ByteBuffer key, @NotNull final ByteBuffer value) {
        final Value prev = map.put(key, Value.of(value));
        if (prev == null) {
            tableSize = tableSize + key.remaining() + value.remaining();
        } else if (prev.isTombstone()) {
            tableSize = tableSize + value.remaining();
        } else {
            tableSize = tableSize + value.remaining() - prev.getData().remaining();
        }
    }

    @Override
    public void remove(@NotNull final ByteBuffer key) {
        final Value prev = map.put(key, Value.tombstone());
        if (prev == null) {
            tableSize = tableSize + key.remaining();
        } else if (!prev.isTombstone()) {
            tableSize = tableSize - prev.getData().remaining();
        }
    }

    @Override
    public long sizeInBytes() {
        return tableSize;
    }
}