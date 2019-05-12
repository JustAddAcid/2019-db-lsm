package ru.mail.polis.utils;

import java.nio.ByteBuffer;

public final class Bytes {
    private Bytes() {
    }

    /**
     * Get ByteBuffer from int.
     *
     * @param value int value to convert
     * @return ByteBuffer
     */
    public static ByteBuffer fromInt(final int value) {
        final ByteBuffer result = ByteBuffer.allocate(Integer.BYTES);
        result.putInt(value);
        result.rewind();
        return result;
    }

    /**
     * Get ByteBuffer from long.
     *
     * @param value long value to convert
     * @return ByteBuffer
     */
    public static ByteBuffer fromLong(final long value) {
        final ByteBuffer result = ByteBuffer.allocate(Long.BYTES);
        result.putLong(value);
        result.rewind();
        return result;
    }
}
