package de.roland_illig.sffs.internal;

import java.io.IOException;

final class StorageWriter {

    private final Storage storage;
    private long pos;

    StorageWriter(Storage storage, long pos) {
        this.storage = storage;
        this.pos = pos;
    }

    void writeInt(int v) throws IOException {
        storage.writeInt(pos, v);
        pos += 4;
    }

    void writeLong(long v) throws IOException {
        storage.writeLong(pos, v);
        pos += 8;
    }

    void write(byte[] b, int off, int len) throws IOException {
        storage.write(pos, b, off, len);
        pos += len;
    }

    void writePadding() throws IOException {
        var col = (int) (pos & 0x0F);
        if (col != 0) write(new byte[16], 0, 16 - col);
    }
}
