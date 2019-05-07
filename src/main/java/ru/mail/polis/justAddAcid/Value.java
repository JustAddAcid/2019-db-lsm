package ru.mail.polis.justAddAcid;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public final class Value implements Comparable<Value> {

    private final long timestamp;
    private final ByteBuffer data;
    private final boolean isTombstone;

    public Value(final long timestamp, final ByteBuffer data, boolean isTombstone) {
        assert (timestamp >= 0);
        this.timestamp = timestamp;
        this.data = data;
        this.isTombstone = isTombstone;
    }

    public static Value of(final ByteBuffer data) {
        return new Value(System.currentTimeMillis(), data.duplicate(), false);
    }

    public static Value tombstone() {
        return new Value(System.currentTimeMillis(), null, true);
    }

    public boolean isTombstone() {
        return isTombstone;
    }

    public ByteBuffer getData() {
        if (data == null) {
            throw new IllegalArgumentException("");
        }
        return data.asReadOnlyBuffer();
    }

    @Override
    public int compareTo(@NotNull Value o) {
        return -Long.compare(timestamp, o.timestamp);
    }

    public long getTimestamp() {
        return timestamp;
    }
}
