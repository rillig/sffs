package de.roland_illig.sffs.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.stream.Stream;

final class Filesystem implements AutoCloseable {

    private final Storage storage;

    /**
     * @see RandomAccessFile#RandomAccessFile(File, String)
     */
    Filesystem(File f, String mode) throws IOException {
        this.storage = new Storage(new RandomAccessFile(f, mode));
    }

    void mkdir(Path dir) throws IOException {
        var parent = lookupParent(dir);
        if (parent == null) throw new FileNotFoundException(dir.getParent().toString());
        parent.mkdir(dir);
    }

    void rmdir(Path dir) {
        throw new UnsupportedOperationException();
    }

    Stream<DirectoryEntry> readdir(Path dir) {
        throw new UnsupportedOperationException();
    }

    void rename(Path path, String newName) {
        throw new UnsupportedOperationException();
    }

    void move(Path oldPath, Path newPath) {
        throw new UnsupportedOperationException();
    }

    void delete(Path file) {
        throw new UnsupportedOperationException();
    }

    RegularFile open(Path file) {
        throw new UnsupportedOperationException();
    }

    public void close() throws IOException {
        storage.close();
    }

    private Directory lookupParent(Path path) throws IOException {
        var superblock = new Superblock(storage);
        var cwd = superblock.getRootDirectory();
        for (int i = 0, n = path.getNameCount() - 1; cwd != null && i < n; i++)
            cwd = cwd.lookupDir(path.getName(i).toString());
        return cwd;
    }
}
