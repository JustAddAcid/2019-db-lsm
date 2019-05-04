package ru.mail.polis.justAddAcid;

import java.nio.ByteBuffer;
import java.util.Comparator;

public class Row {

    public static final Comparator <Row> COMPARATOR = Comparator.comparing(Row::getKey).thenComparing(Row::getRowValue);

    private final ByteBuffer key;
    private final RowValue rowValue;

    public Row(ByteBuffer key, RowValue rowValue) {
        this.key = key;
        this.rowValue = rowValue;
    }

    public ByteBuffer getKey() {
        return key;
    }

    public RowValue getRowValue() {
        return rowValue;
    }

    @Override
    public String toString(){
        return this.getKey().toString() + "||" + this.getRowValue().toString();
    }
}
