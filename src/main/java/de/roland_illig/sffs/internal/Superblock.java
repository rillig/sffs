package de.roland_illig.sffs.internal;

import java.io.IOException;

final class Superblock {

    private final Block block;

    Superblock(Storage storage) throws IOException {
        this.block = new Block(storage, 0).checkType(BlockType.SUPER);
    }

    static void init(StorageWriter wr, long rootDirectoryRef, long firstFreeRef) throws IOException {
        wr.writeInt(BlockType.SUPER.getMagic());
        wr.writeInt(16);
        wr.writeRef(rootDirectoryRef);
        wr.writeRef(firstFreeRef);
        wr.writePadding();
    }

    void setRootDirectory(Block block) throws IOException {
        this.block.writeRef(0, block);
    }

    Directory getRootDirectory() throws IOException {
        return new Directory(block.ref(getRootDirectoryRef()));
    }

    long getRootDirectoryRef() throws IOException {
        return block.readRef(0);
    }
}
