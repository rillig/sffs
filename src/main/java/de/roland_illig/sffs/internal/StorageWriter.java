package de.roland_illig.sffs.internal;

import java.io.IOException;

class StorageWriter {

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
}
