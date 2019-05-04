package ru.mail.polis.justAddAcid;

import com.google.common.collect.Iterators;
import org.jetbrains.annotations.NotNull;
import ru.mail.polis.DAO;
import ru.mail.polis.Iters;
import ru.mail.polis.Record;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JustDAO implements DAO {

    private MemTable memTable;
    private final File baseDirectory;
    private List <SSTable> ssTables;

    public JustDAO(@NotNull final File baseDirectory, long flushLimit) throws IOException {
        this.baseDirectory = baseDirectory;
        this.memTable = new MemTable(this.baseDirectory, flushLimit);
        this.ssTables = new ArrayList<>();
        Files.walkFileTree(this.baseDirectory.toPath(), new SimpleFileVisitor<>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                ssTables.add(new SSTable(file.toFile()));
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private Iterator<Record> ssTablesIterator(@NotNull ByteBuffer from) {
        final List <Iterator<Row>> sstablesListIter = new ArrayList<>();
        for (SSTable ssTable : ssTables) {
            sstablesListIter.add(ssTable.iterator(from));
        }
        final Iterator <Row> sstablesIter = Iterators.mergeSorted(sstablesListIter, Row.COMPARATOR);

        

        final Iterator <Row> sstableCollapseIter = Iters.collapseEquals(sstablesIter, Row::getKey);
//        while (sstablesIter.hasNext()){
//            System.out.println(sstablesIter.next().toString());
//        }
//        System.out.println();
//        while (sstableCollapseIter.hasNext()){
//            System.out.println(sstableCollapseIter.next().toString());
//        }
        final Iterator <Row> aliveClusters = Iterators.filter(sstableCollapseIter, cluster-> {
            assert  cluster != null;
            return !cluster.getRowValue().isTombstone();
        });
//        Iterator<Record> ass = Iterators.transform(aliveClusters, cluster->{
//            assert  cluster != null;
//            return Record.of(cluster.getKey(), cluster.getRowValue().getData());
//        });
//        System.out.println();
//        while (ass.hasNext()){
//            System.out.println(ass.next().toString());
//        }
        return Iterators.transform(aliveClusters, cluster->{
            assert  cluster != null;
            return Record.of(cluster.getKey(), cluster.getRowValue().getData());
        });
    }

    private Iterator<Record> memTableIterator(@NotNull ByteBuffer from) throws IOException {
        /*
         * Get Alive Clusters form MemTable
         * */
        final Iterator <Row> clusters = memTable.iterator(from);
        final Iterator <Row> aliveClusters = Iterators.filter(clusters, cluster -> {
            assert cluster != null;
            return !cluster.getRowValue().isTombstone();
        });
        return Iterators.transform(aliveClusters, cluster -> {
            assert cluster != null;
            return Record.of(cluster.getKey(), cluster.getRowValue().getData());
        });
    }

    @NotNull
    @Override
    public Iterator<Record> iterator(@NotNull ByteBuffer from) throws IOException {
        final Iterator <Record> memtableIter = memTableIterator(from);
        final Iterator <Record> sstablesIter = ssTablesIterator(from);
        final List <Iterator<Record>> iteratorList = new ArrayList<>();
        iteratorList.add(memtableIter);
        iteratorList.add(sstablesIter);

        final Iterator <Record> iterator = Iterators.mergeSorted(iteratorList, Record::compareTo);
        return Iters.collapseEquals(iterator, Record::getKey);
    }

    @Override
    public void upsert(@NotNull ByteBuffer key, @NotNull ByteBuffer value) throws IOException {
        memTable.upsert(key, value);
    }

    @Override
    public void remove(@NotNull ByteBuffer key) throws IOException {
        memTable.remove(key);
    }

    @Override
    public void close() throws IOException {
        WriteToFileHelper.writeToFile(memTable.iterator(ByteBuffer.allocate(0)), baseDirectory, memTable.getGeneration());
    }
}
