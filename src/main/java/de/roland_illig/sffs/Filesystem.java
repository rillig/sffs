package de.roland_illig.sffs;

import de.roland_illig.sffs.internal.Api;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public interface Filesystem extends AutoCloseable {

    static Filesystem open(File file, String mode) throws IOException {
        return Api.open(file, mode);
    }

    void close() throws IOException;

    void rename(Path path, String newName) throws IOException;

    void move(Path oldPath, Path newPath) throws IOException;

    void mkdir(Path dir) throws IOException;

    void rmdir(Path dir) throws IOException;

    OpenFile open(Path file, String mode) throws IOException;

    void delete(Path file) throws IOException;
}
