package ru.mail.polis.justaddacid;

import com.google.common.collect.Iterators;
import org.jetbrains.annotations.NotNull;
import ru.mail.polis.DAO;
import ru.mail.polis.Iters;
import ru.mail.polis.Record;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class JustDAO implements DAO {

    private static final String SUFFIX_DAT = ".dat";
    private static final String SUFFIX_TMP = ".tmp";

    private final File file;
    private final long flushLimit;
    private MemTable memTable;
    private final Collection<SSTable> ssTables;
    private int generation;

    /**
     * Create persistence DAO
     * @param file database location
     * @param flushLimit when is reached -- write to disk
     * @throws IOException if I/O error
     */
    public JustDAO(@NotNull final File file, final long flushLimit) throws IOException {
        this.file = file;
        assert flushLimit >= 0L;
        this.flushLimit = flushLimit;
        memTable = new MemTable();
        ssTables = new ArrayList<>();
        try(Stream<Path> walk = Files.walk(file.toPath(), 1)){
            walk.filter(path -> path
                    .getFileName()
                    .toString()
                    .endsWith(SUFFIX_DAT))
                    .forEach(path -> {
                try {
                    ssTables.add(new SSTable(path.toFile()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

    }

    @NotNull
    @Override
    public Iterator<Record> iterator(@NotNull final ByteBuffer from) {
        final List<Iterator<Cell>> iterators = new ArrayList<>();
        for (final SSTable ssTable : this.ssTables) {
            iterators.add(ssTable.iterator(from));
        }

        iterators.add(memTable.iterator(from));
        final Iterator<Cell> cellIterator = Iters.collapseEquals(
                Iterators.mergeSorted(iterators, Cell.COMPARATOR),
                Cell::getKey
        );
        final Iterator<Cell> alive = Iterators.filter(
                cellIterator, cell -> {
                    assert cell != null;
                    return !cell.getValue().isTombstone();
                }
        );
        return Iterators.transform(alive, cell -> {
            assert cell != null;
            return Record.of(cell.getKey(), cell.getValue().getData());
        });
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
        flush();
    }

    private void flush() throws IOException {
        final File tmp = new File(file, generation + SUFFIX_TMP);
        SSTable.writeToFile(memTable.iterator(ByteBuffer.allocate(0)), tmp);
        final File dest = new File(file, generation + SUFFIX_DAT);
        Files.move(tmp.toPath(), dest.toPath(), StandardCopyOption.ATOMIC_MOVE);
        generation = generation + 1;
        memTable = new MemTable();
    }
}
