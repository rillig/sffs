package de.roland_illig.sffs.internal;

import java.io.IOException;

class Superblock {

    private final Block block;

    Superblock(Block block) {
        this.block = block;
    }

    public static void init(StorageWriter wr, long rootDirectory) throws IOException {
        wr.writeInt(BlockType.SUPER.getMagic());
        wr.writeInt(16);
        wr.writeLong(1); // root directory
        wr.writeLong(0); // first free block
    }

    void setRootDirectory(Directory dir) throws IOException {
        block.writeBlockRef(0, dir.block);
    }

    Directory getRootDirectory() throws IOException {
        var offset = block.readBlockOffset(0);
        return new Directory(new Block(block, offset));
    }
}
