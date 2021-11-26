package de.roland_illig.sffs.internal;

import java.io.IOException;

final class Block {

    final Storage storage;
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

    int read(int pos, byte[] buf, int off, int len) throws IOException {
        return storage.read(offset(pos, len), buf, off, len);
    }

    void readFully(int pos, byte[] buf, int off, int len) throws IOException {
        storage.readFully(offset(pos, len), buf, off, len);
    }

    int readInt(int pos) throws IOException {
        return storage.readInt(offset(pos, 4));
    }

    long readLong(int pos) throws IOException {
        return storage.readLong(offset(pos, 8));
    }

    long readRef(int pos) throws IOException {
        return storage.readRef(offset(pos, 8));
    }

    void write(int pos, byte[] buf, int off, int len) throws IOException {
        storage.write(offset(pos, len), buf, off, len);
    }

    void writeInt(int pos, int v) throws IOException {
        storage.writeInt(offset(pos, 4), v);
    }

    void writeLong(int pos, long v) throws IOException {
        storage.writeLong(offset(pos, 8), v);
    }

    void writeRef(int pos, Block obj) throws IOException {
        writeRef(pos, obj != null ? obj.offset / 16 : 0);
    }

    void writeRef(int pos, long ref) throws IOException {
        storage.writeRef(offset(pos, 8), ref);
    }

    Block ref(long ref) throws IOException {
        return new Block(storage, 16 * ref);
    }

    Block ref(long ref, BlockType type) throws IOException {
        return ref(ref).checkType(type);
    }

    private long offset(int pos, int len) throws IOException {
        SffsUtil.checkRange(pos, len, getSize());
        return U.plus(offset + 8, pos);
    }

    Block checkType(BlockType type) throws IOException {
        if (type != getType())
            throw new IllegalStateException("expecting " + type + ", got " + getType());
        return this;
    }

    void free() throws IOException {
        storage.free(offset);
    }
}
