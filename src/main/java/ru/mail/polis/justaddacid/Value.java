package ru.mail.polis.justaddacid;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public final class Value implements Comparable<Value> {

    private final long timestamp;
    private final ByteBuffer data;
    private final boolean isTombstone;

    /**
     * Creates instanse of Cell Value.
     *
     * @param timestamp when the data stored
     * @param data to store in value
     * @param isTombstone is the value is removed
     */
    public Value(final long timestamp, final ByteBuffer data, final boolean isTombstone) {
        assert timestamp >= 0;
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


    /**
     * Gets data from the Value.
     *
     * @return data contained in the Cell Value
     */
    public ByteBuffer getData() {
        if (data == null) {
            throw new IllegalArgumentException("");
        }
        return data.asReadOnlyBuffer();
    }

    @Override
    public int compareTo(final @NotNull Value o) {
        return -Long.compare(timestamp, o.timestamp);
    }

    public long getTimestamp() {
        return timestamp;
    }
}
