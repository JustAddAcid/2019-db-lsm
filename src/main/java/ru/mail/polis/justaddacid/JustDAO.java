package ru.mail.polis.justaddacid;

import com.google.common.collect.Iterators;
import org.jetbrains.annotations.NotNull;
import ru.mail.polis.DAO;
import ru.mail.polis.Iters;
import ru.mail.polis.Record;
import ru.mail.polis.utils.Generation;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class JustDAO implements DAO {

    public static final String SUFFIX_DAT = ".dat";
    public static final String SUFFIX_TMP = ".tmp";
    public static final String PREFIX_FILE = "TABLE";

    private final File file;
    private final long flushLimit;
    private MemTable memTable;
    private final List<SSTable> ssTables;
    private long generation;
    private static final int TABLES_LIMIT = 9;

    /**
     * Create persistence DAO.
     *
     * @param file database location
     * @param flushLimit when is reached -- write to disk
     * @throws IOException if I/O error
     */
    public JustDAO(@NotNull final File file, final long flushLimit) throws IOException {
        this.file = file;
        assert flushLimit >= 0L;
        this.flushLimit = flushLimit;
        ssTables = new ArrayList<>();

        try(Stream<Path> walk = Files.walk(file.toPath(), 1)){
            walk.filter(path -> {
                final String filename = path.getFileName().toString();
                return filename.endsWith(SUFFIX_DAT) && filename.startsWith(PREFIX_FILE);
            })
            .forEach(path -> {
                try {
                    final long currGeneration = Generation.fromPath(path);
                    if (currGeneration > generation){
                        generation = currGeneration;
                    }
                    ssTables.add(new SSTable(path.toFile(), currGeneration));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        generation ++;
        memTable = new MemTable(generation);
    }

    @NotNull
    @Override
    public Iterator<Record> iterator(@NotNull final ByteBuffer from) {
        return Iterators.transform(aliveCells(from), cell -> {
            assert cell != null;
            return Record.of(cell.getKey(), cell.getValue().getData());
        });
    }

    private Iterator<Cell> aliveCells(@NotNull final ByteBuffer from){
        final List<Iterator<Cell>> iterators = new ArrayList<>();
        for (final SSTable ssTable : this.ssTables) {
            iterators.add(ssTable.iterator(from));
        }

        iterators.add(memTable.iterator(from));
        final Iterator<Cell> cellIterator = Iters.collapseEquals(
                Iterators.mergeSorted(iterators, Cell.COMPARATOR),
                Cell::getKey
        );

        return Iterators.filter(
                cellIterator, cell -> {
                    assert cell != null;
                    return !cell.getValue().isTombstone();
                }
        );
    }

    @Override
    public void upsert(@NotNull final ByteBuffer key, @NotNull final ByteBuffer value) throws IOException {
        memTable.upsert(key, value);
        if (memTable.sizeInBytes() >= flushLimit) {
            flush();
        }
    }

    @Override
    public void remove(@NotNull final ByteBuffer key) throws IOException {
        memTable.remove(key);
        if (memTable.sizeInBytes() >= flushLimit) {
            flush();
        }
    }

    @Override
    public void close() throws IOException {
        if (memTable.sizeInBytes() > 0) {
            flush();
        }
    }

    private void flush() throws IOException {
        if (ssTables.size() > TABLES_LIMIT){
            compact();
            return;
        }

        final String tempFilename = PREFIX_FILE + generation + SUFFIX_TMP;
        final String filename = PREFIX_FILE + generation + SUFFIX_DAT;

        final File tmp = new File(file,  tempFilename);
        SSTable.writeToFile(memTable.iterator(ByteBuffer.allocate(0)), tmp);
        final File dest = new File(file, filename);
        Files.move(tmp.toPath(), dest.toPath(), StandardCopyOption.ATOMIC_MOVE);
        generation ++;
        memTable = new MemTable(generation);
    }

    @Override
    public void compact() throws IOException {
        final String tempFilename = PREFIX_FILE + generation + SUFFIX_TMP;
        final String filename = PREFIX_FILE + generation + SUFFIX_DAT;
        final File tmp = new File(file,  tempFilename);

        final Iterator<Cell> cellIterator = aliveCells(ByteBuffer.allocate(0));
        SSTable.writeToFile(cellIterator, tmp);
        final File dest = new File(file, filename);
        Files.move(tmp.toPath(), dest.toPath(), StandardCopyOption.ATOMIC_MOVE);

        for (final SSTable ssTable: ssTables){
            Files.delete(ssTable.getFile().toPath());
        }

        ssTables.clear();
        ssTables.add(new SSTable(dest, generation));
        generation ++;
        memTable = new MemTable(generation);
    }
}
