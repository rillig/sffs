package de.roland_illig.sffs;

class Block {
    private final Storage storage;
    private final long offset;

    private int type;
    private int size;

    Block(Storage storage, long offset) {
        this.storage = storage;
        this.offset = offset;
    }

    final int getType() {
        if (type == 0) type = storage.readIntAt(offset);
        return type;
    }

    final int getSize() {
        if (size == 0) size = storage.readIntAt(offset + 4);
        return size;
    }

    final void readFully(int pos, byte[] buf, int bufOffset, int bufLength) {
        SffsUtil.require(bufLength >= 0);
        SffsUtil.require(SffsUtil.ule(pos, getSize()));
        SffsUtil.require(SffsUtil.ule(pos + bufLength, getSize()));

        storage.readFully(SffsUtil.uplus(this.offset + 8, pos), buf, bufOffset, bufLength);
    }
}
