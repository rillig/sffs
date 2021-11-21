package de.roland_illig.sffs.internal;

import java.nio.file.Path;
import java.util.stream.Stream;

public class Filesystem {

    public void mkdir(Path dir) {
        throw new UnsupportedOperationException();
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
}
