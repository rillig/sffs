package de.roland_illig.sffs.internal;

import java.io.IOException;

final class Superblock {

    private final Block block;

    Superblock(Block block) {
        this.block = block;
    }

    static void init(StorageWriter wr, long rootDirectory, long firstFree) throws IOException {
        wr.writeInt(BlockType.SUPER.getMagic());
        wr.writeInt(16);
        wr.writeLong(rootDirectory);
        wr.writeLong(firstFree);
        wr.writePadding();
    }

    void setRootDirectory(Directory dir) throws IOException {
        block.writeBlockRef(0, dir.block);
    }

    Directory getRootDirectory() throws IOException {
        var offset = block.readBlockOffset(0);
        return new Directory(new Block(block, offset));
    }
}
