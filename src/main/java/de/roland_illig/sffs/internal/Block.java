package de.roland_illig.sffs.internal;

import java.io.IOException;

final class Block {

    private final Storage storage;
    private final long offset;

    private int type;
    private int size;

    Block(Storage storage, long offset) {
        this.storage = storage;
        this.offset = offset;
    }

    Block(Block block, long offset) {
        this(block.storage, offset);
    }

    int getType() throws IOException {
        if (type == 0) type = storage.readInt(offset);
        return type;
    }

    int getSize() throws IOException {
        if (size == 0) size = storage.readInt(offset + 4);
        return size;
    }

    void readFully(int pos, byte[] buf, int bufOffset, int bufLength) throws IOException {
        SffsUtil.require(bufLength >= 0);
        SffsUtil.require(U.le(pos, getSize()));
        SffsUtil.require(U.le(pos + bufLength, getSize()));

        storage.readFully(offset(pos), buf, bufOffset, bufLength);
    }

    long readBlockOffset(int pos) throws IOException {
        return storage.readLong(pos);
    }

    void write(int pos, byte[] buf, int bufOffset, int bufLength) throws IOException {
        storage.write(offset(pos), buf, bufOffset, bufLength);
    }

    void writeBlockRef(int pos, Block block) throws IOException {
        storage.writeLong(offset(pos), block != null ? block.offset / 16 : 0);
    }

    void writeInt(int pos, int v) throws IOException {
        storage.writeInt(offset(pos), v);
    }

    void writeLong(int pos, int v) throws IOException {
        storage.writeLong(offset(pos), v);
    }

    private long offset(int pos) {
        return U.plus(offset + 8, pos);
    }
}
