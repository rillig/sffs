package de.roland_illig.sffs;

import java.io.IOException;

public interface OpenFile extends AutoCloseable {
    int read(byte[] buf, int off, int len) throws IOException;

    void write(byte[] buf, int off, int len) throws IOException;

    void append(byte[] buf, int i, int length) throws IOException;

    void seek(long offset);

    long tell();

    @Override
    void close() throws IOException;
}
