package de.roland_illig.sffs.internal;

import java.io.IOException;

final class Superblock {

    private final Block block;

    Superblock(Block block) throws IOException {
        this.block = block.checkType(BlockType.SUPER);
    }

    static void init(StorageWriter wr, long rootDirectory, long firstFree) throws IOException {
        wr.writeInt(BlockType.SUPER.getMagic());
        wr.writeInt(16);
        wr.writeLong(rootDirectory);
        wr.writeLong(firstFree);
        wr.writePadding();
    }

    void setRootDirectoryRef(Block block) throws IOException {
        this.block.writeRef(0, block);
    }

    long getRootDirectoryRef() throws IOException {
        return block.readRef(0);
    }

    Directory getRootDirectory() throws IOException {
        return new Directory(block.ref(getRootDirectoryRef()));
    }
}
