package de.roland_illig.sffs.internal;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.stream.Stream;

public class Filesystem implements AutoCloseable {

    private final Storage storage;

    /**
     * @see RandomAccessFile#RandomAccessFile(File, String)
     */
    public Filesystem(File f, String mode) throws IOException {
        this.storage = new Storage(new RandomAccessFile(f, mode));
    }

    public void mkdir(Path dir) {
        var parent = locateDir(dir.getParent());
        parent.mkdir(dir.getFileName().toString());
    }

    public void rmdir(Path dir) {
        throw new UnsupportedOperationException();
    }

    public Stream<DirectoryEntry> readdir(Path dir) {
        throw new UnsupportedOperationException();
    }

    public void rename(Path path, String newName) {
        throw new UnsupportedOperationException();
    }

    public void move(Path oldPath, Path newPath) {
        throw new UnsupportedOperationException();
    }

    public void delete(Path file) {
        throw new UnsupportedOperationException();
    }

    public RegularFile open(Path file) {
        throw new UnsupportedOperationException();
    }

    public void close() throws IOException {
        storage.close();
    }

    private Directory locateDir(Path dir) {
        throw new UnsupportedOperationException();
    }
}
