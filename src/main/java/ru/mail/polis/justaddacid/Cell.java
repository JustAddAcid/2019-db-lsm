package ru.mail.polis.justaddacid;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.Comparator;

public class Cell {

    static final Comparator<Cell> COMPARATOR = Comparator
            .comparing(Cell::getKey)
            .thenComparing(Cell::getValue)
            .thenComparing(Cell::getGeneration, Comparator.reverseOrder());

    private final ByteBuffer key;
    private final Value value;
    private final long generation;

    /**
     * Create instance of Cell.
     *
     * @param key uniquely identifies the record
     * @param value record data
     * @param generation version of data
     */
    public Cell(@NotNull final ByteBuffer key, @NotNull final Value value, final long generation) {
        this.key = key;
        this.value = value;
        this.generation = generation;
    }

    public ByteBuffer getKey() {
        return key;
    }

    Value getValue() {
        return value;
    }

    public long getGeneration() {
        return generation;
    }
}
