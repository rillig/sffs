package de.roland_illig.sffs.internal;

import java.io.IOException;

class Block {
    private final Storage storage;
    private final long offset;

    private int type;
    private int size;

    Block(Storage storage, long offset) {
        this.storage = storage;
        this.offset = offset;
    }

    final int getType() throws IOException {
        if (type == 0) type = storage.readInt(offset);
        return type;
    }

    final int getSize() throws IOException {
        if (size == 0) size = storage.readInt(offset + 4);
        return size;
    }

    final void readFully(int pos, byte[] buf, int bufOffset, int bufLength) throws IOException {
        SffsUtil.require(bufLength >= 0);
        SffsUtil.require(U.le(pos, getSize()));
        SffsUtil.require(U.le(pos + bufLength, getSize()));

        storage.readFully(U.plus(this.offset + 8, pos), buf, bufOffset, bufLength);
    }

    long readBlockOffset(int pos) throws IOException {
        return storage.readLong(pos);
    }

    void write(int pos, byte[] buf, int bufOffset, int bufLength) throws IOException {
        storage.write(U.plus(this.offset + 8, pos), buf, bufOffset, bufLength);
    }

    void writeBlockRef(int pos, Block block) throws IOException {
        storage.writeLong(U.plus(this.offset + 8, pos), block.offset / 16);
    }
}
