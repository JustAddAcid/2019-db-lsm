package ru.mail.polis.justAddAcid;


import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public final class RowValue implements Comparable <RowValue> {

    private final ByteBuffer data;
    private final long timestamp;
    private final boolean tombstone;

    public long getTimestamp() {
        return timestamp;
    }

    public ByteBuffer getData() {
        return data.asReadOnlyBuffer();
    }

    public static RowValue of(final ByteBuffer data) {
        return new RowValue(data, System.currentTimeMillis(), false);
    }

    public static RowValue deadCluster() {
        return new RowValue(null, System.currentTimeMillis(), true);
    }

    public RowValue(ByteBuffer data, long timestamp, boolean isDead) {
        this.data = data;
        this.timestamp = timestamp;
        this.tombstone = isDead;
    }

    public boolean isTombstone() {
        return tombstone;
    }

    @Override
    public int compareTo(@NotNull RowValue o) {
        return -Long.compare(timestamp, o.timestamp);
    }

    @Override
    public String toString() {
        if (data != null){
            return "value ||" + timestamp + "||" + tombstone;
        } else {
            return "null ||" + timestamp + "||" + tombstone;
        }

    }
}
