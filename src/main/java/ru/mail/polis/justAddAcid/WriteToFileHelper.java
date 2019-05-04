package ru.mail.polis.justAddAcid;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WriteToFileHelper {

    private static final String FILE_NAME = "SSTable_";
    private static final String SUFFIX = ".txt";

    public static void writeToFile(Iterator<Row> clusters, File directory, long generation) throws IOException {
        try(FileChannel fileChannel = FileChannel.open(
                Path.of(directory.getAbsolutePath(), FILE_NAME+generation+SUFFIX), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE))
        {
            final List<Long> offsets = new ArrayList<>();
            long offset = 0;
            while (clusters.hasNext()) {
                offsets.add(offset);

                final Row row = clusters.next();

                // Write Key

                final ByteBuffer key = row.getKey();
                ByteBuffer keyDuplicate = key.duplicate();
                final int keySize = row.getKey().remaining();
                fileChannel.write(ToByteBuffer.fromInt(keySize));
                offset += Integer.BYTES; // 4 byte
                fileChannel.write(keyDuplicate);
                offset += keySize;

                // Value
                final RowValue value = row.getRowValue();

                // Write Timestamp
                if (value.isTombstone()) {
                    fileChannel.write(ToByteBuffer.fromLong(-row.getRowValue().getTimestamp()));
                } else {
                    fileChannel.write(ToByteBuffer.fromLong(row.getRowValue().getTimestamp()));
                }
                offset += Long.BYTES; // 8 byte

                // Write Value Size and Value

                if (!value.isTombstone()) {
                    final ByteBuffer valueData = value.getData();
                    final int valueSize = value.getData().remaining();
                    fileChannel.write(ToByteBuffer.fromInt(valueSize));
                    offset += Integer.BYTES; // 4 byte
                    fileChannel.write(valueData);
                    offset += valueSize;
                }
                //else - not write Value
            }
            // Write Offsets
            for (final Long anOffset : offsets) {
                fileChannel.write(ToByteBuffer.fromLong(anOffset));
            }
            //Cells
            fileChannel.write(ToByteBuffer.fromLong(offsets.size()));
        }
    }
    private WriteToFileHelper(){}
}
