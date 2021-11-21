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

    void readFully(int pos, byte[] buf, int bufOffset, int bufLength) throws IOException {
        SffsUtil.require(bufLength >= 0);
        SffsUtil.require(U.le(pos, getSize()));
        SffsUtil.require(U.le(pos + bufLength, getSize()));

        storage.readFully(offset(pos), buf, bufOffset, bufLength);
    }

    long readLong(int pos) throws IOException {
        return storage.readLong(offset(pos));
    }

    long readRef(int pos) throws IOException {
        return readLong(pos);
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

    void writeLong(int pos, long v) throws IOException {
        storage.writeLong(offset(pos), v);
    }

    Block ref(long ref) throws IOException {
        return new Block(storage, 16 * ref);
    }

    private long offset(int pos) {
        return U.plus(offset + 8, pos);
    }

    Storage getStorage() {
        return storage;
    }

    public Block checkType(BlockType type) throws IOException {
        SffsUtil.require(type == getType());
        return this;
    }
}
