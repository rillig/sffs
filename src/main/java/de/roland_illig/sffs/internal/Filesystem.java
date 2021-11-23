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
        var parent = lookup(dir, -1);
        if (parent == null) throw new FileNotFoundException(dir.getParent().toString());
        parent.mkdir(dir);
    }

    void rmdir(Path dir) throws IOException {
        var d = lookup(dir, 0);
        if (d == null) throw new FileNotFoundException(dir.toString());
        d.remove(dir);
    }

    Stream<DirectoryEntry> readdir(Path dir) {
        throw new UnsupportedOperationException();
    }

    void rename(Path path, String newName) throws IOException {
        var dir = lookup(path, -1);
        if (dir == null) throw new FileNotFoundException(path.toString());
        dir.rename(path, newName);
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

    private Directory lookup(Path path, int rtrim) throws IOException {
        var superblock = new Superblock(storage);
        var cwd = superblock.getRootDirectory();
        for (int i = 0, n = path.getNameCount() + rtrim; cwd != null && i < n; i++)
            cwd = cwd.lookupDir(path.getName(i).toString());
        return cwd;
    }
}
