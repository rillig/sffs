package de.roland_illig.sffs.internal;

import java.io.IOException;

final class Block {

    private final Storage storage;
    private final long offset;

    private int size;

    Block(Storage storage, long offset) throws IOException {
        this.storage = storage;
        this.offset = offset;

        getType(); // verify that the magic number is known
    }

    long getRef() {
        return offset / 16;
    }

    BlockType getType() throws IOException {
        return BlockType.byMagic(storage.readInt(offset));
    }

    int getSize() throws IOException {
        if (size == 0) size = storage.readInt(offset + 4);
        return size;
    }

    void readFully(int pos, byte[] buf, int off, int len) throws IOException {
        storage.readFully(offset(pos, len), buf, off, len);
    }

    long readRef(int pos) throws IOException {
        return storage.readLong(offset(pos, 8));
    }

    void write(int pos, byte[] buf, int off, int len) throws IOException {
        storage.write(offset(pos, len), buf, off, len);
    }

    void writeRef(int pos, Block block) throws IOException {
        writeRef(pos, block != null ? block.offset / 16 : 0);
    }

    void writeRef(int pos, long ref) throws IOException {
        storage.writeLong(offset(pos, 8), ref);
    }

    Block ref(long ref) throws IOException {
        return new Block(storage, 16 * ref);
    }

    private long offset(int pos, int len) throws IOException {
        var size = getSize();
        assert pos >= 0;
        assert len >= 0;
        assert pos + len >= 0;
        assert pos + len <= size;

        return U.plus(offset + 8, pos);
    }

    Storage getStorage() {
        return storage;
    }

    Block checkType(BlockType type) throws IOException {
        assert type == getType();
        return this;
    }

    void free() throws IOException {
        storage.free(offset);
    }
}
